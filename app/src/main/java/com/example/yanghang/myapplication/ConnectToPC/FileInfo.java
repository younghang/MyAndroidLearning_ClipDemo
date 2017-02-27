package com.example.yanghang.myapplication.ConnectToPC;

/**
 * Created by yanghang on 2016/12/8.
 */
public class FileInfo {
    private String fileName;
    private long fileSize;
    public FileInfo()
    {

    }

    public FileInfo(String fileName, long fileSize) {
        this.fileName = fileName;
        this.fileSize = fileSize;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
}
