package com.example.yanghang.clipboard.Fragment;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.yanghang.clipboard.ActivityBangumi.TAG;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentSpecialDayInfo.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentSpecialDayInfo#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentSpecialDayInfo extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_DATE = "setDate";
    private static final String ARG_NAME = "setName";
    private GridView gridView;
    private TextView tvName;

    // TODO: Rename and change types of parameters
    private String mDate;
    private String mName;

    private OnFragmentInteractionListener mListener;
    private SimpleAdapter adapter;

    public FragmentSpecialDayInfo() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param date
     * @param name
     * @return A new instance of fragment FragmentSpecialDayInfo.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentSpecialDayInfo newInstance(String date, String name) {
        FragmentSpecialDayInfo fragment = new FragmentSpecialDayInfo();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        args.putString(ARG_NAME, name);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mDate = getArguments().getString(ARG_DATE);
            mName = getArguments().getString(ARG_NAME);
        }
    }
    public void updateInfo(String mDate)
    {
        this.mDate=mDate;
        initData();
        initGridView();
        //SimpleAdapter无法自动刷新
//        ((SimpleAdapter)gridView.getAdapter()).notifyDataSetChanged();
    }


    View mView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_fragment_special_day_info, container, false);
        initialView();
        return mView;
    }

    private List<Map<String, String>> dataList;
    //时间数值
    private List<String> nums;
    private void initialView() {
        gridView = mView.findViewById(R.id.fragment_special_day_gridView);
        tvName = mView.findViewById(R.id.fragment_special_day_tv);
        tvName.setText("距离"+mName+"已经过去了...");
        initData();
        initGridView();

    }

    private void initGridView() {
        String[] from={"num","unit"};
        int[] to={R.id.item_special_day_num,R.id.item_special_day_unit};
        adapter = new SimpleAdapter(getActivity(), dataList, R.layout.item_special_day_gridview,from,to );
        gridView.setAdapter(adapter);
    }


    private void initData() {
        initNums();
        //时间单位
        String name[]={"年","月","周","天","时","分"};
        dataList = new ArrayList<Map<String, String>>();
        for (int i = 0; i <name.length; i++) {
            Map<String, String> map=new HashMap<String, String>();
            map.put("num",nums.get(i));
            map.put("unit",name[i]);
            dataList.add(map);
        }
    }

    //计算时间
    private void initNums() {
        Log.d(TAG, "initNums: 重新计算num");
        nums = new ArrayList<>();
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        Date setDate=new Date();
        try {
            setDate = sf.parse(mDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Date currentDay = new Date();
        double yearInterval=0;
        int monthInterval=0;
        int weekInterval=0;
        int dayInterval=0;
        int hourInterval=0;
        int minuteInterval=0;

        Calendar currentCalendar= new GregorianCalendar();
        currentCalendar.setTime(currentDay);
        Calendar setCalendar = new GregorianCalendar();
        setCalendar.setTime(setDate);


        monthInterval= (currentCalendar.get(Calendar.YEAR) - setCalendar.get(Calendar.YEAR)) * 12 + currentCalendar.get(Calendar.MONTH)
                - setCalendar.get(Calendar.MONTH);

        long between_days=(currentDay.getTime()-setDate.getTime())/(1000*3600*24);
        Double days=Double.parseDouble(String.valueOf(between_days));

        Calendar tempCalendar = new GregorianCalendar();
        tempCalendar.set(currentCalendar.get(Calendar.YEAR),setCalendar.get(Calendar.MONTH),setCalendar.get(Calendar.DATE));
        yearInterval=currentCalendar.get(Calendar.YEAR) - setCalendar.get(Calendar.YEAR);
        yearInterval+=(currentCalendar.get(Calendar.DAY_OF_YEAR)*1.0-tempCalendar.get(Calendar.DAY_OF_YEAR))/currentCalendar.getActualMaximum(Calendar.DAY_OF_YEAR);
        DecimalFormat df = new DecimalFormat("0.000");
        int tempWeek=0;
        if((days/7)>0 && (days/7)<=1){
            //不满一周的按一周算
            tempWeek= 1;
        }else if(days/7>1){
            int day=days.intValue();
            if(day%7>0){
                tempWeek= day/7+1;
            }else{
                tempWeek= day/7;
            }
        }else if((days/7)==0){
            tempWeek= 0;
        }else{
            //负数返还null
            tempWeek= 0;
        }
        weekInterval=tempWeek;


        minuteInterval=Integer.parseInt(String.valueOf((currentDay.getTime()-setDate.getTime())/(1000  * 60 )));
        hourInterval=minuteInterval/60;
        dayInterval=Integer.parseInt(String.valueOf((currentDay.getTime()-setDate.getTime())/(1000 * 24 * 60 * 60)));


        nums.add(df.format(yearInterval)+"");
        nums.add(monthInterval+"");
        nums.add(weekInterval+"");
        nums.add(dayInterval+"");
        nums.add(hourInterval+"");
        nums.add(minuteInterval+"");

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
