package com.example.yanghang.clipboard.Fragment;


import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.yanghang.clipboard.ActivityCalendar;
import com.example.yanghang.clipboard.ActivityEditInfo;
import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemEditDialog;
import com.example.yanghang.clipboard.ListPackage.CalendarItemList.CalendarItemsData;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarAddItemsAdapter;
import com.example.yanghang.clipboard.ListPackage.CalendarList.CalendarImageManager;
import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListData;
import com.example.yanghang.clipboard.R;

import java.util.Calendar;
import java.util.List;

import static android.app.Activity.RESULT_OK;
import static com.example.yanghang.clipboard.MainFormActivity.LIST_DATA;
import static com.example.yanghang.clipboard.MainFormActivity.LIST_DATA_POS;
import static com.example.yanghang.clipboard.MainFormActivity.REQUEST_TEXT_EDITE_BACK;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarItem extends Fragment {


    public FragmentCalendarItem() {
        // Required empty public constructor
    }

    ActivityCalendar activityCalendar;
    CalendarItemAdapter calendarItemAdapter;

    private View mView;
    private Chronometer timer;
    private Button btnStart;
    private Button btnEnd;
    private CardView cardView;
    private RecyclerView recyclerView;
    private LinearLayout quickItemsContainer;
    private DBListInfoManager dbListInfoManager;
    boolean reset = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activityCalendar = (ActivityCalendar) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_calendar_item, container, false);
        initialView();
        return mView;
    }

    private void initialView() {
        dbListInfoManager = new DBListInfoManager(getActivity());
        timer = mView.findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0" + String.valueOf(hour) + ":%s");
        btnEnd = mView.findViewById(R.id.end_timer);
        btnStart = mView.findViewById(R.id.start_timer);
        cardView = mView.findViewById(R.id.timer_CardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setText(timer.getText());
                Toast.makeText(getActivity(), timer.getText() + "复制到粘贴板", Toast.LENGTH_SHORT).show();
            }
        });
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                timer.start();
            }
        });
        btnEnd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (reset) {
                    timer.setBase(SystemClock.elapsedRealtime());
                    btnEnd.setText("结束");
                } else {
                    timer.stop();
                    btnEnd.setText("重置");
                }
                reset = !reset;
            }
        });

        recyclerView = mView.findViewById(R.id.fragment_calendar_item_recycleView);
        quickItemsContainer = mView.findViewById(R.id.calendar_quick_items_container);
        List<CalendarItemsData> lists = activityCalendar.calendarImageManager.getLists();
        calendarItemAdapter = new CalendarItemAdapter(lists, getActivity(), new CalendarItemAdapter.OnCalendarItemVisibilityChanged() {
            @Override
            public void onChanged() {
                saveCalendarItems();
            }
        });
        calendarItemAdapter.setOnAddClickListener(new CalendarItemAdapter.OnAddClick() {
            @Override
            public void onAddClick() {
                CalendarItemEditDialog.show(getActivity(), activityCalendar.calendarImageManager,
                        new CalendarItemsData("", "star", true), "添加日历条目",
                        new CalendarItemEditDialog.OnCalendarItemSaved() {
                            @Override
                            public void onSaved(CalendarItemsData item) {
                                calendarItemAdapter.addItem(item);
                                saveCalendarItems();
                            }
                        });
            }
        });
        calendarItemAdapter.setOnItemClickListener(new CalendarAddItemsAdapter.OnItemClickListener() {
            @Override
            public void OnItemClick(View v, final int position) {
                CalendarItemsData calendarItemsData = calendarItemAdapter.getLists().get(position);
                CalendarItemEditDialog.show(getActivity(), activityCalendar.calendarImageManager,
                        calendarItemsData, "修改日历条目",
                        new CalendarItemEditDialog.OnCalendarItemSaved() {
                            @Override
                            public void onSaved(CalendarItemsData item) {
                                calendarItemAdapter.editItem(item, position);
                                saveCalendarItems();
                            }
                        },
                        new CalendarItemEditDialog.OnCalendarItemDeleted() {
                            @Override
                            public void onDeleted() {
                                calendarItemAdapter.deleteItem(position);
                                saveCalendarItems();
                            }
                        });
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(calendarItemAdapter);
        attachCalendarItemDragSort();
        renderQuickItems();
    }

    private void attachCalendarItemDragSort() {
        final boolean[] moved = new boolean[]{false};
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public boolean isLongPressDragEnabled() {
                return true;
            }

            @Override
            public boolean isItemViewSwipeEnabled() {
                return false;
            }

            @Override
            public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == CalendarItemAdapter.FOOTER_TYPE) {
                    return makeMovementFlags(0, 0);
                }
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
            }

            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                if (target.getItemViewType() == CalendarItemAdapter.FOOTER_TYPE) {
                    return false;
                }
                moved[0] = calendarItemAdapter.moveItem(viewHolder.getAdapterPosition(), target.getAdapterPosition()) || moved[0];
                return moved[0];
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                if (moved[0]) {
                    moved[0] = false;
                    saveCalendarItems();
                }
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    private void saveCalendarItems() {
        activityCalendar.calendarImageManager.setLists(calendarItemAdapter.getLists());
        activityCalendar.calendarImageManager.saveImageLists();
        renderQuickItems();
    }

    private void renderQuickItems() {
        if (quickItemsContainer == null || getActivity() == null) {
            return;
        }
        quickItemsContainer.removeAllViews();
        List<CalendarItemsData> visibleItems = activityCalendar.calendarImageManager.getVisibleLists();
        for (final CalendarItemsData item : visibleItems) {
            quickItemsContainer.addView(createQuickItemView(item));
        }
    }

    private View createQuickItemView(final CalendarItemsData item) {
        LinearLayout layout = new LinearLayout(getActivity());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(dpToPx(8), dpToPx(7), dpToPx(8), dpToPx(6));
        layout.setBackgroundResource(R.drawable.bg_calendar_quick_item_pressed);
        layout.setClickable(true);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(78), dpToPx(78));
        params.setMargins(0, 0, dpToPx(8), 0);
        layout.setLayoutParams(params);

        ImageView imageView = new ImageView(getActivity());
        LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(dpToPx(28), dpToPx(28));
        imageView.setLayoutParams(imageParams);
        CalendarImageManager.setImageSource(imageView, item.getCalendarItemPic());
        layout.addView(imageView);

        TextView textView = new TextView(getActivity());
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        textParams.setMargins(0, dpToPx(6), 0, 0);
        textView.setLayoutParams(textParams);
        textView.setText(item.getCalendarItemName());
        textView.setTextColor(0xff222a38);
        textView.setTextSize(12);
        textView.setGravity(android.view.Gravity.CENTER);
        textView.setSingleLine(true);
        layout.addView(textView);

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addCalendarItem(item);
            }
        });
        return layout;
    }

    private void addCalendarItem(CalendarItemsData item) {
        String today = FragmentCalendar.DAY_FORMAT.format(Calendar.getInstance().getTime());
        String itemName = item.getCalendarItemName();
        String itemPic = item.getCalendarItemPic();
        String recordKey = getCalendarRecordKey(itemName, itemPic);
        List<ListData> todayLists = activityCalendar.listTreeMap.get(today);
        if (todayLists != null) {
            for (int i = 0; i < todayLists.size(); i++) {
                ListData listData = todayLists.get(i);
                if (FragmentCalendar.CALENDAR_CATALOGUE_NAME.equals(listData.getCatalogue())
                        && (itemName.equals(listData.getRemarks())
                        || itemPic.equals(listData.getRemarks())
                        || recordKey.equals(listData.getRemarks()))) {
                    Intent intent = new Intent(getActivity(), ActivityEditInfo.class);
                    intent.putExtra(LIST_DATA_POS, i);
                    intent.putExtra(LIST_DATA, listData);
                    startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
                    return;
                }
            }
        }

        int orderid = dbListInfoManager.getDataCount();
        Intent intent = new Intent(getActivity(), ActivityEditInfo.class);
        intent.putExtra(LIST_DATA_POS, -1);
        intent.putExtra(LIST_DATA, new ListData(recordKey, "", orderid, FragmentCalendar.CALENDAR_CATALOGUE_NAME));
        startActivityForResult(intent, REQUEST_TEXT_EDITE_BACK);
    }

    private String getCalendarRecordKey(String itemName, String itemPic) {
        if ("diary".equals(itemPic) || "diary".equals(itemName) || "日记".equals(itemName)) {
            return "diary";
        }
        return itemName;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_TEXT_EDITE_BACK:
                if (resultCode == RESULT_OK) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    dbListInfoManager.updateDataByOrderId(listData.getOrderID(), listData.getCatalogue(),
                            listData.getRemarks(), listData.getContent(), listData.getCreateDate());
                    activityCalendar.loadDBToDataTree(FragmentCalendar.DAY_FORMAT.format(Calendar.getInstance().getTime()));
                } else if (resultCode == ActivityEditInfo.RESULT_ADD_NEW) {
                    ListData listData = (ListData) data.getExtras().get(LIST_DATA);
                    long result = dbListInfoManager.insertData(listData.getRemarks(), listData.getContent(),
                            listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());
                    activityCalendar.loadDBToDataTree(FragmentCalendar.DAY_FORMAT.format(Calendar.getInstance().getTime()));
                    if (result == -1) {
                        Toast.makeText(getActivity(), "存储该行数据出错", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return (int) (dp * density + 0.5f);
    }
}
