package com.hollysmart.formlib.activitys;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolygonOptions;
import com.amap.api.maps.model.animation.Animation;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.Overlay;
import com.baidu.mapapi.map.OverlayOptions;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.main.MainPresenter;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.ButterKnife;

public class GaoDeMapRangeActivity extends StyleAnimActivity implements AMapLocationListener,
        LocationSource, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener,
        AMap.InfoWindowAdapter, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener,
        Animation.AnimationListener, View.OnClickListener, AMap.OnMapClickListener {


    AMap mGaoDeMap;
    com.amap.api.maps.MapView mMapView = null;
    boolean isFirstLoc = true;// 是否首次定位

    //声明定位回调监听器
    public AMapLocationClientOption mLocationOption = null;

    public AMapLocationClient mLocationClient = null;


    private ImageButton bn_weixing;
    private ImageButton bn_dingwei;
    private ImageButton imagbtn_enlarge;
    private ImageButton imagbtn_zoomOut;
    private ImageView iv_center;

    private List<ResDataBean> treeList = new ArrayList<>();


    private boolean isCheck;
    private GridBean roadbean;
    private ResDataBean tree_resDataBean;
    private ProjectBean projectBean;
    private DongTaiFormBean dongTaiFormBean;

    private static final int MARKER_FLAG = 1;
    private static final int LINE_FLAG = 2;
    private static final int PLANE_FLAG = 3;

    private int flagtype = 0;
    private LatLng centerlatlng = null;

    //多边形顶点位置
    private List<LatLng> points = new ArrayList<>();

    private List<LatLng> treesPoints = new ArrayList<>();


    @Override
    public int layoutResID() {
        return R.layout.activity_gaode_map_range;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMap(savedInstanceState);

        requestPermisson();
        isLogin();


        String falg = getIntent().getStringExtra("falg");
        isCheck = getIntent().getBooleanExtra("isCheck", false);
        dongTaiFormBean = (DongTaiFormBean) getIntent().getSerializableExtra("bean");
        roadbean = (GridBean) getIntent().getSerializableExtra("roadbean");

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

        }else {

            treeList = (List<ResDataBean>) getIntent().getSerializableExtra("treeList");
        }

        if (!Utils.isEmpty(isEdit) && isEdit.equals("0")) {

            findViewById(R.id.btn_add).setVisibility(View.GONE);
            findViewById(R.id.btn_chexiao).setVisibility(View.GONE);
            findViewById(R.id.bn_dingwei).setVisibility(View.GONE);
            iv_center.setVisibility(View.GONE);
        }else {
            treeList = (List<ResDataBean>) getIntent().getSerializableExtra("treeList");
        }

        if (dongTaiFormBean != null && (!Utils.isEmpty(dongTaiFormBean.getPropertyLabel()))) {

            String propertyLabel = dongTaiFormBean.getPropertyLabel();

            String[] localpoints = propertyLabel.split("\\|");
            if (localpoints.length > 0) {

                for (int i = 0; i < localpoints.length; i++) {

                    String localpoint = localpoints[i];

                    String[] str_latlng = localpoint.split(",");

                    if (str_latlng.length > 1) {
                        Double lat = Double.parseDouble(str_latlng[0]);
                        Double lng = Double.parseDouble(str_latlng[1]);
                        LatLng latLng = new LatLng(lat, lng);
                        points.add(latLng);
                    }
                }
            }
        }
        if (treeList != null && (treeList.size() > 0)) {

            for (int i = 0; i < treeList.size(); i++) {
                ResDataBean resDataBean = treeList.get(i);
                Double lat = Double.parseDouble(resDataBean.getLatitude());
                Double lng = Double.parseDouble(resDataBean.getLongitude());
                LatLng latLng = new LatLng(lat, lng);
                treesPoints.add(latLng);
            }

        }

        drawRangeInMap(flagtype);
    }

    @Override
    public void findView() {
        ButterKnife.bind(this);
        try {

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
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    /**
     * 使用地图sdk前需先初始化BMapManager. BMapManager是全局的，可为多个MapView共用，它需要地图模块创建前创建，
     * 并在地图地图模块销毁后销毁，只要还有地图模块在使用，BMapManager就不应该销毁
     */
    private List<ResDataBean> resDatalist;
    private int mIndex = -1;
    private HashMap<Integer, Marker> mMarkers;
    private HashMap<Integer, Overlay> mOverlays;
    private LatLng dingWeiDian;
    private String TreeFormModelId;

    private ResDataBean roadBean;

    private String propertyLabel;

    private UiSettings uiSettings;

    private String isEdit = "";  // 是否可修改  0：不能修改 1：可以修改 @property (nonatomic, copy) NSMutableString


    @Override
    public void init() {


    }

    /***
     * 绘制表格
     */

    private void drawGrid(GridBean gridBean) {

        if (gridBean == null) {
            return;
        }

        List<LatLng> rectangles = createRectangle(gridBean);

        if (rectangles != null) {

//            PolygonOptions polygonOptions = new PolygonOptions()
//                    .addAll(rectangles)
//                    .fillColor(Color.argb(100, 158, 230, 252))
//                    .strokeColor(Color.argb(255, 177, 152, 198))
//                    .strokeWidth(4);

            mGaoDeMap.addPolygon(new PolygonOptions()
                    .addAll(rectangles)
                    .fillColor(Color.argb(130, 158, 230, 252))
                    .strokeColor(Color.argb(130, 177, 152, 198))
                    .strokeWidth(5)
            );
        }
        drawTreesInMap(treesPoints);

        setMapBounds(rectangles);
    }

    private void setMapBounds(List<LatLng> latLngs) {
        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (LatLng latlng : latLngs) {

            builder.include(latlng);

        }

        if (points != null && points.size() > 0) {
            for (int i = 0; i < points.size(); i++) {

                builder.include(points.get(i));
            }

        }
        if (treesPoints != null && treesPoints.size() > 0) {
            for (int i = 0; i < treesPoints.size(); i++) {

                builder.include(treesPoints.get(i));
            }

        }


        LatLngBounds bounds = builder.build();

        mGaoDeMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds, 50, 50, 50, 50));


    }

    private List<LatLng> createRectangle(GridBean gridBean) {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(gridBean.getFdLbLat(), gridBean.getFdRtLng()));
        latLngs.add(new LatLng(gridBean.getFdRtLat(), gridBean.getFdRtLng()));
        latLngs.add(new LatLng(gridBean.getFdRtLat(), gridBean.getFdLbLng()));
        latLngs.add(new LatLng(gridBean.getFdLbLat(), gridBean.getFdLbLng()));
        return latLngs;
    }

    /***
     * 初始化地图
     */

    private void initMap(Bundle savedInstanceState) {
        // 地图初始化
        mMapView = findViewById(R.id.bmapView);


        mMapView.onCreate(savedInstanceState);// 此方法必须重写
        mGaoDeMap = mMapView.getMap();
        mGaoDeMap.setMapType(AMap.MAP_TYPE_NORMAL);

        //初始化定位
        mLocationClient = new AMapLocationClient(this);
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);
        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，AMapLocationMode.Battery_Saving为低功耗模式，AMapLocationMode.Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置是否返回地址信息（默认返回地址信息）
        mLocationOption.setNeedAddress(true);
        //设置是否只定位一次,默认为false
        mLocationOption.setOnceLocation(false);
        //设置是否强制刷新WIFI，默认为强制刷新
        mLocationOption.setWifiActiveScan(true);
        //设置是否允许模拟位置,默认为false，不允许模拟位置
        mLocationOption.setMockEnable(false);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(2000);
        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

        uiSettings = mGaoDeMap.getUiSettings();

        //设置地图缩放
        settingZoom();

        initMapListener();


    }


    private void initMapListener() {
        mGaoDeMap.setOnMapLoadedListener(this);
        mGaoDeMap.setOnCameraChangeListener(this);
        mGaoDeMap.setOnMarkerClickListener(this);
        mGaoDeMap.setOnInfoWindowClickListener(this);
        mGaoDeMap.setInfoWindowAdapter(this);// 设置自定义InfoWindow样式
        mGaoDeMap.setOnMapClickListener(this);
//        centerMarker.setAnimationListener(this);
    }


    private void settingZoom() {

        //是否允许显示地图缩放按钮
        uiSettings.setZoomControlsEnabled(false);
        //是否允许收拾手势缩放地图
        uiSettings.setZoomGesturesEnabled(true);
        //设置双击地图放大在地图中心位置放大，false则是在点击位置放大
        uiSettings.setZoomInByScreenCenter(true);
        //地图缩放按钮的位置
        uiSettings.setZoomPosition(AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM);
//        AMapOptions.ZOOM_POSITION_RIGHT_CENTER
//        AMapOptions.ZOOM_POSITION_RIGHT_BUTTOM
        //获取地图缩放按钮位置
        Mlog.d("settingZoom: " + uiSettings.getZoomPosition());
    }


    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override

        public void onLocationChanged(AMapLocation aMapLocation) {

            if (aMapLocation != null) {

                if (aMapLocation.getErrorCode() == 0) {

                    //定位成功回调信息，设置相关消息

                    aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见定位类型表

                    aMapLocation.getLatitude();//获取纬度

                    aMapLocation.getLongitude();//获取经度

                    aMapLocation.getAccuracy();//获取精度信息

                    java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


                    aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。

                    aMapLocation.getCountry();//国家信息

                    aMapLocation.getProvince();//省信息

                    aMapLocation.getCity();//城市信息

                    aMapLocation.getDistrict();//城区信息

                    aMapLocation.getStreet();//街道信息

                    aMapLocation.getStreetNum();//街道门牌号信息

                    aMapLocation.getCityCode();//城市编码

                    aMapLocation.getAdCode();//地区编码

                    Log.i("zjc", aMapLocation.getCity());

                    mLocationClient.stopLocation();//停止定位

                } else {

                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。

                    Log.e("info", "location Error, ErrCode:" + aMapLocation.getErrorCode() + ", errInfo:" + aMapLocation.getErrorInfo());

                }

            }

        }
    };


    private final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_CODE_PERMISSION_CAMERA = 0x09;
    private final int MY_PERMISSIONS_REQUEST_CALL = 2;
    private final int MY_PERMISSIONS_REQUEST_LOCATION = 0x10;

    private void requestPermisson() {

        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // 权限请求成功的操作
                } else {
                    // 权限请求失败的操作
                    Utils.showToast(mContext, "请在权限管理中设置存储权限,不然会影响正常使用");
                }
                break;
            case MY_PERMISSIONS_REQUEST_CALL:
                // 权限请求成功的操作
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    // 权限请求失败的操作
                    Utils.showToast(mContext, "请在权限管理中设置打电话权限,不然会影响正常使用");
                }
                break;
            case MY_PERMISSIONS_REQUEST_LOCATION:
                // 权限请求成功的操作
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    return;
                } else {
                    // 权限请求失败的操作
                    Utils.showToast(mContext, "请在权限管理中设置打电话权限,不然会影响正常使用");
                }
                break;
            case REQUEST_CODE_PERMISSION_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "请授权使用camera权限", Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {

        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
    }

    private boolean isRefresh = false;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 4) {

            if (resultCode == 1) {
                mGaoDeMap.clear();

//                initResDataList(projectBean.getId());

                isRefresh = true;

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
                add(centerlatlng);
                break;
            case R.id.bn_weixing:
                mapChaged();
                break;
            case R.id.bn_dingwei:
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLatLng);
//                mGaoDeMap.animateMapStatus(u);
                break;
            case R.id.imagbtn_enlarge:
                ZoomChange(true);
                break;
            case R.id.imagbtn_zoomOut:
                ZoomChange(false);
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

                mGaoDeMap.clear();
                drawRangeInMap(flagtype);
                drawTreesInMap(treesPoints);
                Utils.showToast(mContext, "暂无坐标点可撤销");
                return;
            }
        }

        drawRangeInMap(flagtype);


    }

    private void drawTreesInMap(List<LatLng> treesPoints) {

        if (treesPoints != null && treesPoints.size() > 0) {

            BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.resflag_add));
            for (int i = 0; i < treesPoints.size(); i++) {

                mGaoDeMap.addMarker(new MarkerOptions().position(treesPoints.get(i)).icon(bitmapDescriptor));

            }

        }
    }


    public void mapChaged() {

        int mapType = mGaoDeMap.getMapType();
        if (mapType == 1) {
            mGaoDeMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        } else {
            mGaoDeMap.setMapType(AMap.MAP_TYPE_NORMAL);
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

                    LatLng latLng = points.get(0);
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
        mGaoDeMap.clear();
        drawGrid(roadbean);

        if (points == null || points.size() == 0) {
            return;
        }

        switch (type) {
            case MARKER_FLAG:
                if (points != null && points.size() > 0) {

                    BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.resflag_add));
                    for (int i = 0; i < points.size(); i++) {

                        mGaoDeMap.addMarker(new MarkerOptions().position(points.get(i)).icon(bitmapDescriptor));

                    }

                }

                break;
            case LINE_FLAG:

                if (points.size() < 2) {


                } else {
                    //在地图上显示多边形
                }

                break;
            case PLANE_FLAG:
                break;
        }


    }


    /**
     * 地图进行缩放
     *
     * @param b
     */
    public void ZoomChange(boolean b) {

        gaoDeMapZoomChange(mGaoDeMap, b);
    }


    /**
     * 对地图进行缩放
     *
     * @param mGaoDeMap
     * @param b
     */
    public void gaoDeMapZoomChange(AMap mGaoDeMap, boolean b) {

        float zoomLevel = mGaoDeMap.getCameraPosition().zoom;
        Mlog.d("zoom:" + zoomLevel);
        if (b) {
            if (zoomLevel < mGaoDeMap.getMaxZoomLevel()) {
                zoomLevel = zoomLevel + 1;
            }
        } else {
            if (zoomLevel > mGaoDeMap.getMinZoomLevel()) {
                zoomLevel = zoomLevel - 1;
            }
        }
//        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(zoomLevel);
//        mGaoDeMap.animateMapStatus(u);

        mGaoDeMap.moveCamera(CameraUpdateFactory.zoomTo(zoomLevel));


    }


    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

        Mlog.d("onLocationChanged........>" + aMapLocation.getLatitude() + "......." + aMapLocation.getLongitude());

    }

    @Override
    public View getInfoWindow(com.amap.api.maps.model.Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(com.amap.api.maps.model.Marker marker) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        com.amap.api.maps.model.LatLng target = cameraPosition.target;

        Mlog.d("onCameraChange........>" + target.latitude + "......." + target.longitude);

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        LatLng target = cameraPosition.target;

        Mlog.d("onCameraChangeFinish........>" + target.latitude + "......." + target.longitude);
        centerlatlng = target;

    }

    @Override
    public void onInfoWindowClick(com.amap.api.maps.model.Marker marker) {

    }

    @Override
    public void onMapClick(com.amap.api.maps.model.LatLng latLng) {

    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(com.amap.api.maps.model.Marker marker) {
        return false;
    }

    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

//        mListener = null;
//        if (aMapManager != null) {
//            aMapManager.removeUpdates(this);
//            aMapManager.destory();
//        }
//        aMapManager = null;


    }

    @Override
    public void onAnimationStart() {

    }

    @Override
    public void onAnimationEnd() {

    }
}
