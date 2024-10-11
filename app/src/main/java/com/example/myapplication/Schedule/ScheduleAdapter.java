package com.example.myapplication.Schedule;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.myapplication.R;

import java.util.ArrayList;

public class ScheduleAdapter extends ArrayAdapter<ScheduleItem> {

    private final Context context;
    private final ArrayList<ScheduleItem> scheduleItems;

    public ScheduleAdapter(Context context, ArrayList<ScheduleItem> scheduleItems) {
        super(context, 0, scheduleItems);
        this.context = context;
        this.scheduleItems = scheduleItems;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_schedule, parent, false);
        }

        ScheduleItem scheduleItem = scheduleItems.get(position);

        TextView titleTextView = convertView.findViewById(R.id.scheduleTitleTextView);
        TextView timeTextView = convertView.findViewById(R.id.scheduleTimeTextView);
        ImageButton editButton = convertView.findViewById(R.id.editButton);
        ImageButton deleteButton = convertView.findViewById(R.id.deleteButton);

        titleTextView.setText(scheduleItem.getTitle());
        timeTextView.setText(scheduleItem.getTime());

        editButton.setOnClickListener(v -> {
            if (context instanceof ScheduleActivity) {
                ((ScheduleActivity) context).showEditScheduleDialog(scheduleItem);
            }
        });

        deleteButton.setOnClickListener(v -> {
            if (context instanceof ScheduleActivity) {
                ((ScheduleActivity) context).deleteSchedule(scheduleItem.getId());
            }
        });

        return convertView;
    }
}










