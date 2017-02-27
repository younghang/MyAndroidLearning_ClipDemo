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
                OnDisconnect();
                
            }

        }

        public void Close()
        {
            myClientSocket.Shutdown(SocketShutdown.Both);
            myClientSocket.Close();
        }

        private void ReceiveFile(Socket clientSocket)
        {
            byte[] buffer = new byte[1024 * 4];
            int count = clientSocket.Receive(buffer);
            string fileInfo = Encoding.UTF8.GetString(buffer, 0, count);
            FileInfo fileJson = JsonConvert.DeserializeObject<FileInfo>(fileInfo);
            
            string filePath = "./" + fileJson.fileName ;
            if (File.Exists(filePath))
            {               
                UpdateMessage("文件 “" + fileJson.fileName +"” 已经存在");
                sendBytes= Encoding.UTF8.GetBytes("error");
                clientSocket.Send(sendBytes);
            }
            else {
                sendBytes = Encoding.UTF8.GetBytes("hello");
                clientSocket.Send(sendBytes);
                FileStream fs = new FileStream(filePath, FileMode.Create);
            byte[] data = new byte[8 * 1024];
            int c;
            int size = 0;
            while (size < fileJson.fileSize  && (c = clientSocket.Receive(data)) != 0)
            {
                fs.Write(data, 0, c);
                fs.Flush();
                size += c;
            }
            fs.Close();
            clientSocket.Send(sendBytes);

            UpdateMessage("收到客户端发送的文件" + fileJson.fileName);}
            ReceieveData();
        }



    }
   

}
