package com.example.yanghang.clipboard.ListPackage.AccountList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

public class AccountCatalogueAdapter extends RecyclerView.Adapter {
    private static final String TAG = "nihao";
    List<AccountCatalogue> lists;
    Context context;
    LayoutInflater inflater;
    List<Integer> orders = new ArrayList<>();
    List<AccountCatalogue> originLists;
    CataloguesChanged cataloguesChanged;
    public interface CataloguesChanged{
        public void addCatalogue(String name);

        public void removeCatalogue();
        void longClick(int pos);
    }
    public void setCataloguesChanged(CataloguesChanged cataloguesChanged)
    {
        this.cataloguesChanged=cataloguesChanged;
    }

    public AccountCatalogueAdapter(List<AccountCatalogue> lists, Context context) {

        this.context = context;
        inflater = LayoutInflater.from(context);
        this.lists.add(0,new AccountCatalogue("..."));
        originLists=lists;
        for (AccountCatalogue accountCatalogue:lists)
        {
            this.lists.add(accountCatalogue);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_catalogue_selected, parent, false);
        RecyclerView.ViewHolder holder = new ItemHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder container, int position) {
        final ItemHolder holder = (ItemHolder) container;
        holder.catalogueName.setText(lists.get(position).getCatalogueName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: list size="+lists.size());
                if (holder.getAdapterPosition()!=0)
                {
//                    if (lists.get(holder.getAdapterPosition()).getSubCatalogue()==null)
//                        return;
                    lists = lists.get(holder.getAdapterPosition()).getSubCatalogue();
                    if (lists==null)
                    {
                        lists = new ArrayList<>();
                    }
                    lists.add(0,new AccountCatalogue("..."));
                    orders.add(holder.getAdapterPosition());
                    notifyDataSetChanged();
                    cataloguesChanged.addCatalogue(getCurrentAccountCatalogue().getCatalogueName());

                }else
                {
                    if (orders.size()>0)
                    {
                        orders.remove(orders.size()-1);
                        lists.remove(0);
                        lists=getCurrentList();
                        notifyDataSetChanged();
                        cataloguesChanged.removeCatalogue();
                    }

                }

            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                cataloguesChanged.longClick(holder.getAdapterPosition());
                return true;
            }
        });
    }
    public List<AccountCatalogue> getLists()
    {
        return originLists;
    }
    public void addNewCatalogue(String catalogue)
    {
        getCurrentList().add(new AccountCatalogue(catalogue));
    }
    public AccountCatalogue getCurrentAccountCatalogue()
    {
        List<AccountCatalogue> list=originLists;

        for (int i=0;i<orders.size()-1;i++)
        {
            list = list.get(orders.get(i)).getSubCatalogue();
        }
        return list.get(orders.get(orders.size()-1));
    }
    public void removeItem(int pos)
    {
        getCurrentList().remove(pos);
        notifyItemRemoved(pos);
    }

    private List<AccountCatalogue> getCurrentList() {
        List<AccountCatalogue> list=lists;

        for (int i=0;i<orders.size();i++)
        {
            list = list.get(orders.get(i)).getSubCatalogue();
        }
        return list;
    }
    private List<AccountCatalogue> getOriginCurrentList() {
        List<AccountCatalogue> list=lists;

        for (int i=0;i<orders.size();i++)
        {
            list = list.get(orders.get(i)).getSubCatalogue();
        }
        return list;
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        public TextView catalogueName;

        public ItemHolder(View itemView) {
            super(itemView);
            catalogueName = itemView.findViewById(R.id.item_catalogue_tv);
        }
    }
}
