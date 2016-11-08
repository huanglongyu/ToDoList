package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.huanglongyu.ToDoList.R;

import java.util.ArrayList;
import java.util.List;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
//use CursorAdapter instead of 

public class ToDoListAdapter extends WapperAdapter implements View.OnTouchListener{


    private LayoutInflater mInflater = null;
    private List<String> data = new ArrayList<String>();
    private static final String TAG = "ToDoListAdapter";

    public ToDoListAdapter(Context context) {
        super(context);
        this.mInflater = LayoutInflater.from(context);
        data.add("And eternity in an hour0.");
        data.add("And eternity in an hour1.");
        data.add("And eternity in an hour2.");
        data.add("And eternity in an hour3.");
        data.add("And eternity in an hour4.");
        data.add("And eternity in an hour5.");
        data.add("And eternity in an hour6.");
        data.add("And eternity in an hour7.");
        data.add("And eternity in an hour8.");
        data.add("And eternity in an hour9.");
        data.add("And eternity in an hour10.");
    }

    public void remove(int position) {
        data.remove(position);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
//        ViewHolder vh = (ViewHolder) view.getTag();
//        vh.row is the convertView in getView or you may call it the row item itself
//        ((ViewGroup)vh.view).setDescendantFocusability(view instanceof EditText?ViewGroup.FOCUS_AFTER_DESCENDANTS:ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        if(view instanceof EditText){
            ((ViewGroup)view.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
        }else if(view instanceof ViewGroup){
            ((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        }
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public String getItem(int position) {
//        return position;
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
//        Log.i("TestCursorAdapter", "getItemId:" + (position + 1000));
//        return position + 1000;
        Log.i(TAG, "getItemId:" + position + " value:" + getItem(position).hashCode());
        return getItem(position).hashCode();
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        String firstItem = data.set(positionOne, getItem(positionTwo));
        notifyDataSetChanged();
        data.set(positionTwo, firstItem);
    }

    //    @Override
//    public View getView(int position, View convertView, ViewGroup parent) {
//        ViewHolder holder = null;
//        if (convertView == null) {
//            holder = new ViewHolder();
//            convertView = mInflater.inflate(R.layout.todo_list_item, null);
//            holder.info = (TextView) convertView.findViewById(R.id.item_info);
////            holder.view = convertView;
//            holder.info.setOnTouchListener(this);
//            convertView.setOnTouchListener(this);
//            convertView.setTag(holder);
//        } else {
//            holder = (ViewHolder) convertView.getTag();
//        }
//        holder.info.setText((String) data.get(position));
//        return convertView;
//    }

    static class ViewHolder{
        public TextView info;
//        public View view;
    }

    @Override
    protected View InflateItemView(View convertView, int position,
            ViewGroup parent) {
//        ViewHolder holder = new ViewHolder();
//        View view = mInflater.inflate(R.layout.todo_list_item, null);
//        holder.info = (TextView) view.findViewById(R.id.item_info);
//        // holder.view = convertView;
//        holder.info.setOnTouchListener(this);
//        view.setOnTouchListener(this);
//        view.setTag(holder);
//        holder.info.setText((String) data.get(position));
//        return view;
        ViewHolder holder = new ViewHolder();
        View view = mInflater.inflate(R.layout.todo_list_item, null);
        holder.info = (TextView) view.findViewById(R.id.item_info);
        // holder.view = convertView;
        holder.info.setOnTouchListener(this);
        view.setOnTouchListener(this);
        view.setTag(holder);
        EditText et = (EditText) view.findViewById(R.id.item_info);
        et.setOnTouchListener(this);
        et.setImeOptions(EditorInfo.IME_ACTION_SEND);
        holder.info.setText((String) data.get(position));
        return view;
    }

    @Override
    protected void bindItemView(Object object, Context context, int position) {
        if (object instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder) object;
            holder.info.setText((String) data.get(position));
        }
    }

    @Override
    protected View InflateLeftView(Context context, int position,
            ViewGroup parent) {
        ImageView view = new ImageView(context);
        int w = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_size);
        LayoutParams leftLp = new LayoutParams(w, w);
        view.setImageResource(R.drawable.ic_done);
        view.setLayoutParams(leftLp);
        return view;
    }

    @Override
    protected View InflateRightView(Context context, int position,
            ViewGroup parent) {
        ImageView view = new ImageView(context);
        int w = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_size);
        LayoutParams rightLp = new LayoutParams(w, w);
        view.setImageResource(R.drawable.ic_clear);
        view.setLayoutParams(rightLp);
        return view;
    }
}
