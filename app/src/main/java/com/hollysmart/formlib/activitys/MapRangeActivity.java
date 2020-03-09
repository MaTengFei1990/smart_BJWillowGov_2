package com.hollysmart.formlib.activitys;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolygonOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.beans.GPS;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.FormModelBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.main.MainView;
import com.hollysmart.gridslib.apis.FindListPage2API;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.GPSConverterUtils;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapRangeActivity extends StyleAnimActivity implements View.OnClickListener, MainView ,BaiduMap.OnMapLoadedCallback{

    @Override
    public int layoutResID() {
        return R.layout.activity_map_range;
    }



    private MapView mMapView;
    BaiduMap mBaiduMap;

    // 定位相关
    LocationClient mLocClient;

    public MyLocationListener myListener = new MyLocationListener();


    private PolygonOptions mPolygonOptions;


    boolean isFirstLoc = true;// 是否首次定位

    private ImageButton bn_weixing;
    private ImageButton bn_dingwei;
    private ImageButton imagbtn_enlarge;
    private ImageButton imagbtn_zoomOut;
    private LatLng mLatLng;

    //多边形顶点位置
    private List<LatLng> points = new ArrayList<>();

    private DongTaiFormBean dongTaiFormBean;
    private int flagtype=0;

    private static final int MARKER_FLAG = 1;
    private static final int LINE_FLAG = 2;
    private static final int PLANE_FLAG = 3;

    private boolean isCheck;

    private ResDataBean roadbean;
    private ResDataBean tree_resDataBean;
    private ProjectBean projectBean;
    private ImageView iv_center;

    private String isEdit = "";  // 是否可修改  0：不能修改 1：可以修改 @property (nonatomic, copy) NSMutableString

    @Override
    public void findView() {
        findViewById(R.id.iv_back).setOnClickListener(this);

        findViewById(R.id.btn_chexiao).setOnClickListener(this);
        findViewById(R.id.btn_save).setOnClickListener(this);
        findViewById(R.id.btn_add).setOnClickListener(this);

        bn_weixing = findViewById(R.id.bn_weixing);
        bn_dingwei = findViewById(R.id.bn_dingwei);
        iv_center = findViewById(R.id.iv_center);
        imagbtn_enlarge = findViewById(R.id.imagbtn_enlarge);
        imagbtn_zoomOut = findViewById(R.id.imagbtn_zoomOut);

        bn_weixing.setOnClickListener(this);
        bn_dingwei.setOnClickListener(this);
        imagbtn_enlarge.setOnClickListener(this);
        imagbtn_zoomOut.setOnClickListener(this);


        mMapView = (MapView) findViewById(R.id.mMap);
        mMapView.showZoomControls(false);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(18);
        mBaiduMap.animateMapStatus(u);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
        mLocClient = new LocationClient(getApplicationContext());
        //声明LocationClient类
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);// 打开gps
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.setWifiCacheTimeOut(5 * 60 * 1000);
        option.setCoorType("bd09ll"); // 设置坐标类型
        option.setScanSpan(3000);
        mLocClient.setLocOption(option);
        mLocClient.start();
        mBaiduMap.setOnMarkerClickListener(this);

        // 编辑取景
        mBaiduMap.setOnMapStatusChangeListener(this);


    }



    //是否是查看，true查看，不能编辑

    @Override
    public void init() {
        isLogin();

        mPolygonOptions = new PolygonOptions();
        mPolygonOptions.fillColor(getResources().getColor(R.color.titleBg));
        mPolygonOptions.visible(false);
        mPolygonOptions.stroke(new Stroke(5, getResources().getColor(R.color.titleBg)));


        String falg = getIntent().getStringExtra("falg");
        isCheck = getIntent().getBooleanExtra("isCheck", false);
        dongTaiFormBean = (DongTaiFormBean) getIntent().getSerializableExtra("bean");
        roadbean = (ResDataBean) getIntent().getSerializableExtra("roadbean");

        tree_resDataBean = (ResDataBean) getIntent().getSerializableExtra("tree_resDataBean");
        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");

        isEdit = dongTaiFormBean.getIsEdit();

        if (falg.equals("marker")) {
            flagtype = 1;
        }
        if (falg.equals("plane")) {
            flagtype = 3;
        }
        if (falg.equals("line")) {
            flagtype = 2;
        }

        if (isCheck) {
            findViewById(R.id.btn_add).setVisibility(View.GONE);
            findViewById(R.id.btn_chexiao).setVisibility(View.GONE);
            findViewById(R.id.btn_save).setVisibility(View.GONE);
            findViewById(R.id.bn_dingwei).setVisibility(View.GONE);

        }

        if (!Utils.isEmpty(isEdit) && isEdit.equals("0")) {

            findViewById(R.id.btn_add).setVisibility(View.GONE);
            findViewById(R.id.btn_chexiao).setVisibility(View.GONE);
            findViewById(R.id.bn_dingwei).setVisibility(View.GONE);
            iv_center.setVisibility(View.GONE);
        }

        mBaiduMap.setOnMapLoadedCallback(this);


        // 加载最近的树木点
        if (tree_resDataBean != null) {

            loadRecentData();


        }



    }



    private int pageNo=1;
    private int pageSize=30;

    private  List<LatLng> treesPoints = new ArrayList<>();


    private void loadRecentData() {
        new FindListPage2API(pageNo, pageSize, userInfo, tree_resDataBean.getFd_resmodelid(),projectBean , roadbean.getId(), new FindListPage2API.DatadicListCountIF() {
            @Override
            public void datadicListResult(boolean isOk, List<ResDataBean> menuBeanList, int allCount) {


                if (isOk) {
                    for (int i = 0; i < menuBeanList.size(); i++) {

                        ResDataBean resDataBean = menuBeanList.get(i);

                            GPS gps = GPSConverterUtils.Gps84_To_bd09(new Double(resDataBean.getLatitude()),
                                    new Double(resDataBean.getLongitude()));
                            LatLng latLng1 = new LatLng(gps.getLat(), gps.getLon());

                            treesPoints.add(latLng1);

                    }

                    drawTreesInMap(treesPoints);


                }



            }
        }).request();

    }



    private void drawTreesInMap(List<LatLng> points) {


        for (LatLng latLng : points) {

            if (latLng != null) {
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.mipmap.resflag_add);
                OverlayOptions markeroption = new MarkerOptions()
                        .position(latLng)
                        .icon(bitmap);

                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(markeroption);
            }
        }





    }


    private void drawRange(String ranges,int type) {

        if (Utils.isEmpty(ranges)) {

            return;
        }

        //点
        if (type == MARKER_FLAG) {
            String[] latLng = ranges.split(",");

            LatLng latLng1 = new LatLng(new Double(latLng[0]), new Double(latLng[1]));

            points.add(latLng1);
        }
        //线 面
        if (type == PLANE_FLAG||type==LINE_FLAG) {

            if (ranges.contains("|")) {

                String[] str_ranges = ranges.split("\\|");


                for (int i = 0; i < str_ranges.length; i++) {

                    String[] latLng = str_ranges[i].split(",");

                    LatLng latLng1 = new LatLng(new Double(latLng[0]), new Double(latLng[1]));

                    points.add(latLng1);
                }


            }
        }



        drawRangeInMap(type);


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.btn_chexiao:
                cheXiao();
                break;
            case R.id.btn_save:
                save();
                break;
            case R.id.btn_add:
                add(target);
                break;
            case R.id.bn_weixing:
                mapChaged();
                break;
            case R.id.bn_dingwei:
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLatLng);
                mBaiduMap.animateMapStatus(u);
                break;
            case R.id.imagbtn_enlarge:
                ZoomChange(true);
                break;
            case R.id.imagbtn_zoomOut:
                ZoomChange(false);
                break;

        }
    }

    public void mapChaged() {

        int mapType = mBaiduMap.getMapType();
        if (mapType != 1) {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        } else {
            mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
        }
    }


    /**
     * 地图进行缩放
     *
     * @param b
     */
    public void ZoomChange(boolean b) {

        baiDuMapZoomChange(mBaiduMap, b);
    }


    /**
     * 对地图进行缩放
     *
     * @param mBaiduMap
     * @param b
     */
    public void baiDuMapZoomChange(BaiduMap mBaiduMap, boolean b) {

        float zoomLevel = mBaiduMap.getMapStatus().zoom;
        Mlog.d("zoom:" + zoomLevel);
        if (b) {
            if (zoomLevel < mBaiduMap.getMaxZoomLevel()) {
                zoomLevel = zoomLevel + 1;
            }
        } else {
            if (zoomLevel > mBaiduMap.getMinZoomLevel()) {
                zoomLevel = zoomLevel - 1;
            }
        }
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(zoomLevel);
        mBaiduMap.animateMapStatus(u);

    }


    /***
     * 添加
     */
    private void add(LatLng latLng) {
        if (latLng == null) {
            return;
        }

        switch (flagtype) {


            case MARKER_FLAG:
                if (points.contains(latLng)) {

                    Utils.showToast(mContext, "已添加该坐标点");

                    return;
                }
                points.clear();

                points.add(latLng);
                drawRangeInMap(flagtype);
                break;
            case LINE_FLAG:
                if (points.contains(latLng)) {

                    Utils.showToast(mContext, "已添加该坐标点");

                    return;
                }


                points.add(latLng);
                drawRangeInMap(flagtype);
                break;
            case PLANE_FLAG:
                if (points.contains(latLng)) {

                    Utils.showToast(mContext, "已添加该坐标点");

                    return;
                }


                points.add(latLng);
                drawRangeInMap(flagtype);
                break;

        }




    }

    private void drawRangeInMap(int type) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.resflag_add);
        mBaiduMap.clear();

        drowLineInMap();

        drawTreesInMap(treesPoints);

        if (points == null || points.size() == 0) {

            return;

        }









        switch (type) {
            case MARKER_FLAG:
                if (points != null && points.size() > 0) {

                    drawTreesInMap(points);

                }
                OverlayOptions markeroption = new MarkerOptions()
                        .position(points.get(0))
                        .icon(bitmap);
                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(markeroption);

                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(points.get(0));
                mBaiduMap.animateMapStatus(u);

                break;
            case LINE_FLAG:

                if (points.size() < 2) {

                    OverlayOptions linepoint = new MarkerOptions()
                            .position(points.get(0))
                            .icon(bitmap);
                    //在地图上添加Marker，并显示
                    mBaiduMap.addOverlay(linepoint);

                } else {
                    //在地图上显示多边形
                    OverlayOptions ooPolyline2 = new PolylineOptions().width(10).color(R.color.titleBg).points(points);
                    mBaiduMap.addOverlay(ooPolyline2);

                }

                break;
            case PLANE_FLAG:
                if (points.size() < 3) {

                    //构建MarkerOption，用于在地图上添加Marker
                    for (int i = 0; i < points.size(); i++) {
                        OverlayOptions option = new MarkerOptions()
                                .position(points.get(i))
                                .icon(bitmap);
                        //在地图上添加Marker，并显示
                        mBaiduMap.addOverlay(option);
                    }

                } else {
                    //在地图上显示多边形
                    mPolygonOptions.points(points);
                    mBaiduMap.addOverlay(mPolygonOptions);

                    for (int i = 0; i < points.size(); i++) {
                        OverlayOptions option = new MarkerOptions()
                                .position(points.get(i))
                                .icon(bitmap);
                        //在地图上添加Marker，并显示
                        mBaiduMap.addOverlay(option);
                    }


                }
                break;
        }



    }

    private void drowLineInMap() {


        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        if (roadbean != null) {

            try {
                FormModelBean formModel = roadbean.getFormModel();
                if (formModel != null) {
                    List<DongTaiFormBean> cgformFieldList = formModel.getCgformFieldList();

                    for (DongTaiFormBean dongTaiFormBean : cgformFieldList) {

                        if (dongTaiFormBean.getJavaField().equals("location")) {

                            propertyLabel = dongTaiFormBean.getPropertyLabel();

                        }
                    }

                } else {

                    if (roadbean.getFormData() != null) {

                        JSONObject jsonObject = null;
                        try {
                            jsonObject = new JSONObject(roadbean.getFormData());
                            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                            List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                    new TypeToken<List<DongTaiFormBean>>() {
                                    }.getType());
                            for (DongTaiFormBean dongTaiFormBean : dictList) {

                                if (dongTaiFormBean.getJavaField().equals("location")) {

                                    propertyLabel = dongTaiFormBean.getPropertyLabel();

                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }


            } catch (JsonIOException e) {
                e.printStackTrace();
            }


            if (!Utils.isEmpty(propertyLabel)) {

                String[] gpsGroups = propertyLabel.split("\\|");

                List<LatLng> points = new ArrayList<LatLng>();

                for (int j = 0; j < gpsGroups.length; j++) {

                    String firstGps = gpsGroups[j];

                    String[] split = firstGps.split(",");

                    GPS gps = GPSConverterUtils.Gps84_To_bd09(new Double(split[0]),
                            new Double(split[1]));


                    LatLng p1 = new LatLng(gps.getLat(), gps.getLon());
                    points.add(p1);
                    builder.include(p1);

                }

                if (points.size() > 1) {

                    Bundle bundle = new Bundle();

                    bundle.putInt("key", 1);

                    OverlayOptions ooPolyline2 = new PolylineOptions().width(10).color(R.color.titleBg).points(points).extraInfo(bundle);
                    mBaiduMap.addOverlay(ooPolyline2);
                }
            }
        }
    }

    /**
     * 保存
     */
    private void save() {

        String strPoints = "";
        switch (flagtype) {

            case MARKER_FLAG:
                if (points != null && points.size() > 0) {

                    LatLng latLng =points.get(0);
                    strPoints = latLng.latitude + "," + latLng.longitude;


                    Intent intent = new Intent();
                    dongTaiFormBean.setPropertyLabel(strPoints);
                    intent.putExtra("data", dongTaiFormBean);

                    intent.putExtra("strPoints", strPoints);
                    setResult(1, intent);

                    finish();

                }

                break;


            case LINE_FLAG:

                if (points.size() > 1) {


                    for (LatLng latLng : points) {

                        if (Utils.isEmpty(strPoints)) {
                            strPoints = latLng.latitude + "," + latLng.longitude;
                        } else {
                            strPoints = strPoints + "|" + latLng.latitude + "," + latLng.longitude;
                        }

                    }

                    Intent intent = new Intent();
                    intent.putExtra("strPoints", strPoints);

                    dongTaiFormBean.setPropertyLabel(strPoints);
                    intent.putExtra("data", dongTaiFormBean);
                    setResult(1, intent);

                    finish();


                } else {

                    Utils.showToast(mContext, "线最少需要2个点");
                    return;

                }

                break;


            case PLANE_FLAG:
                if (points.size() > 2) {


                    for (LatLng latLng : points) {

                        if (Utils.isEmpty(strPoints)) {
                            strPoints = latLng.latitude + "," + latLng.longitude;
                        } else {
                            strPoints = strPoints + "|" + latLng.latitude + "," + latLng.longitude;
                        }

                    }

                    Intent intent = new Intent();
                    intent.putExtra("strPoints", strPoints);

                    dongTaiFormBean.setPropertyLabel(strPoints);
                    intent.putExtra("data", dongTaiFormBean);
                    setResult(1, intent);

                    finish();


                } else {

                    Utils.showToast(mContext, "范围最少需要三个点");

                }
                break;
        }





    }

    /**
     * 撤销
     */
    private void cheXiao() {
        if (points != null && points.size() > 0) {

            points.remove(points.size() - 1);
        } else {

            if (points == null || points.size() == 0) {

                mBaiduMap.clear();
                drawRangeInMap(flagtype);
                drawTreesInMap(treesPoints);
                Utils.showToast(mContext, "暂无坐标点可撤销");
                return;
            }
        }

        drawRangeInMap(flagtype);


    }

    @Override
    public BaiduMap getBaiDuMap() {
        return null;
    }

    @Override
    public MapView getMapView() {
        return mMapView;
    }

    @Override
    public ImageButton getWeiXingView() {
        return null;
    }

    @Override
    public HashMap<Integer, Marker> getMarker() {
        return null;
    }


    @Override
    public HashMap<Integer, Overlay> getOverLays() {
        return null;
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }


    @Override
    public boolean onMapPoiClick(MapPoi mapPoi) {
        return false;
    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus) {

    }

    @Override
    public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

    }

    @Override
    public void onMapStatusChange(MapStatus mapStatus) {

    }

    private LatLng target;

    @Override
    public void onMapStatusChangeFinish(MapStatus mapStatus) {


        // TODO Auto-generated method stub
        target = mBaiduMap.getMapStatus().target;
        System.out.println(target.toString());


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    private String propertyLabel;

    @Override
    public void onMapLoaded() {

        String ranges = dongTaiFormBean.getPropertyLabel();

        if (!Utils.isEmpty(ranges)) {

            drawRange(ranges, flagtype);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            if (roadbean != null) {

                try {
                    FormModelBean formModel = roadbean.getFormModel();
                    if (formModel != null) {
                        List<DongTaiFormBean> cgformFieldList = formModel.getCgformFieldList();

                        for (DongTaiFormBean dongTaiFormBean : cgformFieldList) {

                            if (dongTaiFormBean.getJavaField().equals("location")) {

                                propertyLabel = dongTaiFormBean.getPropertyLabel();

                            }
                        }

                    } else {

                        if (roadbean.getFormData() != null) {

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(roadbean.getFormData());
                                Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                                List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                        new TypeToken<List<DongTaiFormBean>>() {
                                        }.getType());
                                for (DongTaiFormBean dongTaiFormBean : dictList) {

                                    if (dongTaiFormBean.getJavaField().equals("location")) {

                                        propertyLabel = dongTaiFormBean.getPropertyLabel();

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }


                } catch (JsonIOException e) {
                    e.printStackTrace();
                }


                if (!Utils.isEmpty(propertyLabel)) {

                    String[] gpsGroups = propertyLabel.split("\\|");

                    List<LatLng> points = new ArrayList<LatLng>();

                    for (int j = 0; j < gpsGroups.length; j++) {

                        String firstGps = gpsGroups[j];

                        String[] split = firstGps.split(",");

                        GPS gps = GPSConverterUtils.Gps84_To_bd09(new Double(split[0]),
                                new Double(split[1]));


                        LatLng p1 = new LatLng(gps.getLat(), gps.getLon());
                        points.add(p1);
                        builder.include(p1);

                    }

                    if (points.size() > 1) {

                        Bundle bundle = new Bundle();

                        bundle.putInt("key", 1);

                        OverlayOptions ooPolyline2 = new PolylineOptions().width(10).color(R.color.titleBg).points(points).extraInfo(bundle);
                        mBaiduMap.addOverlay(ooPolyline2);
                    }
                }
            }


            int windowWidth = mMapView.getWidth();
            int windowheight = mMapView.getHeight();

            for (LatLng p1 : points) {

                builder.include(p1);
            }

            LatLngBounds plane = builder.build();
            // 设置显示在屏幕中的地图地理范围

            MapStatusUpdate planstate = MapStatusUpdateFactory.newLatLngBounds(plane, windowWidth, windowheight);
            mBaiduMap.setMapStatus(planstate);

        } else {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            if (roadbean != null) {

                try {
                    FormModelBean formModel = roadbean.getFormModel();
                    if (formModel != null) {
                        List<DongTaiFormBean> cgformFieldList = formModel.getCgformFieldList();

                        for (DongTaiFormBean dongTaiFormBean : cgformFieldList) {

                            if (dongTaiFormBean.getJavaField().equals("location")) {

                                propertyLabel = dongTaiFormBean.getPropertyLabel();

                            }
                        }

                    } else {

                        if (roadbean.getFormData() != null) {

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(roadbean.getFormData());
                                Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                                List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                        new TypeToken<List<DongTaiFormBean>>() {
                                        }.getType());
                                for (DongTaiFormBean dongTaiFormBean : dictList) {

                                    if (dongTaiFormBean.getJavaField().equals("location")) {

                                        propertyLabel = dongTaiFormBean.getPropertyLabel();

                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    }


                } catch (JsonIOException e) {
                    e.printStackTrace();
                }


                if (!Utils.isEmpty(propertyLabel)) {

                    String[] gpsGroups = propertyLabel.split("\\|");

                    List<LatLng> points = new ArrayList<LatLng>();

                    for (int j = 0; j < gpsGroups.length; j++) {

                        String firstGps = gpsGroups[j];

                        String[] split = firstGps.split(",");

                        GPS gps = GPSConverterUtils.Gps84_To_bd09(new Double(split[0]),
                                new Double(split[1]));


                        LatLng p1 = new LatLng(gps.getLat(), gps.getLon());
                        points.add(p1);
                        builder.include(p1);

                    }

                    if (points.size() > 1) {

                        Bundle bundle = new Bundle();

                        bundle.putInt("key", 1);

                        OverlayOptions ooPolyline2 = new PolylineOptions().width(10).color(R.color.titleBg).points(points).extraInfo(bundle);
                        mBaiduMap.addOverlay(ooPolyline2);
                    }
                }
            }else {

                return;
            }



            int windowWidth = mMapView.getWidth();
            int windowheight = mMapView.getHeight();


            LatLngBounds plane = builder.build();
            // 设置显示在屏幕中的地图地理范围

            MapStatusUpdate planstate = MapStatusUpdateFactory.newLatLngBounds(plane, windowWidth, windowheight);
            mBaiduMap.setMapStatus(planstate);

            MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLatLng);
            mBaiduMap.animateMapStatus(u);

            if (!isCheck) {

                if (!Utils.isEmpty(isEdit) && isEdit.equals("0")) {
                    if (mLatLng != null) {
                        points.add(mLatLng);

                    }
                }

            } else {

                if(!Utils.isEmpty(isEdit) && isEdit.equals("0")){
                        mLatLng = points.get(0);
                  }

            }

            if (mLatLng != null) {
                BitmapDescriptor bitmap = BitmapDescriptorFactory
                        .fromResource(R.mipmap.resflag_add);
                OverlayOptions markeroption = new MarkerOptions()
                        .position(mLatLng)
                        .icon(bitmap);

                //在地图上添加Marker，并显示
                mBaiduMap.addOverlay(markeroption);
            }



        }
    }


    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            int locType = location.getLocType();
            // map view 销毁后不在处理新接收的位置
            if (location == null || mMapView == null || locType == BDLocation.TypeServerError || locType == BDLocation.TypeCriteriaException)
                return;
            if (location.getSatelliteNumber() != -1) {
            }

            if (!Utils.isEmpty(isEdit) && isEdit.equals("0")) {

            } else {

                mLatLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
            }


            if (isFirstLoc&&(points==null||points.size()==0)&&Utils.isEmpty(propertyLabel)) {
                mLatLng = new LatLng(location.getLatitude(),
                        location.getLongitude());
                isFirstLoc = false;
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLatLng);
                mBaiduMap.animateMapStatus(u);

                target = mBaiduMap.getMapStatus().target;
                System.out.println(target.toString());


            }


        }


    }


    /**
     * 判断用户登录状态，登录获取用户信息
     */
    private UserInfo userInfo;

    public boolean isLogin() {
        if (userInfo != null)
            return true;
        try {
            String userPath = Values.SDCARD_FILE(Values.SDCARD_CACHE) + Values.CACHE_USER;
            Object obj = ACache.get(new File(userPath)).getAsObject(Values.CACHE_USERINFO);
            if (obj != null) {
                userInfo = (UserInfo) obj;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


    }
}

