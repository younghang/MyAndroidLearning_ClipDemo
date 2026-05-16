using System;
using System.Diagnostics;
using System.IO;
using System.Text;

namespace FileServer
{
    public static class INetFwManger
    {
        /// <summary>
        /// 添加防火墙例外端口
        /// </summary>
        /// <param name="name">名称</param>
        /// <param name="port">端口</param>
        /// <param name="protocol">协议(TCP、UDP)</param>
        public static void NetFwAddPorts(string name, int port, string protocol)
        {
            string safeName = Quote(name);
            string safeProtocol = NormalizeProtocol(protocol);
            RunNetsh("advfirewall firewall delete rule name=" + safeName + " protocol=" + safeProtocol + " localport=" + port);
            RunNetsh("advfirewall firewall add rule name=" + safeName + " dir=in action=allow protocol=" + safeProtocol + " localport=" + port + " enable=yes");
        }

        /// <summary>
        /// 将应用程序添加到防火墙例外
        /// </summary>
        /// <param name="name">应用程序名称</param>
        /// <param name="executablePath">应用程序可执行文件全路径</param>
        public static void NetFwAddApps(string name, string executablePath)
        {
            if (string.IsNullOrEmpty(executablePath) || !File.Exists(executablePath))
            {
                return;
            }
            string safeName = Quote(name);
            string safePath = Quote(executablePath);
            RunNetsh("advfirewall firewall delete rule name=" + safeName + " program=" + safePath);
            RunNetsh("advfirewall firewall add rule name=" + safeName + " dir=in action=allow program=" + safePath + " enable=yes");
        }

        /// <summary>
        /// 删除防火墙例外端口
        /// </summary>
        /// <param name="port">端口</param>
        /// <param name="protocol">协议（TCP、UDP）</param>
        public static void NetFwDelApps(int port, string protocol)
        {
            RunNetsh("advfirewall firewall delete rule protocol=" + NormalizeProtocol(protocol) + " localport=" + port);
        }

        /// <summary>
        /// 删除防火墙例外中应用程序
        /// </summary>
        /// <param name="executablePath">程序的绝对路径</param>
        public static void NetFwDelApps(string executablePath)
        {
            if (string.IsNullOrEmpty(executablePath))
            {
                return;
            }
            RunNetsh("advfirewall firewall delete rule program=" + Quote(executablePath));
        }

        private static string NormalizeProtocol(string protocol)
        {
            return string.Equals(protocol, "UDP", StringComparison.OrdinalIgnoreCase) ? "UDP" : "TCP";
        }

        private static string Quote(string value)
        {
            if (value == null)
            {
                value = "";
            }
            return "\"" + value.Replace("\"", "\\\"") + "\"";
        }

        private static void RunNetsh(string arguments)
        {
            try
            {
                ProcessStartInfo startInfo = new ProcessStartInfo();
                startInfo.FileName = "netsh";
                startInfo.Arguments = arguments;
                startInfo.CreateNoWindow = true;
                startInfo.UseShellExecute = false;
                startInfo.RedirectStandardOutput = true;
                startInfo.RedirectStandardError = true;
                startInfo.StandardOutputEncoding = Encoding.Default;
                startInfo.StandardErrorEncoding = Encoding.Default;

                using (Process process = Process.Start(startInfo))
                {
                    if (process != null)
                    {
                        process.WaitForExit(3000);
                    }
                }
            }
            catch
            {
                // 防火墙规则失败不应该阻止主程序启动，用户仍可手动允许网络访问。
            }
        }
    }
}
