package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.example.yanghang.clipboard.Fragment.FragmentDiary;
import com.example.yanghang.clipboard.Fragment.FragmentEditInfo;
import com.example.yanghang.clipboard.Fragment.FragmentToDo;
import com.example.yanghang.clipboard.Fragment.JsonData.ToDoData;
import com.example.yanghang.clipboard.OthersView.SlidingButtonView;
import com.example.yanghang.clipboard.R;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by yanghang on 2016/11/22.
 */
public class ListClipInfoAdapter extends RecyclerView.Adapter<ListClipInfoAdapter.ClipInfoViewHolder> implements SlidingButtonView.IonSlidingButtonListener {
    private List<ListData> mDatas;
    private Context mContext;
    private LayoutInflater mInflater;
    private OnItemClickListener mItemClickListener;
    private IonSlidingViewClickListener mIDeleteBtnClickListener;
    private SlidingButtonView mMenu = null;

    public ListClipInfoAdapter(List<ListData> mDatas, Context mContext) {
        this.mDatas = mDatas;
        this.mContext = mContext;
        mIDeleteBtnClickListener = (IonSlidingViewClickListener) mContext;
        mInflater = LayoutInflater.from(mContext);
    }

    public ListData getItemData(int pos) {
        return mDatas.get(pos);
    }

    @Override
    public ClipInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_clip_recycler, parent, false);
        ClipInfoViewHolder clipInfoViewHolder = new ClipInfoViewHolder(view);
        return clipInfoViewHolder;
    }

    @Override
    public void onBindViewHolder(final ClipInfoViewHolder holder, final int position) {
        String strMessage="";
        String catalogueName=mDatas.get(position).getCatalogue();

        switch(catalogueName)
        {
            case "日记":
                strMessage=mDatas.get(position).getContent().replaceAll("@#@","||");

                break;
            case "待办事项":
                strMessage=mDatas.get(position).getContent();
                String endDate="";
                ToDoData toDoData =null;
                try {
                    toDoData= JSON.parseObject(strMessage, ToDoData.class);
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
                if (toDoData!=null)
                {

                    strMessage=toDoData.getContent();
                    endDate=toDoData.getEndTime();
                }
                else
                {

                    strMessage=mDatas.get(position).getContent();
                    SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    String date = sDateFormat.format(new java.util.Date());
                    endDate=date;
                }
                strMessage=strMessage+"\n截止日期["+endDate+"]";
                break;
            default:
                strMessage=mDatas.get(position).getContent();

        }






        holder.layoutContent.getLayoutParams().width = Utils.getScreenWidth(mContext);
        holder.tvMessage.setText(strMessage);
        holder.tvRemarks.setText(mDatas.get(position).getRemarks());
        holder.tvRemarks.init((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE));
        holder.tvRemarks.startScroll();
//        holder.tvMessage.setMaxHeight((int) mContext.getResources().getDimension(R.dimen.max_height_item));
        holder.tvMessage.setEllipsize(TextUtils.TruncateAt.END);
        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断是否有删除菜单打开
                if (menuIsOpen()) {
                    closeMenu();//关闭菜单
                }
                int pos = holder.getAdapterPosition();
                mItemClickListener.OnItemClick(v, pos);
            }

        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int n = holder.getAdapterPosition();
                mIDeleteBtnClickListener.onDeleteBtnCilck(v, n);
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
        int orderid = mDatas.get(pos).getOrderID();
        mDatas.remove(pos);
        for (int i = 0; i < mDatas.size(); i++) {
            int order = mDatas.get(i).getOrderID();
            if (order > orderid) {
                mDatas.get(i).setOrderID(order - 1);
            }
        }
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

    public List<ListData> getDatas() {
        return mDatas;
    }

    public void setDatas(List<ListData> mDatas) {
        this.mDatas = mDatas;
    }

    public void addItem(ListData data,int pos) {


        for (int i = 0; i < mDatas.size(); i++) {
            int order = mDatas.get(i).getOrderID();
            if (order >= data.getOrderID()) {
                mDatas.get(i).setOrderID(order + 1);
            }
        }
        mDatas.add(pos,data);

        if (pos==0)
            notifyDataSetChanged();
        else
        notifyItemInserted(pos);

    }
    /**
     * 删除菜单打开信息接收
     */
    @Override
    public void onMenuIsOpen(View view) {
        mMenu = (SlidingButtonView) view;
    }

    /**
     * 滑动或者点击了Item监听
     *
     * @param slidingButtonView
     */
    @Override
    public void onDownOrMove(SlidingButtonView slidingButtonView) {
        if (menuIsOpen()) {
            if (mMenu != slidingButtonView) {
                closeMenu();
            }
        }
    }
    /**
     * 关闭菜单
     */
    public void closeMenu() {
        mMenu.closeMenu();
        mMenu = null;

    }

    /**
     * 判断是否有菜单打开
     */
    public Boolean menuIsOpen() {
        if (mMenu != null) {
            return true;
        }
        return false;
    }



    public interface OnItemClickListener {
        void OnItemClick(View v, int position);

        boolean OnItemLongClick(View v, int position);
    }
    public interface IonSlidingViewClickListener {


        void onDeleteBtnCilck(View view, int position);
    }

class ClipInfoViewHolder extends RecyclerView.ViewHolder{

    AlwaysMarqueeTextView tvRemarks;
    TextView tvMessage;
    CardView mCardView;
    TextView tvInfoDate;
    TextView btnDelete;
    ViewGroup layoutContent;
    public ClipInfoViewHolder(View itemView) {
        super(itemView);
        tvRemarks = (AlwaysMarqueeTextView) itemView.findViewById(R.id.tv_Remarks);
        tvMessage = (TextView) itemView.findViewById(R.id.tv_Message);
        tvInfoDate= (TextView) itemView.findViewById(R.id.tv_infoDate);
        mCardView = (CardView) itemView.findViewById(R.id.messageCardView);
        btnDelete = (TextView) itemView.findViewById(R.id.tv_delete);
        layoutContent = (ViewGroup) itemView.findViewById(R.id.item_clip_layout_main_content);
        ((SlidingButtonView) itemView).setSlidingButtonListener(ListClipInfoAdapter.this);

    }
}
}
 class Utils {

    //屏幕宽度（像素）
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return outMetrics.widthPixels;
    }
}
