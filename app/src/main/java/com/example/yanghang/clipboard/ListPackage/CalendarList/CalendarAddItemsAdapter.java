package com.example.yanghang.clipboard.ListPackage.CalendarList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


import com.example.yanghang.clipboard.R;

import java.util.List;

/**
 * Created by young on 2017/6/28.
 */

public class CalendarAddItemsAdapter extends RecyclerView.Adapter<CalendarAddItemsAdapter.CalendarItemHolder> implements View.OnClickListener{
    private List<CalendarItemsData> lists;
    Context context;
    LayoutInflater inflater;
    private OnItemClickListener mItemClickListener;

    public CalendarAddItemsAdapter(List<CalendarItemsData> lists, Context context) {
        this.lists = lists;
        this.context = context;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public CalendarItemHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_calendar_item, parent, false);
        view.setOnClickListener(this);
        CalendarItemHolder catalogueHolder = new CalendarItemHolder(view);
        return catalogueHolder;
    }
    @Override
    public void onClick(View v) {
        if (mItemClickListener != null) {
            //注意这里使用getTag方法获取position
            mItemClickListener.OnItemClick(v,(int)v.getTag());
        }
    }

    @Override
    public void onBindViewHolder(CalendarItemHolder holder, int position) {
        holder.tvItemName.setText(lists.get(position).getCalendarItemName());
        holder.itemView.setTag(position);
        String pic = lists.get(position).getCalendarItemPic();
        switch (pic)
        {
            case "luser":
                holder.imgItemPic.setImageResource(R.drawable.toilet_paper);
                break;
            case "diary":
                holder.imgItemPic.setImageResource(R.drawable.diary1);
                break;
            case "weight":
                holder.imgItemPic.setImageResource(R.drawable.weight_scale);
                break;
            case "star":
                holder.imgItemPic.setImageResource(R.drawable.ic_star);
                break;
            case "fire":
                holder.imgItemPic.setImageResource(R.drawable.ic_fire);
                break;
            case "check":
                holder.imgItemPic.setImageResource(R.drawable.ic_green_check);
                break;
            case "jp":
                holder.imgItemPic.setImageResource(R.drawable.jp_learn);
                break;
            case "paint":
                holder.imgItemPic.setImageResource(R.drawable.paint);
                break;
            case "code":
                holder.imgItemPic.setImageResource(R.drawable.code);
                break;
            case "like":
                holder.imgItemPic.setImageResource(R.mipmap.ic_like_normal);
                break;
            case "rest":
                holder.imgItemPic.setImageResource(R.drawable.rest);
                break;

            default:
                holder.imgItemPic.setImageResource(R.drawable.ic_star);
        }
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        if (listener != null) {
            mItemClickListener = listener;
        }
    }
    public interface OnItemClickListener {
        void OnItemClick(View v, int position);
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }
    public class CalendarItemHolder extends RecyclerView.ViewHolder
    {

        public TextView tvItemName;
        public ImageView imgItemPic;
        public CalendarItemHolder(View itemView) {
            super(itemView);
            tvItemName = itemView.findViewById(R.id.calendar_item_name);
            imgItemPic = itemView.findViewById(R.id.calendar_item_image);
        }
    }
}
