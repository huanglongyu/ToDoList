package com.huanglongyu.ToDoList.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.os.Build;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.huanglongyu.ToDoList.MainActivity;
import com.huanglongyu.ToDoList.adapter.TestCursorAdapter;
import com.huanglongyu.ToDoList.adapter.ToDoListAdapter;
import com.huanglongyu.ToDoList.bean.ToDoitem;
import com.huanglongyu.ToDoList.database.DbHelper;
import com.huanglongyu.ToDoList.util.Logger;
import com.huanglongyu.todolist.R;

import java.util.ArrayList;

public class ToDoListView extends ListView implements OnScrollListener, HeaderView.OnHeaderTriggerListener {
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
    private boolean isDown;
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
    private Context mContext;
    private ArrayList<PendingDismissData> mPendingDismisses = new ArrayList<PendingDismissData>();
    private ArrayList<PendingDoneData> mPendingDone = new ArrayList<PendingDoneData>();

    //drag start
    private float mLastMotionEventY = -1;
    private long mMobileItemId = AdapterView.INVALID_POSITION;
    private View dragItemView;
    private int mOriginalMobileItemPosition = AdapterView.INVALID_POSITION;
    private HoverDrawable mHoverDrawable;
    private boolean isDrag = false;
    private Adapter mAdapter;
    private SwitchViewAnimator mSwitchViewAnimator;
    private OnItemMovedListener mOnItemMovedListener;

    //done/dismiss animation start
    private long mMoveItemId = AdapterView.INVALID_POSITION;
    private HoverDrawable mMoveHoverDrawable;
    private PathMeasure mPathMeasure;
    private float[] mCurrentPosition = new float[2];
    private static final int MOVE_ANIMATION_TIME = 400;


    /**
     * Specifies whether or not the hover drawable is currently being animated as result of an up / cancel event.
     */
    private boolean mIsSettlingHoverDrawable;

    /**
     * The previous first visible item before checking if we should switch.
     */
    private int mPreviousFirstVisibleItem = -1;

    /**
     * The previous last visible item before checking if we should switch.
     */
    private int mPreviousLastVisibleItem = -1;

    /**
     * The current first visible item.
     */
    private int mCurrentFirstVisibleItem;

    /**
     * The current last visible item.
     */
    private int mCurrentLastVisibleItem;

    /**
     * The default scroll amount in pixels.
     */
    private int mSmoothScrollPx;


    private interface SwitchViewAnimator {

        void animateSwitchView(final long switchId, final float translationY);
    }

    public interface OnToDoListViewTriggerListener {
        void onDownTriggered();

        void onUpTriggered();

        void onNewTaskCancelTriggered();

        void onNewTaskAddedTriggered(String item);

        void onTaskClear(int position);

        void onToggleDone(int position);

        void onHeaderInitFinished(int height);
    }

    public void setOnToDoListViewTriggerListener(OnToDoListViewTriggerListener trigger) {
        mOnToDoListViewTriggerListener = trigger;
    }

//    public void setRightContent(View rightView) {
//        this.rightView = rightView; 
//    }

//    public void setLeftContent(View leftView) {
//        this.leftView = leftView; 
//    }

    public void setOnItemMovedListener(OnItemMovedListener onItemMovedListener) {
        mOnItemMovedListener = onItemMovedListener;
    }

    public void setHeaderFocus(boolean focus) {
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

    public int getHeadMaxHeight() {
        return itemMaxHeight;
    }

    public int getHeadHeight() {
        return mHeaderView.getHeight();
    }

    public String getHeadText() {
        return mHeaderView.getEditTextContent();
    }

    public void initHead() {
        mHeaderView.initHead();
    }

    private void initWithContext(Context context) {
        mContext = context;
        mScroller = new Scroller(context, new DecelerateInterpolator());
        // XListView need the scroll event, and it will dispatch the event to
        // user's listener (as a proxy).
//        super.setOnScrollListener(this);
        setOnScrollListener(this);

        // init header view
        mHeaderView = new HeaderView(context);
        mHeaderView.setOnHeaderTriggerListener(this);
//         mHeaderView.setStateChangedListener(this);
        addHeaderView(mHeaderView);

        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
        screenWidth = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        itemMarin = context.getResources().getDimensionPixelSize(R.dimen.todo_item_extra_margin);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mSwitchViewAnimator = new KitKatSwitchViewAnimator();
        } else {
            mSwitchViewAnimator = new LSwitchViewAnimator();
        }

        Resources r = getResources();
        mSmoothScrollPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, r.getDisplayMetrics());
    }

    private class SettleHoverDrawableAnimatorListener extends AnimatorListenerAdapter implements ValueAnimator.AnimatorUpdateListener {

        @NonNull
        private final HoverDrawable mAnimatingHoverDrawable;

        @NonNull
        private final View mAnimatingMobileView;

        private SettleHoverDrawableAnimatorListener(@NonNull final HoverDrawable animatingHoverDrawable, @NonNull final View animatingMobileView) {
            mAnimatingHoverDrawable = animatingHoverDrawable;
            mAnimatingMobileView = animatingMobileView;
        }

        @Override
        public void onAnimationStart(final Animator animation) {
            mIsSettlingHoverDrawable = true;
        }

        @Override
        public void onAnimationUpdate(final ValueAnimator animation) {
            mAnimatingHoverDrawable.setTop((Integer) animation.getAnimatedValue());
            postInvalidate();
        }

        @Override
        public void onAnimationEnd(final Animator animation) {
            mAnimatingMobileView.setVisibility(View.VISIBLE);
            isDrag = false;
            mHoverDrawable = null;
            dragItemView = null;
            mMobileItemId = AdapterView.INVALID_POSITION;
            mOriginalMobileItemPosition = AdapterView.INVALID_POSITION;

            mIsSettlingHoverDrawable = false;
        }
    }

    private class LSwitchViewAnimator implements SwitchViewAnimator {

        @Override
        public void animateSwitchView(final long switchId, final float translationY) {
            getViewTreeObserver().addOnPreDrawListener(new AnimateSwitchViewOnPreDrawListener(switchId, translationY));
        }

        private class AnimateSwitchViewOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

            private final long mSwitchId;
            private final float mTranslationY;

            AnimateSwitchViewOnPreDrawListener(final long switchId, final float translationY) {
                mSwitchId = switchId;
                mTranslationY = translationY;
            }

            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                View switchView = getViewForId(mSwitchId);
                if (switchView != null) {
                    switchView.setTranslationY(mTranslationY);
                    switchView.animate().translationY(0).start();
                }

                assert dragItemView != null;
                dragItemView.setVisibility(View.VISIBLE);
                dragItemView = getViewForId(mMobileItemId);
                assert dragItemView != null;
                dragItemView.setVisibility(View.INVISIBLE);
                return true;
            }
        }
    }

    private class KitKatSwitchViewAnimator implements SwitchViewAnimator {

        @Override
        public void animateSwitchView(final long switchId, final float translationY) {
            assert dragItemView != null;
            getViewTreeObserver().addOnPreDrawListener(new AnimateSwitchViewOnPreDrawListener(dragItemView, switchId, translationY));
            dragItemView = getViewForId(mMobileItemId);
        }

        private class AnimateSwitchViewOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {

            private final View mPreviousMobileView;

            private final long mSwitchId;

            private final float mTranslationY;

            AnimateSwitchViewOnPreDrawListener(final View previousMobileView, final long switchId, final float translationY) {
                mPreviousMobileView = previousMobileView;
                mSwitchId = switchId;
                mTranslationY = translationY;
            }

            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);

                View switchView = getViewForId(mSwitchId);
                if (switchView != null) {
                    switchView.setTranslationY(mTranslationY);
                    switchView.animate().translationY(0).start();
                }

                mPreviousMobileView.setVisibility(View.VISIBLE);

                if (dragItemView != null) {
                    dragItemView.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        }
    }

    @Override
    public void setAdapter(final ListAdapter adapter) {
        super.setAdapter(adapter);
        mAdapter = adapter;
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

    public void startDragging(int position) {
        if (isSlide) {
            return;
        }

        position = position - getHeaderViewsCount();
        if (mMobileItemId != AdapterView.INVALID_POSITION) {
            /* We are already dragging */
            Log.i(TAG, "startDragging We are already dragging");
            return;
        }

        if (mLastMotionEventY < 0) {
            throw new IllegalStateException("User must be touching the DynamicListView!");
        }

        if (mAdapter == null) {
            throw new IllegalStateException("This DynamicListView has no adapter set!");
        }

        if (position < 0 || position >= mAdapter.getCount()) {
            /* Out of bounds */
            Log.i(TAG, "startDragging Out of bounds");
            return;
        }
        dragItemView = getChildAt(position - getFirstVisiblePosition() + getHeaderViewsCount());
        Log.i(TAG, "startDragging, getFirstVisiblePosition: " + getFirstVisiblePosition() + " current position:" + position + " Header:" + getHeaderViewsCount());
        if (dragItemView != null) {
            isDrag = true;
            mOriginalMobileItemPosition = position;
            mMobileItemId = mAdapter.getItemId(position);
            mHoverDrawable = new HoverDrawable(dragItemView, mLastMotionEventY);
            dragItemView.setVisibility(View.INVISIBLE);
        }
    }

    private View getViewForId(final long itemId) {
        if (itemId == AdapterView.INVALID_POSITION || getAdapter() == null) {
            return null;
        }
        int firstVisiblePosition = getFirstVisiblePosition();
        View result = null;
        for (int i = 0; i < getChildCount() && result == null; i++) {
            int position = firstVisiblePosition + i;
            if (position >= 0) {
                long id = getAdapter().getItemId(position);
                if (id == itemId) {
                    result = getChildAt(i);
                }
            }
        }
        return result;
    }

    private int getPositionForId(final long itemId) {
        View v = getViewForId(itemId);
        if (v == null) {
            return AdapterView.INVALID_POSITION;
        } else {
            return getPositionForView(v) - getHeaderViewsCount();
        }
    }

    private void switchViews(final View switchView, final long switchId, final float translationY) {
        assert mHoverDrawable != null;
        assert getAdapter() != null;
        assert dragItemView != null;

        final int switchViewPosition = getPositionForView(switchView);
        int mobileViewPosition = getPositionForView(dragItemView);

        if (mAdapter instanceof ToDoListAdapter) {
            ((ToDoListAdapter) mAdapter).swapItems(switchViewPosition - getHeaderViewsCount(), mobileViewPosition - getHeaderViewsCount());
            ((BaseAdapter) mAdapter).notifyDataSetChanged();
        } else if (mAdapter instanceof TestCursorAdapter) {
            ((TestCursorAdapter) mAdapter).swapItems(switchViewPosition - getHeaderViewsCount(), mobileViewPosition - getHeaderViewsCount());
        }

        mHoverDrawable.shift(switchView.getHeight());
        mSwitchViewAnimator.animateSwitchView(switchId, translationY);
    }

    private void switchIfNecessary() {
        if (mHoverDrawable == null || mAdapter == null) {
            return;
        }

        if (mHoverDrawable == null || mAdapter == null) {
            return;
        }

        int listDatePosition = getPositionForId(mMobileItemId);

        long aboveItemId = listDatePosition - 1 >= 0 ? mAdapter.getItemId(listDatePosition - 1) : AdapterView.INVALID_POSITION;
        long belowItemId = listDatePosition + 1 < getCount() - getHeaderViewsCount()
                ? mAdapter.getItemId(listDatePosition + 1)
                : AdapterView.INVALID_POSITION;

//        Log.i(TAG, "listDatePosition:" + listDatePosition + " aboveItemId:" + aboveItemId + " belowItemId:" + belowItemId);

        final long switchId = mHoverDrawable.isMovingUpwards() ? aboveItemId : belowItemId;
        View switchView = getViewForId(switchId);

        final int deltaY = mHoverDrawable.getDeltaY();
        if (switchView != null && Math.abs(deltaY) > mHoverDrawable.getIntrinsicHeight()) {
            switchViews(switchView, switchId, mHoverDrawable.getIntrinsicHeight() * (deltaY < 0 ? -1 : 1));
        }

        handleMobileCellScroll();

        invalidate();
    }

    void handleMobileCellScroll() {
        if (mHoverDrawable == null || mIsSettlingHoverDrawable) {
            return;
        }

        Rect r = mHoverDrawable.getBounds();
        int offset = computeVerticalScrollOffset();
        int height = getHeight();
        int extent = computeVerticalScrollExtent();
        int range = computeVerticalScrollRange();
        int hoverViewTop = r.top;
        int hoverHeight = r.height();

        int scrollPx = (int) Math.max(1, mSmoothScrollPx * 1.0);
        if (hoverViewTop <= 0 && offset > 0) {
            smoothScrollBy(-scrollPx, 0);
        } else if (hoverViewTop + hoverHeight >= height && offset + extent < range) {
            smoothScrollBy(scrollPx, 0);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (mHoverDrawable != null) {
            mHoverDrawable.draw(canvas);
        }
        if (mMoveHoverDrawable != null) {
            mMoveHoverDrawable.draw(canvas);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isDrag) {
            return super.dispatchTouchEvent(ev);
        }
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
                    setOnItemLongClickListener(null);
                } else if (Math.abs(ev
                        .getRawY() - mDownY) > mTouchSlop) {
                    isDown = true;
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
        int dataPostion = slidePosition - getHeaderViewsCount();
        performDone(itemView, dataPostion);
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

    private boolean handleDownEvent(@NonNull final MotionEvent event) {
        mDownX = event.getRawX();
        mDownY = event.getRawY();
        return true;
    }

    private boolean handleCancelEvent() {
        return handleUpEvent();
    }

    private boolean handleUpEvent() {
        if (dragItemView == null) {
            return false;
        }
        assert mHoverDrawable != null;

        ValueAnimator valueAnimator = ValueAnimator.ofInt(mHoverDrawable.getTop(), (int) dragItemView.getY());
        SettleHoverDrawableAnimatorListener listener = new SettleHoverDrawableAnimatorListener(mHoverDrawable, dragItemView);
        valueAnimator.addUpdateListener(listener);
        valueAnimator.addListener(listener);
        valueAnimator.start();

        int newPosition = getPositionForId(mMobileItemId);
        if (mOriginalMobileItemPosition != newPosition && mOnItemMovedListener != null) {
            mOnItemMovedListener.onItemMoved(mOriginalMobileItemPosition, newPosition);
        }
        return true;
    }

    private boolean handleMoveEvent(@NonNull final MotionEvent event) {
        boolean handled = false;
        if (mHoverDrawable != null) {
            mHoverDrawable.handleMoveEvent(event);
            switchIfNecessary();
            invalidate();
            handled = true;
        }

        return handled;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        mLastMotionEventY = ev.getY();
        if (isDrag) {
            /* We are in the process of animating the hover drawable back, do not start a new drag yet. */
            if (mIsSettlingHoverDrawable) {
                return false;
            }
            boolean handled = false;
            switch (ev.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:
                    mLastMotionEventY = ev.getY();
                    handled = handleDownEvent(ev);
                    break;
                case MotionEvent.ACTION_MOVE:
                    mLastMotionEventY = ev.getY();
                    handled = handleMoveEvent(ev);
                    break;
                case MotionEvent.ACTION_UP:
                    handled = handleUpEvent();
                    mLastMotionEventY = -1;
                    break;
                case MotionEvent.ACTION_CANCEL:
                    handled = handleCancelEvent();
                    mLastMotionEventY = -1;
                    break;
                default:
                    break;
            }
            return handled;
        }

        if (isSlide && slidePosition != AdapterView.INVALID_POSITION) {
            addVelocityTracker(ev);
            switch (ev.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    itemView.scrollBy((int) mDownX - (int) (ev.getRawX()), 0);
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
                    setOnItemLongClickListener((MainActivity) mContext);
                    recycleVelocityTracker();
                    break;
            }
            return true;
        }
        if (isDown) {
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
                    } else if ((deltaY < 0 || mHeaderView.getVisiableHeight() <= 0) && limitToScroll) {
//                Logger.i("ACTION_MOVE return true, limitToScroll:" + limitToScroll);
//                return true;
                    } else {
//                Logger.i("else ACTION_MOVE, deltaY:" + deltaY + " mHeaderView.getVisiableHeight():" + mHeaderView.getVisiableHeight());
                    }
//            else if(getFirstVisiblePosition() != 0){
//                mHeaderView.setShowHeight(0);
//            }
                    break;
                case MotionEvent.ACTION_UP:
                    limitToScroll = false;
                    isDown = false;
                    int height = mHeaderView.getVisiableHeight();
                    if (height == 0) {
                        // not visible.
                        break;
                    }
//            float movedY = (ev.getRawY() - mDownY) * 0.9f;
                    if (mHeaderView.getBottom() > itemMaxHeight / 2) {
//                int offset = itemMaxHeight - mHeaderView.getBottom();
                        int offset = itemMaxHeight - mHeaderView.getHeight();
//                Log.i(TAG, "ACTION_UP  down!!!!!!!!height:" + height + " " + " offset:" + offset + " getTop:" + mHeaderView.getTop() + " getBottom:" +  mHeaderView.getBottom()
//                        + " getFirstVisiblePosition:" + getFirstVisiblePosition() + " itemMaxHeight:" + itemMaxHeight);
                        if (getFirstVisiblePosition() != 0) {
//                    Logger.i("up!!!!!!!! setShowHeight 0," + getFirstVisiblePosition());
                            mHeaderView.setShowHeight(0);
                            this.invalidate();
                            break;
                        }
                        if (offset != 0) {
//                    Log.i(TAG, "ACTION_UP down!!!!!!!! startScroll 1, needToMovedY:" + mHeaderView.getBottom());
                            mScroller.startScroll(0, height, 0, offset, SLID_ANIMATION_TIME);
                            this.invalidate();
                        } else if (offset == 0) {
                            if (mOnToDoListViewTriggerListener != null) {
                                mOnToDoListViewTriggerListener.onDownTriggered();
                            }
                        }
                    } else {
//                int offset = mHeaderView.getBottom();
                        int offset = mHeaderView.getHeight();
                        Logger.i("ACTION_UP  up!!!!!!!!height:" + height + " " + " offset:" + offset + " getTop:" + mHeaderView.getTop() + " getBottom:" + mHeaderView.getBottom()
                                + " getFirstVisiblePosition:" + getFirstVisiblePosition() + " itemMaxHeight:" + itemMaxHeight);
                        if (getFirstVisiblePosition() != 0) {
//                    Logger.i("up!!!!!!!! setShowHeight 0," + getFirstVisiblePosition());
                            mHeaderView.setShowHeight(0);
                            this.invalidate();
                            break;
                        }
                        if (offset != 0) {
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
        }
        return super.onTouchEvent(ev);
    }

    public void HeadrollBack() {
        mHeaderView.setOverridDispatch(true);
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
                params.width = (int) valueAnimator.getAnimatedValue();
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
                        performDismiss(itemView, slidePosition);
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
            if (mScroller.getCurrY() == 0) {
                mScroller.abortAnimation();
                if (mOnToDoListViewTriggerListener != null) {
                    mOnToDoListViewTriggerListener.onUpTriggered();
                }
            } else if (mScroller.getCurrY() == itemMaxHeight) {
                mScroller.abortAnimation();
                if (mOnToDoListViewTriggerListener != null) {
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

    //FIXME: toColor
    private void performDone(final View doneView, final int doneDataPosition) {
        Logger.i("doneView :" + doneView.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG));
        Log.i(TAG, "doneView :" + doneView.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG));
        ValueAnimator animatorAlpha = ValueAnimator.ofFloat(0.1f, 1).setDuration(DONE_ANIMATION_TIME);
        animatorAlpha.setInterpolator(new LinearInterpolator());
        animatorAlpha.addUpdateListener(valueAnimator -> {
            float alpha = (Float) valueAnimator.getAnimatedValue();
            doneView.setAlpha(alpha);
        });
        int fromColor , toColor;
        if (MainActivity.USE_CURSOR) {
            Cursor c = (Cursor)getAdapter().getItem(doneDataPosition);
            int isDone = c.getInt(c.getColumnIndex(DbHelper.DONE));
            if (isDone == DbHelper.ITEM_DONE) {
                fromColor = 0xFFDCDCDC;
                toColor = 0xFF787878;
            } else {
                fromColor = 0xFF787878;
                toColor = c.getInt(c.getColumnIndex(DbHelper.COLOR));
            }
        } else {
            ToDoitem item = (ToDoitem)mAdapter.getItem(doneDataPosition);
            int isDone = item.getIsDone();
            if (isDone == DbHelper.ITEM_DONE) {
                fromColor = 0xFFDCDCDC;
                toColor = 0xFF787878;
            } else {
                fromColor = 0xFF787878;
                toColor = item.getColor();
            }
        }

        ValueAnimator animatorColor = ValueAnimator.ofObject(new ArgbEvaluator(), fromColor, toColor).setDuration(DONE_ANIMATION_TIME);
        animatorColor.addUpdateListener(valueAnimator -> {
            int color = (Integer)valueAnimator.getAnimatedValue();
            View content;
            if (MainActivity.USE_CURSOR) {
                content = doneView.findViewWithTag(TestCursorAdapter.ITEM_VIEW_TAG);
            } else {
                content = (View)doneView.getTag(R.id.TAG_VIEW_ID);
            }
            content.setBackgroundColor(color);

    });

        animatorAlpha.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.i(TAG, "onAnimationEnd");
//                if (mOnToDoListViewTriggerListener != null) {
//                    mOnToDoListViewTriggerListener.onToggleDone(doneDataPosition);
//                }
                int firstDoneDataPosition;
                if (MainActivity.USE_CURSOR) {
                    firstDoneDataPosition = ((TestCursorAdapter) mAdapter).getFirstDoneDataPosition();
                } else {
                    firstDoneDataPosition = ((ToDoListAdapter) mAdapter).getFirstDoneDataPosition();
                }
                Log.i(TAG, "onAnimationEnd, firstDoneDataPosition = " + firstDoneDataPosition);
                Log.i(TAG, "firstDoneDataPostion:" + firstDoneDataPosition +
                        " childAtPostion:" + (firstDoneDataPosition - mCurrentFirstVisibleItem));
//                mMoveItemId = mAdapter.getItemId(doneDataPosition);
//                doneView.setVisibility(View.INVISIBLE);
//                getChildAt(firstDoneDataPostion).setVisibility(View.INVISIBLE);
//                Log.i(TAG, "firstDoneDataPostion:" + firstDoneDataPostion
//                        + " doneDataPosition:" + doneDataPosition + " top:" + doneView.getTop());


                float toX = getChildAt(firstDoneDataPosition - mCurrentFirstVisibleItem).getX();
                float toY = getChildAt(firstDoneDataPosition - mCurrentFirstVisibleItem).getTop();
                float fromX = doneView.getX();
                float fromY = doneView.getTop();

                ((ToDoListAdapter) mAdapter).moveItems(doneDataPosition, firstDoneDataPosition);
                if (firstDoneDataPosition > doneDataPosition) {
                    for (int i = doneDataPosition + 2; i <= firstDoneDataPosition; i++) {
                        View switchView = getChildAt(i - mCurrentFirstVisibleItem);
//                        switchView.setBackground(new ColorDrawable(0xFFAABBCC));
                        MoveViewAnimator move = new MoveViewAnimator(switchView, doneView);
                        move.doMove();
                        invalidate();
                    }
                }
                mMoveHoverDrawable = new HoverDrawable(doneView, mLastMotionEventY);

                Path path = new Path();
                path.moveTo(fromX, fromY);
                path.quadTo((toY + fromY) / 2, (toY + fromY) / 2, toX, toY);
                mPathMeasure = new PathMeasure(path, false);

                Log.i(TAG, "from :" + fromX + "," + fromY + " to:" + toX + "," + toY);

                ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, mPathMeasure.getLength());
                valueAnimator.setDuration(MOVE_ANIMATION_TIME);
                final int left = mMoveHoverDrawable.getBounds().left;
                valueAnimator.addUpdateListener(valueAnimator1 -> {
                    float value = (Float) valueAnimator1.getAnimatedValue();
                    mPathMeasure.getPosTan(value, mCurrentPosition, null);
//                        Log.i(TAG, "[0]: " + mCurrentPosition[0] + "  [1]: " + mCurrentPosition[1]);

                    mMoveHoverDrawable.setBounds(left + (int) mCurrentPosition[0]
                            , (int) mCurrentPosition[1], left + (int) mCurrentPosition[0] + mMoveHoverDrawable.getIntrinsicWidth(),
                            (int) mCurrentPosition[1] + mMoveHoverDrawable.getIntrinsicHeight());
                    invalidate();
                });
                valueAnimator.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mOnItemMovedListener != null) {
                            mOnItemMovedListener.onItemMoved(doneDataPosition, firstDoneDataPosition - 1);
                        }
                    }
                });
                valueAnimator.start();
                sendCancelEvent();
            }

        });
//        mPendingDone.add(new PendingDoneData(donePosition, doneView));
        animatorAlpha.start();
//        animatorColor.start();
    }

    private class MoveViewAnimator {
        private View moveView;
        private View doneView;

        public MoveViewAnimator(View v, View done) {
            moveView = v;
            doneView = done;
        }

        public void doMove() {
            getViewTreeObserver().addOnPreDrawListener(new AnimateMoveViewOnPreDrawListener(moveView, doneView));
        }

        private class AnimateMoveViewOnPreDrawListener implements ViewTreeObserver.OnPreDrawListener {
            private View moveView, doneView;
            private long translationY;


            public AnimateMoveViewOnPreDrawListener(View v, View done) {
                moveView = v;
                translationY = done.getHeight();
                doneView = done;
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                if (moveView != null) {
                    moveView.setTranslationY(translationY);
                    moveView.animate().translationY(0)
                            .setListener(new AnimatorListenerAdapter() {
                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    if (doneView != null && doneView.getVisibility() != View.VISIBLE) {
                                        doneView.setVisibility(View.VISIBLE);
                                    }
                                    doneView = null;
                                    mMoveHoverDrawable = null;
                                }

                                @Override
                                public void onAnimationStart(Animator animation) {
                                    super.onAnimationStart(animation);
                                    if (doneView != null && doneView.getVisibility() == View.VISIBLE) {
                                        doneView.setVisibility(View.INVISIBLE);
                                    }
                                }
                            })
                            .setDuration(MOVE_ANIMATION_TIME)
                            .start();
                }

                return true;
            }
        }
    }

    private void sendCancelEvent() {
        // Cancel ListView's touch (un-highlighting the item)
        long time = SystemClock.uptimeMillis();
        MotionEvent cancelEvent = MotionEvent.obtain(time, time,
                MotionEvent.ACTION_CANCEL, 0, 0, 0);
        cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
        onTouchEvent(cancelEvent);
        cancelEvent.recycle();
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
                if (mOnToDoListViewTriggerListener != null) {
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
                sendCancelEvent();
            }
        });
        mPendingDismisses.add(new PendingDismissData(dismissPosition, dismissView));
        animator.start();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (firstVisibleItem != 0 && mHeaderView.getVisiableHeight() != 0) {
            mHeaderView.setShowHeight(0);
        }

        mCurrentFirstVisibleItem = firstVisibleItem;
        mCurrentLastVisibleItem = firstVisibleItem + visibleItemCount;

        mPreviousFirstVisibleItem = mPreviousFirstVisibleItem == -1 ? mCurrentFirstVisibleItem : mPreviousFirstVisibleItem;
        mPreviousLastVisibleItem = mPreviousLastVisibleItem == -1 ? mCurrentLastVisibleItem : mPreviousLastVisibleItem;

        if (mHoverDrawable != null) {
            assert dragItemView != null;
            float y = dragItemView.getY();
            mHoverDrawable.onScroll(y);
        }

        if (!mIsSettlingHoverDrawable) {
            checkAndHandleFirstVisibleCellChange();
            checkAndHandleLastVisibleCellChange();
        }

        mPreviousFirstVisibleItem = mCurrentFirstVisibleItem;
        mPreviousLastVisibleItem = mCurrentLastVisibleItem;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE && mHoverDrawable != null) {
            handleMobileCellScroll();
        }
    }

    /**
     * Determines if the listview scrolled down enough to reveal a new cell at the
     * bottom of the list. If so, switches the newly shown view with the mobile view.
     */
    private void checkAndHandleLastVisibleCellChange() {
        if (mHoverDrawable == null || mAdapter == null || mCurrentLastVisibleItem <= mPreviousLastVisibleItem) {
            return;
        }

        int position = getPositionForId(mMobileItemId);
        if (position == AdapterView.INVALID_POSITION) {
            return;
        }

        long switchItemId = position + 1 < mAdapter.getCount() - getHeaderViewsCount()
                ? mAdapter.getItemId(position + 1)
                : AdapterView.INVALID_POSITION;
        View switchView = getViewForId(switchItemId);
        if (switchView != null) {
            switchViews(switchView, switchItemId, switchView.getHeight());
        }
    }

    /**
     * Determines if the listview scrolled up enough to reveal a new cell at the
     * top of the list. If so, switches the newly shown view with the mobile view.
     */
    private void checkAndHandleFirstVisibleCellChange() {
        if (mHoverDrawable == null || mAdapter == null || mCurrentFirstVisibleItem >= mPreviousFirstVisibleItem) {
            return;
        }

        int position = getPositionForId(mMobileItemId);
        if (position == AdapterView.INVALID_POSITION) {
            return;
        }

        long switchItemId = position - 1 >= 0 ? mAdapter.getItemId(position - 1) : AdapterView.INVALID_POSITION;
        View switchView = getViewForId(switchItemId);
        if (switchView != null) {
            switchViews(switchView, switchItemId, -switchView.getHeight());
        }
    }

    @Override
    public void onNewTaskCancelTriggered() {
        if (mOnToDoListViewTriggerListener != null) {
            mOnToDoListViewTriggerListener.onNewTaskCancelTriggered();
        }
    }

    @Override
    public void onNewTaskAddedTriggerd(String item) {
        if (mOnToDoListViewTriggerListener != null) {
            mOnToDoListViewTriggerListener.onNewTaskAddedTriggered(item);
        }
    }
}