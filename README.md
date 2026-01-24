# Codebuff IntelliJ Plugin

IntelliJ IDEA plugin for [Codebuff](https://github.com/CodebuffAI/codebuff) - an open-source AI coding assistant that coordinates specialized agents to understand your project and make precise changes.

## Features

- 🤖 **Native IDE Integration** - Chat with Codebuff directly in IntelliJ IDEA
- 📝 **Context-Aware** - Send code selections, files, and diagnostics as context
- 👁️ **Diff Review** - Review proposed changes before applying them
- 🔄 **Session Management** - Manage multiple coding sessions
- ⚡ **Streaming Responses** - Real-time streaming of agent output

## Requirements

- IntelliJ IDEA 2024.2 or later
- [Codebuff CLI](https://www.npmjs.com/package/codebuff) installed (`npm install -g codebuff`)
- Codebuff account with API access

## Installation

### From JetBrains Marketplace (Coming Soon)

1. Open IntelliJ IDEA
2. Go to **Settings** → **Plugins** → **Marketplace**
3. Search for "Codebuff"
4. Click **Install**

### From Source

```bash
git clone https://github.com/mochadwi/codebuff-intellij.git
cd codebuff-intellij
./gradlew buildPlugin
```

Then install the plugin from `build/distributions/codebuff-intellij-*.zip`.

## Usage

### Quick Start

1. Open a project in IntelliJ IDEA
2. Open the Codebuff tool window (View → Tool Windows → Codebuff)
3. Type your request and press Enter
4. Review and apply suggested changes

### Keyboard Shortcuts

| Action | macOS | Windows/Linux |
|--------|-------|---------------|
| Open Codebuff | `⌘ + Shift + C` | `Ctrl + Shift + C` |
| Send Selection | `⌥ + ⌘ + K` | `Ctrl + Alt + K` |
| Ask About File | `⌥ + ⌘ + A` | `Ctrl + Alt + A` |

### Context Actions

Right-click in the editor to access:
- **Ask Codebuff about selection** - Get help with selected code
- **Add to Codebuff context** - Include code in your next prompt

## Configuration

Go to **Settings** → **Tools** → **Codebuff** to configure:

- **Codebuff CLI Path** - Path to `codebuff` executable (default: auto-detect from PATH)
- **Auto-apply changes** - Automatically apply changes without diff review
- **Show tool calls** - Display agent tool executions in chat

## Development

### Prerequisites

- JDK 17+
- IntelliJ IDEA (Community or Ultimate)

### Building

```bash
# Run in development sandbox
./gradlew runIde

# Build plugin distribution
./gradlew buildPlugin

# Run tests
./gradlew test
```

### Project Structure

See [AGENTS.md](./AGENTS.md) for detailed architecture and coding conventions.

## Contributing

Contributions are welcome! Please read our contributing guidelines before submitting PRs.

## License

MIT License - see [LICENSE](./LICENSE) for details.

## Links

- [Codebuff Documentation](https://codebuff.com/docs)
- [Codebuff GitHub](https://github.com/CodebuffAI/codebuff)
- [Report Issues](https://github.com/mochadwi/codebuff-intellij/issues)
- [Discord Community](https://codebuff.com/discord)
