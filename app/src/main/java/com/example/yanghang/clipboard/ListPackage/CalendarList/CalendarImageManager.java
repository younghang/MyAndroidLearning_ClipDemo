package com.example.yanghang.clipboard.ListPackage.CalendarList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.R;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.crypto.BadPaddingException;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * Created by young on 2017/10/29.
 */

//用FastJson 记得加一个默认的构造函数（空的）
public class CalendarImageManager {
    private List<CalendarItemsData> lists;
    private Context context;
    private String fileAbsolutePath="";
    public static  String FileName="calendarItemList.json";
    private List<String> englishName;

    public CalendarImageManager(Context context) {
        this.context=context;
        initialList();
    }
    private void initialList()
    {

        //为了兼容之前的数据
        englishName = Arrays.asList("diary","weight","luser","jp","code","paint"
                ,"rest","cost","income","fire","like","check","star");

        fileAbsolutePath=context.getFilesDir()+"/"+FileName;
        File file=new File(fileAbsolutePath);
        if (!(file.exists()&&file.length()!=0))
        {
            try {
                file.createNewFile();
//                Log.d(TAG, "CalendarImageManager initialList: File not Exits or Empty");
            } catch (IOException e) {
                e.printStackTrace();
            }
            setOriginalItems();
            saveImageLists();
        }else
        {
            lists=loadItemDatas();
        }
    }
    public  void setLists(List<CalendarItemsData> lists)
    {
        this.lists=lists;
    }
    public List<CalendarItemsData> getLists()
    {
        loadItemDatas();
        return lists;
    }
    public List<CalendarItemsData> getVisibleLists()
    {
        loadItemDatas();
        List<CalendarItemsData> list=new ArrayList<CalendarItemsData>();
        for (CalendarItemsData item : lists) {
            if (item.getShowImage())
                list.add(item);
        }
        return list;
    }
    public List<String> getVisibleNames()
    {
        List<String> names = new ArrayList<>();
        List<CalendarItemsData> list = getVisibleLists();
        for (CalendarItemsData item : list) {
            names.add(item.getCalendarItemName());
        }
        for (String shortName : englishName)
        {
            if (getTagVisibility(shortName))
                names.add(shortName);
        }
        return names;
    }

    public void saveImageLists() {
        File file = FileUtils.createFile(FileName, context.getFilesDir().getAbsolutePath());

        org.json.JSONObject jsonObject = new org.json.JSONObject();
        String arrayStr=JSON.toJSONString(lists);
//        Log.d(TAG, "CalendarImageManager saveImageLists: arrayStr="+arrayStr);
        try {
            jsonObject = new org.json.JSONObject();
            jsonObject.put("Array", arrayStr);
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
//        Log.d(TAG, "CalendarImageManager saveImageLists: jsonObject="+jsonObject.toString());

        FileUtils.saveJsonObjectToDisk(file, jsonObject,false);
    }
    public boolean containsTag(String tag)
    {
        for (int i=0;i<lists.size();i++)
        {
            if (lists.get(i).getCalendarItemName().equals(tag)||englishName.contains(tag)) {
                return true;
            }
        }
        return false;
    }
    public boolean getTagVisibility(String tag)
    {
        for (CalendarItemsData item:lists)
        {
            if (item.getCalendarItemName().equals(tag)||item.getCalendarItemPic().equals(tag))
            {
                return item.getShowImage();
            }
        }
        return  true;
    }

    private List<CalendarItemsData> loadItemDatas() {

        List<CalendarItemsData> mList = new ArrayList<>();
        JSONArray jsonArray=null;
        try {
            File file = FileUtils.createFile(FileName, context.getFilesDir().getAbsolutePath());
            org.json.JSONObject json = FileUtils.loadJsonFromDisk(file,false);
            if (json==null)
            {
                return mList;
            }
//            Log.d(TAG, "CalendarImageManager loadItemDatas  json="+json.toString());
            jsonArray = JSONArray.parseArray(json.getString("Array"));
            for (int i = 0; i < jsonArray.size(); i++) {
                CalendarItemsData calendarItemsData = JSON.parseObject(jsonArray.get(i).toString(), CalendarItemsData.class);
                mList.add(calendarItemsData);
//                Log.d(TAG, "CalendarImageManager loadItemDatas calendarItem name="+calendarItemsData.getCalendarItemName());
            }

        } catch (JSONException e) {
            File file = FileUtils.createFile(FileName, context.getFilesDir().getAbsolutePath());
            file.delete();
            e.printStackTrace();

        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (org.json.JSONException e) {
            e.printStackTrace();
        }
        return mList;

    }

    private void setOriginalItems() {
        lists = new ArrayList<>();
        lists.add(new CalendarItemsData("日记", "diary",true));
        lists.add(new CalendarItemsData("体重", "weight",true));
        lists.add(new CalendarItemsData("luser", "luser",true));
        lists.add(new CalendarItemsData("日语", "jp",true));
        lists.add(new CalendarItemsData("编程", "code",true));
        lists.add(new CalendarItemsData("画画", "paint",true));
        lists.add(new CalendarItemsData("懒惰","rest",true));
        lists.add(new CalendarItemsData("支出", "cost",true));
        lists.add(new CalendarItemsData("收入", "income",true));
        lists.add(new CalendarItemsData("火焰", "fire",true));
        lists.add(new CalendarItemsData("爱心", "like",true));
        lists.add(new CalendarItemsData("完成", "check",true));
        lists.add(new CalendarItemsData("标记", "star",true));

    }


    public static void setImageSource(ImageView imageView, String pic) {
        if (!pic.startsWith("/")) {

            switch (pic) {
                case "luser":
                    imageView.setImageResource(R.drawable.toilet_paper);
                    break;
                case "diary":
                case "日记":
                    imageView.setImageResource(R.drawable.diary1);
                    break;
                case "weight":
                case "体重":
                    imageView.setImageResource(R.drawable.weight_scale);
                    break;
                case "star":
                case "标记":
                    imageView.setImageResource(R.drawable.ic_star);
                    break;
                case "fire":
                case "火焰":
                    imageView.setImageResource(R.drawable.ic_fire);
                    break;
                case "check":
                case "完成":
                    imageView.setImageResource(R.drawable.ic_check_circle_green_500_24dp);
                    break;
                case "jp":
                case "日语":
                    imageView.setImageResource(R.drawable.jp_learn);
                    break;
                case "like":
                case "爱心":
                    imageView.setImageResource(R.mipmap.ic_like_normal);
                    break;
                case "rest":
                case "懒惰":
                    imageView.setImageResource(R.drawable.rest);
                    break;
                case "code":
                case "编程":
                    imageView.setImageResource(R.drawable.code);
                    break;
                case "paint":
                case "画画":
                    imageView.setImageResource(R.drawable.paint);
                    break;
                case "cost":
                case "支出":
                    imageView.setImageResource(R.drawable.spendmoney);
                    break;
                case "income":
                case "收入":
                    imageView.setImageResource(R.drawable.addmoney);
                    break;
                default:
                    imageView.setImageResource(R.drawable.ic_star);
            }
        }
        else
        {
            try{
                Bitmap bitmap=BitmapFactory.decodeFile(pic);
                imageView.setImageBitmap(bitmap);

            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    }
}
