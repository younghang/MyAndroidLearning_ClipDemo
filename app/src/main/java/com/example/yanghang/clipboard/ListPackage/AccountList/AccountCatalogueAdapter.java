package com.example.yanghang.clipboard.ListPackage.AccountList;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
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
    List<AccountCatalogue> showLists ;
    Context context;
    LayoutInflater inflater;
    List<Integer> orders = new ArrayList<>();
    List<AccountCatalogue> originLists;
    CataloguesChanged cataloguesChanged;

    public interface CataloguesChanged {
        public void addCatalogue(String name);

        public void removeCatalogue();

        void longClick(int pos);
    }

    public void setCataloguesChanged(CataloguesChanged cataloguesChanged) {
        this.cataloguesChanged = cataloguesChanged;
    }

    public AccountCatalogueAdapter(List<AccountCatalogue> lists, Context context) {

        this.context = context;
        inflater = LayoutInflater.from(context);
        if (lists == null) {
            lists = new ArrayList<>();
        }
        originLists = lists;
        updateShowLists();
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
        holder.catalogueName.setText(showLists.get(position).getCatalogueName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: list size="+showLists.size());
                int position = holder.getAdapterPosition();
                if (position == RecyclerView.NO_POSITION) {
                    return;
                }
                if (position != 0) {
                    orders.add(position);
                    Log.d(TAG, "onClick: Position="+position);
                    AccountCatalogue catalogue=getCurrentAccountCatalogue();
                    if (catalogue != null && catalogue.getSubCatalogue() == null) {
                        catalogue.setSubCatalogue(new ArrayList<AccountCatalogue>());
                    }
                    updateShowLists();
                    notifyDataSetChanged();
                    if (catalogue!=null && cataloguesChanged != null)
                    {
                        cataloguesChanged.addCatalogue(catalogue.getCatalogueName());
                    }


                } else {
                    if (orders.size() == 0)
                        return;
                    orders.remove(orders.size() - 1);

                    updateShowLists();
                    notifyDataSetChanged();
                    if (cataloguesChanged != null) {
                        cataloguesChanged.removeCatalogue();
                    }

                }

            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (cataloguesChanged != null && holder.getAdapterPosition() != RecyclerView.NO_POSITION) {
                    cataloguesChanged.longClick(holder.getAdapterPosition());
                }
                return true;
            }
        });
    }

    public List<AccountCatalogue> getLists() {
        return originLists;
    }

    public void setLists(List<AccountCatalogue> lists) {
        if (lists == null) {
            lists = new ArrayList<>();
        }
        originLists = lists;
        orders.clear();
        updateShowLists();
        notifyDataSetChanged();
    }

    public void addNewCatalogue(String catalogue) {
        if (catalogue == null || catalogue.trim().equals("")) {
            return;
        }
        getCurrentList().add(new AccountCatalogue(catalogue.trim()));
        updateShowLists();
        notifyDataSetChanged();
    }

    public AccountCatalogue getCurrentAccountCatalogue() {
        List<AccountCatalogue> list = originLists;

        for (int i = 0; i < orders.size() - 1; i++) {
            int order = orders.get(i)-1;
            if (order < 0 || order >= list.size()) {
                return null;
            }
            AccountCatalogue catalogue = list.get(order);
            list = catalogue.getSubCatalogue();
            if (list == null) {
                return null;
            }
        }
        int index=orders.size() - 1;
        if (index<0||index>(list.size()-1))
        {
            return null;
        }else
        {
           int order = orders.get(index)-1;
           if (order < 0 || order >= list.size()) {
               return null;
           }
           return list.get(order);
        }

    }

    public void removeItem(int pos) {
        List<AccountCatalogue> currentList = getCurrentList();
        if (pos <= 0 || pos - 1 >= currentList.size()) {
            return;
        }
        currentList.remove(pos - 1);
        updateShowLists();
        notifyDataSetChanged();
    }

    //没有用
    private List<AccountCatalogue> getOriginListsCopy__No() {
        List<AccountCatalogue> list = new ArrayList<>();
        for (AccountCatalogue catalogue : originLists) {
            list.add(catalogue);//此处不除，复制也没用
        }
        return list;
    }


    private List<AccountCatalogue> getCurrentList() {
        List<AccountCatalogue> list = originLists;

        for (int i = 0; i < orders.size(); i++) {
            int order = orders.get(i)-1;
            if (order < 0 || order >= list.size()) {
                return originLists;
            }
            AccountCatalogue catalogue = list.get(order);
            if (catalogue.getSubCatalogue() == null) {
                catalogue.setSubCatalogue(new ArrayList<AccountCatalogue>());
            }
            list = catalogue.getSubCatalogue();
        }
        return list;
    }

    private void updateShowLists() {
        showLists = new ArrayList<>();
        showLists.add(new AccountCatalogue("..."));
        showLists.addAll(getCurrentList());
    }


    @Override
    public int getItemCount() {
        return showLists.size();
    }

    class ItemHolder extends RecyclerView.ViewHolder {

        public TextView catalogueName;

        public ItemHolder(View itemView) {
            super(itemView);
            catalogueName = itemView.findViewById(R.id.item_catalogue_tv);
        }
    }
}
