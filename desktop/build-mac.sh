#!/usr/bin/env bash
set -e
cd "$(dirname "$0")"

export ELECTRON_MIRROR="${ELECTRON_MIRROR:-https://npmmirror.com/mirrors/electron/}"

npm run dist:mac
