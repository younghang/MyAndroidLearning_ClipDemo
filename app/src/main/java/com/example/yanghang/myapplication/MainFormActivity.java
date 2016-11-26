package com.example.yanghang.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.yanghang.myapplication.ListPackage.ListData;
import com.example.yanghang.myapplication.ListPackage.ListMessageAdapter;
import com.example.yanghang.myapplication.ListPackage.MyItemTouchHelperCallBack;

import java.util.ArrayList;
import java.util.List;

public class MainFormActivity extends AppCompatActivity   {

    public static boolean IsEdite=false;
    public static boolean IsDelete = false;
    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private ListMessageAdapter messageAdapter;
    private DrawerLayout mDrawerLayout;
    public static final int REQUEST_TEXT_EDITE_BACK=0;
    public static final String LIST_DATA="listdataToEdite";
    public static final String LIST_DATA_POS="listdataToEditePos";
    private  List<ListData> listDatas=GetDatas();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_form);
        InitialView();

    }

    private void InitialView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ClipBoard");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,toolbar,R.string.navigation_drawer_open,
                R.string.navigation_drawer_close

        ) ;
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainFormActivity.this, MainActivity.class);
                startActivity(intent);


            }
        });

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.reglost_srl);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.rv_Message);
        listDatas=GetDatas();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
        messageAdapter=new ListMessageAdapter(listDatas,this);
        messageAdapter.setOnItemClickListener(new ListMessageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(listDatas.get(position).getInformation());
                Toast.makeText(MainFormActivity.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                if (IsEdite)
                    return true;
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, listDatas.get(position));
                intent.putExtra(LIST_DATA_POS, position);
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                return false;
            }
        });
        recyclerView.setAdapter(messageAdapter);
        new ItemTouchHelper(new MyItemTouchHelperCallBack(listDatas,recyclerView,messageAdapter))
                .attachToRecyclerView(recyclerView);
        IsDelete = false;
        IsEdite = false;
    }


    List<ListData> GetDatas()
    {
        List<ListData> mDatas = new ArrayList<ListData>();

        for (int i=0;i<20;i++)
        {
            mDatas.add(new ListData("hello","这是消息"+i));
        }
        mDatas.add(new ListData("ceshi","magnet:?xt=urn:btih:4fc4a218aca38d73147585ff51773fc834e08810"));
        return mDatas;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode)
        {
            case REQUEST_TEXT_EDITE_BACK:
                if (resultCode==RESULT_OK) {
                    ListData listData= (ListData) data.getExtras().get(LIST_DATA);
                    int pos = data.getIntExtra(LIST_DATA_POS,0);
//                    listDatas.get(pos).setInformation(listData.getInformation());
//                    listDatas.get(pos).setRemarks(listData.getRemarks());
                    Log.v("TEM", pos + listData.getInformation());
                    messageAdapter.deleteItem(pos);
                    messageAdapter.addItem(listData,pos);

                }
                if (resultCode== ActivityEditInfo.RESULT_ADD_NEW)
                {
                    ListData listData= (ListData) data.getExtras().get(LIST_DATA);
                    int pos =0;
                    Log.v("TEM", pos + listData.getInformation());
                    messageAdapter.addItem(listData);

                }
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            case R.id.add_info:
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, new ListData("",""));
                intent.putExtra(LIST_DATA_POS, -1);
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                break;
            case R.id.edit_info:
                IsEdite=!IsEdite;
                if (IsEdite)
                item.setIcon(R.mipmap.ic_sort);
                else
                    item.setIcon(R.drawable.ic_edit_white_24dp);
                break;
            case R.id.del_info:
                IsDelete = !IsDelete;
                if (IsDelete)
                    item.setIcon(R.drawable.ic_delete_active_24dp);
                else
                    item.setIcon(R.drawable.ic_delete_inactive_24dp);
                break;

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_form_menu,menu);
        return super.onCreateOptionsMenu(menu);

    }




}
