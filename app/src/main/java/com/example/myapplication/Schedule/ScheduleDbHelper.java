package com.example.myapplication.Schedule;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class ScheduleDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2; // 确保这与你的数据库版本匹配
    private static final String DATABASE_NAME = "schedule.db";

    public ScheduleDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 创建表的SQL语句
        String SQL_CREATE_SCHEDULE_TABLE = "CREATE TABLE schedule (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT NOT NULL, " +
                "description TEXT, " +
                "date TEXT NOT NULL, " +
                "time TEXT NOT NULL);";

        db.execSQL(SQL_CREATE_SCHEDULE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 处理数据库升级
        if (oldVersion < newVersion) {
            db.execSQL("DROP TABLE IF EXISTS schedule");
            onCreate(db);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // 处理数据库降级
        if (oldVersion > newVersion) {
            db.execSQL("DROP TABLE IF EXISTS schedule");
            onCreate(db);
        }
    }
}






