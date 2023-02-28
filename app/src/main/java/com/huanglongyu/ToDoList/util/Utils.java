package com.huanglongyu.ToDoList.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.view.View;

import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.adapter.ToDoListAdapter;
import com.huanglongyu.ToDoList.bean.ToDoitem;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.todolist.R;

public class Utils {
    private static int ISDONE = -100;
    private static final String TAG = "Utils";

    public enum Colours {
        LIGHT_BLUE, RED, ORANGE, DARK_GREEN, LIGHT_GREEN, DARK_BLUE;
        public static final int _LIGHT_BLUE = LIGHT_BLUE.ordinal();
        public static final int _RED = RED.ordinal();
        public static final int _ORANGE = ORANGE.ordinal();
        public static final int _DARK_GREEN = DARK_GREEN.ordinal();
        public static final int _LIGHT_GREEN = LIGHT_GREEN.ordinal();
        public static final int _DARK_BLUE = DARK_BLUE.ordinal();
    }

    public static ContentValues transformToContentValues(ToDoitem item) {
        ContentValues values = new ContentValues();
        values.put(DbHelper.CONTENT, item.getContent());
        values.put(DbHelper.TIME_STAMP, item.getTimeStamp());
        values.put(DbHelper.DONE, item.getIsDone());
        values.put(DbHelper.COLOR, item.getColor());
        return values;
    }

    public static int updateCurrentBackgroundWithoutSave(Context context, View view, ToDoitem item) {
        int isDone = item.getIsDone();
        //this is ugly
        if (isDone == DbHelper.ITEM_DONE){
            view.setBackgroundColor(0xFF787878);
            return ISDONE;
        }
        int oldColour = item.getColor();
        Colours[] allColours = Colours.values ();
        int newColour = (oldColour + 1) % allColours.length;
        if (newColour == Colours._LIGHT_BLUE) {
            view.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
        } else if (newColour == Colours._RED) {
            view.setBackgroundColor(context.getResources().getColor(R.color.red));
        } else if (newColour == Colours._ORANGE) {
            view.setBackgroundColor(context.getResources().getColor(R.color.orange));
        } else if (newColour == Colours._DARK_GREEN) {
            view.setBackgroundColor(context.getResources().getColor(R.color.dark_green));
        } else if (newColour == Colours._LIGHT_GREEN) {
            view.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        } else if (newColour == Colours._DARK_BLUE){
            view.setBackgroundColor(context.getResources().getColor(R.color.dark_blue));
        }
        return newColour;

    }


    public static int updateCurrentBackgroundWithoutSave(Context context, View view, Cursor c) {
        int isDone = c.getInt(c.getColumnIndex(DbHelper.DONE));
        //this is ugly
        if (isDone == DbHelper.ITEM_DONE){
            view.setBackgroundColor(0xFF787878);
            return ISDONE;
        }
        
        int oldColour = c.getInt(c.getColumnIndex(DbHelper.COLOR));
        Colours[] allColours = Colours.values ();
        int newColour = (oldColour + 1) % allColours.length;
        if (newColour == Colours._LIGHT_BLUE) {
            view.setBackgroundColor(context.getResources().getColor(R.color.light_blue));
        } else if (newColour == Colours._RED) {
            view.setBackgroundColor(context.getResources().getColor(R.color.red));
        } else if (newColour == Colours._ORANGE) {
            view.setBackgroundColor(context.getResources().getColor(R.color.orange));
        } else if (newColour == Colours._DARK_GREEN) {
            view.setBackgroundColor(context.getResources().getColor(R.color.dark_green));
        } else if (newColour == Colours._LIGHT_GREEN) {
            view.setBackgroundColor(context.getResources().getColor(R.color.light_green));
        } else if (newColour == Colours._DARK_BLUE){
            view.setBackgroundColor(context.getResources().getColor(R.color.dark_blue));
        }
        return newColour;
    }

    public static void updateCurrentBackground(Context context, TestCursorAdapter adapter, DataAccess da, int position, View view) {
        Cursor c = (Cursor)adapter.getItem(position);
        int newcolor = updateCurrentBackgroundWithoutSave(context, view, c);
        //this is ugly
        if (newcolor == ISDONE) {
            return;
        }
        da.updateItemBackGround(c.getInt(c.getColumnIndex(DbHelper.ID)), newcolor);
    }

    public static void updateListAdapterBackground(Context context, ToDoListAdapter adapter, DataAccess da, int position, View view) {
        Cursor c = (Cursor)adapter.getItem(position);
        int newcolor = updateCurrentBackgroundWithoutSave(context, view, c);
        //this is ugly
        if (newcolor == ISDONE) {
            return;
        }
        da.updateItemBackGround(c.getInt(c.getColumnIndex(DbHelper.ID)), newcolor);
    }

}
