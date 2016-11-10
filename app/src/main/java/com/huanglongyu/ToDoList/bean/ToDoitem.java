package com.huanglongyu.ToDoList.bean;

/**
 * Created by hly on 11/9/16.
 */

public class ToDoitem {
    private int id;
    private String content;
    private int color;
    private int isDone;
    private int timeStamp;

    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getColor() {
        return color;
    }

    public int getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public int getIsDone() {
        return isDone;
    }

    public int getTimeStamp() {
        return timeStamp;
    }
}
