package com.example.yanghang.clipboard.Fragment;

/**
 * Created by young on 2018/2/21.
 */

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.ActivityAccountBook;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountDataAdapter;
import com.example.yanghang.clipboard.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/***
 * refer:
 * https://github.com/PhilJay/MPAndroidChart/blob/master/MPChartExample/src/com/xxmassdeveloper/mpchartexample/PieChartActivity.java
 * https://www.jianshu.com/p/37fd18d67037
 * http://blog.csdn.net/qwm8777411/article/details/45445521
 * http://blog.csdn.net/zhangtian6691844/article/details/54095770
 * https://www.cnblogs.com/liemng/p/5954210.html
 * http://blog.csdn.net/qq_26787115/article/details/53213641
 */


public class AccountCatalogueDialogPieChartFragment extends DialogFragment implements OnChartValueSelectedListener {

    public void show(FragmentManager fragmentManager,ArrayList<PieEntry> entries,AccountDataAdapter adapter ) {
        mEntries=entries;
        accountAdapter=adapter;
        lists=accountAdapter.getData();
        accountAdapter.setData(new ArrayList<AccountData>());
        show(fragmentManager, "AccountCatalogueDialogSelectCategoriesFragment");
    }
    List<AccountData> lists;
    List<AccountData> tempLists;
    AccountDataAdapter accountAdapter;
    ArrayList<PieEntry> mEntries;
    RecyclerView recyclerView;
    View mView;
    private String textInCenter="";
    private Double expenditure=0.0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        textInCenter = getArguments().getString("TEXT_IN_CENTER");
        expenditure=getArguments().getDouble("EXPENDITURE");
        // 设置背景透明
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        mView =inflater.inflate(R.layout.dialog_account_catalogue_piechart, container);
        initialPieChart();

        return mView;

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Log.d(TAG, "onCreateDialog: ");
//        textInCenter = getArguments().getString("TEXT_IN_CENTER");
//        expenditure=getArguments().getDouble("EXPENDITURE");
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.
        Dialog dialog = super.onCreateDialog(savedInstanceState);
//        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);


//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.alertDialog);

//        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
        //模糊度
//        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

//        lp.alpha=0.9f;//（0.0-1.0）
        // 透明度，黑暗度为
//        lp.dimAmount=0.9f;
//        dialog.getWindow().setAttributes(lp);
        return dialog;
    }
    PieChart mPieChart;
    TextView mTextPartText;
    private void initialPieChart() {
        //饼状图
        mPieChart = (PieChart) mView.findViewById(R.id.dialog_account_mPieChart);
        mTextPartText = mView.findViewById(R.id.dialog_account_mPieChart_tv);
        recyclerView = mView.findViewById(R.id.dialog_account_recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(accountAdapter);

        DecimalFormat df = new DecimalFormat("0.00");
        mTextPartText.setText("当月支出"+df.format(Math.abs(expenditure))+"￥");

        mPieChart.setUsePercentValues(true);
        mPieChart.getDescription().setEnabled(false);//设置描述信息
        mPieChart.setExtraOffsets(5, 10, 5, 5);//设置间距

        mPieChart.setDragDecelerationFrictionCoef(0.95f);
        //设置中间文件
        mPieChart.setCenterText(generateCenterSpannableText());

        mPieChart.setDrawHoleEnabled(true);
        mPieChart.setHoleColor(Color.WHITE);

        mPieChart.setTransparentCircleColor(Color.WHITE);//--内圆边框色
        mPieChart.setTransparentCircleAlpha(110); //--内圆边框透明度


        mPieChart.setDrawEntryLabels(true);  //设置是否绘制标签
        mPieChart.setHoleRadius(56f);//-内圆半径
        mPieChart.setTransparentCircleRadius(61f);// //--内圆边框大小半径

        mPieChart.setDrawCenterText(true);//设置是否绘制中心区域文字

        mPieChart.setRotationAngle(0);//设置旋转角度
        // 触摸旋转
        mPieChart.setRotationEnabled(true);
        mPieChart.setHighlightPerTapEnabled(true);//设置是否高亮显示触摸的区域

        //变化监听
        mPieChart.setOnChartValueSelectedListener(this);

        //模拟数据   PieEntry --参数说明：第一个参数代表半分比，第二个参数表示名字。
//        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
//        entries.add(new PieEntry(40, "优秀"));
//        entries.add(new PieEntry(20, "满分"));
//        entries.add(new PieEntry(30, "及格"));
//        entries.add(new PieEntry(10, "不及格"));

        //设置数据
        setData(mEntries);

        mPieChart.animateY(800, Easing.EasingOption.EaseInOutQuad);

        Legend l = mPieChart.getLegend();//设置比例图
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);//最右边显示
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        //      mLegend.setForm(LegendForm.LINE);  //设置比例图的形状，默认是方形
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // 输入标签样式
        mPieChart.setEntryLabelColor(Color.WHITE);
        mPieChart.setEntryLabelTextSize(12f);
    }
    //设置中间文字
    private SpannableString generateCenterSpannableText() {
        //原文：MPAndroidChart\ndeveloped by Philipp Jahoda
        SpannableString s = new SpannableString(textInCenter);
        s.setSpan(new RelativeSizeSpan(1.7f), 0, textInCenter.length(), 0);
//        s.setSpan(new StyleSpan(Typeface.NORMAL), 14, s.length() - 15, 0);
//         s.setSpan(new ForegroundColorSpan(Color.GRAY), 14, s.length() - 15, 0);
//        s.setSpan(new RelativeSizeSpan(.8f), 14, s.length() - 15, 0);
//         s.setSpan(new StyleSpan(Typeface.ITALIC), s.length() - 14, s.length(), 0);
//         s.setSpan(new ForegroundColorSpan(ColorTemplate.getHoloBlue()), s.length() - 14, s.length(), 0);
        return s;
    }
    //设置数据
    private void setData(ArrayList<PieEntry> entries) {
        PieDataSet dataSet = new PieDataSet(entries, textInCenter+"各支出占比");
        float sliceSpace=3f;
        float minValue=100f;
        for (PieEntry pieEntry : entries) {
            float value=pieEntry.getValue();
            if (value<minValue)
                minValue=value;
        }
        float space= (float) (minValue/100*56*2*Math.PI);
        Log.d(TAG, "setData: "+space);
        if (space<sliceSpace)
        {

            sliceSpace=space;
        }
        dataSet.setSliceSpace(sliceSpace);//设置不同区域之间的间距
        dataSet.setSelectionShift(5f);//--选中饼状图时，向外扩张的大小.

        //数据和颜色
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);
        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        mPieChart.setData(data);
        mPieChart.highlightValues(null);
        //刷新
        mPieChart.invalidate();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
//        Log.d(TAG, "onValueSelected: "+((PieEntry)e).getLabel());
//        Log.d(TAG, "onValueSelected: "+e.getY()+"    "+h.getY());

        DecimalFormat df = new DecimalFormat("0.00");
        mTextPartText.setText(((PieEntry)e).getLabel()+"--"+df.format(Math.abs(h.getY()*expenditure/100))+"￥");
        new AnalyseContentTask().execute(((PieEntry)e).getLabel());
    }
    public class AnalyseContentTask extends AsyncTask<String ,String,List<AccountData>>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(List<AccountData> accountData) {
            super.onPostExecute(accountData);
            accountAdapter.setData(tempLists);
        }

        @Override
        protected List<AccountData> doInBackground(String... strings) {

            if (lists==null)
            {
                return new ArrayList<>();
            }
            Log.d(TAG, "doInBackground: "+strings);
            tempLists = new ArrayList<>();
            for (AccountData data : lists) {
                if (data.getType().equals(strings[0]))
                {
                    tempLists.add(data);
                }
            }
            return tempLists;
        }
    }

    @Override
    public void onNothingSelected() {
        DecimalFormat df = new DecimalFormat("0.00");
        mTextPartText.setText("当月支出"+df.format(Math.abs(expenditure))+"￥");
        accountAdapter.setData(new ArrayList<AccountData>());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        accountAdapter.setData(lists);
    }
}
