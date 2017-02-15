package com.example.yanghang.myapplication.ListPackage.CatalogueList;

import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.yanghang.myapplication.R;

import java.util.Collections;
import java.util.List;

/**
 * Created by yanghang on 2016/12/3.
 */

public class CatalogueAdatpter extends RecyclerView.Adapter<CatalogueHolder> implements SimpleItemTouchHelperCallback.ItemTouchHelperAdapter {
    List<String> mDatas;
    Context context;
    LayoutInflater inflater;
    private OnStartDragListener mDragStartListener;
    private OnItemClickListener mItemClickListener;

    public CatalogueAdatpter(List<String> mDatas, Context context) {
        this.mDatas = mDatas;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public List<String> getDatas() {
        return mDatas;
    }

    public String getItem(int pos) {
        return mDatas.get(pos);
    }

    public void setDragStartListener(OnStartDragListener onStartDragListener) {
        if (onStartDragListener != null)
            this.mDragStartListener = onStartDragListener;
    }

    @Override
    public CatalogueHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.item_catalogue_left_drawer, parent, false);
        CatalogueHolder catalogueHolder = new CatalogueHolder(view);
        return catalogueHolder;
    }

    @Override
    public void onBindViewHolder(final CatalogueHolder holder, int position) {
        holder.tvCatalogue.setText(mDatas.get(position));
        holder.dragImage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (MotionEventCompat.getActionMasked(event) ==
                        MotionEvent.ACTION_DOWN) {
                    mDragStartListener.onStartDrag(holder);
                }
                return false;
            }
        });
        holder.tvCatalogue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.OnItemClick(v, holder.getAdapterPosition());
            }
        });
        holder.tvCatalogue.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return mItemClickListener.OnItemLongClick(v, holder.getAdapterPosition());
            }
        });

    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        if (listener != null) {
            mItemClickListener = listener;
        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    @Override
    public void onItemDismiss(int position) {
        mDatas.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        Collections.swap(mDatas, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    public interface OnItemClickListener {
        void OnItemClick(View v, int position);

        boolean OnItemLongClick(View v, int position);
    }


    public interface OnStartDragListener {
        /**
         * Called when a view is requesting a start of a drag.
         *
         * @param viewHolder The holder of the view to drag.
         */
        void onStartDrag(RecyclerView.ViewHolder viewHolder);

    }

    public interface ItemTouchHelperViewHolder {
        /**
         * Called when the {@link ItemTouchHelper} first registers an item as being moved or swiped.
         * Implementations should update the item view to indicate it's active state.
         */
        void onItemSelected();


        /**
         * Called when the {@link ItemTouchHelper} has completed the move or swipe, and the active item
         * state should be cleared.
         */
        void onItemClear();
    }

}

class CatalogueHolder extends RecyclerView.ViewHolder implements
        CatalogueAdatpter.ItemTouchHelperViewHolder {

    TextView tvCatalogue;
    ImageView dragImage;
    CardView cardView;


    public CatalogueHolder(View itemView) {
        super(itemView);
        tvCatalogue = (TextView) itemView.findViewById(R.id.tv_catalogue_recycler);
        dragImage = (ImageView) itemView.findViewById(R.id.drag_image);
        cardView = (CardView) itemView.findViewById(R.id.catalogueCardView);


    }

    @Override
    public void onItemSelected() {
//        itemView.setBackgroundColor(Color.LTGRAY);
    }

    @Override
    public void onItemClear() {
//        itemView.setBackgroundColor(0);
//        cardView.setBackgroundColor(Color.parseColor("#227b7474"));
    }
}


