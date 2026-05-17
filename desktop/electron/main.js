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
  const storage = prepareStorageDirs();
  process.env.CLIPBOARD_DESKTOP_DATA_DIR = storage.dataDir;
  process.env.CLIPBOARD_DESKTOP_RECEIVED_DIR = storage.receivedDir;
  process.env.CLIPBOARD_DESKTOP_PROJECTS_DIR = storage.projectsDir;
  if (storage.warning) {
    process.env.CLIPBOARD_DESKTOP_STORAGE_WARNING = storage.warning;
  } else {
    delete process.env.CLIPBOARD_DESKTOP_STORAGE_WARNING;
  }

  const bridge = require("../bridge/server");
  bridge.startAll();
}

function prepareStorageDirs() {
  const appRoot = path.join(executableBaseDir(), "ClipboardDesktop");
  try {
    ensureWritableStorage(appRoot);
    migrateDocumentsStorage(appRoot);
    return storageDirs(appRoot, "");
  } catch (error) {
    const fallbackRoot = path.join(app.getPath("documents"), "ClipboardDesktop");
    ensureWritableStorage(fallbackRoot);
    return storageDirs(
      fallbackRoot,
      `exe 所在目录不可写，已临时改用文档目录保存数据：${fallbackRoot}。把 exe 放到可写目录后，数据会保存到 exe 旁边的 ClipboardDesktop 文件夹。原错误：${error.message}`,
    );
  }
}

function executableBaseDir() {
  // electron-builder portable runs from a temp unpack dir; these point to the outer exe.
  if (app.isPackaged && process.env.PORTABLE_EXECUTABLE_DIR) {
    return process.env.PORTABLE_EXECUTABLE_DIR;
  }
  if (app.isPackaged && process.env.PORTABLE_EXECUTABLE_FILE) {
    return path.dirname(process.env.PORTABLE_EXECUTABLE_FILE);
  }
  if (app.isPackaged) {
    return path.dirname(app.getPath("exe"));
  }
  return path.resolve(__dirname, "..");
}

function storageDirs(rootDir, warning) {
  return {
    rootDir,
    dataDir: path.join(rootDir, "data"),
    receivedDir: path.join(rootDir, "received"),
    projectsDir: path.join(rootDir, "projects"),
    warning,
  };
}

function ensureWritableStorage(rootDir) {
  const dirs = [
    rootDir,
    path.join(rootDir, "data"),
    path.join(rootDir, "received"),
    path.join(rootDir, "projects"),
  ];
  dirs.forEach((dir) => fs.mkdirSync(dir, { recursive: true }));
  const testPath = path.join(rootDir, `.write-test-${process.pid}-${Date.now()}`);
  fs.writeFileSync(testPath, "ok", "utf8");
  fs.unlinkSync(testPath);
}

function migrateDocumentsStorage(targetRoot) {
  const sourceRoot = path.join(app.getPath("documents"), "ClipboardDesktop");
  if (path.resolve(sourceRoot) === path.resolve(targetRoot) || !fs.existsSync(sourceRoot)) return;
  copyFileIfMissing(
    path.join(sourceRoot, "clipboard-desktop.sqlite"),
    path.join(targetRoot, "data", "clipboard-desktop.sqlite"),
  );
  copyFileIfMissing(
    path.join(sourceRoot, "clipboard-data.json"),
    path.join(targetRoot, "data", "clipboard-data.json"),
  );
  copyFileIfMissing(
    path.join(sourceRoot, "desktop-device-id.txt"),
    path.join(targetRoot, "data", "desktop-device-id.txt"),
  );
  copyDirectoryContentsIfMissing(
    path.join(sourceRoot, "received"),
    path.join(targetRoot, "received"),
  );
  copyDirectoryContentsIfMissing(
    path.join(sourceRoot, "projects"),
    path.join(targetRoot, "projects"),
  );
}

function copyFileIfMissing(source, target) {
  if (!fs.existsSync(source) || fs.existsSync(target)) return;
  fs.mkdirSync(path.dirname(target), { recursive: true });
  fs.copyFileSync(source, target);
}

function copyDirectoryContentsIfMissing(sourceDir, targetDir) {
  if (!fs.existsSync(sourceDir)) return;
  fs.mkdirSync(targetDir, { recursive: true });
  fs.readdirSync(sourceDir, { withFileTypes: true }).forEach((entry) => {
    const source = path.join(sourceDir, entry.name);
    const target = path.join(targetDir, entry.name);
    if (entry.isDirectory()) {
      copyDirectoryContentsIfMissing(source, target);
      return;
    }
    copyFileIfMissing(source, target);
  });
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
