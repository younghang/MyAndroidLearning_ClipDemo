package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
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

import static com.example.yanghang.clipboard.MainFormActivity.TAG;

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
        runEnterAnimation(holder.itemView,position);
        String strMessage="";
        String catalogueName=mDatas.get(position).getCatalogue();
        strMessage=mDatas.get(position).getSimpleContent();

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
        notifyDataSetChanged();
        lastAnimatedPosition=-1;
        animationsLocked=false;
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

    //添加载入动画,从别的地方抄来的，稍加修改，大部分都是这样的
    private int lastAnimatedPosition=-1;
    private boolean animationsLocked = false;
    private boolean delayEnterAnimation = true;
    private void runEnterAnimation(final View view, final int position) {


        if (animationsLocked)
        {//animationsLocked是布尔类型变量，一开始为false，确保仅屏幕一开始能够显示的item项才开启动画

//            return;
            if (position < lastAnimatedPosition)
                return;
//            Log.d(TAG, "run EnterAnimation: ");

            lastAnimatedPosition = position;
            view.setTranslationY(100);//相对于原始位置下方500
            view.setAlpha(0.f);//完全透明
            view.setScaleX(0.8f);
            view.setScaleY(0.85f);
            //每个item项两个动画，从透明到不透明，从下方移动到原来的位置
            //并且根据item的位置设置延迟的时间，达到一个接着一个的效果
            view.animate()
                    .translationY(0).alpha(1.f)//设置最终效果为完全不透明，并且在原来的位置
                    .scaleX(1).scaleY(1)
                    .setInterpolator(new DecelerateInterpolator(0.5f))//设置动画效果为在动画开始的地方快然后慢
                    .setDuration(350)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                        }
                    })
                    .start();
        }else{


        if (position > lastAnimatedPosition) {//lastAnimatedPosition是int类型变量，一开始为-1，这两行代码确保了recycleview滚动式回收利用视图时不会出现不连续的效果
//            Log.d(TAG, "run First EnterAnimation: ");
            lastAnimatedPosition = position;
            view.setTranslationY(300);//相对于原始位置下方500
            view.setAlpha(0.f);//完全透明
            view.setScaleX(0.75f);
            view.setScaleY(0.85f);
            //每个item项两个动画，从透明到不透明，从下方移动到原来的位置
            //并且根据item的位置设置延迟的时间，达到一个接着一个的效果
            view.animate()
                    .translationY(0).alpha(1.f)//设置最终效果为完全不透明，并且在原来的位置
                    .scaleX(1).scaleY(1)
//                    .setStartDelay(delayEnterAnimation ? 20 * (position) : 0)//根据item的位置设置延迟时间，达到依次动画一个接一个进行的效果
                    .setInterpolator(new DecelerateInterpolator(0.5f))//设置动画效果为在动画开始的地方快然后慢
                    .setDuration(420)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            view.clearAnimation();
                            animationsLocked = true;//确保仅屏幕一开始能够显示的item项才开启动画，也就是说屏幕下方还没有显示的item项滑动时是没有动画效果
                        }



                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            if (position<lastAnimatedPosition-5)
                                view.clearAnimation();
                        }
                    })
                    .start();
        }
        }

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
