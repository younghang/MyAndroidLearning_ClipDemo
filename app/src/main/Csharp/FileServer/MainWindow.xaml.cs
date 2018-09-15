using System;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Navigation;
using System.Windows.Shapes;

namespace FileServer
{
    /// <summary>
    /// MainWindow.xaml 的交互逻辑
    /// </summary>
    public partial class MainWindow : Window
    {
        public MainWindow()
        {
            InitializeComponent();

            serverThread = new Thread(new ThreadStart(RunServer));
            serverThread.Start();

        }
        Thread serverThread;
        Server server = null;
        public void RunServer()
        {
            //setPortFireWall("20300", "fileServer");
            //INetFwManger.NetFwAddApps("fileServer", this.GetType().Assembly.Location);
            //开启公用网络防火墙通行
            INetFwManger.NetFwAddPorts("fileServerPort", 20300, "TCP");
            Server server = new Server();
            server.UpdateMessage += SetString;
            server.OnClientRequest += OnClientRequest;
            server.RunServer();
        }

        private void OnClientRequest(Socket socket)
        {
            this.Dispatcher.Invoke(() =>
            {
                ClientWindow cl = new ClientWindow();
                cl.InitialPhoneServer(socket);
                cl.Show();
                cl.DealRequest();
                cl.ConnectPhoneServer();
            });
        }

        public void SetString(string str)
        {
            this.richEdit.Dispatcher.Invoke(new Action<string>(AppendString), new object[] { str });

        }
        private void AppendString(string str)
        {
            this.richEdit.AppendText(str + "\n");
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            //写到closing里面去

        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            System.Environment.Exit(0);
            try
            {
                serverThread.Abort();
                server.Close();
            }
            catch (Exception)
            {

            }

            Application.Current.Shutdown();

        } 
        void btnClose(object sender, RoutedEventArgs e)
        {
            this.Close();
        }




        
        void btnSetting(object sender, RoutedEventArgs e)
        {
            IPHostEntry ipHostInfo = Dns.Resolve(Dns.GetHostName());
          
            SettingWindow settingWindow = new SettingWindow();
            
            IPAddress[] address = ipHostInfo.AddressList;
            settingWindow.ipLists.Clear();
            for (int i = 0; i < address.Length; i++)
            {
                settingWindow.ipLists.Add(address[i].ToString());
            }
            settingWindow.ReStartServer += RestartServer;
            settingWindow.Show();

        }
        public static int LocalServerIPAddressIndex=-1;
        void RestartServer(int IPIndex)
        {
            LocalServerIPAddressIndex = IPIndex;            
            serverThread = new Thread(new ThreadStart(RunServer));
            serverThread.Start();
        }
        void btnMininue(object sender, RoutedEventArgs e)
        {
            this.WindowState = System.Windows.WindowState.Minimized;
        }
        //StackPanel 没办法MouseDown
        void dragPanel_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {

            DragMove();
            e.Handled = true;

        }

        void window1_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {
            //			this.DragMove();
            //			e.Handled=true;
        }
        void setPortFireWall(string port,string inname)
        {
          
            string str = " netsh advfirewall firewall add rule name=" + inname + " dir=in action=allow protocol=TCP localport= " + port;
            System.Diagnostics.Process pro = new System.Diagnostics.Process();//实例化进程
            pro.StartInfo.FileName = "cmd.exe";//设置要运行的程序文件
            pro.StartInfo.UseShellExecute = false;//是否使用操作系统shell程序启动
            pro.StartInfo.RedirectStandardInput = true;//是否接受来自应用程序的调用
            pro.StartInfo.RedirectStandardOutput = true;//是否接受来自应用程序的输出信息
            pro.StartInfo.RedirectStandardError = true;//是否接受重定向错误信息
            pro.StartInfo.CreateNoWindow = true;//不显示窗口信息
            pro.Start();//启动程序

            //向cmd窗口发送输入信息
            pro.StandardInput.WriteLine(str + "&exit");

            pro.StandardInput.AutoFlush = true;

            //获取窗口输出的信息
            string info = pro.StandardOutput.ReadToEnd();

            pro.WaitForExit();//等待程序运行完退出程序
            pro.Close();//关闭进程
            SetString(info + "\n" + "Port=" + port );
        }


    }
}

//		void dragPanel_MouseMove(object sender, MouseEventArgs e)
//		{
//			if (e.LeftButton == MouseButtonState.Pressed)
//			{
//				 	double x = e.GetPosition(dragPanel).X;
//				double y = e.GetPosition(dragPanel).Y;
//				this.Left +=( x - oldx);
//				this.Top +=( y - oldy);}
//			e.Handled=true;
//		}
//	 
//		void dragPanel_MouseDown(object sender, MouseButtonEventArgs e)
//		{
//			oldx = e.GetPosition(dragPanel).X;
//			oldy = e.GetPosition(dragPanel).Y;
//		}