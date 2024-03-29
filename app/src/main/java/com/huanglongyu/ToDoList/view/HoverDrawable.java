package com.huanglongyu.ToDoList.view;

/**
 * Created by hly on 11/7/16.
 */

import android.graphics.drawable.BitmapDrawable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.huanglongyu.ToDoList.util.BitmapUtils;


/**
 * A Drawable which represents a dragging {@link View}.
 */
class HoverDrawable extends BitmapDrawable {

    /**
     * The original y coordinate of the top of given {@code View}.
     */
    private float mOriginalY;

    /**
     * The original y coordinate of the position that was touched.
     */
    private float mDownY;

    /**
     * The distance the {@code ListView} has been scrolling while this {@code HoverDrawable} is alive.
     */
    private float mScrollDistance;

    /**
     * Creates a new {@code HoverDrawable} for given {@link View}, using given {@link MotionEvent}.
     *
     * @param view the {@code View} to represent.
     * @param ev   the {@code MotionEvent} to use as down position.
     */
    HoverDrawable(@NonNull final View view, @NonNull final MotionEvent ev) {
        this(view, ev.getY());
    }

    /**
     * Creates a new {@code HoverDrawable} for given {@link View}, using given {@link MotionEvent}.
     *
     * @param view  the {@code View} to represent.
     * @param downY the y coordinate of the down event.
     */
    HoverDrawable(@NonNull final View view, final float downY) {
        super(view.getResources(), BitmapUtils.getBitmapFromView(view));
        mOriginalY = view.getTop();
        mDownY = downY;

        setBounds(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    /**
     * Calculates the new position for this {@code HoverDrawable} using given {@link MotionEvent}.
     *
     * @param ev the {@code MotionEvent}.
     *           {@code ev.getActionMasked()} should typically equal {@link MotionEvent#ACTION_MOVE}.
     */
    void handleMoveEvent(@NonNull final MotionEvent ev) {
        int top = (int) (mOriginalY - mDownY + ev.getY() + mScrollDistance);
        setTop(top);
    }

    /**
     * Updates the original y position of the view, and calculates the scroll distance.
     *
     * @param mobileViewTopY the top y coordinate of the mobile view this {@code HoverDrawable} represents.
     */
    void onScroll(final float mobileViewTopY) {
        mScrollDistance += mOriginalY - mobileViewTopY;
        mOriginalY = mobileViewTopY;
    }

    /**
     * Returns whether the user is currently dragging this {@code HoverDrawable} upwards.
     *
     * @return true if dragging upwards.
     */
    boolean isMovingUpwards() {
        return mOriginalY > getBounds().top;
    }

    /**
     * Returns the number of pixels between the original y coordinate of the view, and the current y coordinate.
     * A negative value means this {@code HoverDrawable} is moving upwards.
     *
     * @return the number of pixels.
     */
    int getDeltaY() {
        return (int) (getBounds().top - mOriginalY);
    }

    /**
     * Returns the top coordinate of this {@code HoverDrawable}.
     */
    int getTop() {
        return getBounds().top;
    }

    /**
     * Sets the top coordinate of this {@code HoverDrawable}.
     */
    void setTop(final int top) {
        setBounds(getBounds().left, top, getBounds().left + getIntrinsicWidth(), top + getIntrinsicHeight());
    }

    /**
     * Shifts the original y coordinates of this {@code HoverDrawable} {code height} pixels upwards or downwards,
     * depending on the move direction.
     *
     * @param height the number of pixels this {@code HoverDrawable} should be moved. Should be positive.
     */
    void shift(final int height) {
        int shiftSize = isMovingUpwards() ? -height : height;
        mOriginalY += shiftSize;
        mDownY += shiftSize;
    }
}
