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
