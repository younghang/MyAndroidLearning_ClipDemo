package com.example.yanghang.clipboard.Task;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by young on 2017/10/26.
 */

public class TaskAutoSave {
    Context context;

    public TaskAutoSave(Context context) {
        this.context=context;
    }
    public void runAutoSave()
    {
        boolean isAutoSave= PreferenceManager.getDefaultSharedPreferences(context).getBoolean("autoSavePreference",false);
        if (isAutoSave)
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Date nowDate= Calendar.getInstance().getTime();
                    String todayString = DateFormat.format("yyyy-MM-dd", nowDate).toString();
                    String saveDate=PreferenceManager.getDefaultSharedPreferences(context).getString("autoSaveDate","");
                    if (!saveDate.equals(todayString)||saveDate.equals(""))
                    {
                        saveData();
                    }
                }
            }).start();
        }

    }
    private void saveData()
    {
        List<ListData> listDatas=new DBListInfoManager(context).getDatas("");
        if (listDatas.size()==0)
            return;
        FileUtils.SEED=PreferenceManager.getDefaultSharedPreferences(context).getString("autoSaveKey","");
        FileUtils.saveListData(listDatas,PreferenceManager.getDefaultSharedPreferences(context).getString("dataFilePathPreference",context.getFilesDir().getAbsolutePath()),"autoSave",true);
        Date nowDate= Calendar.getInstance().getTime();
        String todayString = DateFormat.format("yyyy-MM-dd", nowDate).toString();
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString("autoSaveDate", todayString).apply();
    }


}
