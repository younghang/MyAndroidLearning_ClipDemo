package com.example.yanghang.clipboard;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.Fragment.JsonData.DiaryData;
import com.example.yanghang.clipboard.ListPackage.BangumiList.BangumiData;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueInfos;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.SimpleItemTouchHelperCallback;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListClipInfoAdapter;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.MyItemTouchHelperCallBack;
import com.example.yanghang.clipboard.ListPackage.DailyTaskList.DailyTaskAdapter;
import com.example.yanghang.clipboard.ListPackage.DailyTaskList.DailyTaskData;
import com.example.yanghang.clipboard.Task.TaskAutoSave;
import com.example.yanghang.clipboard.Task.TaskShowToDoList;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.List;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainFormActivity extends AppCompatActivity implements ListClipInfoAdapter.IonSlidingViewClickListener {

    private static final String DONT_ASK_AGAIN = "dont_ask_again";
    public static final int REQUEST_TEXT_EDITE_BACK = 0;
    public static final String LIST_DATA = "listdataToEdite";
    public static final String LIST_DATA_POS = "listdataToEditePos";
    private static final int MSG_FINISH_SORTING_DATA = 123;
    private static final String MSG_SEARCH_DATA = "finish_sorting_listdata";
    private static final int MSG_FINISH_CHECK_TODO_DATA = 456;
    private static final int MSG_FINISH_CHECK_DAILY_DATA = 5623;
    public static boolean IsEdite = false;

    public static String TAG = "nihao";
    public static boolean IsDelete = false;

    DBListInfoManager dbListInfoManager;
    Toolbar toolbar;
    SearchView searchView;
    List<CatalogueInfos> catalogues;
    EditText editText;
    Button btnOK;
    Button btnCancle;
    Button btnCalendar;
    public static int TotalDataCount = 0;
    private RecyclerView recyclerView;
    private RecyclerView catalogueRecycler;
    private SwipeRefreshLayout refreshLayout;
    private ListClipInfoAdapter listClipInfoAdapter;
    private CatalogueAdapter catalogueAdapter;
    private LinearLayoutManager linearLayoutManager;
    private DrawerLayout mDrawerLayout;
    private List<ListData> listDatas;
    private String messageToDoList;
    private List<DailyTaskData> dailyList;
    private ListData todayMissionList;
    private View popPositionTagView;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int msgLoading = msg.getData().getInt(MSG_SEARCH_DATA);
            switch (msgLoading) {
                case MSG_FINISH_SORTING_DATA:
                    refreshLayout.setRefreshing(false);
                    listClipInfoAdapter.setDatas(listDatas);

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainFormActivity.this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
                    recyclerView.setAdapter(listClipInfoAdapter);
                    break;
                case MSG_FINISH_CHECK_TODO_DATA:
                    View view = LayoutInflater.from(MainFormActivity.this.getApplicationContext()).inflate(R.layout.loading, null);
                    final EditText editText = (EditText) view.findViewById(R.id.loadingEditText);
                    final ProgressBar progress = (ProgressBar) view.findViewById((R.id.loadingProgressBar));

                    final AlertDialog loadingDialog = new AlertDialog.Builder(MainFormActivity.this).setView(view)
                            .setTitle("待办事项").create();

                    editText.setText(MainFormActivity.this.messageToDoList);
                    editText.setKeyListener(null);

                    editText.setBackground(null);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        editText.setTextColor(getColor(R.color.message_text));
                    } else {
                        editText.setTextColor(getResources().getColor(R.color.message_text));
                    }
                    editText.setTextSize(18);
                    progress.setVisibility(View.INVISIBLE);
                    if (messageToDoList.trim().equals(""))
                        return;
                    loadingDialog.show();
                    break;
                case MSG_FINISH_CHECK_DAILY_DATA:
                    View dialogDailyTaskView = LayoutInflater.from(MainFormActivity.this.getApplicationContext()).inflate(R.layout.dialog_show_daily_list, null);
                    RecyclerView recyclerView = dialogDailyTaskView.findViewById(R.id.dialog_dailyTask_recyclerView);
                    final DailyTaskAdapter dailyTaskAdapter = new DailyTaskAdapter(MainFormActivity.this, dailyList);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());//设置默认动画

                    recyclerView.setLayoutManager(new LinearLayoutManager(MainFormActivity.this, LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(dailyTaskAdapter);
                    new android.support.v7.app.AlertDialog.Builder(MainFormActivity.this).setView(dialogDailyTaskView)
                            .setTitle("日常任务")
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dbListInfoManager.updateDataByOrderId(todayMissionList.getOrderID(), todayMissionList.getCatalogue(), dailyTaskAdapter.getTotalProgress(), JSONArray.toJSONString(dailyTaskAdapter.getLists()), todayMissionList.getCreateDate());
                                    listClipInfoAdapter.setDatas(dbListInfoManager.getDatas(""));

                                }
                            }).setNegativeButton("取消", null)
                            .show();

                    break;

            }
        }
    };
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(final String query) {
//            Log.v(TAG, "开始查询");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    listDatas = dbListInfoManager.searchData(query);
                    Message msg = new Message();
                    Bundle data = new Bundle();
                    data.putInt(MSG_SEARCH_DATA, MSG_FINISH_SORTING_DATA);
                    msg.setData(data);
                    handler.sendMessage(msg);
                }
            }).start();

//            listClipInfoAdapter.setDatas(listDatas);
//            listClipInfoAdapter.notifyDataSetChanged();
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
    private String currentCatalogueDetail = "";
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
        initGlobalPer();

    }

    private void InitialView() {
        dbListInfoManager = new DBListInfoManager(MainFormActivity.this.getApplicationContext());
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
                FileUtils.saveCatalogue(getApplicationContext().getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
                if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                        || !Environment.isExternalStorageRemovable()) {
                    FileUtils.saveCatalogue(getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
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

//不是兼容包
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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                listDatas = dbListInfoManager.getDatas(currentCatalogue);
                                TotalDataCount = listDatas.size();
                                Message msg = new Message();
                                Bundle data = new Bundle();
                                data.putInt(MSG_SEARCH_DATA, MSG_FINISH_SORTING_DATA);
                                msg.setData(data);
                                handler.sendMessage(msg);
                            }
                        }).start();

                    }
                }, 3000);
            }
        });
        refreshLayout.setRefreshing(true);

        recyclerView = (RecyclerView) findViewById(R.id.rv_ClipInfos);
        listDatas = new ArrayList<>();
        new Thread(new Runnable() {
            @Override
            public void run() {
                listDatas = dbListInfoManager.getDatas("");
                TotalDataCount = listDatas.size();
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putInt(MSG_SEARCH_DATA, MSG_FINISH_SORTING_DATA);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }).start();
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)); // 设置布局，否则无法正常使用
        listClipInfoAdapter = new ListClipInfoAdapter(listDatas, this);
        listClipInfoAdapter.setOnItemClickListener(new ListClipInfoAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
//                Log.v(TAG, "ItemClick orderid=" + listClipInfoAdapter.getItemData(position).getOrderID());
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(listClipInfoAdapter.getItemData(position).getSimpleContent());
                Toast.makeText(MainFormActivity.this, "复制到粘贴板", Toast.LENGTH_LONG).show();
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                if (IsEdite)
                    return false;
                //不要用 listDatas.get
                ListData data = listClipInfoAdapter.getItemData(position);
                Intent intent;
                if (data.getCatalogue().equals("番剧")) {
                    intent = new Intent(MainFormActivity.this, ActivityBangumi.class);
                } else {
                    intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                }
                intent.putExtra(LIST_DATA, listClipInfoAdapter.getItemData(position));
                intent.putExtra(LIST_DATA_POS, position);
                startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
//                Log.v(TAG, "长按  current pos=" + position + " 数据为：  order=" + listClipInfoAdapter.getItemData(position).getOrderID() + "  message=" + listClipInfoAdapter.getItemData(position).getContent() + "  catalogue=" + listClipInfoAdapter.getItemData(position).getCatalogue());
                return true;
            }
        });
        recyclerView.setAdapter(listClipInfoAdapter);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        new ItemTouchHelper(new MyItemTouchHelperCallBack(recyclerView, listClipInfoAdapter, dbListInfoManager))
                .attachToRecyclerView(recyclerView);
        IsDelete = false;
        IsEdite = false;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                InitLeftDrawerView();
            }
        }, 300);
        new TaskShowToDoList(MainFormActivity.this, new TaskShowToDoList.IShowToDoList() {
            @Override
            public void showToDoList(String messageToDoList) {
                Message msg = new Message();
                Bundle data = new Bundle();
                MainFormActivity.this.messageToDoList = messageToDoList;
                data.putInt(MSG_SEARCH_DATA, MSG_FINISH_CHECK_TODO_DATA);
                msg.setData(data);
                handler.sendMessage(msg);
            }

            @Override
            public void showTodayMission(String todayMission) {
                //nothing need to be done
            }

            @Override
            public void showDailyList(List<DailyTaskData> mDailyList, ListData listData) {
                Message msg = new Message();
                Bundle data = new Bundle();
                MainFormActivity.this.dailyList = mDailyList;
                MainFormActivity.this.todayMissionList = listData;
                if (mDailyList == null || listData == null||mDailyList.isEmpty())
                    return;
                data.putInt(MSG_SEARCH_DATA, MSG_FINISH_CHECK_DAILY_DATA);
                msg.setData(data);
                handler.sendMessage(msg);
            }
        }).runToDoListCheck();

        btnCalendar = (Button) findViewById(R.id.main_activity_form_btn_calendar);
        btnCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainFormActivity.this, ActivityCalendar.class);
                startActivity(intent);
            }
        });
        new TaskAutoSave(getApplicationContext()).runAutoSave();
        /*
        修改用的
         */
        ///----------------------------------------------------------------------
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<ListData> listDatas = dbListInfoManager.getDatas("dailyMission");
////                List<BangumiData> list = new ArrayList<BangumiData>();
//                for (int i = 0; i < listDatas.size(); i++) {
//                    ListData listData = listDatas.get(i);
//                    dbListInfoManager.updateDataByOrderId(listData.getOrderID(),listData.getCatalogue(),listData.getRemarks(),listData.getContent().replace("taskProgress","tP").replace("taskName","tN"),listData.getCreateDate());
//
//
//                }
////                ListData listData = new ListData("2017年4月新番",JSONArray.toJSONString(list),dbListInfoManager.getDataCount(),"番剧");
////                dbListInfoManager.insertData(listData);
//
//            }
//        }).start();

        ///--------------------------------------------------
        popPositionTagView = findViewById(R.id.main_form_view);

    }

    private void InitLeftDrawerView() {
        catalogueRecycler = (RecyclerView) findViewById(R.id.rv_catalogue);
        try {
            catalogues = FileUtils.loadCatalogue(getFilesDir().getAbsolutePath());

        } catch (Exception e) {
            catalogues = FileUtils.loadCatalogue(getExternalFilesDir(null).getAbsolutePath());

        }
        // 设置布局，否则无法正常使用
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        catalogueRecycler.setLayoutManager(linearLayoutManager);

        catalogueAdapter = new CatalogueAdapter(catalogues, MainFormActivity.this);
        if (!catalogueAdapter.contains("default")) {
            catalogueAdapter.addItem(new CatalogueInfos("default", ""));
        }
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(catalogueAdapter);

        final ItemTouchHelper mItemTouchHelper = new ItemTouchHelper(callback);
        catalogueAdapter.setDragStartListener(new CatalogueAdapter.OnStartDragListener() {
            @Override
            public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
                mItemTouchHelper.startDrag(viewHolder);
            }
        });
        catalogueRecycler.setAdapter(catalogueAdapter);
        mItemTouchHelper.attachToRecyclerView(catalogueRecycler);
        catalogueAdapter.setOnItemClickListener(new CatalogueAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                final String catalogue = catalogueAdapter.getItem(position).getCatalogue();
                switch (catalogue) {
                    case "test":
                        Intent testIntent = new Intent(MainFormActivity.this, TestActivity.class);
                        startActivity(testIntent);
                        break;
                    default:
                        refreshLayout.setRefreshing(true);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                listDatas = dbListInfoManager.getDatas(catalogue);
                                Message msg = new Message();
                                Bundle data = new Bundle();
                                data.putInt(MSG_SEARCH_DATA, MSG_FINISH_SORTING_DATA);
                                msg.setData(data);
                                handler.sendMessage(msg);

                            }
                        }).start();
                        toolbar.setTitle(catalogueAdapter.getItem(position).getCatalogue());
                        currentCatalogue = catalogue;
                        currentCatalogueDetail = catalogueAdapter.getItem(position).getCatalogueDescription();
                        mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                String catlogue = catalogueAdapter.getItem(position).getCatalogue();
//                Log.v(TAG, "ItemLongClick:  catalogue=" + catlogue);
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
//                    Log.v(TAG, "返回后 current pos=" + pos + " 数据为：  order=" + listData.getOrderID() + "  catalogue=" + listData.getCatalogue());
                    listClipInfoAdapter.editItem(pos, listData);
                    dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), listData.getRemarks(), listData.getContent(), listData.getCreateDate());
                    return;
                }
                if (resultCode == ActivityEditInfo.RESULT_ADD_NEW) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = 0;
//                    Log.v("TEM", pos + listData.getContent());
                    long result = dbListInfoManager.insertData(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

                    if (result == -1)
                        Toast.makeText(MainFormActivity.this, "存储该行数据出错", Toast.LENGTH_SHORT).show();
                    else
                        listClipInfoAdapter.addItem(listData);
                    return;
                }
                if (resultCode == ActivityBangumi.RESULT_BANGUMI_ACTIVITY) {

                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = data.getIntExtra(LIST_DATA_POS, 0);
                    Log.d(TAG, "onActivityResult: content=" + JSON.toJSONString(listData.getContent()));

//                    Log.v(TAG, "返回后 current pos=" + pos + " 数据为：  order=" + listData.getOrderID() + "  catalogue=" + listData.getCatalogue());
                    listClipInfoAdapter.editItem(pos, listData);
                    return;
                }
                break;

        }

    }

    public static boolean isDailyTask = false;

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {

        switch (item.getItemId()) {
            case R.id.add_info:
                int orderid = dbListInfoManager.getDataCount();
//                Log.v(TAG, "新建 orderid=" + orderid);
                final Intent intent = new Intent(MainFormActivity.this, ActivityEditInfo.class);
                intent.putExtra(LIST_DATA, new ListData("", "", orderid, currentCatalogue));
                intent.putExtra(LIST_DATA_POS, -1);
                if (currentCatalogue.equals("待办事项")) {
                    //创建弹出式菜单对象（最低版本11）
                    PopupMenu popup = new PopupMenu(this, popPositionTagView);//第二个参数是绑定的那个view

                    //获取菜单填充器
                    MenuInflater inflater = popup.getMenuInflater();
                    //填充菜单
                    inflater.inflate(R.menu.todo_menu, popup.getMenu());
                    //绑定菜单项的点击事件
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem menuItem) {
                            switch (menuItem.getItemId()) {
                                case R.id.todo_menu_daily_task:
                                    isDailyTask = true;
                                    break;
                                case R.id.todo_menu_todo_task:
                                    isDailyTask = false;
                                    break;
                                default:
                                    break;
                            }
                            startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                            return false;
                        }
                    });
                    //显示(这一行代码不要忘记了)
                    popup.show();
                } else
                    startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);

                break;
            case R.id.edit_info:
                IsEdite = !IsEdite;
                if (IsEdite)
                    item.setIcon(R.mipmap.ic_sort);
                else
                    item.setIcon(R.drawable.ic_edit_white_24dp);
                break;
            case R.id.about_info:
                IsDelete = !IsDelete;
                new AlertDialog.Builder(MainFormActivity.this)
                        .setTitle("目录说明").setMessage(currentCatalogueDetail)
                        .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showCatalogueAlter();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .create().show();

            case R.id.menu_main_search:
                break;
            case R.id.add_catalogue:
//                showCatalogueAlter();
                showPopWindow("");
                break;
            case R.id.settings:
                Intent intent1 = new Intent(MainFormActivity.this, SettingsActivity.class);
                startActivity(intent1);
                break;

        }

        return super.onOptionsItemSelected(item);
    }


    private void showCatalogueAlter() {
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_modify_catalogue, null);
        final EditText edCatalogueName = (EditText) view.findViewById(R.id.dialogue_catalogue_name);
        final EditText edCatalogueDescription = ((EditText) view.findViewById(R.id.dialogue_catalogue_description));

        AlertDialog alertDialog = new AlertDialog.Builder(MainFormActivity.this)
                .setTitle("修改目录")
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String description = edCatalogueDescription.getText().toString();
                        String name = edCatalogueName.getText().toString();
                        catalogueAdapter.set(catalogueAdapter.indexOf(currentCatalogue), new CatalogueInfos(name, description));
                        currentCatalogueDetail = description;
                        setCatalogueChanged(currentCatalogue, name);
                        currentCatalogue = name;
                        toolbar.setTitle(currentCatalogue);
                    }
                })
                .setNegativeButton("取消", null)
                .create();


        edCatalogueName.setText(currentCatalogue);
        alertDialog.show();
        if (currentCatalogueDetail.equals("")) {
            edCatalogueDescription.setHint("目录描述");
//            Log.d(TAG, "showCatalogueAlter: catalogueDescriptionDetail=empty");
        } else
            edCatalogueDescription.setText(currentCatalogueDetail);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.add_catalogue).setVisible(isSettingShow);
        menu.findItem(R.id.settings).setVisible(isSettingShow);
        menu.findItem(R.id.add_info).setVisible(!isSettingShow);
//        if (currentCatalogue.equals(""))
        menu.findItem(R.id.about_info).setVisible(!isSettingShow);
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
            SearchView.SearchAutoComplete textView = (SearchView.SearchAutoComplete) searchView
                    .findViewById(
                            android.support.v7.appcompat.R.id.search_src_text
                    );
            textView.setTextColor(Color.WHITE);
            try {
                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
                mCursorDrawableRes.setAccessible(true);
                mCursorDrawableRes.set(textView, R.drawable.cursor_color);
            } catch (Exception e) {

            }
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
                if (catalogueAdapter.contains(catalogueNewName)) {
                    Toast.makeText(MainFormActivity.this, catalogueNewName + "已经存在", Toast.LENGTH_SHORT).show();
                    return;
                }
                //新建目录
                if (catalogueName.equals("")) {
                    catalogueAdapter.addItem(new CatalogueInfos(catalogueNewName, ""));

                } else {
                    int index = 0;
                    index = catalogueAdapter.indexOf(catalogueName);
                    if (index == -1)
                        return;
//                    Log.v(TAG, "change catalogue: index" + index + "  catalogue=" + catalogueName);
                    setCatalogueChanged(catalogueName, catalogueNewName);
                    catalogueAdapter.set(index, catalogueNewName);
                    catalogueAdapter.notifyItemChanged(index);
//                    linearLayoutManager.scrollToPositionWithOffset(index,0);
                    currentCatalogue = catalogueNewName;
                    toolbar.setTitle(currentCatalogue);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FileUtils.saveCatalogue(getApplicationContext().getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            FileUtils.saveCatalogue(getApplicationContext().getExternalFilesDir(null).getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
        }
    }

    private void setCatalogueChanged(String oldCatalogue, String newCatalogue) {
        dbListInfoManager.changeCatalogue(oldCatalogue, newCatalogue);
    }

    /*<=======================================全局基础权限申请=================================================>*/

    /**
     * 申请全局都需要的权限,如读写权限,这些权限是进入app就需要的,拒绝则警告用户程序可能会崩溃
     */
    private void initGlobalPer() {
        MainFormActivityPermissionsDispatcher.sucessWithCheck(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MainFormActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);//将回调交给代理类处理
    }

    @NeedsPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void sucess() {//权限申请成功

    }

    @OnShowRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void showRationaleForCamera(PermissionRequest request) {
        showRationaleDialog("为了正常使用会进行缓存及文件存储操作,需要您授予相关的存储权限!\n请您放心,该权限为正常使用权限,不会涉及到您的隐私!\n稍后请点击弹出框的允许按钮", request);
    }

    @OnPermissionDenied(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onCameraDenied() {//被拒绝
        Toast.makeText(MainFormActivity.this, "您拒绝了权限，可能会导致该应用内部发生错误,请尽快授权", Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    void onCameraNeverAskAgain() {//被拒绝并且勾选了不再提醒
        if (!PreferenceManager.getDefaultSharedPreferences(MainFormActivity.this).getBoolean(DONT_ASK_AGAIN, false))
            AskForPermission();
    }

    /**
     * 再用户拒绝过一次之后,告知用户具体需要权限的原因
     *
     * @param messageResId
     * @param request
     */
    private void showRationaleDialog(String messageResId, final PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(@NonNull DialogInterface dialog, int which) {
                        request.proceed();//请求权限
                    }
                })
                .setTitle("请求权限")
                .setCancelable(false)
                .setMessage(messageResId)
                .show();
    }

    /**
     * 被拒绝并且不再提醒,提示用户去设置界面重新打开权限
     */
    private void AskForPermission() {
        new AlertDialog.Builder(this)
                .setTitle("缺少基础存储权限")
                .setMessage("当前应用缺少存储权限,请去设置界面授权.\n授权之后按两次返回键可回到该应用哦")
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Toast.makeText(MainFormActivity.this, "您拒绝了权限，可能会导致该应用无法使用,请尽快授权", Toast.LENGTH_SHORT).show();

                    }
                })
                .setNeutralButton("不在提醒", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        PreferenceManager.getDefaultSharedPreferences(MainFormActivity.this).edit().putBoolean(DONT_ASK_AGAIN, true).commit();
                        Toast.makeText(MainFormActivity.this, "将不再提醒请求基础权限,建议尽快授权", Toast.LENGTH_SHORT).show();

                    }
                }).setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        }).create().show();
    }

    @Override
    public void onDeleteBtnCilck(View view, int position) {

        // 将数据集中的数据移除
        final int pos = position;
        ListData listDatatemp = listClipInfoAdapter.getItemData(pos);
        final ListData listData = new ListData(listDatatemp.getRemarks(), listDatatemp.getContent(), listDatatemp.getCreateDate(), listDatatemp.getOrderID(), listDatatemp.getCatalogue());

        listClipInfoAdapter.deleteItem(pos);
        dbListInfoManager.deleteDataByOrderID(listData.getOrderID());
        Snackbar.make(recyclerView, "确定删除？", Snackbar.LENGTH_LONG).setAction("撤销", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listClipInfoAdapter.addItem(listData, pos);

                dbListInfoManager.cancelDelete(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

            }
        }).setDuration(Snackbar.LENGTH_LONG).show();
    }
/*<========================================================================================>*/
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                List<ListData> listDatas=dbListInfoManager.getDatas("日记");
//                for (int i=0;i<listDatas.size();i++)
//                {
//                    ListData listData = listClipInfoAdapter.getItemData(i);
//                    listData.setRemarks("diary");
//                    String content=listData.getContent();
//                    String morningDiary="";
//                    String afternoonDiary="";
//                    String eveningDiary="";
//                    try {
//                        morningDiary=content.split("@#@")[0];
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                        morningDiary="";
//                    }
//                    try {
//                        afternoonDiary=content.split("@#@")[1];
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                        afternoonDiary="";
//                    }
//                    try {
//                        eveningDiary=content.split("@#@")[2];
//                    }
//                    catch (Exception e)
//                    {
//                        e.printStackTrace();
//                        eveningDiary="";
//                    }
//                    listData.setContent(JSON.toJSONString(new DiaryData(morningDiary, afternoonDiary, eveningDiary)));
//
//                    dbListInfoManager.updateDataByOrderId(listData.getOrderID(), FragmentCalendar.CALENDAR_CATALOGUE_NAME,listData.getRemarks(),listData.getContent(),listData.getCreateDate());
//                }
//            }
//        }).start();
//        new Thread(new Runnable() {
//        @Override
//        public void run() {
//            List<ListData> listDatas=dbListInfoManager.getDatas("luser");
//            for (int i=0;i<listDatas.size();i++)
//            {
//                ListData listData = listDatas.get(i);
//                listData.setRemarks("luser");
//                dbListInfoManager.updateDataByOrderId(listData.getOrderID(), FragmentCalendar.CALENDAR_CATALOGUE_NAME,listData.getRemarks(),listData.getContent(),listData.getCreateDate());
//            }
//        }
//    }).start();
//        new Thread(new Runnable() {
//        @Override
//        public void run() {
//            List<ListData> listDatas=dbListInfoManager.getDatas("体重");
//            for (int i=0;i<listDatas.size();i++)
//            {
//                ListData listData = listClipInfoAdapter.getItemData(i);
//                listData.setRemarks("weight");
//                dbListInfoManager.updateDataByOrderId(listData.getOrderID(), FragmentCalendar.CALENDAR_CATALOGUE_NAME,listData.getRemarks(),listData.getContent(),listData.getCreateDate());
//            }
//        }
//    }).start();


}
