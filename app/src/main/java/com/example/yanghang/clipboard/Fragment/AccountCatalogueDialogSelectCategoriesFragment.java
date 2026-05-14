package com.example.yanghang.clipboard.Fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.OrientationHelper;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountCatalogue;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountCatalogueAdapter;
import com.example.yanghang.clipboard.ListPackage.AccountList.AccountCatalogueNamesAdapter;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by young on 2017/11/25.
 */

public class AccountCatalogueDialogSelectCategoriesFragment extends DialogFragment {
    private static final String TAG ="nihao" ;
    private static final String PREF_ACCOUNT_CATALOGUES = "account_catalogues";

    public void show(FragmentManager fragmentManager) {

        show(fragmentManager, "AccountCatalogueDialogSelectCategoriesFragment");

    }
    public interface Click{
        public void click(String name);
    }
    Click onSaveCatalogNameClick;
    RecyclerView recyclerView;
    RecyclerView horizontalRecyclerView;
    ImageButton btnAddNewCatalogue;
    Button btnResetDefaultCatalogue;
    Button btnSelectCatalogue;
    AccountCatalogueAdapter accountCatalogueAdapter;
    AccountCatalogueNamesAdapter accountCatalogueNamesAdapter;


    public void setOnSaveCatalogNameClick(Click catalogNameClick)
    {
        this.onSaveCatalogNameClick = catalogNameClick;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 设置背景透明
        getDialog().getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        // set cancel on touch outside

        final View view = inflater.inflate(R.layout.dialog_account_catalogue, null);




        recyclerView = view.findViewById(R.id.dialog_account_catalogue_recyclerView);
        accountCatalogueAdapter = new AccountCatalogueAdapter(loadCatalogues(),getActivity());
        accountCatalogueAdapter.setCataloguesChanged(new AccountCatalogueAdapter.CataloguesChanged() {
            @Override
            public void addCatalogue(String name) {
                accountCatalogueNamesAdapter.addItem(name);
            }

            @Override
            public void removeCatalogue() {
                if (accountCatalogueNamesAdapter != null && accountCatalogueNamesAdapter.getItemCount() > 0) {
                    accountCatalogueNamesAdapter.deleteItem(accountCatalogueNamesAdapter.getItemCount()-1);
                }
            }

            @Override
            public void longClick(int pos) {
                accountCatalogueAdapter.removeItem(pos);
                saveCatalogues();
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(accountCatalogueAdapter);

        btnAddNewCatalogue = view.findViewById(R.id.dialog_account_catalogue_addNewCatalogueBtn);
        btnAddNewCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view1 = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_bangumi_alter_name, null);
                final EditText editText = view1.findViewById(R.id.bangumi_alter_name);
                editText.setText("");
                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(getActivity()).setView(view1)
                        .setTitle("添加类别").setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                String name = editText.getText().toString();
                                accountCatalogueAdapter.addNewCatalogue(name);
                                saveCatalogues();
                            }
                        }).setNegativeButton("取消", null).show();
            }
        });

        btnResetDefaultCatalogue = view.findViewById(R.id.dialog_account_catalogue_resetDefaultBtn);
        btnResetDefaultCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("恢复默认分类")
                        .setMessage("会清空你手动添加的记账分类，恢复为默认分类。")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                accountCatalogueAdapter.setLists(initialCatalogues());
                                accountCatalogueNamesAdapter.setData(new ArrayList<String>());
                                clearSavedCatalogues();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        String str= JSONArray.toJSONString(accountCatalogueAdapter.getLists());
//        Log.d(TAG, "onCreateDialog: str"+str);
        btnSelectCatalogue = view.findViewById(R.id.dialog_account_catalogue_chooseCatalogueBtn);

        btnSelectCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountCatalogue accountCatalogue=accountCatalogueAdapter.getCurrentAccountCatalogue();

//                Log.d(TAG, "onClick: "+"selectName=" + accountCatalogue.getCatalogueName());
                if  (accountCatalogue==null)
                {
                    accountCatalogue = new AccountCatalogue("吃饭");
                }
                onSaveCatalogNameClick.click(accountCatalogue.getCatalogueName());
            }
        });
        horizontalRecyclerView = view.findViewById(R.id.dialog_account_catalogue_horizontal_recyclerView);
        horizontalRecyclerView.setHasFixedSize(true);//设置固定大小
        horizontalRecyclerView.setItemAnimator(new DefaultItemAnimator());//设置默认动画
        LinearLayoutManager mLayoutManage=new LinearLayoutManager(getActivity());
        mLayoutManage.setOrientation(OrientationHelper.HORIZONTAL);//设置滚动方向，横向滚动
        horizontalRecyclerView.setLayoutManager(mLayoutManage);
        accountCatalogueNamesAdapter=new AccountCatalogueNamesAdapter(getActivity(),new ArrayList<String>());
        horizontalRecyclerView.setAdapter(accountCatalogueNamesAdapter);

        return view;
    }
//记住不要再这里面写 Dialog 不然周围会有黑色边框！！！搞了30分钟才发现
//    @Override
//    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.alertDialog);
//
//        builder.setView(view,0,0,0,0);
//
//        return builder.create();
//    }




    private List<AccountCatalogue> loadCatalogues() {
        String saved = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(PREF_ACCOUNT_CATALOGUES, "");
        if (saved != null && !saved.trim().equals("")) {
            try {
                List<AccountCatalogue> list = JSONArray.parseArray(saved, AccountCatalogue.class);
                removeBackItems(list);
                if (list != null && !list.isEmpty()) {
                    return list;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return initialCatalogues();
    }

    private void saveCatalogues() {
        if (accountCatalogueAdapter == null) {
            return;
        }
        List<AccountCatalogue> list = accountCatalogueAdapter.getLists();
        removeBackItems(list);
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .putString(PREF_ACCOUNT_CATALOGUES, JSONArray.toJSONString(list))
                .apply();
    }

    private void clearSavedCatalogues() {
        PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                .remove(PREF_ACCOUNT_CATALOGUES)
                .apply();
    }

    private void removeBackItems(List<AccountCatalogue> list) {
        if (list == null) {
            return;
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            AccountCatalogue catalogue = list.get(i);
            if (catalogue == null || "...".equals(catalogue.getCatalogueName())) {
                list.remove(i);
            } else {
                removeBackItems(catalogue.getSubCatalogue());
            }
        }
    }

    private List<AccountCatalogue> initialCatalogues() {
        AccountCatalogue accountCatalogue0=new AccountCatalogue("吃饭");
        accountCatalogue0.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue0.addSubCatalogueName("早饭");
        accountCatalogue0.addSubCatalogueName("中饭");
        accountCatalogue0.addSubCatalogueName("晚饭");

        AccountCatalogue accountCatalogue=new AccountCatalogue("饮食");
        accountCatalogue.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue.addSubCatalogueName("水果");
        accountCatalogue.addSubCatalogueName("零食");
        accountCatalogue.addSubCatalogueName("饮料");
        accountCatalogue.addSubCatalogueName("外卖");

        AccountCatalogue accountCatalogue1=new AccountCatalogue("购物");
        accountCatalogue1.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue1.addSubCatalogueName("网购");
        accountCatalogue1.addSubCatalogueName("服装时尚");
        accountCatalogue1.addSubCatalogueName("报销");
        accountCatalogue1.addSubCatalogueName("实验耗材");

        AccountCatalogue accountCatalogue3=new AccountCatalogue("工资");

        AccountCatalogue accountCatalogue4=new AccountCatalogue("出行");
        AccountCatalogue accountCatalogue6=new AccountCatalogue("房租");


        AccountCatalogue accountCatalogue5=new AccountCatalogue("其他");
        accountCatalogue5.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue5.addSubCatalogueName("日常生活用品");
        accountCatalogue5.addSubCatalogueName("水电");
        accountCatalogue5.addSubCatalogueName("看病");
        accountCatalogue5.addSubCatalogueName("买药");
        accountCatalogue5.addSubCatalogueName("住宿");

        AccountCatalogue accountCatalogue2=new AccountCatalogue("Top");
        accountCatalogue2.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue2.addSubCatalogue(accountCatalogue0);
        accountCatalogue2.addSubCatalogue(accountCatalogue);
        accountCatalogue2.addSubCatalogue(accountCatalogue1);
        accountCatalogue2.addSubCatalogue(accountCatalogue3);
        accountCatalogue2.addSubCatalogue(accountCatalogue4);
        accountCatalogue2.addSubCatalogue(accountCatalogue6);
        accountCatalogue2.addSubCatalogue(accountCatalogue5);
        return accountCatalogue2.getSubCatalogue();
    }
}
