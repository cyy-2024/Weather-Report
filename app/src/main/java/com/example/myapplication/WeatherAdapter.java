package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class WeatherAdapter extends ArrayAdapter<Weather> {

    private Context mContext;
    private ArrayList<Weather> mWeatherList;
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM-dd");

    public WeatherAdapter(Context context, ArrayList<Weather> weatherList) {
        super(context, 0, weatherList);
        mContext = context;
        mWeatherList = weatherList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItem = convertView;
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.list_weather_item, parent, false);
        }

        Weather currentWeather = mWeatherList.get(position);

        ImageView weatherIcon = listItem.findViewById(R.id.weatherIcon);
        TextView dateTextView = listItem.findViewById(R.id.dateTextView);
        TextView weatherTextView = listItem.findViewById(R.id.weatherTextView);
        TextView highTextView = listItem.findViewById(R.id.highTextView);
        TextView lowTextView = listItem.findViewById(R.id.lowTextView);

        // 设置列表项中的数据
        // 修改：根据 wea_img 设置天气图标
        int iconResId = getWeatherIconResId(currentWeather.getWea_img());
        weatherIcon.setImageResource(iconResId);
        dateTextView.setText(formatDate(currentWeather.getDate()));
        weatherTextView.setText(currentWeather.getWea());
        highTextView.setText(currentWeather.getTem_day() + "°C");
        lowTextView.setText(currentWeather.getTem_night() + "°C");

        // 设置点击事件，启动 WeatherDetailActivity 并传递详细信息
        listItem.setOnClickListener(v -> {
            Intent intent = new Intent(mContext, WeatherDetailActivity.class);
            intent.putExtra("date", currentWeather.getDate());
            intent.putExtra("dayTemperature", currentWeather.getTem_day());
            intent.putExtra("nightTemperature", currentWeather.getTem_night());
            intent.putExtra("weatherDescription", currentWeather.getWea());
            intent.putExtra("wind", currentWeather.getWin());
            intent.putExtra("windSpeed", currentWeather.getWin_speed());
            intent.putExtra("weaImg", currentWeather.getWea_img());
            intent.putExtra("weatherIconResId", iconResId);
            mContext.startActivity(intent);
        });

        return listItem;
    }

    // 修改：根据天气类型获取对应的天气图标资源ID
    int getWeatherIconResId(String wea_img) {
        switch (wea_img) {
            case "qing":
                return R.drawable.weather_sunny;
            case "yu":
                return R.drawable.weather_rain;
            case "yun":
                return R.drawable.weather_mostlysunny;
            case "yin":
                return R.drawable.cloudy;
            default:
                return -1;
        }
    }


    // 格式化日期，只显示月份和日期
    private String formatDate(String date) {
        try {
            Date parsedDate = inputDateFormat.parse(date);
            return outputDateFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; // 如果解析失败，则返回原始日期字符串
        }
    }
}
