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
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DataLoader;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.ToDoList.util.Utils;
import com.huanglongyu.ToDoList.view.PreImeRelativeLayout;
import com.huanglongyu.ToDoList.view.ToDoListView;


public class MainActivity extends Activity implements OnItemClickListener,ToDoListView.OnToDoListViewTriggerListener,LoaderManager.LoaderCallbacks<Cursor> ,
        DataAccess.DataObserverListener, View.OnClickListener {

    private ToDoListView mToDoListView;
    private ToDoListAdapter mToDoListAdapter;
    private PreImeRelativeLayout mPreImeRelativeLayout;
    private View dimView;
    private int dimOffset;

    private DataAccess mDataAccess;
    private TestCursorAdapter mTestCursorAdapter;
    private static final boolean USE_CURSOR = true;
    private static final int LOADER_ID = 0;
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
    public void onNewTaskAddedTriggered(String item) {
        Logger.i("onNewTaskAddedTriggered");
        dimView.setVisibility(View.GONE);
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
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
         View v = view.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG);
         Utils.updateCurrentBackground(this, mTestCursorAdapter, mDataAccess, position -1, v);
         listDataChanged();
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
