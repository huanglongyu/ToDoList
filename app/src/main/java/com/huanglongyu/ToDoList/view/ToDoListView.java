package com.huanglongyu.ToDoList.view;

import java.util.ArrayList;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.database.Cursor;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.huanglongyu.ToDoList.R;
import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;

public class ToDoListView extends ListView implements OnScrollListener,HeaderView.OnHeaderTriggerListener {
    public static final String ITEM_CONTENT_LAYOUT_TAG = "ITEM_CONTENT_LAYOUT_TAG";
    public static final String ITEM_CONTENT_LEFT_TAG = "ITEM_CONTENT_LEFT_TAG";
    public static final String ITEM_CONTENT_RIGHT_TAG = "ITEM_CONTENT_RIGHT_TAG";


    private Scroller mScroller;
    private HeaderView mHeaderView;
    private boolean limitToScroll = false;
    private int itemMaxHeight;
    private float mLastY = -1, mDownY = -1; // save event y
    private float mLastX = -1, mDownX = -1;
    private boolean isTouchingScreen = false;// 手指是否触摸屏幕
    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
    private final static String TAG = "ToDoListView";
    private OnToDoListViewTriggerListener mOnToDoListViewTriggerListener;

    private View itemView;
//    private View rightView, leftView;
    private int mTouchSlop;
    private boolean isSlide;
    private boolean pendingSlide = false;
    private boolean pendingDismiss = false;
    private boolean pendingDone = false;
    private int slidePosition;
    private VelocityTracker velocityTracker;
    private static final int SNAP_VELOCITY = 600;
    private static final int SLID_ANIMATION_TIME = 120;
    private static final int DISMISS_ANIMATION_TIME = 120;
    private static final int ROLLBACK_ANIMATION_TIME = 400;
    private static final int DONE_ANIMATION_TIME = 200;
    private int velocityX;
    private int screenWidth;
    private int itemMarin;
    private ArrayList<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private ArrayList<PendingDoneData> mPendingDone = new ArrayList<PendingDoneData>();


    public interface OnToDoListViewTriggerListener{
        void onDownTriggered();
        void onUpTriggered();
        void onNewTaskCancelTriggered();
        void onNewTaskAddedTriggered(String item);
        void onTaskClear(int position);
        void onToggleDone(int position);
        void onHeaderInitFinished(int height);
    }

    public void setOnToDoListViewTriggerListener(OnToDoListViewTriggerListener trigger){
        mOnToDoListViewTriggerListener = trigger;
    }

//    public void setRightContent(View rightView) {
//        this.rightView = rightView; 
//    }

//    public void setLeftContent(View leftView) {
//        this.leftView = leftView; 
//    }

    public void setHeaderFocus(boolean focus){
        mHeaderView.setFocus(focus);
    }

    public ToDoListView(Context context) {
        super(context);
        initWithContext(context);
    }

    public ToDoListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    public ToDoListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWithContext(context);
    }

    public void setHeadMaxHeight(int max) {
        itemMaxHeight = max;
        mHeaderView.setMaxHeight(max);
    }

    public int getHeadMaxHeight(){
        return itemMaxHeight;
    }

    public int getHeadHeight(){
        return mHeaderView.getHeight();
    }

    public String getHeadText(){
        return mHeaderView.getEditTextContent();
    }

    public void initHead() {
        mHeaderView.initHead();
    }

    private void initWithContext(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        // XListView need the scroll event, and it will dispatch the event to
        // user's listener (as a proxy).
//        super.setOnScrollListener(this);
        setOnScrollListener(this);

        // init header view
        mHeaderView = new HeaderView(context);
        mHeaderView.setOnHeaderTriggerListener(this);
        // mHeaderView.setStateChangedListener(this);
        addHeaderView(mHeaderView);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        itemMarin = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_margin);
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setAdapter(adapter);
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                View item = adapter.getView(0, null, null);
                item.measure(0, 0);
                int height = item.getMeasuredHeight();
                setHeadMaxHeight(height);
                initHead();
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
                if (mOnToDoListViewTriggerListener != null) {
                    mOnToDoListViewTriggerListener.onHeaderInitFinished(height);
                }
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN: {
            addVelocityTracker(ev);
            if (!mScroller.isFinished()) {
                return super.dispatchTouchEvent(ev);
            }
            mDownX = ev.getRawX();
            mDownY = ev.getRawY();

            slidePosition = pointToPosition((int) ev.getX(), (int) ev.getY());
            if (slidePosition == AdapterView.INVALID_POSITION) {
                itemView = null;
                return super.dispatchTouchEvent(ev);
            }
//            View v = getChildAt(slidePosition - getFirstVisiblePosition());
//            itemView = v.findViewWithTag(v.getTag(R.id.TAG_HODLER_ID));
//            Logger.i("v:" + v  + " itemView:" + itemView);
            itemView = getChildAt(slidePosition - getFirstVisiblePosition());
//            Logger.i("itemView:" + itemView + " " + itemView.getWidth());
            break;
        }
        case MotionEvent.ACTION_MOVE: {
            if (Math.abs(getXScrollVelocity()) > SNAP_VELOCITY
                    || (Math.abs(ev.getRawX() - mDownX) > mTouchSlop && Math.abs(ev
                    .getRawY() - mDownY) < mTouchSlop)) {
                isSlide = true;
            } else {
                isSlide = false;
            }
            break;
        }
        case MotionEvent.ACTION_UP:
            recycleVelocityTracker();
            break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void recycleVelocityTracker() {
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    private void addVelocityTracker(MotionEvent ev) {
        if (velocityTracker == null) {
            velocityTracker = VelocityTracker.obtain();
        }
        velocityTracker.addMovement(ev);
    }

    //get x velocity
    //return >0, slide to right
    //return <0, slide to left
    private int getXScrollVelocity() {
        velocityTracker.computeCurrentVelocity(1000);
        int velocity = (int) velocityTracker.getXVelocity();
        return velocity;
    }

    private void scrollRight() {
//        removeDirection = RemoveDirection.RIGHT;
        pendingDismiss = false;
        pendingDone = true;
        int currentScrollX = itemView.getScrollX();
//        int halfWidth = screenWidth/2;
//        int delta;
//        if (currentScrollX >= -halfWidth) {
//            delta = halfWidth + itemView.getScrollX();
//        } else {
//            delta = (screenWidth + itemView.getScrollX());
//        }
//        Logger.i("scrollRight begin :" + currentScrollX + " end:" + (-delta));
        mScroller.startScroll(currentScrollX, 0, -currentScrollX, 0, DONE_ANIMATION_TIME);
        performDone(itemView,slidePosition);
        postInvalidate();
    }

    private void scrollLeft() {
//        removeDirection = RemoveDirection.LEFT;
        pendingDismiss = true;
        pendingDone = false;
        final int delta = (screenWidth - itemView.getScrollX());
        Logger.i("scrollLeft begin :" + itemView.getScrollX() + " end:" + (-delta));
        mScroller.startScroll(itemView.getScrollX(), 0, delta, 0, SLID_ANIMATION_TIME);
        postInvalidate();
    }

    private void scrollByDistanceX() {
        int delX = itemView.getScrollX();
        int thresholdWidth = screenWidth / 3;
        if (delX >= thresholdWidth) {
            scrollLeft();
        } else if (delX <= -thresholdWidth) {
            scrollRight();
        } else {
            //scroll back
            pendingDone = false;
            pendingDismiss = false;
            mScroller.startScroll(delX, 0, -delX, 0, Math.abs(delX));
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            addVelocityTracker(ev);
            switch (ev.getAction()) {
            case MotionEvent.ACTION_MOVE:
                itemView.scrollBy((int)mDownX - (int) (ev.getRawX()), 0);
                mDownX = ev.getRawX();
                velocityX = getXScrollVelocity();
//                View left = itemView.findViewWithTag(ToDoListView.ITEM_CONTENT_LEFT_TAG);
//                left.setVisibility(View.VISIBLE);
//                Logger.i("left:" + left + " " + left.getWidth() + " " + left.getHeight());
//                postInvalidate();
                break;
            case MotionEvent.ACTION_UP:
                pendingSlide = true;
                if (velocityX > SNAP_VELOCITY) {
                    scrollRight();
                } else if (velocityX < -SNAP_VELOCITY) {
                    scrollLeft();
                } else {
                    scrollByDistanceX();
                }
                isSlide = false;
                recycleVelocityTracker();
                break;
            }
            return true;
        }
        switch (ev.getAction()) {
        case MotionEvent.ACTION_DOWN:
//            mScroller.abortAnimation();
            mLastY = ev.getRawY();
            mDownY = ev.getRawY();

            isTouchingScreen = true;
            limitToScroll = false;
            break;
        case MotionEvent.ACTION_MOVE:
            final float deltaY = ev.getRawY() - mLastY;
            mLastY = ev.getRawY();
            // Logger.i("longyu","getFirstVisiblePosition:" +
            // getFirstVisiblePosition() + " deltaY:" + deltaY +
            // " getVisiableHeight():" + mHeaderView.getVisiableHeight());
//            Logger.i("mHeaderView.getBottom():" + mHeaderView.getBottom() + " getTop:" + mHeaderView.getTop());

            if (getFirstVisiblePosition() == 0
                    && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
                // the first item is showing, header has shown or pull down.
                // updateHeaderHeight(deltaY / OFFSET_RADIO);
                limitToScroll = true;
                updateHeaderHeight(deltaY);
//                Logger.i("updateHeaderHeight");
            }else if((deltaY < 0 || mHeaderView.getVisiableHeight() <=0) && limitToScroll){
//                Logger.i("ACTION_MOVE return true, limitToScroll:" + limitToScroll);
//                return true;
            }else{
//                Logger.i("else ACTION_MOVE, deltaY:" + deltaY + " mHeaderView.getVisiableHeight():" + mHeaderView.getVisiableHeight());
            }
//            else if(getFirstVisiblePosition() != 0){
//                mHeaderView.setShowHeight(0);
//            }
            break;
        case MotionEvent.ACTION_UP:
            limitToScroll = false;
            int height = mHeaderView.getVisiableHeight();
            if (height == 0) {
            // not visible.
                break;
            }
//            float movedY = (ev.getRawY() - mDownY) * 0.9f;
            if(mHeaderView.getBottom() > itemMaxHeight/2){
//                int offset = itemMaxHeight - mHeaderView.getBottom();
                int offset = itemMaxHeight - mHeaderView.getHeight();
                Log.i(TAG, "ACTION_UP  down!!!!!!!!height:" + height + " " + " offset:" + offset + " getTop:" + mHeaderView.getTop() + " getBottom:" +  mHeaderView.getBottom()
                        + " getFirstVisiblePosition:" + getFirstVisiblePosition() + " itemMaxHeight:" + itemMaxHeight);
                if(getFirstVisiblePosition() !=0){
//                    Logger.i("up!!!!!!!! setShowHeight 0," + getFirstVisiblePosition());
                    mHeaderView.setShowHeight(0);
                    this.invalidate();
                    break;
                }
                if(offset != 0){
                    Log.i(TAG, "ACTION_UP down!!!!!!!! startScroll 1, needToMovedY:" + mHeaderView.getBottom());
                    mScroller.startScroll(0, height, 0, offset, SLID_ANIMATION_TIME);
                    this.invalidate();
                } else if (offset == 0){
                    if(mOnToDoListViewTriggerListener != null){
                        mOnToDoListViewTriggerListener.onDownTriggered();
                    }
                }
            }else{
//                int offset = mHeaderView.getBottom();
                int offset = mHeaderView.getHeight();
                Logger.i("ACTION_UP  up!!!!!!!!height:" + height + " " + " offset:" + offset + " getTop:" + mHeaderView.getTop() + " getBottom:" +  mHeaderView.getBottom()
                        + " getFirstVisiblePosition:" + getFirstVisiblePosition() + " itemMaxHeight:" + itemMaxHeight);
                if(getFirstVisiblePosition() !=0){
//                    Logger.i("up!!!!!!!! setShowHeight 0," + getFirstVisiblePosition());
                    mHeaderView.setShowHeight(0);
                    this.invalidate();
                    break;
                }
                if(offset != 0){
                    Logger.i("ACTION_UP up!!!!!!!! startScroll 2, needToMovedY:" + mHeaderView.getBottom() + " offset:" + offset);
                    mScroller.startScroll(0, height, 0, -offset, SLID_ANIMATION_TIME);
                    this.invalidate();
                }
            }
            break;
        default:
            Logger.i("default");
            // mLastY = -1; // reset
            // isTouchingScreen = false;
            // // TODO
            // //
            // 存在bug：当两个if的条件都满足的时候，只能滚动一个，所以在reSetHeader的时候就不起作用了，一般就只会reSetFooter
            // if (getFirstVisiblePosition() == 0) {
            // resetHeaderHeight();
            // }
            // if (getLastVisiblePosition() == mTotalItemCount - 1) {
            // // invoke load more.
            // if (mEnablePullLoad
            // && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
            // startLoadMore();
            // }
            // resetFooterHeight();
            // }
            break;
        }
        return super.onTouchEvent(ev);
    }

    public void HeadrollBack(){
        mScroller.startScroll(0, itemMaxHeight, 0, -itemMaxHeight, ROLLBACK_ANIMATION_TIME);
        this.invalidate();
    }

    public interface newItemAniamation {
        void end();
    }

    public void addNewItem(final newItemAniamation mNewItemAniamation) {
        final View view = mHeaderView.findViewById(R.id.header_parent);
        int targetValue = screenWidth - 2 * itemMarin;
        final int fromValue = view.getWidth();
        Log.i(TAG, "targetValue:" + targetValue + " fromValue:" + fromValue + " viewW:" + " " + view.getLayoutParams().width);

        ValueAnimator animator = ValueAnimator.ofInt(fromValue, targetValue).setDuration(150);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                params.width = (int)valueAnimator.getAnimatedValue();
//                Log.i(TAG, "onAnimationUpdate :" + params.width);
                view.setLayoutParams(params);
                view.requestLayout();
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mHeaderView.reset();
                FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) view.getLayoutParams();
                params.width = fromValue;
                view.setLayoutParams(params);
                mNewItemAniamation.end();

            }
        });
        animator.start();
    }

    private void updateHeaderHeight(float delta) {
        int newHeight = (int) delta + mHeaderView.getVisiableHeight();
        // Logger.i("longyu","getVisiableHeight:" + mHeaderView.getVisiableHeight()
        // + " delta:" + delta);
        // mHeaderView.setVisiableHeight(newHeight);// 动态设置HeaderView的高度
//        Logger.i("newHeight:" + newHeight + " getVisiableHeight:" + mHeaderView.getVisiableHeight() + " delta:" + delta + " bottom:" + mHeaderView.getBottom() +
//                " top:" + mHeaderView.getTop());
        mHeaderView.setShowHeight(newHeight);
    }

    @Override
    public void computeScroll() {
        if (pendingSlide) {
            if (mScroller.computeScrollOffset()) {
                itemView.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
                if (mScroller.isFinished()) {
                    if (pendingDismiss) {
                        performDismiss(itemView,slidePosition);
                        pendingDismiss = false;
                    } else if (pendingDone) {
//                        performDone(itemView,slidePosition);
                        pendingDone = false;
                    }
                    pendingSlide = false;
                }
                postInvalidate();
            }
            return;
        }
        if (mScroller.computeScrollOffset()) {
//            Logger.i("computeScroll mScroller.getCurrY():" + mScroller.getCurrY());
            if(mScroller.getCurrY() == 0){
                mScroller.abortAnimation();
                if(mOnToDoListViewTriggerListener != null){
                    mOnToDoListViewTriggerListener.onUpTriggered();
                }
            }else if(mScroller.getCurrY() == itemMaxHeight){
                mScroller.abortAnimation();
                if(mOnToDoListViewTriggerListener != null){
                    mOnToDoListViewTriggerListener.onDownTriggered();
                }
            }
            mHeaderView.setShowHeight(mScroller.getCurrY());
//            Logger.i("computeScroll mHeaderView:" + mHeaderView.getVisiableHeight() + " mScroller.getCurrY():" + mScroller.getCurrY() + " mHeaderView.gettop:" + mHeaderView.getTop());
            postInvalidate();
        }
        super.computeScroll();
    }

    class PendingDoneData implements Comparable<PendingDoneData> {
        public int position;
        public View view;

        public PendingDoneData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDoneData other) {
            return other.position - position;
        }
    }

    private void performDone(final View doneView, final int donePosition) {
//        Logger.i("doneView :" + doneView.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG));
        ValueAnimator animator = ValueAnimator.ofFloat(0.2f, 1).setDuration(DONE_ANIMATION_TIME);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float alpha = (Float)valueAnimator.getAnimatedValue();
                doneView.setAlpha(alpha);
            }
        });
        Cursor c = (Cursor)getAdapter().getItem(donePosition);
        int isDone = c.getInt(c.getColumnIndex(DbHelper.DONE));
        int fromColor , toColor;
        if (isDone == 1) {
            fromColor = 0xFFDCDCDC;
            toColor = 0xFF787878;
        } else {
            fromColor = 0xFF787878;
            toColor = c.getInt(c.getColumnIndex(DbHelper.COLOUR));
        }
        
        ValueAnimator animatorColor = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor).setDuration(DONE_ANIMATION_TIME);
        animatorColor.addUpdateListener(new ValueAnimator.AnimatorUpdateListener(){

            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                int color = (Integer)valueAnimator.getAnimatedValue();
                View content = doneView.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG);
                content.setBackgroundColor(color);
        }});

        animator.addListener(new AnimatorListenerAdapter(){

            @Override
            public void onAnimationEnd(Animator animation) {
                if(mOnToDoListViewTriggerListener != null){
                    mOnToDoListViewTriggerListener.onToggleDone(donePosition - 1);
                }
                long time = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                onTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }

        });
//        mPendingDone.add(new PendingDoneData(donePosition, doneView));
        animator.start();
        animatorColor.start();
    }

    class PendingDismissData implements Comparable<PendingDismissData> {
        public int position;
        public View view;

        public PendingDismissData(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public int compareTo(PendingDismissData other) {
            return other.position - position;
        }
    }

    private void performDismiss(final View dismissView, final int dismissPosition) {
        final ViewGroup.LayoutParams lp = dismissView.getLayoutParams();
        final int originalHeight = dismissView.getHeight();
        ValueAnimator animator = ValueAnimator.ofInt(originalHeight, 1).setDuration(DISMISS_ANIMATION_TIME);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                lp.height = (Integer) valueAnimator.getAnimatedValue();
                dismissView.setLayoutParams(lp);
            }
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(mOnToDoListViewTriggerListener != null){
                    mOnToDoListViewTriggerListener.onTaskClear(dismissPosition - 1);
                }
                ViewGroup.LayoutParams lp;
                for (PendingDismissData pendingDismiss : mPendingDismisses) {
                    // Reset view presentation
                    pendingDismiss.view.setAlpha(1f);
                    pendingDismiss.view.scrollTo(0, 0);
                    lp = pendingDismiss.view.getLayoutParams();
                    lp.height = originalHeight;
                    pendingDismiss.view.setLayoutParams(lp);
                }
                // Cancel ListView's touch (un-highlighting the item)
                long time = SystemClock.uptimeMillis();
                MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                        MotionEvent.ACTION_CANCEL, 0, 0, 0);
                cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                onTouchEvent(cancelEvent);
                cancelEvent.recycle();
            }
        });
        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if(firstVisibleItem != 0 && mHeaderView.getVisiableHeight() != 0){
            mHeaderView.setShowHeight(0);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onNewTaskCancelTriggered() {
        if(mOnToDoListViewTriggerListener != null){
            mOnToDoListViewTriggerListener.onNewTaskCancelTriggered();
        }
    }

    @Override
    public void onNewTaskAddedTriggerd(String item) {
        if(mOnToDoListViewTriggerListener != null){
            mOnToDoListViewTriggerListener.onNewTaskAddedTriggered(item);
        }
    }
}