package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListData implements Serializable {
    private String Remarks = "";
    private String Content = "";
    private String CreateDate = "";
    private int OrderID = 0;
    private String Catalogue = "default";

    public ListData(String remarks, String content, int orderID, String catalogue) {
        Remarks = remarks;
        Content = content;
        OrderID = orderID;
        Catalogue = catalogue;
        CreateDate = GetDate();
    }

    public ListData(String remarks, String content, String createDate, int orderID, String catalogue) {
        Remarks = remarks;
        Content = content;
        CreateDate = createDate;
        OrderID = orderID;
        Catalogue = catalogue;
    }

    static String GetDate() {
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd\nHH:mm:ss");
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    public String getCatalogue() {
        return Catalogue;
    }

    public void setCatalogue(String catalogue) {
        Catalogue = catalogue;
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

    public String getContent() {
        return Content;
    }

    public void setContent(String content) {
        Content = content;
    }
}
