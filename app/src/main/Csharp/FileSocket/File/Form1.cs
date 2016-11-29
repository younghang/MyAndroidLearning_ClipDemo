/*
 * 由SharpDevelop创建。
 * 用户： yanghang
 * 日期: 2016/11/29
 * 时间: 22:03
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Drawing;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace Fileopt
{
	/// <summary>
	/// Description of Form1.
	/// </summary>
	public partial class Form1 : Form
	{
		string destFile="e:/javaio/新建文件夹/temp.txt";
		public Form1()
		{
			//
			// The InitializeComponent() call is required for Windows Forms designer support.
			//
			InitializeComponent();
			textBox1.Text="121";
			//
			// TODO: Add constructorr code after the InitializeComponent() call.
			//
		}
		public void SetString(string str)
		{
			if (textBox1.InvokeRequired)
			{
			 
				this.Invoke(new Action<string>(SetString), new object[] { str });
			}
			else
			{
				this.textBox1.Text = str;
			}
		}
		
		public void StartRun()
		{
			#region "Socket传输文件"
			Socket server =new Socket(
				AddressFamily.InterNetwork,
				SocketType.Stream,
				ProtocolType.Tcp
			);
//		 IPAddress newaddress = IPAddress.Parse("222.20.54.142");
//		  IPAddress newaddress = IPAddress.Parse("222.20.53.179");
			
			IPHostEntry ipHostInfo = Dns.Resolve(Dns.GetHostName());
			IPAddress ipAddress = ipHostInfo.AddressList[0];
			IPEndPoint ipep=new IPEndPoint(  ipAddress,20300);
			server.Bind(ipep);
			server.Listen(5);
			Console.Write("IP:{0}  Port:{1}  \n",ipep.Address,ipep.Port);
			while(true){
				Socket client=server.Accept();
				Console.WriteLine("收到{0}连接！！",client.LocalEndPoint.ToString());
				byte []data=new byte[8*1024];
				int c;
				FileStream fs=new FileStream(destFile,FileMode.Create);
				while((c=client.Receive(data))!=0)
				{
					
					fs.Write(data,0,c);
					fs.Flush();
					
				}
				fs.Close();
				Console.WriteLine("Receive Over");
				client.Shutdown(SocketShutdown.Both);
				client.Close();
				StreamReader sr=new StreamReader (destFile,Encoding.UTF8);
				string str=sr.ReadToEnd();
				sr.Close();
				SetString(str);
				
				
			}
			server.Close();
			
			
			
			
			
			#endregion
		}
		
		public string RecievedInfo="";
		
		void Form1Load(object sender, EventArgs e)
		{
			new Thread(new ThreadStart(StartRun)).Start();
		}
		 
		void Button1Click(object sender, EventArgs e)
		{
			Clipboard.SetDataObject(textBox1.Text); 
		}
	}
}
