package com.example.yanghang.clipboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.Fragment.AccountCatalogueDialogPieChartFragment;
import com.example.yanghang.clipboard.Fragment.AccountCatalogueDialogSelectCategoriesFragment;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountDataAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.Log.CrashHandler;
import com.example.yanghang.clipboard.OthersView.AutoFixText.AutofitTextView;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.SwipeBackLayout;
import com.example.yanghang.clipboard.OthersView.swipebacklayout.lib.app.SwipeBackActivity;
import com.github.mikephil.charting.data.PieEntry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.example.yanghang.clipboard.ActivityBangumi.RESULT_BANGUMI_ACTIVITY;

public class ActivityAccountBook extends SwipeBackActivity {

    private static final String TAG = "ActivityAccountBook";
    RecyclerView accountRecycleView;
    private SwipeBackLayout mSwipeBackLayout;
    AccountDataAdapter accountDataAdapter;
    TextView accountMonth;
    TextView accountYear;
    AutofitTextView accountMoney;
    TextView accountIncome;
    TextView accountExpenditure;
    Toolbar toolbar;
    TextView catalogueNameEdit;
    ImageView accountMoneyImage;
    SwipeRefreshLayout refreshLayout;
    String selectCatalogueName = "";
    private ListData listData;
    private int posInListData;
    private List<AccountData> allAccountData = new ArrayList<>();
    private String currentSearchQuery = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        final Window window = getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_account_book);
        toolbar = (Toolbar) findViewById(R.id.accountToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setStatusBarColor(getResources().getColor(R.color.bgColor));
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddAccountDialog();
            }
        });
        initialView();
        initialData();
    }


    private void initialData() {
        try {
            Bundle bundle = getIntent().getExtras();
            if (bundle == null) {
                throw new IllegalStateException("ActivityAccountBook extras is null");
            }
            listData = (ListData) bundle.get(MainFormActivity.LIST_DATA);
            posInListData = bundle.getInt(MainFormActivity.LIST_DATA_POS, -1);
            if (listData == null) {
                throw new IllegalStateException("ActivityAccountBook LIST_DATA is null");
            }
            ListData latestData = new DBListInfoManager(ActivityAccountBook.this).getDataByOrderId(listData.getOrderID());
            if (latestData != null) {
                listData = latestData;
            }
            catalogueNameEdit.setText(listData.getRemarks());
            new AnalyseContentTask().execute(getSafeAccountContent());
        } catch (Exception e) {
            Log.e(TAG, "记账初始化失败", e);
            CrashHandler.appendLog(ActivityAccountBook.this, "记账初始化失败", "", e);
            Toast.makeText(ActivityAccountBook.this, "记账数据读取失败，已返回主界面", Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_donut:
                AccountCatalogueDialogPieChartFragment fragment = new AccountCatalogueDialogPieChartFragment();
                //通过bundle对象向Fragment传值
                Bundle bundle = new Bundle();
                bundle.putString("TEXT_IN_CENTER", listData.getRemarks());
                bundle.putDouble("EXPENDITURE",mExpenditure);
                fragment.setArguments(bundle);
                fragment.show(getSupportFragmentManager(),entries,accountDataAdapter);

                break;
            case R.id.action_search:
                break;
        }
        return true;
    }
    SearchView searchView;
    SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(final String query) {
//            Log.v(TAG, "开始查询");
            applySearchQuery(query);
            return true;
        }        @Override
        public boolean onQueryTextChange(String newText) {
            applySearchQuery(newText);
            return true;
        }


    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setIcon(R.drawable.ic_search_green);
        searchView = (SearchView) searchItem.getActionView();
        if (searchView != null) {
//            Toast.makeText(MainFormActivity.this, "null searchview", Toast.LENGTH_SHORT).show();
//            searchView.setBackground(getDrawable(R.drawable.ic_search_green));
            searchView.setIconifiedByDefault(true);
            searchView.setQueryHint("搜索类型/备注");
            searchView.setSubmitButtonEnabled(true);
            searchView.setOnQueryTextListener(onQueryTextListener);
            searchView.setOnCloseListener(new SearchView.OnCloseListener() {
                @Override
                public boolean onClose() {
                    applySearchQuery("");
                    return false;
                }
            });
            SearchView.SearchAutoComplete textView = (SearchView.SearchAutoComplete) searchView
                    .findViewById(
                           R.id.search_src_text
                    );
            textView.setTextColor(Color.GREEN);
            textView.setHintTextColor(Color.GRAY);

//            try {
//                Field mCursorDrawableRes = TextView.class.getDeclaredField("mCursorDrawableRes");
//                mCursorDrawableRes.setAccessible(true);
//                mCursorDrawableRes.set(textView, R.drawable.cursor_color);
//            } catch (Exception e) {
//
//            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    private void initialView() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_green_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setLatestResultAndFinish();
                finish();
            }
        });
        mSwipeBackLayout = getSwipeBackLayout();
        //设置可以滑动的区域，推荐用屏幕像素的一半来指定
        mSwipeBackLayout.setEdgeSize(100);
        //设定滑动关闭的方向，SwipeBackLayout.EDGE_ALL表示向下、左、右滑动均可。EDGE_LEFT，EDGE_RIGHT，EDGE_BOTTOM
        mSwipeBackLayout.setEdgeTrackingEnabled(SwipeBackLayout.EDGE_LEFT);


        catalogueNameEdit = (TextView) findViewById(R.id.bangumi_edit_catalogue);
        catalogueNameEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(ActivityAccountBook.this).inflate(R.layout.dialog_bangumi_alter_name, null);
                final EditText editText = view1.findViewById(R.id.bangumi_alter_name);
                editText.setText(catalogueNameEdit.getText());
                AlertDialog alertDialog = new AlertDialog.Builder(ActivityAccountBook.this).setView(view1)
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
        accountRecycleView = (RecyclerView) findViewById(R.id.accountRecycleView);
        List<AccountData> list = new ArrayList<>();
        accountDataAdapter = new AccountDataAdapter(list, ActivityAccountBook.this);
        accountDataAdapter.setItemClickListener(new CatalogueAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                showAlterDialog(position);
            }

            @Override
            public boolean OnItemLongClick(View v, int position) {
                showDeleteDialog(position);
                return true;
            }
        });
        accountRecycleView.setLayoutManager(new LinearLayoutManager(ActivityAccountBook.this, LinearLayoutManager.VERTICAL, false));
        accountRecycleView.setAdapter(accountDataAdapter);

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh);
        refreshLayout.setColorSchemeResources(android.R.color.holo_blue_light, android.R.color.holo_red_light, android.R.color.holo_orange_light, android.R.color.holo_green_light);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        reloadLatestAccountData();
                    }
                }, 200);
            }
        });

        accountMonth = (TextView) findViewById(R.id.accountMonth);
        accountYear = (TextView) findViewById(R.id.accountYear);
        accountIncome = (TextView) findViewById(R.id.accountMoneyIncome);
        accountExpenditure = (TextView) findViewById(R.id.accountMoneyExpenditure);
        accountMoney = (AutofitTextView) findViewById(R.id.accountMoney);
        accountMoneyImage = (ImageView) findViewById(R.id.accountMoneyImage);


    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

        }
        return super.onKeyDown(keyCode, event);
    }



    private void showAlterDialog(final int position)
    {
        final AccountData oldAccountData = accountDataAdapter.getItem(position);
        View view = LayoutInflater.from(ActivityAccountBook.this).inflate(R.layout.dialog_add_new_account, null);
        Button saveAccount = view.findViewById(R.id.accountDialogSave);
        final EditText accountMoneyEdit = view.findViewById(R.id.accountDialogMoney);
        final EditText accountContentEdit = view.findViewById(R.id.accountDialogContent);
        final TextView accountDialogCatalogueName = view.findViewById(R.id.accountDialogCatalogueName);

        String type=accountDataAdapter.getItem(position).getType().equals("")?"吃饭":accountDataAdapter.getItem(position).getType();
        selectCatalogueName=type;
        accountDialogCatalogueName.setText(type);
        accountMoneyEdit.setText(Double.toString(Math.abs(accountDataAdapter.getItem(position).getMoney())));
        accountContentEdit.setText(accountDataAdapter.getItem(position).getContent());
        accountDialogCatalogueName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AccountCatalogueDialogSelectCategoriesFragment fragment = new AccountCatalogueDialogSelectCategoriesFragment();
                fragment.setOnSaveCatalogNameClick(new AccountCatalogueDialogSelectCategoriesFragment.Click() {
                    @Override
                    public void click(String name) {
                        accountDialogCatalogueName.setText(name);
                        selectCatalogueName=name;
                        fragment.dismiss();
                    }
                });
                fragment.show(getSupportFragmentManager());
            }
        });
        RadioGroup radioGroup = (RadioGroup)view. findViewById(R.id.incomeOrExpenditure);
        if (accountDataAdapter.getItem(position).getMoney()<0)
        {
            isIncome=false;
            radioGroup.check(R.id.rb1);
        }else {
            isIncome=true;
            radioGroup.check(R.id.rb2);
        }
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                String result = radioButton.getText().toString();
                if (result.equals("支出"))
                {
                    isIncome=false;
                }else
                {
                    isIncome=true;
                }

            }
        });

        saveAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                double money=0;
                try {
                    money=Double.parseDouble(accountMoneyEdit.getText().toString());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (!isIncome)
                    money*=-1;
                String content=accountContentEdit.getText().toString();

                AccountData accountData = new AccountData(oldAccountData.getAccountTime(), "", money, "#cccccc", selectCatalogueName, content);
                replaceAccountData(oldAccountData, accountData);
                saveToDataBase();
                applyCurrentFilter();
                if (alertDialog!=null)
                {
                    alertDialog.dismiss();
                }

            }
        });

        alertDialog = new AlertDialog.Builder(ActivityAccountBook.this).setView(view)
                .setTitle("").show();

    }
    boolean isIncome=false;
    AlertDialog alertDialog=null;
    private void showAddAccountDialog() {

        View view = LayoutInflater.from(ActivityAccountBook.this).inflate(R.layout.dialog_add_new_account, null);
        Button saveAccount = view.findViewById(R.id.accountDialogSave);
        final EditText accountMoneyEdit = view.findViewById(R.id.accountDialogMoney);
        final EditText accountContentEdit = view.findViewById(R.id.accountDialogContent);
        final TextView accountDialogCatalogueName = view.findViewById(R.id.accountDialogCatalogueName);
        selectCatalogueName="吃饭";
        isIncome=false;
        accountDialogCatalogueName.setText("吃饭");
        accountDialogCatalogueName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AccountCatalogueDialogSelectCategoriesFragment fragment = new AccountCatalogueDialogSelectCategoriesFragment();
                fragment.setOnSaveCatalogNameClick(new AccountCatalogueDialogSelectCategoriesFragment.Click() {
                    @Override
                    public void click(String name) {
                        accountDialogCatalogueName.setText(name);
                        selectCatalogueName=name;
                        fragment.dismiss();
                    }
                });
                fragment.show(getSupportFragmentManager());
            }
        });
        RadioGroup radioGroup = (RadioGroup)view. findViewById(R.id.incomeOrExpenditure);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton radioButton = (RadioButton) group.findViewById(checkedId);
                String result = radioButton.getText().toString();
                if (result.equals("支出"))
                {
                    isIncome=false;
                }else
                {
                    isIncome=true;
                }

            }
        });
        saveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double money=0;
                try {
                    money=Double.parseDouble(accountMoneyEdit.getText().toString());
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                if (!isIncome)
                    money*=-1;
                String content=accountContentEdit.getText().toString();

                AccountData accountData = new AccountData(ListData.GetDate(), "", money, "#cccccc", selectCatalogueName,content );
                allAccountData.add(0, accountData);
                accountRecycleView.scrollToPosition(0);
                saveToDataBase();
                applyCurrentFilter();
                if (alertDialog!=null)
                {
                    alertDialog.dismiss();
                }

            }
        });

       alertDialog = new AlertDialog.Builder(ActivityAccountBook.this).setView(view)
                .setTitle("").show();
    }

    private void showDeleteDialog(final int pos) {
        View view = LayoutInflater.from(ActivityAccountBook.this).inflate(R.layout.dialog_bangumi_alter_name, null);
        final EditText editText = view.findViewById(R.id.bangumi_alter_name);
        editText.setText("确定删除"+accountDataAdapter.getItemContent(pos)+"？");
        editText.setTextColor(Color.RED);
        editText.setOnKeyListener(null);
        editText.setEnabled(false);
        editText.setTextSize(20);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            editText.setBackground(getDrawable(R.drawable.corner_background));
        }
        AlertDialog alertDialog = new AlertDialog.Builder(ActivityAccountBook.this).setView(view)
                .setTitle("Alert").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        allAccountData.remove(accountDataAdapter.getItem(pos));
                        saveToDataBase();
                        applyCurrentFilter();
                    }
                }).setNegativeButton("取消", null).show();

    }
    private void saveToDataBase() {
        try {
            if (listData == null) {
                throw new IllegalStateException("listData is null when saving account book");
            }
            DBListInfoManager dbListInfoManager = new DBListInfoManager(ActivityAccountBook.this);
            String content = JSONArray.toJSONString(allAccountData);
            String remark = catalogueNameEdit.getText().toString();
            listData.setContent(content);
            listData.setRemarks(remark);
//        Log.d(TAG, "saveToDataBase: content=" + content);
            boolean updated = dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), remark, content, listData.getCreateDate());
            if (!updated) {
                Log.e(TAG, "记账保存失败，orderID=" + listData.getOrderID());
                CrashHandler.appendLog(ActivityAccountBook.this, "记账保存失败", "orderID=" + listData.getOrderID(), null);
                Toast.makeText(ActivityAccountBook.this, "记账保存失败，请返回后刷新", Toast.LENGTH_SHORT).show();
                return;
            }
            setLatestResult();
        } catch (Exception e) {
            Log.e(TAG, "记账保存异常", e);
            CrashHandler.appendLog(ActivityAccountBook.this, "记账保存异常", "", e);
            Toast.makeText(ActivityAccountBook.this, "记账保存异常：" + e.getClass().getSimpleName(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getSafeAccountContent() {
        String content = listData == null ? null : listData.getContent();
        return content == null || content.trim().equals("") ? "[]" : content;
    }

    private void reloadLatestAccountData() {
        try {
            if (listData == null) {
                return;
            }
            ListData latestData = new DBListInfoManager(ActivityAccountBook.this).getDataByOrderId(listData.getOrderID());
            if (latestData != null) {
                listData = latestData;
            }
            new AnalyseContentTask().execute(getSafeAccountContent());
        } catch (Exception e) {
            Log.e(TAG, "记账刷新异常", e);
            CrashHandler.appendLog(ActivityAccountBook.this, "记账刷新异常", "", e);
            Toast.makeText(ActivityAccountBook.this, "记账刷新失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setLatestResultAndFinish() {
        setLatestResult();
    }

    private void setLatestResult() {
        if (listData == null) {
            return;
        }
        ListData latestData = null;
        try {
            latestData = new DBListInfoManager(ActivityAccountBook.this).getDataByOrderId(listData.getOrderID());
        } catch (Exception e) {
            Log.e(TAG, "读取最新记账返回数据失败", e);
            CrashHandler.appendLog(ActivityAccountBook.this, "读取最新记账返回数据失败", "", e);
        }
        if (latestData != null) {
            listData = latestData;
        }
        Intent intent = new Intent(ActivityAccountBook.this, MainFormActivity.class);
        intent.putExtra(MainFormActivity.LIST_DATA, listData);
        intent.putExtra(MainFormActivity.LIST_DATA_POS, posInListData);
        setResult(RESULT_BANGUMI_ACTIVITY, intent);
    }

    private void replaceAccountData(AccountData oldAccountData, AccountData newAccountData) {
        int index = allAccountData.indexOf(oldAccountData);
        if (index >= 0) {
            allAccountData.set(index, newAccountData);
        }
    }

    private static boolean containsQuery(String text, String query) {
        return text != null && text.contains(query);
    }

    private void applyCurrentFilter() {
        applySearchQuery(currentSearchQuery);
    }

    private void applySearchQuery(String query) {
        currentSearchQuery = query == null ? "" : query.trim();
        if (currentSearchQuery.equals("")) {
            showAccountData(allAccountData);
        } else {
            showAccountData(filterAccountData(currentSearchQuery));
        }
    }

    private List<AccountData> filterAccountData(String query) {
        List<AccountData> result = new ArrayList<>();
        for (AccountData data : allAccountData) {
            if (containsQuery(data.getType(), query)||containsQuery(data.getContent(), query)||containsQuery(data.getAccountTime(), query))
            {
                result.add(data);
            }
        }
        return result;
    }

    private void showAccountData(List<AccountData> accountData) {
        if (accountData == null) {
            accountData = new ArrayList<>();
        }
        AccountSummary summary = buildAccountSummary(accountData);
        accountDataAdapter.setData(accountData);
        accountIncome.setText(formatMoney(summary.income));
        accountExpenditure.setText(formatMoney(summary.expenditure));
        BigDecimal money = summary.income.subtract(summary.expenditure);

        accountMoney.setText(formatMoney(money.abs()));
        if (money.signum() < 0)
        {
            accountMoneyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_expenditure));
        }else {
            accountMoneyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_income));
        }
    }

    private AccountSummary buildAccountSummary(List<AccountData> accountData) {
        AccountSummary summary = new AccountSummary();
        dataSet.clear();
        entries.clear();
        if (accountData == null) {
            return summary;
        }
        for (AccountData data : accountData) {
            BigDecimal money = moneyOf(data.getMoney());
            if (money.signum() < 0)
            {
                BigDecimal expenditure = money.abs();
                String type=data.getType();
                BigDecimal valueTemp = dataSet.containsKey(type) ? dataSet.get(type).add(expenditure) : expenditure;
                dataSet.put(type,valueTemp);
                summary.expenditure = summary.expenditure.add(expenditure);
            }else {
                summary.income = summary.income.add(money);
            }
        }
        for (Map.Entry<String, BigDecimal> d : dataSet.entrySet()) {
            entries.add(new PieEntry(d.getValue().floatValue(), d.getKey()));
        }
        mExpenditure=summary.expenditure.doubleValue();
        return summary;
    }

    private static BigDecimal moneyOf(double money) {
        return BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_UP);
    }

    private static String formatMoney(BigDecimal money) {
        return money.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static class AccountSummary {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expenditure = BigDecimal.ZERO;
    }

    private Map<String, BigDecimal> dataSet = new LinkedHashMap<>();
    ArrayList<PieEntry> entries = new ArrayList<PieEntry>();
    double mExpenditure=0;

    public class AnalyseContentTask extends AsyncTask<String ,String,List<AccountData>>
    {
        List<AccountData> list;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(List<AccountData> accountData) {
            super.onPostExecute(accountData);
            refreshLayout.setRefreshing(false);
            allAccountData = accountData;
            currentSearchQuery = "";
            showAccountData(allAccountData);
        }

        @Override
        protected List<AccountData> doInBackground(String... strings) {
            try {
                String content = strings == null || strings.length == 0 || strings[0] == null ? "[]" : strings[0];
                list = JSONArray.parseArray(content, AccountData.class);
                if (list==null)
                {
                    return new ArrayList<>();
                }
            } catch (Exception e) {
                Log.e(TAG, "记账 JSON 解析失败", e);
                CrashHandler.appendLog(ActivityAccountBook.this, "记账 JSON 解析失败", "", e);
                return new ArrayList<>();
            }
//            List<AccountData> Alist = new ArrayList<>();
//            Alist.addAll(list);
//            list.clear();
//            for (int i=0;i<Alist.size();i++)
//            {
//                list.add(Alist.get(Alist.size()-1 - i));
//            }
            return list;
        }
    }

}
