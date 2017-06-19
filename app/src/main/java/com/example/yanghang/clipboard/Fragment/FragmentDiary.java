package com.example.yanghang.clipboard.Fragment;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yanghang.clipboard.ActivityEditInfo;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link FragmentDiary.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link FragmentDiary#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentDiary extends FragmentEditAbstract {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    String morningDiary;
    String afternoonDiary;
    String eveningDiary;
    private View mView;
    // TODO: Rename and change types of parameters

    TabLayout mTabLayout;
    ViewPager mViewPager;
    List<FragmentEditInfo> fragmentEditInfos = new ArrayList<FragmentEditInfo>();

    FragmentPagerAdapter fragmentPagerAdapter;
    String[] mTitles = new String[]{"上午", "下午","晚上"};
    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param information Parameter 1.
     * @param isEdit Parameter 2.
     * @return A new instance of fragment FragmentDiary.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentDiary newInstance(String information, boolean isEdit) {
        FragmentDiary fragment = new FragmentDiary();
        newInstance(fragment, information, isEdit);
        return fragment;
    }

    public FragmentDiary() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onICreate();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView= inflater.inflate(R.layout.fragment_diary, null);
        initView();
        return mView;
    }

    private void initView()
    {
        try {
            morningDiary=infoEdit.split("@#@")[0];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            morningDiary="";
        }
        try {
            afternoonDiary=infoEdit.split("@#@")[1];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            afternoonDiary="";
        }
        try {
            eveningDiary=infoEdit.split("@#@")[2];
        }
        catch (Exception e)
        {
            e.printStackTrace();
            eveningDiary="";
        }

        mTabLayout = (TabLayout) mView.findViewById(R.id.id_tabLayout);
        mViewPager = (ViewPager) mView.findViewById(R.id.id_viewPager);
        FragmentEditInfo fragmentEditInfo1 = FragmentEditInfo.newInstance(morningDiary, isEdit);
        FragmentEditInfo fragmentEditInfo2 = FragmentEditInfo.newInstance(afternoonDiary, isEdit);
        FragmentEditInfo fragmentEditInfo3 = FragmentEditInfo.newInstance(eveningDiary, isEdit);
//        Log.v(MainFormActivity.TAG, "    EditText  "+(fragmentEditInfo1.editInfo==fragmentEditInfo2.editInfo));
        fragmentEditInfos.add(fragmentEditInfo1);
        fragmentEditInfos.add(fragmentEditInfo2);
        fragmentEditInfos.add(fragmentEditInfo3);
        fragmentPagerAdapter= new FragmentPagerAdapter(((ActivityEditInfo) getActivity()).getSupportFragmentManager()) {
            @Override
            public android.support.v4.app.Fragment getItem(int position) {
                return fragmentEditInfos.get(position);
            }

            @Override
            public int getCount() {
                return mTitles.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {

                return mTitles[position];
            }

        };
        mViewPager.setAdapter(fragmentPagerAdapter);
        mViewPager.setOffscreenPageLimit(mTitles.length);
        mTabLayout.setupWithViewPager(mViewPager);
    }
    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void redo() {
        fragmentEditInfos.get(mViewPager.getCurrentItem()).redo();

    }

    @Override
    public void undo() {
        fragmentEditInfos.get(mViewPager.getCurrentItem()).undo();
    }

    @Override
    public String getString() {
        String str="";
        morningDiary=((FragmentEditAbstract)fragmentPagerAdapter.getItem(0)).getString().replace("@#@", "*#*");
        afternoonDiary=((FragmentEditAbstract)fragmentPagerAdapter.getItem(1)).getString().replace("@#@", "*#*");
        eveningDiary=((FragmentEditAbstract)fragmentPagerAdapter.getItem(2)).getString().replace("@#@", "*#*");
        str=morningDiary+"@#@"+afternoonDiary+"@#@"+eveningDiary;
//        Log.v(MainFormActivity.TAG, "getString called in FragmentDiary :morning= " + morningDiary);
//        Log.v(MainFormActivity.TAG, "getString called in FragmentDiary :afternoon= " + afternoonDiary);
        return str;
    }

    @Override
    public void enableEdit() {
        for (int i=0;i<fragmentEditInfos.size();i++)
        {
            ((FragmentEditAbstract)fragmentPagerAdapter.getItem(i)).enableEdit();
        }

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
