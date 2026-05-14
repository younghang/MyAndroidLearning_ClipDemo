using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows;
using System.Windows.Controls;
using System.Windows.Data;
using System.Windows.Documents;
using System.Windows.Input;
using System.Windows.Media;
using System.Windows.Media.Imaging;
using System.Windows.Shapes;

namespace FileServer
{
    /// <summary>
    /// Interaction logic for SettingWindow.xaml
    /// </summary>
    public partial class SettingWindow : Window
    {
        public delegate void CallMainWindowDelegate(int IPIndex);
        public event CallMainWindowDelegate ReStartServer;
        public List<String> ipLists = new List<string>();
        public SettingWindow()
        {
            InitializeComponent();
            ipCombox.ItemsSource = ipLists;
     
        }

        private void Window_Closed(object sender, EventArgs e)
        {
            //写到closing里面去

        }

        private void Window_Closing(object sender, System.ComponentModel.CancelEventArgs e)
        {


        }
        void btnClose(object sender, RoutedEventArgs e)
        {
            this.Close();
        }
        void btnRestart_Click(object sender, RoutedEventArgs e)
        {
            ReStartServer(ipCombox.SelectedIndex);
            this.Close();
        }



        private void dragPanel_MouseLeftButtonDown(object sender, MouseButtonEventArgs e)
        {

            DragMove();
            e.Handled = true;
        }

        private void window2_Loaded(object sender, RoutedEventArgs e)
        {
            int index= MainWindow.LocalServerIPAddressIndex;
            if(index==-1)
            {
                ipCombox.SelectedIndex = 0;
            }else
            {
                ipCombox.SelectedIndex = index;
            }
            
        }
    }
}