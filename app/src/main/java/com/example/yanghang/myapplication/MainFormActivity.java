package com.example.yanghang.myapplication;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.yanghang.myapplication.DBClipInfos.MyDBManager;
import com.example.yanghang.myapplication.ListPackage.CatalogueList.CatalogueAdatpter;
import com.example.yanghang.myapplication.ListPackage.CatalogueList.SimpleItemTouchHelperCallback;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListMessageAdapter;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.MyItemTouchHelperCallBack;

import java.util.ArrayList;
import java.util.List;

public class MainFormActivity extends AppCompatActivity {

    public static final int REQUEST_TEXT_EDITE_BACK = 0;
    public static final String LIST_DATA = "listdataToEdite";
    public static final String LIST_DATA_POS = "listdataToEditePos";
    public static boolean IsEdite = false;
    public static String MTTAG = "nihao";
    public static boolean IsDelete = false;
    MyDBManager myDBManager;
    Toolbar toolbar;
    SearchView searchView;
    private RecyclerView recyclerView;
    private RecyclerView catalogueRecycler;
    private SwipeRefreshLayout refreshLayout;
    private ListMessageAdapter messageAdapter;
    private CatalogueAdatpter catalogueAdatpter;
    private DrawerLayout mDrawerLayout;
    private List<ListData> listDatas;
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
//            Log.v(MTTAG, "开始查询");
            myDBManager.open();
            Cursor cursor = myDBManager.searchDataInContent(query);
            List<ListData> mDatas = new ArrayList<>();
            if (cursor.moveToFirst()) {
                int remarkIndex = cursor.getColumnIndex(MyDBManager.KEY_REMARK);
                int contentIndex = cursor.getColumnIndex(MyDBManager.KEY_CONTENT);
                int datetimeIndex = cursor.getColumnIndex(MyDBManager.KEY_DATETIME);
                int orderIdIndex = cursor.getColumnIndex(MyDBManager.KEY_ORDERID);
                int catalogueIndex = cursor.getColumnIndex(MyDBManager.KEY_CATALOGUE);
                while (!cursor.isAfterLast()) {
                    String remark = cursor.getString(remarkIndex);
                    String content = cursor.getString(contentIndex);
                    String datetime = cursor.getString(datetimeIndex);
                    String catalogue = cursor.getString(catalogueIndex);
                    int orderID = cursor.getInt(orderIdIndex);
                    ListData listData = new ListData(remark, content, datetime, orderID, catalogue);
                    mDatas.add(listData);
                    cursor.moveToNext();
                }
            }
            cursor.close();
            myDBManager.close();
            listDatas = mDatas;
            messageAdapter.setDatas(listDatas);
            messageAdapter.notifyDataSetChanged();
            toolbar.setTitle("查询\"" + query + "\"结果");
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            return true;

        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }


    };
    private boolean isSettingShow;

//    private SimpleCursorAdapter adapter;
//    private SQLiteDatabase db;
//    private Cursor cursor;
private List<String> mCatalogue;
    private String currentCatalogue = "";

    //    private DaoSession session;
//    private DaoMaster.DevOpenHelper helper;
//    private ListDatasDao userDao;
//    private DaoMaster master;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_form);
        InitialView();

    }

    private void InitialView() {
        myDBManager = new MyDBManager(MainFormActivity.this.getApplicationContext());
//        helper = new DaoMaster.DevOpenHelper(MainFormActivity.this, "user-db", null);
//        db = helper.getWritableDatabase();
//        master = new DaoMaster(db);
//        session = master.newSession();
//        //得到StudentDAO对象，所以在这看来，对于这三个DAO文件，我们更能接触到的是StudentDao文件，进行CRUD操作也是通过StudentDao对象来操作
//        userDao = session.getListDatasDao();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("ClipBoard");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer);

        final ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout, toolbar, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                isSettingShow = false;
                invalidateOptionsMenu();
                MyApplication.setCatalogue(catalogueAdatpter.getDatas());
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                isSettingShow = true;
                invalidateOptionsMenu();
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainFormActivity.this, ActivityConnect.class);
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

        recyclerView = (RecyclerView) findViewById(R.id.rv_ClipInfos);
        listDatas = GetDatas("");
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
        messageAdapter = new ListMessageAdapter(listDatas, this);
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
                    return false;
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, listDatas.get(position));
                intent.putExtra(LIST_DATA_POS, position);
                Log.v(MTTAG, "长按  current pos=" + position + " 数据为：  order=" + listDatas.get(position).getOrderID() + "  message=" + listDatas.get(position).getInformation());
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                return true;
            }
        });
        recyclerView.setAdapter(messageAdapter);
        new ItemTouchHelper(new MyItemTouchHelperCallBack(listDatas, recyclerView, messageAdapter, myDBManager))
                .attachToRecyclerView(recyclerView);
        IsDelete = false;
        IsEdite = false;
        InitLeftDrawerView();


    }

    private void InitLeftDrawerView() {
        catalogueRecycler = (RecyclerView) findViewById(R.id.rv_catalogue);
        // 设置布局，否则无法正常使用
        catalogueRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        catalogueAdatpter = new CatalogueAdatpter(MyApplication.getCatalogue(), MainFormActivity.this);
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(catalogueAdatpter);

        final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        catalogueAdatpter.setDragStartListener(new CatalogueAdatpter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
        catalogueRecycler.setAdapter(catalogueAdatpter);
        mItemTouchHelper.attachToRecyclerView(catalogueRecycler);
        catalogueAdatpter.setOnItemClickListener(new CatalogueAdatpter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                listDatas = GetDatas(catalogueAdatpter.getItem(position));
                messageAdapter.setDatas(listDatas);
                messageAdapter.notifyDataSetChanged();
                toolbar.setTitle(catalogueAdatpter.getItem(position));
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                return false;
            }
        });

    }

    List<ListData> GetDatas(String currentCatalogue) {
        List<ListData> mDatas = new ArrayList<ListData>();
        myDBManager.open();
        Cursor cursor = myDBManager.fetchAllDataByCatalogue(currentCatalogue);
        if (cursor == null) {
            mDatas.add(new ListData("ceshi", "magnet:?xt=urn:btih:4fc4a218aca38d73147585ff51773fc834e08810", 0, currentCatalogue));

        } else {
            if (cursor.moveToFirst()) {
                int remarkIndex = cursor.getColumnIndex(MyDBManager.KEY_REMARK);
                int contentIndex = cursor.getColumnIndex(MyDBManager.KEY_CONTENT);
                int datetimeIndex = cursor.getColumnIndex(MyDBManager.KEY_DATETIME);
                int orderIdIndex = cursor.getColumnIndex(MyDBManager.KEY_ORDERID);
                while (!cursor.isAfterLast()) {
                    String remark = cursor.getString(remarkIndex);
                    String content = cursor.getString(contentIndex);
                    String datetime = cursor.getString(datetimeIndex);
                    int orderID = cursor.getInt(orderIdIndex);
                    ListData listData = new ListData(remark, content, datetime, orderID, currentCatalogue);
                    mDatas.add(listData);
                    cursor.moveToNext();
                }
            }
            cursor.close();
        }
        myDBManager.close();
        return mDatas;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TEXT_EDITE_BACK:
                if (resultCode == RESULT_OK) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = data.getIntExtra(LIST_DATA_POS, 0);
//
                    Log.v(MTTAG, "返回后 current pos=" + pos + " 数据为：  order=" + listData.getOrderID() + "  catalogue=" + listData.getCatalogue());
                    messageAdapter.editItem(pos, listData);
                    myDBManager.open();
                    myDBManager.updateData(listData.getOrderID(), listData.getCatalogue(), listData.getRemarks(), listData.getInformation(), listData.getCreateDate());
                    myDBManager.close();

                }
                if (resultCode == ActivityEditInfo.RESULT_ADD_NEW) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = 0;
                    Log.v("TEM", pos + listData.getInformation());

                    myDBManager.open();

                    long result = myDBManager.insertData(listData.getRemarks(), listData.getInformation(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());
                    myDBManager.close();
                    if (result == -1)
                        Toast.makeText(MainFormActivity.this, "存储该行数据出错", Toast.LENGTH_SHORT).show();
                    else
                        messageAdapter.addItem(listData);
                }
                break;

        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_info:
                myDBManager.open();
                int orderid = myDBManager.getDataCount();
                myDBManager.close();
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, new ListData("", "", orderid, "default"));
                intent.putExtra(LIST_DATA_POS, -1);
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                break;
            case R.id.edit_info:
                IsEdite = !IsEdite;
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
            case R.id.menu_main_search:
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.add_catalogue).setVisible(isSettingShow);
        menu.findItem(R.id.settings).setVisible(isSettingShow);
        menu.findItem(R.id.add_info).setVisible(!isSettingShow);
        menu.findItem(R.id.del_info).setVisible(!isSettingShow);
        menu.findItem(R.id.edit_info).setVisible(!isSettingShow);
        menu.findItem(R.id.menu_main_search).setVisible(isSettingShow);

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_form_menu, menu);
        searchView = (SearchView) menu.findItem(R.id.menu_main_search).getActionView();
        if (searchView != null) {
//            Toast.makeText(MainFormActivity.this, "null searchview", Toast.LENGTH_SHORT).show();
            searchView.setOnQueryTextListener(onQueryTextListener);
        }

        return super.onCreateOptionsMenu(menu);

    }


}
