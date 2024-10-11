package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WeatherDetailActivity extends AppCompatActivity {

    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat outputDateFormat = new SimpleDateFormat("MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_detail);
        TextView dateTextView = findViewById(R.id.detail_date);
        ImageView weatherIconImageView = findViewById(R.id.detail_weather_icon);
        TextView dayTemperatureTextView = findViewById(R.id.detail_temperature_day);
        TextView nightTemperatureTextView = findViewById(R.id.detail_temperature_night);
        TextView weatherDescriptionTextView = findViewById(R.id.detail_weather_description);
        TextView windTextView = findViewById(R.id.detail_wind);
        TextView windSpeedTextView = findViewById(R.id.detail_wind_speed);
        TextView tipTextView = findViewById(R.id.detail_tip);

        Intent intent = getIntent();
        String date = intent.getStringExtra("date");
        String dayTemperature = "最高气温"+intent.getStringExtra("dayTemperature") + "°C";
        String nightTemperature = "最低气温"+intent.getStringExtra("nightTemperature") + "°C";
        String weatherDescription = intent.getStringExtra("weatherDescription");
        String wind = "风向"+intent.getStringExtra("wind");
        String windSpeed = intent.getStringExtra("windSpeed");
        String weaImg = intent.getStringExtra("weaImg");

        dateTextView.setText(formatDate(date));
        dayTemperatureTextView.setText(dayTemperature);
        nightTemperatureTextView.setText(nightTemperature);
        weatherDescriptionTextView.setText(weatherDescription);
        windTextView.setText(wind);
        windSpeedTextView.setText(windSpeed);
        tipTextView.setText(getTipForWeather(weatherDescription));

        // 根据天气图标名称设置图标资源
        int weatherIconResId = getWeatherIconResId(weaImg);
        if (weatherIconResId != -1) {
            weatherIconImageView.setImageResource(weatherIconResId);
        }
    }

    private int getWeatherIconResId(String weaImg) {
        switch (weaImg) {
            case "qing":
                return R.drawable.weather_sunny;
            case "yu":
                return R.drawable.weather_rain;
            case "yun":
                return R.drawable.weather_cloudy;
            case "yin":
                return R.drawable.cloudy;

            default:
                return -1;
        }
    }

    private String formatDate(String date) {
        try {
            Date parsedDate = inputDateFormat.parse(date);
            return outputDateFormat.format(parsedDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return date; //错误时返回原日期
        }
    }

    private String getTipForWeather(String weatherDescription) {
        switch (weatherDescription) {
            case "晴":
                return "今天天气不错，不如去外面郊游吧！";
            case "多云":
                return "天气多云，可以出去走走，但注意别晒太久。";
            case "雨":
                return "今天有雨，记得带伞，注意保暖。";
            case "阴":
                return "今日阴天，适合在室内活动。";
            case "多云转阵雨":
                return "多云转阵雨，外出时带把伞以防万一。";
            case "阵雨":
                return "阵雨天气，出门记得带伞。";
            case "晴转阴":
                return "晴转阴，天气变化较快，注意保暖。";
            case "雷阵雨":
                return "雷阵雨天气，尽量避免外出，注意安全。";
            case "小雨转晴":
                return "小雨转晴，雨后天晴，空气清新。";
            default:
                return "请注意天气变化，合理安排出行计划。";
        }
    }
}





