const { app, BrowserWindow, shell } = require("electron");
const fs = require("fs");
const path = require("path");

const HTTP_PORT = 20312;

let mainWindow = null;

const gotLock = app.requestSingleInstanceLock();
if (!gotLock) {
  app.quit();
}

function startBridge() {
  const desktopDataDir = path.join(app.getPath("documents"), "ClipboardDesktop");
  const receivedDir = path.join(desktopDataDir, "received");
  fs.mkdirSync(desktopDataDir, { recursive: true });
  fs.mkdirSync(receivedDir, { recursive: true });
  process.env.CLIPBOARD_DESKTOP_DATA_DIR = desktopDataDir;
  process.env.CLIPBOARD_DESKTOP_RECEIVED_DIR = receivedDir;

  const bridge = require("../bridge/server");
  bridge.startAll();
}

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1280,
    height: 820,
    minWidth: 980,
    minHeight: 640,
    title: "Clipboard Desktop",
    backgroundColor: "#eef2f7",
    autoHideMenuBar: true,
    show: false,
    webPreferences: {
      contextIsolation: true,
      nodeIntegration: false,
    },
  });

  mainWindow.once("ready-to-show", () => {
    mainWindow.show();
  });

  mainWindow.webContents.setWindowOpenHandler(({ url }) => {
    shell.openExternal(url);
    return { action: "deny" };
  });

  setTimeout(() => {
    mainWindow.loadURL(`http://127.0.0.1:${HTTP_PORT}`);
  }, 250);
}

app.on("second-instance", () => {
  if (!mainWindow) return;
  if (mainWindow.isMinimized()) mainWindow.restore();
  mainWindow.focus();
});

app.whenReady().then(() => {
  startBridge();
  createWindow();
});

app.on("activate", () => {
  if (BrowserWindow.getAllWindows().length === 0) {
    createWindow();
  }
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    app.quit();
  }
});
