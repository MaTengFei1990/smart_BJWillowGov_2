package com.hollysmart.formlib;

import android.content.Context;

import com.amap.api.maps.AMap;
import com.amap.api.maps.model.LatLng;
import com.hollysmart.cluster.ClusterClickListener;
import com.hollysmart.cluster.ClusterItem;
import com.hollysmart.cluster.RegionItem;

import java.util.List;
import java.util.Vector;

public class DealRunable implements Runnable {
    public List<String> testBeanList;
    public Vector<ClusterItem> items;
    public Context context;
    public AMap mGaoDeMap;
    private int clusterRadius = 100; //半径
    private ClusterClickListener clusterClickListener;

    public DealRunable(List<String> subList, Vector<ClusterItem> items, Context context, AMap mGaoDeMap, ClusterClickListener clusterClickListener) {
        testBeanList = subList;
        this.items = items;
        this.context = context;
        this.mGaoDeMap = mGaoDeMap;
        this.clusterClickListener = clusterClickListener;
    }

    @Override
    public void run() {
        for (String testBean : testBeanList) {
            String[] split = testBean.split(",");
            Double lng = Double.parseDouble(split[1]);
            Double lat = Double.parseDouble(split[2]);
            LatLng latLng = new LatLng(lat, lng, false);
            RegionItem regionItem1 = new RegionItem(latLng, split[0]);
            items.add(regionItem1);


        }


    }


    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public int dp2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
