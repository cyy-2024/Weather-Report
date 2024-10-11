package com.example.myapplication;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;

import java.util.HashMap;
import java.util.Map;

public class LocationHelper {
    private Context context;
    private OnLocationReceivedListener listener;
    private LocationClient locationClient;
    private GeoCoder mGeoCoder;
    private Map<String, String> cityNameToIdMap = new HashMap<>();

    public LocationHelper(Context context, OnLocationReceivedListener listener) {
        this.context = context.getApplicationContext();  // 使用 ApplicationContext
        this.listener = listener;

        // 设置隐私政策同意并初始化 SDK
        SDKInitializer.setAgreePrivacy(this.context, true);  // 同意隐私政策
        SDKInitializer.initialize(this.context);  // 初始化 SDK

        LocationClient.setAgreePrivacy(true);  // 同意隐私政策

        try {
            locationClient = new LocationClient(this.context);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (locationClient != null) {
            mGeoCoder = GeoCoder.newInstance();
            setupLocationClient();
            initializeCityNameToIdMap();
        } else {
            Toast.makeText(context, "LocationClient 初始化失败", Toast.LENGTH_LONG).show();
        }
    }

    private void initializeCityNameToIdMap() {
        cityNameToIdMap.put("北京", "101010100");
        cityNameToIdMap.put("沈阳", "101070101");
        cityNameToIdMap.put("上海", "101020100");
        cityNameToIdMap.put("广州", "101280101");
        cityNameToIdMap.put("深圳", "101280601");
        cityNameToIdMap.put("成都", "101270101");
        // 添加更多城市...
    }

    public Map<String, String> getCityNameToIdMap() {
        return cityNameToIdMap;
    }

    private void setupLocationClient() {
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
        option.setScanSpan(10000);
        locationClient.setLocOption(option);

        locationClient.registerLocationListener(new BDAbstractLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation location) {
                if (location != null) {
                    LatLng point = new LatLng(location.getLatitude(), location.getLongitude());
                    String cityName = location.getCity();
                    Log.d("LocationHelper", "Received city from BDLocation: " + cityName);
                    reverseGeoCode(point);
                }
            }
        });

        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                if (result == null || result.error != SearchResult.ERRORNO.NO_ERROR) {
                    Toast.makeText(context, "抱歉，未能找到结果", Toast.LENGTH_LONG).show();
                } else {
                    String cityName = result.getAddressDetail().city;
                    Log.d("LocationHelper", "Received city from GeoCoder: " + cityName);
                    listener.onLocationReceived(cityName);
                }
            }

            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
                // 如果需要，处理正地理编码结果
            }
        });
    }

    public void startLocationUpdates() {
        if (locationClient != null && !locationClient.isStarted()) {
            locationClient.start();
        }
    }

    public void stopLocationUpdates() {
        if (locationClient != null && locationClient.isStarted()) {
            locationClient.stop();
        }
        mGeoCoder.destroy();
    }

    private void reverseGeoCode(LatLng point) {
        mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(point).newVersion(1).radius(500));
    }

    public interface OnLocationReceivedListener {
        void onLocationReceived(String cityName);
    }
}
