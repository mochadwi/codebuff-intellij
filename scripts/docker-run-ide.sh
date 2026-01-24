#!/bin/bash
set -e
cd "$(dirname "$0")/.."

# Note: Running IntelliJ IDE in Docker requires X11 forwarding or VNC.
# This script is primarily for CI/headless testing.
# For local development, consider running gradle runIde directly if you have JDK 17 installed.

echo "Warning: runIde requires GUI. For headless builds, use docker-test.sh or docker-build.sh"
echo "Attempting to run with headless mode..."

docker compose run --rm \
  -e DISPLAY="${DISPLAY:-:0}" \
  gradle runIde --args="--headless"
