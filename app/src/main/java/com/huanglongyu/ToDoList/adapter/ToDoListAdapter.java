package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.huanglongyu.ToDoList.R;

import java.util.ArrayList;
import java.util.List;
//use CursorAdapter instead of 

public class ToDoListAdapter extends WapperAdapter implements View.OnTouchListener{


    private LayoutInflater mInflater = null;
    private List<String> data = new ArrayList<String>();

    public ToDoListAdapter(Context context) {
        super(context);
        this.mInflater = LayoutInflater.from(context);
        data.add("To see a world in a grain of sand,");
        data.add("And a heaven in a wild flower,");
        data.add("Hold infinity in the palm of your hand,");
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
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
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
        ViewHolder holder = new ViewHolder();
        View view = mInflater.inflate(R.layout.todo_list_item, null);
        holder.info = (TextView) view.findViewById(R.id.item_info);
        // holder.view = convertView;
        holder.info.setOnTouchListener(this);
        view.setOnTouchListener(this);
        view.setTag(holder);
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
        ImageView left = new ImageView(context);
        LayoutParams leftLp = new LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        left.setBackgroundResource(R.drawable.ic_launcher);
        left.setLayoutParams(leftLp);
        return left;
    }

    @Override
    protected View InflateRightView(Context context, int position,
            ViewGroup parent) {
        ImageView right = new ImageView(context);
        LayoutParams rightLp = new LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
        right.setBackgroundResource(R.drawable.ic_launcher);
        right.setLayoutParams(rightLp);
        return right;
    }
}
