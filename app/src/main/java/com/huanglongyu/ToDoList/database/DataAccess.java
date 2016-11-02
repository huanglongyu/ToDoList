package com.huanglongyu.ToDoList.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataAccess {

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
        values.put(DbHelper.DONE, isDone);
        db.update(DbHelper.TABLE_NAME, values, DbHelper.ID + " = " + id,
                null);
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
                .query(DbHelper.TABLE_NAME, null, null, null, null, null, null);
    }

    public void clearDb() {
        db.execSQL("delete from " + DbHelper.TABLE_NAME);
        dbHelper.onUpgrade(db, 1, 1);
    }
}
