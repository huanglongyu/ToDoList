package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.view.ToDoListView;

public abstract class WapperAdapter extends BaseAdapter{
    protected Context mContext;

    public WapperAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = InflateContent(convertView, position, parent);
        }
        bindItemView(convertView.getTag(R.id.TAG_VIEW_ID), mContext, position);
        return convertView;
    }
    
    private View InflateContent(View convertView, int position, ViewGroup parent) {
        View swipeCenterView = InflateItemView(convertView, position, parent);
        if (swipeCenterView == null) {
            throw new IllegalStateException("the itemView can't be null!");
        }

        int margin = mContext.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_margin);

        //first. set user's layout be full of screen
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels - margin * 2;
        LayoutParams lp = new LayoutParams(w_screen, ViewGroup.LayoutParams.WRAP_CONTENT);
        swipeCenterView.setLayoutParams(lp);


        LinearLayout itemLayout = new LinearLayout(mContext);
        itemLayout.setTag(R.id.TAG_VIEW_ID, swipeCenterView);
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setBackgroundColor(0xffffff);

        //second. add left view
        View swipeLeftView = InflateLeftView(mContext, position, parent);
        if (swipeLeftView != null) {
            LayoutParams cookLp = (LayoutParams) swipeLeftView.getLayoutParams();
            cookLp.leftMargin = -cookLp.width;
            cookLp.rightMargin = margin;
            swipeLeftView.setLayoutParams(cookLp);
            swipeLeftView.setTag(ToDoListView.ITEM_CONTENT_LEFT_TAG);
            itemLayout.addView(swipeLeftView);
        }

        //third. add middle view
        itemLayout.addView(swipeCenterView);

        //four. add right view
        View swipeRightView = InflateRightView(mContext, position, parent);
        if (swipeRightView != null) {
            LayoutParams cookRp = (LayoutParams) swipeRightView.getLayoutParams();
            cookRp.leftMargin = margin;
            swipeRightView.setLayoutParams(cookRp);
            swipeRightView.setTag(ToDoListView.ITEM_CONTENT_RIGHT_TAG);
            itemLayout.addView(swipeRightView);
        }
        return itemLayout;
    }

    protected abstract View InflateItemView(View convertView, int position, ViewGroup parent);

    protected abstract View InflateLeftView(Context context, int position, ViewGroup parent);

    protected abstract View InflateRightView(Context context, int position, ViewGroup parent);
    
    protected abstract void bindItemView(Object object, Context context, int position);

    public abstract void swapItems(int positionOne, int positionTwo);

}
