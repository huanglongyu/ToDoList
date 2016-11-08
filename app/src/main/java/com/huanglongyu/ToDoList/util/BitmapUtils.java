package com.huanglongyu.ToDoList.util;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.View;

/**
 * Created by hly on 11/7/16.
 */

public class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * Returns a bitmap showing a screenshot of the view passed in.
     */
    @NonNull
    public static Bitmap getBitmapFromView(@NonNull final View v) {
        Bitmap bitmap = Bitmap.createBitmap(v.getMeasuredWidth(), v.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

}