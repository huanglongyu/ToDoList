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
import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.bean.ToDoitem;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Utils;

import java.util.ArrayList;
import java.util.List;

public class ToDoListAdapter extends WapperAdapter implements View.OnTouchListener{


    private LayoutInflater mInflater = null;
    private DataAccess mDataAccess;
    private ArrayList<ToDoitem> data;
    private static final String TAG = "ToDoListAdapter";

    public ToDoListAdapter(Context context, DataAccess acc) {
        super(context);
        this.mInflater = LayoutInflater.from(context);
        mDataAccess = acc;
        data = acc.getAllItems();
    }

    public int getFirstDoneDataPostion() {
        int firstDoneId = mDataAccess.getFirstDoneItemId();
        Log.i(TAG, "firstDoneId:" + firstDoneId);
        for (int i = 0; i < data.size(); i++) {
            ToDoitem item = data.get(i);
            Log.i(TAG, "item:" + item.getId());
            if (item.getId() == firstDoneId) {
                return i;
            }
        }
        return -1;
    }


    public void remove(int position) {
        ToDoitem toDoitem = data.remove(position);
        mDataAccess.removeItem(toDoitem.getId());
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
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
//        Log.i(TAG, "getItemId:" + position + " value:" + getItem(position).hashCode());
        return getItem(position).hashCode();
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        ToDoitem firstItem = data.set(positionOne, (ToDoitem) getItem(positionTwo));
        notifyDataSetChanged();
        data.set(positionTwo, firstItem);
    }

    @Override
    public void moveItems(int doneDataPosition, int firstDoneDataPostion) {
        ToDoitem doneItem = data.remove(doneDataPosition);
        notifyDataSetChanged();
        data.add(firstDoneDataPostion - 1, doneItem);
    }

    public void updateItem(int position, ToDoitem item) {
        data.set(position, item);
        notifyDataSetChanged();
    }

    public List getSubData(int from, int to) {
        return data.subList(from, to + 1);
    }

    static class ViewHolder{
        public EditText editText;
    }

    @Override
    protected View InflateItemView(View convertView, int position, ViewGroup parent) {
        ViewHolder holder = new ViewHolder();
        View view = mInflater.inflate(R.layout.todo_list_item, null);
        holder.editText = (EditText) view.findViewById(R.id.item_info);
        holder.editText.setOnTouchListener(this);
        holder.editText.setImeOptions(EditorInfo.IME_ACTION_SEND);
        view.setOnTouchListener(this);
        view.setTag(holder);
        return view;
    }

    /**
     * bind the inflated center view data
     * @param object center view
     * @param context
     * @param position
     */
    @Override
    protected void bindItemView(Object object, Context context, int position) {
        if (object instanceof View) {
            View view = (View) object;
            ViewHolder holder = (ViewHolder)view.getTag();
            ToDoitem item = data.get(position);
            holder.editText.setText(item.getContent());
            Utils.updateCurrentBackgroundWithoutSave(context, view, item);
            int isDone = item.getIsDone();
            if (isDone == DbHelper.ITEM_DONE) {
                holder.editText.getPaint().setStrikeThruText(true);
            } else {
                holder.editText.getPaint().setStrikeThruText(false);
            }
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
