package com.hollysmart.gridslib;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.hollysmart.gridslib.adapters.TreeListAdapter;
import com.hollysmart.gridslib.apis.FindListPage2API;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.CCM_DateTime;
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
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TreeList2Activity extends StyleAnimActivity implements FindListPage2API.DatadicListCountIF {


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


    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集


    @Override
    public void findView() {
        ButterKnife.bind(this);
        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
        ib_back.setOnClickListener(this);
        tv_maplsit.setOnClickListener(this);
        bn_add.setOnClickListener(this);
    }

    private List<ResDataBean> mJingDians;
    private TreeListAdapter resDataManageAdapter;
    Map<String, String> map = new HashMap<String, String>();
    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<String, List<JDPicInfo>>();

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    private List<ResModelBean> resModelList = new ArrayList<ResModelBean>();
    private List<DongTaiFormBean> formBeanList = new ArrayList<>();// 当前资源的动态表单

    private LoadingProgressDialog lpd;

    private BlockBean roadBean;
    private String TreeFormModelId;

    private ProjectBean projectBean;

    private boolean loading = false;

    @Override
    public void init() {
        isLogin();
        picList = new ArrayList<>();
        soundList = new ArrayList<>();
        mJingDians = new ArrayList<>();



        ischeck = getIntent().getBooleanExtra("ischeck", false);
        TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");

        roadBean = (BlockBean) getIntent().getSerializableExtra("roadBean");
        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        lv_treeList.setLayoutManager(new LinearLayoutManager(this));

        lv_treeList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);


            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) //向下滚动
                {
                    int visibleItemCount = linearLayoutManager.getChildCount();
                    int totalItemCount = linearLayoutManager.getItemCount();
                    int pastVisiblesItems = linearLayoutManager.findFirstVisibleItemPosition();

                    if (!loading && (visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loading = true;
                        loadMoreData();
                    }
                }
            }
        });
        setLpd();
        selectDB();



    }

    private int pageNo=1;
    private int pageSize=30;

    private void loadMoreData() {
        new FindListPage2API(pageNo,pageSize ,userInfo,TreeFormModelId, projectBean,roadBean.getId(),this).request();

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

                    startActivityForResult(intent, 4);

                }




                break;
        }
    }


    // 查询
    private void selectDB() {
        lpd.show();
        mJingDians.clear();
            lpd.setMessage("请求数据中...请稍后");
            lpd.show();
            if (ischeck) {
                bn_add.setVisibility(View.GONE);

            }
            getResTaskById();


    }


    private void getResTaskById() {

//        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
//        lv_treeList.setLayoutManager(layoutManager);

        resDataManageAdapter = new TreeListAdapter(mContext,TreeFormModelId, projectBean,roadBean, mJingDians, ischeck);

        lv_treeList.setAdapter(resDataManageAdapter);

        new FindListPage2API(pageNo,pageSize,userInfo,TreeFormModelId, projectBean,roadBean.getId(), new FindListPage2API.DatadicListCountIF() {
            @Override
            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList,int allcount) {
                List<String> idList = new ArrayList<>();

                for (ResDataBean resDataBean : mJingDians) {

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


                                mJingDians.add(resDataBean);

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
        mJingDians.clear();

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
                        for (int i = 0; i < mJingDians.size(); i++) {
                            ResDataBean resDataBean = mJingDians.get(i);

                            List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                            resDataBean.setJdPicInfos(jdPicInfoList);

                        }



                        new FindListPage2API(pageNo,pageSize,userInfo, TreeFormModelId,projectBean,roadBean.getId(), new FindListPage2API.DatadicListCountIF() {
                            @Override
                            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList,int allcount) {


                                List<String> idList = new ArrayList<>();

                                for (ResDataBean resDataBean : mJingDians) {

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


                                                mJingDians.add(resDataBean);


                                            }
                                        }


                                    }
                                }


                                lpd.cancel();
                                if (mJingDians != null && mJingDians.size() > 0) {
                                    lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                                    lv_treeList.setVisibility(View.VISIBLE);
                                }else {
                                    lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                                    lv_treeList.setVisibility(View.GONE);
                                }


                                resDataManageAdapter.notifyDataSetChanged();


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

                mJingDians.addAll(resDataBeans);
            }


            JDPicDao jdPicDao = new JDPicDao(mContext);
            for (int i = 0; i < mJingDians.size(); i++) {
                ResDataBean resDataBean = mJingDians.get(i);

                List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                resDataBean.setJdPicInfos(jdPicInfoList);

            }


            new FindListPage2API(pageNo,pageSize,userInfo, TreeFormModelId,projectBean,roadBean.getId(), new FindListPage2API.DatadicListCountIF() {
                @Override
                public void datadicListResult(boolean isOk, List<ResDataBean> netDataList,int allcount) {


                    List<String> idList = new ArrayList<>();

                    for (ResDataBean resDataBean : mJingDians) {

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


                                    mJingDians.add(resDataBean);


                                }
                            }


                        }
                    }

                    if (mJingDians != null && mJingDians.size() > 0) {
                        lay_fragment_ProdutEmpty.setVisibility(View.GONE);
                        lv_treeList.setVisibility(View.VISIBLE);
                    }else {
                        lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
                        lv_treeList.setVisibility(View.GONE);
                    }


                    resDataManageAdapter.notifyDataSetChanged();


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


    @Override
    public void datadicListResult(boolean isOk, List<ResDataBean> menuBeanList, int allCount) {

        lpd.cancel();
        if (isOk && menuBeanList != null) {
            if (loading) {
                mJingDians.clear();
                loading = false;
            }
            mJingDians.addAll(menuBeanList);
            if (mJingDians.size() == allCount) {
            } else {
                pageNo++;
            }
            resDataManageAdapter.notifyDataSetChanged();
        }


    }
}
