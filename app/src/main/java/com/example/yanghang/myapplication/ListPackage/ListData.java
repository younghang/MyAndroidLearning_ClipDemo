package com.example.yanghang.myapplication.ListPackage;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListData implements Serializable {
    String Remarks="";
    String Information="";
    String CreateDate = "";
    int OrderID = 0;

    public String getCreateDate() {
        return CreateDate;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
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

    public int getOrderID() {
        return OrderID;
    }

    public void setOrderID(int orderID) {
        OrderID = orderID;
    }



    static String GetDate()
    {
        SimpleDateFormat sDateFormat    =   new SimpleDateFormat("yyyy-MM-dd    hh:mm:ss");
        String    date    =    sDateFormat.format(new    java.util.Date());
        return date;
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
