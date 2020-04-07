package com.hollysmart.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Toast;

import com.hollysmart.value.LocationInfo;
import com.hollysmart.value.MapBean;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by cai on 15/11/4.
 */
public class OtherMap {
    private Context context;
    public OtherMap(Context context) {
        this.context = context;
        initMapData();
    }
    public void selectDialog(final MapIf mapIf){
        if (maps.size()>0){
            String[] mItems = new String[maps.size()];
            for (int i=0; i < maps.size(); i++){
                mItems[i] = maps.get(i).getName();
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("请选择地图");
            builder.setItems(mItems, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    mapIf.selectMap(maps.get(which).getId());
                }
            });
            builder.create().show();

        }else {
            Toast.makeText(context, "您的手机尚未安装百度地图和高德地图，建议您先安装地图软件", Toast.LENGTH_LONG).show();
        }
    }

    public void startBaiduMap(double mlat,double mlng, String unitName){

        double locLat = LocationInfo.getInstance().getLat();
        double locLng = LocationInfo.getInstance().getLng();
        try {
            String map = "intent://map/direction?origin=latlng:"+ locLat + "," + locLng +"|name:我的位置"
                    +"&destination=latlng:"+mlat + ","+  mlng + "|name:" + unitName + "&mode=driving&referer=Autohome|GasStation#Intent;scheme=bdapp;package=com.baidu.BaiduMap;end";
            Intent mIntent = Intent.getIntent(map);
            context.startActivity(mIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startGaoDeMap(double mlat,double mlng, String unitName){

        double locLat = LocationInfo.getInstance().getLat();
        double locLng = LocationInfo.getInstance().getLng();
        try {
            //androidamap://route?sourceApplication=softname&slat=36.2&slon=116.1&sname=abc&dlat=36.3&dlon=116.2&dname=def&dev=0&m=0&t=1
            String map = "androidamap://route?sourceApplication=北京飞絮防治&slat=" + locLat +"&slon="+ locLng
                    +"&sname=" + "我的位置" + "&dlat=" + mlat+ "&dlon=" + mlng+ "&dname=" + unitName + "&dev=1&m=0&t=2";
            Intent intent = new Intent("android.intent.action.VIEW",
                    android.net.Uri.parse(map));
            intent.setPackage("com.autonavi.minimap");
            context.startActivity(intent);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private boolean isInstallByread(String packageName) {

        return new File("/data/data/" + packageName).exists();
    }
    private List<MapBean> maps = new ArrayList<>();
    public final int GAODETAG = 1;
    public final int BAIDUTAG = 2;
    public static final String PN_GAODE_MAP = "com.autonavi.minimap";// 高德地图包名
    public static final String PN_BAIDU_MAP = "com.baidu.BaiduMap"; // 百度地图包名

    private void initMapData(){
        if (isInstallByread(PN_GAODE_MAP)){
            MapBean mapBean = new MapBean();
            mapBean.setId(GAODETAG);
            mapBean.setName("高德地图");
            mapBean.setPkg(PN_GAODE_MAP);
            maps.add(mapBean);
        }
        if (isInstallByread(PN_BAIDU_MAP)){
            MapBean mapBean = new MapBean();
            mapBean.setId(BAIDUTAG);
            mapBean.setName("百度地图");
            mapBean.setPkg(PN_BAIDU_MAP);
            maps.add(mapBean);
        }
    }
    public interface MapIf{
        void selectMap(int tag);
    }

}
