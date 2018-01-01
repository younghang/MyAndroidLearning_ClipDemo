package com.example.yanghang.clipboard;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountCatalogueDialogFragment;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountData;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountDataAdapter;
import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.AutoFixText.AutofitTextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.example.yanghang.clipboard.ActivityBangumi.RESULT_BANGUMI_ACTIVITY;

public class ActivityAccountBook extends AppCompatActivity {

    private static final String TAG = "ActivityAccountBook";
    RecyclerView accountRecycleView;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_book);
        toolbar = (Toolbar) findViewById(R.id.accountToolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

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
        Bundle bundle = getIntent().getExtras();
        listData = (ListData) bundle.get(MainFormActivity.LIST_DATA);
        posInListData = (int) bundle.get(MainFormActivity.LIST_DATA_POS);
        catalogueNameEdit.setText(listData.getRemarks());
        new AnalyseContentTask().execute(listData.getContent());
    }


    private void initialView() {
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ActivityAccountBook.this, MainFormActivity.class);
                intent.putExtra(MainFormActivity.LIST_DATA, listData);
                intent.putExtra(MainFormActivity.LIST_DATA_POS, posInListData);
                setResult(RESULT_BANGUMI_ACTIVITY, intent);
                finish();
            }
        });

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
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                new AnalyseContentTask().execute(listData.getContent());
                            }
                        }).start();

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
            Intent intent = new Intent(ActivityAccountBook.this, MainFormActivity.class);
            intent.putExtra(MainFormActivity.LIST_DATA, listData);
            intent.putExtra(MainFormActivity.LIST_DATA_POS, posInListData);
            setResult(RESULT_BANGUMI_ACTIVITY, intent);
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showAlterDialog(final int position)
    {
        View view = LayoutInflater.from(ActivityAccountBook.this).inflate(R.layout.dialog_add_new_account, null);
        Button saveAccount = view.findViewById(R.id.accountDialogSave);
        final EditText accountMoneyEdit = view.findViewById(R.id.accountDialogMoney);
        final EditText accountContentEdit = view.findViewById(R.id.accountDialogContent);
        final TextView accountDialogCatalogueName = view.findViewById(R.id.accountDialogCatalogueName);
        accountDialogCatalogueName.setText(accountDataAdapter.getItem(position).getType().equals("")?"吃饭":accountDataAdapter.getItem(position).getType());
        accountMoneyEdit.setText(Double.toString(Math.abs(accountDataAdapter.getItem(position).getMoney())));
        accountContentEdit.setText(accountDataAdapter.getItem(position).getContent());
        accountDialogCatalogueName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AccountCatalogueDialogFragment fragment = new AccountCatalogueDialogFragment();
                fragment.setOnSaveCatalogNameClick(new AccountCatalogueDialogFragment.Click() {
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
            radioGroup.check(R.id.rb1);
        }else {
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
                AccountData accountData = new AccountData(accountDataAdapter.getItem(position).getAccountTime(), "", money, "#cccccc", selectCatalogueName, accountContentEdit.getText().toString());
                accountDataAdapter.editItem(position,accountData);
                saveToDataBase();
                if (alertDialog!=null)
                {
                    alertDialog.dismiss();
                }
                new AnalyseContentTask().execute(listData.getContent());

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
        accountDialogCatalogueName.setText("吃饭");
        accountDialogCatalogueName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AccountCatalogueDialogFragment fragment = new AccountCatalogueDialogFragment();
                fragment.setOnSaveCatalogNameClick(new AccountCatalogueDialogFragment.Click() {
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
                if (content.equals(""))
                    content=selectCatalogueName;
                AccountData accountData = new AccountData(ListData.GetDate(), "", money, "#cccccc", selectCatalogueName,content );
                accountDataAdapter.addItem(accountData);
                accountRecycleView.scrollToPosition(0);
                saveToDataBase();
                if (alertDialog!=null)
                {
                    alertDialog.dismiss();
                }
                new AnalyseContentTask().execute(listData.getContent());

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
                        accountDataAdapter.deleteItem(pos);
                        saveToDataBase();
                        new AnalyseContentTask().execute(listData.getContent());
                    }
                }).setNegativeButton("取消", null).show();

    }
    private void saveToDataBase() {
        DBListInfoManager dbListInfoManager = new DBListInfoManager(ActivityAccountBook.this);
        String content = JSONArray.toJSONString(accountDataAdapter.getData());
        listData.setContent(content);
//        Log.d(TAG, "saveToDataBase: content=" + content);
        dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(), catalogueNameEdit.getText().toString(), content, listData.getCreateDate());
    }
    public class AnalyseContentTask extends AsyncTask<String ,String,List<AccountData>>
    {
        List<AccountData> list;
        double income=0;
        double expenditure=0;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            refreshLayout.setRefreshing(true);
        }

        @Override
        protected void onPostExecute(List<AccountData> accountData) {
            super.onPostExecute(accountData);
            refreshLayout.setRefreshing(false);
            DecimalFormat df = new DecimalFormat("0.00");
            accountDataAdapter.setData(accountData);
            accountIncome.setText(df.format(income));
            accountExpenditure.setText(df.format(Math.abs(expenditure)));
            double money=income+expenditure;

            accountMoney.setText(df.format(Math.abs(money)));
            if (money<0)
            {
                accountMoneyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_expenditure));
            }else {
                accountMoneyImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_income));
            }
        }

        @Override
        protected List<AccountData> doInBackground(String... strings) {
            list = JSONArray.parseArray(strings[0], AccountData.class);
            if (list==null)
            {
                return new ArrayList<>();
            }
            for (AccountData data : list) {
                if (data.getMoney()<0)
                {
                    expenditure += data.getMoney();
                }else
                    income+=data.getMoney();
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
