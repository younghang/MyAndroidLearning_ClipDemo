package com.example.yanghang.clipboard.Fragment;


import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;

import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarItem extends Fragment {


    public FragmentCalendarItem() {
        // Required empty public constructor
    }

    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_calendar_item, container, false);
        initialView();
        return mView;
    }
    private Chronometer timer;
    private Button btnStart;
    private Button btnEnd;
    private CardView cardView;
    boolean reset=false;
    private void initialView()
    {
        timer = mView.findViewById(R.id.timer);
        timer.setBase(SystemClock.elapsedRealtime());
        int hour = (int) ((SystemClock.elapsedRealtime() - timer.getBase()) / 1000 / 60);
        timer.setFormat("0"+String.valueOf(hour)+":%s");
        btnEnd = mView.findViewById(R.id.end_timer);
        btnStart = mView.findViewById(R.id.start_timer);
        cardView = mView.findViewById(R.id.timer_CardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                cm.setText(timer.getText());
                Toast.makeText(getActivity(), timer.getContentDescription()+"复制到粘贴板", Toast.LENGTH_SHORT).show();
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
                if (reset)
                {
                    timer.setBase(SystemClock.elapsedRealtime());
                    btnEnd.setText("结束");
                }
                else {
                    timer.stop();
                    btnEnd.setText("重置");
                }
                reset=!reset;
            }
        });


    }


}
