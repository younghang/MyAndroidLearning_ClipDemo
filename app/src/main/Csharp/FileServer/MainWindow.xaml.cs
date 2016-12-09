using System;
using System.Collections.Generic;
using System.Linq;
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
        Server server;
        public void RunServer()
        {
            Server server = new Server();
            server.UpdateMessage += SetString;
            server.RunServer();
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




    }
}
