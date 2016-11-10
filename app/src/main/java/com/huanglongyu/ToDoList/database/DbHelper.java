package com.huanglongyu.ToDoList.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static DbHelper sInstance;

    public static final String DATABASE_NAME = "todolist_database.db";
    public static final String TABLE_NAME = "todolist";
    public static final int DATABASE_VERSION = 1;

    public static final String ID = "_id";
    public static final String CONTENT = "content";
    public static final String COLOR = "color";
    public static final String DONE = "done";
    public static final String TIME_STAMP = "time_stamp";

    public static final int ITEM_DONE = 1;
    public static final int ITEM_NOT_DONE = 0;

    private static final String CREATE_TABLE = 
                    "create table " + TABLE_NAME
                    + " (" + "_id integer primary key autoincrement, "
                    + CONTENT + " text not null, "
                    + COLOR +  " integer not null, "
                    + TIME_STAMP +  " integer not null, "
                    + DONE   + " integer not null);";

    private String[] statements = {
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test1', '7', '0', '1');",
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test2', '7', '0', '2');",
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test3', '7', '0', '3');",
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test4', '7', '0', '4');",
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test5', '7', '1', '5');",
            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + "," + TIME_STAMP +") VALUES (NULL, 'test6', '7', '1', '6');"
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test7', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test8', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test9', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test10', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test11', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test12', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test13', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test14', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test15', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test16', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test17', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test18', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test19', '0', '0');",
//            "INSERT INTO " + TABLE_NAME + "(_id," +  CONTENT + "," + COLOR + "," +  DONE + ") VALUES (NULL, 'test20', '0', '0');"
    };

    public static synchronized DbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    private DbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
        for (String s : statements) {
            db.execSQL(s);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

}
