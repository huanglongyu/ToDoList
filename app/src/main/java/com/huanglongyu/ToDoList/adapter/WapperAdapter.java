package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.util.DisplayMetrics;
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
//            convertView = InflateItemView(convertView, position, parent);
        } else {
            bindItemView(convertView.getTag(R.id.TAG_HODLER_ID), mContext, position);
        }
        return convertView;
    }
    
    private View InflateContent(View convertView, int position, ViewGroup parent) {
        View itemContent = InflateItemView(convertView, position, parent);
        if (itemContent == null) {
            throw new IllegalStateException("the itemView can't be null!");
        }
        DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
        int w_screen = dm.widthPixels;
        LayoutParams lp = new LayoutParams(w_screen, ViewGroup.LayoutParams.WRAP_CONTENT);
        itemContent.setLayoutParams(lp);
        // itemLayout indicates the outermost layout of the new view.
        LinearLayout itemLayout = new LinearLayout(mContext);
        itemLayout.setTag(R.id.TAG_HODLER_ID, itemContent.getTag());
        itemLayout.setOrientation(LinearLayout.HORIZONTAL);
//        itemLayout.setLayoutParams(new AbsListView.LayoutParams(
//                itemContent.getLayoutParams().width, itemContent.getLayoutParams().height));
//        itemLayout.addView(itemContent);
        
        View swipeLeftView = InflateLeftView(mContext, position, parent);
        if (swipeLeftView != null) {
            LayoutParams cookLp = (LayoutParams) swipeLeftView.getLayoutParams();
            cookLp.setMarginStart(-cookLp.width);
            swipeLeftView.setLayoutParams(cookLp);
            swipeLeftView.setTag(ToDoListView.ITEM_CONTENT_LEFT_TAG);
            itemLayout.addView(swipeLeftView);
        }
        itemLayout.addView(itemContent);

        View swipeRightView = InflateRightView(mContext, position, parent);
        if (swipeRightView != null) {
            swipeRightView.setTag(ToDoListView.ITEM_CONTENT_RIGHT_TAG);
            itemLayout.addView(swipeRightView);
        }
        return itemLayout;
    }

    protected abstract View InflateItemView(View convertView, int position, ViewGroup parent);

    protected abstract View InflateLeftView(Context context, int position, ViewGroup parent);

    protected abstract View InflateRightView(Context context, int position, ViewGroup parent);
    
    protected abstract void bindItemView(Object object, Context context, int position);

}
