using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Windows;

namespace FileServer
{
    class LanLinkServer
    {
        public const int TcpPort = 20310;
        public const int UdpPort = 20311;
        private const string DiscoveryRequest = "CLIPBOARD_LINK_DISCOVER_V1";
        private const string DiscoveryResponsePrefix = "CLIPBOARD_LINK_PC|";
        private static readonly Encoding Utf8NoBom = new UTF8Encoding(false);

        private volatile bool isRunning;
        private UdpClient udpClient;
        private TcpListener tcpListener;
        private Thread udpThread;
        private Thread tcpThread;
        private readonly object transferLock = new object();
        private readonly Dictionary<string, IncomingFileTransfer> incomingFileTransfers =
            new Dictionary<string, IncomingFileTransfer>();

        public event UpdataInfo UpdateMessage;

        public void Start()
        {
            if (isRunning)
            {
                return;
            }
            isRunning = true;
            udpThread = new Thread(RunDiscoveryServer);
            udpThread.IsBackground = true;
            udpThread.Start();

            tcpThread = new Thread(RunCommandServer);
            tcpThread.IsBackground = true;
            tcpThread.Start();
            Log("局域网连接服务已启动 TCP:" + TcpPort + " UDP:" + UdpPort);
        }

        public void Close()
        {
            isRunning = false;
            try
            {
                if (udpClient != null)
                {
                    udpClient.Close();
                }
            }
            catch
            {
            }
            try
            {
                if (tcpListener != null)
                {
                    tcpListener.Stop();
                }
            }
            catch
            {
            }
        }

        private void RunDiscoveryServer()
        {
            try
            {
                udpClient = new UdpClient(UdpPort);
                udpClient.EnableBroadcast = true;
                while (isRunning)
                {
                    IPEndPoint remote = new IPEndPoint(IPAddress.Any, 0);
                    byte[] bytes = udpClient.Receive(ref remote);
                    string message = Utf8NoBom.GetString(bytes);
                    if (message != DiscoveryRequest)
                    {
                        continue;
                    }
                    string response = DiscoveryResponsePrefix + "1|" + Environment.MachineName + "|" + TcpPort;
                    byte[] responseBytes = Utf8NoBom.GetBytes(response);
                    udpClient.Send(responseBytes, responseBytes.Length, remote);
                    Log("响应手机发现请求：" + remote.Address);
                }
            }
            catch (Exception ex)
            {
                if (isRunning)
                {
                    Log("局域网发现服务出错：" + ex.Message);
                }
            }
        }

        private void RunCommandServer()
        {
            try
            {
                tcpListener = new TcpListener(IPAddress.Any, TcpPort);
                tcpListener.Start();
                while (isRunning)
                {
                    TcpClient client = tcpListener.AcceptTcpClient();
                    Thread clientThread = new Thread(HandleClient);
                    clientThread.IsBackground = true;
                    clientThread.Start(client);
                }
            }
            catch (Exception ex)
            {
                if (isRunning)
                {
                    Log("局域网连接服务出错：" + ex.Message);
                }
            }
        }

        private void HandleClient(object clientObject)
        {
            TcpClient client = (TcpClient)clientObject;
            string remote = "";
            try
            {
                if (client.Client.RemoteEndPoint != null)
                {
                    remote = client.Client.RemoteEndPoint.ToString();
                }
                Log("手机已连接：" + remote);
                using (NetworkStream stream = client.GetStream())
                using (StreamReader reader = new StreamReader(stream, Utf8NoBom))
                using (StreamWriter writer = new StreamWriter(stream, Utf8NoBom))
                {
                    writer.AutoFlush = true;
                    string line;
                    while (isRunning && (line = reader.ReadLine()) != null)
                    {
                        HandleMessage(line, writer);
                    }
                }
            }
            catch (Exception ex)
            {
                Log("手机连接断开：" + remote + " " + ex.Message);
            }
            finally
            {
                try
                {
                    client.Close();
                }
                catch
                {
                }
            }
        }

        private void HandleMessage(string line, StreamWriter writer)
        {
            LinkMessage message = null;
            try
            {
                message = JsonConvert.DeserializeObject<LinkMessage>(line);
            }
            catch (Exception ex)
            {
                WriteAck(writer, "error", "json解析失败：" + ex.Message);
                return;
            }
            if (message == null || string.IsNullOrEmpty(message.type))
            {
                WriteAck(writer, "error", "消息为空");
                return;
            }
            if (HandleFileMessage(message, writer))
            {
                return;
            }
            if (message.type == "hello")
            {
                Log("手机握手：" + message.deviceName);
                WriteAck(writer, "hello_ack", "ok");
                return;
            }
            if (message.type == "disconnect")
            {
                Log("手机主动断开：" + message.deviceName);
                WriteAck(writer, "disconnect_ack", "ok");
                return;
            }
            if (message.type == "clipboard_push")
            {
                SetClipboardText(message.text);
                Log("手机剪贴板已同步到电脑");
                WriteAck(writer, "clipboard_ack", "ok");
                return;
            }
            if (message.type == "record_push")
            {
                string filePath = SaveRecord(message, line);
                Log("收到手机记录 ID=" + message.recordId + " 已保存：" + filePath);
                WriteAck(writer, "record_ack", filePath);
                return;
            }
            WriteAck(writer, "error", "未知消息：" + message.type);
        }

        private bool HandleFileMessage(LinkMessage message, StreamWriter writer)
        {
            try
            {
                if (message.type == "file_start")
                {
                    StartIncomingFile(message);
                    WriteAck(writer, "file_start_ack", "ok");
                    return true;
                }
                if (message.type == "file_chunk")
                {
                    AppendIncomingFile(message);
                    return true;
                }
                if (message.type == "file_end")
                {
                    string filePath = FinishIncomingFile(message);
                    Log("received phone file: " + filePath);
                    WriteAck(writer, "file_ack", filePath);
                    return true;
                }
                if (message.type == "file_push")
                {
                    string filePath = SaveLegacyFile(message);
                    Log("received phone file: " + filePath);
                    WriteAck(writer, "file_ack", filePath);
                    return true;
                }
            }
            catch (Exception ex)
            {
                Log("file transfer failed: " + ex.Message);
                WriteAck(writer, "file_error", ex.Message);
                return true;
            }
            return false;
        }

        private void SetClipboardText(string text)
        {
            try
            {
                Application.Current.Dispatcher.Invoke(new Action(delegate
                {
                    Clipboard.SetText(text == null ? "" : text);
                }));
            }
            catch (Exception ex)
            {
                Log("写入电脑剪贴板失败：" + ex.Message);
            }
        }

        private string SaveRecord(LinkMessage message, string rawJson)
        {
            string folder = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "ClipboardPcRecords");
            Directory.CreateDirectory(folder);
            string recordId = SafeFileName(string.IsNullOrEmpty(message.recordId) ? message.orderId.ToString() : message.recordId);
            string fileName = "record_" + recordId + "_" + DateTime.Now.ToString("yyyyMMdd_HHmmss") + ".json";
            string filePath = Path.Combine(folder, fileName);
            File.WriteAllText(filePath, rawJson, Encoding.UTF8);
            return filePath;
        }

        private void StartIncomingFile(LinkMessage message)
        {
            string transferId = string.IsNullOrEmpty(message.transferId) ? Guid.NewGuid().ToString() : message.transferId;
            string fileName = SafeFileName(string.IsNullOrEmpty(message.fileName) ? "file" : message.fileName);
            string filePath = AvailableFilePath(GetReceivedFileFolder(), fileName);
            FileStream stream = new FileStream(filePath, FileMode.CreateNew, FileAccess.Write, FileShare.Read);
            IncomingFileTransfer transfer = new IncomingFileTransfer();
            transfer.TransferId = transferId;
            transfer.FilePath = filePath;
            transfer.Stream = stream;
            lock (transferLock)
            {
                IncomingFileTransfer oldTransfer;
                if (incomingFileTransfers.TryGetValue(transferId, out oldTransfer))
                {
                    CloseQuietly(oldTransfer.Stream);
                    incomingFileTransfers.Remove(transferId);
                }
                incomingFileTransfers[transferId] = transfer;
            }
        }

        private void AppendIncomingFile(LinkMessage message)
        {
            if (string.IsNullOrEmpty(message.transferId) || string.IsNullOrEmpty(message.base64))
            {
                return;
            }
            IncomingFileTransfer transfer = null;
            lock (transferLock)
            {
                incomingFileTransfers.TryGetValue(message.transferId, out transfer);
            }
            if (transfer == null || transfer.Stream == null)
            {
                return;
            }
            byte[] bytes = Convert.FromBase64String(message.base64);
            transfer.Stream.Write(bytes, 0, bytes.Length);
            transfer.Received += bytes.Length;
        }

        private string FinishIncomingFile(LinkMessage message)
        {
            if (string.IsNullOrEmpty(message.transferId))
            {
                return "";
            }
            IncomingFileTransfer transfer = null;
            lock (transferLock)
            {
                if (incomingFileTransfers.TryGetValue(message.transferId, out transfer))
                {
                    incomingFileTransfers.Remove(message.transferId);
                }
            }
            if (transfer == null)
            {
                return "";
            }
            CloseQuietly(transfer.Stream);
            return transfer.FilePath;
        }

        private string SaveLegacyFile(LinkMessage message)
        {
            if (string.IsNullOrEmpty(message.base64))
            {
                return "";
            }
            string fileName = SafeFileName(string.IsNullOrEmpty(message.fileName) ? "file" : message.fileName);
            string filePath = AvailableFilePath(GetReceivedFileFolder(), fileName);
            byte[] bytes = Convert.FromBase64String(message.base64);
            File.WriteAllBytes(filePath, bytes);
            return filePath;
        }

        private string GetReceivedFileFolder()
        {
            string folder = Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.MyDocuments), "ClipboardPcFiles");
            Directory.CreateDirectory(folder);
            return folder;
        }

        private string AvailableFilePath(string folder, string fileName)
        {
            string filePath = Path.Combine(folder, fileName);
            if (!File.Exists(filePath))
            {
                return filePath;
            }
            string name = Path.GetFileNameWithoutExtension(fileName);
            string ext = Path.GetExtension(fileName);
            int index = 1;
            do
            {
                filePath = Path.Combine(folder, name + "(" + index + ")" + ext);
                index++;
            } while (File.Exists(filePath));
            return filePath;
        }

        private void CloseQuietly(Stream stream)
        {
            try
            {
                if (stream != null)
                {
                    stream.Flush();
                    stream.Close();
                }
            }
            catch
            {
            }
        }

        private string SafeFileName(string value)
        {
            if (string.IsNullOrEmpty(value))
            {
                return "unknown";
            }
            foreach (char invalidChar in Path.GetInvalidFileNameChars())
            {
                value = value.Replace(invalidChar, '_');
            }
            return value;
        }

        private void WriteAck(StreamWriter writer, string type, string message)
        {
            LinkAck ack = new LinkAck();
            ack.type = type;
            ack.message = message;
            writer.WriteLine(JsonConvert.SerializeObject(ack));
        }

        private void Log(string message)
        {
            if (UpdateMessage != null)
            {
                UpdateMessage(message);
            }
        }

        private class LinkMessage
        {
            public int protocol { get; set; }
            public string type { get; set; }
            public string deviceId { get; set; }
            public string deviceName { get; set; }
            public string messageId { get; set; }
            public long time { get; set; }
            public string text { get; set; }
            public string recordId { get; set; }
            public int orderId { get; set; }
            public string catalogue { get; set; }
            public string remarks { get; set; }
            public string content { get; set; }
            public string simpleContent { get; set; }
            public string createDate { get; set; }
            public string transferId { get; set; }
            public string fileName { get; set; }
            public string mimeType { get; set; }
            public long size { get; set; }
            public int index { get; set; }
            public long offset { get; set; }
            public string base64 { get; set; }
            public int chunks { get; set; }
        }

        private class LinkAck
        {
            public string type { get; set; }
            public string message { get; set; }
        }

        private class IncomingFileTransfer
        {
            public string TransferId { get; set; }
            public string FilePath { get; set; }
            public FileStream Stream { get; set; }
            public long Received { get; set; }
        }
    }
}
