/*
 * 由SharpDevelop创建。
 * 用户： yanghang
 * 日期: 2015/6/11
 * 时间: 14:45
 * 
 * 要改变这种模板请点击 工具|选项|代码编写|编辑标准头文件
 */
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;

namespace Fileopt
{
	class Program
	{[STAThreadAttribute]
		public static void Main(string[] args)
		{
			Console.WriteLine("Hello World!");
			
			// TODO: Implement Functionality Here
		 string scrFile="e:/javaio/hello你好.txt";
		 string destFile="e:/javaio/新建文件夹/cl.flv";
		 #region "FileStream操作"
//		 FileStream fsd=new FileStream(destFile,FileMode.Create);
//		 FileStream fss=new FileStream(scrFile,FileMode.Open);
//		 byte []buffer=new byte[8*1024];
//		 int c;
//		 while((c=fss.Read(buffer,0,buffer.Length))!=0)
//		 {
////		 	fsd.Write(buffer,0,c);
////		 	fsd.Flush();
//		 	Console.Write(Encoding.ASCII.GetString(buffer));
//		 }
//		 fss.Close();
////		 fsd.Close();
//		 Console.WriteLine("123");
			 
		 #endregion
		 
		 
		 #region "StreamReader"
//		 StreamReader sr=new StreamReader(new FileStream(scrFile,FileMode.Open,FileAccess.Read));
//		 string str ;
//		 while((str =sr.ReadLine())!=null)
//		 {
//		 	Console.WriteLine(str);
//		 }
		
		 
//		 StreamReader sr=new StreamReader(new FileStream(scrFile,FileMode.Open,FileAccess.Read));
//		 int c ;
//		 char []buffer=new char[1024];
//		 while((c=sr.Read(buffer,0,buffer.Length))!=0)
//		 {
//		 	byte []bytebuffer=Encoding.UTF8.GetBytes(buffer);
//		 	Console.WriteLine(Encoding.UTF8.GetString(bytebuffer));
//		 }
		 
		 
		  #endregion
		 Form1 f=new Form1();
		 f.ShowDialog();
		
		  
		  
		  
		  
		 Console.ReadKey();




		                                  
		}
	}
}