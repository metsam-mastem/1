package org.sovereignos

import android.content.Intent
import android.content.ClipData
import android.content.ClipboardManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.sovereignos.git.GitStatusSnapshot
import org.sovereignos.git.GitRemoteInfo
import org.sovereignos.git.GitWorkspace
import org.sovereignos.models.*
import org.sovereignos.storage.ResourceRecord
import org.sovereignos.storage.ResourceRepository
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var repository: ResourceRepository
    private lateinit var gitWorkspace: GitWorkspace
    private lateinit var endpointStore: ModelEndpointStore
    private lateinit var embeddedModelStore: EmbeddedModelStore
    private lateinit var embeddedRuntime: EmbeddedModelRuntime
    private val modelClient = LocalModelClient()
    private var resourceRevision by mutableIntStateOf(0)
    private var status by mutableStateOf("Local mode · no automatic data transmission")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        repository = ResourceRepository(filesDir, contentResolver)
        gitWorkspace = GitWorkspace(File(filesDir, "git-workspace"), "SOVEREIGN User", "local@sovereign.invalid")
        endpointStore = ModelEndpointStore(this)
        embeddedModelStore = EmbeddedModelStore(this)
        embeddedRuntime = EmbeddedModelRuntime(cacheDir)
        runCatching {
            if ("personal" !in gitWorkspace.repositoryNames()) gitWorkspace.createRepository("personal")
        }.onFailure { status = "Git workspace initialization failed: ${it.message}" }
        acceptIncoming(intent)

        setContent {
            MaterialTheme {
                val scope = rememberCoroutineScope()
                var page by remember { mutableIntStateOf(0) }
                var refresh by remember { mutableIntStateOf(0) }
                val records = remember(refresh, resourceRevision) { repository.list() }

                var repoName by remember { mutableStateOf("personal") }
                var repoNames by remember { mutableStateOf(gitWorkspace.repositoryNames()) }
                var gitStatus by remember { mutableStateOf<GitStatusSnapshot?>(null) }
                var branches by remember { mutableStateOf(emptyList<String>()) }
                var commits by remember { mutableStateOf(emptyList<org.sovereignos.git.GitCommitInfo>()) }
                var diff by remember { mutableStateOf("") }
                var newRepoName by remember { mutableStateOf("") }
                var filePath by remember { mutableStateOf("README.md") }
                var fileContent by remember { mutableStateOf("# My offline workspace") }
                var commitMessage by remember { mutableStateOf("Update local workspace") }
                var branchName by remember { mutableStateOf("feature/experiment") }
                var remoteName by remember { mutableStateOf("origin") }
                var remoteUrl by remember { mutableStateOf("") }
                var remoteUsername by remember { mutableStateOf("") }
                var remoteToken by remember { mutableStateOf("") }
                var remoteNames by remember { mutableStateOf(emptyList<GitRemoteInfo>()) }
                var remoteBranches by remember { mutableStateOf(emptyList<String>()) }
                var confirmPushRemote by remember { mutableStateOf<String?>(null) }

                var endpoints by remember { mutableStateOf(endpointStore.all()) }
                var embeddedModels by remember { mutableStateOf(embeddedModelStore.list()) }
                var activeEmbeddedModel by remember { mutableStateOf(embeddedModelStore.active()) }
                var embeddedReady by remember { mutableStateOf(false) }
                var embeddedStatus by remember { mutableStateOf("No embedded model loaded.") }
                var endpointName by remember { mutableStateOf("My local model") }
                var endpointUrl by remember { mutableStateOf("http://127.0.0.1:11434") }
                var provider by remember { mutableStateOf(ModelProvider.OLLAMA) }
                var selectedEndpointId by remember { mutableStateOf("") }
                var modelChoices by remember { mutableStateOf(emptyList<String>()) }
                var selectedModel by remember { mutableStateOf("") }
                var prompt by remember { mutableStateOf("Review this repository. Identify risks and propose a small, testable improvement. Do not modify files.") }
                var modelAnswer by remember { mutableStateOf("") }
                var connectionStatus by remember { mutableStateOf("No model has been contacted yet.") }
                var approvedRemoteEndpoint by remember { mutableStateOf("") }
                var isWorking by remember { mutableStateOf(false) }

                fun refreshGit() {
                    scope.launch {
                        isWorking = true
                        runCatching {
                            withContext(Dispatchers.IO) {
                                Triple(gitWorkspace.status(repoName), gitWorkspace.listBranches(repoName), gitWorkspace.recentCommits(repoName, 10))
                            }
                        }.onSuccess { (snapshot, branchList, logList) ->
                            gitStatus = snapshot; branches = branchList; commits = logList
                            diff = withContext(Dispatchers.IO) { gitWorkspace.diff(repoName) }
                            repoNames = gitWorkspace.repositoryNames()
                            remoteNames = withContext(Dispatchers.IO) { gitWorkspace.listRemotes(repoName) }
                            remoteBranches = withContext(Dispatchers.IO) { gitWorkspace.listRemoteBranches(repoName) }
                        }.onFailure { status = "Git: ${it.message}" }
                        isWorking = false
                    }
                }

                fun runRemoteOperation(targetRemote: String, push: Boolean) {
                    val targetRepository = repoName
                    val username = remoteUsername.trim().ifBlank { null }
                    val token = remoteToken.takeIf { it.isNotBlank() }?.toCharArray()
                    remoteToken = ""
                    scope.launch {
                        isWorking = true
                        runCatching {
                            withContext(Dispatchers.IO) {
                                if (push) gitWorkspace.push(targetRepository, targetRemote, username, token)
                                else gitWorkspace.fetch(targetRepository, targetRemote, username, token)
                            }
                        }.onSuccess {
                            status = it
                            refreshGit()
                        }.onFailure {
                            status = "Remote operation failed: ${it.message}"
                        }
                        isWorking = false
                    }
                }

                LaunchedEffect(repoName) { refreshGit() }

                val importEmbeddedModel = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) scope.launch {
                        isWorking = true
                        runCatching {
                            withContext(Dispatchers.IO) {
                                val displayName = queryDocumentName(uri)
                                embeddedModelStore.importModel(uri, contentResolver, displayName)
                            }
                        }.onSuccess { activeEmbeddedModel = it; embeddedModels = embeddedModelStore.list(); embeddedStatus = "Model imported locally: ${it.name}" }
                            .onFailure { embeddedStatus = "Model import failed: ${it.message}" }
                        isWorking = false
                    }
                }
                val importFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                    scope.launch {
                        uris.forEach { uri ->
                            runCatching { withContext(Dispatchers.IO) { repository.import(uri) } }
                                .onFailure { status = "Import failed: ${it.message}" }
                        }
                        refresh++
                        status = "Imported selected resources"
                    }
                }
                val importGitFiles = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
                    scope.launch {
                        runCatching {
                            withContext(Dispatchers.IO) {
                                uris.forEachIndexed { index, uri ->
                                    val rawName = queryDocumentName(uri)
                                    val safeName = rawName.replace(Regex("[^A-Za-z0-9._-]+"), "_").trim('.', '_', '-').ifBlank { "imported-file" }
                                    val input = contentResolver.openInputStream(uri) ?: error("Cannot read $rawName")
                                    gitWorkspace.importFile(repoName, "imports/${System.currentTimeMillis()}-$index-$safeName", input)
                                }
                            }
                        }.onSuccess { status = "Files copied into local Git repository."; refreshGit() }
                            .onFailure { status = "Git file import failed: ${it.message}" }
                    }
                }
                val exportBundle = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
                    if (uri != null) scope.launch {
                        isWorking = true
                        runCatching {
                            withContext(Dispatchers.IO) {
                                val temp = File(cacheDir, "$repoName-${System.currentTimeMillis()}.bundle")
                                try {
                                    gitWorkspace.exportBundle(repoName, temp)
                                    contentResolver.openOutputStream(uri)?.use { output ->
                                        temp.inputStream().use { input -> input.copyTo(output) }
                                    } ?: error("Cannot write selected destination")
                                } finally {
                                    temp.delete()
                                }
                            }
                        }.onSuccess { status = "Offline Git bundle exported." }
                            .onFailure { status = "Bundle export failed: ${it.message}" }
                        isWorking = false
                    }
                }
                val importBundle = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                    if (uri != null) scope.launch {
                        val destinationName = newRepoName.trim()
                        if (destinationName.isBlank()) {
                            status = "Enter a new, unused name in New repo name before importing a bundle."
                        } else {
                            isWorking = true
                            runCatching {
                                withContext(Dispatchers.IO) {
                                    val temp = File(cacheDir, "incoming-${System.currentTimeMillis()}.bundle")
                                    try {
                                        contentResolver.openInputStream(uri)?.use { input -> temp.outputStream().use { input.copyTo(it) } }
                                            ?: error("Cannot read selected bundle")
                                        gitWorkspace.importBundle(destinationName, temp)
                                    } finally {
                                        temp.delete()
                                    }
                                }
                            }.onSuccess {
                                newRepoName = ""
                                repoName = destinationName
                                status = "Imported offline Git bundle as a new repository: $destinationName"
                                refreshGit()
                            }.onFailure { status = "Bundle import failed: ${it.message}" }
                            isWorking = false
                        }
                    }
                }

                confirmPushRemote?.let { targetRemote ->
                    AlertDialog(
                        onDismissRequest = { confirmPushRemote = null },
                        title = { Text("Push local commits?") },
                        text = { Text("This sends the current local branch from $repoName to $targetRemote. The working tree must be clean. Review the remote URL and destination before continuing.") },
                        confirmButton = {
                            TextButton(onClick = {
                                confirmPushRemote = null
                                runRemoteOperation(targetRemote, true)
                            }) { Text("Confirm push") }
                        },
                        dismissButton = {
                            TextButton(onClick = { confirmPushRemote = null }) { Text("Cancel") }
                        }
                    )
                }

                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SOVEREIGN OS", style = MaterialTheme.typography.headlineMedium)
                        Text("Personal · offline-first · model-independent", style = MaterialTheme.typography.bodyMedium)
                        TabRow(selectedTabIndex = page) {
                            listOf("Library", "Local Git", "AI Connections").forEachIndexed { index, label ->
                                Tab(selected = page == index, onClick = { page = index }, text = { Text(label) })
                            }
                        }

                        when (page) {
                            0 -> {
                                Text(status, style = MaterialTheme.typography.labelMedium)
                                Button(onClick = { importFiles.launch(arrayOf("*/*")) }) { Text("Import files") }
                                Text("${records.size} resources stored locally", style = MaterialTheme.typography.titleMedium)
                                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    items(records, key = { it.id }) { record ->
                                        ResourceCard(record, onShare = { shareText(contextFor(record), "SOVEREIGN context") })
                                    }
                                }
                            }
                            1 -> {
                                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Private local workspace · all Git operations here work offline.", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(value = repoName, onValueChange = { repoName = it }, label = { Text("Repository") }, modifier = Modifier.weight(1f), singleLine = true)
                                        Button(onClick = { refreshGit() }, enabled = !isWorking) { Text("Refresh") }
                                    }
                                    if (repoNames.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            repoNames.forEach { name -> TextButton(onClick = { repoName = name }) { Text(name) } }
                                        }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(value = newRepoName, onValueChange = { newRepoName = it }, label = { Text("New repo name") }, modifier = Modifier.weight(1f), singleLine = true)
                                        OutlinedButton(onClick = {
                                            scope.launch {
                                                runCatching { withContext(Dispatchers.IO) { gitWorkspace.createRepository(newRepoName.trim()) } }
                                                    .onSuccess { repoName = newRepoName.trim(); newRepoName = ""; refreshGit(); status = "Local repository created." }
                                                    .onFailure { status = "Create repository failed: ${it.message}" }
                                            }
                                        }) { Text("Create") }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(onClick = { importGitFiles.launch(arrayOf("*/*")) }) { Text("Import files") }
                                        OutlinedButton(onClick = { exportBundle.launch("$repoName.bundle") }) { Text("Export .bundle") }
                                        OutlinedButton(onClick = { importBundle.launch(arrayOf("application/octet-stream", "application/x-git-bundle", "*/*")) }) { Text("Import .bundle") }
                                        OutlinedButton(onClick = { shareText(gitContext(repoName), "SOVEREIGN Git context") }) { Text("Share context") }
                                        OutlinedButton(onClick = { copyText(gitContext(repoName), "Git context") }) { Text("Copy context") }
                                    }
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Remote repositories · explicit sync", style = MaterialTheme.typography.titleMedium)
                                            Text("HTTPS only in this milestone. Public remotes need no token; private remotes may use a one-time access token. Credentials are not saved.", style = MaterialTheme.typography.bodySmall)
                                            OutlinedTextField(value = remoteName, onValueChange = { remoteName = it }, label = { Text("Remote name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                            OutlinedTextField(value = remoteUrl, onValueChange = { remoteUrl = it }, label = { Text("HTTPS remote URL · GitHub / GitLab / self-hosted") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                OutlinedButton(onClick = {
                                                    scope.launch {
                                                        runCatching {
                                                            withContext(Dispatchers.IO) { gitWorkspace.addRemote(repoName, remoteName.trim(), remoteUrl.trim()) }
                                                        }.onSuccess {
                                                            remoteUrl = ""
                                                            status = "Remote $remoteName added."
                                                            refreshGit()
                                                        }.onFailure { status = "Add remote failed: ${it.message}" }
                                                    }
                                                }, enabled = !isWorking && remoteName.isNotBlank() && remoteUrl.isNotBlank()) { Text("Add remote") }
                                                OutlinedButton(onClick = {
                                                    val destinationName = newRepoName.trim()
                                                    val sourceUrl = remoteUrl.trim()
                                                    val username = remoteUsername.trim().ifBlank { null }
                                                    val token = remoteToken.takeIf { it.isNotBlank() }?.toCharArray()
                                                    remoteToken = ""
                                                    scope.launch {
                                                        isWorking = true
                                                        runCatching {
                                                            withContext(Dispatchers.IO) {
                                                                gitWorkspace.cloneRepository(destinationName, sourceUrl, username, token)
                                                            }
                                                        }.onSuccess {
                                                            repoName = destinationName
                                                            newRepoName = ""
                                                            remoteUrl = ""
                                                            status = "Cloned remote into local repository: $destinationName"
                                                            refreshGit()
                                                        }.onFailure { status = "Clone failed: ${it.message}" }
                                                        isWorking = false
                                                    }
                                                }, enabled = !isWorking && newRepoName.isNotBlank() && remoteUrl.isNotBlank()) { Text("Clone into new repo name") }
                                            }
                                            OutlinedTextField(value = remoteUsername, onValueChange = { remoteUsername = it }, label = { Text("Git account username (optional for public repos)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                            OutlinedTextField(value = remoteToken, onValueChange = { remoteToken = it }, label = { Text("One-time access token · cleared after operation") }, modifier = Modifier.fillMaxWidth(), singleLine = true, visualTransformation = PasswordVisualTransformation())
                                            if (remoteNames.isNotEmpty()) {
                                                remoteNames.forEach { remote ->
                                                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                                        Text(remote.name, style = MaterialTheme.typography.titleSmall)
                                                        Text(remote.url, style = MaterialTheme.typography.bodySmall)
                                                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                            OutlinedButton(onClick = { runRemoteOperation(remote.name, false) }, enabled = !isWorking) { Text("Fetch") }
                                                            OutlinedButton(onClick = { confirmPushRemote = remote.name }, enabled = !isWorking && gitStatus?.isClean == true) { Text("Push…") }
                                                            TextButton(onClick = {
                                                                scope.launch {
                                                                    runCatching { withContext(Dispatchers.IO) { gitWorkspace.removeRemote(repoName, remote.name) } }
                                                                        .onSuccess { status = "Removed remote ${remote.name}"; refreshGit() }
                                                                        .onFailure { status = "Remove remote failed: ${it.message}" }
                                                                }
                                                            }, enabled = !isWorking) { Text("Remove") }
                                                        }
                                                    }
                                                }
                                                if (remoteBranches.isNotEmpty()) {
                                                    Text("Fetched remote branches", style = MaterialTheme.typography.titleSmall)
                                                    remoteBranches.forEach { remoteBranch ->
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                                                        ) {
                                                            Text(remoteBranch, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                                            TextButton(
                                                                enabled = !isWorking && gitStatus?.isClean == true,
                                                                onClick = {
                                                                    scope.launch {
                                                                        isWorking = true
                                                                        runCatching {
                                                                            withContext(Dispatchers.IO) {
                                                                                gitWorkspace.checkoutRemoteBranch(repoName, remoteBranch)
                                                                            }
                                                                        }.onSuccess {
                                                                            status = "Checked out local branch $it tracking $remoteBranch"
                                                                            refreshGit()
                                                                        }.onFailure {
                                                                            status = "Remote branch checkout failed: ${it.message}"
                                                                        }
                                                                        isWorking = false
                                                                    }
                                                                }
                                                            ) { Text("Checkout") }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    gitStatus?.let { snapshot ->
                                        Card(Modifier.fillMaxWidth()) {
                                            Column(Modifier.padding(10.dp)) {
                                                Text("Branch: ${snapshot.branch}", style = MaterialTheme.typography.titleMedium)
                                                Text(if (snapshot.isClean) "Working tree clean" else "Staged ${snapshot.staged.size} · modified ${snapshot.modified.size} · untracked ${snapshot.untracked.size} · missing ${snapshot.missing.size}")
                                            }
                                        }
                                    }
                                    OutlinedTextField(value = filePath, onValueChange = { filePath = it }, label = { Text("File path") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                    OutlinedTextField(value = fileContent, onValueChange = { fileContent = it }, label = { Text("File content") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
                                    Button(onClick = {
                                        scope.launch {
                                            runCatching { withContext(Dispatchers.IO) { gitWorkspace.writeFile(repoName, filePath, fileContent) } }
                                                .onSuccess { status = "File written in local repository."; refreshGit() }
                                                .onFailure { status = "Write failed: ${it.message}" }
                                        }
                                    }) { Text("Save file in repository") }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Button(onClick = {
                                            scope.launch {
                                                runCatching { withContext(Dispatchers.IO) { gitWorkspace.stageAll(repoName); gitWorkspace.commit(repoName, commitMessage) } }
                                                    .onSuccess { status = "Local commit created: ${it.take(10)}"; refreshGit() }
                                                    .onFailure { status = "Commit failed: ${it.message}" }
                                            }
                                        }) { Text("Stage all + Commit") }
                                        OutlinedTextField(value = commitMessage, onValueChange = { commitMessage = it }, label = { Text("Commit message") }, modifier = Modifier.weight(1f), singleLine = true)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(value = branchName, onValueChange = { branchName = it }, label = { Text("New branch") }, modifier = Modifier.weight(1f), singleLine = true)
                                        OutlinedButton(onClick = {
                                            scope.launch {
                                                runCatching { withContext(Dispatchers.IO) { gitWorkspace.createBranch(repoName, branchName) } }
                                                    .onSuccess { status = "Branch created."; refreshGit() }
                                                    .onFailure { status = "Branch failed: ${it.message}" }
                                            }
                                        }) { Text("Create branch") }
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                                        branches.forEach { branch ->
                                            TextButton(onClick = {
                                                scope.launch {
                                                    runCatching { withContext(Dispatchers.IO) { gitWorkspace.checkoutBranch(repoName, branch) } }
                                                        .onSuccess { status = "Checked out $branch"; refreshGit() }
                                                        .onFailure { status = "Checkout failed: ${it.message}" }
                                                }
                                            }) { Text(branch) }
                                        }
                                    }
                                    Text("Recent commits", style = MaterialTheme.typography.titleSmall)
                                    commits.take(5).forEach { Text("${it.id.take(8)}  ${it.message}", style = MaterialTheme.typography.bodySmall) }
                                    Text("Diff preview", style = MaterialTheme.typography.titleSmall)
                                    Text(diff.take(1800), style = MaterialTheme.typography.bodySmall)
                                    Text(status, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            else -> {
                                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Three routes: fully offline in-app inference, no-key local servers, and manual app-to-app transfer.", style = MaterialTheme.typography.bodyMedium)
                                    Card(Modifier.fillMaxWidth()) {
                                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("On-device model · no API · no network", style = MaterialTheme.typography.titleMedium)
                                            Text(activeEmbeddedModel?.let { "${it.name} · ${it.length() / (1024 * 1024)} MB" } ?: "Import a .litertlm model file from local storage.", style = MaterialTheme.typography.bodySmall)
                                            if (embeddedModels.isNotEmpty()) {
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    embeddedModels.forEach { model ->
                                                        TextButton(onClick = {
                                                            runCatching { embeddedModelStore.select(model) }
                                                                .onSuccess { activeEmbeddedModel = model; embeddedReady = false; embeddedStatus = "Selected ${model.name}. Load it to start inference." }
                                                                .onFailure { embeddedStatus = it.message ?: "Could not select model." }
                                                        }) { Text(model.name.take(18)) }
                                                    }
                                                }
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Button(onClick = { importEmbeddedModel.launch(arrayOf("*/*")) }) { Text("Import .litertlm") }
                                                OutlinedButton(onClick = {
                                                    val modelFile = activeEmbeddedModel
                                                    if (modelFile == null) embeddedStatus = "Import a model first."
                                                    else scope.launch {
                                                        isWorking = true
                                                        runCatching { embeddedRuntime.load(modelFile) }
                                                            .onSuccess { embeddedReady = true; embeddedStatus = "Loaded on-device · CPU backend · fully offline." }
                                                            .onFailure { embeddedReady = false; embeddedStatus = "Model load failed: ${it.message}" }
                                                        isWorking = false
                                                    }
                                                }, enabled = !isWorking && activeEmbeddedModel != null) { Text("Load") }
                                                OutlinedButton(onClick = {
                                                    scope.launch {
                                                        isWorking = true
                                                        runCatching { embeddedRuntime.unload() }
                                                            .onSuccess { embeddedReady = false; embeddedStatus = "Embedded model unloaded." }
                                                            .onFailure { embeddedStatus = "Unload failed: ${it.message}" }
                                                        isWorking = false
                                                    }
                                                }, enabled = !isWorking && embeddedReady) { Text("Unload") }
                                            }
                                            Text(embeddedStatus, style = MaterialTheme.typography.labelSmall)
                                            Text("The model file stays in app-private storage. Start with a small quantized model and check free RAM/storage before loading.", style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                    Text("Ollama · llama.cpp server · LM Studio · Jan · LocalAI · other OpenAI-compatible local servers", style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        FilterChip(selected = provider == ModelProvider.OLLAMA, onClick = { provider = ModelProvider.OLLAMA; endpointUrl = "http://127.0.0.1:11434" }, label = { Text("Ollama") })
                                        FilterChip(selected = provider == ModelProvider.OPENAI_COMPATIBLE, onClick = { provider = ModelProvider.OPENAI_COMPATIBLE; endpointUrl = "http://127.0.0.1:1234/v1" }, label = { Text("OpenAI-compatible local") })
                                    }
                                    OutlinedTextField(value = endpointName, onValueChange = { endpointName = it }, label = { Text("Connection name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                    OutlinedTextField(value = endpointUrl, onValueChange = { endpointUrl = it }, label = { Text("Base URL") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = {
                                            val validation = EndpointPolicy.validate(endpointUrl)
                                            if (!validation.isValid) connectionStatus = validation.message
                                            else {
                                                val endpoint = ModelEndpoint(name = endpointName.ifBlank { provider.name }, provider = provider, baseUrl = endpointUrl.trim())
                                                runCatching { endpointStore.save(endpoint) }
                                                    .onSuccess { endpoints = endpointStore.all(); selectedEndpointId = endpoint.id; connectionStatus = "Saved. Press Test to discover local models." }
                                                    .onFailure { connectionStatus = it.message ?: "Could not save endpoint." }
                                            }
                                        }) { Text("Save connection") }
                                        OutlinedButton(onClick = {
                                            val endpoint = ModelEndpoint(name = endpointName, provider = provider, baseUrl = endpointUrl.trim())
                                            scope.launch {
                                                isWorking = true
                                                val result = withContext(Dispatchers.IO) { modelClient.probe(endpoint) }
                                                connectionStatus = result.message
                                                modelChoices = result.models
                                                if (result.models.isNotEmpty()) selectedModel = result.models.first()
                                                isWorking = false
                                            }
                                        }, enabled = !isWorking) { Text("Test / Find models") }
                                    }
                                    Text(connectionStatus, style = MaterialTheme.typography.bodySmall)
                                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                                        Checkbox(checked = approvedRemoteEndpoint == endpointUrl.trim(), onCheckedChange = { approvedRemoteEndpoint = if (it) endpointUrl.trim() else "" })
                                        Text("I approve sending repository text to this public HTTPS endpoint for this session.")
                                    }
                                    OutlinedButton(onClick = { shareText(gitContext(repoName), "Git context for installed AI app") }) { Text("Send to installed AI app · no API") }
                                    Text("Saved connections", style = MaterialTheme.typography.titleMedium)
                                    LazyColumn(Modifier.heightIn(max = 170.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(endpoints, key = { it.id }) { endpoint ->
                                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                TextButton(onClick = {
                                                    selectedEndpointId = endpoint.id; endpointName = endpoint.name
                                                    endpointUrl = endpoint.baseUrl; provider = endpoint.provider
                                                    modelChoices = emptyList(); selectedModel = endpoint.selectedModel
                                                    connectionStatus = "Selected connection. Run Test / Find models to refresh its model list."
                                                }) { Text("${endpoint.name} · ${endpoint.provider}") }
                                                TextButton(onClick = {
                                                    runCatching { endpointStore.remove(endpoint.id); endpoints = endpointStore.all() }
                                                }) { Text("Remove") }
                                            }
                                        }
                                    }
                                    if (modelChoices.isNotEmpty()) {
                                        Text("Available models", style = MaterialTheme.typography.titleSmall)
                                        modelChoices.forEach { model ->
                                            FilterChip(selected = selectedModel == model, onClick = {
                                                selectedModel = model
                                                endpoints.firstOrNull { it.id == selectedEndpointId }?.let { endpoint ->
                                                    runCatching { endpointStore.saveSelectedModel(endpoint.id, model); endpoints = endpointStore.all() }
                                                }
                                            }, label = { Text(model) })
                                        }
                                    }
                                    OutlinedTextField(value = prompt, onValueChange = { prompt = it }, label = { Text("Ask model about this Git repository") }, modifier = Modifier.fillMaxWidth(), minLines = 2, maxLines = 4)
                                    Button(onClick = {
                                        val endpoint = endpoints.firstOrNull { it.id == selectedEndpointId }
                                        if (endpoint == null) connectionStatus = "Save and select a connection first."
                                        else if (selectedModel.isBlank()) connectionStatus = "Test the endpoint and select a model first."
                                        else if (EndpointPolicy.requiresRemoteConsent(endpoint.baseUrl) && approvedRemoteEndpoint != endpoint.baseUrl.trim()) connectionStatus = "This public HTTPS endpoint could receive source code. Confirm the checkbox first."
                                        else scope.launch {
                                            isWorking = true
                                            runCatching {
                                                val context = withContext(Dispatchers.IO) { gitWorkspace.modelContext(repoName) }
                                                withContext(Dispatchers.IO) { modelClient.chat(endpoint, selectedModel, context, prompt, approvedRemoteEndpoint == endpoint.baseUrl.trim()) }
                                            }.onSuccess { modelAnswer = it; connectionStatus = "Model response received." }
                                                .onFailure { connectionStatus = "Model request failed: ${it.message}" }
                                            isWorking = false
                                        }
                                    }, enabled = !isWorking) { Text(if (isWorking) "Working…" else "Ask selected model · read-only Git context") }
                                    if (embeddedReady) {
                                        OutlinedButton(onClick = {
                                            scope.launch {
                                                isWorking = true
                                                runCatching {
                                                    val context = withContext(Dispatchers.IO) { gitWorkspace.modelContext(repoName) }
                                                    embeddedRuntime.generate(buildString {
                                                        appendLine(context)
                                                        appendLine("USER REQUEST:")
                                                        append(prompt)
                                                    })
                                                }.onSuccess { modelAnswer = it; embeddedStatus = "On-device response received. No network used." }
                                                    .onFailure { embeddedStatus = "On-device inference failed: ${it.message}" }
                                                isWorking = false
                                            }
                                        }, enabled = !isWorking) { Text("Ask on-device model · offline") }
                                    }
                                    if (modelAnswer.isNotBlank()) {
                                        Card(Modifier.fillMaxWidth()) {
                                            Column(Modifier.padding(10.dp)) {
                                                Text("Model response", style = MaterialTheme.typography.titleSmall)
                                                Text(modelAnswer, style = MaterialTheme.typography.bodySmall)
                                                TextButton(onClick = { shareText(modelAnswer, "SOVEREIGN model response") }) { Text("Share response") }
                                            }
                                        }
                                    }
                                    Text("No credentials are collected or stored. The app contacts only the endpoint you configure, when you press Test or Ask. Model access to Git is read-only in this version.", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun queryDocumentName(uri: Uri): String = contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) cursor.getString(0) else null
    } ?: "imported-file"

    private fun contextFor(record: ResourceRecord): String = buildString {
        appendLine("# SOVEREIGN context handoff")
        appendLine("Source: ${record.name}")
        appendLine("SHA-256: ${record.sha256}")
        appendLine("Evidence: locally extracted text; verify against original file.")
        appendLine()
        appendLine(record.extractedText.orEmpty().take(12000))
    }

    private fun gitContext(name: String): String = runCatching { gitWorkspace.modelContext(name) }.getOrDefault("No Git context available.")

    private fun copyText(value: String, label: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, value))
        Toast.makeText(this, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun shareText(value: String, title: String) {
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_SUBJECT, title); putExtra(Intent.EXTRA_TEXT, value)
        }
        startActivity(Intent.createChooser(send, "Choose destination"))
    }

    override fun onDestroy() {
        if (::embeddedRuntime.isInitialized) embeddedRuntime.shutdown()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent); setIntent(intent); acceptIncoming(intent)
    }

    private fun acceptIncoming(incoming: Intent?) {
        if (incoming == null) return
        val sharedText = incoming.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        val sharedTitle = incoming.getStringExtra(Intent.EXTRA_TITLE)
        val uris = mutableListOf<Uri>()
        when (incoming.action) {
            Intent.ACTION_SEND -> (incoming.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: incoming.data)?.let(uris::add)
            Intent.ACTION_SEND_MULTIPLE -> incoming.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)?.let(uris::addAll)
        }
        incoming.clipData?.let { clip ->
            for (index in 0 until clip.itemCount) clip.getItemAt(index).uri?.let(uris::add)
        }
        val uniqueUris = uris.distinct()
        if (sharedText.isNullOrBlank() && uniqueUris.isEmpty()) return

        lifecycleScope.launch {
            val (importedCount, failures) = withContext(Dispatchers.IO) {
                var imported = 0
                val failed = mutableListOf<String>()
                if (!sharedText.isNullOrBlank()) {
                    runCatching { repository.importText(sharedText, sharedTitle) }
                        .onSuccess { imported++ }
                        .onFailure { failed += "shared text: ${it.message ?: "unknown error"}" }
                }
                uniqueUris.forEach { uri ->
                    runCatching { repository.import(uri) }
                        .onSuccess { imported++ }
                        .onFailure { failed += "shared file: ${it.message ?: "unknown error"}" }
                }
                imported to failed.toList()
            }
            if (importedCount > 0) resourceRevision++
            status = when {
                importedCount > 0 && failures.isEmpty() -> "Imported $importedCount shared item(s) into private storage."
                importedCount > 0 -> "Imported $importedCount item(s); ${failures.size} failed: ${failures.joinToString("; ")}"
                failures.isNotEmpty() -> "Share intake failed: ${failures.joinToString("; ")}"
                else -> "No supported shared content was received."
            }
        }
    }
}

@Composable
private fun ResourceCard(record: ResourceRecord, onShare: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(record.name, style = MaterialTheme.typography.titleSmall)
            Text("${record.mimeType} · ${record.size} bytes", style = MaterialTheme.typography.bodySmall)
            Text("SHA-256 ${record.sha256.take(16)}…", style = MaterialTheme.typography.labelSmall)
            if (record.duplicateOf != null) Text("Duplicate of existing resource", color = MaterialTheme.colorScheme.tertiary)
            if (record.extractedText != null) {
                Text(record.extractedText.take(220), style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onShare) { Text("Send text to another app") }
            } else Text("Stored safely · text extraction not available for this format yet", style = MaterialTheme.typography.bodySmall)
        }
    }
}
