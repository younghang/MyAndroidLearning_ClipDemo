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
    public delegate void SendData(string str);
    public delegate void OnClientConnect(Socket socket);
    public delegate void OnSuccess();
    class Server
    {

        private static byte[] result = new byte[1024];
        
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
            try{
            server.Bind(ipep);
            }
            catch(SocketException)
            {
            	UpdateMessage("默认端口20300被占用，或其他错误，服务未能成功启动");
            	return;
            }
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
        public event OnClientConnect OnClientRequest;
        private void ReceiveMessage(object clientSocket)
        {
            Socket myClientSocket = (Socket)clientSocket; 
            OnClientRequest(myClientSocket); 
        }
      
        public void Close()
        {
            CLOSE_SEVER = true;

            server.Shutdown(SocketShutdown.Both);
            server.Close();
        }

       

    }
}
