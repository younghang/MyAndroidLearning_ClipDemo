package com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yanghang.clipboard.Fragment.FragmentCalendar;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.BaseCalendarListAdapter;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.CalendarHelper;
import com.example.yanghang.clipboard.R;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;


public class ListDataAdapter extends BaseCalendarListAdapter<ListData> {


    public ListDataAdapter(Context context) {
        super(context);
    }
    public void setDataItem(int pos,String date,ListData data)
    {
        dateDataMap.get(date).set(pos, data);
        notifyDataSetChanged();
    }
    public void addDataItem(String date,ListData data)
    {
        if  (dateDataMap.get(date)==null)
        {
            List<ListData> listDatas = new ArrayList<>();
            listDatas.add(data);
            dateDataMap.put(date, listDatas);
            //解决一个问题notifyDataSetChanged时indexOutOfRange的问题
            //因为有个List保存了Map的key,却不能同步更新，只能设置setDateDataMap的时候才行
            setDateDataMap(dateDataMap);
            notifyDataSetChanged();

        }
        else
        {
            dateDataMap.get(date).add(data);
            notifyDataSetChanged();
        }


    }


    @Override
    public View getSectionHeaderView(String date, View convertView, ViewGroup parent) {
        HeaderViewHolder headerViewHolder;
        List<ListData> modelList = dateDataMap.get(date);
        if (convertView != null) {
            headerViewHolder = (HeaderViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_header, null);
            headerViewHolder = new HeaderViewHolder();
            headerViewHolder.dayText = (TextView) convertView.findViewById(R.id.header_day);
            headerViewHolder.yearMonthText = (TextView) convertView.findViewById(R.id.header_year_month);
            headerViewHolder.isFavImage = (ImageView) convertView.findViewById(R.id.header_btn_fav);
            convertView.setTag(headerViewHolder);
        }

        Calendar calendar = CalendarHelper.getCalendarByYearMonthDay(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String dayStr = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        if (day < 10) {
            dayStr = "0" + dayStr;
        }
        headerViewHolder.dayText.setText(dayStr);
        headerViewHolder.yearMonthText.setText(FragmentCalendar.YEAR_MONTH_YUE_FORMAT.format(calendar.getTime()));
        return convertView;
    }

    @Override
    public View getItemView(final ListData model, String date, final int pos, View convertView, ViewGroup parent) {
        ContentViewHolder contentViewHolder;
        if (convertView != null) {
            contentViewHolder = (ContentViewHolder) convertView.getTag();

        } else {
            convertView = inflater.inflate(R.layout.listitem_calendar_content, null);
            contentViewHolder = new ContentViewHolder();
            contentViewHolder.contentTextView = (TextView) convertView.findViewById(R.id.item_calendar_content);
            contentViewHolder.createTimeTextView = (TextView) convertView.findViewById(R.id.item_calendar_create_time);
            contentViewHolder.remarkTextView = (TextView) convertView.findViewById(R.id.item_calendar_remark);
            contentViewHolder.contentLayout = convertView.findViewById(R.id.item_list_calendar_linearLayout);

            convertView.setTag(contentViewHolder);
        }

        contentViewHolder.contentTextView.setText(model.getSimpleContent());
        contentViewHolder.createTimeTextView.setText(model.getCreateDate().replace("\n","  "));
        contentViewHolder.remarkTextView.setText(model.getRemarks().equals("")?"Long Click Edit":model.getRemarks());
//        contentViewHolder.contentLayout.setLongClickable(true);
        contentViewHolder.remarkTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.d(TAG, "onLongClick: at ListDataAdapter");
                listDataLongClickListener.onLongClick(model,pos);
                return true;
            }
        });
//        GenericDraweeHierarchy hierarchy = GenericDraweeHierarchyBuilder.newInstance(convertView.getResources())
//                .setRoundingParams(RoundingParams.asCircle())
//                .build();
//        contentViewHolder.remarkTextView.setHierarchy(hierarchy);
//        contentViewHolder.remarkTextView.setImageURI(Uri.parse(model.getImages().get(0)));

//import com.squareup.picasso.Picasso;
//        Picasso.with(convertView.getContext()).load(Uri.parse(model.getImages().get(0)))
//                .into(contentViewHolder.remarkTextView);
        return convertView;
    }

    private static class HeaderViewHolder {
        TextView dayText;
        TextView yearMonthText;
        ImageView isFavImage;
    }

    private static class ContentViewHolder {
        TextView contentTextView;
        TextView createTimeTextView;
        TextView remarkTextView;
        RelativeLayout contentLayout;
    }

    public void setOnListDataLongClickListener(OnListDataLongClickListener listener) {
        this.listDataLongClickListener=listener;
    }
    private OnListDataLongClickListener listDataLongClickListener;
    public interface OnListDataLongClickListener
    {
        void onLongClick(ListData data,int pos);
    }

}
