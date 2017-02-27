/*
 * 由SharpDevelop创建。
 * 用户： yanghang
 * 日期: 2017/1/1
 * 时间: 23:11
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.Collections.Generic;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;

namespace FileServer
{
    /// <summary>
    /// Interaction logic for ClientWindow.xaml
    /// </summary>
    public partial class ClientWindow : Window
    {

        private ClientServer clientServer;
        private PhoneServer phoneServer;
        private string CurrentFile;
        public void InitialPhoneServer(Socket socket)
        {
            clientServer = new ClientServer(socket);
            clientServer.UpdateMessage += UpdateMessage;
            clientServer.OnDisconnect += DisconnectClient;
            richTextBox.AppendText("已经与" + socket.RemoteEndPoint.ToString() + "连接\n");
            string host = socket.RemoteEndPoint.ToString().Split(':')[0];
            phoneServer = new PhoneServer(host);
        }

        private void DisconnectClient()
        {
            this.Dispatcher.Invoke(()=> {
                btnSend.IsEnabled = false;
                btnFile.IsEnabled = false;
            });
        }

        private void UpdateMessage(string message)
        {
            this.richTextBox.Dispatcher.Invoke(() =>
            {
                richTextBox.AppendText(message + "\n");
            });
        }

        public ClientWindow()
        {
            InitializeComponent();
        }

        //StackPanel 没办法MouseDown
        void dragPanelNotPanelButABorder_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {

            DragMove();
            e.Handled = true;

        }
        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {
            try
            {
                clientServer.Close();
                phoneServer.Close();
            }
            catch (Exception)
            {
            }

        }
        string filePath = "";
        void window1_Loaded(object sender, RoutedEventArgs e)
        {
            filePath = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);

        }
        void btnMininue(object sender, RoutedEventArgs e)
        {
            this.WindowState = System.Windows.WindowState.Minimized;
        }

        void btnClose(object sender, RoutedEventArgs e)
        {
            this.Close();
        }
        void FileChooseClick(object sender, RoutedEventArgs e)
        {
            Microsoft.Win32.OpenFileDialog op = new Microsoft.Win32.OpenFileDialog();
            //op.InitialDirectory = _dir;你可以指定文件夹
            op.RestoreDirectory = true;
            op.ShowDialog();
            txtMessage.Text = op.FileName;            
            CurrentFile = op.FileName;
        }
        public void ConnectPhoneServer()
        {
          
                phoneServer.ConnectPhoneServer();
            
        }

        public void DealRequest()
        {
            new Thread(new ThreadStart(() =>
            {
                clientServer.RecieveMessage();
            })).Start();


        }



        private void txtMessage_MouseEnter(object sender, MouseEventArgs e)
        {
            if (txtMessage.Text == "在此处发送消息")
            {
                txtMessage.Text = "";
            }
        }

        private void btnSend_Click(object sender, RoutedEventArgs e)
        {
            if (txtMessage.Text == CurrentFile)
            {
                phoneServer.SendFile(CurrentFile);

            }
            else
                phoneServer.SendMessage(txtMessage.Text);
           
            richTextBox.AppendText(txtMessage.Text+"\n");
            txtMessage.Text = "";
        }
    }
}