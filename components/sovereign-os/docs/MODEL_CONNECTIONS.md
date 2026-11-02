# Model connections — no paid API requirement

SOVEREIGN has three distinct ways to use models. They have different privacy and networking properties.

## 1. Fully offline inference inside SOVEREIGN

1. Obtain a compatible .litertlm model file from a source whose model licence permits your use.
2. In AI Connections, choose Import .litertlm.
3. Select the model from the local model list and press Load.
4. Ask the embedded model. In this branch, inference uses LiteRT-LM's CPU backend and does not call a server or require an API key.

The model file is copied to SOVEREIGN's private app storage. It is not bundled in the APK. Keep in mind that model weights can be hundreds of MB or multiple GB. This build has not yet been benchmarked on the target phone; select a small quantized model and monitor RAM, temperature, storage and battery. GPU/NPU acceleration is not enabled in this first adapter.

Official runtime guide: https://developers.google.com/edge/litert-lm/android

## 2. Self-hosted local model server

Select one of the endpoint types:

- Ollama: base URL such as http://127.0.0.1:11434. SOVEREIGN calls /api/tags to list models and /api/chat to send a prompt.
- OpenAI-compatible local server: base URL such as http://127.0.0.1:1234/v1. SOVEREIGN calls /v1/models and /v1/chat/completions. Compatible examples include llama.cpp server, LM Studio, Jan and LocalAI when their OpenAI-compatible server is enabled.

This uses an HTTP/JSON API-shaped protocol, but the server is yours: no hosted model account, paid endpoint or API key is required for local inference. It is not the same as zero API traffic; it is simply a local protocol rather than a paid cloud API.

Important address rule:
- 127.0.0.1 means the Android phone itself.
- When the model server runs on a separate laptop/desktop, enter that computer's private LAN IP address, for example http://192.168.1.20:11434 or http://192.168.1.20:1234/v1. The example IP is illustrative; use the actual address of your computer.
- Plain HTTP is accepted by the app policy only for loopback and private/LAN IP ranges.
- Public HTTPS endpoints are accepted, but repository context is sent only after checking the per-endpoint consent box for that exact URL.

This connector currently does not support server authentication headers or API-key credentials. Use a trusted, isolated LAN and firewall rules. Do not expose an unauthenticated model server to the public Internet, do not port-forward its port, and do not assume a LAN endpoint is safe merely because it has a private address. The app does not automatically contact an endpoint; the user must press Test or Ask.

Official local-server references:
- Ollama API: https://docs.ollama.com/api
- LM Studio local server: https://lmstudio.ai/docs/app/api/endpoints/openai

## 3. Human-mediated handoff to ChatGPT or another installed app

1. Open Local Git and press Share context or Copy context.
2. Choose ChatGPT or another installed AI app from Android's Sharesheet, or paste the copied context in the target app.
3. Review what is being sent. Continue the conversation in that app.
4. To bring a response back, share/copy the text into SOVEREIGN. It is imported as a resource, not auto-applied as a code patch.

This uses no developer API, no API key and no paid integration. It is a manual bridge: SOVEREIGN cannot silently read or write a ChatGPT conversation, connect directly to its private backend, or synchronize chat history.

## Model-to-Git permission boundary

At this stage, a selected model can read a bounded text context built from the local repository's status, branch, recent commits, selected text files and diff. Obvious secret-named paths are excluded; common secret assignments are redacted. This is best-effort filtering, not guaranteed secret detection.

Model output is advisory text only. It cannot edit repository files, stage, commit, switch branches, push, merge or execute scripts. A later patch workflow will use:
1. proposed patch,
2. exact diff preview,
3. separate user approval,
4. apply to a branch,
5. test and inspect,
6. separate approval for any remote push or merge.

## Routes not available yet

- Embedded llama.cpp/GGUF runtime.
- Direct model-specific adapters beyond Ollama and OpenAI-compatible local endpoints.
- Authenticated endpoints and secret vault for bearer/API credentials.
- SSH and OAuth remote authentication, persistent credential vault, remote-branch checkout/tracking, automatic/background repository sync and conflict resolution.
- MCP, autonomous tool access or model write permission.
