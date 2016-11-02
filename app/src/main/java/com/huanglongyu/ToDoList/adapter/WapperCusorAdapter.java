package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.view.ToDoListView;

public abstract class WapperCusorAdapter extends CursorAdapter{

    public WapperCusorAdapter(Context context, Cursor c) {
        super(context, c);
    }

//    @Override
//    public void bindView(View arg0, Context arg1, Cursor arg2) {
//
//    }

    @Override
    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
        return InflateContent(arg0,arg1,arg2);
    }

    private View InflateContent(Context context, Cursor arg1, ViewGroup arg2) {
        View itemContent = InflateItemView(context, arg1, arg2);
        if (itemContent == null) {
            throw new IllegalStateException("the itemView can't be null!");
        }
        //first. set user's layout be full of screen
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        LayoutParams lp = new LayoutParams(w_screen, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemContent.setLayoutParams(lp);

        LinearLayout itemLayout = new LinearLayout(context);
        itemLayout.setTag(R.id.TAG_HODLER_ID, itemContent.getTag());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);

        //second. add left view
        View swipeLeftView = InflateLeftView(context, arg1, arg2);
        if (swipeLeftView != null) {
            LayoutParams cookLp = (LayoutParams) swipeLeftView.getLayoutParams();
            cookLp.setMarginStart(-cookLp.width);
            swipeLeftView.setLayoutParams(cookLp);
            swipeLeftView.setTag(ToDoListView.ITEM_CONTENT_LEFT_TAG);
            itemLayout.addView(swipeLeftView);
        }
        //third. add middle view
        itemLayout.addView(itemContent);

        //four. add right view
        View swipeRightView = InflateRightView(context, arg1, arg2);
        if (swipeRightView != null) {
            swipeRightView.setTag(ToDoListView.ITEM_CONTENT_RIGHT_TAG);
            itemLayout.addView(swipeRightView);
        }

        return itemLayout;
    }

    protected abstract View InflateItemView(Context arg0, Cursor arg1, ViewGroup arg2);

    protected abstract View InflateLeftView(Context arg0, Cursor arg1, ViewGroup arg2);

    protected abstract View InflateRightView(Context arg0, Cursor arg1, ViewGroup arg2);
}
