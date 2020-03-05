package com.hollysmart.roadlib;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.d.lib.xrv.LRecyclerView;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.roadlib.adapters.RoadListAdapter;
import com.hollysmart.roadlib.apis.SearchListAPI;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchRoadActivity extends StyleAnimActivity implements  SearchListAPI.DataSearchListIF{


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


    private List<ResDataBean> roadBeanList;
    private RoadListAdapter resDataManageAdapter;

    private LoadingProgressDialog lpd;

    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    private String roadFormModelId;
    private String TreeFormModelId;
    private String PcToken;

    private ProjectBean projectBean;

    private List<DongTaiFormBean> DongTainewFormList;

    @Override
    public void findView() {

        ButterKnife.bind(this);
        iv_back.setOnClickListener(this);
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
        roadFormModelId = getIntent().getStringExtra("roadFormModelId");
        TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");
        PcToken = getIntent().getStringExtra("PcToken");




        resDataManageAdapter = new RoadListAdapter(PcToken,mContext, roadFormModelId, TreeFormModelId, roadBeanList, picList, soundList, projectBean, DongTainewFormList, ischeck);
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
        new SearchListAPI(userInfo,"2",editSearch,search_resDataBean, this).request();





    }

    @Override
    public void dataSearchList(boolean isOk, List<ResDataBean> menuBeanList) {
        lpd.cancel();

        if (isOk) {
            roadBeanList.clear();
            roadBeanList.addAll(menuBeanList);
            lv_roadList.setVisibility(View.VISIBLE);
            resDataManageAdapter.notifyDataSetChanged();


        }else {
            roadBeanList.clear();
            resDataManageAdapter.notifyDataSetChanged();
            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}
