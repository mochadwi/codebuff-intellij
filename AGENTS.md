# Codebuff IntelliJ Plugin - Agent Instructions

## Project Overview

IntelliJ IDEA plugin for [Codebuff](https://github.com/CodebuffAI/codebuff) AI coding assistant. This plugin provides a native IDE experience for interacting with Codebuff's multi-agent system.

**Architecture**: Thin UI client that delegates agent logic to Codebuff CLI backend via JSON Lines over stdio.

## Tech Stack

- **Language**: Kotlin
- **Build**: Gradle Kotlin DSL with IntelliJ Platform Plugin
- **Target**: IntelliJ IDEA 2024.2+ (build 242+)
- **JDK**: 17+
- **Dependencies**: 
  - OkHttp 4.x (HTTP client)
  - Kotlin Coroutines (async operations)
  - IntelliJ Platform SDK

## Project Structure

```
codebuff-intellij/
├── build.gradle.kts              # Gradle build configuration
├── settings.gradle.kts           # Project settings
├── gradle.properties             # Gradle/IntelliJ properties
├── src/
│   ├── main/
│   │   ├── kotlin/com/codebuff/intellij/
│   │   │   ├── CodebuffBundle.kt           # i18n message bundle
│   │   │   ├── actions/                     # AnAction implementations
│   │   │   │   ├── OpenToolWindowAction.kt
│   │   │   │   ├── SendSelectionAction.kt
│   │   │   │   └── AskAboutFileAction.kt
│   │   │   ├── services/                    # Project-level services
│   │   │   │   ├── CodebuffProjectService.kt
│   │   │   │   ├── SessionManager.kt
│   │   │   │   └── ContextCollector.kt
│   │   │   ├── backend/                     # Backend communication
│   │   │   │   ├── BackendClient.kt         # Interface
│   │   │   │   ├── CliBackendClient.kt      # CLI subprocess impl
│   │   │   │   ├── Protocol.kt              # JSON Lines protocol
│   │   │   │   └── StreamingEventRouter.kt
│   │   │   ├── ui/                          # UI components
│   │   │   │   ├── CodebuffToolWindowFactory.kt
│   │   │   │   ├── ChatPanel.kt
│   │   │   │   ├── SessionPanel.kt
│   │   │   │   └── ContextAttachmentsPanel.kt
│   │   │   ├── diff/                        # Diff viewer integration
│   │   │   │   ├── DiffViewerService.kt
│   │   │   │   └── DiffActions.kt
│   │   │   └── settings/                    # Plugin configuration
│   │   │       ├── CodebuffSettings.kt
│   │   │       └── CodebuffSettingsConfigurable.kt
│   │   └── resources/
│   │       ├── META-INF/
│   │       │   └── plugin.xml               # Plugin descriptor
│   │       ├── messages/
│   │       │   └── CodebuffBundle.properties
│   │       └── icons/
│   │           ├── codebuff.svg
│   │           └── codebuff_dark.svg
│   └── test/kotlin/
│       └── com/codebuff/intellij/
├── AGENTS.md                     # This file
├── README.md                     # User documentation
└── .github/
    └── workflows/
        └── build.yml             # CI/CD pipeline
```

## Commands

All Gradle commands run through Docker for consistency.

```bash
# Docker setup (first time)
docker compose build              # Build the development image

# Development (via Docker)
docker compose run --rm gradle runIde        # Run plugin in sandbox IDE
docker compose run --rm gradle buildPlugin   # Build plugin distribution
docker compose run --rm gradle verifyPlugin  # Verify plugin compatibility

# Testing (via Docker)
docker compose run --rm gradle test          # Run unit tests
docker compose run --rm gradle check         # Run all checks (test + verify)

# Formatting & Linting (via Docker)
docker compose run --rm gradle ktlintCheck   # Check Kotlin code style
docker compose run --rm gradle ktlintFormat  # Auto-format Kotlin code

# Convenience scripts (after Docker setup)
./scripts/docker-test.sh          # Shorthand for docker compose run test
./scripts/docker-build.sh         # Shorthand for docker compose run buildPlugin
```

### Docker Configuration

The project uses Docker to ensure consistent JDK 17 and Gradle environment:

- **docker/Dockerfile**: Base image with JDK 17, Gradle
- **docker-compose.yml**: Service definitions with volume mounts
- **Gradle cache**: Persisted in named volume `gradle-cache`

## Architecture Details

### Backend Protocol (JSON Lines over stdio)

Communication with Codebuff CLI via `codebuff ide --stdio`:

**Request format**:
```json
{"id":"req-001","type":"sendMessage","sessionId":"sess-123","text":"Add error handling","context":[{"type":"file","path":"src/main.kt","content":"..."}]}
```

**Event types from backend**:
```json
{"type":"token","sessionId":"sess-123","text":"I'll add..."}
{"type":"tool_call","sessionId":"sess-123","tool":"read_files","input":{...}}
{"type":"tool_result","sessionId":"sess-123","tool":"read_files","output":{...}}
{"type":"diff","sessionId":"sess-123","files":[{"path":"...","before":"...","after":"..."}]}
{"type":"error","sessionId":"sess-123","message":"..."}
{"type":"done","sessionId":"sess-123"}
```

### Service Layer

All services are **Project-level** (`@Service(Service.Level.PROJECT)`):

1. **CodebuffProjectService**: Lifecycle owner, creates/destroys backend connection
2. **SessionManager**: Tracks sessions, messages, persists state via `PersistentStateComponent`
3. **ContextCollector**: Gathers editor selection, files, diagnostics, git diff
4. **DiffViewerService**: Shows proposed changes using IntelliJ Diff API

### Threading Model

- **Backend I/O**: Background threads via Kotlin coroutines (`Dispatchers.IO`)
- **UI Updates**: Always marshal to EDT via `ApplicationManager.getApplication().invokeLater`
- **File Writes**: Use `WriteCommandAction.runWriteCommandAction()` for all VFS modifications

## Git Workflow

### Git Hooks (Pre-commit)

The project uses tracked Git hooks in `.githooks/` directory for code quality enforcement.

**Setup (one-time after clone):**
```bash
./scripts/setup-hooks.sh
```

This configures `core.hooksPath` to use `.githooks/`, which works across all worktrees.

**Available Hooks:**
- `pre-commit` - Runs ktlint on staged Kotlin files before commit

**Bypass when needed:**
```bash
git commit --no-verify  # Skip hooks for this commit
```


### Feature Branch Strategy

All development uses feature branches merged to main:

```bash
# Create feature branch for a task
git checkout -b <task-id>
# Example: git checkout -b cb-vnl.13

# Work on the feature...
git add -A && git commit -m "[<task-id>] <description>"

# Merge to main
git checkout main
git merge <task-id>
git branch -d <task-id>  # Delete feature branch after merge
```

**Branch Rules:**
- Feature branch name matches task ID (e.g., `cb-vnl.13`)
- Always merge to `main` branch
- Delete feature branch after successful merge
- Never commit directly to main

### Git Worktree Strategy (Optional for Epics)

For parallel development of multiple epics, use git worktrees:

```bash
# Create worktree for an epic
git worktree add ../worktrees/<epic-id> -b <epic-id>
# Example: git worktree add ../worktrees/cb-vnl -b cb-vnl

# List all worktrees
git worktree list

# Remove worktree after epic is complete and merged
git worktree remove ../worktrees/<epic-id>
```

**Worktree Rules:**
- Naming convention: `../worktrees/<epic-id>` (e.g., `../worktrees/cb-vnl`)
- Branch name matches epic ID
- One worktree per epic, never share worktrees between epics
- Clean up worktrees after merge to main

## Development Workflow

### Test-Driven Development (TDD) Workflow

**MANDATORY:** All implementation follows TDD. Tests are written FIRST.

**TDD Cycle (Red-Green-Refactor):**
1. **RED** - Write a failing test for the feature/task
2. **RUN** - Execute test, confirm it fails (expected)
3. **GREEN** - Write minimal code to make the test pass
4. **RUN** - Execute test, confirm it passes
5. **REFACTOR** - Clean up code while keeping tests green
6. **REPEAT** - Continue until task is complete

**Task Dependencies:**
- Every epic MUST have test tasks as **blockers** for implementation tasks
- Use `bd dep` to set dependencies:
  ```bash
  # Make test task block implementation task
  bd dep <test-task-id> --blocks <implementation-task-id>
  # Example: bd dep cb-xxx.1 --blocks cb-xxx.2
  
  # Alternative syntax
  bd dep add <blocked-id> <blocker-id>
  ```
- No implementation task can start until its test task is complete

**Example epic structure:**
```
○ cb-xxx [epic] - Feature X
  ○ cb-xxx.1 [task] [test] - Write tests for Component A  ← BLOCKER
  ○ cb-xxx.2 [task] - Implement Component A (blocked by cb-xxx.1)
  ○ cb-xxx.3 [task] [test] - Write tests for Component B  ← BLOCKER
  ○ cb-xxx.4 [task] - Implement Component B (blocked by cb-xxx.3)
```

### Task Completion Rules

**Each completed task MUST:**
1. Have all associated tests passing
2. Be committed with its test cases in the same commit (or linked commits)
3. Reference the Beads task ID in commit message

**Commit message format:**
```
[<task-id>] <description>

- What was implemented
- What tests were added
```

**Example:**
```
[cb-vnl.1] Setup Gradle Kotlin DSL build configuration

- Added build.gradle.kts with IntelliJ Platform Plugin
- Added settings.gradle.kts and gradle.properties
- Added test for build configuration validation
```

### Epic Completion Workflow

When ALL tasks in an epic are done:

1. **Verify all tests pass:**
   ```bash
   docker compose run --rm gradle test
   ```

2. **Run full quality gates:**
   ```bash
   docker compose run --rm gradle check
   ```

3. **Build the plugin:**
   ```bash
   docker compose run --rm gradle buildPlugin
   ```

4. **If ANY step fails:**
   - Fix the issue
   - Run the failing step again
   - Repeat until ALL steps pass with zero errors

5. **Push changes:**
   ```bash
   git push origin <epic-id>
   ```

6. **Create PR and merge to main**

7. **Clean up worktree:**
   ```bash
   git worktree remove ../worktrees/<epic-id>
   git branch -d <epic-id>  # After merge
   ```

## Coding Conventions

### General
- Follow Kotlin coding conventions
- Use IntelliJ Platform patterns (services, actions, extensions)
- Prefer composition over inheritance
- Keep UI code separate from business logic

### Naming
- Services: `*Service.kt`
- Actions: `*Action.kt`
- UI components: `*Panel.kt`, `*Dialog.kt`
- Interfaces: No `I` prefix, use descriptive names

### Error Handling
- Never swallow exceptions silently
- Use IntelliJ notifications for user-facing errors
- Log errors with `com.intellij.openapi.diagnostic.Logger`

### Testing
- Unit test services and protocol parsing
- Use `BasePlatformTestCase` for integration tests
- Mock backend responses for UI testing

## Safety Rules

1. **Never modify files outside write actions** - Use `WriteCommandAction`
2. **Always show diffs before applying changes** - Unless user explicitly enables auto-apply
3. **Validate all backend paths** - Ensure they're within project boundaries
4. **No secrets in logs** - Never log API keys, tokens, or credentials
5. **Handle process lifecycle** - Restart backend on crash, clean shutdown on project close

## Extension Points

Plugin extends these IntelliJ extension points:

- `com.intellij.toolWindow` - Codebuff ToolWindow
- `com.intellij.projectService` - Project-level services
- `com.intellij.applicationConfigurable` - Settings page
- `com.intellij.notificationGroup` - Notifications
- `com.intellij.editorActionHandler` - Editor context actions

## GitHub Actions CI/CD

### PR Verification Workflow

Automatic verification runs on all PRs via `.github/workflows/pr-verification.yml`:

**Steps:**
1. Setup Java 17 + Gradle with caching
2. Run tests: `./gradlew test`
3. Verify plugin: `./gradlew verifyPlugin`
4. Build plugin: `./gradlew buildPlugin`
5. Upload artifacts (plugin ZIP, test reports)

> **Note:** ktlint is enforced via pre-commit hooks, not in CI, to reduce build time.

**Before Creating a PR:**
```bash
docker compose run --rm gradle test          # Must pass
docker compose run --rm gradle verifyPlugin  # Must pass
docker compose run --rm gradle buildPlugin   # Must succeed
```

### PR Template

PRs use `.github/PULL_REQUEST_TEMPLATE.md` with:
- Summary and changes sections
- Type of change checkboxes (bug fix, feature, breaking change, etc.)
- Testing checklist
- Related issues linking

### PR Title & Description Guidelines

**MANDATORY:** All PRs must have proper titles and descriptions.

**PR Title Format:**
```
<type>: <short description>
```

**Types:**
- `feat` - New feature
- `fix` - Bug fix
- `docs` - Documentation only
- `style` - Code style/formatting (no logic change)
- `refactor` - Code refactoring (no feature change)
- `test` - Adding/updating tests
- `chore` - Build, CI, dependencies, tooling
- `perf` - Performance improvement

**Examples:**
```
feat: Add streaming response display in chat panel
fix: Resolve connection timeout on large files
refactor: Extract protocol parsing into separate module
chore: Add GitHub Actions PR verification workflow
```

**PR Description Must Include:**
1. **Summary** - What this PR does (1-2 sentences)
2. **Changes** - Bullet list of specific changes
3. **Testing** - How the changes were verified
4. **Related Issues** - Link to Beads tasks or GitHub issues

### GitHub Access via MCP

**Use MCP (Model Context Protocol) for GitHub operations:**

The project uses MCP with the `github` server for programmatic GitHub access. This enables:
- Creating and managing PRs
- Checking workflow status
- Merging PRs
- Managing branches and issues

**MCP GitHub Server Setup:**
```json
// In your MCP configuration
{
  "mcpServers": {
    "github": {
      "command": "npx",
      "args": ["-y", "@modelcontextprotocol/server-github"],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "<your-token>"
      }
    }
  }
}
```

**Common MCP GitHub Operations:**
```
# Create PR
mcp github create_pull_request --repo mochadwi/codebuff-intellij --title "feat: ..." --body "..." --head <branch> --base main

# List PRs
mcp github list_pull_requests --repo mochadwi/codebuff-intellij --state open

# Merge PR
mcp github merge_pull_request --repo mochadwi/codebuff-intellij --pull_number <num>

# Check workflow runs
mcp github list_workflow_runs --repo mochadwi/codebuff-intellij --branch <branch>
```

**When to Use MCP vs CLI:**
- Use MCP for: Creating PRs, merging, checking CI status, managing issues
- Use git CLI for: Commits, pushes, rebases, local branch management

## Resources

- [IntelliJ Platform SDK Docs](https://plugins.jetbrains.com/docs/intellij/)
- [Kotlin Coroutines Guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Codebuff Documentation](https://codebuff.com/docs)
- [Codebuff SDK](https://www.npmjs.com/package/@codebuff/sdk)
- [OpenCode_UI Reference](https://github.com/LaiZhou/OpenCode_UI)

## Phase 2 Audit Findings (cb-ble.13)

**Code audit on 2026-01-24** identified 11 critical issues in the Phase 2 GREEN implementation.

### Critical Patterns to Avoid

| Pattern | Problem | Solution |
|---------|---------|----------|
| `GlobalScope.launch` | Leaks coroutines, ignores project lifecycle | Use project-tied `CoroutineScope(SupervisorJob() + Dispatchers.IO)` |
| `runBlocking` in `dispose()` | Deadlocks/freezes IDE shutdown | Non-blocking cleanup, cancel scope |
| `PrintWriter` | Silently swallows I/O errors | Use `BufferedWriter(OutputStreamWriter(...))` |
| `redirectErrorStream(true)` | Mixes stderr into JSONL stream | Consume stderr on separate coroutine |
| Custom `Disposable` interface | Conflicts with IntelliJ lifecycle | Use `com.intellij.openapi.Disposable` |
| Swallowed exceptions | Violates error handling rules | Log with `Logger.getInstance()` |
| Missing `@Volatile` | Thread visibility bugs | Add `@Volatile` to shared state |
| `readLine() == null` not handled | Infinite loop on EOF | Break loop, emit ErrorEvent |
| Unknown event → `DoneEvent` | Prematurely terminates streaming | Add `UnknownEvent` or log+skip |
| Lock held during callbacks | Potential deadlock | Copy listeners before callbacks |
| `invokeLater` without disposal check | Dispatches to disposed components | Check `project.isDisposed` first |

### Correct Patterns

```kotlin
// Correct: Project-tied coroutine scope
private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

// Correct: Non-blocking disposal
override fun dispose() {
    scope.cancel()
    writer?.close()
    reader?.close()
    process?.destroy()
}

// Correct: BufferedWriter with error propagation
writer = BufferedWriter(OutputStreamWriter(process.outputStream, StandardCharsets.UTF_8))
writer.write(json)
writer.write("\n")
writer.flush()

// Correct: Separate stderr consumption
scope.launch {
    process.errorStream.bufferedReader().useLines { lines ->
        lines.forEach { log.debug("[codebuff stderr] $it") }
    }
}

// Correct: Thread-safe state
@Volatile private var isConnectedFlag = false

// Correct: Listener snapshot before callbacks
val snapshot = synchronized(listeners) { listeners.toList() }
snapshot.forEach { it.onToken(event) }

// Correct: Disposal check in invokeLater
if (isDisposed || project.isDisposed) return
ApplicationManager.getApplication().invokeLater({
    if (isDisposed || project.isDisposed) return@invokeLater
    // ... dispatch events
}, ModalityState.defaultModalityState())
```

### Refactor Tasks (cb-ble.13.x)

| Task | Description | Files |
|------|-------------|-------|
| cb-ble.13.1 | Replace GlobalScope with project-tied scope | CliBackendClient.kt |
| cb-ble.13.2 | Remove runBlocking from dispose() | CliBackendClient.kt |
| cb-ble.13.3 | Replace PrintWriter with BufferedWriter | CliBackendClient.kt |
| cb-ble.13.4 | Separate stderr from stdout | CliBackendClient.kt |
| cb-ble.13.5 | Use IntelliJ Disposable | CliBackendClient.kt |
| cb-ble.13.6 | Add Logger, fix swallowed exceptions | CliBackendClient.kt |
| cb-ble.13.7 | Add @Volatile to shared state | CliBackendClient.kt |
| cb-ble.13.8 | Fix EOF infinite loop | CliBackendClient.kt |
| cb-ble.13.9 | Fix unknown event type handling | Protocol.kt |
| cb-ble.13.10 | Fix listener lock during callbacks | StreamingEventRouter.kt |
| cb-ble.13.11 | Add disposed check to invokeLater | StreamingEventRouter.kt |

## Landing the Plane (Session Completion)

**When ending a work session**, you MUST complete ALL steps below. Work is NOT complete until `git push` succeeds.

**MANDATORY WORKFLOW:**

1. **Ensure all tests pass:**
   ```bash
   docker compose run --rm gradle test
   ```

2. **File issues for remaining work** - Create issues for anything that needs follow-up
   ```bash
   bd create "Remaining work description" --type task
   ```

3. **Run quality gates** (if code changed):
   ```bash
   docker compose run --rm gradle check
   docker compose run --rm gradle buildPlugin
   ```

4. **Update issue status** - Close finished work, update in-progress items:
   ```bash
   bd update <task-id> --status done
   bd update <epic-id> --status done  # If all tasks complete
   ```

5. **PUSH TO REMOTE** - This is MANDATORY:
   ```bash
   git pull --rebase
   bd sync
   git push
   git status  # MUST show "up to date with origin"
   ```

6. **Clean up worktrees** (if epic is complete and merged):
   ```bash
   git worktree list                        # Check active worktrees
   git worktree remove ../worktrees/<epic-id>  # Remove completed epic worktree
   git branch -d <epic-id>                  # Delete merged branch
   ```

7. **Clean up** - Clear stashes, prune remote branches:
   ```bash
   git stash clear
   git remote prune origin
   ```

8. **Verify** - All changes committed AND pushed

9. **Hand off** - Provide context for next session

**CRITICAL RULES:**
- Work is NOT complete until `git push` succeeds
- NEVER stop before pushing - that leaves work stranded locally
- NEVER say "ready to push when you are" - YOU must push
- If push fails, resolve and retry until it succeeds
- ALL tests must pass before pushing
- Clean up worktrees only AFTER merge to main
