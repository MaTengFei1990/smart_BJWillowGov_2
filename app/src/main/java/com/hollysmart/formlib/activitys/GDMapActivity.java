package com.hollysmart.formlib.activitys;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.amap.api.maps.MapView;
import com.hollysmart.bjwillowgov.R;

public class GDMapActivity extends AppCompatActivity {
    MapView mMapView = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gdmap);


        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.map);
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mMapView.onCreate(savedInstanceState);

    }
}
