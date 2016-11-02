package com.huanglongyu.ToDoList.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.ToDoList.util.Utils;

public class TestCursorAdapter extends WapperCusorAdapter implements View.OnTouchListener{
    private LayoutInflater mInflater;
    private EditText listenedEditText;
    private DataAccess.DataObserverListener mDataObserverListener;
    public static final int ITEM_VIEW_TAG = 1;

    public TestCursorAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = LayoutInflater.from(context);
    }

    public void setDataObserverListener(DataAccess.DataObserverListener l) {
        mDataObserverListener = l;
    }

//    @Override
//    public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
//        View view = mInflater.inflate(R.layout.todo_list_item, null, false); 
//        view.setOnTouchListener(this);
//        EditText et = (EditText) view.findViewById(R.id.item_info);
//        et.setOnTouchListener(this);
//        return  view;
//    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(view instanceof EditText){
            ((ViewGroup)view.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                listenedEditText = (EditText)view;
//                listenedEditText.setOnEditorActionListener(editorActionListener);
            }
        }else if(view instanceof ViewGroup){
            ((ViewGroup)view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            if (listenedEditText != null) {
//                listenedEditText.setOnEditorActionListener(null);
//                listenedEditText.removeTextChangedListener(textWatcher);
                int id = (Integer)listenedEditText.getTag();
                Logger.i("saved text:" + listenedEditText.getText() + " id:" + id);
                mDataObserverListener.onContendUpdate(id, listenedEditText.getText().toString());
                listenedEditText = null;
            }
        }
        return false;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        EditText et = (EditText) view.findViewById(R.id.item_info);
        et.setText(cursor.getString(cursor.getColumnIndex(DbHelper.CONTENT)));
        et.setTag(cursor.getInt(cursor.getColumnIndex(DbHelper.ID)));
//        et.setTag(cursor.getPosition());
        Utils.updateCurrentBackgroundWithoutSave(context, view.findViewWithTag(ITEM_VIEW_TAG), cursor);
        int isDone = cursor.getInt(cursor.getColumnIndex(DbHelper.DONE));
        Logger.i("bindView:" + cursor.getInt(cursor.getColumnIndex(DbHelper.ID)) + " isDone:" + isDone);
        if (isDone == 1) {
//            et.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
            et.getPaint().setStrikeThruText(true);
        } else {
//            int flags = et.getPaint().getFlags();
//            flags &= ~Paint.STRIKE_THRU_TEXT_FLAG;
//            et.getPaint().setFlags(flags);
            et.getPaint().setStrikeThruText(false);
        }
    }

    @Override
    protected View InflateItemView(Context context, Cursor cursor, ViewGroup viewgroup) {
        View view = mInflater.inflate(R.layout.todo_list_item, null, false);
        view.setTag(ITEM_VIEW_TAG);
        view.setOnTouchListener(this);
        EditText et = (EditText) view.findViewById(R.id.item_info);
        et.setOnTouchListener(this);
        et.setImeOptions(EditorInfo.IME_ACTION_SEND);
        return view;
    }

    @Override
    protected View InflateLeftView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ImageView view = new ImageView(context);
        int w = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_size);
        LayoutParams leftLp = new LayoutParams(w, w);
        view.setImageResource(R.drawable.ic_done);
        view.setLayoutParams(leftLp);
        return view;
    }

    @Override
    protected View InflateRightView(Context context, Cursor cursor, ViewGroup viewGroup) {
        ImageView view = new ImageView(context);
        int w = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_size);
        LayoutParams rightLp = new LayoutParams(w, w);
        view.setImageResource(R.drawable.ic_clear);
        view.setLayoutParams(rightLp);
//        View view = mInflater.inflate(R.layout.todo_list_item_extra, null, false);
//
//        ImageView left = (ImageView)view.findViewById(R.id.todo_list_item_extra_ic);
////        LayoutParams leftLp = new LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
//        left.setImageResource(R.drawable.ic_clear);
////        left.setLayoutParams(leftLp);
//
////        ImageView right = new ImageView(arg0);
////        LayoutParams rightLp = new LayoutParams(100, ViewGroup.LayoutParams.WRAP_CONTENT);
////        right.setImageResource(R.drawable.ic_clear);
////        right.setLayoutParams(rightLp);
        return view;
    }

//   private OnEditorActionListener editorActionListener = new OnEditorActionListener(){
//
//    @Override
//    public boolean onEditorAction(TextView v, int actionId,KeyEvent event) {
//         
//        return false;
//    }
//   };

}
