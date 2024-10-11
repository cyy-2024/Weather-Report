package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.baidu.mapapi.SDKInitializer;
import com.example.myapplication.Schedule.ScheduleActivity;
import com.example.myapplication.musicplayer.MusicPlayerActivity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationHelper.OnLocationReceivedListener {
    private TextView locationCityName;
    private EditText searchCityName;
    private Button searchCityButton;
    private ListView listView;
    private TextView textViewTime;
    private Spinner citySpinner;
    private MyHandler handler = new MyHandler();
    private ArrayList<Weather> list;
    private String selectedCityId = "101010100";
    private String currentCityName; // 当前定位城市名
    private TextView detailWeatherName, detailHighLowTemp, detailWindDirection, detailWindSpeed;
    private Button musicPlayerButton, scheduleButton;
    private ImageView weatherIcon;
    private LocationHelper locationHelper;
    private ArrayAdapter<CharSequence> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isNightMode = sharedPreferences.getBoolean("night_mode", false);

        if (isNightMode) {
            setTheme(R.style.AppTheme_Night);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);

        // 同意隐私合规政策
        SDKInitializer.setAgreePrivacy(getApplicationContext(), true);

        // 初始化 SDK
        SDKInitializer.initialize(getApplicationContext());

        setContentView(R.layout.detail);
        locationCityName = findViewById(R.id.locationCityName);
        searchCityName = findViewById(R.id.searchCityName);
        searchCityButton = findViewById(R.id.searchCityButton);

        textViewTime = findViewById(R.id.textViewTime);
        listView = findViewById(R.id.listView);
        citySpinner = findViewById(R.id.citySpinner);
        detailWeatherName = findViewById(R.id.detail_weather_name);
        detailHighLowTemp = findViewById(R.id.detail_high_low_temp);
        detailWindDirection = findViewById(R.id.detail_wind_direction);
        detailWindSpeed = findViewById(R.id.detail_wind_speed);
        musicPlayerButton = findViewById(R.id.musicPlayerButton);
        scheduleButton = findViewById(R.id.scheduleButton);
        weatherIcon = findViewById(R.id.dForecastImage);
        Button switchThemeButton = findViewById(R.id.switchThemeButton);

        // 初始化 LocationHelper 实例
        locationHelper = new LocationHelper(this, this);
        locationHelper.startLocationUpdates();

        // 设置 Spinner 适配器
        adapter = ArrayAdapter.createFromResource(this,
                R.array.cities_array, R.layout.spinner_item);
        adapter.setDropDownViewResource(R.layout.spinner_item);
        citySpinner.setAdapter(adapter);
        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (view != null && view instanceof TextView) {
                    ((TextView) view).setTextColor(Color.BLACK);
                }
                selectedCityId = getCityIdFromPosition(position);
                String selectedCityName = parent.getItemAtPosition(position).toString();
                locationCityName.setText(selectedCityName); // 更新显示的城市名
                loadWeatherDataByCityId(selectedCityId); // 加载选中城市的天气数据
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        switchThemeButton.setOnClickListener(v -> switchTheme(!isNightMode));

        musicPlayerButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MusicPlayerActivity.class)));

        scheduleButton.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, ScheduleActivity.class)));

        searchCityButton.setOnClickListener(v -> {
            String cityName = searchCityName.getText().toString().trim();
            if (!cityName.isEmpty()) {
                loadWeatherDataByCityName(cityName);
            } else {
                Toast.makeText(MainActivity.this, "请输入城市名称", Toast.LENGTH_SHORT).show();
            }
        });

        // 启动时间线程
        Thread timeThread = new Thread(new TimeThread());
        timeThread.start();
    }

    @Override
    public void onLocationReceived(String cityName) {
        this.currentCityName = cityName.replace("市", ""); // 去掉“市”字
        locationCityName.setText(this.currentCityName);
        loadWeatherDataByCityName(this.currentCityName);
    }

    private void loadWeatherDataByCityId(String cityId) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用，请检查您的网络连接。", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = getWeatherUrlByCityId(cityId);
        new MyThread(url).start();
    }

    private void loadWeatherDataByCityName(String cityName) {
        if (!NetworkUtil.isNetworkAvailable(this)) {
            Toast.makeText(this, "网络不可用，请检查您的网络连接。", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = getWeatherUrlByCityName(cityName);
        new MyThread(url).start();
        locationCityName.setText(cityName); // 更新显示的城市名
    }

    private String getWeatherUrlByCityId(String cityId) {
        return "http://v1.yiketianqi.com/free/week?unescape=1&appid=22331119&appsecret=3SdGgijt&cityid=" + cityId;
    };


    private String getWeatherUrlByCityName(String cityName) {
        return "http://v1.yiketianqi.com/free/week?unescape=1&appid=22331119&appsecret=3SdGgijt&city=" + cityName;
    }

    private String getCityIdFromPosition(int position) {
        switch (position) {
            case 0: return "101010100"; // 北京
            case 1: return "101070101"; // 沈阳
            case 2: return "101020100"; // 上海
            case 3: return "101280101"; // 广州
            case 4: return "101280601"; // 深圳
            case 5: return "101030100"; // 天津
            case 6: return "101040100"; // 重庆
            case 7: return "101210101"; // 杭州
            case 8: return "101190101"; // 南京
            case 9: return "101270101"; // 成都
            case 10: return "101200101"; // 武汉
            case 11: return "101110101"; // 西安
            case 12: return "101250101"; // 长沙
            case 13: return "101120201"; // 青岛
            case 14: return "101070201"; // 大连
            case 15: return "101070101"; // 沈阳
            case 16: return "101120101"; // 济南
            case 17: return "101050101"; // 哈尔滨
            case 18: return "101180101"; // 郑州
            case 19: return "101230201"; // 厦门
            case 20: return "101190401"; // 苏州
            case 21: return "101210401"; // 宁波
            case 22: return "101230101"; // 福州
            case 23: return "101220101"; // 合肥
            case 24: return "101090101"; // 石家庄
            case 25: return "101240101"; // 南昌
            case 26: return "101290101"; // 昆明
            case 27: return "101060101"; // 长春
            case 28: return "101160101"; // 兰州
            case 29: return "101100101"; // 太原
            case 30: return "101260101"; // 贵阳
            case 31: return "101130101"; // 乌鲁木齐
            case 32: return "101310101"; // 海口
            default: return "101010100"; // 默认城市
        }
    }

    class MyHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                updateListView();
                if (list != null && !list.isEmpty()) {
                    Weather todayWeather = list.get(0);
                    detailWeatherName.setText(todayWeather.getWea());
                    detailHighLowTemp.setText(todayWeather.getTem_day() + "°C / " + todayWeather.getTem_night() + "°C");
                    detailWindDirection.setText(todayWeather.getWin());
                    detailWindSpeed.setText(todayWeather.getWin_speed());
                    textViewTime.setText(new Date().toLocaleString());
                    int resId = getWeatherIconResId(todayWeather.getWea_img());
                    weatherIcon.setImageResource(resId);
                }
            } else if (msg.what == 2) {
                textViewTime.setText(new Date().toLocaleString());
            }
        }
    }

    private int getWeatherIconResId(String wea_img) {
        switch (wea_img) {
            case "qing":
                return R.drawable.sun;
            case "yin":
                return R.drawable.cloudy;
            case "yu":
                return R.drawable.weather_lightrain;
            case "yun":
                return R.drawable.cloudy;
            default:
                return R.drawable.weather_sunny_n;
        }
    }

    class TimeThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    handler.sendEmptyMessage(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MyThread extends Thread {
        private String url;

        MyThread(String url) {
            this.url = url;
        }

        @Override
        public void run() {
            super.run();
            try {
                String str = NetUtil.net(url, null, "GET");
                Log.i("NET", str);
                JSONObject jsonObject = JSON.parseObject(str);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                list = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonData = jsonArray.getJSONObject(i);
                    Weather weather = new Weather();
                    weather.setDate(jsonData.getString("date"));
                    weather.setTem_day(jsonData.getString("tem_day"));
                    weather.setTem_night(jsonData.getString("tem_night"));
                    weather.setWea(jsonData.getString("wea"));
                    weather.setWin(jsonData.getString("win"));
                    weather.setWin_speed(jsonData.getString("win_speed"));
                    weather.setWea_img(jsonData.getString("wea_img"));
                    list.add(weather);
                }
                handler.sendEmptyMessage(1);
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("MyThread", "IOException occurred: " + e.getMessage());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("MyThread", "JSONException occurred: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("MyThread", "Exception occurred: " + e.getMessage());
            }
        }
    }

    private void switchTheme(boolean isNightMode) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("night_mode", isNightMode);
        editor.apply();
        recreate();
    }

    private void updateListView() {
        if (list != null && list.size() > 1) {
            WeatherAdapter adapter = new WeatherAdapter(MainActivity.this, new ArrayList<>(list.subList(1, list.size())));
            listView.setAdapter(adapter);
        }
    }
}
