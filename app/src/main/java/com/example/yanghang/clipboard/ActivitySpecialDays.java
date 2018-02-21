package com.example.yanghang.clipboard;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.FragmentSpecialDayInfo;
import com.example.yanghang.clipboard.Fragment.FragmentSpecialDayText;
import com.example.yanghang.clipboard.OthersView.Rotate3D;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;
import com.jzxiang.pickerview.TimePickerDialog;
import com.jzxiang.pickerview.data.Type;
import com.jzxiang.pickerview.listener.OnDateSetListener;

import com.example.yanghang.clipboard.Fragment.JsonData.SpecialDaysData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.yanghang.clipboard.ActivityBangumi.RESULT_BANGUMI_ACTIVITY;

public class ActivitySpecialDays extends SwipeBackActivity implements OnDateSetListener ,FragmentSpecialDayInfo.OnFragmentInteractionListener {

    private static final String TAG = "nihao";
    FragmentManager fragmentManager;
    private ListData listData;
    private int pos;
    private SpecialDaysData specialDaysData;
    private TimePickerDialog mDialogYearMonthDay;
    private FragmentSpecialDayInfo fragmentSpecialDayInfo;
    private FragmentSpecialDayText fragmentSpecialDayText;
    private SwipeBackLayout mSwipeBackLayout;

    private FloatingActionButton fab;
    private FrameLayout flContainer;
    private LinearLayout linearLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        setContentView(R.layout.activity_special_days);
        initialData();
        initialView();
    }



    private void initialData() {
        fragmentManager = getSupportFragmentManager();
        Bundle bundle = getIntent().getExtras();
        listData = (ListData) bundle.get(MainFormActivity.LIST_DATA);
        pos = (int) bundle.get(MainFormActivity.LIST_DATA_POS);
        String content = listData.getContent();
        specialDaysData=null;
        try {
            specialDaysData= JSON.parseObject(content, SpecialDaysData.class);
        }
        catch (Exception e)
        {
           //这里不会报错的，阿里库内部已经处理掉了
            e.printStackTrace();

        }
        if (specialDaysData==null)
        {

            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String date = sDateFormat.format(new java.util.Date());
            specialDaysData = new SpecialDaysData(date,"");
        }


    }
    private Boolean isFront=true;
    private void initialView() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.activity_special_day_toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        mSwipeBackLayout = getSwipeBackLayout();
        //设置可以滑动的区域，推荐用屏幕像素的一半来指定
        mSwipeBackLayout.setEdgeSize(100);
        //设定滑动关闭的方向，SwipeBackLayout.EDGE_ALL表示向下、左、右滑动均可。EDGE_LEFT，EDGE_RIGHT，EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_BOTH);

        linearLayout = (LinearLayout) findViewById(R.id.activity_special_day_ll);
        flContainer = (FrameLayout) findViewById(R.id.activity_special_day_container);
        fragmentSpecialDayText = (FragmentSpecialDayText) fragmentManager.findFragmentById(R.id.activity_special_day_contentView_back);
        fragmentSpecialDayText.getView().setVisibility(View.GONE);
        fragmentSpecialDayText.setInfoText(specialDaysData.getInfo());
        fragmentSpecialDayInfo=FragmentSpecialDayInfo.newInstance(specialDaysData.getDate(),listData.getRemarks());
        setFragment(fragmentSpecialDayInfo);

        final Window window = getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
//        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
//        window.setStatusBarColor(color);
        fab= (FloatingActionButton) findViewById(R.id.activity_special_day_fab);
        fab.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View v) {
                isFront = !isFront;
                if (isFront)
                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        window.setStatusBarColor(getResources().getColor(R.color.bgColor));
//                    }
//                    linearLayout.setBackgroundColor(getResources().getColor(R.color.bgColor));
                    applyRotation(0,0,-90);
                }
                else
                {
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                        window.setStatusBarColor(getResources().getColor(R.color.text_10));
//                    }
//                    linearLayout.setBackgroundColor(getResources().getColor(R.color.text_10));
                    applyRotation(1,0,90);
                }
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.special_day_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_calendar:
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                Date selectDate= Calendar.getInstance().getTime();
                Date miniDate = new Date();
                try {
                    selectDate=sDateFormat.parse(specialDaysData.getDate());
                    miniDate = sDateFormat.parse("1980-01-01");
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if(isFront)
                {
                    mDialogYearMonthDay = new TimePickerDialog.Builder()
                            .setType(Type.YEAR_MONTH_DAY)
                            .setTitleStringId("选择日")
                            .setMinMillseconds(miniDate.getTime())
                            .setCurrentMillseconds(selectDate.getTime())
                            .setCallBack(this)
                            .setThemeColor(getResources().getColor(R.color.deep_gray))
                            .setWheelItemTextSelectorColor(getResources().getColor(R.color.light_gray))
                            .build();
                }
                else {
                    mDialogYearMonthDay = new TimePickerDialog.Builder()
                            .setType(Type.YEAR_MONTH_DAY)
                            .setTitleStringId("选择日")
                            .setMinMillseconds(miniDate.getTime())
                            .setCurrentMillseconds(selectDate.getTime())
                            .setCallBack(this)
                            .setThemeColor(getResources().getColor(R.color.black))
                            .setWheelItemTextSelectorColor(getResources().getColor(R.color.light_gray))
                            .build();
                }

                mDialogYearMonthDay.show(getSupportFragmentManager(), "year_month_day");
                break;
            case R.id.menu_editable:
                View view1 = LayoutInflater.from(ActivitySpecialDays.this).inflate(R.layout.dialog_special_day_alter_info, null);
                final EditText editText = view1.findViewById(R.id.dialog_special_day_alter_info_editText);
                editText.setText(specialDaysData.getInfo());
                AlertDialog alertDialog = new AlertDialog.Builder(ActivitySpecialDays.this).setView(view1)
                        .setTitle("修改").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = editText.getText().toString();
                                specialDaysData.setInfo(name);
                                fragmentSpecialDayText.setInfoText(specialDaysData.getInfo());
                                saveData();
                            }
                        }).setNegativeButton("取消", null).show();
                break;
        }
        return true;
    }
    void setFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction= fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.activity_special_day_contentView, fragment);
        //下面这句会导致回退不彻底
        //fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//          saveData();
        }
        return super.onKeyDown(keyCode, event);
    }
    private void saveData()
    {
        listData.setContent(JSON.toJSONString(specialDaysData));
        Intent intent = new Intent(ActivitySpecialDays.this, MainFormActivity.class);
        intent.putExtra(MainFormActivity.LIST_DATA, listData);
        intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
        setResult(RESULT_BANGUMI_ACTIVITY, intent);
        //这个得自己保存，MainActivity 不管的
        DBListInfoManager dbListInfoManager = new DBListInfoManager(ActivitySpecialDays.this);
        dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), listData.getRemarks(), listData.getContent(), listData.getCreateDate());

    }

    @Override
    public void onDateSet(TimePickerDialog timePickerView, long millseconds) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = new Date(millseconds);
        String text = sf.format(d);
        specialDaysData.setDate(text);
        saveData();
        fragmentSpecialDayInfo.updateInfo(text);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    /**
     * 执行翻转第一阶段翻转动画
     * @param tag view索引
     * @param start 起始角度
     * @param end 结束角度
     */
    private void applyRotation(int tag, float start, float end) {
        // 得到中心点(以中心翻转)
        //X轴中心点
        final float centerX = flContainer.getWidth() / 2.0f;
        //Y轴中心点
        final float centerY = flContainer.getHeight() / 2.0f;
        //Z轴中心点
        final float depthZ = 500.0f;
        // 根据参数创建一个新的三维动画,并且监听触发下一个动画
        final Rotate3D rotation = new Rotate3D(start, end, centerX, centerY,depthZ, true);
        rotation.setDuration(200);//设置动画持续时间
        rotation.setInterpolator(new AccelerateInterpolator());//设置动画变化速度
        rotation.setAnimationListener(new DisplayNextView(tag));//设置第一阶段动画监听器
        flContainer.startAnimation(rotation);


    }

    /**
     * 第一阶段动画监听器
     *
     */
    private final class DisplayNextView implements Animation.AnimationListener {
        private final int tag;

        private DisplayNextView(int tag) {
            this.tag = tag;
        }

        public void onAnimationStart(Animation animation) {
        }

        public void onAnimationEnd(Animation animation) {
            //第一阶段动画结束时，也就是整个Activity垂直于手机屏幕，
            //执行第二阶段动画
            final Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            if (isFront)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(getResources().getColor(R.color.bgColor));
                }
                linearLayout.setBackgroundColor(getResources().getColor(R.color.bgColor));

            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    window.setStatusBarColor(getResources().getColor(R.color.text_10));
                }
                linearLayout.setBackgroundColor(getResources().getColor(R.color.text_10));

            }
            flContainer.post(new SwapViews(tag));
            //调整两个界面各自的visibility
            adjustVisiable();
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    /**
     * 执行翻转第二个阶段动画
     *
     */
    private final class SwapViews implements Runnable {
        private final int tag;

        public SwapViews(int position) {
            tag = position;
        }

        public void run() {
            if (tag == 0) {
                //首页页面以90~0度翻转
                showView(fragmentSpecialDayInfo.getView(), fragmentSpecialDayText.getView(), 90, 0);
            } else if (tag == 1) {
                //音乐页面以-90~0度翻转
                showView(fragmentSpecialDayText.getView(), fragmentSpecialDayInfo.getView(), -90, 0);
            }
        }
    }

    /**
     * 显示第二个视图动画
     * @param showView 要显示的视图
     * @param hiddenView 要隐藏的视图
     * @param startDegree 开始角度
     * @param endDegree 目标角度
     */
    private void showView(View showView, View hiddenView, int startDegree, int endDegree) {
        //同样以中心点进行翻转
        float centerX = showView.getWidth() / 2.0f;
        float centerY = showView.getHeight() / 2.0f;
        float centerZ = 0.0f;
        if (centerX == 0 || centerY == 0) {
            //调用该方法getMeasuredWidth()，必须先执行measure()方法，否则会出异常。
            showView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            //获取该view在父容器里面占的大小
            centerX = showView.getMeasuredWidth() / 2.0f;
            centerY = showView.getMeasuredHeight() / 2.0f;
        }
//        Log.e("centerX",centerX + "");
//        Log.e("centerY",centerY + "");
        hiddenView.setVisibility(View.GONE);
        showView.setVisibility(View.VISIBLE);
        Rotate3D rotation = new Rotate3D(startDegree, endDegree, centerX, centerY, centerZ, false);
        rotation.setDuration(200);//设置动画持续时间
        rotation.setInterpolator(new DecelerateInterpolator());//设置动画变化速度
        flContainer.startAnimation(rotation);
    }

    /**
     * 两个布局的visibility调节
     */

    private void adjustVisiable(){
        if(fragmentSpecialDayInfo.getView().getVisibility() == View.VISIBLE){
            fragmentSpecialDayInfo.getView().setVisibility(View.GONE);
        }else {
            fragmentSpecialDayInfo.getView().setVisibility(View.VISIBLE);
        }
        if(fragmentSpecialDayText.getView().getVisibility() == View.VISIBLE){
            fragmentSpecialDayText.getView().setVisibility(View.GONE);
        }else {
            fragmentSpecialDayText.getView().setVisibility(View.VISIBLE);
        }
    }
}
