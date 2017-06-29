package com.example.yanghang.clipboard.OthersView.calendarlistview.library.sample;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.BaseCalendarItemAdapter;
import com.example.yanghang.clipboard.OthersView.calendarlistview.library.BaseCalendarItemModel;

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

/**
 * Created by kelin on 16-7-19.
 */
public class CalendarItemAdapter extends BaseCalendarItemAdapter<CustomCalendarItemModel> {

    public CalendarItemAdapter(Context context) {
        super(context);
    }

    @Override
    public View getView(String date, CustomCalendarItemModel model, View convertView, ViewGroup parent) {
        ViewGroup view = (ViewGroup) LayoutInflater.from(mContext).inflate(R.layout.griditem_custom_calendar_item, null);

        TextView dayNum = (TextView) view.findViewById(R.id.tv_day_num);
        dayNum.setText(model.getDayNumber());

        view.setBackgroundResource(R.drawable.bg_shape_calendar_item_normal);

        if (model.isToday()) {
            dayNum.setTextColor(mContext.getResources().getColor(R.color.red_ff725f));
            dayNum.setText(mContext.getResources().getString(R.string.today));
        }

        if (model.isHoliday()) {
            dayNum.setTextColor(mContext.getResources().getColor(R.color.red_ff725f));
        }


        if (model.getStatus() == BaseCalendarItemModel.Status.DISABLE) {
            dayNum.setTextColor(mContext.getResources().getColor(android.R.color.darker_gray));
        }

        if (!model.isCurrentMonth()) {
            dayNum.setTextColor(mContext.getResources().getColor(R.color.gray_bbbbbb));
            view.setClickable(true);
        }

        TextView dayNewsCount = (TextView) view.findViewById(R.id.tv_day_new_count);
        if (model.getNewsCount() > 0) {
            dayNewsCount.setText(String.format(mContext.getResources().getString(R.string.calendar_item_new_count), model.getNewsCount()));
            dayNewsCount.setVisibility(View.VISIBLE);
        } else {
            dayNewsCount.setVisibility(View.GONE);
        }


        for (int i = 0; i < model.imageCount; i++) {
//            Log.d(TAG, "getView: imageCount="+model.imageCount);
            switch (i) {
                case 0:
                    ImageView imageView1 = view.findViewById(R.id.calendar_item_image1);
                    setImage(imageView1,model.pics.get(i));
                    break;
                case 1:
                    ImageView imageView2 = view.findViewById(R.id.calendar_item_image2);
                    setImage(imageView2,model.pics.get(i));
                    break;
                case 2:
                    ImageView imageView3 = view.findViewById(R.id.calendar_item_image3);
                    setImage(imageView3,model.pics.get(i));
                    break;
                case 3:
                    ImageView imageView4 = view.findViewById(R.id.calendar_item_image4);
                    setImage(imageView4,model.pics.get(i));
                    break;
                case 4:
                    ImageView imageView5 = view.findViewById(R.id.calendar_item_image5);
                    setImage(imageView5,model.pics.get(i));
                    break;
                case 5:
                    ImageView imageView6 = view.findViewById(R.id.calendar_item_image6);
                    setImage(imageView6,model.pics.get(i));
                    break;
                case 6:
                    ImageView imageView7 = view.findViewById(R.id.calendar_item_image7);
                    setImage(imageView7,model.pics.get(i));
                    break;
            }
        }


        return view;
    }
    private  void setImage(ImageView imageView,String pic)
    {
        Log.d(TAG, "setImage: pic="+pic);
         
        switch (pic)
        {
            case "luser":
                imageView.setImageResource(R.drawable.toilet_paper);
                break;
            case "diary":
                imageView.setImageResource(R.drawable.diary1);
                break;
            case "weight":
                imageView.setImageResource(R.drawable.weight_scale);
                break;
            case "star":
                imageView.setImageResource(R.drawable.ic_star);
                break;
            case "fire":
                imageView.setImageResource(R.drawable.ic_fire);
                break;
            case "check":
                imageView.setImageResource(R.drawable.ic_green_check);
                break;
            case "jp":
                imageView.setImageResource(R.drawable.jp_learn);
                break;
            case "like":
                imageView.setImageResource(R.mipmap.ic_like_normal);
                break;
            case "rest":
                imageView.setImageResource(R.drawable.rest);
                break;
            default:
                imageView.setImageResource(R.drawable.ic_star);
        }
        imageView.setVisibility(View.VISIBLE);
    }
}
