package com.hollysmart.gridslib;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.PolygonOptions;
import com.d.lib.xrv.LRecyclerView;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.activitys.SearchActivity;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.adapters.GridsListAdapter;
import com.hollysmart.gridslib.apis.FindListPageAPI;
import com.hollysmart.gridslib.apis.SearchGridsListPageAPI;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchGridsActivity extends StyleAnimActivity implements
        SearchGridsListPageAPI.SearchDataIF, GridsListAdapter.setMapBtnClickListener, AMapLocationListener,
        LocationSource, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener,
        AMap.InfoWindowAdapter, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener,
        Animation.AnimationListener, View.OnClickListener, AMap.OnMapClickListener{


    @Override
    public int layoutResID() {
        return R.layout.activity_search_road;
    }

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.lv_roadList)
    LRecyclerView lv_roadList;


    @BindView(R.id.lay_fragment_ProdutEmpty)
    LinearLayout lay_fragment_ProdutEmpty;

    @BindView(R.id.ed_search)
    EditText ed_search;

    @BindView(R.id.bmapView)
    MapView mMapView;

    @BindView(R.id.bn_closeMap)
    ImageButton bn_closeMap;

    @BindView(R.id.tv_griNumber)
    TextView tv_griNumber;

    @BindView(R.id.rl_mapContent)
    RelativeLayout rl_mapContent;


    private List<BlockAndStatusBean> roadBeanList;
    private GridsListAdapter resDataManageAdapter;

    private LoadingProgressDialog lpd;

    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集

    Map<String, String> map = new HashMap<String , String>();

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    private String TreeFormModelId;
    private String PcToken;

    private ProjectBean projectBean;

    private List<DongTaiFormBean> DongTainewFormList;

    AMap mGaoDeMap;

    //声明定位回调监听器
    public AMapLocationClientOption mLocationOption = null;

    public AMapLocationClient mLocationClient = null;

    private UiSettings uiSettings;

    private BlockBean currrentBlockBeanshownOnMap;//当前显示在地图的网格
    private int  currrentPositionGridshownOnMap ;//当前显示在地图的网格


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initMap(savedInstanceState);

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
    public void findView() {

        ButterKnife.bind(this);
        iv_back.setOnClickListener(this);

        bn_closeMap.setOnClickListener(this);

        findViewById(R.id.imagbtn_enlarge).setOnClickListener(this);
        findViewById(R.id.imagbtn_zoomOut).setOnClickListener(this);
        findViewById(R.id.bn_weixing).setOnClickListener(this);
        findViewById(R.id.bn_dingwei).setOnClickListener(this);

        findViewById(R.id.ll_search).setOnClickListener(this);
        isLogin();

        ed_search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });


    }

    private ResDataBean search_resDataBean;

    @Override
    public void init() {

        setLpd();
        roadBeanList = new ArrayList<>();
        lay_fragment_ProdutEmpty.setVisibility(View.GONE);

        search_resDataBean = (ResDataBean) getIntent().getSerializableExtra("search_resDataBean");
        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");
        DongTainewFormList = (List<DongTaiFormBean>) getIntent().getSerializableExtra("DongTainewFormList");

        ischeck = getIntent().getBooleanExtra("ischeck", false);
        TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");
        map = (Map<String, String>) getIntent().getSerializableExtra("exter");
        PcToken = getIntent().getStringExtra("PcToken");




        resDataManageAdapter = new GridsListAdapter(PcToken,mContext, TreeFormModelId, roadBeanList, projectBean, ischeck);
        resDataManageAdapter.setMap(map);
        resDataManageAdapter.setMapBtnClickListener(SearchGridsActivity.this);
        lv_roadList.setAdapter(resDataManageAdapter);


    }

    private void setLpd() {
        lpd = new LoadingProgressDialog();
        lpd.setMessage("正在获取道路列表，请稍等...");
        lpd.create(this, lpd.STYLE_SPINNER);
        lpd.setCancelable(false);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.bn_closeMap:
                rl_mapContent.setVisibility(View.GONE);

                break;

            case R.id.bn_weixing:
                mapChaged();
                break;
            case R.id.bn_dingwei:
//                MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(mLatLng);
//                mGaoDeMap.animateMapStatus(u);

                dingwei();
                break;
            case R.id.imagbtn_enlarge:
                ZoomChange(true);
                break;
            case R.id.imagbtn_zoomOut:
                ZoomChange(false);
                break;

        }


    }


    private void dingwei() {


        MyLocationStyle myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
        myLocationStyle.interval(2000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）默认执行此种模式。
        mGaoDeMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
        mGaoDeMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        mGaoDeMap.moveCamera(CameraUpdateFactory.zoomTo(17));


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
     *
     * @param editSearch
     */

    private void search(String editSearch){

        if (Utils.isEmpty(editSearch)) {
            roadBeanList.clear();
            resDataManageAdapter.notifyDataSetChanged();
            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);

            return;
        }


        lpd.show();
        lv_roadList.setAdapter(resDataManageAdapter);
        new SearchGridsListPageAPI(1,editSearch,userInfo,map.get("id"), this).request();





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
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


    @Override
    public void searchDatadicListResult(boolean isOk, List<BlockAndStatusBean> menuBeanList, int count) {
        lpd.cancel();

        if (isOk) {
            roadBeanList.clear();
            roadBeanList.addAll(menuBeanList);
            lv_roadList.setVisibility(View.VISIBLE);
            lay_fragment_ProdutEmpty.setVisibility(View.GONE);
            resDataManageAdapter.notifyDataSetChanged();


        }else {
            roadBeanList.clear();
            resDataManageAdapter.notifyDataSetChanged();
            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
        }

    }




    @Override
    public void onAnimationStart(Animation animation) {

    }

    @Override
    public void onAnimationEnd(Animation animation) {

    }

    @Override
    public void onAnimationRepeat(Animation animation) {

    }

    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {

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

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {

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
        return false;
    }

    @Override
    public void activate(LocationSource.OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void MapBtnClick(BlockBean blockBean, int curPosition) {
        currrentBlockBeanshownOnMap = blockBean;
        currrentPositionGridshownOnMap = curPosition;
//        mGaoDeMap.setMyLocationEnabled(false);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        if (!rl_mapContent.isShown()) {

            rl_mapContent.setVisibility(View.VISIBLE);
        }

        new FindListPageAPI(10000,userInfo,TreeFormModelId, projectBean, blockBean.getId(), new FindListPageAPI.DatadicListIF() {
            @Override
            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {
                List<LatLng> points = new ArrayList<>();

                if (isOk) {
                    if (netDataList != null && netDataList.size() > 0) {

                        for (int i = 0; i < netDataList.size(); i++) {

                            ResDataBean resDataBean = netDataList.get(i);

                            Double lat = Double.parseDouble(resDataBean.getLatitude());
                            Double lng = Double.parseDouble(resDataBean.getLongitude());
                            LatLng latLng = new LatLng(lat, lng);
                            points.add(latLng);

                        }
                    }


                    drawGrid(blockBean,points);

                    tv_griNumber.setText(blockBean.getFdBlockCode());


                }


            }
        }).request();

    }




    /***
     * 绘制表格
     */

    private void drawGrid(BlockBean blockBean, List<LatLng> netDataList) {
        mGaoDeMap.clear();

        if (blockBean == null) {
            return;
        }

        List<LatLng> rectangles = createRectangle(blockBean);

        if (rectangles != null) {
            mGaoDeMap.addPolygon(new PolygonOptions()
                    .addAll(rectangles)
                    .fillColor(Color.argb(130, 158, 230,252))
                    .strokeColor(Color.argb(130, 177, 152, 198))
                    .strokeWidth(5)
            );
        }
        drawMarkerTrees(netDataList);
        setMapBounds(rectangles,netDataList);
    }


    private void drawMarkerTrees(List<LatLng> points) {
        if (points != null && points.size() > 0) {

            for (int i=0;i<points.size();i++){
                LatLng latLng = points.get(i);

                BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromBitmap(BitmapFactory.decodeResource(getResources(), R.mipmap.resflag_add));
                mGaoDeMap.addMarker(new MarkerOptions().position(latLng).period(i+1).icon(bitmapDescriptor));
            }

        }
    }


    private void setMapBounds(List<LatLng> latLngs,List<LatLng> points) {
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



    public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
        private int space;

        public SpacesItemDecoration(int space) {
            this.space = space;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view,
                                   RecyclerView parent, RecyclerView.State state) {
            outRect.left = space;
            outRect.right = space;
            outRect.bottom = space;

            // Add top margin only for the first item to avoid double space between items
            if (parent.getChildPosition(view) == 0)
                outRect.top = space;
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
}
