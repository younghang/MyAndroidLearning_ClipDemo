package com.example.yanghang.clipboard.ListPackage.CatalogueList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by young on 2017/11/23.
 */

public class CatalogueExportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    List<CatalogueExportData> lists;
    Context context;
    LayoutInflater inflater;

    public CatalogueExportAdapter(List<CatalogueExportData> list, Context context) {
        this.lists = list;
        this.context = context;
        inflater=LayoutInflater.from(context);
    }
    public List<CatalogueExportData> getLists()
    {
        return lists;
    }

    public List<String> getExportLists()
    {
        List<String> ls=new ArrayList<>();
        for (CatalogueExportData data:lists)
        {
            if (data.isExport())
                ls.add(data.getCatalogueName());
        }
        return ls;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_calendar_select_item, parent, false);
        CalendarItemHolder catalogueHolder = new CalendarItemHolder(view);
        return catalogueHolder;

    }

    private boolean onBind;
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder0, final int position) {

        final CalendarItemHolder holder=(CalendarItemHolder)holder0;
        holder.tvItemName.setText(lists.get(position).getCatalogueName());
        holder.imgItemPic.setVisibility(View.GONE);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.showSwitch.setChecked(!holder.showSwitch.isChecked());
            }
        });
        onBind=true;
        holder.showSwitch.setChecked(lists.get(position).isExport());
        onBind=false;

    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
    public class CalendarItemHolder extends RecyclerView.ViewHolder
    {

        public TextView tvItemName;
        public ImageView imgItemPic;
        public Switch showSwitch;
        public CalendarItemHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.calendar_select_item_name);
            imgItemPic = itemView.findViewById(R.id.calendar_select_item_image);
            showSwitch = itemView.findViewById(R.id.calendar_select_item_switch);
            showSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    lists.get(getAdapterPosition()).setExport(isChecked);
                    if (!onBind){

                    }

                }
            });
        }
    }

}
