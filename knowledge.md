# Codebuff IntelliJ Plugin

IntelliJ IDEA plugin for Codebuff AI coding assistant. Thin UI client that communicates with Codebuff CLI backend via JSON Lines over stdio.

## Tech Stack

- **Language**: Kotlin
- **Build**: Gradle Kotlin DSL + IntelliJ Platform Plugin
- **Target**: IntelliJ IDEA 2024.2+ (build 242+)
- **JDK**: 17+
- **Dependencies**: OkHttp 4.x, Kotlin Coroutines, IntelliJ Platform SDK

## Commands

```bash
# Development
./gradlew runIde          # Run plugin in sandbox IDE
./gradlew buildPlugin     # Build plugin distribution
./gradlew verifyPlugin    # Verify plugin compatibility

# Testing
./gradlew test            # Run unit tests
./gradlew check           # Run all checks (test + verify)

# Formatting
./gradlew ktlintCheck     # Check Kotlin code style
./gradlew ktlintFormat    # Auto-format Kotlin code

# Issue Tracking (Beads)
bd list                   # View all issues
bd create "title"         # Create new issue
bd show <id>              # View issue details
bd update <id> --status in_progress  # Update status
bd sync                   # Sync with git remote
```

## Project Structure

```
src/main/kotlin/com/codebuff/intellij/
├── actions/       # AnAction implementations (*Action.kt)
├── services/      # Project-level services (*Service.kt)
├── backend/       # CLI communication (Protocol.kt, CliBackendClient.kt)
├── ui/            # UI components (*Panel.kt, *Dialog.kt)
├── diff/          # Diff viewer integration
└── settings/      # Plugin configuration

src/main/resources/
├── META-INF/plugin.xml    # Plugin descriptor
├── messages/              # i18n bundles
└── icons/                 # Plugin icons
```

## Key Conventions

### Threading
- Backend I/O: `Dispatchers.IO` (Kotlin coroutines)
- UI Updates: `ApplicationManager.getApplication().invokeLater`
- File Writes: `WriteCommandAction.runWriteCommandAction()`

### Services
All services are **Project-level** (`@Service(Service.Level.PROJECT)`):
- `CodebuffProjectService` - Lifecycle owner
- `SessionManager` - Session/message persistence
- `ContextCollector` - Gathers editor context
- `DiffViewerService` - Shows proposed changes

### Safety Rules
1. Never modify files outside write actions
2. Always show diffs before applying changes (unless auto-apply enabled)
3. Validate all backend paths are within project boundaries
4. Never log API keys, tokens, or credentials
5. Handle backend process lifecycle (restart on crash, clean shutdown)

### Backend Protocol
Communication via `codebuff ide --stdio` using JSON Lines:
- Events: `token`, `tool_call`, `tool_result`, `diff`, `error`, `done`

## Epic Development Workflow

### Git Worktree Strategy
Every Beads epic MUST be developed in a separate git worktree:

```bash
# Create worktree for epic
git worktree add ../worktrees/<epic-id> -b <epic-id>

# List worktrees
git worktree list

# Remove after merge
git worktree remove ../worktrees/<epic-id>
```

### Test-Driven Development (TDD)

**MANDATORY:** Tests are written FIRST, implementation follows.

**TDD Cycle:**
1. **RED** - Write failing test
2. **RUN** - Confirm test fails
3. **GREEN** - Write code to pass test
4. **RUN** - Confirm test passes
5. **REFACTOR** - Clean up, keep tests green

**Task Dependencies:**
- Test tasks are **blockers** for implementation tasks
- Set with: `bd dep <test-task-id> --blocks <impl-task-id>`

### Task Completion
- Each task committed with its test cases
- Commit message format: `[<task-id>] <description>`
- All tests must pass before committing

### Epic Completion
1. All tasks done → `./gradlew test`
2. Quality gates → `./gradlew check`
3. Build → `./gradlew buildPlugin`
4. If fails → Fix and repeat until green
5. Push → `git push origin <epic-id>`
6. Merge PR → Clean up worktree

## Session Completion Workflow

**Always complete before ending:**
1. Run tests: `./gradlew test`
2. File issues for remaining work (`bd create`)
3. Run quality gates (`./gradlew check && ./gradlew buildPlugin`)
4. Update issue status (`bd update <id> --status done`)
5. Push to remote: `git pull --rebase && bd sync && git push`
6. Clean up worktrees (if epic merged): `git worktree remove ../worktrees/<epic-id>`
7. Verify `git status` shows "up to date with origin"
