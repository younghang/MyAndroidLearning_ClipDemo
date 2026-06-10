package com.example.yanghang.clipboard.ListPackage.CalendarItemList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager;
import com.example.yanghang.clipboard.R;

import java.util.List;

public class CalendarItemEditDialog {
    public interface OnCalendarItemSaved {
        void onSaved(CalendarItemsData item);
    }

    public interface OnCalendarItemDeleted {
        void onDeleted();
    }

    public static void show(final Context context,
                            final CalendarImageManager imageManager,
                            final CalendarItemsData initialItem,
                            final String title,
                            final OnCalendarItemSaved listener) {
        show(context, imageManager, initialItem, title, listener, null);
    }

    public static void show(final Context context,
                            final CalendarImageManager imageManager,
                            final CalendarItemsData initialItem,
                            final String title,
                            final OnCalendarItemSaved listener,
                            final OnCalendarItemDeleted deleteListener) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_add_calendar_item, null);
        final EditText editText = view.findViewById(R.id.dialogue_add_calendar_item_name);
        final ImageView preview = view.findViewById(R.id.dialogue_calendar_icon_preview);
        final TextView iconName = view.findViewById(R.id.dialogue_calendar_icon_name);
        RecyclerView iconRecyclerView = view.findViewById(R.id.dialogue_calendar_icon_recyclerView);

        final String[] selectedPic = new String[]{"star"};
        if (initialItem != null) {
            editText.setText(initialItem.getCalendarItemName());
            if (initialItem.getCalendarItemPic() != null && initialItem.getCalendarItemPic().length() > 0) {
                selectedPic[0] = initialItem.getCalendarItemPic();
            }
        }
        CalendarImageManager.setImageSource(preview, selectedPic[0]);
        iconName.setText(selectedPic[0]);

        final List<CalendarItemsData> candidates = imageManager.getIconCandidates();
        CalendarAddItemsAdapter adapter = new CalendarAddItemsAdapter(candidates, context);
        adapter.setOnItemClickListener(new CalendarAddItemsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, int position) {
                CalendarItemsData item = candidates.get(position);
                selectedPic[0] = item.getCalendarItemPic();
                CalendarImageManager.setImageSource(preview, selectedPic[0]);
                iconName.setText(item.getCalendarItemName());
            }
        });
        iconRecyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        iconRecyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("确定", null)
                .setNegativeButton("取消", null);
        if (deleteListener != null) {
            builder.setNeutralButton("删除", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int which) {
                    deleteListener.onDeleted();
                }
            });
        }
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                positiveButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                String name = editText.getText().toString().trim();
                if (name.length() == 0) {
                    Toast.makeText(context, "先写一个条目名称", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (listener != null) {
                    boolean showImage = initialItem == null || initialItem.getShowImage() == null
                            || initialItem.getShowImage();
                    listener.onSaved(new CalendarItemsData(name, selectedPic[0], showImage));
                }
                dialog.dismiss();
                    }
                });
            }
        });
        dialog.show();
    }
}
