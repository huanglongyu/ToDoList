package com.huanglongyu.ToDoList.util;

import android.database.Cursor;
import android.graphics.Color;
import android.view.View;

import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.database.DataAccess;
import com.huanglongyu.ToDoList.database.DbHelper;

public class Utils {
    private static int ISDONE = -100;

    private enum Colours {
        LIGHT_BLUE, RED, ORANGE, DARK_GREEN, LIGHT_GREEN, DARK_BLUE;
        public static final int _LIGHT_BLUE = LIGHT_BLUE.ordinal();
        public static final int _RED = RED.ordinal();
        public static final int _ORANGE = ORANGE.ordinal();
        public static final int _DARK_GREEN = DARK_GREEN.ordinal();
        public static final int _LIGHT_GREEN = LIGHT_GREEN.ordinal();
        public static final int _DARK_BLUE = DARK_BLUE.ordinal();
    }

    public static int updateCurrentBackgroundWithoutSave(View view, Cursor c) {
        int isDone = c.getInt(c.getColumnIndex(DbHelper.DONE));
        //this is ugly
        if (isDone == 1){
            view.setBackgroundColor(0xFF787878);
            return ISDONE;
        }
        
        int oldColour = c.getInt(c.getColumnIndex(DbHelper.COLOUR));
        Colours[] allColours = Colours.values ();
        int newColour = (oldColour + 1) % allColours.length;
        if (newColour == Colours._LIGHT_BLUE) {
            view.setBackgroundColor(Color.BLUE);
        } else if (newColour == Colours._RED) {
            view.setBackgroundColor(Color.CYAN);
        } else if (newColour == Colours._ORANGE) {
            view.setBackgroundColor(Color.DKGRAY);
        } else if (newColour == Colours._DARK_GREEN) {
            view.setBackgroundColor(Color.GREEN);
        } else if (newColour == Colours._LIGHT_GREEN) {
            view.setBackgroundColor(Color.RED);
        } else if (newColour == Colours._DARK_BLUE){
            view.setBackgroundColor(Color.YELLOW);
        }
        return newColour;
    }

    public static void updateCurrentBackground(TestCursorAdapter adapter, DataAccess da, int position, View view) {
        Cursor c = (Cursor)adapter.getItem(position);
        int newcolor = updateCurrentBackgroundWithoutSave(view, c);
        //this is ugly
        if (newcolor == ISDONE) {
            return;
        }
        da.updateItemBackGround(c.getInt(c.getColumnIndex(DbHelper.ID)), newcolor);
    }

}
