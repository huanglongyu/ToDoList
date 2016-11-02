package com.huanglongyu.ToDoList.database;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.database.Cursor;

public class DataLoader extends AsyncTaskLoader<Cursor> {
    private DataAccess mDataAccess;

    public DataLoader(Context context, DataAccess da) {
        super(context);
        mDataAccess = da;
    }

    @Override
    public Cursor loadInBackground() {
        return mDataAccess.getAll();
    }
}
