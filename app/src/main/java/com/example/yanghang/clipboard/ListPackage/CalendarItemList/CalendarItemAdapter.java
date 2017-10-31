package com.example.yanghang.clipboard.ListPackage.CalendarItemList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;


import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager;
import com.example.yanghang.clipboard.R;

import java.util.List;

import static com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager.setImageSource;

/**
 * Created by young on 2017/10/30.
 */

public class CalendarItemAdapter extends RecyclerView.Adapter<CalendarItemAdapter.CalendarItemSelectHolder> {

    private List<CalendarItemsData> lists;
    Context context;
    LayoutInflater inflater;
    public interface OnCalendarItemVisibilityChanged
    {
        void onChanged();
    }
    OnCalendarItemVisibilityChanged onCalendarItemVisibilityChanged;
    public CalendarItemAdapter(List<CalendarItemsData> lists, Context context,OnCalendarItemVisibilityChanged onCalendarItemVisibilityChanged) {
        this.lists = lists;
        this.context = context;
        inflater=LayoutInflater.from(context);
        this.onCalendarItemVisibilityChanged = onCalendarItemVisibilityChanged;
    }


    @Override
    public CalendarItemSelectHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_calendar_select_item, parent, false);
        CalendarItemSelectHolder catalogueHolder = new CalendarItemSelectHolder(view);
        return catalogueHolder;
    }

    @Override
    public void onBindViewHolder(CalendarItemSelectHolder holder, final int position) {
        holder.tvItemName.setText(lists.get(position).getCalendarItemName());

        holder.showSwitch.setChecked(lists.get(position).getShowImage());
        holder.showSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    lists.get(position).setShowImage(isChecked);
                    onCalendarItemVisibilityChanged.onChanged();
            }
        });
        String pic = lists.get(position).getCalendarItemPic();
        setImageSource(holder.imgItemPic,pic);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
    public List<CalendarItemsData> getLists()
    {
        return lists;
    }

    public class CalendarItemSelectHolder extends RecyclerView.ViewHolder
    {

        public TextView tvItemName;
        public ImageView imgItemPic;
        public Switch showSwitch;
        public CalendarItemSelectHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.calendar_select_item_name);
            imgItemPic = itemView.findViewById(R.id.calendar_select_item_image);
            showSwitch = itemView.findViewById(R.id.calendar_select_item_switch);
        }
    }
}
