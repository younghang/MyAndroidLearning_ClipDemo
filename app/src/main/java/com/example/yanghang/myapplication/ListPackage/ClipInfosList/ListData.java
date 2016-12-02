package com.example.yanghang.myapplication.ListPackage.ClipInfosList;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListData implements Serializable {
    private String Remarks = "";
    private String Information = "";
    private String CreateDate = "";
    private int OrderID = 0;

    public ListData(ListData from) {
        this.Remarks = from.getRemarks();
        this.Information = from.getInformation();
        this.CreateDate = from.getCreateDate();
    }

    public ListData(String remarks, String information, int orderID) {
        Remarks = remarks;
        Information = information;
        this.OrderID = orderID;
        CreateDate=GetDate();
    }

    public ListData(String remarks, int orderID, String createDate, String information) {
        Remarks = remarks;
        OrderID = orderID;
        CreateDate = createDate;
        Information = information;
    }

    static String GetDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }

    public int getOrderID() {
        return OrderID;
    }

    public void setOrderID(int orderID) {
        OrderID = orderID;
    }

    public String getRemarks() {
        return Remarks;
    }

    public void setRemarks(String remarks) {
        Remarks = remarks;
    }

    public String getInformation() {
        return Information;
    }

    public void setInformation(String information) {
        Information = information;
    }
}
