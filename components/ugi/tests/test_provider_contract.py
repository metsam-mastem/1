import tempfile
import threading
import unittest
from pathlib import Path

from ugi.adapter import UgiLocalAdapter
from ugi.core import GitError
from ugi.provider import GitProvider
from ugi.server import make_server


class UgiLocalProviderContractTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.server = make_server("127.0.0.1", 0, Path(self.temp.name))
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()
        self.provider = UgiLocalAdapter(f"http://127.0.0.1:{self.server.server_port}")

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join(timeout=2)
        self.temp.cleanup()

    def test_adapter_implements_versioned_git_provider_protocol(self):
        self.assertIsInstance(self.provider, GitProvider)
        self.assertEqual(self.provider.provider_id, "ugi.local-http")

    def test_contract_repository_lifecycle_and_commit_history(self):
        created = self.provider.create_repository("contract-project")
        self.assertEqual(created["name"], "contract-project")
        initial = self.provider.status("contract-project")
        self.assertEqual(initial["branch"], "main")

        source = Path(self.temp.name) / "contract-project" / "README.md"
        source.write_text("provider contract v1\n", encoding="utf-8")
        self.provider.stage_all("contract-project")
        committed = self.provider.commit(
            "contract-project", "Add contract fixture", "UGI Test", "ugi@example.invalid"
        )
        self.assertEqual(len(committed["sha"]), 40)
        history = self.provider.log("contract-project")
        self.assertEqual(history[0]["subject"], "Add contract fixture")
        self.assertEqual(self.provider.status("contract-project")["files"], [])

    def test_contract_bundle_round_trip_preserves_repository_content(self):
        self.provider.create_repository("contract-source")
        source = Path(self.temp.name) / "contract-source" / "artifact.txt"
        source.write_text("portable git bundle\n", encoding="utf-8")
        self.provider.stage_all("contract-source")
        self.provider.commit("contract-source", "Create bundle source", "UGI Test", "ugi@example.invalid")

        bundle = Path(self.temp.name) / "contract.bundle"
        self.assertGreater(self.provider.export_bundle("contract-source", bundle), 0)
        imported = self.provider.import_bundle(bundle, "contract-import")
        self.assertEqual(imported["name"], "contract-import")
        copied = Path(self.temp.name) / "contract-import" / "artifact.txt"
        self.assertEqual(copied.read_text(encoding="utf-8"), "portable git bundle\n")
        self.assertEqual(self.provider.log("contract-import")[0]["subject"], "Create bundle source")

    def test_contract_rejects_bundle_import_into_existing_repository(self):
        self.provider.create_repository("occupied-target")
        invalid_bundle = Path(self.temp.name) / "invalid.bundle"
        invalid_bundle.write_bytes(b"# v2 git bundle\ninvalid payload")
        with self.assertRaises(GitError):
            self.provider.import_bundle(invalid_bundle, "occupied-target")
        self.assertTrue((Path(self.temp.name) / "occupied-target" / ".git").is_dir())

    def test_contract_rejects_invalid_bundle_without_creating_target(self):
        invalid_bundle = Path(self.temp.name) / "invalid.bundle"
        invalid_bundle.write_bytes(b"not a git bundle")
        with self.assertRaises(GitError):
            self.provider.import_bundle(invalid_bundle, "invalid-target")
        self.assertFalse((Path(self.temp.name) / "invalid-target").exists())

    def test_contract_health_and_repository_listing_are_provider_neutral(self):
        health = self.provider.health()
        self.assertEqual(health["status"], "ok")
        self.provider.create_repository("listed-project")
        names = [item["name"] for item in self.provider.repositories()]
        self.assertIn("listed-project", names)


if __name__ == "__main__":
    unittest.main()
