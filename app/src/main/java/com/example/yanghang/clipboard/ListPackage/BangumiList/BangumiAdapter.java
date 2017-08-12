package com.example.yanghang.clipboard.ListPackage.BangumiList;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yanghang.clipboard.ListPackage.CatalogueList.CatalogueAdapter;
import com.example.yanghang.clipboard.R;

import java.util.List;

/**
 * Created by young on 2017/7/3.
 */

public class BangumiAdapter extends RecyclerView.Adapter<QuarterAnimationHolder> {

    private List<BangumiData> datas;
    private Context mContext;
    private CatalogueAdapter.OnItemClickListener mItemClickListener;

    public BangumiAdapter(List<BangumiData> datas, Context mContext) {
        this.datas = datas;
        this.mContext = mContext;
    }

    public List<BangumiData> getData() {
        return datas;
    }

    public String getItemName(int pos) {
        return datas.get(pos).getName();
    }
    public void addItem(BangumiData bangumiData) {
        datas.add(bangumiData);
        notifyItemInserted(datas.size());
    }

    public void setData(List<BangumiData> list) {
        this.datas = list;
        notifyDataSetChanged();
    }

    public void deleteItem(int pos) {
        datas.remove(pos);
        notifyItemRemoved(pos);
    }

    public void editItem(int pos, BangumiData bangumiData) {
        datas.set(pos, bangumiData);
        notifyItemChanged(pos);
    }


    @Override
    public QuarterAnimationHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_bangumi, parent, false);
        QuarterAnimationHolder holder = new QuarterAnimationHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final QuarterAnimationHolder holder, int position) {
        holder.tvAnimationName.setText(datas.get(position).getName());
        holder.tvScore.setText(datas.get(position).getGrades()+"");
        holder.tvScore.setTextColor(getScoreColor(datas.get(position).getGrades()));
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mItemClickListener.OnItemClick(view, holder.getAdapterPosition());
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                return mItemClickListener.OnItemLongClick(view, holder.getAdapterPosition());
            }
        });
        holder.tvProgress.setText(datas.get(position).getProgress()+"");
        holder.tvProgress.setTextColor(Color.GREEN);

    }

    private int getScoreColor(int grades) {
        if (grades >=90) {
            return Color.parseColor("#ff0000");
        }else if (grades>=85)
        {
            return Color.parseColor("#ff5588");
        }
        else if (grades >= 80) {
            return Color.parseColor("#ff5500");
        } else if (grades >= 70) {
            return Color.parseColor("#d99610");
        } else if (grades>=60)
            return Color.parseColor("#d4b16b");
        else return Color.BLACK;

    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public void setOnItemClickListener(CatalogueAdapter.OnItemClickListener listener) {
        if (listener != null) {
            mItemClickListener = listener;
        }
    }

    public BangumiData getItem(int position) {
        return datas.get(position);
    }
}

class QuarterAnimationHolder extends RecyclerView.ViewHolder {
    TextView tvAnimationName;
    CardView cardView;
    TextView tvScore;
    TextView tvProgress;

    public QuarterAnimationHolder(View itemView) {
        super(itemView);
        tvAnimationName = itemView.findViewById(R.id.item_bangumi_tv_animation_name);
        cardView = itemView.findViewById(R.id.item_bangumi_CardView);
        tvScore = itemView.findViewById(R.id.item_bangumi_score);
        tvProgress = itemView.findViewById(R.id.item_bangumi_progress);
    }
}
