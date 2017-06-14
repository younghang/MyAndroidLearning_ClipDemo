package com.example.yanghang.clipboard.ListPackage.ClipInfosList;

import android.animation.ObjectAnimator;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.yanghang.clipboard.DBClipInfos.DBListInfoManager;
import com.example.yanghang.clipboard.MainFormActivity;
import com.example.yanghang.clipboard.R;

import java.util.Collections;

/**
 * Created by yanghang on 2016/11/23.
 */
public class MyItemTouchHelperCallBack extends ItemTouchHelper.Callback {

    float FromEval;
    float ToEval;
    private RecyclerView.ViewHolder vh;
    private IItemTouch mItemTouchListener;

    private RecyclerView recyclerView;
    private ListClipInfoAdapter messageAdapter;
    private DBListInfoManager DBListInfoManager;

    public MyItemTouchHelperCallBack(RecyclerView recyclerView, ListClipInfoAdapter messageAdapter, DBListInfoManager DBListInfoManager) {

        this.recyclerView = recyclerView;
        this.messageAdapter = messageAdapter;
        this.DBListInfoManager = DBListInfoManager;
        FromEval = recyclerView.getContext().getResources().getDimension(R.dimen.from_eval);
        ToEval = recyclerView.getContext().getResources().getDimension(R.dimen.to_eval);
    }


    public MyItemTouchHelperCallBack(IItemTouch mItemTouchListener) {
        this.mItemTouchListener = mItemTouchListener;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        // 拖拽的标记，这里允许上下左右四个方向
        int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        // 滑动的标记，这里允许左右滑动
        int swipeFlags = 0;
        if (MainFormActivity.IsDelete)
            swipeFlags = ItemTouchHelper.START | ItemTouchHelper.END;
        return makeMovementFlags(dragFlags, swipeFlags);

    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 移动时更改列表中对应的位置并返回true
        //只有更改了，才会调用，根本不是Move就调用
        Collections.swap(messageAdapter.getDatas(), viewHolder.getAdapterPosition(), target
                .getAdapterPosition());
//        ListData from = messageAdapter.getItemData(viewHolder.getAdapterPosition());
//        ListData to = messageAdapter.getItemData(target.getAdapterPosition());
//        ListData froma=new ListData(from);
//        froma.setOrderID(to.getOrderID());
//        ListData toa=new ListData(to);
//        toa.setOrderID(from.getOrderID());
//        listDatas.set(viewHolder.getAdapterPosition(), toa);
//        listDatas.set(target.getAdapterPosition(), froma);

        return true;
    }

    /*
            当onMove返回true时调用
         */
    @Override
    public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int
            fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {
        super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        // 移动完成后刷新列表

        messageAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        ListData from = messageAdapter.getItemData(viewHolder.getAdapterPosition());
        ListData to = messageAdapter.getItemData(target.getAdapterPosition());
        int tempTo = to.getOrderID();
        int tempFrom = from.getOrderID();
        Log.v(MainFormActivity.TAG, "交换数据from   pos=" + viewHolder.getAdapterPosition() + " 数据为：  order=" + from.getOrderID() + "  catalogue=" + from.getCatalogue() + "  message=" + from.getContent());
        Log.v(MainFormActivity.TAG, "交换数据to     pos=" + target.getAdapterPosition() + " 数据为：  order=" + to.getOrderID() + "  catalogue=" + to.getCatalogue() + "  message=" + to.getContent());


        DBListInfoManager.updateDataOrder(to.getOrderID(), -1);
        DBListInfoManager.updateDataOrder(from.getOrderID(), to.getOrderID());
        DBListInfoManager.updateDataOrder(-1, from.getOrderID());

        messageAdapter.getItemData(viewHolder.getAdapterPosition()).setOrderID(tempTo);
        messageAdapter.getItemData(target.getAdapterPosition()).setOrderID(tempFrom);

    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        // 将数据集中的数据移除
        final int pos = viewHolder.getLayoutPosition();
        ListData listDatatemp = messageAdapter.getItemData(pos);
        final ListData listData = new ListData(listDatatemp.getRemarks(), listDatatemp.getContent(), listDatatemp.getCreateDate(), listDatatemp.getOrderID(), listDatatemp.getCatalogue());

        messageAdapter.deleteItem(pos);
        DBListInfoManager.deleteDataByOrderID(listData.getOrderID());
        Snackbar.make(recyclerView, "确定删除？", Snackbar.LENGTH_LONG).setAction("撤销", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messageAdapter.addItem(listData, pos);

                DBListInfoManager.cancelDelete(listData.getRemarks(), listData.getContent(), listData.getCreateDate(), listData.getOrderID(), listData.getCatalogue());

            }
        }).setDuration(Snackbar.LENGTH_LONG).show();
    }

    /*
           这个方法会在某个Item被拖动和移动的时候回调，这里我们用来播放动画，当viewHolder不为空时为选中状态
           否则为释放状态
        */
    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        super.onSelectedChanged(viewHolder, actionState);
        if (viewHolder != null) {
                vh = viewHolder;
                pickUpAnimation((ClipInfoViewHolder) viewHolder);
            } else {if (vh != null) {
                putDownAnimation((ClipInfoViewHolder) vh);
            }
        }
    }

    private void pickUpAnimation(ClipInfoViewHolder view) {
//        view.mCardView.setCardBackgroundColor(Color.parseColor("#37000000"));

        ObjectAnimator animator = ObjectAnimator.ofFloat(view.mCardView, "cardElevation", FromEval, ToEval);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    private void putDownAnimation(ClipInfoViewHolder view) {
//        view.mCardView.setCardBackgroundColor(Color.parseColor("#FFFAFAFA"));
        ObjectAnimator animator = ObjectAnimator.ofFloat(view.mCardView, "cardElevation", ToEval, FromEval);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(300);
        animator.start();
    }

    public interface IItemTouch {
        void onItemMove(int fromPosition, int toPosition);

        void onItemDismiss(int position);
    }

}
