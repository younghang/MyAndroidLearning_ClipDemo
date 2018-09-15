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
        private Boolean IsPhoneSeverWorking=false;
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
            this.Dispatcher.Invoke(() =>
            {
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

            Run run = new Run(op.FileName);
            Paragraph paragraph = new Paragraph(run);
            txtMessage.Document.Blocks.Add(paragraph);
            CurrentFile = op.FileName;
        }
        public void ConnectPhoneServer()
        {
            try
            {
                phoneServer.ConnectPhoneServer();
                IsPhoneSeverWorking = true;
            }
            catch (Exception e)
            {
                IsPhoneSeverWorking = false;
                UpdateMessage("启动客服端服务出错,无法向客服端发送信息");
                this.Dispatcher.Invoke(() =>
                {
                    btnSend.IsEnabled = false;
                    btnFile.IsEnabled = false;
                });
            }


        }

        public void DealRequest()
        {
            new Thread(new ThreadStart(() =>
            {
                clientServer.RecieveMessage();
            })).Start();


        }


        private string GetTxtMessage()
        {
            TextRange textRange = new TextRange(txtMessage.Document.ContentStart, txtMessage.Document.ContentEnd);
            return textRange.Text;
        }
        private void txtMessage_MouseEnter(object sender, MouseEventArgs e)
        {
            string richText = GetTxtMessage();

            if (richText == "在此处发送消息\r\n")
            {
                txtMessage.Document.Blocks.Clear();
            }
        }

        private void btnSend_Click(object sender, RoutedEventArgs e)
        {
            if (GetTxtMessage() == (CurrentFile + "\r\n"))
            {
                phoneServer.SendFile(CurrentFile);

            }
            else
                phoneServer.SendMessage(GetTxtMessage());

            richTextBox.AppendText(GetTxtMessage() + "\n");
            txtMessage.Document.Blocks.Clear();
        }
    }
}