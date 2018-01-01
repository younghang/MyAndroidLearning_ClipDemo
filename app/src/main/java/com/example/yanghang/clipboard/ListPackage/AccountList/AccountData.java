package com.example.yanghang.clipboard.ListPackage.AccountList;

/**
 * Created by young on 2017/11/18.
 */

public class AccountData {
    //accountTime
    private String aT;
    //remark
    private String re;
    //mo
    private double mo;
    //color
    private String cor;
    //type
    private String ty;
    //content
    private String co;

    public AccountData() {
    }

    public AccountData(String accountTime, String remark, double money, String color, String type, String content) {
        this.aT = accountTime;
        this.re = remark;
        this.mo = money;
        this.cor = color;
        this.ty = type;
        this.co = content;
    }

    public String getContent() {
        return co;
    }

    public void setContent(String content) {
        this.co = content;
    }

    public String getAccountTime() {
        return aT;
    }

    public void setAccountTime(String accountTime) {
        this.aT = accountTime;
    }

    public String getRemark() {
        return re;
    }

    public void setRemark(String remark) {
        this.re = remark;
    }

    public double getMoney() {
        return mo;
    }

    public void setMoney(double money) {
        this.mo = money;
    }

    public String getColor() {
        return cor;
    }

    public void setColor(String color) {
        this.cor = color;
    }

    public String getType() {
        return ty;
    }

    public void setType(String type) {
        this.ty = type;
    }
}
