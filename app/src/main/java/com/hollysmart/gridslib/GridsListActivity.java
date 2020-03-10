package com.hollysmart.gridslib;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
public class GridsListActivity extends StyleAnimActivity implements OnRefreshLoadMoreListener {

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




    private ProjectBean projectBean;



    private String roadFormModelId = "";
    private String TreeFormModelId = "";

    private int page=1;
    private boolean isRefresh=false;
    private boolean loadMore=false;
    private int  pageSize=100;


    @Override
    public void findView() {
        ButterKnife.bind(this);
        iv_back.setOnClickListener(this);
        tv_maplsit.setOnClickListener(this);
        rl_bottom.setOnClickListener(this);
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
        }
    }


    // 查询
    private void selectDB() {
        gridBeanList.clear();
        if (map != null && map.size() > 0) {
            lpd.show();

            new getResTaskListAPI(userInfo.getAccess_token(), map.get("id"), 100, new getResTaskListAPI.ResTaskListIF() {
                @Override
                public void onResTaskListResult(boolean isOk, List<ProjectBean> projectBeanList, int count,String msg) {

                    if (isOk) {
                        if (projectBeanList != null && projectBeanList.size() > 0) {

                            projectBean = projectBeanList.get(0);

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
                                        selectDB(projectBean.getId());

                                    } else {
                                        lpd.cancel();
                                    }


                                }
                            }).request();

                        }


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
        //在地图页面修改了地图修改
        if (requestCode == 7) {

            int position = data.getIntExtra("position", 0);
            GridBean gridBean = (GridBean) data.getSerializableExtra("gridBean");

            gridBeanList.get(position).setChildTreeCount(gridBean.getChildTreeCount());
            resDataManageAdapter.notifyDataSetChanged();
        }
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


}
