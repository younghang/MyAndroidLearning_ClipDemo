package com.example.yanghang.clipboard.ListPackage.DailyTaskList;

/**
 * Created by young on 2017/8/11.
 */

public class DailyTaskData {
    //taskId for short
    String tI;
    //taskName for short
    String tN;
    //tP for short
    int tP;

    public DailyTaskData() {
    }

    public DailyTaskData(String tN, int tP) {
        this.tN = tN;
        this.tP = tP;
    }

    public DailyTaskData(String tI, String tN, int tP) {
        this.tI = tI;
        this.tN = tN;
        this.tP = tP;
    }

    public String gettI() {
        return tI;
    }

    public void settI(String tI) {
        this.tI = tI;
    }

    public String gettN() {
        return tN;
    }

    public void settN(String tN) {
        this.tN = tN;
    }

    public int gettP() {
        return tP;
    }

    public void settP(int tP) {
        this.tP = tP;
    }
}
