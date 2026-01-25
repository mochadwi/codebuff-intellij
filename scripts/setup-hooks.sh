#!/bin/bash

# Setup Git Hooks for Codebuff IntelliJ Plugin
# Run this once after cloning the repository.

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

echo -e "${YELLOW}Setting up Git hooks...${NC}"

git config core.hooksPath .githooks

chmod +x "$PROJECT_ROOT/.githooks/"* 2>/dev/null || true

echo -e "${GREEN}✅ Git hooks configured!${NC}"
echo ""
echo "Hooks are now active from: .githooks/"
echo "Available hooks:"
ls -1 "$PROJECT_ROOT/.githooks/" 2>/dev/null | sed 's/^/  - /'
echo ""
echo "To bypass hooks temporarily: git commit --no-verify"
echo "To disable hooks: git config --unset core.hooksPath"
