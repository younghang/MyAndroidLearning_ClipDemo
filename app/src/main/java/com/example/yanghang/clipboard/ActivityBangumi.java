package com.example.yanghang.clipboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.FragmentBangumi;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiAdapter;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiData;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class ActivityBangumi extends AppCompatActivity {

    public static final int RESULT_BANGUMI_ACTIVITY = 132456789;
    private RecyclerView recyclerView;
    private BangumiAdapter adapter;
    private List<BangumiData> lists;
    private GridLayoutManager mLayoutManage;
    private TextView catalogueNameEdit;
    private boolean newBangumi = true;
    private Toolbar toolbar;
    private int currentPos = 0;

    public static final String NEW_BANGUMI = "newBangumi";
    public static final String TAG = "nihao_bangumi";

    private ListData listData;
    private int pos;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bangumi);

        initialView();
        initialData();


    }


    Handler handler = new Handler();

    private void initialData() {
        Bundle bundle = getIntent().getExtras();
        listData = (ListData) bundle.get(MainFormActivity.LIST_DATA);
        catalogueNameEdit.setText(listData.getRemarks());
        Log.d(TAG, "initialData: content=" + listData.getContent());
        pos = (int) bundle.get(MainFormActivity.LIST_DATA_POS);
        new Thread() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String content = listData.getContent();
                        try {
                            List<BangumiData> list = JSONArray.parseArray(content, BangumiData.class);
                            lists = list;
//                            Log.d(TAG, "run: list.size="+lists.size());
                            Collections.sort(lists, new Comparator<BangumiData>() {
                                @Override
                                public int compare(BangumiData bangumiData, BangumiData t1) {
                                    if (bangumiData.getEvaluateScore() > t1.getEvaluateScore()) {
                                        return -1;
                                    }
                                    if (bangumiData.getEvaluateScore() < t1.getEvaluateScore()) {
                                        return 1;
                                    }
                                    return 0;
                                }
                            });
                            //需要手动设置一下
                            adapter.setData(list);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }.start();


    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent(ActivityBangumi.this, MainFormActivity.class);
            intent.putExtra(MainFormActivity.LIST_DATA, listData);
            intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
            setResult(RESULT_BANGUMI_ACTIVITY, intent);
        }
        return super.onKeyDown(keyCode, event);
    }


    private void initialView() {

        toolbar = (Toolbar) findViewById(R.id.bangumi_toolbar);
        toolbar.setTitle("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            toolbar.setTitleTextColor(getColor(R.color.white));
        }
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityBangumi.this, MainFormActivity.class);
                intent.putExtra(MainFormActivity.LIST_DATA, listData);
                intent.putExtra(MainFormActivity.LIST_DATA_POS, pos);
                setResult(RESULT_BANGUMI_ACTIVITY, intent);
                finish();
            }
        });
        catalogueNameEdit = (TextView) findViewById(R.id.bangumi_edit_catalogue);
        catalogueNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(ActivityBangumi.this).inflate(R.layout.dialog_bangumi_alter_name, null);
                final EditText editText = view1.findViewById(R.id.bangumi_alter_name);
                editText.setText(catalogueNameEdit.getText());
                AlertDialog alertDialog = new AlertDialog.Builder(ActivityBangumi.this).setView(view1)
                        .setTitle("修改名称").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = editText.getText().toString();
                                catalogueNameEdit.setText(name);
                                listData.setRemarks(name);
                                saveToDataBase();
                            }
                        }).setNegativeButton("取消", null).show();
            }
        });
        recyclerView = (RecyclerView) findViewById(R.id.bangumi_recycleView);
        recyclerView.setItemAnimator(new DefaultItemAnimator());//设置默认动画
        mLayoutManage = new GridLayoutManager(this, 2);
//        mLayoutManage.setOrientation(OrientationHelper.HORIZONTAL);//设置滚动方向，横向滚动
        recyclerView.setLayoutManager(mLayoutManage);
        lists = new ArrayList<BangumiData>();

        adapter = new BangumiAdapter(lists, ActivityBangumi.this);
//        BangumiData bangumiData = new BangumiData("怪怪守护神");
//        bangumiData.setClassifications(new ArrayList<String>(){{add("打斗"); add("妖怪");}});
//        bangumiData.setComments(new HashMap<String, String>(){{put("1","还行");put("2","一般");put("3","非常好看");}});
//        bangumiData.setEpisodes(12);
//        bangumiData.setGrades(90);
//        bangumiData.setRemark("这是一部非常好看的动漫，有搞笑，有很多其他的东西");
//        bangumiData.setEpisodesState(new HashMap<String, Boolean>(){{put("1",true);put("2",true);put("3",true);}});
//        lists.add(bangumiData);
        adapter.setOnItemClickListener(new CatalogueAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                newBangumi = false;
                currentPos = position;
                invalidateOptionsMenu();
                popFragment(adapter.getItem(position), false);
            }

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public boolean OnItemLongClick(View v, int position) {
                showDeleteDialog(position);
                return true;
            }
        });
        recyclerView.setAdapter(adapter);

        FragmentManager manager = getSupportFragmentManager();
        frag = new FragmentBangumi();
        manager.beginTransaction().add(R.id.contentView, frag).commitNow();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void showDeleteDialog(final int pos) {
        View view = LayoutInflater.from(ActivityBangumi.this).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText("确定删除《"+adapter.getItemName(pos)+"》？");
        editText.setTextColor(Color.RED);
        editText.setOnKeyListener(null);
        editText.setEnabled(false);
        editText.setTextSize(20);
        editText.setBackground(getDrawable(R.drawable.corner_background));
        AlertDialog alertDialog = new AlertDialog.Builder(ActivityBangumi.this).setView(view)
                .setTitle("Alert").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        adapter.deleteItem(pos);
                        saveToDataBase();
                    }
                }).setNegativeButton("取消", null).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.bangumi_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.bangumi_menu_add).setVisible(newBangumi);
        menu.findItem(R.id.bangumi_menu_check).setVisible(!newBangumi);

        return super.onPrepareOptionsMenu(menu);
    }

    private FragmentBangumi frag;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bangumi_menu_add:
                newBangumi = false;
                invalidateOptionsMenu();
                popFragment(new BangumiData(), true);
                break;
            case R.id.bangumi_menu_check:
                BangumiData bangumiData = frag.hideFragment();
                if (frag.addNewBangumi) {
                    adapter.addItem(bangumiData);
                } else {
//                    Log.d(TAG, "onOptionsItemSelected: editItemBack. Content is follow↓↓↓");
                    adapter.editItem(currentPos, bangumiData);
                }
                saveToDataBase();
//                Log.d(TAG, "onOptionsItemSelected: bangumi="+ JSON.toJSONString(bangumiData));
                newBangumi = true;
                invalidateOptionsMenu();
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                break;
        }
        return true;
    }


    void popFragment(BangumiData bangumiData, boolean addNewBangumi) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(NEW_BANGUMI, addNewBangumi);
        bundle.putSerializable("bangumi", bangumiData);
        frag.show((View) recyclerView.getParent(), bundle);
    }

    private void saveToDataBase() {
        DBListInfoManager dbListInfoManager = new DBListInfoManager(ActivityBangumi.this);
        String content = JSONArray.toJSONString(adapter.getData());
        listData.setContent(content);
        Log.d(TAG, "saveToDataBase: content=" + content);
        dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), catalogueNameEdit.getText().toString(), content, listData.getCreateDate());

    }


}
