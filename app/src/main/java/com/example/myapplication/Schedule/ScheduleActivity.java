package com.example.myapplication.Schedule;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.EditText;
import android.widget.CalendarView;

import com.example.myapplication.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

public class ScheduleActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_ALARM = 2;
    private static final int REQUEST_CODE_PERMISSION_NOTIFICATIONS = 1;
    private ArrayList<ScheduleItem> scheduleItems;
    private ScheduleAdapter scheduleAdapter;
    private ListView scheduleListView;
    private FloatingActionButton addScheduleButton;
    private ScheduleDbHelper dbHelper;
    private CalendarView calendarView;
    private String selectedDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        dbHelper = new ScheduleDbHelper(this);
        scheduleItems = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(this, scheduleItems);
        scheduleListView = findViewById(R.id.scheduleListView);
        scheduleListView.setAdapter(scheduleAdapter);
        addScheduleButton = findViewById(R.id.addScheduleButton);
        addScheduleButton.setOnClickListener(v -> showAddScheduleDialog());
        calendarView = findViewById(R.id.calendarView);
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth);
            loadSchedules();
        });

        Calendar calendar = Calendar.getInstance();
        selectedDate = String.format("%d-%02d-%02d", calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        loadSchedules();

        // 请求通知和闹钟权限
        checkAndRequestPermissions();
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_CODE_PERMISSION_NOTIFICATIONS);
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.SCHEDULE_EXACT_ALARM) != PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, Manifest.permission.SET_ALARM) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.SCHEDULE_EXACT_ALARM, Manifest.permission.SET_ALARM},
                        REQUEST_CODE_PERMISSION_ALARM);
            }
        }
    }

    private void loadSchedules() {
        scheduleItems.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query("schedule", null, "date = ?", new String[]{selectedDate}, null, null, null);

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String time = cursor.getString(cursor.getColumnIndexOrThrow("time"));
            String date = cursor.getString(cursor.getColumnIndexOrThrow("date"));

            scheduleItems.add(new ScheduleItem(id, title, time, date));
        }

        cursor.close();
        scheduleAdapter.notifyDataSetChanged();
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_schedule, null);
        builder.setView(view);

        EditText titleEditText = view.findViewById(R.id.scheduleTitleEditText);
        EditText timeEditText = view.findViewById(R.id.scheduleTimeEditText);

        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(ScheduleActivity.this, (view1, hourOfDay, minute1) -> {
                timeEditText.setText(String.format("%02d:%02d", hourOfDay, minute1));
            }, hour, minute, true);
            timePickerDialog.show();
        });

        builder.setPositiveButton("添加", (dialog, which) -> {
            String title = titleEditText.getText().toString();
            String time = timeEditText.getText().toString();
            if (!title.isEmpty() && !time.isEmpty()) {
                addSchedule(title, time);
            } else {
                Toast.makeText(ScheduleActivity.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void showEditScheduleDialog(ScheduleItem scheduleItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_add_schedule, null);
        builder.setView(view);

        EditText titleEditText = view.findViewById(R.id.scheduleTitleEditText);
        EditText timeEditText = view.findViewById(R.id.scheduleTimeEditText);

        titleEditText.setText(scheduleItem.getTitle());
        timeEditText.setText(scheduleItem.getTime());

        timeEditText.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);
            TimePickerDialog timePickerDialog = new TimePickerDialog(ScheduleActivity.this, (view1, hourOfDay, minute1) -> {
                timeEditText.setText(String.format("%02d:%02d", hourOfDay, minute1));
            }, hour, minute, true);
            timePickerDialog.show();
        });

        builder.setPositiveButton("保存", (dialog, which) -> {
            String title = titleEditText.getText().toString();
            String time = timeEditText.getText().toString();
            if (!title.isEmpty() && !time.isEmpty()) {
                updateSchedule(scheduleItem.getId(), title, time);
            } else {
                Toast.makeText(ScheduleActivity.this, "请输入完整信息", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("取消", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void deleteSchedule(int id) {
        new AlertDialog.Builder(this)
                .setTitle("删除确认")
                .setMessage("确定要删除该日程吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    int rowsDeleted = db.delete("schedule", "id = ?", new String[]{String.valueOf(id)});
                    if (rowsDeleted > 0) {
                        Toast.makeText(this, "日程删除成功", Toast.LENGTH_SHORT).show();
                        loadSchedules();
                    } else {
                        Toast.makeText(this, "删除日程失败", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateSchedule(int id, String title, String time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("time", time);

        int rowsUpdated = db.update("schedule", values, "id = ?", new String[]{String.valueOf(id)});
        if (rowsUpdated > 0) {
            Toast.makeText(this, "日程更新成功", Toast.LENGTH_SHORT).show();
            loadSchedules();

            // 获取提醒时间
            Calendar reminderCalendar = Calendar.getInstance();
            String[] timeParts = time.split(":");
            reminderCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            reminderCalendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            reminderCalendar.set(Calendar.SECOND, 0);

            ScheduleItem scheduleItem = new ScheduleItem(id, title, time, selectedDate);
            scheduleItem.setReminderTime(reminderCalendar);  // 设置提醒时间
            setReminder(scheduleItem);
        } else {
            Toast.makeText(this, "更新日程失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void setReminder(ScheduleItem scheduleItem) {
        Intent intent = new Intent(this, ReminderBroadcast.class);
        intent.putExtra("title", "日程提醒");
        intent.putExtra("content", scheduleItem.getTitle());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, scheduleItem.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        long reminderTime = scheduleItem.getReminderTime().getTimeInMillis();
        Log.d("ReminderTime", "Setting reminder for: " + reminderTime);
        try {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(this, "提醒设置失败，缺少必要的权限。", Toast.LENGTH_SHORT).show();
        }
    }

    private void addSchedule(String title, String time) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("time", time);
        values.put("date", selectedDate);

        long newRowId = db.insert("schedule", null, values);
        if (newRowId != -1) {
            Toast.makeText(this, "日程添加成功", Toast.LENGTH_SHORT).show();
            loadSchedules();

            // 设置提醒时间
            Calendar reminderCalendar = Calendar.getInstance();
            reminderCalendar.setTimeInMillis(System.currentTimeMillis());
            String[] timeParts = time.split(":");
            reminderCalendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
            reminderCalendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
            reminderCalendar.set(Calendar.SECOND, 0);

            // 添加日志以调试提醒时间
            Log.d("ReminderTime", "Reminder set for: " + reminderCalendar.getTime());

            ScheduleItem scheduleItem = new ScheduleItem((int) newRowId, title, time, selectedDate);
            scheduleItem.setReminderTime(reminderCalendar);  // 设置提醒时间
            setReminder(scheduleItem);
        } else {
            Toast.makeText(this, "添加日程失败", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_NOTIFICATIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "通知权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "通知权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == REQUEST_CODE_PERMISSION_ALARM) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "闹钟权限已授予", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "闹钟权限被拒绝", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
