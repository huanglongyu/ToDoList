package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.view.ToDoListView;

public abstract class WapperCusorAdapter extends CursorAdapter {
    private static final String TAG = "WapperCusorAdapter";

    public WapperCusorAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return InflateContent(context, cursor, viewGroup);
    }

    private View InflateContent(Context context, Cursor cursor, ViewGroup viewGroup) {
        View swipeCenterView = InflateItemView(context, cursor, viewGroup);
        if (swipeCenterView == null) {
            throw new IllegalStateException("the itemView can't be null!");
        }

        int margin = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_margin);

        //first. set user's layout be full of screen
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels - margin * 2;
        LayoutParams lp = new LayoutParams(w_screen, ViewGroup.LayoutParams.WRAP_CONTENT);
        swipeCenterView.setLayoutParams(lp);

        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setTag(R.id.TAG_HODLER_ID, swipeCenterView.getTag());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
        itemLayout.setGravity(Gravity.CENTER_VERTICAL);
        itemLayout.setBackgroundColor(0xffffff);

        //second. add left view
        View swipeLeftView = InflateLeftView(context, cursor, viewGroup);
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
        View swipeRightView = InflateRightView(context, cursor, viewGroup);
        if (swipeRightView != null) {
            LayoutParams cookRp = (LayoutParams) swipeRightView.getLayoutParams();
            cookRp.leftMargin = margin;
            swipeRightView.setLayoutParams(cookRp);
            swipeRightView.setTag(ToDoListView.ITEM_CONTENT_RIGHT_TAG);
            itemLayout.addView(swipeRightView);
        }

        return itemLayout;
    }

    protected abstract View InflateItemView(Context arg0, Cursor arg1, ViewGroup arg2);

    protected abstract View InflateLeftView(Context arg0, Cursor arg1, ViewGroup arg2);

    protected abstract View InflateRightView(Context arg0, Cursor arg1, ViewGroup arg2);

    public abstract void swapItems(int positionOne, int positionTwo);
}
