package com.example.myapplication.Schedule;

import java.util.Calendar;

public class ScheduleItem {
    private int id;
    private String title;
    private String time;
    private String date;
    private Calendar reminderTime;  // 添加提醒时间字段

    public ScheduleItem(int id, String title, String time, String date) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public Calendar getReminderTime() {
        return reminderTime;
    }

    public void setReminderTime(Calendar reminderTime) {
        this.reminderTime = reminderTime; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDate(String date) {
        this.date = date;
    }
}







