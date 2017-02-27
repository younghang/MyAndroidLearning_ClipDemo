using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading.Tasks;
 
namespace FileServer
{
    class PhoneServer
    {
        private Socket socket;
        private int Port = 20600;
        private string Host = "";
        public event SendData SendMessageEvent;
        public event SendData SendFileEvent;

        byte[] sendBytes = Encoding.UTF8.GetBytes("hello");
        public void SendMessage(string message)
        {
            socket.Send(new byte[] { 0x60 });
            socket.Receive(sendBytes);
            byte []dataBytes = Encoding.UTF8.GetBytes(message);
            socket.Send(ConvertIntToByteArray(dataBytes.Length));
            socket.Receive(sendBytes);

            socket.Send(dataBytes);

        }
        public void SendFile(string filePath )
        {
            socket.Send(new byte[] { 0x66 });
            socket.Receive(sendBytes);
            //这边都是简单粗暴，对于FileInfo直接发送四个字节，而不是while 每1k字节的发送
            //java 那边是1k 1k 的接收的。
            byte[] buffer = new byte[1024 * 4];
            long fileLength = 0;
            FileStream fs = new FileStream(filePath , FileMode.Open);
            fileLength = fs.Length;
            string[] fps = filePath.Split('\\');
            string fileName = fps[fps.Length-1];
            FileInfo fileInfo = new FileInfo(fileName, (int)fileLength);
            string fileInfoString = JsonConvert.SerializeObject(fileInfo);

            byte[] dataBytes = Encoding.UTF8.GetBytes(fileInfoString);
            socket.Send(ConvertIntToByteArray(dataBytes.Length));
            socket.Receive(sendBytes);

            socket.Send(dataBytes);
            socket.Receive(sendBytes);
            int c = 0;
            long size = 0;
            while (size < fileLength && (c = fs.Read(buffer, 0, buffer.Length)) != 0)
            {
                socket.Send(buffer, c,SocketFlags.None);
                size += c;
            }
            fs.Close();

        }
        public void Close()
        {
            socket.Send(new byte[] { 0xfc});
            socket.Close();
        }
        public PhoneServer(String host)
        {
            this.Host = host;
        }
        public void ConnectPhoneServer()
        {
            //创建终结点EndPoint
            IPAddress ip = IPAddress.Parse(Host);
            IPEndPoint ipe = new IPEndPoint(ip, Port);   //把ip和端口转化为IPEndPoint的实例
            socket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);   //  创建Socket
            socket.Connect(ipe);
        }
        /// <summary>
        /// 把int32类型的数据转存到4个字节的byte数组中
        /// </summary>
        /// <param name="m">int32类型的数据</param>
        /// <param name="arry">4个字节大小的byte数组</param>
        /// <returns></returns>
        static byte[] ConvertIntToByteArray(Int32 m  )
        {

            byte[]array= BitConverter.GetBytes(m);
            return array;

        }


    }
}
