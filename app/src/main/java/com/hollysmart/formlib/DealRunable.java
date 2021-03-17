package com.hollysmart.formlib;

import com.amap.api.maps.model.LatLng;
import com.hollysmart.beans.HistTreeBean;
import com.hollysmart.cluster.ClusterItem;
import com.hollysmart.cluster.RegionItem;

import java.util.List;
import java.util.Vector;

public class DealRunable implements Runnable {
    public List<HistTreeBean> testBeanList;
    public Vector<ClusterItem> items;

    public DealRunable(List<HistTreeBean> subList, Vector<ClusterItem> items) {
        testBeanList = subList;
        this.items = items;
    }

    @Override
    public void run() {
        for (HistTreeBean testBean : testBeanList) {
            String[] split = testBean.getContent().split(",");
            Double lng = Double.parseDouble(split[1]);
            Double lat = Double.parseDouble(split[2]);
            LatLng latLng = new LatLng(lat, lng, false);
            RegionItem regionItem1 = new RegionItem(latLng, split[0]);
            items.add(regionItem1);


        }


    }


}
