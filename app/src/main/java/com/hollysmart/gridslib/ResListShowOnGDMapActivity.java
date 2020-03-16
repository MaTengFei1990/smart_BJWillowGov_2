package com.hollysmart.gridslib;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
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
import com.baidu.mapapi.map.Overlay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.apis.ResDataGetAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.main.MainPresenter;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ResListShowOnGDMapActivity extends StyleAnimActivity implements AMapLocationListener,
        LocationSource, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener,
        AMap.InfoWindowAdapter, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener,
        Animation.AnimationListener, View.OnClickListener, AMap.OnMapClickListener {


    AMap mGaoDeMap;
    MapView mMapView = null;
    boolean isFirstLoc = true;// 是否首次定位

    //声明定位回调监听器
    public AMapLocationClientOption mLocationOption = null;

    public AMapLocationClient mLocationClient = null;

    private boolean isCheck;
    private BlockBean roadbean;
    private ResDataBean tree_resDataBean;
    private ProjectBean projectBean;
    private DongTaiFormBean dongTaiFormBean;

    private int flagtype = 0;
    private LatLng centerlatlng = null;

    //多边形顶点位置
    private List<LatLng> points = new ArrayList<>();

    private List<ResDataBean> treeslist;


    @BindView(R.id.tv_projectName)
    TextView tv_projectName;
    @BindView(R.id.iv_back)
    ImageView iv_back;
    @BindView(R.id.bmapView)
    MapView bmapView;
    @BindView(R.id.shizi)
    ImageView shizi;
    @BindView(R.id.imagbtn_startOrContinue)
    ImageButton imagbtn_startOrContinue;
    @BindView(R.id.imagbtn_end)
    ImageButton bn_jieshu;
    @BindView(R.id.linearLayout1)
    LinearLayout linearLayout1;
    @BindView(R.id.bn_weixing)
    ImageButton bn_weixing;
    @BindView(R.id.bn_dingwei)
    ImageButton bn_dingwei;
    @BindView(R.id.imagbtn_enlarge)
    ImageButton bn_fangda;
    @BindView(R.id.imagbtn_zoomOut)
    ImageButton bn_suoxiao;
    @BindView(R.id.imagbtn_route)
    ImageButton bn_xialu;
    @BindView(R.id.bn_all)
    LinearLayout bn_all;
    @Nullable
    ImageView image_luyin;
    @Nullable
    @BindView(R.id.imageButton_luyin)
    ImageButton imageButton_luyin;
    @Nullable
    @BindView(R.id.button_luyin)
    Button button_luyin;
    @Nullable
    @BindView(R.id.layout_bt_luyin)
    LinearLayout layout_bt_luyin;
    @BindView(R.id.layout_luyin)
    View luyin;


    @Override
    public int layoutResID() {
        return R.layout.activity_res_list_show_on_gd_map;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMap(savedInstanceState);

        requestPermisson();
        isLogin();

        ButterKnife.bind(this);
        try {

            findViewById(R.id.iv_back).setOnClickListener(this);
            findViewById(R.id.imagbtn_enlarge).setOnClickListener(this);
            findViewById(R.id.imagbtn_zoomOut).setOnClickListener(this);

            bn_weixing.setOnClickListener(this);

        } catch (Exception e) {
            e.printStackTrace();
        }


        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");
        treeslist = (ArrayList) getIntent().getSerializableExtra("treeList");

        if (projectBean != null) {
            isCheck = getIntent().getBooleanExtra("ischeck", false);
            TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");
            tv_projectName.setText("树木地图");
            roadbean = (BlockBean) getIntent().getSerializableExtra("blockBean");
        }


        if (treeslist != null && (treeslist.size() > 0)) {

            for (ResDataBean resDataBean : treeslist) {
                Double lat = Double.parseDouble(resDataBean.getLatitude());
                Double lng = Double.parseDouble(resDataBean.getLongitude());
                LatLng latLng = new LatLng(lat, lng);
                points.add(latLng);

            }
        }

        drawRangeInMap(flagtype);
    }

    @Override
    public void findView() {



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
    private MainPresenter mainPresenter;
    private String TreeFormModelId;

    private String propertyLabel;

    private UiSettings uiSettings;

    private String isEdit = "";  // 是否可修改  0：不能修改 1：可以修改 @property (nonatomic, copy) NSMutableString


    @Override
    public void init() {


    }

    /***
     * 绘制表格
     */

    private void drawGrid(BlockBean blockBean) {

        if (blockBean == null) {
            return;
        }

        List<LatLng> rectangles = createRectangle(blockBean);

        if (rectangles != null) {
//            mGaoDeMap.addPolygon(new PolygonOptions()
//                    .addAll(rectangles)
//                    .fillColor(getResources().getColor(R.color.touming))
//                    .strokeColor(R.color.bg_lan)
//                    .strokeWidth(2)
//            );

            mGaoDeMap.addPolygon(new PolygonOptions()
                    .addAll(rectangles)
                    .fillColor(Color.argb(130, 158, 230,252))
                    .strokeColor(Color.argb(130, 177, 152, 198))
                    .strokeWidth(5)
            );
        }

        setMapBounds(rectangles);
        drawMarkerTrees();
    }

    private void drawMarkerTrees() {
        if (points != null && points.size() > 0) {

            for (int i=0;i<points.size();i++){
                LatLng latLng = points.get(i);

                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.resflag_add));
                mGaoDeMap.addMarker(new MarkerOptions().position(latLng).period(i+1).icon(bitmapDescriptor));
            }

        }
    }

    private void setMapBounds(List<LatLng> latLngs) {
        LatLngBounds.Builder builder = LatLngBounds.builder();

        for (LatLng latlng : latLngs) {

            builder.include(latlng);

        }

        if (points != null && points.size() > 0) {

            for (int i=0;i<points.size();i++){
                LatLng latLng = points.get(i);
                builder.include(latLng);
            }

        }


        LatLngBounds bounds = builder.build();

        mGaoDeMap.animateCamera(CameraUpdateFactory.newLatLngBoundsRect(bounds, 50, 50, 50, 50));


    }

    private List<LatLng> createRectangle(BlockBean blockBean) {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(blockBean.getFdLbLat(), blockBean.getFdRtLng()));
        latLngs.add(new LatLng(blockBean.getFdRtLat(), blockBean.getFdRtLng()));
        latLngs.add(new LatLng(blockBean.getFdRtLat(), blockBean.getFdLbLng()));
        latLngs.add(new LatLng(blockBean.getFdLbLat(), blockBean.getFdLbLng()));
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
//                cheXiao();
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

    public void mapChaged() {

        int mapType = mGaoDeMap.getMapType();
        if (mapType == 1) {
            mGaoDeMap.setMapType(AMap.MAP_TYPE_SATELLITE);
        } else {
            mGaoDeMap.setMapType(AMap.MAP_TYPE_NORMAL);
        }
    }


    private void drawRangeInMap(int type) {
        mGaoDeMap.clear();

        if (points == null || points.size() == 0) {
            return;
        }


        drawGrid(roadbean);


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
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        return null;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        LatLng target = cameraPosition.target;

        Mlog.d("onCameraChange........>" + target.latitude + "......." + target.longitude);

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

        LatLng target = cameraPosition.target;

        Mlog.d("onCameraChangeFinish........>" + target.latitude + "......." + target.longitude);
        centerlatlng = target;

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {


    }

    @Override
    public void onMapLoaded() {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        marker.getPeriod();
        int position = marker.getPeriod()-1;
        ResDataBean resDataBean = treeslist.get(position);

        final Intent intent = new Intent(this, TreeDetailsActivity.class);
        final String formData = resDataBean.getFormData();


        if (Utils.isEmpty(formData)) {

            new ResDataGetAPI(userInfo.getAccess_token(), resDataBean, new ResDataGetAPI.ResDataDeleteIF() {
                @Override
                public void onResDataDeleteResult(boolean isOk, ResDataBean resDataBen) {

                    if (isOk) {
                        String formData = resDataBen.getFormData();

                        startDetailActivity(intent, formData, position);

                    }

                }
            }).request();

        } else {
            startDetailActivity(intent, formData, position);
        }


        return false;
    }

    private void startDetailActivity(Intent intent, String formData, int position) {

        List<DongTaiFormBean> formBeanList=new ArrayList<>();// 当前资源的动态表单
        HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();
        formBeanList.clear();

        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(formData);
            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            List<DongTaiFormBean> oldFormList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                    new TypeToken<List<DongTaiFormBean>>() {}.getType());
            formBeanList.addAll(oldFormList);
            getFormPicMap(formBeanList,formPicMap);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("formBeanList", (Serializable) formBeanList);
        intent.putExtra("resDataBean", treeslist.get(position));
        intent.putExtra("formPicMap", (Serializable) formPicMap);
        intent.putExtra("roadbean", roadbean);
        intent.putExtra("projectBean", projectBean);
        intent.putExtra("ischeck",  isCheck);
        startActivityForResult(intent, 4);
    }

    private void getFormPicMap(List<DongTaiFormBean> formBeans,HashMap<String, List<JDPicInfo>> formPicMap) {

        for (int i = 0; i < formBeans.size(); i++) {
            DongTaiFormBean formBean = formBeans.get(i);

            if (formBean.getPic() != null && formBean.getPic().size() > 0) {
                formPicMap.put(formBean.getJavaField(), formBean.getPic());

            }else {

                if (formBean.getShowType().equals("image")) {

                    if (!Utils.isEmpty(formBean.getPropertyLabel())) {
                        String[] split = formBean.getPropertyLabel().split(",");
                        List<JDPicInfo> picInfos = new ArrayList<>();

                        for (int k = 0; k < split.length; k++) {

                            JDPicInfo jdPicInfo = new JDPicInfo();

                            jdPicInfo.setImageUrl(split[k]);
                            jdPicInfo.setIsDownLoad("true");
                            jdPicInfo.setIsAddFlag(0);

                            picInfos.add(jdPicInfo);
                        }
                        if (picInfos != null && picInfos.size() > 0) {

                            formPicMap.put(formBean.getJavaField(), picInfos);
                        }


                    }


                }

            }

            if (formBean.getCgformFieldList() != null && formBean.getCgformFieldList().size() > 0) {

                List<DongTaiFormBean> childList = formBean.getCgformFieldList();

                for (int j = 0; j < childList.size(); j++) {

                    DongTaiFormBean childbean = childList.get(j);

                    if (childbean.getPic() != null && childbean.getPic().size() > 0) {
                        formPicMap.put(childbean.getJavaField(), childbean.getPic());

                    }else {

                        if (childbean.getShowType().equals("image")) {

                            if (!Utils.isEmpty(childbean.getPropertyLabel())) {
                                String[] split = childbean.getPropertyLabel().split(",");
                                List<JDPicInfo> picInfos = new ArrayList<>();

                                for (int k = 0; k < split.length; k++) {

                                    JDPicInfo jdPicInfo = new JDPicInfo();

                                    jdPicInfo.setImageUrl(split[k]);
                                    jdPicInfo.setIsDownLoad("true");
                                    jdPicInfo.setIsAddFlag(0);

                                    picInfos.add(jdPicInfo);
                                }
                                if (picInfos != null && picInfos.size() > 0) {

                                    formPicMap.put(childbean.getJavaField(), picInfos);
                                }


                            }


                        }


                    }



                }

            }

        }

    }



    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {

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