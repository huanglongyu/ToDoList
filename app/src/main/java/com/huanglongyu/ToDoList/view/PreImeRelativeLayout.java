package com.huanglongyu.ToDoList.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.huanglongyu.todolist.R;

public class PreImeRelativeLayout extends RelativeLayout {
    private ToDoListView mToDoListView;

    public PreImeRelativeLayout(Context context, AttributeSet attrs,
            int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PreImeRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PreImeRelativeLayout(Context context) {
        super(context);
    }

    public void setToDoListView(ToDoListView list){
        mToDoListView = list;
    }

    @Override
    public boolean dispatchKeyEventPreIme(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (mToDoListView.getHeadHeight() == mToDoListView.getHeadMaxHeight()
                    && mToDoListView.getHeadText().trim().equals("")) {
                View view = findViewById(R.id.full_dim);
                view.setVisibility(View.GONE);
                mToDoListView.HeadrollBack();
            }
        }
        return super.dispatchKeyEventPreIme(event);
    }

}
