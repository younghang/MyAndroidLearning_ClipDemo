package com.example.yanghang.clipboard.ListPackage.CalendarItemList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

import static com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager.setImageSource;

/**
 * Created by young on 2017/10/30.
 */

public class CalendarItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final static int ITEM_TYPE = 1; // 正常条目
    public final static int FOOTER_TYPE = 2;//底部添加新项

    private List<CalendarItemsData> lists;
    Context context;
    LayoutInflater inflater;
    public interface OnCalendarItemVisibilityChanged
    {
        void onChanged();
    }
    public interface OnAddClick
    {
        void onAddClick();
    }
    OnAddClick onAddClick;
    CalendarAddItemsAdapter.OnItemClickListener onItemClickListener;
    public void setOnAddClickListener(OnAddClick onAddClickListener)
    {
        this.onAddClick=onAddClickListener;
    }

    public void setOnItemClickListener(CalendarAddItemsAdapter.OnItemClickListener listener) {
        if (listener != null) {
            this.onItemClickListener = listener;
        }
    }
    OnCalendarItemVisibilityChanged onCalendarItemVisibilityChanged;
    public CalendarItemAdapter(List<CalendarItemsData> lists, Context context,OnCalendarItemVisibilityChanged onCalendarItemVisibilityChanged) {
        this.lists = lists;
        this.context = context;
        inflater=LayoutInflater.from(context);
        this.onCalendarItemVisibilityChanged = onCalendarItemVisibilityChanged;
        //添加footer
        this.lists.add(null);
    }
    public void addItem(CalendarItemsData itemsData)
    {
        lists.remove(lists.size() - 1);
        lists.add(itemsData);
        notifyItemChanged(lists.size()-1);
        this.lists.add(null);
        notifyItemInserted(lists.size()-1);
    }
    public void editItem(CalendarItemsData itemsData,int position)
    {
        lists.set(position, itemsData);
        notifyItemChanged(position);
    }
    public List<CalendarItemsData> getLists()
    {
        List<CalendarItemsData> ls=new ArrayList<>();
        ls.addAll(lists);
        ls.remove(ls.size() - 1);
        return ls;
    }
    public void deleteItem(int pos)
    {
        lists.remove(pos);
        notifyItemRemoved(pos);
    }



    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        if (viewType==FOOTER_TYPE){
            View view = inflater.inflate(R.layout.item_calendar_item_footer_view, parent, false);
            viewHolder = new FooterViewHolder(view);

        }else {
            View view = inflater.inflate(R.layout.item_calendar_select_item, parent, false);
            viewHolder = new CalendarItemSelectHolder(view);
        }
        return viewHolder;

    }
    private boolean onBind;
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder0, final int position) {

        //这时候 CalendarItemsData null，先把 footer 处理了
        if (holder0 instanceof FooterViewHolder) {
            if (onAddClick!=null)
            {
                FooterViewHolder holder=(FooterViewHolder)holder0;
                holder.linearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onAddClick.onAddClick();
                    }
                });

            }
            return;
        }

        final CalendarItemSelectHolder holder=(CalendarItemSelectHolder)holder0;
        holder.tvItemName.setText(lists.get(position).getCalendarItemName());

        onBind = true;
        holder.showSwitch.setChecked(lists.get(position).getShowImage());
        onBind = false;



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onItemClickListener.OnItemClick(v,holder.getAdapterPosition());
            }
        });
        String pic = lists.get(position).getCalendarItemPic();
        setImageSource(holder.imgItemPic,pic);
    }

    @Override
    public int getItemViewType(int position) {
        CalendarItemsData itemsData = lists.get(position);
        if (itemsData == null) {
            return FOOTER_TYPE;
        }
        return ITEM_TYPE;

    }

    @Override
    public int getItemCount() {
        return lists.size();
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
             showSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    lists.get(getAdapterPosition()).setShowImage(isChecked);

                    onCalendarItemVisibilityChanged.onChanged();
                    if(!onBind) {
                        notifyItemChanged(getAdapterPosition());
                    }

                }
            });

        }
    }
    class FooterViewHolder extends RecyclerView.ViewHolder {

        public ImageView imgAddNewItem;
        public LinearLayout linearLayout;

        public FooterViewHolder(View itemView) {
            super(itemView);
            imgAddNewItem = itemView.findViewById(R.id.calendar_item_footer_image);
            linearLayout = itemView.findViewById(R.id.calendar_item_footer_ll);
        }
    }
}
