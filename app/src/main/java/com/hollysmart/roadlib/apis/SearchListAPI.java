package com.hollysmart.roadlib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

public class SearchListAPI implements INetModel {



    private UserInfo userInfo;
    private DataSearchListIF dataSearchList;
    private String  type; // 1 tree   2 road
    private String  searchContent;
    private ResDataBean  resDataBean;


    public SearchListAPI(UserInfo userInfo,String type,String searchContent,ResDataBean resDataBean, DataSearchListIF dataSearchList) {
        this.userInfo = userInfo;
        this.type = type;
        this.resDataBean = resDataBean;
        this.searchContent = searchContent;
        this.dataSearchList = dataSearchList;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {

            if ("1".equals(type)) {
                object.put("fdTreeNumber",searchContent );

            }else {
                object.put("fdRoadName",searchContent );
            }

            object.put("fd_restaskid", resDataBean.getFdTaskId()) ;
            object.put("fd_resmodelid", resDataBean.getFd_resmodelid());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String urlStr = Values.SERVICE_URL_FORM + "/admin/api/resdata/searchList";
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", userInfo.getAccess_token())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                dataSearchList.dataSearchList(false, null);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("getSearchList......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 200) {

                        JSONObject jsData = object.getJSONObject("data");

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<ResDataBean> menuBeanList = mGson.fromJson(jsData.getString("list"),
                                new TypeToken<List<ResDataBean>>() {}.getType());


                        for (ResDataBean DataBean : menuBeanList) {
                            DataBean.setFdTaskId(resDataBean.getFdTaskId());
                            DataBean.setFd_resmodelid(resDataBean.getFd_resmodelid());
                            DataBean.setFd_resmodelname(resDataBean.getFd_resmodelname());
                        }



                        dataSearchList.dataSearchList(true, menuBeanList);
                    } else {
                        dataSearchList.dataSearchList(false, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    dataSearchList.dataSearchList(false, null);
                }
            }
        });
    }

    public interface DataSearchListIF {
        void dataSearchList(boolean isOk, List<ResDataBean> menuBeanList);
    }
}
