package com.hollysmart.roadlib;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.d.lib.xrv.LRecyclerView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.apis.ResModelListAPI;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.beans.ResModelBean;
import com.hollysmart.db.JDPicDao;
import com.hollysmart.db.LastTreeResDataDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.ResListShowOnMapActivity;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.LastTreeDataBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.roadlib.adapters.MyClassicsHeader;
import com.hollysmart.roadlib.adapters.TreeListAdapter;
import com.hollysmart.roadlib.apis.FindListPageAPI;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.CCM_DateTime;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.footer.ClassicsFooter;
import com.scwang.smartrefresh.layout.listener.OnRefreshLoadMoreListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TreeListActivity extends StyleAnimActivity  implements OnRefreshLoadMoreListener {


    @Override
    public int layoutResID() {
        return R.layout.activity_tree_list;
    }

    @BindView(R.id.ib_back)
    ImageView ib_back;

    @BindView(R.id.tv_maplsit)
    TextView tv_maplsit;

    @BindView(R.id.lv_treeList)
    LRecyclerView lv_treeList;

    @BindView(R.id.rl_bottom)
    RelativeLayout bn_add;


    @BindView(R.id.lay_fragment_ProdutEmpty)
    LinearLayout lay_fragment_ProdutEmpty;

    @BindView(R.id.smart_refresh)
    SmartRefreshLayout refreshLayout;


    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集




    @Override
    public void findView() {
        ButterKnife.bind(this);
        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
        ib_back.setOnClickListener(this);
        tv_maplsit.setOnClickListener(this);
        bn_add.setOnClickListener(this);
        findViewById(R.id.ll_search).setOnClickListener(this);
        findViewById(R.id.tv_guifan).setOnClickListener(this);



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

    private List<ResDataBean> treeslist;
    private TreeListAdapter treeListAdapter;
    Map<String, String> map = new HashMap<String, String>();
    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<String, List<JDPicInfo>>();

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    private List<ResModelBean> resModelList = new ArrayList<ResModelBean>();
    private List<DongTaiFormBean> formBeanList = new ArrayList<>();// 当前资源的动态表单

    private LoadingProgressDialog lpd;

    private ResDataBean roadBean;
    private String TreeFormModelId;
    private String PcToken;

    private ProjectBean projectBean;

    @Override
    public void init() {
        isLogin();
        picList = new ArrayList<>();
        soundList = new ArrayList<>();
        treeslist = new ArrayList<>();



        ischeck = getIntent().getBooleanExtra("ischeck", false);
        PcToken = getIntent().getStringExtra("PcToken");
        TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");

        roadBean = (ResDataBean) getIntent().getSerializableExtra("roadBean");
        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");
        setLpd();
        selectDB();

        showGrid();
    }

    private void showGrid() {


        LayoutInflater inflater = LayoutInflater.from(this);
        View layout =  inflater.inflate(R.layout.dialog_layout_gridle, null);
        //新建对话框对象
        Dialog mDialog= new AlertDialog.Builder(this).create();
        mDialog.setCancelable(true);
        mDialog.show();
        // 设置弹出框的透明度
        WindowManager.LayoutParams lp = mDialog.getWindow().getAttributes();
        mDialog.getWindow().setAttributes(lp);
        mDialog.getWindow().setContentView(layout);

        layout.findViewById(R.id.btn_iKnow).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.cancel();

            }
        });





    }


    private void setLpd() {
        lpd = new LoadingProgressDialog();
        lpd.setMessage("正在获取树木列表，请稍等...");
        lpd.create(this, lpd.STYLE_SPINNER);
        lpd.setCancelable(false);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
            case R.id.tv_maplsit:
                Intent mapintent = new Intent(mContext, ResListShowOnMapActivity.class);
                mapintent.putExtra("roadBean", roadBean);
                mapintent.putExtra("projectBean", projectBean);
                mapintent.putExtra("exter", (Serializable) map);
                mapintent.putExtra("ischeck", ischeck);
                mapintent.putExtra("TreeFormModelId", TreeFormModelId);
                startActivityForResult(mapintent, 6);
                break;

            case R.id.rl_bottom:
                Intent intent = new Intent(mContext, TreeDetailsActivity.class);

                ResModelBean resModelBean = new ResModelBean();

                for (int i = 0; i < resModelList.size(); i++) {

                    if (TreeFormModelId.equals(resModelList.get(i).getId())) {


                        resModelBean = resModelList.get(i);
                    }
                }

                LastTreeResDataDao lastTreeResDataDao = new LastTreeResDataDao(mContext);
                LastTreeDataBean upDataBean = lastTreeResDataDao.getLastId();//上一个资源

                if (upDataBean != null) {


                    String formDatas = upDataBean.getFormData();
                    formBeanList.clear();
                    try {
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        JSONObject jsonObject = new JSONObject(formDatas);
                        List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                new TypeToken<List<DongTaiFormBean>>() {
                                }.getType());
                        formBeanList.addAll(dictList);
                        for (DongTaiFormBean formBean : formBeanList) {

                            if (formBean.getJavaField().equals("location")) {

                                formBean.setPropertyLabel("");

                            }
                            if (formBean.getJavaField().equals("tree_dangerous")) {

                                formBean.setPropertyLabel("");



                            }
                            if (formBean.getJavaField().equals("tree_images")) {

                                formBean.setPropertyLabel("");

                                formBean.setPic(new ArrayList<>());

                            }
                            if (formBean.getJavaField().equals("time")) {

                                formBean.setPropertyLabel("");

                                formBean.setPic(new ArrayList<>());

                            }
                        }

                    } catch (JsonIOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                    ResDataBean resDataBean = new ResDataBean();
                    String createTime = new CCM_DateTime().Datetime2();
                    resDataBean.setId(System.currentTimeMillis() + "");
                    resDataBean.setFd_resmodelid(resModelBean.getId());
                    resDataBean.setCreatedAt(createTime);
                    resDataBean.setFd_resdate(createTime);
                    resDataBean.setFd_resmodelname(resModelBean.getfModelName());
                    resDataBean.setFd_restaskname(projectBean.getfTaskname());
                    resDataBean.setFd_parentid(roadBean.getId());
                    resDataBean.setFdTaskId(projectBean.getId());

                    intent.putExtra("formBeanList", (Serializable) formBeanList);
                    intent.putExtra("resDataBean", resDataBean);
                    intent.putExtra("sportEditFlag", true);
                    formPicMap.clear();
                    intent.putExtra("formPicMap", (Serializable) formPicMap);
                    intent.putExtra("roadbean", (Serializable) roadBean);
                    intent.putExtra("projectBean", (Serializable) projectBean);
                    intent.putExtra("isNewAdd",  true);
                    intent.putExtra("PcToken",  PcToken);

                    startActivityForResult(intent, 4);
                } else {

                    ResDataBean resDataBean = new ResDataBean();
                    String createTime = new CCM_DateTime().Datetime2();
                    resDataBean.setId(System.currentTimeMillis() + "");
                    resDataBean.setFd_resmodelid(resModelBean.getId());
                    resDataBean.setCreatedAt(createTime);
                    resDataBean.setFd_resdate(createTime);
                    resDataBean.setFd_resmodelname(resModelBean.getfModelName());
                    resDataBean.setFd_restaskname(projectBean.getfTaskname());
                    resDataBean.setFd_parentid(roadBean.getId());
                    resDataBean.setFdTaskId(projectBean.getId());

                    intent.putExtra("formBeanList", (Serializable) formBeanList);
                    intent.putExtra("resDataBean", resDataBean);
                    intent.putExtra("sportEditFlag", true);
                    formPicMap.clear();
                    intent.putExtra("formPicMap", (Serializable) formPicMap);
                    intent.putExtra("roadbean", (Serializable) roadBean);
                    intent.putExtra("projectBean", (Serializable) projectBean);

                    intent.putExtra("isNewAdd",  true);
                    intent.putExtra("PcToken",  PcToken);

                    startActivityForResult(intent, 4);

                }




                break;

            case R.id.ll_search:

                Intent searchIntent = new Intent(mContext, SearchTreeActivity.class);
                searchIntent.putExtra("TreeFormModelId", TreeFormModelId);
                searchIntent.putExtra("projectBean", projectBean);
                searchIntent.putExtra("roadBean", roadBean);
                searchIntent.putExtra("ischeck", ischeck);
                startActivity(searchIntent);

                break;
            case R.id.tv_guifan:

                showGrid();

                break;
        }
    }


    // 查询
    private void selectDB() {
        lpd.show();
        treeslist.clear();
            lpd.setMessage("请求数据中...请稍后");
            lpd.show();
            if (ischeck) {
                bn_add.setVisibility(View.GONE);

            }
            getResTaskById();


    }


    private void getResTaskById() {

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        lv_treeList.setLayoutManager(layoutManager);

        treeListAdapter = new TreeListAdapter(mContext,TreeFormModelId, projectBean,roadBean, treeslist, ischeck);

        lv_treeList.setAdapter(treeListAdapter);

        new FindListPageAPI(userInfo,TreeFormModelId, projectBean,roadBean.getId(), new FindListPageAPI.DatadicListIF() {
            @Override
            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {
                List<String> idList = new ArrayList<>();

                for (ResDataBean resDataBean : treeslist) {

                    idList.add(resDataBean.getId());
                }


                if (isOk) {
                    if (netDataList != null && netDataList.size() > 0) {
                        int j = 0;

                        for (int i = 0; i < netDataList.size(); i++) {

                            ResDataBean resDataBean = netDataList.get(i);

                            if (!idList.contains(resDataBean.getId())) {
                                String fd_resposition = resDataBean.getFd_resposition();

                                if (!Utils.isEmpty(fd_resposition)) {

                                    String[] split = fd_resposition.split(",");
                                    resDataBean.setLatitude(split[0]);
                                    resDataBean.setLongitude(split[1]);

                                }


                                treeslist.add(resDataBean);

                                j = j + 1;

                            }
                        }


                    }
                }

                selectDB(roadBean.getId());

                lpd.cancel();

            }
        }).request();
    }


    // 查询
    private void selectDB(final String jqId) {
        Mlog.d("jqId = " + jqId);
        treeslist.clear();

        resModelList.clear();


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

                            if (TreeFormModelId.equals(resModelList.get(i).getId())) {


                                resModelBean = resModelList.get(i);
                            }
                        }


                        String formData = resModelBean.getfJsonData();
                        formBeanList.clear();
                        try {
                            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                            List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                                    new TypeToken<List<DongTaiFormBean>>() {
                                    }.getType());
                            formBeanList.addAll(dictList);
                        } catch (JsonIOException e) {
                            e.printStackTrace();
                        }

                        JDPicDao jdPicDao = new JDPicDao(mContext);
                        for (int i = 0; i < treeslist.size(); i++) {
                            ResDataBean resDataBean = treeslist.get(i);

                            List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                            resDataBean.setJdPicInfos(jdPicInfoList);

                        }



                        new FindListPageAPI(userInfo, TreeFormModelId,projectBean,roadBean.getId(), new FindListPageAPI.DatadicListIF() {
                            @Override
                            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {


                                List<String> idList = new ArrayList<>();

                                for (ResDataBean resDataBean : treeslist) {

                                    idList.add(resDataBean.getId());
                                }


                                if (isOk) {
                                    if (netDataList != null && netDataList.size() > 0) {
                                        int j = 0;

                                        for (int i = 0; i < netDataList.size(); i++) {

                                            ResDataBean resDataBean = netDataList.get(i);

                                            if (!idList.contains(resDataBean.getId())) {
                                                String fd_resposition = resDataBean.getFd_resposition();

                                                if (!Utils.isEmpty(fd_resposition)) {

                                                    String[] split = fd_resposition.split(",");
                                                    resDataBean.setLatitude(split[0]);
                                                    resDataBean.setLongitude(split[1]);

                                                }


                                                treeslist.add(resDataBean);


                                            }
                                        }


                                    }
                                }


                                lpd.cancel();
                                if (treeslist != null && treeslist.size() > 0) {
                                    lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                                    lv_treeList.setVisibility(View.VISIBLE);
                                }else {
                                    lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                                    lv_treeList.setVisibility(View.GONE);
                                }


                                treeListAdapter.notifyDataSetChanged();


                            }
                        }).request();





                    }


                }
            }).request();


        } else {
            ResModelBean resModelBean = resModelList.get(0);


            String formData = resModelBean.getfJsonData();
            formBeanList.clear();
            try {
                Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                        new TypeToken<List<DongTaiFormBean>>() {
                        }.getType());
                formBeanList.addAll(dictList);
            } catch (JsonIOException e) {
                e.printStackTrace();
            }


            ResDataDao resDataDao = new ResDataDao(getApplication());
            List<ResDataBean> resDataBeans = resDataDao.getData(jqId + "");
            if (resDataBeans != null && resDataBeans.size() > 0) {

                treeslist.addAll(resDataBeans);
            }


            JDPicDao jdPicDao = new JDPicDao(mContext);
            for (int i = 0; i < treeslist.size(); i++) {
                ResDataBean resDataBean = treeslist.get(i);

                List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                resDataBean.setJdPicInfos(jdPicInfoList);

            }


            new FindListPageAPI(userInfo,TreeFormModelId, projectBean,roadBean.getId(), new FindListPageAPI.DatadicListIF() {
                @Override
                public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {


                    List<String> idList = new ArrayList<>();

                    for (ResDataBean resDataBean : treeslist) {

                        idList.add(resDataBean.getId());
                    }


                    if (isOk) {
                        if (netDataList != null && netDataList.size() > 0) {
                            int j = 0;

                            for (int i = 0; i < netDataList.size(); i++) {

                                ResDataBean resDataBean = netDataList.get(i);

                                if (!idList.contains(resDataBean.getId())) {
                                    String fd_resposition = resDataBean.getFd_resposition();

                                    if (!Utils.isEmpty(fd_resposition)) {

                                        String[] split = fd_resposition.split(",");
                                        resDataBean.setLatitude(split[0]);
                                        resDataBean.setLongitude(split[1]);

                                    }


                                    treeslist.add(resDataBean);


                                }
                            }


                        }
                    }

                    if (treeslist != null && treeslist.size() > 0) {
                        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                        lv_treeList.setVisibility(View.VISIBLE);
                    }else {
                        lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                        lv_treeList.setVisibility(View.GONE);
                    }


                    treeListAdapter.notifyDataSetChanged();


                }
            }).request();
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 4) {

            if (resultCode == 1) {


                selectDB(roadBean.getId());

            }
        }
        //在地图页面修改了地图修改
        if (requestCode == 6) {

            if (resultCode == 1) {


                selectDB(roadBean.getId());

            }
        }
    }


    private boolean isRefresh=false;

    private boolean loadMore=false;

    private int page=1;

    private int  pageSize=20;

    @Override
    public void onLoadMore(RefreshLayout refreshLayout) {

        loadMore = true;
        page++;
        getData();



    }

    @Override
    public void onRefresh(RefreshLayout refreshLayout) {
        isRefresh = true;
        page = 1;
        Mlog.d("刷新了页面");
        getData();


    }

    private void getData() {

        new FindListPageAPI(page, pageSize, userInfo, TreeFormModelId, projectBean, roadBean.getId(), new FindListPageAPI.DatadicListCountIF() {
            @Override
            public void datadicListResult(boolean isOk, List<ResDataBean> list, int allCount) {

            if (isOk) {
                if (isRefresh) {
                    treeslist.clear();
                }
                if (treeslist.size() > 0 || list.size() >0 ){
                    lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                }else {
                    lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                }

                if (list.size() < pageSize) {
                    refreshLayout.setEnableLoadMore(false);
                    refreshLayout.finishLoadMore();
                    refreshLayout.finishLoadMoreWithNoMoreData();
                } else {
                    if (!refreshLayout.isEnableLoadMore()) {
                        refreshLayout.setEnableLoadMore(true);
                        refreshLayout.setNoMoreData(false);//恢复没有更多数据的原始状态 1.0.5
                    }
                }
                treeslist.addAll(list);
                treeListAdapter.notifyDataSetChanged();
            }

            if (isRefresh) {
                refreshLayout.finishRefresh();
                isRefresh = false;
            }
            if (loadMore) {
                refreshLayout.finishLoadMore();
                loadMore = false;
            }


            }
        }).request();

    }
}
