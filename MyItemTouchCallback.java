package com.ly.core.listener;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ly.core.R;
import com.ly.core.commons.FusionCode;

/**
 * 滑动回调
 * Created by fzJiang on 2016-5-25.
 */
public class MyItemTouchCallback extends ItemTouchHelper.Callback {

    private static final String TAG = "MyItemTouchCallback";

    private ItemTouchInterface mItemTouchInterface;

    private Handler mHandler;

    private TextView menuDelete;

    private int actionStates;

    public MyItemTouchCallback(ItemTouchInterface itemTouchInterface, Handler handler) {

        this.mItemTouchInterface = itemTouchInterface;
        this.mHandler = handler;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return false;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        if (recyclerView.getLayoutManager() instanceof GridLayoutManager) {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT;
            final int swipeFlags = ItemTouchHelper.LEFT;
            return makeMovementFlags(dragFlags, swipeFlags);
        } else {
            final int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
            final int swipeFlags = 0;
            return makeMovementFlags(dragFlags, swipeFlags);
        }
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();//得到拖动ViewHolder的position
        int toPosition = target.getAdapterPosition();//得到目标ViewHolder的position
        Log.d("MyItemTouchCallback", "onMove:fromPosition=" + fromPosition + ",toPosition=" + toPosition);
        mItemTouchInterface.onMove(fromPosition, toPosition);
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();//得到删除ViewHolder的position
        Log.d("MyItemTouchCallback", "onSwiped:position=" + position);
        mItemTouchInterface.onSwiped(position);//删除
        if (onDragListener != null) {
            onDragListener.onFinishSwiped();
        }
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //滑动时改变Item的透明度
            final float alpha = 1 - Math.abs(dX) / (float) viewHolder.itemView.getWidth();
            viewHolder.itemView.setAlpha(alpha);
            viewHolder.itemView.setTranslationX(dX);
        } else {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        }
    }

    @Override
    public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
        //拖动时改变颜色
        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder.itemView.setAlpha(0.6f);
            viewHolder.itemView.setBackgroundColor(Color.LTGRAY);
            //
            // menuDelete = (TextView) viewHolder.itemView.findViewById(R.id.menu_delete);
            //menuDelete.setVisibility(View.VISIBLE);

        } else if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
            //获取相应菜单的子布局
            TextView menu_icon = (TextView) viewHolder.itemView.findViewById(R.id.menu_icon);
            TextView menu_text = (TextView) viewHolder.itemView.findViewById(R.id.menu_text);
            //滑动时改变布局（删除垃圾桶）,同时提示用户继续向右滑动即可删除item
            menu_icon.setVisibility(View.GONE);
            menu_text.setVisibility(View.GONE);
            viewHolder.itemView.setBackgroundResource(R.mipmap.deletes);
            mHandler.sendEmptyMessage(FusionCode.Home.DELETE_MENU);
        }
        super.onSelectedChanged(viewHolder, actionState);
    }

    @Override
    public void clearView(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        super.clearView(recyclerView, viewHolder);
        viewHolder.itemView.setAlpha(1.0f);
        viewHolder.itemView.setBackgroundColor(Color.parseColor("#FAFBFE"));
        //获取相应菜单的子布局
        TextView menu_icon = (TextView) viewHolder.itemView.findViewById(R.id.menu_icon);
        TextView menu_text = (TextView) viewHolder.itemView.findViewById(R.id.menu_text);
        //还原布局
        // menuDelete.setVisibility(View.INVISIBLE);
        menu_icon.setVisibility(View.VISIBLE);
        menu_text.setVisibility(View.VISIBLE);

        if (onDragListener != null) {
            onDragListener.onFinishDrag();
        }
    }

    private Drawable background = null;
    private int bkcolor = -1;

    private OnDragListener onDragListener;

    public MyItemTouchCallback setOnDragListener(OnDragListener onDragListener) {
        this.onDragListener = onDragListener;
        return this;
    }

    public interface OnDragListener {
        void onFinishDrag();

        void onFinishSwiped();
    }

    public interface ItemTouchInterface {
        void onMove(final int fromPosition, final int toPosition);

        void onSwiped(int position);
    }
}
