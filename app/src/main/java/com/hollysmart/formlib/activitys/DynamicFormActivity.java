package com.hollysmart.formlib.activitys;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.apis.GetDictListDataAPI;
import com.hollysmart.apis.GetResModelAPI;
import com.hollysmart.apis.GetResModelVersionAPI;
import com.hollysmart.beans.DictionaryBean;
import com.hollysmart.beans.ResModelBean;
import com.hollysmart.beans.cgformRuleBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.DictionaryDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.adapters.BiaoGeRecyclerAdapter2;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DynamicFormActivity extends StyleAnimActivity  {


    @Override
    public int layoutResID() {
        return R.layout.activity_dynamic_form;
    }

    private RecyclerView recy_view;

    private List<DongTaiFormBean> resFromBeanLsit;
    private List<DongTaiFormBean> formBeanList;


    private BiaoGeRecyclerAdapter2 biaoGeRecyclerAdapter;

    private TextView tv_title;

    //目标项是否在最后一个可见项之后
    private boolean mShouldScroll;
    //记录目标项位置
    private int mToPosition;

    private ResModelBean showRes;

    private ResModelDao resModelDao;
    private  DictionaryDao dictionaryDao;

    @Override
    public void findView() {
        recy_view = findViewById(R.id.recy_view);
        tv_title = findViewById(R.id.tv_title);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //设置布局管理器
        recy_view.setLayoutManager(layoutManager);

        findViewById(R.id.iv_shure).setOnClickListener(this);
        findViewById(R.id.ib_back).setOnClickListener(this);


    }

    @Override
    public void init() {
        isLogin();

        ResModelBean selectBean = (ResModelBean) getIntent().getSerializableExtra("selectBean");
        resFromBeanLsit = (List<DongTaiFormBean>) getIntent().getSerializableExtra("formBeanList");
        tv_title.setText("信息录入-" + selectBean.getName());

        resModelDao = new ResModelDao(mContext);
        dictionaryDao = new DictionaryDao(mContext);

        showRes = resModelDao.getDatById(selectBean.getId());

        initDatas(selectBean);

    }

    private void initDatas(final ResModelBean selectBean) {

        //先判断有无网络
        int netWorkStart = Utils.getNetWorkStart(mContext);


        if (Utils.NETWORK_NONE == netWorkStart) {
            //无网络
            showLocalData( selectBean);

        } else {
            //有网络
            new GetResModelVersionAPI(userInfo.getAccess_token(), selectBean.getId(), new GetResModelVersionAPI.GetResModelVersionIF() {
                @Override
                public void onGetResModelVersionResult(boolean isOk, int version) {

                    HashMap<String, List<DictionaryBean>> stringListHashMap = getlocalDicItems(resFromBeanLsit);

                    if ((isOk && (version > showRes.getfVersion()))||(stringListHashMap==null||stringListHashMap.size()==0)) {//有更新获取网络数据
                        // 获取表单数据

                        new GetResModelAPI(userInfo.getAccess_token(), showRes.getId(), new GetResModelAPI.GetResModelIF() {
                            @Override
                            public void ongetResModelIFResult(boolean isOk, ResModelBean resModelBean) {

                                if (isOk) {//获取到网络数据

                                    resModelDao.addOrUpdate(resModelBean);

                                    String getfJsonData = resModelBean.getfJsonData();
                                    Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                                    formBeanList = mGson.fromJson(getfJsonData, new TypeToken<List<DongTaiFormBean>>() {
                                    }.getType());


                                    String showType = new String();

                                    List<String> showTypelist = new ArrayList<>();


                                    for (int i = 0; i < formBeanList.size(); i++) {

                                        DongTaiFormBean dongTaiFormBean = formBeanList.get(i);

                                        if (dongTaiFormBean.getShowType().equals("list")) {

                                            if (!Utils.isEmpty(dongTaiFormBean.getDictText())) {
                                                if (!showTypelist.contains(dongTaiFormBean.getDictText())) {
                                                    showTypelist.add(dongTaiFormBean.getDictText());
                                                    if (showTypelist.size()==1) {
                                                        showType = dongTaiFormBean.getDictText();
                                                    } else {
                                                        showType = showType + "," + dongTaiFormBean.getDictText();
                                                    }
                                                }


                                            }


                                        }

                                    }
                                    cocalmap.clear();

                                    //获取字典数据
                                    new GetDictListDataAPI(userInfo.getAccess_token(), showType, showTypelist, new GetDictListDataAPI.GetDictListDataIF() {
                                        @Override
                                        public void getResDataList(boolean isOk, HashMap<String, List<DictionaryBean>> map) {

                                            if (isOk) {
                                                cocalmap.putAll(map);

                                                if (resFromBeanLsit != null && resFromBeanLsit.size() > 0) {
                                                    formBeanList.clear();
                                                    formBeanList.addAll(resFromBeanLsit);


                                                }

                                                biaoGeRecyclerAdapter = new BiaoGeRecyclerAdapter2(mContext, formBeanList,false);

                                                recy_view.setAdapter(biaoGeRecyclerAdapter);

                                                recy_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                    @Override
                                                    public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                                                        super.onScrollStateChanged(recyclerView, newState);
                                                        if (mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                                                            mShouldScroll = false;
                                                            smoothMoveToPosition(recy_view, mToPosition);
                                                        }
                                                    }
                                                });

                                                UpdateData();
                                                biaoGeRecyclerAdapter.setMap(map);

                                            }

                                        }
                                    }).request();


                                } else {
                                    showLocalData(selectBean);
                                }


                            }
                        }).request();


                    } else {
                        showLocalData(selectBean);
                    }






                }
            }).request();


        }


    }

    /***
     * 显示本地的数据
     */

    private void showLocalData(ResModelBean selectBean) {

        //获取本地表单

        showRes = resModelDao.getDatById(selectBean.getId());
        String getfJsonData = showRes.getfJsonData();
        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
        formBeanList = mGson.fromJson(getfJsonData, new TypeToken<List<DongTaiFormBean>>() {
        }.getType());

        if (resFromBeanLsit != null && resFromBeanLsit.size() != 0) {
            formBeanList.clear();
            formBeanList.addAll(resFromBeanLsit);
        }

        //获取本地字典
        for (DongTaiFormBean dongTaiFormBean : formBeanList) {

            if (dongTaiFormBean.getShowType().equals("list")) {

                if (!Utils.isEmpty(dongTaiFormBean.getDictText())) {

                    List<DictionaryBean> dictionaryBeanList = dictionaryDao.getDataType(dongTaiFormBean.getDictText());

                    cocalmap.put(dongTaiFormBean.getDictText(), dictionaryBeanList);

                }


            }

        }


        biaoGeRecyclerAdapter = new BiaoGeRecyclerAdapter2(mContext, formBeanList,false);
        recy_view.setAdapter(biaoGeRecyclerAdapter);
        recy_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                    mShouldScroll = false;
                    smoothMoveToPosition(recy_view, mToPosition);
                }
            }
        });

        biaoGeRecyclerAdapter.setMap(cocalmap);



    }


    /***
     * 获取本地字典数据
     *
     */


    private HashMap<String, List<DictionaryBean>> getlocalDicItems(List<DongTaiFormBean> formBeans) {

        HashMap<String, List<DictionaryBean>> map = new HashMap<>();

        //获取本地字典
        for (DongTaiFormBean dongTaiFormBean : formBeans) {

            if (dongTaiFormBean.getShowType().equals("list")) {

                if (!Utils.isEmpty(dongTaiFormBean.getDictText())) {

                    List<DictionaryBean> dictionaryBeanList = dictionaryDao.getDataType(dongTaiFormBean.getDictText());

                    if (dictionaryBeanList != null && dictionaryBeanList.size() > 0) {

                        map.put(dongTaiFormBean.getDictText(), dictionaryBeanList);
                    }

                }


            }

        }


        return map;
    }


    /***
     * 获取网络的字典数据
     */

    private void getNetDicItems() {

        String showType = new String();

        List<String> showTypelist = new ArrayList<>();


        for (int i = 0; i < formBeanList.size(); i++) {

            DongTaiFormBean dongTaiFormBean = formBeanList.get(i);

            if (dongTaiFormBean.getShowType().equals("list")) {

                if (!Utils.isEmpty(dongTaiFormBean.getDictText())) {
                    if (!showTypelist.contains(dongTaiFormBean.getDictText())) {
                        showTypelist.add(dongTaiFormBean.getDictText());
                        if (showTypelist.size()==1) {
                            showType = dongTaiFormBean.getDictText();
                        } else {
                            showType = showType + "," + dongTaiFormBean.getDictText();
                        }
                    }


                }


            }

        }
        cocalmap.clear();


        //获取字典数据
        new GetDictListDataAPI(userInfo.getAccess_token(), showType, showTypelist, new GetDictListDataAPI.GetDictListDataIF() {
            @Override
            public void getResDataList(boolean isOk, HashMap<String, List<DictionaryBean>> map) {

                if (isOk) {
                    map.putAll(map);

                    DictionaryDao dictionaryDao = new DictionaryDao(mContext);

                    for (List<DictionaryBean> value : map.values()) {
                        dictionaryDao.addOrUpdate(value);
                    }


                    if (resFromBeanLsit != null && resFromBeanLsit.size() > 0) {
                        formBeanList.clear();
                        formBeanList.addAll(resFromBeanLsit);


                    }

                    biaoGeRecyclerAdapter = new BiaoGeRecyclerAdapter2(mContext, formBeanList,false);

                    recy_view.setAdapter(biaoGeRecyclerAdapter);

                    recy_view.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                            super.onScrollStateChanged(recyclerView, newState);
                            if (mShouldScroll && RecyclerView.SCROLL_STATE_IDLE == newState) {
                                mShouldScroll = false;
                                smoothMoveToPosition(recy_view, mToPosition);
                            }
                        }
                    });

                    UpdateData();
                    biaoGeRecyclerAdapter.setMap(map);

                }

            }
        }).request();

    }



    /****
     * 更新本地的数据，然后在展示数据；
     */

    private void UpdateData() {
        Set<String>   keys = cocalmap.keySet();
        for(String key :keys){

            List<DictionaryBean> dataType = dictionaryDao.getDataType(key);

            if (dataType != null && dataType.size() > 0) {

                dictionaryDao.deletByType(key);
            }

        }

        for (List<DictionaryBean> value : cocalmap.values()) {

            dictionaryDao.addOrUpdate(value);

        }











    }


    /**
     * 滑动到指定位置
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        // 第一个可见位置
        int firstItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        int lastItem = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));
        if (position < firstItem) {
            // 第一种可能:跳转位置在第一个可见位置之前
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItem) {
            // 第二种可能:跳转位置在第一个可见位置之后
            int movePosition = position - firstItem;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_shure:

               labe: for (int i = 0; i < formBeanList.size(); i++) {
                    DongTaiFormBean dongTaiFormBean = formBeanList.get(i);


                    List<DongTaiFormBean> propertys = dongTaiFormBean.getCgformFieldList();


                    List<cgformRuleBean> cgformRuleList = dongTaiFormBean.getCgformRuleList();

                    if (cgformRuleList != null && cgformRuleList.size() > 0) {
                        cgformRuleBean cgformRuleBean = cgformRuleList.get(0);
                        String par = cgformRuleBean.getPattern();
                        Pattern p = Pattern.compile(par);
                        if (!Utils.isEmpty(dongTaiFormBean.getPropertyLabel())) {
                            Matcher m = p.matcher(dongTaiFormBean.getPropertyLabel());
                            if (!m.matches()) {
                                smoothMoveToPosition(recy_view, dongTaiFormBean.getPosition());
                                dongTaiFormBean.setPropertyLabel("");
                                break;
                            }

                        }
                    }

                    if (dongTaiFormBean.getFieldMustInput()) {

                        if (Utils.isEmpty(dongTaiFormBean.getPropertyLabel())) {

                            dongTaiFormBean.setShowTiShi(true);
                            smoothMoveToPosition(recy_view, dongTaiFormBean.getPosition());
                            break;

                        } else {
                            dongTaiFormBean.setShowTiShi(false);
                        }

                    }


                    if (propertys != null && propertys.size() > 0) {


                        if (propertys != null && (!Utils.isEmpty(dongTaiFormBean.getPropertyLabel()) && dongTaiFormBean.getPropertyLabel().equals("是"))) {

                            for (int j = 0; j < propertys.size(); j++) {


                                DongTaiFormBean dongTaiFormBean1 = propertys.get(j);

                                List<cgformRuleBean> cgformRuleList2 = dongTaiFormBean1.getCgformRuleList();

                                if (cgformRuleList2 != null && cgformRuleList2.size() > 0) {
                                    cgformRuleBean cgformRuleBean = cgformRuleList2.get(0);
                                    String par = cgformRuleBean.getPattern();
                                    Pattern p = Pattern.compile(par);
                                    if (!Utils.isEmpty(dongTaiFormBean1.getPropertyLabel())) {
                                        Matcher m = p.matcher(dongTaiFormBean1.getPropertyLabel());
                                        if (!m.matches()) {
                                            smoothMoveToPosition(recy_view, dongTaiFormBean1.getPosition());
                                            dongTaiFormBean1.setPropertyLabel("");
                                            break labe;
                                        }

                                    }
                                }

                                if (dongTaiFormBean1.getFieldMustInput()) {

                                    if (Utils.isEmpty(dongTaiFormBean1.getPropertyLabel())) {

                                        dongTaiFormBean1.setShowTiShi(true);
                                        smoothMoveToPosition(recy_view, dongTaiFormBean1.getPosition());
                                        break labe;

                                    } else {
                                        dongTaiFormBean1.setShowTiShi(false);
                                    }

                                }


                            }
                        }




                    } else {


                    if (cgformRuleList != null && cgformRuleList.size() > 0) {
                        cgformRuleBean cgformRuleBean = cgformRuleList.get(0);
                        String par = cgformRuleBean.getPattern();
                        Pattern p = Pattern.compile(par);
                        if (!Utils.isEmpty(dongTaiFormBean.getPropertyLabel())) {
                            Matcher m = p.matcher(dongTaiFormBean.getPropertyLabel());
                            if (!m.matches()) {
                                smoothMoveToPosition(recy_view,dongTaiFormBean.getPosition());
                                dongTaiFormBean.setPropertyLabel("");
                                break;
                            }

                        }
                    }

                    if (dongTaiFormBean.getFieldMustInput()) {

                        if (Utils.isEmpty(dongTaiFormBean.getPropertyLabel())) {

                            dongTaiFormBean.setShowTiShi(true);
                            smoothMoveToPosition(recy_view, dongTaiFormBean.getPosition());
                            break;

                        } else {
                            dongTaiFormBean.setShowTiShi(false);
                        }

                    }


                    }


                }
                biaoGeRecyclerAdapter.notifyDataSetChanged();

                boolean allEdit = false;


                for (DongTaiFormBean dongTaiFormBean : formBeanList) {

                    if (dongTaiFormBean.getFieldMustInput()) {

                        if (Utils.isEmpty(dongTaiFormBean.getPropertyLabel())) {
                            allEdit = false;
                            break;

                        }

                    }


                    List<DongTaiFormBean> propertys = dongTaiFormBean.getCgformFieldList();

                    if (propertys != null && propertys.size() > 0) {

                        for (DongTaiFormBean dongTaiFormBean1 : propertys) {

                            if (dongTaiFormBean1.getFieldMustInput()) {

                                if (Utils.isEmpty(dongTaiFormBean1.getPropertyLabel())) {
                                    allEdit=false;
                                    break;

                                }


                            }

                        }


                    }

                    allEdit = true;
                }
                if (allEdit) {
                    Intent intent = new Intent();
                    resFromBeanLsit.clear();
                    resFromBeanLsit.addAll(formBeanList);
                    intent.putExtra("formBeanList", (Serializable) resFromBeanLsit);

                    setResult(4, intent);

                    this.finish();

                }

                break;


            case R.id.ib_back:

//                Intent intent2 = new Intent();
//                resFromBeanLsit.clear();
//                resFromBeanLsit.addAll(formBeanList);
//                intent2.putExtra("formBeanList", (Serializable) resFromBeanLsit);
//
//                setResult(4, intent2);

                this.finish();
                break;

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


    private HashMap<String, List<DictionaryBean>> cocalmap = new HashMap<>();


}
