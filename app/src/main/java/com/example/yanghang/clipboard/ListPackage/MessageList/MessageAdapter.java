package com.example.yanghang.clipboard.ListPackage.MessageList;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.OthersView.MessageText;
import com.example.yanghang.clipboard.R;

import java.io.File;
import java.util.List;

/**
 * Created by yanghang on 2016/12/3.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    LayoutInflater inflater;
    List<MessageData> mDatas;
    OnItemClickListener onItemClickListener;
    private int COMPUTER_TYPE = 456;
    private int YOU_TYPE = 123;
    public MessageAdapter(Context context, List<MessageData> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        inflater = LayoutInflater.from(context);
    }

    public void addItem(MessageData messageData) {
        mDatas.add(messageData);
        notifyItemInserted(mDatas.size() - 1);
    }

    public String getItemMessageAt(int pos) {
        return mDatas.get(pos).getMessageText();
    }

    public MessageData getItem(int pos) {
        return mDatas.get(pos);
    }

    @Override
    public int getItemViewType(int position) {
        switch (mDatas.get(position).getMessageType()) {
            case COMPUTER:
                return COMPUTER_TYPE;
            case YOU:
                return YOU_TYPE;
        }
        return 0;

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        if (onItemClickListener != null) {
            this.onItemClickListener = onItemClickListener;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        if (viewType == COMPUTER_TYPE) {
            view = inflater.inflate(R.layout.item_pc_message_recycler, parent, false);
            return new MessagePCHolder(view);
        }
        if (viewType == YOU_TYPE) {
            view = inflater.inflate(R.layout.item_phone_message_recycler, parent, false);
            return new MessagePhoneHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        Log.v(MainFormActivity.TAG, "onBind set text:" + mDatas.get(position).getMessageText());
        if (holder instanceof MessagePCHolder) {
            MessageData messageData=mDatas.get(position);
            if (messageData.getMessageKind() == MessageData.MessageKind.MESSAGE) {
                ((MessagePCHolder) holder).messageText.setText(mDatas.get(position).getMessageText());
            }
            else
            {
                File file = new File(messageData.getMessageText());
                String fileName = file.getName();
                ((MessagePCHolder) holder).messageText.setText("文件【"+fileName+"】接收完成");
            }

            ((MessagePCHolder) holder).messageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, holder.getAdapterPosition());
                }
            });
            ((MessagePCHolder) holder).messageText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    return onItemClickListener.onItemLongClick(view,holder.getAdapterPosition());
                }
            });
        }
        if (holder instanceof MessagePhoneHolder) {
            ((MessagePhoneHolder) holder).messageText.setText(mDatas.get(position).getMessageText());
            ((MessagePhoneHolder) holder).messageText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onItemClick(v, holder.getAdapterPosition());
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    public interface OnItemClickListener {
        public void onItemClick(View v, int pos);

        public boolean onItemLongClick(View v, int pos);
    }
}

class MessagePCHolder extends RecyclerView.ViewHolder {


    MessageText messageText;

    public MessagePCHolder(View itemView) {
        super(itemView);
        messageText = (MessageText) itemView.findViewById(R.id.tv_message_item_pc);
    }
}

class MessagePhoneHolder extends RecyclerView.ViewHolder {
    MessageText messageText;

    public MessagePhoneHolder(View itemView) {
        super(itemView);
        messageText = (MessageText) itemView.findViewById(R.id.tv_message_item_phone);
    }
}
