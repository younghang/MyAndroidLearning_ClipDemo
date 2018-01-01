package com.example.yanghang.clipboard.ListPackage.AccountList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by young on 2017/11/26.
 */

public class AccountCatalogueNamesAdapter extends RecyclerView.Adapter{

    Context context;
    LayoutInflater inflater;
    List<String> catalogueNames;

    public AccountCatalogueNamesAdapter(Context context, List<String> catalogueNames) {
        this.context = context;
        this.catalogueNames = catalogueNames;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=inflater.inflate(R.layout.item_account_catalog_names,null);
        MyHolder myHolder = new MyHolder(view);
        return myHolder;
    }
    public void addItem(String bangumiData) {
        catalogueNames.add(bangumiData);
        notifyItemInserted(catalogueNames.size());
    }

    public void setData(List<String> list) {
        this.catalogueNames = list;
        notifyDataSetChanged();
    }
    public void deleteItem(int pos) {
        catalogueNames.remove(pos);
        notifyItemRemoved(pos);
    }

    public void editItem(int pos, String accountData) {
        catalogueNames.set(pos, accountData);
        notifyItemChanged(pos);
    }




    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((MyHolder)holder).textView.setText(catalogueNames.get(position));
    }



    @Override
    public int getItemCount() {
        return catalogueNames.size();
    }
    class MyHolder extends RecyclerView.ViewHolder{
        TextView textView;
        public MyHolder(View itemView) {
            super(itemView);
            textView=itemView.findViewById(R.id.item_account_catalogue_name);
        }
    }



}
