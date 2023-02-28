package com.huanglongyu.ToDoList.adapter;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DataLoader;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.ToDoList.util.Utils;
import com.huanglongyu.todolist.R;

public class TestCursorAdapter extends WapperCusorAdapter implements View.OnTouchListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "TestCursorAdapter";
    private LayoutInflater mInflater;
    private EditText listenedEditText;
    private DataAccess.DataObserverListener mDataObserverListener;
    public static final int ITEM_VIEW_TAG = 1;
    private static final int LOADER_ID = 0;
    private Cursor mCursor;
    private Context mContext;
    private DataAccess mDataAccess;

    public TestCursorAdapter(Context context, DataAccess access) {
        super(context, access.getAll());
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataAccess = access;
        mCursor = mDataAccess.getAll();
        ((Activity) context).getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    public void setDataObserverListener(DataAccess.DataObserverListener l) {
        mDataObserverListener = l;
    }

    public int getFirstDoneDataPosition() {
        int oldPosition = mCursor.getPosition();
        int count = mCursor.getCount();
        for (int i = 0; i < count; i++) {
            mCursor.moveToPosition(i);
            int isDone = mCursor.getInt(mCursor.getColumnIndex(DbHelper.DONE));
            Log.i(TAG, "oldPosition: " + oldPosition + " count: " + count + " isDone:" + isDone);
            if (isDone == DbHelper.ITEM_DONE) {
                mCursor.moveToPosition(oldPosition);
                return i;
            }
        }
        return -1;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        if (loaderID == LOADER_ID) {
            return new DataLoader(mContext, mDataAccess);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
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
    public long getItemId(int position) {
        Cursor c = (Cursor) getItem(position);
        String content = c.getString(c.getColumnIndex(DbHelper.CONTENT));
        int id = c.getInt(c.getColumnIndex(DbHelper.ID));
        int time = c.getInt(c.getColumnIndex(DbHelper.TIME_STAMP));
        int color = c.getInt(c.getColumnIndex(DbHelper.COLOR));
        long hascode = content.hashCode();
        Log.i(TAG, "getItemId:" + hascode + " position:" + position + " content:" + content +
                " id:" + id + " time:" + time + " color:" + color);
        return hascode;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view instanceof EditText) {
            ((ViewGroup) view.getParent()).setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);
            if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                listenedEditText = (EditText) view;
//                listenedEditText.setOnEditorActionListener(editorActionListener);
            }
        } else if (view instanceof ViewGroup) {
            ((ViewGroup) view).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
            if (listenedEditText != null) {
//                listenedEditText.setOnEditorActionListener(null);
//                listenedEditText.removeTextChangedListener(textWatcher);
                int id = (Integer) listenedEditText.getTag();
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
        if (isDone == DbHelper.ITEM_DONE) {
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
        return view;
    }

    @Override
    public void swapItems(int positionOne, int positionTwo) {
        if (mCursor != null) {
            mCursor.moveToPosition(positionOne);
            int oneRowId = mCursor.getInt(mCursor.getColumnIndex(DbHelper.ID));
            String oneContent = mCursor.getString(mCursor.getColumnIndex(DbHelper.CONTENT));
            ContentValues one = getCurrentValues(mCursor);

            mCursor.moveToPosition(positionTwo);
            int towRowId = mCursor.getInt(mCursor.getColumnIndex(DbHelper.ID));
            String twoContent = mCursor.getString(mCursor.getColumnIndex(DbHelper.CONTENT));
            ContentValues two = getCurrentValues(mCursor);

            Log.i("swapItems", "oneRowId:" + oneRowId + " oneContent:" + oneContent +
                    " towRowId:" + towRowId + " twoContent:" + twoContent + " positionOne:" + positionOne +
                    " positionTwo:" + positionTwo);
            mDataAccess.updateItemAll(towRowId, one);
            swapCursor(mDataAccess.getAll());
            mDataAccess.updateItemAll(oneRowId, two);
            swapCursor(mDataAccess.getAll());
            mCursor = mDataAccess.getAll();
        }
    }




    private ContentValues getCurrentValues(Cursor c) {
        if (c != null) {
            ContentValues values = new ContentValues();
            values.put(DbHelper.DONE, c.getInt(c.getColumnIndex(DbHelper.DONE)));
//            values.put(DbHelper.TIME_STAMP, c.getInt(c.getColumnIndex(DbHelper.TIME_STAMP)));
            values.put(DbHelper.COLOR, c.getInt(c.getColumnIndex(DbHelper.COLOR)));
            values.put(DbHelper.CONTENT, c.getString(c.getColumnIndex(DbHelper.CONTENT)));
            return values;
        }
        return null;
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
