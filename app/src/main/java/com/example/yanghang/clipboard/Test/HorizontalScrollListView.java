package com.example.yanghang.clipboard.Test;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;

import java.util.ArrayList;
import java.util.List;

public class HorizontalScrollListView extends AppCompatActivity {

    private RecyclerView horizontalRecycleView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_horizontal_scroll_list_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        horizontalRecycleView = (RecyclerView) findViewById(R.id.test_horizontal_recycleView);
        horizontalRecycleView.setHasFixedSize(true);//设置固定大小
        horizontalRecycleView.setItemAnimator(new DefaultItemAnimator());//设置默认动画
        LinearLayoutManager mLayoutManage=new LinearLayoutManager(this);
        mLayoutManage.setOrientation(OrientationHelper.HORIZONTAL);//设置滚动方向，横向滚动
        horizontalRecycleView.setLayoutManager(mLayoutManage);
        final List<String> lists = new ArrayList<>();
        for (int i=0;i<9;i++)
        {
            lists.add(i + "   ");
        }
        horizontalRecycleView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public MyHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view= LayoutInflater.from(HorizontalScrollListView.this).inflate(R.layout.item_catalogue_left_drawer,null);
                MyHolder myHolder = new MyHolder(view);
                return myHolder;
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((MyHolder)holder).textView.setText(lists.get(position));
            }



            @Override
            public int getItemCount() {
                return lists.size();
            }
            class MyHolder extends RecyclerView.ViewHolder{


                TextView textView;
                public MyHolder(View itemView) {
                    super(itemView);
                    textView=itemView.findViewById(R.id.tv_catalogue_recycler);

                }
            }

        });



    }

}
