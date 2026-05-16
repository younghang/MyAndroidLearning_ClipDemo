# Android Data Compatibility Notes

The desktop app should not try to reuse Android Java code directly. It should reuse the data
format and implement compatible import/export.

## Android List Export Shape

Android exports list records as a JSON object with an array named like the app's list-data key.
Each record contains:

```text
remark
content
datetime
orderid
catalogue
```

The desktop app only needs a subset at first:

- simple notes
- todo boards
- research topics

## Research Topic Content

Research topic records store their actual data in `content` as JSON.

Important fields:

```text
dataType
title
problem
goal
conclusion
viewMode
nodes
```

Node fields:

```text
id
type
title
content
links
relatedNodeIds
mapPositioned
mapX
mapY
```

## Todo Content

Todo records store board/task JSON in `content`. The desktop app should preserve unknown fields
instead of dropping them, so Android can still read older or richer data.

## Android Encryption

The Android backup encryption currently uses:

```text
AESUtils.des(jsonText, FileUtils.SEED, Cipher.ENCRYPT_MODE)
```

Important compatibility details from Android:

- Password comes from `FileUtils.SEED`.
- Key is derived with `InsecureSHA1PRNGKeyDerivator.deriveInsecureKey(passwordBytes, 32)`.
- Cipher is requested as `Cipher.getInstance("AES")`.
- Android output is an uppercase hex string.
- Decryption reverses hex -> bytes -> AES decrypt -> UTF-8 JSON text.

The desktop app should implement this in the Tauri Rust backend, not in the React UI.

No real user password, backup password, or secret key should be committed to this repository.
`FileUtils.SEED` is only the Android variable name that receives the password at runtime. It is
not a hard-coded secret and should never be replaced by a real password in docs or source code.

The browser prototype intentionally does not decrypt encrypted Android files. It can import only
plain JSON. Encrypted import should wait for the Tauri backend so the password is handled by a
native command and never stored in frontend code.

## Desktop Local Database

The Electron/bridge version stores desktop records in a plain JSON file:

```text
Documents/ClipboardDesktop/clipboard-data.json
```

The browser-only prototype still uses `localStorage` as a fallback. When the bridge is running,
the UI loads from `/api/data/load` and saves to `/api/data/save`; if the JSON file does not exist
yet, the current fallback data is written into the file on startup.

## Desktop Peer Link V2

Phone link compatibility stays on the existing Android protocol. Desktop-to-desktop transfer uses
the newer peer protocol in `desktop/bridge/server.js`.

Discovery still uses UDP broadcast:

```text
CLIPBOARD_DESKTOP_DISCOVER_V1 -> UDP 20311
CLIPBOARD_DESKTOP|{ protocol, deviceId, name, platform, port, requiresPairCode }
```

After discovery, one desktop opens a TCP connection to the other desktop on port `20313`.
The first message is `desktop_hello`; it must include the 6 digit pair code shown on the target
desktop. If the code is correct, the target replies with `desktop_hello_ack`.

Desktop V2 supports multiple active desktop peers at the same time. HTTP bridge calls that send to
another desktop can include `deviceId` or `connectionId` to choose the target peer. Supported
desktop message types:

```text
clipboard_push
message_push
record_push
file_start
file_chunk
file_end
disconnect
```

Files are sent as small base64 chunks over the persistent TCP peer connection. This is not as
efficient as raw binary streaming, but it avoids loading the whole file into memory and keeps the
protocol easy to debug.

## Safer Future Format

For long-term desktop-first backups, prefer a new explicit format:

```text
PBKDF2 or Argon2 key derivation
AES-GCM
random salt
random nonce
versioned JSON envelope
```

But the first goal is Android compatibility, so keep the legacy format available.
