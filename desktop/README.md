# Clipboard Desktop

This folder is an independent desktop workspace for the note, todo, and research-topic features.

It is intentionally not connected to the old Android Gradle project. Do not add this folder to
`settings.gradle`; the Android app should keep building exactly as before.

## Current Runnable Prototype

The prototype can still run directly in a browser:

```text
desktop/prototype/index.html
```

Open that file to try:

- Notes
- Todo tables
- Research topics
- Basic search
- Local save via browser `localStorage`
- JSON export/import for prototype data
- Android plain JSON import
- Delete the selected note, todo, or research-topic record

It does not need Node.js or Rust. It is just a runnable product prototype.

## Prototype Controls

Research map:

- The topic itself is shown as a visual root box; it is not stored as a normal node.
- Nodes without related nodes connect from the topic root, matching the Android data model.
- Research data keeps the Android fields: `dataType`, `title`, `problem`, `goal`, `conclusion`, `viewMode`, `nodes`.
- Node data keeps `id`, `type`, `title`, `content`, `links`, `relatedNodeIds`, `mapPositioned`, `mapX`, and `mapY`.
- Drag blank area: pan the canvas
- Mouse wheel: zoom
- Drag node: move node
- Right-click a node or blank area: open quick actions
- Map toolbar: zoom in, zoom out, fit to view, reset to 1:1
- Arrow keys: select nearby nodes
- `Space`: edit the selected node title
- `Tab`: insert child node under the selected node
- `Enter`: insert sibling node beside the selected node
- `F`: fit the graph to the current view
- `+` / `-`: zoom in or out
- `0`: reset zoom to 1:1
- `Delete`: delete the selected node

Todo:

- Table editing for title, status, priority, importance, urgency, progress, and due date
- Summary chips for total, done, doing, high-priority, and overdue tasks
- Always-visible filters for active, all, doing, done, high-priority, overdue, and trash
- Stage notes stay below the table so the task area remains large

Import:

- The top "Data" menu contains prototype export/import and Android plain JSON import.
- Encrypted Android import is intentionally disabled in the browser prototype.

This lets the product shape evolve before the full Tauri shell is introduced.

## Electron Desktop Shell

The current packaged desktop app uses Electron because it can reuse the existing
Node bridge for LAN discovery, clipboard sync, record sync, and file transfer.

Development run:

```powershell
cd desktop
npm install
npm start
```

Windows portable build:

```powershell
cd desktop
npm run dist
```

Or double-click:

```text
desktop/build-electron.bat
```

Build output:

```text
desktop/dist/Clipboard Desktop 1.1.0.exe
desktop/dist/win-unpacked/Clipboard Desktop.exe
```

macOS unsigned build:

```bash
cd desktop
npm install
npm run dist:mac
```

Or run:

```bash
chmod +x build-mac.sh
./build-mac.sh
```

The macOS build should be run on a Mac. The current config creates unsigned
`.dmg` and `.zip` packages for local use; publishing to other machines later
should add Apple Developer signing and notarization.

The packaged app stores files received from the phone under:

```text
Documents/ClipboardDesktop/received
```

The packaged app stores note, todo, and research-topic data in:

```text
Documents/ClipboardDesktop/clipboard-data.json
```

When the bridge is not running, the prototype still falls back to browser
`localStorage`. When the bridge starts for the first time and no JSON data file
exists yet, it migrates the current `localStorage` data into `clipboard-data.json`.

## Planned Stack

The planned desktop implementation is:

```text
Tauri
React
TypeScript
SQLite or local JSON
React Flow for research topic graphs
Rust commands for Android-compatible encryption/decryption
```

Recommended future commands after Node.js and Rust are installed:

```powershell
cd desktop
npm create tauri-app@latest .
```

Choose React + TypeScript, then port the prototype data model and views into the generated app.

## Installing Node.js And Rust Later

Node.js is only needed for the real Tauri/React app, not for the current prototype.

Recommended Windows installation options:

```powershell
winget install OpenJS.NodeJS.LTS
winget install Rustlang.Rustup
```

Then open a new PowerShell and verify:

```powershell
node -v
npm -v
rustc --version
cargo --version
```

macOS options:

```bash
brew install node
curl --proto '=https' --tlsv1.2 -sSf https://sh.rustup.rs | sh
```

The final Tauri packaging step can wait until the prototype shape is stable.

## Why Keep It Separate

The Android project is old and should remain stable. The desktop app should:

- Live under `desktop/`
- Use its own `package.json`
- Use its own Tauri/Rust project files
- Share only data formats with Android
- Import/export encrypted Android backups when the compatibility layer is ready

## Important Files

- `prototype/index.html` - runnable UI prototype
- `DATA_COMPATIBILITY.md` - Android export/encryption compatibility notes
- `../RESEARCH_TOPIC_REQUIREMENTS.md` - product requirements for research topics
