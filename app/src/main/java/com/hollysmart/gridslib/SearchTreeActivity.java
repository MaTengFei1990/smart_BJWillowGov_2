package com.hollysmart.gridslib;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.d.lib.xrv.LRecyclerView;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.gridslib.adapters.TreeListAdapter;
import com.hollysmart.gridslib.apis.SearchListAPI;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchTreeActivity extends StyleAnimActivity implements  SearchListAPI.DataSearchListIF{


    @Override
    public int layoutResID() {
        return R.layout.activity_search_tree;
    }


    @BindView(R.id.iv_back)
    ImageView iv_back;

    @BindView(R.id.lv_treeList)
    LRecyclerView lv_treeList;


    @BindView(R.id.lay_fragment_ProdutEmpty)
    LinearLayout lay_fragment_ProdutEmpty;

    @BindView(R.id.ed_search)
    EditText ed_search;

    private List<ResDataBean> treeslist;
    private TreeListAdapter treeListAdapter;

    private LoadingProgressDialog lpd;


    private GridBean roadBean;
    private String TreeFormModelId;

    private boolean ischeck ;

    private ProjectBean projectBean;


    private ResDataBean search_resDataBean;


    @Override
    public void findView() {
        ButterKnife.bind(this);
        iv_back.setOnClickListener(this);
        findViewById(R.id.ll_search).setOnClickListener(this);
        isLogin();
        setLpd();

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

    @Override
    public void init() {

        treeslist = new ArrayList<>();
        lay_fragment_ProdutEmpty.setVisibility(View.GONE);

        TreeFormModelId = getIntent().getStringExtra("TreeFormModelId");

        roadBean = (GridBean) getIntent().getSerializableExtra("roadBean");
        projectBean = (ProjectBean) getIntent().getSerializableExtra("projectBean");

        ischeck = getIntent().getBooleanExtra("ischeck", false);


        search_resDataBean=new ResDataBean();

        search_resDataBean.setFdTaskId(projectBean.getId());
        search_resDataBean.setFd_resmodelid(TreeFormModelId);





        treeListAdapter = new TreeListAdapter(mContext,TreeFormModelId, projectBean,roadBean, treeslist, ischeck);
        lv_treeList.setAdapter(treeListAdapter);

    }



    private void setLpd() {
        lpd = new LoadingProgressDialog();
        lpd.setMessage("正在获取树木列表，请稍等...");
        lpd.create(this, lpd.STYLE_SPINNER);
        lpd.setCancelable(false);
    }




    /**
     *
     * @param editSearch
     */

    private void search(String editSearch){

        if (Utils.isEmpty(editSearch)) {
            treeslist.clear();
            treeListAdapter.notifyDataSetChanged();
            lay_fragment_ProdutEmpty.setVisibility(View.VISIBLE);

            return;
        }


        lpd.show();
        lv_treeList.setAdapter(treeListAdapter);
        new SearchListAPI(userInfo,"1",editSearch,search_resDataBean, this).request();



    }



    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_back:
                finish();
                break;
        }

    }




    @Override
    public void dataSearchList(boolean isOk, List<ResDataBean> menuBeanList) {
        lpd.cancel();

        if (isOk) {
            treeslist.clear();
            treeslist.addAll(menuBeanList);
            lv_treeList.setVisibility(View.VISIBLE);
            lay_fragment_ProdutEmpty.setVisibility(View.GONE);
            treeListAdapter.notifyDataSetChanged();


        }else {
            treeslist.clear();
            treeListAdapter.notifyDataSetChanged();
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
