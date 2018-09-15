package com.example.yanghang.clipboard.ListPackage.AccountList;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.R;

import java.util.List;

/**
 * Created by young on 2017/11/18.
 */

public class AccountDataAdapter extends RecyclerView.Adapter {


    private List<AccountData> accounts;
    private Context mContext;
    private CatalogueAdapter.OnItemClickListener mItemClickListener;

    public AccountDataAdapter(List<AccountData> accounts, Context mContext) {
        this.accounts = accounts;
        this.mContext = mContext;
    }
    public List<AccountData> getData() {
        return accounts;
    }

    public String getItemContent(int pos) {
        return accounts.get(pos).getContent();
    }
    public AccountData getItem(int pos)
    {
        return accounts.get(pos);
    }

    public void addItem(AccountData bangumiData) {
        accounts.add(0,bangumiData);
        notifyItemInserted(0);
    }

    public void setData(List<AccountData> list) {
        this.accounts = list;
        notifyDataSetChanged();
    }
    public void deleteItem(int pos) {
        accounts.remove(pos);
        notifyItemRemoved(pos);
    }

    public void editItem(int pos, AccountData accountData) {
        accounts.set(pos, accountData);
        notifyItemChanged(pos);
    }

    public void setItemClickListener(CatalogueAdapter.OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.item_account_data, parent, false);
        RecyclerView.ViewHolder holder = new AccountDataHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        final AccountDataHolder holder= (AccountDataHolder) viewHolder;
        AccountData accountData= accounts.get(position);
        double money=accountData.getMoney();
        holder.tvMoney.setText(Double.toString(money));
        if (money<0)
        {
            holder.tvMoney.setTextColor(Color.GREEN);
        }else {
            holder.tvMoney.setTextColor(Color.RED);
        }
        holder.createTimeTextView.setText(accountData.getAccountTime().replace("\n","  "));

        holder.contentTextView.setText(accountData.getContent().equals("")?accountData.getType():accountData.getType()+": "+accountData.getContent());
        if (accountData.getType().equals(accountData.getContent()))
            holder.contentTextView.setText(accountData.getContent());

        int color = Color.parseColor("#000000");
        try {
            color = Color.parseColor(accounts.get(position).getColor());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        holder.contentTextView.setTextColor(color);
        holder.contentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.OnItemClick(v,holder.getAdapterPosition());
            }
        });
        holder.contentLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mItemClickListener.OnItemLongClick(v, holder.getAdapterPosition());
                return true;

            }
        });
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }
    public class AccountDataHolder extends RecyclerView.ViewHolder {
        public TextView tvMoney;
        TextView contentTextView;
        TextView createTimeTextView;
        LinearLayout contentLayout;
        public AccountDataHolder(View itemView) {
            super(itemView);
            tvMoney = itemView.findViewById(R.id.item_account_money);
            contentLayout = itemView.findViewById(R.id.item_account_linearLayout);
            contentTextView = itemView.findViewById(R.id.item_account_content);
            createTimeTextView = itemView.findViewById(R.id.item_account_create_time);
        }
    }


}
