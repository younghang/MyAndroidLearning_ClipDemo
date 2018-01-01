package com.example.yanghang.clipboard.ListPackage.AccountList;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.alibaba.fastjson.JSONArray;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by young on 2017/11/25.
 */

public class AccountCatalogueDialogFragment extends DialogFragment {
    private static final String TAG ="nihao" ;

    public void show(FragmentManager fragmentManager) {
        show(fragmentManager, "AccountCatalogueDialogFragment");
    }
    public interface Click{
        public void click(String name);
    }
    Click onSaveCatalogNameClick;
    RecyclerView recyclerView;
    RecyclerView horizontalRecyclerView;
    ImageButton btnAddNewCatalogue;
    Button btnSelectCatalogue;
    AccountCatalogueAdapter accountCatalogueAdapter;
    AccountCatalogueNamesAdapter accountCatalogueNamesAdapter;


    public void setOnSaveCatalogNameClick(Click catalogNameClick)
    {
        this.onSaveCatalogNameClick = catalogNameClick;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),R.style.alertDialog);
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_account_catalogue, null);
        builder.setView(view);


        recyclerView = view.findViewById(R.id.dialog_account_catalogue_recyclerView);
        accountCatalogueAdapter = new AccountCatalogueAdapter(initialCatalogues(),getActivity());
        accountCatalogueAdapter.setCataloguesChanged(new AccountCatalogueAdapter.CataloguesChanged() {
            @Override
            public void addCatalogue(String name) {
                accountCatalogueNamesAdapter.addItem(name);
            }

            @Override
            public void removeCatalogue() {
                accountCatalogueNamesAdapter.deleteItem(accountCatalogueNamesAdapter.getItemCount()-1);
            }

            @Override
            public void longClick(int pos) {
                accountCatalogueAdapter.removeItem(pos);
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
                            }
                        }).setNegativeButton("取消", null).show();
            }
        });

       String str= JSONArray.toJSONString(accountCatalogueAdapter.getLists());
        Log.d(TAG, "onCreateDialog: str"+str);
        btnSelectCatalogue = view.findViewById(R.id.dialog_account_catalogue_chooseCatalogueBtn);

        btnSelectCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AccountCatalogue accountCatalogue=accountCatalogueAdapter.getCurrentAccountCatalogue();

                Log.d(TAG, "onClick: "+"selectName=" + accountCatalogue.getCatalogueName());
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
        Dialog dialog=builder.create();
        WindowManager.LayoutParams lp=dialog.getWindow().getAttributes();
//模糊度
        dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        lp.alpha=0.9f;//（0.0-1.0）
        // 透明度，黑暗度为
        lp.dimAmount=0.9f;
        dialog.getWindow().setAttributes(lp);
        return dialog;
    }




    private List<AccountCatalogue> initialCatalogues() {
        AccountCatalogue accountCatalogue0=new AccountCatalogue("吃饭");
        accountCatalogue0.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue0.addSubCatalogueName("早饭");
        accountCatalogue0.addSubCatalogueName("中饭");
        accountCatalogue0.addSubCatalogueName("晚饭");

        AccountCatalogue accountCatalogue=new AccountCatalogue("饮食");
        accountCatalogue.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue.addSubCatalogue(accountCatalogue0);
        accountCatalogue.addSubCatalogueName("水果");
        accountCatalogue.addSubCatalogueName("零食");
        accountCatalogue.addSubCatalogueName("其他");
        AccountCatalogue accountCatalogue1=new AccountCatalogue("购物");
        accountCatalogue1.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue1.addSubCatalogueName("网购");
        accountCatalogue1.addSubCatalogueName("报销");
        accountCatalogue1.addSubCatalogueName("其他");

        AccountCatalogue accountCatalogue2=new AccountCatalogue("Top");
        accountCatalogue2.setSubCatalogue(new ArrayList<AccountCatalogue>());
        accountCatalogue2.addSubCatalogue(accountCatalogue);
        accountCatalogue2.addSubCatalogue(accountCatalogue1);
        return accountCatalogue2.getSubCatalogue();
    }
}
