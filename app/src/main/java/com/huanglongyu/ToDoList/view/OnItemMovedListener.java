package com.huanglongyu.ToDoList.view;

/**
 * Created by hly on 11/10/16.
 */

/**
 * An interface which provides a callback that is called when an item has moved.
 */
public interface OnItemMovedListener {

    /**
     * Called when an item that was dragged has been dropped.
     *
     * @param originalPosition the original position of the item that was dragged.
     * @param newPosition the new position of the item that was dragged.
     */
    void onItemMoved(int originalPosition, int newPosition);
}
