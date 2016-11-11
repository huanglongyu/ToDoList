package com.huanglongyu.ToDoList;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.RelativeLayout;

import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.adapter.ToDoListAdapter;
import com.huanglongyu.ToDoList.bean.ToDoitem;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.ToDoList.util.ThreadUtil;
import com.huanglongyu.ToDoList.util.Utils;
import com.huanglongyu.ToDoList.view.OnItemMovedListener;
import com.huanglongyu.ToDoList.view.PreImeRelativeLayout;
import com.huanglongyu.ToDoList.view.ToDoListView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements OnItemClickListener, ToDoListView.OnToDoListViewTriggerListener,
        DataAccess.DataObserverListener, View.OnClickListener, AdapterView.OnItemLongClickListener, OnItemMovedListener {

    private ToDoListView mToDoListView;
    private ToDoListAdapter mToDoListAdapter;
    private PreImeRelativeLayout mPreImeRelativeLayout;
    private View dimView;

    private DataAccess mDataAccess;
    private TestCursorAdapter mTestCursorAdapter;
    public static final boolean USE_CURSOR = false;
    private static final String TAG = "MainActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        dimView = findViewById(R.id.full_dim);
        dimView.setOnClickListener(this);

        mToDoListView = (ToDoListView) findViewById(R.id.listview);
//        mToDoListView.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mToDoListView.setOnItemClickListener(this);
        mToDoListView.setOnToDoListViewTriggerListener(this);
        mToDoListView.setOnItemLongClickListener(this);
        mToDoListView.setOnItemMovedListener(this);

        mPreImeRelativeLayout = (PreImeRelativeLayout)findViewById(R.id.parent);
        mPreImeRelativeLayout.setToDoListView(mToDoListView);

        mDataAccess = new DataAccess(this);
        mDataAccess.openDb();
//        mDataAccess.clearDb();

        if (USE_CURSOR) {
            mTestCursorAdapter = new TestCursorAdapter(this, mDataAccess);
            mTestCursorAdapter.setDataObserverListener(this);
            mToDoListView.setAdapter(mTestCursorAdapter);
        } else {
            mToDoListAdapter = new ToDoListAdapter(this, mDataAccess);
            mToDoListView.setAdapter(mToDoListAdapter);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDataAccess.closeDb();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onHeaderInitFinished(int height) {
        View view = findViewById(R.id.title);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int w = dm.widthPixels;
        int h = dm.heightPixels - view.getHeight() - height;
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)dimView.getLayoutParams();
        params.width = w;
        params.height = h ;
        params.topMargin = height + view.getHeight();
        dimView.setLayoutParams(params);
    }

    @Override
    public void onItemMoved(final int originalPosition, final int newPosition) {
        final List list = mToDoListAdapter.getSubData(originalPosition, newPosition);
        Log.i(TAG, "onItemMoved originalPosition:" + originalPosition + " newPosition:" + newPosition +
                " size:" + list.size());
//        for (int i = 0; i < list.size(); i++) {
//            ToDoitem item = (ToDoitem)list.get(i);
//            Log.i(TAG, "MoveItem:" + item.getContent());
//        }

        ThreadUtil.runOnBackground(new Runnable() {
            @Override
            public void run() {
                mDataAccess.updateSubItems(list, originalPosition, newPosition);
            }
        });
    }

    @Override
    public void onDownTriggered() {
        Log.i(TAG , "onDownTriggered");
        mToDoListView.setHeaderFocus(true);
        dimView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onUpTriggered() {
        Logger.i("onUpTriggered");
        mToDoListView.setHeaderFocus(false);
        dimView.setVisibility(View.GONE);
    }

    @Override
    public void onNewTaskCancelTriggered() {
        Logger.i("onNewTaskCancelTriggered");
        dimView.setVisibility(View.GONE);
        mToDoListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        mToDoListView.HeadrollBack();
    }

    @Override
    public void onNewTaskAddedTriggered(final String content) {
        Logger.i("onNewTaskAddedTriggered " + content);
        dimView.setVisibility(View.GONE);
        mToDoListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        mToDoListView.addNewItem(new ToDoListView.newItemAniamation() {
            @Override
            public void end() {
                mDataAccess.addItem(content);
                listDataChanged();
            }
        });
    }

    private void listDataChanged() {
        if (USE_CURSOR) {
            //this is bad choice, need to optimize!
            mTestCursorAdapter.swapCursor(mDataAccess.getAll());
        } else {
            mToDoListAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public void onContendUpdate(int id, String newcontent) {
        mDataAccess.updateItemContent(id, newcontent);
        listDataChanged();
    }

    @Override
    public void onTaskClear(int dismissPosition) {
        if (USE_CURSOR) {
            Cursor c = (Cursor)mTestCursorAdapter.getItem(dismissPosition);
            mDataAccess.removeItem(c.getInt(c.getColumnIndex(DbHelper.ID)));
            listDataChanged();
        } else {
            mToDoListAdapter.remove(dismissPosition);
        }
    }

    @Override
    public void onToggleDone(int donePosition) {
        Logger.i("onDone:" + donePosition);
        if (USE_CURSOR) {
            Cursor c = (Cursor)mTestCursorAdapter.getItem(donePosition);
            int oldValue = c.getInt(c.getColumnIndex(DbHelper.DONE));
            int newValue;
            if (oldValue == DbHelper.ITEM_DONE) {
                newValue = DbHelper.ITEM_NOT_DONE;
            } else {
                newValue = DbHelper.ITEM_DONE;
            }
            mDataAccess.updateItemIsDone(c.getInt(c.getColumnIndex(DbHelper.ID)), newValue);
            listDataChanged();
        } else {
            ToDoitem item = (ToDoitem)mToDoListAdapter.getItem(donePosition);
            int oldValue = item.getIsDone();
            int newValue;
            if (oldValue == DbHelper.ITEM_DONE) {
                newValue = DbHelper.ITEM_NOT_DONE;
            } else {
                newValue = DbHelper.ITEM_DONE;
            }
            item.setIsDone(newValue);
            mDataAccess.updateItemIsDone(item.getId(), newValue);
            mToDoListAdapter.updateItem(donePosition, item);
        }


    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         View v = view.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG);
         Utils.updateCurrentBackground(this, mTestCursorAdapter, mDataAccess, position -1, v);
         listDataChanged();
    }

    @Override
    public boolean onItemLongClick(final AdapterView<?> parent, final View view,
                                   final int position, final long id) {
        mToDoListView.startDragging(position);
        //do not send the event to onItemClick
        return true;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.full_dim:
                Log.i(TAG, "dim view clicked");
                onNewTaskCancelTriggered();
                break;
        }
    }
}
