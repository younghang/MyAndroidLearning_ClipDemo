using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FileServer
{
    public class FileInfo
    {
        public string fileName { get; set; }
        public int fileSize { get; set; }
        public FileInfo(string name,int size)
        {
            fileName = name;
            fileSize = size;
        }
    }
}
