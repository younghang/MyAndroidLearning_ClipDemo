using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FileServer
{
    class FileInfo
    {
        public FileInfo(String fileName, long fileSize)
        {
            this.fileName = fileName;
            this.fileSize = fileSize;
        }

        public String getFileName()
        {
            return fileName;
        }

        public void setFileName(String fileName)
        {
            this.fileName = fileName;
        }

        public long getFileSize()
        {
            return fileSize;
        }

        public void setFileSize(long fileSize)
        {
            this.fileSize = fileSize;
        }

        private String fileName;
        private long fileSize;
    }
}
