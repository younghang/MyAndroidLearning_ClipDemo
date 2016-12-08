package com.example.yanghang.myapplication;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.example.yanghang.myapplication.DBClipInfos.MyDBManager;
import com.example.yanghang.myapplication.FileUtils.FileUtils;
import com.example.yanghang.myapplication.ListPackage.CatalogueList.CatalogueAdatpter;
import com.example.yanghang.myapplication.ListPackage.CatalogueList.SimpleItemTouchHelperCallback;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.ListMessageAdapter;
import com.example.yanghang.myapplication.ListPackage.ClipInfosList.MyItemTouchHelperCallBack;

import java.util.List;

public class MainFormActivity extends AppCompatActivity {

    public static final int REQUEST_TEXT_EDITE_BACK = 0;
    public static final String LIST_DATA = "listdataToEdite";
    public static final String LIST_DATA_POS = "listdataToEditePos";
    private static final int MSG_FINISH_LOADING_DATA = 123;
    private static final String MSG_LOADING_DATA = "finish_loading_listdata";
    public static boolean IsEdite = false;
    public static String MTTAG = "nihao";
    public static boolean IsDelete = false;
    MyDBManager myDBManager;
    Toolbar toolbar;
    SearchView searchView;
    List<String> catalogues;
    EditText editText;
    Button btnOK;
    Button btnCancle;
    private RecyclerView recyclerView;
    private RecyclerView catalogueRecycler;
    private SwipeRefreshLayout refreshLayout;
    private ListMessageAdapter messageAdapter;
    private CatalogueAdatpter catalogueAdatpter;
    private DrawerLayout mDrawerLayout;
    private List<ListData> listDatas;
    Handler hander = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgLoading = msg.getData().getInt(MSG_LOADING_DATA);
            switch (msgLoading) {
                case MSG_FINISH_LOADING_DATA:
                    refreshLayout.setRefreshing(false);
                    messageAdapter.setDatas(listDatas);
                    messageAdapter.notifyDataSetChanged();

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainFormActivity.this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
                    recyclerView.setAdapter(messageAdapter);
                    break;
            }
        }
    };
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(final String query) {
//            Log.v(MTTAG, "开始查询");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listDatas = myDBManager.searchData(query);
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putInt(MSG_LOADING_DATA, MSG_FINISH_LOADING_DATA);
                    msg.setData(data);
                    hander.sendMessage(msg);
                }
            }).start();

//            messageAdapter.setDatas(listDatas);
//            messageAdapter.notifyDataSetChanged();
            toolbar.setTitle("查询\"" + query + "\"结果");
            mDrawerLayout.closeDrawer(Gravity.LEFT);
            refreshLayout.setRefreshing(true);

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
    private PopupWindow popupWindow;

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
                FileUtils.saveCatalogue(getApplicationContext().getFilesDir().getAbsolutePath(), catalogueAdatpter.getDatas());
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                        || !Environment.isExternalStorageRemovable()) {
                    FileUtils.saveCatalogue(getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), catalogueAdatpter.getDatas());
                }
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
        listDatas = myDBManager.getDatas("");
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
        messageAdapter = new ListMessageAdapter(listDatas, this);
        messageAdapter.setOnItemClickListener(new ListMessageAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                Log.v(MTTAG, "ItemClick orderid=" + messageAdapter.getItemData(position).getOrderID());
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(listDatas.get(position).getContent());
                Toast.makeText(MainFormActivity.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                if (IsEdite)
                    return false;
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, listDatas.get(position));
                intent.putExtra(LIST_DATA_POS, position);
                Log.v(MTTAG, "长按  current pos=" + position + " 数据为：  order=" + listDatas.get(position).getOrderID() + "  message=" + listDatas.get(position).getContent() + "  catalogue=" + listDatas.get(position).getCatalogue());
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                return true;
            }
        });
        recyclerView.setAdapter(messageAdapter);
        new ItemTouchHelper(new MyItemTouchHelperCallBack(recyclerView, messageAdapter, myDBManager))
                .attachToRecyclerView(recyclerView);
        IsDelete = false;
        IsEdite = false;
        InitLeftDrawerView();


    }

    private void InitLeftDrawerView() {
        catalogueRecycler = (RecyclerView) findViewById(R.id.rv_catalogue);
        catalogues = FileUtils.loadCatalogue(getApplicationContext().getFilesDir().getAbsolutePath());
        // 设置布局，否则无法正常使用
        catalogueRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        if (catalogues.indexOf("default") == -1) {
            catalogues.add(0, "default");
        }
        catalogueAdatpter = new CatalogueAdatpter(catalogues, MainFormActivity.this);
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
                final String catlogue = catalogueAdatpter.getItem(position);
                refreshLayout.setRefreshing(true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        listDatas = myDBManager.getDatas(catlogue);
                        Message msg = new Message();
                        Bundle data = new Bundle();
                        data.putInt(MSG_LOADING_DATA, MSG_FINISH_LOADING_DATA);
                        msg.setData(data);
                        hander.sendMessage(msg);

                    }
                }).start();

//                Log.v(MTTAG, "ItemClick:  catalogue=" + catlogue);

                toolbar.setTitle(catalogueAdatpter.getItem(position));
                currentCatalogue = catlogue;
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                String catlogue = catalogueAdatpter.getItem(position);
//                Log.v(MTTAG, "ItemLongClick:  catalogue=" + catlogue);
                showPopWindow(catlogue);
                return true;
            }
        });


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
                    myDBManager.updateData(listData.getOrderID(), listData.getCatalogue(), listData.getRemarks(), listData.getContent(), listData.getCreateDate());

                }
                if (resultCode == ActivityEditInfo.RESULT_ADD_NEW) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = 0;
                    Log.v("TEM", pos + listData.getContent());
                    long result = myDBManager.insertData(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

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
                int orderid = myDBManager.getDataCount();
                Log.v(MTTAG, "新建 orderid=" + orderid);
                Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, new ListData("", "", orderid, currentCatalogue));
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
            case R.id.add_catalogue:
//                showDialog();
                showPopWindow("");
                break;
            case R.id.settings:
                Intent intent1 = new Intent(MainFormActivity.this, SettingsActivity.class);
                startActivity(intent1);
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    private void showDialog() {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.pop_window, null);
        final EditText editText = (EditText) view.findViewById(R.id.edit_catalogue_pop_window);
        AlertDialog alertDialog = new AlertDialog.Builder(MainFormActivity.this)
                .setTitle("添加新项")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String str = editText.getText().toString();
                        Toast.makeText(MainFormActivity.this, str, Toast.LENGTH_SHORT).show();

                    }
                })
                .setNegativeButton("取消", null)
                .setNeutralButton("哈哈", null)
                .show();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.add_catalogue).setVisible(isSettingShow);
        menu.findItem(R.id.settings).setVisible(isSettingShow);
        menu.findItem(R.id.add_info).setVisible(!isSettingShow);
//        if (currentCatalogue.equals(""))
        menu.findItem(R.id.del_info).setVisible(!isSettingShow);
//        else {
//            menu.findItem(R.id.del_info).setVisible(false);
//            IsDelete = false;
//        }

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

    private void showPopWindow(final String catalogueName) {

        int width = (int) getResources().getDimension(R.dimen.pop_window_width);
        int height = (int) getResources().getDimension(R.dimen.pop_window_height);
        if (popupWindow == null) {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(R.layout.pop_window, null);

            editText = (EditText) view.findViewById(R.id.edit_catalogue_pop_window);
            btnOK = (Button) view.findViewById(R.id.btn_ok_catalogue_pop_window);
            btnCancle = (Button) view.findViewById(R.id.btn_cancle_catalogue_pop_window);

            // 创建一个PopuWidow对象

            popupWindow = new PopupWindow(view, width, height);
        }
        editText.setText(catalogueName);
        btnCancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss();
            }
        });
        btnOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String catalogueNewName = editText.getText().toString();
                if (catalogues.indexOf(catalogueNewName) != -1) {
                    Toast.makeText(MainFormActivity.this, catalogueNewName + "已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (catalogueName.equals("")) {
                    catalogues.add(0, catalogueNewName);
                    catalogueAdatpter.notifyDataSetChanged();

                } else {
                    int index = 0;
                    index = catalogues.indexOf(catalogueName);
                    if (index == -1)
                        return;
//                    Log.v(MTTAG, "change catalogue: index" + index + "  catalogue=" + catalogueName);
                    setCatalogueChanged(catalogueName, catalogueNewName);
                    catalogues.set(index, catalogueNewName);
                    catalogueAdatpter.notifyItemChanged(index);
                }
                popupWindow.dismiss();
            }
        });
        // 使其聚集
        popupWindow.setFocusable(true);
        // 设置允许在外点击消失
        popupWindow.setOutsideTouchable(true);

        // 这个是为了点击“返回Back”也能使其消失，并且并不会影响你的背景
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        // 显示的位置为:屏幕的宽度的一半-PopupWindow的高度的一半
        Point rect = new Point();
        windowManager.getDefaultDisplay().getSize(rect);
        int xPos = rect.x / 2
                - popupWindow.getWidth() / 2;
        int yPos = popupWindow.getWidth() / 2;
        popupWindow.setAnimationStyle(R.style.popwin_anim_style);//设置窗口显示的动画效果
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.showAsDropDown(this.findViewById(R.id.toolbar), xPos, yPos);


    }

    private void setCatalogueChanged(String oldCatalogue, String newCatalogue) {
        myDBManager.changeCatalogue(oldCatalogue, newCatalogue);
    }


}
