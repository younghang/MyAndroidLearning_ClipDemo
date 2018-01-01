package com.example.yanghang.clipboard.ListPackage.FileList;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.yanghang.clipboard.ListPackage.ClipInfosList.ListClipInfoAdapter;
import com.example.yanghang.clipboard.R;

import java.util.List;


/**
 * Created by young on 2017/11/11.
 */

public class SettingFileAdapter extends RecyclerView.Adapter<SettingFileAdapter.SettingFileHolder> {


    @Override
    public SettingFileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.item_catalogue_selected,parent,false);
        SettingFileHolder holder = new SettingFileHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final SettingFileHolder holder, int position) {
        holder.tvFileName.setText(fileNames.get(holder.getAdapterPosition()));
        holder.tvFileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.OnItemClick(v, holder.getAdapterPosition());
            }
        });
        holder.tvFileName.setLongClickable(true);
        holder.tvFileName.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mItemClickListener.OnItemLongClick(v,holder.getAdapterPosition());
            }
        });
    }
    public void deleteFile(int pos)
    {
        fileNames.remove(pos);
        notifyItemRemoved(pos);
    }

    public String getFileName(int pos) {
        return fileNames.get(pos);
    }

    @Override
    public int getItemCount() {
        return fileNames.size();
    }
    List<String> fileNames;
    Context context;

    public SettingFileAdapter(List<String> fileNames, Context context) {
        this.fileNames = fileNames;
        this.context = context;
    }
    ListClipInfoAdapter.OnItemClickListener mItemClickListener;

    public void setOnItemClickListener(ListClipInfoAdapter.OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }
    public class SettingFileHolder extends RecyclerView.ViewHolder{

        public TextView tvFileName;
        public SettingFileHolder(View itemView) {
            super(itemView);
            tvFileName=itemView.findViewById(R.id.item_catalogue_tv);
        }
    }

}
