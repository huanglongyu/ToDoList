package com.huanglongyu.ToDoList.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by hly on 11/10/16.
 */

public class ThreadUtil {
    private static ExecutorService sExecutor = null;


    public static ExecutorService obtainExecutor() {
        if (sExecutor == null) {
            sExecutor = Executors.newSingleThreadExecutor();
        }
        return sExecutor;
    }

    public static void runOnBackground(Runnable con) {
        obtainExecutor().execute(con);
    }
}
