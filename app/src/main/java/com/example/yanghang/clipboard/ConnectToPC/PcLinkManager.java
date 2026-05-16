package com.example.yanghang.clipboard.ConnectToPC;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.provider.OpenableColumns;
import android.util.Base64;
import android.util.Log;

import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.MainFormActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PcLinkManager {
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_DISCOVERING = 1;
    public static final int STATE_CONNECTED = 2;

    private static final int DISCOVERY_PORT = 20311;
    private static final int DISCOVERY_TIMEOUT_MS = 900;
    private static final int DISCOVERY_ROUNDS = 4;
    private static final int CONNECT_TIMEOUT_MS = 4000;
    private static final int FILE_CHUNK_BYTES = 48 * 1024;
    private static final String DISCOVERY_REQUEST = "CLIPBOARD_LINK_DISCOVER_V1";
    private static final String DISCOVERY_RESPONSE_PREFIX = "CLIPBOARD_LINK_PC|";

    private static PcLinkManager instance;

    private final Context appContext;
    private final Object sendLock = new Object();
    private Listener listener;
    private Socket socket;
    private BufferedWriter writer;
    private BufferedReader reader;
    private volatile int state = STATE_DISCONNECTED;
    private String pcName = "";
    private String deviceId;
    private String lastClipboardText = "";
    private long lastClipboardTime = 0;
    private volatile int requestVersion = 0;
    private final Map<String, IncomingFileTransfer> incomingFileTransfers = new HashMap<>();

    public interface Listener {
        void onStateChanged(int state, String message, String pcName);
        void onClipboardFromPc(String text);
        void onMessageFromPc(String text);
        void onRecordUpdateFromPc(ListData data);
        void onRecordInsertFromPc(ListData data);
        void onFileFromPc(String filePath);
    }

    private PcLinkManager(Context context) {
        appContext = context.getApplicationContext();
        deviceId = Settings.Secure.getString(appContext.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (deviceId == null || deviceId.trim().equals("")) {
            deviceId = UUID.randomUUID().toString();
        }
    }

    public static synchronized PcLinkManager getInstance(Context context) {
        if (instance == null) {
            instance = new PcLinkManager(context);
        }
        return instance;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
        notifyState(state, "", pcName);
    }

    public boolean isConnected() {
        return state == STATE_CONNECTED && socket != null && socket.isConnected() && !socket.isClosed();
    }

    public boolean isDiscovering() {
        return state == STATE_DISCOVERING;
    }

    public String getPcName() {
        return pcName;
    }

    public void discoverAndConnect() {
        if (state == STATE_DISCOVERING || isConnected()) {
            notifyState(state, stateMessage(), pcName);
            return;
        }
        final int version = ++requestVersion;
        setState(STATE_DISCOVERING, "正在发现电脑", "");
        new Thread(new Runnable() {
            @Override
            public void run() {
                DiscoveryResult result = discoverPc();
                if (version != requestVersion || state != STATE_DISCOVERING) {
                    return;
                }
                if (result == null) {
                    closeSocketOnly();
                    setState(STATE_DISCONNECTED, "没有发现电脑，请确认电脑端已打开并在同一局域网", "");
                    return;
                }
                connectToPc(result, version);
            }
        }).start();
    }

    public void disconnect() {
        requestVersion++;
        final boolean shouldSendDisconnect = isConnected();
        if (shouldSendDisconnect) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendDisconnectMessage();
                    closeSocketOnly();
                }
            }).start();
        } else {
            closeSocketOnly();
        }
        setState(STATE_DISCONNECTED, "已断开电脑连接", "");
    }

    public boolean sendClipboard(String text) {
        if (text == null || text.trim().equals("")) {
            return false;
        }
        long now = System.currentTimeMillis();
        if (text.equals(lastClipboardText) && now - lastClipboardTime < 800) {
            return true;
        }
        lastClipboardText = text;
        lastClipboardTime = now;
        try {
            final JSONObject jsonObject = createBaseMessage("clipboard_push");
            jsonObject.put("text", text);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    sendJson(jsonObject);
                }
            }).start();
            return true;
        } catch (Exception e) {
            handleConnectionError("剪贴板同步失败", e);
            return false;
        }
    }

    public boolean sendRecord(ListData data) {
        if (data == null) {
            return false;
        }
        try {
            JSONObject jsonObject = createBaseMessage("record_push");
            jsonObject.put("recordId", String.valueOf(data.getOrderID()));
            jsonObject.put("orderId", data.getOrderID());
            jsonObject.put("catalogue", nullToEmpty(data.getCatalogue()));
            jsonObject.put("remarks", nullToEmpty(data.getRemarks()));
            jsonObject.put("content", nullToEmpty(data.getContent()));
            jsonObject.put("simpleContent", nullToEmpty(data.getSimpleContent()));
            jsonObject.put("createDate", nullToEmpty(data.getCreateDate()));
            return sendJson(jsonObject);
        } catch (Exception e) {
            handleConnectionError("记录发送失败", e);
            return false;
        }
    }

    public boolean sendFile(Uri uri) {
        if (uri == null || !isConnected()) {
            return false;
        }
        InputStream inputStream = null;
        try {
            long fileSize = getFileSize(uri);
            String transferId = UUID.randomUUID().toString();
            String fileName = getFileName(uri);
            String mimeType = nullToEmpty(appContext.getContentResolver().getType(uri));
            JSONObject start = createBaseMessage("file_start");
            start.put("transferId", transferId);
            start.put("fileName", fileName);
            start.put("mimeType", mimeType);
            start.put("size", fileSize);
            if (!sendJson(start)) {
                return false;
            }

            inputStream = appContext.getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                return false;
            }
            byte[] buffer = new byte[FILE_CHUNK_BYTES];
            int count;
            int index = 0;
            long offset = 0;
            while ((count = inputStream.read(buffer)) != -1) {
                JSONObject chunk = createBaseMessage("file_chunk");
                chunk.put("transferId", transferId);
                chunk.put("fileName", fileName);
                chunk.put("mimeType", mimeType);
                chunk.put("size", fileSize);
                chunk.put("index", index);
                chunk.put("offset", offset);
                chunk.put("base64", Base64.encodeToString(buffer, 0, count, Base64.NO_WRAP));
                if (!sendJson(chunk)) {
                    return false;
                }
                index++;
                offset += count;
            }

            JSONObject end = createBaseMessage("file_end");
            end.put("transferId", transferId);
            end.put("fileName", fileName);
            end.put("mimeType", mimeType);
            end.put("size", fileSize);
            end.put("chunks", index);
            return sendJson(end);
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send file to pc failed: " + e.getMessage());
            return false;
        } finally {
            closeQuietly(inputStream);
        }
    }

    public boolean sendRecordUpdateAck(int orderId, boolean updated) {
        try {
            JSONObject jsonObject = createBaseMessage("record_update_ack");
            jsonObject.put("orderId", orderId);
            jsonObject.put("updated", updated);
            jsonObject.put("message", updated ? "updated" : "record not found");
            return sendJson(jsonObject);
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send record update ack failed: " + e.getMessage());
            return false;
        }
    }

    public boolean sendRecordInsertAck(int orderId, boolean inserted) {
        try {
            JSONObject jsonObject = createBaseMessage("record_insert_ack");
            jsonObject.put("orderId", orderId);
            jsonObject.put("inserted", inserted);
            jsonObject.put("message", inserted ? "inserted" : "insert failed");
            return sendJson(jsonObject);
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send record insert ack failed: " + e.getMessage());
            return false;
        }
    }

    private DiscoveryResult discoverPc() {
        DatagramSocket datagramSocket = null;
        try {
            datagramSocket = new DatagramSocket();
            datagramSocket.setBroadcast(true);
            datagramSocket.setSoTimeout(DISCOVERY_TIMEOUT_MS);
            byte[] requestBytes = DISCOVERY_REQUEST.getBytes("UTF-8");
            byte[] buffer = new byte[1024];
            for (int i = 0; i < DISCOVERY_ROUNDS; i++) {
                sendDiscoveryPacket(datagramSocket, requestBytes);
                long endTime = System.currentTimeMillis() + DISCOVERY_TIMEOUT_MS;
                while (System.currentTimeMillis() < endTime) {
                    DatagramPacket response = new DatagramPacket(buffer, buffer.length);
                    try {
                        datagramSocket.receive(response);
                        DiscoveryResult result = parseDiscoveryResponse(response);
                        if (result != null) {
                            return result;
                        }
                    } catch (Exception ignored) {
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "discover pc failed: " + e.getMessage());
        } finally {
            if (datagramSocket != null) {
                datagramSocket.close();
            }
        }
        return null;
    }

    private void sendDiscoveryPacket(DatagramSocket datagramSocket, byte[] requestBytes) {
        try {
            DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length,
                    InetAddress.getByName("255.255.255.255"), DISCOVERY_PORT);
            datagramSocket.send(packet);
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send default broadcast failed: " + e.getMessage());
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                List<InterfaceAddress> addresses = networkInterface.getInterfaceAddresses();
                for (int i = 0; i < addresses.size(); i++) {
                    InetAddress broadcast = addresses.get(i).getBroadcast();
                    if (broadcast == null) {
                        continue;
                    }
                    DatagramPacket packet = new DatagramPacket(requestBytes, requestBytes.length, broadcast, DISCOVERY_PORT);
                    datagramSocket.send(packet);
                }
            }
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send interface broadcast failed: " + e.getMessage());
        }
    }

    private DiscoveryResult parseDiscoveryResponse(DatagramPacket packet) {
        try {
            String response = new String(packet.getData(), packet.getOffset(), packet.getLength(), "UTF-8");
            if (!response.startsWith(DISCOVERY_RESPONSE_PREFIX)) {
                return null;
            }
            String[] parts = response.split("\\|");
            if (parts.length < 4) {
                return null;
            }
            DiscoveryResult result = new DiscoveryResult();
            result.host = packet.getAddress().getHostAddress();
            result.name = parts[2];
            result.port = Integer.parseInt(parts[3]);
            return result;
        } catch (Exception e) {
            return null;
        }
    }

    private void connectToPc(DiscoveryResult result, int version) {
        try {
            Socket newSocket = new Socket();
            newSocket.connect(new InetSocketAddress(result.host, result.port), CONNECT_TIMEOUT_MS);
            if (version != requestVersion || state != STATE_DISCOVERING) {
                newSocket.close();
                return;
            }
            newSocket.setKeepAlive(true);
            socket = newSocket;
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            pcName = result.name;
            sendJson(createBaseMessage("hello"));
            startReadLoop();
            setState(STATE_CONNECTED, "已连接电脑：" + pcName, pcName);
        } catch (Exception e) {
            handleConnectionError("连接电脑失败", e);
        }
    }

    private void startReadLoop() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean errorHandled = false;
                try {
                    String line;
                    while (reader != null && (line = reader.readLine()) != null) {
                        handleIncomingMessage(line);
                    }
                } catch (Exception e) {
                    if (state == STATE_CONNECTED) {
                        errorHandled = true;
                        handleConnectionError("电脑连接已断开", e);
                    }
                } finally {
                    if (!errorHandled && state == STATE_CONNECTED) {
                        closeSocketOnly();
                        setState(STATE_DISCONNECTED, "电脑连接已断开", "");
                    }
                }
            }
        }).start();
    }

    private void handleIncomingMessage(String line) {
        try {
            JSONObject jsonObject = new JSONObject(line);
            String type = jsonObject.optString("type", "");
            if ("clipboard_push".equals(type)) {
                Listener currentListener = listener;
                if (currentListener != null) {
                    currentListener.onClipboardFromPc(jsonObject.optString("text", ""));
                }
            } else if ("message_push".equals(type)) {
                Listener currentListener = listener;
                if (currentListener != null) {
                    currentListener.onMessageFromPc(jsonObject.optString("text", ""));
                }
            } else if ("record_update".equals(type)) {
                ListData data = parseRecordUpdate(jsonObject);
                Listener currentListener = listener;
                if (data != null && currentListener != null) {
                    currentListener.onRecordUpdateFromPc(data);
                }
            } else if ("record_insert".equals(type)) {
                ListData data = parseRecordInsert(jsonObject);
                Listener currentListener = listener;
                if (data != null && currentListener != null) {
                    currentListener.onRecordInsertFromPc(data);
                }
            } else if ("file_start".equals(type)) {
                startIncomingFile(jsonObject);
            } else if ("file_chunk".equals(type)) {
                appendIncomingFile(jsonObject);
            } else if ("file_end".equals(type)) {
                String filePath = finishIncomingFile(jsonObject);
                Listener currentListener = listener;
                if (filePath != null && currentListener != null) {
                    currentListener.onFileFromPc(filePath);
                }
            } else if ("file_push".equals(type)) {
                String filePath = saveIncomingFile(jsonObject);
                Listener currentListener = listener;
                if (filePath != null && currentListener != null) {
                    currentListener.onFileFromPc(filePath);
                }
            }
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "pc link read message failed: " + e.getMessage());
        }
    }

    private void startIncomingFile(JSONObject jsonObject) {
        try {
            String transferId = jsonObject.optString("transferId", UUID.randomUUID().toString());
            String fileName = sanitizeFileName(jsonObject.optString("fileName", "file"));
            File baseDir = getPhoneLinkDir();
            File file = availableFile(baseDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            IncomingFileTransfer transfer = new IncomingFileTransfer();
            transfer.transferId = transferId;
            transfer.file = file;
            transfer.outputStream = outputStream;
            incomingFileTransfers.put(transferId, transfer);
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "start incoming file failed: " + e.getMessage());
        }
    }

    private void appendIncomingFile(JSONObject jsonObject) {
        try {
            String transferId = jsonObject.optString("transferId", "");
            IncomingFileTransfer transfer = incomingFileTransfers.get(transferId);
            if (transfer == null || transfer.outputStream == null) {
                return;
            }
            byte[] bytes = Base64.decode(jsonObject.optString("base64", ""), Base64.DEFAULT);
            transfer.outputStream.write(bytes);
            transfer.received += bytes.length;
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "append incoming file failed: " + e.getMessage());
        }
    }

    private String finishIncomingFile(JSONObject jsonObject) {
        String transferId = jsonObject.optString("transferId", "");
        IncomingFileTransfer transfer = incomingFileTransfers.remove(transferId);
        if (transfer == null) {
            return null;
        }
        try {
            if (transfer.outputStream != null) {
                transfer.outputStream.flush();
                transfer.outputStream.close();
            }
            return transfer.file == null ? null : transfer.file.getAbsolutePath();
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "finish incoming file failed: " + e.getMessage());
            return null;
        }
    }

    private String saveIncomingFile(JSONObject jsonObject) {
        try {
            String fileName = sanitizeFileName(jsonObject.optString("fileName", "file"));
            String base64 = jsonObject.optString("base64", "");
            if (base64.trim().equals("")) {
                return null;
            }
            byte[] bytes = Base64.decode(base64, Base64.DEFAULT);
            File baseDir = getPhoneLinkDir();
            File file = availableFile(baseDir, fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            outputStream.write(bytes);
            outputStream.flush();
            outputStream.close();
            return file.getAbsolutePath();
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "save incoming file failed: " + e.getMessage());
            return null;
        }
    }

    private File getPhoneLinkDir() {
        File externalDir = appContext.getExternalFilesDir(null);
        if (externalDir == null) {
            externalDir = appContext.getFilesDir();
        }
        File baseDir = new File(externalDir, "PhoneLink");
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        return baseDir;
    }

    private File availableFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if (!file.exists()) {
            return file;
        }
        int dotIndex = fileName.lastIndexOf('.');
        String name = dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
        String ext = dotIndex > 0 ? fileName.substring(dotIndex) : "";
        int index = 1;
        while (file.exists()) {
            file = new File(dir, name + "(" + index + ")" + ext);
            index++;
        }
        return file;
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.trim().equals("")) {
            return "file";
        }
        return fileName.replace("\\", "_").replace("/", "_").replace(":", "_")
                .replace("*", "_").replace("?", "_").replace("\"", "_")
                .replace("<", "_").replace(">", "_").replace("|", "_");
    }

    private String getFileName(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = appContext.getContentResolver().query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (index >= 0) {
                    String name = cursor.getString(index);
                    if (name != null && !name.trim().equals("")) {
                        return sanitizeFileName(name);
                    }
                }
            }
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "get file name failed: " + e.getMessage());
        } finally {
            closeQuietly(cursor);
        }
        String name = uri.getLastPathSegment();
        if (name == null || name.trim().equals("")) {
            name = "file";
        }
        int split = name.lastIndexOf('/');
        if (split >= 0 && split < name.length() - 1) {
            name = name.substring(split + 1);
        }
        return sanitizeFileName(name);
    }

    private long getFileSize(Uri uri) {
        Cursor cursor = null;
        try {
            cursor = appContext.getContentResolver().query(uri, new String[]{OpenableColumns.SIZE}, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (index >= 0 && !cursor.isNull(index)) {
                    return cursor.getLong(index);
                }
            }
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "get file size failed: " + e.getMessage());
        } finally {
            closeQuietly(cursor);
        }
        return -1;
    }

    private void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception ignored) {
        }
    }

    private ListData parseRecordUpdate(JSONObject jsonObject) {
        JSONObject record = jsonObject.optJSONObject("record");
        if (record == null) {
            record = jsonObject;
        }
        int orderId = -1;
        if (record.has("orderId")) {
            orderId = record.optInt("orderId", -1);
        } else if (record.has("androidOrderId")) {
            orderId = record.optInt("androidOrderId", -1);
        } else if (record.has("recordId")) {
            try {
                orderId = Integer.parseInt(record.optString("recordId", "-1"));
            } catch (Exception ignored) {
            }
        }
        if (orderId < 0) {
            Log.v(MainFormActivity.TAG, "record_update missing orderId");
            return null;
        }
        String catalogue = record.optString("catalogue", record.optString("androidCatalogue", "default"));
        String remarks = record.optString("remarks", record.optString("title", ""));
        String content = record.optString("content", "");
        String createDate = record.optString("createDate", record.optString("updatedAt", ListData.GetDate()));
        return new ListData(remarks, content, createDate, orderId, catalogue);
    }

    private ListData parseRecordInsert(JSONObject jsonObject) {
        JSONObject record = jsonObject.optJSONObject("record");
        if (record == null) {
            record = jsonObject;
        }
        String catalogue = record.optString("catalogue", record.optString("androidCatalogue", "default"));
        String remarks = record.optString("remarks", record.optString("title", ""));
        String content = record.optString("content", "");
        String createDate = record.optString("createDate", record.optString("updatedAt", ListData.GetDate()));
        return new ListData(remarks, content, createDate, -1, catalogue);
    }

    private JSONObject createBaseMessage(String type) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("protocol", 1);
        jsonObject.put("type", type);
        jsonObject.put("deviceId", deviceId);
        jsonObject.put("deviceName", Build.MODEL == null ? "Android" : Build.MODEL);
        jsonObject.put("messageId", UUID.randomUUID().toString());
        jsonObject.put("time", System.currentTimeMillis());
        return jsonObject;
    }

    private boolean sendJson(JSONObject jsonObject) {
        if (!isConnected() && !"hello".equals(jsonObject.optString("type"))) {
            return false;
        }
        synchronized (sendLock) {
            try {
                writer.write(jsonObject.toString());
                writer.write("\n");
                writer.flush();
                return true;
            } catch (Exception e) {
                handleConnectionError("发送到电脑失败", e);
                return false;
            }
        }
    }

    private void sendDisconnectMessage() {
        if (writer == null) {
            return;
        }
        try {
            writeJsonDirect(createBaseMessage("disconnect"));
        } catch (Exception e) {
            Log.v(MainFormActivity.TAG, "send disconnect failed: " + e.getMessage());
        }
    }

    private void writeJsonDirect(JSONObject jsonObject) throws Exception {
        synchronized (sendLock) {
            writer.write(jsonObject.toString());
            writer.write("\n");
            writer.flush();
        }
    }

    private void handleConnectionError(String message, Exception e) {
        Log.v(MainFormActivity.TAG, message + ": " + e.getMessage());
        closeSocketOnly();
        setState(STATE_DISCONNECTED, message, "");
    }

    private void closeSocketOnly() {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (Exception ignored) {
        }
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception ignored) {
        }
        reader = null;
        writer = null;
        socket = null;
    }

    private void setState(int state, String message, String pcName) {
        this.state = state;
        this.pcName = pcName == null ? "" : pcName;
        notifyState(state, message, this.pcName);
    }

    private void notifyState(int state, String message, String pcName) {
        Listener currentListener = listener;
        if (currentListener != null) {
            currentListener.onStateChanged(state, message, pcName);
        }
    }

    private String stateMessage() {
        if (state == STATE_CONNECTED) {
            return "已连接电脑：" + pcName;
        }
        if (state == STATE_DISCOVERING) {
            return "正在发现电脑";
        }
        return "未连接电脑";
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static class DiscoveryResult {
        String host;
        int port;
        String name;
    }

    private static class IncomingFileTransfer {
        String transferId;
        File file;
        FileOutputStream outputStream;
        long received;
    }
}
