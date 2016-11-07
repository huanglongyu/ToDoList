package com.huanglongyu.ToDoList.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.huanglongyu.ToDoList.util.Utils;

public class DataAccess {

    private static final String TAG = "DataAccess";
    private DbHelper dbHelper;
    private SQLiteDatabase db;

    public interface DataObserverListener {
        void onContendUpdate(int id, String newcontent);
    }

    public DataAccess(Context context) {
        dbHelper = DbHelper.getInstance(context);
    }

    public void openDb() {
        db = dbHelper.getWritableDatabase();
    }

    public void closeDb() {
        db.close();
    }

    public void removeItem(int id) {
        db.delete(DbHelper.TABLE_NAME, DbHelper.ID + " = " + id, null);
    }

    public void addItem(String content) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.CONTENT, content);
        values.put(DbHelper.DONE, DbHelper.ITEM_NOT_DONE);
        values.put(DbHelper.COLOUR, Utils.Colours._DARK_BLUE);
        long result = db.insert(DbHelper.TABLE_NAME, null, values);
        Log.i(TAG, "addItem :" + result);
    }

    public void updateItemContent(int id, String newcontent) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.CONTENT, newcontent);
        db.update(DbHelper.TABLE_NAME, values, DbHelper.ID + " = " + id,
                null);
    }

    public void updateItemBackGround(int id, int newcolor) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.COLOUR, newcolor);
        db.update(DbHelper.TABLE_NAME, values, DbHelper.ID + " = " + id,
                null);
    }

    public void updateItemIsDone(int id, int isDone) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.TIME_STAMP, System.currentTimeMillis());
        values.put(DbHelper.DONE, isDone);
        db.update(DbHelper.TABLE_NAME, values, DbHelper.ID + " = " + id,
                null);

//        ContentValues values = new ContentValues();
//        values.put(DbHelper.DONE, isDone);
//        Cursor c = getItem(id + "");
//        c.moveToFirst();
//        values.put(DbHelper.CONTENT, c.getString(c.getColumnIndex(DbHelper.CONTENT)));
//        values.put(DbHelper.COLOUR, c.getInt(c.getColumnIndex(DbHelper.COLOUR)));
//        values.put(DbHelper.TIME_STAMP, System.currentTimeMillis());
//        db.insert(DbHelper.TABLE_NAME, null, values);
//        removeItem(id);
    }

//    public void createRow(String customer, String order, String address,
//            String phone, String price, String time) {
//        ContentValues values = new ContentValues();
//        values.put(dbHelper.COL_CUSTOMER, customer);
//        values.put(dbHelper.COL_ORDER, order);
//        values.put(dbHelper.COL_ADDRESS, address);
//        values.put(dbHelper.COL_NUMBER, phone);
//        values.put(dbHelper.COL_PRICE, price);
//        values.put(dbHelper.COL_TIME, time);
//
//        db.insert(dbHelper.TABLE_NAME, null, values);
//        Logger.i(TAG, "Inserted row with customer name = " + customer);
//    }

//    public void deleteRow(String id) {
//        System.out.println("123" + id);
//        System.out.println("222" + dbHelper.COL_ID);
//        db.delete(dbHelper.TABLE_NAME, dbHelper.COL_ID + " = " + id, null);
//        Logger.i(TAG, "Deleted row with row id = " + id);
//    }

//    public void updateRow(String id, String customer, String order,
//            String address, String phone, String price, String time) {
//        ContentValues values = new ContentValues();
//        values.put(dbHelper.COL_CUSTOMER, customer);
//        values.put(dbHelper.COL_ORDER, order);
//        values.put(dbHelper.COL_ADDRESS, address);
//        values.put(dbHelper.COL_NUMBER, phone);
//        values.put(dbHelper.COL_PRICE, price);
//        values.put(dbHelper.COL_TIME, time);
//
//        db.update(dbHelper.TABLE_NAME, values, dbHelper.COL_ID + " = " + id,
//                null);
//        Logger.i(TAG, "Updated row with row id = " + id);
//    }

    public Cursor getItem(String id) {
        Cursor cursor = db.query(DbHelper.TABLE_NAME, null, DbHelper.ID
                + " = " + id, null, null, null, null);
        return cursor;
    }

    public Cursor getAll() {
        return db
                .query(DbHelper.TABLE_NAME, null, null, null, null, null, "done ,  time_stamp");
    }

    public void clearDb() {
        db.execSQL("delete from " + DbHelper.TABLE_NAME);
        dbHelper.onUpgrade(db, 1, 1);
    }
}
