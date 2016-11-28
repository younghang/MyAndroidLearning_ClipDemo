package com.example.yanghang.myapplication.ListPackage;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yanghang.myapplication.R;

import java.util.List;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListMessageAdapter extends RecyclerView.Adapter<MessageViewHolder> {
    private List<ListData> mDatas;
    private Context mContext;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;

    public ListMessageAdapter(List<ListData> mDatas, Context mContext) {
        this.mDatas = mDatas;
        this.mContext = mContext;
        mInflater = LayoutInflater.from(mContext);
    }

    public ListData GetItemData(int pos) {
        return mDatas.get(pos);
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_message_info, parent, false);
        MessageViewHolder messageViewHolder = new MessageViewHolder(view);
        return  messageViewHolder;
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder,final int position) {
        holder.tvMessage.setText(mDatas.get(position).getInformation());
        holder.tvRemarks.setText(mDatas.get(position).getRemarks());
        holder.tvMessage.setMaxHeight((int) mContext.getResources().getDimension(R.dimen.max_height_item));
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pos = holder.getAdapterPosition();
                mItemClickListener.OnItemClick(v, pos);
            }

        });

        holder.tvInfoDate.setText(mDatas.get(position).getCreateDate());
        holder.mCardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                int pos = holder.getAdapterPosition();
                return mItemClickListener.OnItemLongClick(v, pos);

            }
        });

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        if (listener != null) {
            mItemClickListener=listener;
        }
    }

    public void deleteItem(int pos) {
        mDatas.remove(pos);
        notifyItemRemoved(pos);
    }

    public void editItem(int pos, ListData ls) {
        mDatas.set(pos, ls);
        notifyItemChanged(pos);
    }

    public void addItem(ListData data) {
        mDatas.add(0, data);
        notifyDataSetChanged();
    }

    public void addItem(ListData data,int pos) {

        mDatas.add(pos,data);
        if (pos==0)
            notifyDataSetChanged();
        else
        notifyItemInserted(pos);

    }

    public interface OnItemClickListener {
        void OnItemClick(View v, int position);

        boolean OnItemLongClick(View v, int position);
    }
}
class MessageViewHolder extends RecyclerView.ViewHolder{

    AlwaysMarqueeTextView tvRemarks;
    TextView tvMessage;
    CardView mCardView;
    TextView tvInfoDate;
    public MessageViewHolder(View itemView) {
        super(itemView);
        tvRemarks = (AlwaysMarqueeTextView) itemView.findViewById(R.id.tv_Remarks);
        tvMessage = (TextView) itemView.findViewById(R.id.tv_Message);
        tvInfoDate= (TextView) itemView.findViewById(R.id.tv_infoDate);
        mCardView = (CardView) itemView.findViewById(R.id.messageCardView);

    }
}
