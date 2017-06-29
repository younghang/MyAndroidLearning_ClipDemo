package com.example.yanghang.clipboard.Fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yanghang.clipboard.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendarItem extends Fragment {


    public FragmentCalendarItem() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_calendar_item, container, false);
    }

}
