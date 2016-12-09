using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;

namespace FileServer
{
    public delegate void UpdataInfo(string sr);
    class Server
    {

        private static byte[] result = new byte[1024];
        byte[] sendBytes = Encoding.UTF8.GetBytes("hello");
        private Socket server = null;
        private bool CLOSE_SEVER = false;
        public void RunServer()
        {
            Socket server = new Socket(
            AddressFamily.InterNetwork,
            SocketType.Stream,
            ProtocolType.Tcp);


            IPHostEntry ipHostInfo = Dns.Resolve(Dns.GetHostName());
            IPAddress ipAddress = ipHostInfo.AddressList[0];
            IPEndPoint ipep = new IPEndPoint(ipAddress, 20300);
            server.Bind(ipep);
            server.Listen(5);
            string st = string.Format("IP:{0}  Port:{1}  \n", ipep.Address, ipep.Port);
            UpdateMessage(st);
            while (!CLOSE_SEVER)
            {
                Socket clientSocket = server.Accept();
                string stf = string.Format("收到{0}连接！！", clientSocket.RemoteEndPoint.ToString());
                UpdateMessage(stf);
                Thread receiveThread = new Thread(ReceiveMessage);
                receiveThread.Start(clientSocket);

            }
            server.Close();

        }
        public event UpdataInfo UpdateMessage;
        private void ReceiveMessage(object clientSocket)
        {
            Socket myClientSocket = (Socket)clientSocket;
            String str = "";


            try
            {
                //通过clientSocket接收数据
                int receiveNumber = myClientSocket.Receive(result);
                str = Encoding.UTF8.GetString(result, 0, receiveNumber);
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
                ReceieveData(myClientSocket);

            }
            catch
            {
                UpdateMessage("客户端关闭");
                return;
            }


        }
        private void ReceieveData(Socket myClientSocket)
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


                }

        }
        public void Close()
        {
            CLOSE_SEVER = true;

            server.Shutdown(SocketShutdown.Both);
            server.Close();
        }

        private void ReceiveFile(Socket clientSocket)
        {
            byte[] buffer = new byte[1024 * 4];
            int count = clientSocket.Receive(buffer);
            string fileInfo = Encoding.UTF8.GetString(buffer, 0, count);
            FileInfo fileJson = JsonConvert.DeserializeObject<FileInfo>(fileInfo);
            clientSocket.Send(sendBytes);

            FileStream fs = new FileStream("./" + fileJson.getFileName(), FileMode.Create);
            byte[] data = new byte[8 * 1024];
            int c;
            int size = 0;
            while (size < fileJson.getFileSize() && (c = clientSocket.Receive(data)) != 0)
            {
                fs.Write(data, 0, c);
                fs.Flush();
                size += c;
            }
            fs.Close();
            clientSocket.Send(sendBytes);

            UpdateMessage("收到客户端发送的文件" + fileJson.getFileName());
            ReceieveData(clientSocket);
        }

    }
}
