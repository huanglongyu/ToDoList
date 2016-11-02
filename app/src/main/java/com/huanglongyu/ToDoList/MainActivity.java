package com.huanglongyu.ToDoList;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.adapter.ToDoListAdapter;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DataLoader;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.ToDoList.util.Utils;
import com.huanglongyu.ToDoList.view.PreImeRelativeLayout;
import com.huanglongyu.ToDoList.view.ToDoListView;


public class MainActivity extends Activity implements OnItemClickListener,ToDoListView.OnToDoListViewTriggerListener,LoaderManager.LoaderCallbacks<Cursor> ,
        DataAccess.DataObserverListener {

    private ToDoListView mToDoListView;
    private ToDoListAdapter mToDoListAdapter;
    private PreImeRelativeLayout mPreImeRelativeLayout;

    private DataAccess mDataAccess;
    private TestCursorAdapter mTestCursorAdapter;
    private static final boolean USE_CURSOR = true;
    private static final int LOADER_ID = 0;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        mToDoListView = (ToDoListView) findViewById(R.id.waterdrop_listview);
//        list.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mToDoListAdapter = new ToDoListAdapter(this);
        mToDoListView.setOnItemClickListener(this);
        mToDoListView.setOnToDoListViewTriggerListener(this);

        mPreImeRelativeLayout = (PreImeRelativeLayout)findViewById(R.id.parent);
        mPreImeRelativeLayout.setToDoListView(mToDoListView);

        mDataAccess = new DataAccess(this);
        mDataAccess.openDb();
//        mDataAccess.clearDb();
        mTestCursorAdapter = new TestCursorAdapter(this,mDataAccess.getAll());
        mTestCursorAdapter.setDataObserverListener(this);
        if (USE_CURSOR) {
            mToDoListView.setAdapter(mTestCursorAdapter);
        } else {
            mToDoListView.setAdapter(mToDoListAdapter);
        }
//        getLoaderManager().initLoader(LOADER_ID, null, this).forceLoad();
        getLoaderManager().initLoader(LOADER_ID, null, this);
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
    public void onDownTriggered() {
        Logger.i("onDownTriggered");
        mToDoListView.setHeaderFocus(true);
    }

    @Override
    public void onUpTriggered() {
        Logger.i("onUpTriggered");
        mToDoListView.setHeaderFocus(false);
    }

    @Override
    public void onNewTaskCancelTriggered() {
        Logger.i("onNewTaskCancelTriggered");
        mToDoListView.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        mToDoListView.HeadrollBack();
    }

    @Override
    public void onNewTaskAddedTriggered(String item) {
        Logger.i("onNewTaskAddedTriggered");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        if (loaderID == LOADER_ID) {
            return new DataLoader(this, mDataAccess);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mTestCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {}

    public boolean onQueryTextChanged() {
        // Called when the action bar search text has changed.  Update
        // the search filter, and restart the loader to do a new query
        // with this filter.
        getLoaderManager().restartLoader(LOADER_ID, null, this).forceLoad();
        return true;
    }

    private void listDataChanged() {
        //this is bad choice, need to optimize!
        mTestCursorAdapter.swapCursor(mDataAccess.getAll());
    }

    @Override
    public void onContendUpdate(int id, String newcontent) {
        mDataAccess.updateItemContent(id, newcontent);
        listDataChanged();
    }

    @Override
    public void onDissMiss(int dismissPosition) {
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         View v = view.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG);
         Utils.updateCurrentBackground(mTestCursorAdapter, mDataAccess, position -1, v);
         listDataChanged();
    }

}
