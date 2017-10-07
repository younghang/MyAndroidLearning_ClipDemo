package com.example.yanghang.clipboard.ListPackage.DailyTaskList;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.yanghang.clipboard.R;

import java.util.List;

/**
 * Created by young on 2017/8/11.
 */

public class DailyTaskAdapter extends RecyclerView.Adapter<DailyTaskAdapter.DailyTaskHolder> {

    private Context mContext;
    private List<DailyTaskData> lists;

    public DailyTaskAdapter(Context mContext, List<DailyTaskData> lists) {
        this.mContext = mContext;
        this.lists = lists;
    }
    public String getTotalProgress()
    {
        int sum=0;
        for (int i=0;i<lists.size();i++)
        {
            sum+=lists.get(i).gettP();
        }
        return sum/lists.size()+"";
    }

    public List<DailyTaskData> getLists()
    {
        return lists;
    }

    @Override
    public DailyTaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_daily_task,parent,false);
        DailyTaskHolder holder = new DailyTaskHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final DailyTaskHolder holder, int position) {
        holder.tvTaskProgress.setText(lists.get(position).gettP()+"%");
        holder.tvTaskName.setText(lists.get(position).gettN());
        holder.llTaskClick.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final int pos=holder.getAdapterPosition();
                final View dialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_horizontal_progress, null);
                final TextView tvProgress=dialogView.findViewById(R.id.dailyTask_Progress);
                final SeekBar seekBar = dialogView.findViewById(R.id.dailyTask_SeekBar);
                int progress=lists.get(pos).gettP();
                tvProgress.setText(progress+"%");
                seekBar.setMax(10);
                seekBar.setProgress(progress/10);
                seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                        tvProgress.setText(i*10+"%");
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {

                    }
                });
                AlertDialog alertDialog = new AlertDialog.Builder(mContext).setView(dialogView)
                        .setTitle("修改进度")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                lists.get(pos).settP(seekBar.getProgress()*10);
                                notifyItemChanged(pos);
                            }
                        }).setNegativeButton("取消",null)
                        .show();

            }
        });

    }
    @Override
    public int getItemCount() {
        return lists.size();
    }


    class DailyTaskHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName;
        TextView tvTaskProgress;
        LinearLayout llTaskClick;

        public DailyTaskHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.item_dailyTask_taskName);
            tvTaskProgress = itemView.findViewById(R.id.item_dailyTask_progress);
            llTaskClick = itemView.findViewById(R.id.item_dailyTask_ll);
        }
    }

}
