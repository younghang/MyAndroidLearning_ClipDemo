package com.example.yanghang.myapplication.ListPackage.MessageList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.yanghang.myapplication.R;

import java.util.List;

/**
 * Created by yanghang on 2016/12/3.
 */
public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    Context context;
    LayoutInflater inflater;
    List<MessageData> mDatas;

    private int COMPUTER_TYPE = 456;
    private int YOU_TYPE = 123;

    public MessageAdapter(Context context, List<MessageData> mDatas) {
        this.context = context;
        this.mDatas = mDatas;
        inflater = LayoutInflater.from(context);
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
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MessagePCHolder) {
            ((MessagePCHolder) holder).messageText.setText(mDatas.get(position).getMessageText());
        }
        if (holder instanceof MessagePhoneHolder) {
            ((MessagePhoneHolder) holder).messageText.setText(mDatas.get(position).getMessageText());
        }

    }

    @Override
    public int getItemCount() {
        return mDatas.size();
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
