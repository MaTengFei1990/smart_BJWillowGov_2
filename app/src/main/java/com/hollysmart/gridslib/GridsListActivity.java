package com.hollysmart.gridslib;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
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
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.d.lib.xrv.LRecyclerView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.apis.GetResModelAPI;
import com.hollysmart.apis.ResModelListAPI;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.beans.ResModelBean;
import com.hollysmart.db.JDPicDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.apis.SaveResTaskAPI;
import com.hollysmart.formlib.apis.getResTaskListAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.gridslib.adapters.GridsListAdapter;
import com.hollysmart.gridslib.adapters.MyClassicsHeader;
import com.hollysmart.gridslib.apis.FindGridsListPageAPI;
import com.hollysmart.gridslib.apis.FindListPageAPI;
import com.hollysmart.gridslib.apis.GetGridTreeCountAPI;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.CCM_DateTime;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.utils.taskpool.OnNetRequestListener;
import com.hollysmart.utils.taskpool.TaskPool;
import com.hollysmart.value.Values;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

/***
 * 网格类表；
 */
public class GridsListActivity extends StyleAnimActivity implements OnRefreshLoadMoreListener, AMapLocationListener,
        LocationSource, AMap.OnMarkerClickListener, AMap.OnInfoWindowClickListener,
        AMap.InfoWindowAdapter, AMap.OnMapLoadedListener, AMap.OnCameraChangeListener,
        Animation.AnimationListener, View.OnClickListener, AMap.OnMapClickListener, GridsListAdapter.setMapBtnClickListener {

    @Override
    public int layoutResID() {
        return R.layout.activity_road_list;
    }

    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.tv_maplsit)
    TextView tv_maplsit;

    @BindView(R.id.lv_roadList)
    LRecyclerView lv_roadList;

    @BindView(R.id.rl_bottom)
    RelativeLayout rl_bottom;

    @BindView(R.id.lay_fragment_ProdutEmpty)
    LinearLayout lay_fragment_ProdutEmpty;

    @BindView(R.id.smart_refresh)
    SmartRefreshLayout refreshLayout;

    @BindView(R.id.bmapView)
    MapView mMapView;

    @BindView(R.id.tv_griNumber)
    TextView tv_griNumber;
    @BindView(R.id.bn_closeMap)
    ImageButton bn_closeMap;
    @BindView(R.id.rl_mapContent)
    RelativeLayout rl_mapContent;



    private GridBean currrentGridBeanshownOnMap ;//当前显示在地图的网格
    private int  currrentPositionGridshownOnMap ;//当前显示在地图的网格

    private ProjectBean projectBean;

    AMap mGaoDeMap;

    //声明定位回调监听器
    public AMapLocationClientOption mLocationOption = null;

    public AMapLocationClient mLocationClient = null;

    private UiSettings uiSettings;



    private String roadFormModelId = "";
    private String TreeFormModelId = "";

    private int page=1;
    private boolean isRefresh=false;
    private boolean loadMore=false;
    private int  pageSize=100;


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

    @Override
    public void findView() {
        ButterKnife.bind(this);
        iv_back.setOnClickListener(this);
        tv_maplsit.setOnClickListener(this);
        rl_bottom.setOnClickListener(this);
        bn_closeMap.setOnClickListener(this);

        findViewById(R.id.imagbtn_enlarge).setOnClickListener(this);
        findViewById(R.id.imagbtn_zoomOut).setOnClickListener(this);
        findViewById(R.id.bn_weixing).setOnClickListener(this);
        findViewById(R.id.bn_dingwei).setOnClickListener(this);

        findViewById(R.id.ll_search).setOnClickListener(this);

        //添加刷新监听
        refreshLayout.setEnableRefresh(true);
        refreshLayout.setEnableLoadMore(true);
        refreshLayout.setEnableHeaderTranslationContent(true);//是否下拉Header的时候向下平移列表或者内容
        refreshLayout.setEnableFooterTranslationContent(true);//是否上拉Footer的时候向上平移列表或者内容
        refreshLayout.setRefreshFooter(new ClassicsFooter(this));
        refreshLayout.setRefreshHeader(new MyClassicsHeader(this));
        refreshLayout.setOnLoadMoreListener(this);
        refreshLayout.setOnRefreshListener(this);
    }

    private List<GridBean> gridBeanList;
    private GridsListAdapter resDataManageAdapter;
    Map<String, String> map = new HashMap<String , String>();
    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<String, List<JDPicInfo>>();

    boolean ischeck =false; //是否只能查看 true  只能查看不能编辑；

    private List<ResModelBean> resModelList = new ArrayList<ResModelBean>();
    private List<DongTaiFormBean> formBeanList=new ArrayList<>();// 当前资源的动态表单

    private LoadingProgressDialog lpd;
    private String PcToken;

    private   List<DongTaiFormBean> DongTainewFormList;
    @Override
    public void init() {
        isLogin();
        setLpd();
        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
        gridBeanList = new ArrayList<>();



        map = (Map<String, String>) getIntent().getSerializableExtra("exter");

        ischeck = getIntent().getBooleanExtra("ischeck", false);
        PcToken = getIntent().getStringExtra("PcToken");

        if (ischeck) {
            rl_bottom.setVisibility(View.GONE);

            getResTaskById();

        } else {

            createResTask();

        }

    }


    private void setLpd() {
        lpd = new LoadingProgressDialog();
        lpd.setMessage("正在获取表格列表，请稍等...");
        lpd.create(this, lpd.STYLE_SPINNER);
        lpd.setCancelable(false);
    }




    private void getResTaskById() {
       selectDB();
    }




    private void createResTask() {
        map.toString();

        ProjectBean newprojectBean = new ProjectBean();

        newprojectBean.setRemarks("");
        newprojectBean.setfTaskname(map.get("name"));
        newprojectBean.setfTaskmodel(map.get("type"));
        newprojectBean.setfBegindate(map.get("btime"));
        newprojectBean.setfEnddate(map.get("etime"));
        newprojectBean.setfState("2");
        newprojectBean.setfRange("");
        newprojectBean.setId(map.get("id"));
        newprojectBean.setfOfficeId(userInfo.getOffice().getId());
        newprojectBean.setfTaskmodelnames(map.get("typename"));
        newprojectBean.setfDescription("");

        lpd.show();
        new SaveResTaskAPI(userInfo.getAccess_token(), newprojectBean, new SaveResTaskAPI.SaveResTaskIF() {
            @Override
            public void onSaveResTaskResult(boolean isOk, ProjectBean projectBean1) {

                if (isOk) {

                    getResTaskById();

                }else {
                    lpd.cancel();
                }

            }
        }).request();
    }




    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_back:
                finish();
                break;
            case R.id.tv_maplsit:
                Intent mapintent = new Intent(mContext, RoadListShowOnMapActivity.class);
                mapintent.putExtra("projectBean", projectBean);
                mapintent.putExtra("exter", (Serializable) map);
                mapintent.putExtra("ischeck", ischeck);
                mapintent.putExtra("roadFormModelId", roadFormModelId);
                mapintent.putExtra("TreeFormModelId", TreeFormModelId);
                startActivityForResult(mapintent,6);
                break;

            case R.id.rl_bottom:
                Intent intent = new Intent(mContext, RoadDetailsActivity.class);
                ResModelBean resModelBean = new ResModelBean();
                for (int i = 0; i < resModelList.size(); i++) {

                    if (roadFormModelId.equals(resModelList.get(i).getId())) {


                        resModelBean = resModelList.get(i);
                    }
                }

                ResDataBean resDataBean = new ResDataBean();
                String createTime = new CCM_DateTime().Datetime2();
                resDataBean.setId(System.currentTimeMillis() + "");
                resDataBean.setFd_resmodelid(resModelBean.getId());
                resDataBean.setCreatedAt(createTime);
                resDataBean.setFd_resdate(createTime);
                resDataBean.setFdTaskId(projectBean.getId());
                resDataBean.setFd_restaskname(projectBean.getfTaskname());
                resDataBean.setFd_resmodelname(resModelBean.getfModelName());

                intent.putExtra("formBeanList", (Serializable) formBeanList);
                intent.putExtra("resDataBean", resDataBean);
                intent.putExtra("sportEditFlag", true);
                formPicMap.clear();
                intent.putExtra("formPicMap", (Serializable) formPicMap);

                startActivityForResult(intent,4);
                break;

            case R.id.ll_search:
                Intent searchIntent = new Intent(mContext, SearchGridsActivity.class);

                ResModelBean search_resModelBean = new ResModelBean();

                for (int i = 0; i < resModelList.size(); i++) {

                    if (roadFormModelId.equals(resModelList.get(i).getId())) {


                        search_resModelBean = resModelList.get(i);
                    }
                }

                ResDataBean search_resDataBean = new ResDataBean();
                String createTime1 = new CCM_DateTime().Datetime2();
                search_resDataBean.setId(System.currentTimeMillis() + "");
                search_resDataBean.setFd_resmodelid(search_resModelBean.getId());
                search_resDataBean.setCreatedAt(createTime1);
                search_resDataBean.setFd_resdate(createTime1);
                search_resDataBean.setFdTaskId(projectBean.getId());
                search_resDataBean.setFd_restaskname(projectBean.getfTaskname());
                search_resDataBean.setFd_resmodelname(search_resModelBean.getfModelName());

                searchIntent.putExtra("search_resDataBean", search_resDataBean);

                searchIntent.putExtra("projectBean", projectBean);
                searchIntent.putExtra("exter", (Serializable) map);
                searchIntent.putExtra("ischeck", ischeck);
                searchIntent.putExtra("roadFormModelId", roadFormModelId);
                searchIntent.putExtra("TreeFormModelId", TreeFormModelId);
                searchIntent.putExtra("PcToken", PcToken);
                searchIntent.putExtra("DongTainewFormList", (Serializable)DongTainewFormList);
                startActivity(searchIntent);

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


    // 查询
    private void selectDB() {
        gridBeanList.clear();
        if (map != null && map.size() > 0) {
            lpd.show();

            new getResTaskListAPI(userInfo.getAccess_token(), map.get("id"), 100, new getResTaskListAPI.ResTaskListIF() {
                @Override
                public void onResTaskListResult(boolean isOk, ProjectBean protBean, String msg) {

                    if (isOk) {
                        projectBean=protBean;

                            TreeFormModelId = projectBean.getfTaskmodel().split(",")[0];
                            new GetResModelAPI(userInfo.getAccess_token(), TreeFormModelId, new GetResModelAPI.GetResModelIF() {
                                @Override
                                public void ongetResModelIFResult(boolean isOk, ResModelBean resModelBen) {

                                    if (isOk) {//获取到网络数据

                                        ResModelDao resModelDao = new ResModelDao(mContext);
                                        resModelDao.addOrUpdate(resModelBen);
                                        String getfJsonData = resModelBen.getfJsonData();
                                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                                        List<DongTaiFormBean> newFormList = mGson.fromJson(getfJsonData, new TypeToken<List<DongTaiFormBean>>() {
                                        }.getType());

                                        DongTainewFormList=newFormList;


                                        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
                                        lv_roadList.setLayoutManager(layoutManager);

                                        resDataManageAdapter = new GridsListAdapter(PcToken,mContext,  TreeFormModelId, gridBeanList, projectBean, ischeck);
                                        lv_roadList.setAdapter(resDataManageAdapter);
                                        resDataManageAdapter.setMapBtnClickListener(GridsListActivity.this);
                                        int space = 20;
                                        lv_roadList.addItemDecoration(new SpacesItemDecoration(space));
                                        selectDB(projectBean.getId());

                                    } else {
                                        lpd.cancel();
                                    }


                                }
                            }).request();



                    } else {
                        lpd.cancel();
                        projectBean=new ProjectBean();
                        if (gridBeanList != null && gridBeanList.size() > 0) {
                            lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                            lv_roadList.setVisibility(View.VISIBLE);
                        } else {
                            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                            lv_roadList.setVisibility(View.GONE);
                        }

                        if (!Utils.isEmpty(msg)) {
                            Utils.showDialog(mContext, msg);
                            findViewById(R.id.rl_bottom).setVisibility(View.GONE);

                        }
                    }

                }
            }).request();

        }






    }







    // 查询
    private void selectDB(final String jqId) {
        Mlog.d("jqId = " + jqId);
        gridBeanList.clear();

        resModelList.clear();
        lpd.show();

        String classifyIds = projectBean.getfTaskmodel();
        if (classifyIds != null) {

            String[] ids = classifyIds.split(",");
            ResModelDao resModelDao = new ResModelDao(mContext);
            for(int i=0;i<ids.length;i++) {
                ResModelBean resModelBean = resModelDao.getDatById(ids[i]);
                if (resModelBean != null) {

                    resModelList.add(resModelBean);
                }
            }


        }

        if (resModelList == null || resModelList.size() == 0) {

            new ResModelListAPI(userInfo.getAccess_token(), new ResModelListAPI.ResModelListIF() {
                @Override
                public void onResModelListResult(boolean isOk, List<ResModelBean> projectBeanList) {

                    if (isOk) {
                        ResModelDao resModelDao = new ResModelDao(mContext);
                        resModelList.clear();
                        resModelList.addAll(projectBeanList);
                        resModelDao.addOrUpdate(resModelList);
                        ResModelBean resModelBean = new ResModelBean();

                        for (int i = 0; i < resModelList.size(); i++) {

                            if (roadFormModelId.equals(resModelList.get(i).getId())) {


                                 resModelBean = resModelList.get(i);
                            }
                        }





                        String formData = resModelBean.getfJsonData();
                        formBeanList.clear();
                        try {
                            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                            List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                                    new TypeToken<List<DongTaiFormBean>>() {}.getType());
                            formBeanList.addAll(dictList);
                        } catch (JsonIOException e) {
                            e.printStackTrace();
                        }

                        JDPicDao jdPicDao = new JDPicDao(mContext);
                        for (int i = 0; i < gridBeanList.size(); i++) {
                            GridBean resDataBean = gridBeanList.get(i);

                            List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

//                            resDataBean.setJdPicInfos(jdPicInfoList);

                        }


                        getdataList();


                    }


                }
            }).request();


        } else {

            ResModelBean resModelBean = new ResModelBean();

            for (int i = 0; i < resModelList.size(); i++) {

                if (TreeFormModelId.equals(resModelList.get(i).getId())) {


                    resModelBean = resModelList.get(i);
                }
            }



            String formData = resModelBean.getfJsonData();
            formBeanList.clear();
            try {
                Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                        new TypeToken<List<DongTaiFormBean>>() {}.getType());
                formBeanList.addAll(dictList);
            } catch (JsonIOException e) {
                e.printStackTrace();
            }


            ResDataDao resDataDao = new ResDataDao(getApplication());
//            List<GridBean> resDataBeans = resDataDao.getData(jqId + "",true);
//            if (resDataBeans != null && resDataBeans.size() > 0) {
//
//                gridBeanList.addAll(resDataBeans);
//            }


            JDPicDao jdPicDao = new JDPicDao(mContext);
            for (int i = 0; i < gridBeanList.size(); i++) {
                GridBean resDataBean = gridBeanList.get(i);

                List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

//                resDataBean.setJdPicInfos(jdPicInfoList);

            }


            getdataList();
        }

    }

    private void getdataList(){

        new FindGridsListPageAPI(page,userInfo, new FindGridsListPageAPI.DatadicListIF() {
            @Override
            public void datadicListResult(boolean isOk, List<GridBean> netDataList,int count) {
                if (isOk) {
                    if (isRefresh) {
                        gridBeanList.clear();
                    }
                    if (gridBeanList.size() > 0 || netDataList.size() >0 ){
                        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                    }else {
                        lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                    }

                    if (page > count) {
                        refreshLayout.setEnableLoadMore(false);
                        refreshLayout.finishLoadMore();
                        refreshLayout.finishLoadMoreWithNoMoreData();
                    } else {
                        if (!refreshLayout.isEnableLoadMore()) {
                            refreshLayout.setEnableLoadMore(true);
                            refreshLayout.setNoMoreData(false);//恢复没有更多数据的原始状态 1.0.5
                        }
                    }
                    gridBeanList.addAll(netDataList);
                    resDataManageAdapter.notifyDataSetChanged();
                    lpd.cancel();
                }

                if (isRefresh) {
                    refreshLayout.finishRefresh();
                    isRefresh = false;
                }
                if (loadMore) {
                    refreshLayout.finishLoadMore();
                    loadMore = false;
                }

//                getTreeNum();


            }

            private void getTreeNum() {
                final TaskPool taskPool=new TaskPool();

                OnNetRequestListener listener=new OnNetRequestListener() {
                    @Override
                    public void onFinish() {
                        lpd.cancel();
                        if (gridBeanList != null && gridBeanList.size() > 0) {
                            lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                            lv_roadList.setVisibility(View.VISIBLE);
                        }else {
                            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                            lv_roadList.setVisibility(View.GONE);
                        }
                        resDataManageAdapter.notifyDataSetChanged();
                    }


                    @Override
                    public void OnNext() {

                        taskPool.execute(this);
                    }

                    @Override
                    public void OnResult(boolean isOk, String msg, Object object) {
                        taskPool.execute(this);
                    }
                };

                if (gridBeanList != null && gridBeanList.size() > 0) {

                    for (int i = 0; i < gridBeanList.size(); i++) {

                        GridBean resDataBean = gridBeanList.get(i);

                        taskPool.addTask(new GetGridTreeCountAPI(userInfo, TreeFormModelId, resDataBean, listener));

                    }

                    taskPool.execute(listener);
                } else {

                    lpd.cancel();
                    if (gridBeanList != null && gridBeanList.size() > 0) {
                        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                        lv_roadList.setVisibility(View.VISIBLE);
                    } else {
                        lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                        lv_roadList.setVisibility(View.GONE);
                    }
                }
            }
        }).request();


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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 4) {

            if (resultCode == 1) {


                selectDB(projectBean.getId());

            }
        }
        //在地图页面修改了地图修改
        if (requestCode == 6) {

            if (resultCode == 1) {


                selectDB(projectBean.getId());

            }
        }
//        //在地图页面修改了地图修改
//        if (requestCode == 7) {
//
//            int position = data.getIntExtra("position", 0);
//            GridBean gridBean = (GridBean) data.getSerializableExtra("gridBean");
//
//            gridBeanList.get(position).setChildTreeCount(gridBean.getChildTreeCount());
//            resDataManageAdapter.notifyDataSetChanged();
//        }
    }


    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {


        loadMore = true;
        page++;
        getdataList();

    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        isRefresh = true;
        page = 1;
        Mlog.d("刷新了页面");
        getdataList();
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
    public void activate(OnLocationChangedListener onLocationChangedListener) {

    }

    @Override
    public void deactivate() {

    }

    @Override
    public void MapBtnClick(GridBean gridBean,int curPosition) {
        currrentGridBeanshownOnMap = gridBean;
        currrentPositionGridshownOnMap = curPosition;
//        mGaoDeMap.setMyLocationEnabled(false);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。

        if (!rl_mapContent.isShown()) {

            rl_mapContent.setVisibility(View.VISIBLE);
        }

        new FindListPageAPI(10000,userInfo,TreeFormModelId, projectBean, gridBean.getId(), new FindListPageAPI.DatadicListIF() {
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


                    drawGrid(gridBean,points);

                    tv_griNumber.setText(gridBean.getFdBlockCode());


                    }


            }
        }).request();

    }




    /***
     * 绘制表格
     */

    private void drawGrid(GridBean gridBean,List<LatLng> netDataList) {
        mGaoDeMap.clear();

        if (gridBean == null) {
            return;
        }

        List<LatLng> rectangles = createRectangle(gridBean);

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

    private List<LatLng> createRectangle(GridBean gridBean) {
        List<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(gridBean.getFdLbLat(), gridBean.getFdRtLng()));
        latLngs.add(new LatLng(gridBean.getFdRtLat(), gridBean.getFdRtLng()));
        latLngs.add(new LatLng(gridBean.getFdRtLat(), gridBean.getFdLbLng()));
        latLngs.add(new LatLng(gridBean.getFdLbLat(), gridBean.getFdLbLng()));
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
}
