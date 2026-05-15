using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;

namespace FileServer
{
    class ClientServer
    {
        public event UpdataInfo UpdateMessage;
        public event OnSuccess OnDisconnect;
        byte[] sendBytes = Encoding.UTF8.GetBytes("hello");
        private Socket myClientSocket;
        private static byte[] result = new byte[1024];
        public ClientServer(Socket socket)
        {
            this.myClientSocket = socket;
        }
     
        public void RecieveMessage()
        {
            try
            {
                //通过clientSocket接收数据
                int receiveNumber = myClientSocket.Receive(result);
                String str = Encoding.UTF8.GetString(result, 0, receiveNumber);
                //Console.WriteLine("接收客户端{0}消息{1}", myClientSocket.RemoteEndPoint.ToString(), str);
                string show = string.Format("接收客户端{0}消息\n{1}", myClientSocket.RemoteEndPoint.ToString(), str);
                //if(UpdateMessage!=null)
                UpdateMessage(show);
                //myClientSocket.Send(sendBytes);
            }
            catch (Exception ex)
            {
                Console.WriteLine(ex.Message);
                myClientSocket.Shutdown(SocketShutdown.Both);
                myClientSocket.Close();

            }
            try
            {
                ReceieveData();

            }
            catch
            {
                UpdateMessage("客户端连接已经关闭,连接异常");
                return;
            }
        }
        private void ReceieveData()
        {
            byte[] a = new byte[1];
            int c = myClientSocket.Receive(a);
            if (c <= 0)
            {
                UpdateMessage("客户端连接已经关闭");
                myClientSocket.Close();
                if (OnDisconnect != null)
                {
                    OnDisconnect();
                }
                return;
            }
            if (a[0] == 0x66)
            {
                myClientSocket.Send(sendBytes);
                ReceiveFile(myClientSocket);
            }
            else
                if (a[0] == 0xfc)
            {
                UpdateMessage("客户端关闭连接");
                myClientSocket.Close();
                if (OnDisconnect != null)
                {
                    OnDisconnect();
                }
                
            }

        }

        public void Close()
        {
            myClientSocket.Shutdown(SocketShutdown.Both);
            myClientSocket.Close();
        }

        private void ReceiveFile(Socket clientSocket)
        {
            string fileName = "";
            try
            {
                clientSocket.ReceiveTimeout = 30000;
                FileInfo fileJson = ReceiveFileInfo(clientSocket);
                fileName = SanitizeFileName(fileJson.fileName);
                string saveFolder = Environment.GetFolderPath(Environment.SpecialFolder.DesktopDirectory);
                if (string.IsNullOrEmpty(saveFolder))
                {
                    saveFolder = AppDomain.CurrentDomain.BaseDirectory;
                }
                Directory.CreateDirectory(saveFolder);
                string filePath = GetAvailableFilePath(saveFolder, fileName);

                using (FileStream fs = new FileStream(filePath, FileMode.CreateNew, FileAccess.Write, FileShare.None))
                {
                    clientSocket.Send(Encoding.UTF8.GetBytes("hello"));
                    byte[] data = new byte[8 * 1024];
                    long size = 0;
                    while (size < fileJson.fileSize)
                    {
                        int readSize = (int)Math.Min(data.Length, fileJson.fileSize - size);
                        int c = clientSocket.Receive(data, 0, readSize, SocketFlags.None);
                        if (c <= 0)
                        {
                            throw new IOException("socket closed before file finished");
                        }
                        fs.Write(data, 0, c);
                        size += c;
                    }
                }

                clientSocket.Send(Encoding.UTF8.GetBytes("hello"));
                UpdateMessage("收到客户端发送的文件 " + filePath);
                ReceieveData();
            }
            catch (Exception ex)
            {
                UpdateMessage("接收文件失败 " + fileName + " " + ex.Message);
                TrySendError(clientSocket);
                try
                {
                    clientSocket.Close();
                }
                catch
                {
                }
                if (OnDisconnect != null)
                {
                    OnDisconnect();
                }
            }
        }

        private FileInfo ReceiveFileInfo(Socket clientSocket)
        {
            byte[] buffer = new byte[1024];
            StringBuilder builder = new StringBuilder();
            while (builder.Length < 8192)
            {
                int count = clientSocket.Receive(buffer);
                if (count <= 0)
                {
                    throw new IOException("socket closed before file info");
                }
                builder.Append(Encoding.UTF8.GetString(buffer, 0, count));
                string fileInfo = builder.ToString().Trim();
                if (!fileInfo.EndsWith("}"))
                {
                    continue;
                }
                FileInfo fileJson;
                try
                {
                    fileJson = JsonConvert.DeserializeObject<FileInfo>(fileInfo);
                }
                catch
                {
                    continue;
                }
                if (fileJson == null || string.IsNullOrEmpty(fileJson.fileName) || fileJson.fileSize < 0)
                {
                    throw new IOException("invalid file info");
                }
                return fileJson;
            }
            throw new IOException("file info is too large");
        }

        private string SanitizeFileName(string fileName)
        {
            if (string.IsNullOrEmpty(fileName))
            {
                return "file";
            }
            foreach (char invalidChar in Path.GetInvalidFileNameChars())
            {
                fileName = fileName.Replace(invalidChar, '_');
            }
            return fileName;
        }

        private string GetAvailableFilePath(string folder, string fileName)
        {
            string filePath = Path.Combine(folder, fileName);
            if (!File.Exists(filePath))
            {
                return filePath;
            }
            string name = Path.GetFileNameWithoutExtension(fileName);
            string extension = Path.GetExtension(fileName);
            int index = 1;
            do
            {
                filePath = Path.Combine(folder, name + "(" + index + ")" + extension);
                index++;
            } while (File.Exists(filePath));
            return filePath;
        }

        private void TrySendError(Socket clientSocket)
        {
            try
            {
                clientSocket.Send(Encoding.UTF8.GetBytes("error"));
            }
            catch
            {
            }
        }



    }
   

}
