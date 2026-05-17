package com.example.yanghang.clipboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
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
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.ItemTouchHelper;

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
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.ConnectToPC.PcLinkManager;
import com.example.yanghang.clipboard.FileUtils.FileUtils;
import com.example.yanghang.clipboard.Fragment.JsonData.ProjectData;
import com.example.yanghang.clipboard.Fragment.JsonData.ResearchTopicData;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
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
    private static final int REQUEST_PC_LINK_FILE = 20310;
    public static final String LIST_DATA = "listdataToEdite";
    public static final String LIST_DATA_POS = "listdataToEditePos";
    private static final int MSG_FINISH_SORTING_DATA = 123;
    private static final String MSG_SEARCH_DATA = "finish_sorting_listdata";
    private static final int MSG_FINISH_CHECK_TODO_DATA = 456;
    private static final int MSG_FINISH_CHECK_DAILY_DATA = 5623;
    public static boolean IsEdite = false;

    public static String TAG = "nihao";
    public static boolean IsDelete = false;
    public static final String PREF_CATALOGUE_CHANGED = "catalogue_changed";

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
    private FloatingActionButton pcFloatingButton;
    private boolean isPcLinkMode;
    private PcLinkManager pcLinkManager;
    private ClipboardManager pcClipboardManager;
    private ClipboardManager.OnPrimaryClipChangedListener pcClipboardListener;
    private boolean isApplyingClipboardFromPc;
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
                    new androidx.appcompat.app.AlertDialog.Builder(MainFormActivity.this).setView(dialogDailyTaskView)
                            .setTitle("日常任务")
                            .setPositiveButton("更新", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    TaskShowToDoList.updateDailyMissionList(dbListInfoManager, TaskShowToDoList.getTodayString(), dailyTaskAdapter.getLists());
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

    @Override
    protected void onResume() {
        super.onResume();
        if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(PREF_CATALOGUE_CHANGED, false)) {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(PREF_CATALOGUE_CHANGED, false).apply();
            refreshCatalogueDrawer();
        }
    }

    private void updatePcFloatingButton(boolean pcLinkMode) {
        isPcLinkMode = pcLinkMode;
        if (pcFloatingButton == null) {
            return;
        }
        if (isPcLinkMode) {
            pcFloatingButton.setImageResource(R.drawable.ic_pc_link_white_24dp);
            pcFloatingButton.setRotation(0);
            return;
        }
        pcFloatingButton.setImageResource(android.R.drawable.ic_menu_send);
        pcFloatingButton.setRotation(-90);
    }

    private void initialPcLink() {
        pcLinkManager = PcLinkManager.getInstance(getApplicationContext());
        pcLinkManager.setListener(new PcLinkManager.Listener() {
            @Override
            public void onStateChanged(final int state, final String message, final String pcName) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updatePcFloatingButton(state != PcLinkManager.STATE_DISCONNECTED);
                        if (message != null && !message.trim().equals("")) {
                            Toast.makeText(MainFormActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onClipboardFromPc(final String text) {
                if (text == null) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isApplyingClipboardFromPc = true;
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("PC", text));
                        isApplyingClipboardFromPc = false;
                        Toast.makeText(MainFormActivity.this, "电脑剪贴板已同步到手机", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onMessageFromPc(final String text) {
                if (text == null) {
                    return;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText("PC", text));
                        Toast.makeText(MainFormActivity.this, "电脑消息已复制到手机剪贴板", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onRecordUpdateFromPc(final ListData data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(true);
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final boolean updated = dbListInfoManager.updateDataByOrderId(data.getOrderID(),
                                data.getCatalogue(), data.getRemarks(), data.getContent(), data.getCreateDate());
                        if (pcLinkManager != null) {
                            pcLinkManager.sendRecordUpdateAck(data.getOrderID(), updated);
                        }
                        final List<ListData> nextDatas = dbListInfoManager.getDatas(currentCatalogue);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.setRefreshing(false);
                                listDatas = nextDatas;
                                listClipInfoAdapter.setDatas(listDatas);
                                Toast.makeText(MainFormActivity.this,
                                        updated ? "电脑修改已更新到手机记录：" + data.getOrderID() : "电脑回传失败，未找到记录：" + data.getOrderID(),
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onRecordInsertFromPc(final ListData data) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefreshing(true);
                    }
                });
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        final int orderId = dbListInfoManager.getDataCount();
                        data.setOrderID(orderId);
                        final boolean inserted = dbListInfoManager.insertData(data) != -1;
                        if (pcLinkManager != null) {
                            pcLinkManager.sendRecordInsertAck(orderId, inserted);
                        }
                        final List<ListData> nextDatas = dbListInfoManager.getDatas(currentCatalogue);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                refreshLayout.setRefreshing(false);
                                listDatas = nextDatas;
                                listClipInfoAdapter.setDatas(listDatas);
                                Toast.makeText(MainFormActivity.this,
                                        inserted ? "电脑记录已新增到手机：" + orderId : "电脑记录新增失败",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onFileFromPc(final String filePath) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainFormActivity.this, "电脑文件已保存：" + filePath, Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
        setupPcClipboardSync();
    }

    private void setupPcClipboardSync() {
        pcClipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        pcClipboardListener = new ClipboardManager.OnPrimaryClipChangedListener() {
            @Override
            public void onPrimaryClipChanged() {
                if (isApplyingClipboardFromPc || pcLinkManager == null || !pcLinkManager.isConnected()) {
                    return;
                }
                String text = getClipboardText();
                if (text == null || text.trim().equals("")) {
                    return;
                }
                pcLinkManager.sendClipboard(text);
            }
        };
        pcClipboardManager.addPrimaryClipChangedListener(pcClipboardListener);
    }

    private String getClipboardText() {
        try {
            ClipData clipData = pcClipboardManager.getPrimaryClip();
            if (clipData == null || clipData.getItemCount() == 0) {
                return "";
            }
            CharSequence text = clipData.getItemAt(0).coerceToText(this);
            return text == null ? "" : text.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private void openPcLinkFilePicker() {
        if (pcLinkManager == null || !pcLinkManager.isConnected()) {
            Toast.makeText(MainFormActivity.this, "请先连接电脑", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, REQUEST_PC_LINK_FILE);
    }

    private void showPcLinkMenu(View anchor) {
        PopupMenu popupMenu = new PopupMenu(MainFormActivity.this, anchor);
        popupMenu.getMenu().add("发送文件到电脑");
        popupMenu.getMenu().add("断开电脑连接");
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                String title = item.getTitle().toString();
                if ("发送文件到电脑".equals(title)) {
                    openPcLinkFilePicker();
                    return true;
                }
                if ("断开电脑连接".equals(title) && pcLinkManager != null) {
                    pcLinkManager.disconnect();
                    return true;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void InitialView() {
        dbListInfoManager = new DBListInfoManager(MainFormActivity.this.getApplicationContext());
        initialPcLink();
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
                saveCatalogueToInternal();
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
        pcFloatingButton = (FloatingActionButton) findViewById(R.id.fab);
        updatePcFloatingButton(false);
        pcFloatingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pcLinkManager != null && pcLinkManager.isConnected()) {
                    showPcLinkMenu(view);
                    return;
                }
                if (pcLinkManager != null && pcLinkManager.isDiscovering()) {
                    pcLinkManager.disconnect();
                    return;
                }
                Intent intent = new Intent(MainFormActivity.this, ActivityConnect.class);
                startActivity(intent);
            }
        });
        pcFloatingButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (pcLinkManager != null) {
                    if (pcLinkManager.isConnected()) {
                        openPcLinkFilePicker();
                    } else if (pcLinkManager.isDiscovering()) {
                        Toast.makeText(MainFormActivity.this, "正在发现电脑", Toast.LENGTH_SHORT).show();
                    } else {
                        pcLinkManager.discoverAndConnect();
                    }
                }
                return true;
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
                switch (data.getCatalogue())
                {
                    case "番剧":
                        intent = new Intent(MainFormActivity.this, ActivityBangumi.class);
                        break;
                    case "记账":
                        intent = new Intent(MainFormActivity.this, ActivityAccountBook.class);
                        break;
                    case "日子":
                        intent = new Intent(MainFormActivity.this, ActivitySpecialDays.class);
                        break;
                    default:
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
                if (mDailyList == null || mDailyList.isEmpty())
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
        catalogues = loadCatalogueList();

        // 设置布局，否则无法正常使用
        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        catalogueRecycler.setLayoutManager(linearLayoutManager);

        catalogueAdapter = new CatalogueAdapter(catalogues, MainFormActivity.this);
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

    private void refreshCatalogueDrawer() {
        if (catalogueAdapter == null) {
            return;
        }
        catalogues = loadCatalogueList();
        catalogueAdapter.setDatas(catalogues);
        FileUtils.saveCatalogue(getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
    }

    private List<CatalogueInfos> loadCatalogueList() {
        List<CatalogueInfos> catalogueList = FileUtils.loadCatalogue(getFilesDir().getAbsolutePath());
        if (catalogueList==null||catalogueList.size()==0)
        {
            String filePath=PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("dataFilePathPreference",getFilesDir().getAbsolutePath());
            catalogueList = FileUtils.loadCatalogue(filePath);
        }
        if (catalogueList == null) {
            catalogueList = new ArrayList<CatalogueInfos>();
        }
        if (!containsCatalogue(catalogueList, "default")) {
            catalogueList.add(0, new CatalogueInfos("default", ""));
        }
        if (!containsCatalogue(catalogueList, ResearchTopicData.CATALOGUE_NAME)) {
            catalogueList.add(new CatalogueInfos(ResearchTopicData.CATALOGUE_NAME, "课题路线图、研究资料、问题和任务的结构化整理"));
        }
        if (!containsCatalogue(catalogueList, ProjectData.CATALOGUE_NAME)) {
            catalogueList.add(new CatalogueInfos(ProjectData.CATALOGUE_NAME, "项目阶段、时间计划、甘特图和交付物索引"));
        }
        return catalogueList;
    }

    private boolean containsCatalogue(List<CatalogueInfos> catalogueList, String catalogueName) {
        if (catalogueList == null) {
            return false;
        }
        for (int i = 0; i < catalogueList.size(); i++) {
            if (catalogueList.get(i).getCatalogue().equals(catalogueName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PC_LINK_FILE:
                if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                    final Uri fileUri = data.getData();
                    Toast.makeText(MainFormActivity.this, "正在发送文件到电脑", Toast.LENGTH_SHORT).show();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final boolean success = pcLinkManager != null && pcLinkManager.sendFile(fileUri);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainFormActivity.this,
                                            success ? "文件已发送到电脑" : "文件发送失败，请确认电脑连接正常",
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }).start();
                }
                return;
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
                    {
                        listClipInfoAdapter.addItem(listData);
                        recyclerView.scrollToPosition(0);
                    }

                    return;
                }
                if (resultCode == ActivityBangumi.RESULT_BANGUMI_ACTIVITY) {

                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    int pos = data.getIntExtra(LIST_DATA_POS, 0);
//                    Log.d(TAG, "onActivityResult: content=" + JSON.toJSONString(listData.getContent()));

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
                ListData newData = new ListData("", "", orderid, currentCatalogue);
//                if (currentCatalogue.equals(ProjectData.CATALOGUE_NAME)) {
//                    ProjectData sample = ProjectData.createSample();
//                    newData.setRemarks(sample.getTitle());
//                    newData.setContent(JSON.toJSONString(sample));
//                }
                intent.putExtra(LIST_DATA, newData);
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
                            R.id.search_src_text
                    );
            textView.setTextColor(Color.WHITE);
            try {
                @SuppressLint("SoonBlockedPrivateApi") Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
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
        if (pcClipboardManager != null && pcClipboardListener != null) {
            pcClipboardManager.removePrimaryClipChangedListener(pcClipboardListener);
        }
        saveCatalogueToInternal();
    }

    private void saveCatalogueToInternal() {
        if (catalogueAdapter == null) {
            return;
        }
        FileUtils.saveCatalogue(getFilesDir().getAbsolutePath(), catalogueAdapter.getDatas(), false, "");
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
        if (softDeleteTodoIfNeeded(pos, listData)) {
            return;
        }

        listClipInfoAdapter.deleteItem(pos);
        dbListInfoManager.deleteDataByOrderID(listData.getOrderID());
        Snackbar.make(recyclerView, "确定删除？", Snackbar.LENGTH_LONG).setAction("撤销", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listClipInfoAdapter.addItem(listData, pos);

                dbListInfoManager.cancelDelete(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

            }
        }).setDuration(BaseTransientBottomBar.LENGTH_LONG).show();
    }

    @Override
    public void onSendPcBtnClick(View view, int position) {
        if (listClipInfoAdapter != null) {
            listClipInfoAdapter.closeMenu();
        }
        if (pcLinkManager == null || !pcLinkManager.isConnected()) {
            Toast.makeText(MainFormActivity.this, "请先长按右下角按钮连接电脑", Toast.LENGTH_SHORT).show();
            return;
        }
        final ListData data = listClipInfoAdapter.getItemData(position);
        new Thread(new Runnable() {
            @Override
            public void run() {
                final boolean success = pcLinkManager.sendRecord(data);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (success) {
                            Toast.makeText(MainFormActivity.this, "已发送到电脑：" + data.getOrderID(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainFormActivity.this, "发送失败，请重新连接电脑", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }).start();
    }

    private boolean softDeleteTodoIfNeeded(final int pos, final ListData oldData) {
        if (!oldData.getCatalogue().equals("待办事项")) {
            return false;
        }
        ToDoData toDoData = null;
        try {
            toDoData = JSON.parseObject(oldData.getContent(), ToDoData.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (toDoData == null) {
            return false;
        }
        ToDoData boardData = ToDoData.normalizeBoard(toDoData);
        if (boardData.isDeleted()) {
            return false;
        }
        boardData.setStatus(ToDoData.STATUS_TRASH);
        boardData.setDeletedAt(ListData.GetDate());
        final ListData trashData = new ListData(oldData.getRemarks(), JSON.toJSONString(boardData), oldData.getCreateDate(), oldData.getOrderID(), oldData.getCatalogue());
        listClipInfoAdapter.editItem(pos, trashData);
        dbListInfoManager.updateDataByOrderId(trashData.getOrderID(), trashData.getCatalogue(), trashData.getRemarks(), trashData.getContent(), trashData.getCreateDate());
        Snackbar.make(recyclerView, "已移入回收站", Snackbar.LENGTH_LONG).setAction("撤销", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listClipInfoAdapter.editItem(pos, oldData);
                dbListInfoManager.updateDataByOrderId(oldData.getOrderID(), oldData.getCatalogue(), oldData.getRemarks(), oldData.getContent(), oldData.getCreateDate());
            }
        }).setDuration(BaseTransientBottomBar.LENGTH_LONG).show();
        return true;
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
