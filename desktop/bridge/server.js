const dgram = require("dgram");
const http = require("http");
const net = require("net");
const fs = require("fs");
const os = require("os");
const path = require("path");
const { spawn } = require("child_process");
const { encryptAndroidText, decryptAndroidText } = require("./androidCrypto");

const HTTP_PORT = 20312;
const TCP_PORT = 20310;
const UDP_PORT = 20311;
const DISCOVERY_REQUEST = "CLIPBOARD_LINK_DISCOVER_V1";
const DISCOVERY_RESPONSE_PREFIX = "CLIPBOARD_LINK_PC|";

const rootDir = process.env.CLIPBOARD_DESKTOP_ROOT || path.resolve(__dirname, "..");
const prototypeDir = process.env.CLIPBOARD_DESKTOP_PROTOTYPE_DIR || path.join(rootDir, "prototype");
const receivedDir = process.env.CLIPBOARD_DESKTOP_RECEIVED_DIR || path.join(rootDir, "received");
const dataDir = process.env.CLIPBOARD_DESKTOP_DATA_DIR || path.join(rootDir, "data");
const dataFilePath = path.join(dataDir, "clipboard-data.json");

let linkedSocket = null;
let linkedDevice = null;
let eventSeq = 0;
const events = [];
let clipboardWriteChain = Promise.resolve();
const incomingFileTransfers = new Map();
const serverStatus = {
  udp: { ok: false, message: "" },
  tcp: { ok: false, message: "" },
  http: { ok: false, message: "" },
};

function markServerPort(name, ok, message) {
  serverStatus[name] = { ok, message: message || "" };
  if (!ok) {
    const port = name === "udp" ? UDP_PORT : name === "tcp" ? TCP_PORT : HTTP_PORT;
    pushEvent("port_error", { name, port, message: message || "port unavailable" });
  }
}

function pushEvent(type, payload) {
  eventSeq += 1;
  const event = {
    seq: eventSeq,
    type,
    time: Date.now(),
    payload: payload || {},
  };
  events.push(event);
  while (events.length > 300) events.shift();
  console.log(`[${new Date().toLocaleTimeString()}] ${type}`, payload || "");
}

function startDiscoveryServer() {
  const udp = dgram.createSocket("udp4");
  udp.on("error", (error) => {
    console.error("UDP discovery error:", error.message);
    markServerPort("udp", false, error.code === "EADDRINUSE"
      ? `UDP ${UDP_PORT} 被占用，手机自动发现不可用`
      : error.message);
    udp.close();
  });
  udp.on("message", (message, remote) => {
    if (message.toString("utf8") !== DISCOVERY_REQUEST) return;
    const response = `${DISCOVERY_RESPONSE_PREFIX}1|Clipboard Desktop|${TCP_PORT}`;
    udp.send(Buffer.from(response, "utf8"), remote.port, remote.address);
    pushEvent("discovery_request", { address: remote.address });
  });
  udp.bind(UDP_PORT, () => {
    udp.setBroadcast(true);
    markServerPort("udp", true, "");
    console.log(`UDP discovery listening on ${UDP_PORT}`);
  });
}

function startPhoneTcpServer() {
  const server = net.createServer((socket) => {
    if (linkedSocket && linkedSocket !== socket) {
      linkedSocket.destroy();
    }
    linkedSocket = socket;
    linkedDevice = {
      address: socket.remoteAddress,
      port: socket.remotePort,
      name: "",
      deviceId: "",
    };
    pushEvent("phone_connected", linkedDevice);

    let buffer = "";
    socket.setEncoding("utf8");
    socket.on("data", (chunk) => {
      buffer += chunk;
      let newlineIndex = buffer.indexOf("\n");
      while (newlineIndex >= 0) {
        const line = buffer.slice(0, newlineIndex).trim();
        buffer = buffer.slice(newlineIndex + 1);
        if (line) handlePhoneMessage(socket, line);
        newlineIndex = buffer.indexOf("\n");
      }
    });
    socket.on("close", () => {
      if (linkedSocket === socket) {
        pushEvent("phone_disconnected", linkedDevice || {});
        linkedSocket = null;
        linkedDevice = null;
      }
    });
    socket.on("error", (error) => {
      pushEvent("phone_error", { message: error.message });
    });
  });
  server.on("error", (error) => {
    markServerPort("tcp", false, error.code === "EADDRINUSE"
      ? `TCP ${TCP_PORT} 被占用，手机连接功能不可用`
      : error.message);
    console.error("Phone link TCP error:", error.message);
  });
  server.listen(TCP_PORT, "0.0.0.0", () => {
    markServerPort("tcp", true, "");
    console.log(`Phone link TCP listening on ${TCP_PORT}`);
  });
}

function handlePhoneMessage(socket, line) {
  let message;
  try {
    message = JSON.parse(line);
  } catch (error) {
    writeLine(socket, { type: "error", message: "invalid json" });
    return;
  }

  if (linkedDevice) {
    linkedDevice.name = message.deviceName || linkedDevice.name;
    linkedDevice.deviceId = message.deviceId || linkedDevice.deviceId;
  }

  if (message.type === "hello") {
    writeLine(socket, { type: "hello_ack", message: "ok" });
    pushEvent("phone_hello", { device: linkedDevice });
    return;
  }

  if (message.type === "disconnect") {
    writeLine(socket, { type: "disconnect_ack", message: "ok" });
    socket.end();
    return;
  }

  if (message.type === "clipboard_push") {
    setSystemClipboard(message.text || "");
    writeLine(socket, { type: "clipboard_ack", message: "ok" });
    pushEvent("clipboard_push", {
      text: message.text || "",
      deviceId: message.deviceId || "",
      messageId: message.messageId || "",
    });
    return;
  }

  if (message.type === "record_push") {
    const filePath = saveReceivedRecord(message);
    writeLine(socket, { type: "record_ack", message: filePath });
    pushEvent("record_push", {
      filePath,
      record: message,
    });
    return;
  }

  if (message.type === "record_update_ack") {
    pushEvent("record_update_ack", {
      orderId: message.orderId,
      updated: Boolean(message.updated),
      message: message.message || "",
      deviceId: message.deviceId || "",
      messageId: message.messageId || "",
    });
    return;
  }

  if (message.type === "record_insert_ack") {
    pushEvent("record_insert_ack", {
      orderId: message.orderId,
      inserted: Boolean(message.inserted),
      message: message.message || "",
      deviceId: message.deviceId || "",
      messageId: message.messageId || "",
    });
    return;
  }

  if (message.type === "file_start") {
    startIncomingFileTransfer(message);
    writeLine(socket, { type: "file_start_ack", transferId: message.transferId || "", message: "ok" });
    return;
  }

  if (message.type === "file_chunk") {
    appendIncomingFileChunk(message);
    return;
  }

  if (message.type === "file_end") {
    const filePath = finishIncomingFileTransfer(message);
    writeLine(socket, { type: "file_ack", transferId: message.transferId || "", message: filePath || "" });
    if (filePath) {
      pushEvent("file_push", {
        filePath,
        fileName: message.fileName || path.basename(filePath),
        size: Number(message.size) || 0,
        deviceId: message.deviceId || "",
        messageId: message.messageId || "",
      });
    }
    return;
  }

  if (message.type === "file_push") {
    const filePath = saveReceivedFile(message);
    writeLine(socket, { type: "file_ack", message: filePath });
    pushEvent("file_push", {
      filePath,
      fileName: message.fileName || "file",
      size: Number(message.size) || 0,
      deviceId: message.deviceId || "",
      messageId: message.messageId || "",
    });
    return;
  }

  writeLine(socket, { type: "error", message: `unknown type: ${message.type}` });
}

function startIncomingFileTransfer(message) {
  const transferId = String(message.transferId || createId());
  const fileDir = path.join(receivedDir, "files");
  fs.mkdirSync(fileDir, { recursive: true });
  const fileName = sanitizeFileName(String(message.fileName || "file"));
  const filePath = availableFilePath(fileDir, fileName);
  const stream = fs.createWriteStream(filePath);
  incomingFileTransfers.set(transferId, {
    transferId,
    filePath,
    fileName,
    stream,
    received: 0,
    size: Number(message.size) || 0,
  });
  pushEvent("file_transfer_start", {
    transferId,
    fileName,
    size: Number(message.size) || 0,
  });
}

function appendIncomingFileChunk(message) {
  const transferId = String(message.transferId || "");
  const transfer = incomingFileTransfers.get(transferId);
  if (!transfer) return;
  const buffer = Buffer.from(String(message.base64 || ""), "base64");
  transfer.stream.write(buffer);
  transfer.received += buffer.length;
}

function finishIncomingFileTransfer(message) {
  const transferId = String(message.transferId || "");
  const transfer = incomingFileTransfers.get(transferId);
  if (!transfer) return "";
  transfer.stream.end();
  incomingFileTransfers.delete(transferId);
  return transfer.filePath;
}

function writeLine(socket, message) {
  try {
    socket.write(`${JSON.stringify(message)}\n`, "utf8");
  } catch (error) {
    pushEvent("phone_error", { message: error.message });
  }
}

function saveReceivedRecord(message) {
  fs.mkdirSync(receivedDir, { recursive: true });
  const recordId = sanitizeFileName(String(message.recordId || message.orderId || "unknown"));
  const filePath = path.join(receivedDir, `record_${recordId}_${formatStamp(new Date())}.json`);
  fs.writeFileSync(filePath, JSON.stringify(message, null, 2), "utf8");
  return filePath;
}

function saveReceivedFile(message) {
  const fileDir = path.join(receivedDir, "files");
  fs.mkdirSync(fileDir, { recursive: true });
  const fileName = sanitizeFileName(String(message.fileName || "file"));
  const filePath = availableFilePath(fileDir, fileName);
  const buffer = Buffer.from(String(message.base64 || ""), "base64");
  fs.writeFileSync(filePath, buffer);
  return filePath;
}

function loadDesktopData() {
  if (!fs.existsSync(dataFilePath)) {
    return { exists: false, path: dataFilePath, state: null };
  }
  const text = fs.readFileSync(dataFilePath, "utf8");
  return {
    exists: true,
    path: dataFilePath,
    state: text.trim() ? JSON.parse(text) : null,
  };
}

function saveDesktopData(state) {
  if (!state || typeof state !== "object" || Array.isArray(state)) {
    throw new Error("invalid state");
  }
  fs.mkdirSync(dataDir, { recursive: true });
  const tempPath = path.join(dataDir, `clipboard-data-${process.pid}-${Date.now()}.tmp`);
  fs.writeFileSync(tempPath, JSON.stringify(state, null, 2), "utf8");
  fs.renameSync(tempPath, dataFilePath);
  return dataFilePath;
}

function availableFilePath(dir, fileName) {
  let filePath = path.join(dir, fileName);
  if (!fs.existsSync(filePath)) return filePath;
  const parsed = path.parse(fileName);
  let index = 1;
  while (fs.existsSync(filePath)) {
    filePath = path.join(dir, `${parsed.name}(${index})${parsed.ext}`);
    index += 1;
  }
  return filePath;
}

function setSystemClipboard(text) {
  clipboardWriteChain = clipboardWriteChain
    .then(() => writeSystemClipboard(text))
    .catch((error) => {
      pushEvent("clipboard_error", { message: error.message });
    });
}

function writeSystemClipboard(text) {
  if (process.platform === "win32") {
    const tempPath = path.join(os.tmpdir(), `clipboard-link-${process.pid}-${Date.now()}.txt`);
    fs.writeFileSync(tempPath, text, "utf8");
    const command = [
      "Add-Type -AssemblyName System.Windows.Forms",
      "$path = $env:CLIPBOARD_LINK_TEMP",
      "$text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)",
      "for ($i = 0; $i -lt 8; $i++) {",
      "  try {",
      "    if ($text.Length -eq 0) { [System.Windows.Forms.Clipboard]::Clear() } else { [System.Windows.Forms.Clipboard]::SetText($text) }",
      "    break",
      "  } catch {",
      "    if ($i -eq 7) { throw }",
      "    Start-Sleep -Milliseconds 120",
      "  }",
      "}",
    ].join("; ");
    return runClipboardProcess("powershell.exe", ["-NoProfile", "-STA", "-ExecutionPolicy", "Bypass", "-Command", command], tempPath);
  }
  if (process.platform === "darwin") {
    return runClipboardProcess("pbcopy", [], null, text);
  }
  return runClipboardProcess("sh", ["-c", "command -v wl-copy >/dev/null && wl-copy || xclip -selection clipboard"], null, text);
}

function runClipboardProcess(command, args, tempPath, stdinText) {
  return new Promise((resolve, reject) => {
    const child = spawn(command, args, {
      stdio: stdinText === undefined ? ["ignore", "ignore", "pipe"] : ["pipe", "ignore", "pipe"],
      windowsHide: true,
      env: tempPath ? { ...process.env, CLIPBOARD_LINK_TEMP: tempPath } : process.env,
    });
    let stderr = "";
    child.stderr?.on("data", (chunk) => {
      stderr += chunk.toString("utf8");
    });
    child.on("error", (error) => {
      cleanupTemp(tempPath);
      reject(error);
    });
    child.on("close", (code) => {
      cleanupTemp(tempPath);
      if (code === 0) {
        resolve();
      } else {
        reject(new Error(stderr.trim() || `clipboard process exited with ${code}`));
      }
    });
    if (stdinText !== undefined) {
      child.stdin.end(stdinText, "utf8");
    }
  });
}

function cleanupTemp(tempPath) {
  if (!tempPath) return;
  try {
    fs.unlinkSync(tempPath);
  } catch (error) {
    // The PowerShell command usually deletes it after a successful clipboard write.
  }
}

function startHttpServer() {
  const server = http.createServer((request, response) => {
    setCors(response);
    if (request.method === "OPTIONS") {
      response.writeHead(204);
      response.end();
      return;
    }

    const url = new URL(request.url, `http://127.0.0.1:${HTTP_PORT}`);
    if (url.pathname === "/api/link/status") {
      sendJson(response, {
        ok: true,
        bridge: true,
        connected: Boolean(linkedSocket && !linkedSocket.destroyed),
        device: linkedDevice,
        ports: { http: HTTP_PORT, tcp: TCP_PORT, udp: UDP_PORT },
        serverStatus,
        nextSeq: eventSeq,
      });
      return;
    }
    if (url.pathname === "/api/link/events") {
      const since = Number(url.searchParams.get("since") || 0);
      sendJson(response, {
        ok: true,
        events: events.filter((event) => event.seq > since),
        nextSeq: eventSeq,
      });
      return;
    }
    if (url.pathname === "/api/link/send-clipboard" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        writeLine(linkedSocket, {
          protocol: 1,
          type: "clipboard_push",
          text: body.text || "",
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/link/send-message" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        writeLine(linkedSocket, {
          protocol: 1,
          type: "message_push",
          text: body.text || "",
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/link/send-record" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        writeLine(linkedSocket, {
          protocol: 1,
          type: "record_update",
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
          record: body.record || body,
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/link/insert-record" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        writeLine(linkedSocket, {
          protocol: 1,
          type: "record_insert",
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
          record: body.record || body,
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/link/send-file" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        writeLine(linkedSocket, {
          protocol: 1,
          type: "file_push",
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
          fileName: body.fileName || "file",
          mimeType: body.mimeType || "application/octet-stream",
          size: Number(body.size) || 0,
          base64: body.base64 || "",
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/link/send-file-chunk" && request.method === "POST") {
      readJson(request, (body) => {
        if (!linkedSocket || linkedSocket.destroyed) {
          sendJson(response, { ok: false, message: "phone is not connected" }, 409);
          return;
        }
        const phase = body.phase || "chunk";
        const type = phase === "start" ? "file_start" : phase === "end" ? "file_end" : "file_chunk";
        writeLine(linkedSocket, {
          protocol: 1,
          type,
          deviceName: os.hostname(),
          messageId: createId(),
          time: Date.now(),
          transferId: body.transferId || "",
          fileName: body.fileName || "file",
          mimeType: body.mimeType || "application/octet-stream",
          size: Number(body.size) || 0,
          index: Number(body.index) || 0,
          offset: Number(body.offset) || 0,
          base64: body.base64 || "",
        });
        sendJson(response, { ok: true });
      });
      return;
    }
    if (url.pathname === "/api/data/load" && request.method === "GET") {
      try {
        const loaded = loadDesktopData();
        sendJson(response, { ok: true, ...loaded });
      } catch (error) {
        sendJson(response, { ok: false, message: error.message }, 500);
      }
      return;
    }
    if (url.pathname === "/api/data/save" && request.method === "POST") {
      readJson(request, (body) => {
        try {
          const filePath = saveDesktopData(body.state);
          sendJson(response, { ok: true, path: filePath });
        } catch (error) {
          sendJson(response, { ok: false, message: error.message }, 400);
        }
      });
      return;
    }
    if (url.pathname === "/api/data/encrypt" && request.method === "POST") {
      readJson(request, (body) => {
        try {
          if (!body.password) throw new Error("missing password");
          sendJson(response, {
            ok: true,
            encrypted: encryptAndroidText(body.text || "", body.password),
          });
        } catch (error) {
          sendJson(response, { ok: false, message: "encrypt failed" }, 400);
        }
      });
      return;
    }
    if (url.pathname === "/api/data/decrypt" && request.method === "POST") {
      readJson(request, (body) => {
        try {
          if (!body.password) throw new Error("missing password");
          sendJson(response, {
            ok: true,
            text: decryptAndroidText(body.encrypted || "", body.password),
          });
        } catch (error) {
          sendJson(response, { ok: false, message: "decrypt failed" }, 400);
        }
      });
      return;
    }
    serveStatic(url.pathname, response);
  });
  server.on("error", (error) => {
    markServerPort("http", false, error.code === "EADDRINUSE"
      ? `HTTP ${HTTP_PORT} 被占用，桌面界面可能无法打开`
      : error.message);
    console.error("Prototype HTTP error:", error.message);
  });
  server.listen(HTTP_PORT, "127.0.0.1", () => {
    markServerPort("http", true, "");
    console.log(`Prototype app: http://127.0.0.1:${HTTP_PORT}`);
    console.log("Keep this window open while using phone link.");
  });
}

function serveStatic(urlPath, response) {
  let relativePath = decodeURIComponent(urlPath);
  if (relativePath === "/" || relativePath === "") relativePath = "/index.html";
  const filePath = path.resolve(prototypeDir, `.${relativePath}`);
  if (!filePath.startsWith(prototypeDir)) {
    response.writeHead(403);
    response.end("Forbidden");
    return;
  }
  fs.readFile(filePath, (error, data) => {
    if (error) {
      response.writeHead(404);
      response.end("Not found");
      return;
    }
    response.writeHead(200, { "Content-Type": contentType(filePath) });
    response.end(data);
  });
}

function readJson(request, callback) {
  let raw = "";
  request.setEncoding("utf8");
  request.on("data", (chunk) => {
    raw += chunk;
    if (raw.length > 1024 * 1024 * 16) request.destroy();
  });
  request.on("end", () => {
    try {
      callback(raw ? JSON.parse(raw) : {});
    } catch (error) {
      callback({});
    }
  });
}

function sendJson(response, data, statusCode) {
  response.writeHead(statusCode || 200, { "Content-Type": "application/json; charset=utf-8" });
  response.end(JSON.stringify(data));
}

function setCors(response) {
  response.setHeader("Access-Control-Allow-Origin", "*");
  response.setHeader("Access-Control-Allow-Headers", "Content-Type");
  response.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
}

function contentType(filePath) {
  if (filePath.endsWith(".html")) return "text/html; charset=utf-8";
  if (filePath.endsWith(".js")) return "application/javascript; charset=utf-8";
  if (filePath.endsWith(".css")) return "text/css; charset=utf-8";
  if (filePath.endsWith(".json")) return "application/json; charset=utf-8";
  return "application/octet-stream";
}

function sanitizeFileName(value) {
  return value.replace(/[\\/:*?"<>|]/g, "_");
}

function formatStamp(date) {
  const pad = (value) => String(value).padStart(2, "0");
  return `${date.getFullYear()}${pad(date.getMonth() + 1)}${pad(date.getDate())}_${pad(date.getHours())}${pad(date.getMinutes())}${pad(date.getSeconds())}`;
}

function createId() {
  return Math.random().toString(36).slice(2) + Date.now().toString(36);
}

function startAll() {
  startDiscoveryServer();
  startPhoneTcpServer();
  startHttpServer();
}

if (require.main === module) {
  startAll();
} else {
  module.exports = {
    startAll,
    ports: {
      http: HTTP_PORT,
      tcp: TCP_PORT,
      udp: UDP_PORT,
    },
  };
}
