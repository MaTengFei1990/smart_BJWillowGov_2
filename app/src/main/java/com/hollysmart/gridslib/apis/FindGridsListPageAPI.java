package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Lenovo on 2019/4/18.
 */

public class FindGridsListPageAPI implements INetModel {


    private UserInfo userInfo;
    private DatadicListIF datadicListIF;
    private int pageNo;
    private int  pageSize;

    public FindGridsListPageAPI(UserInfo userInfo, DatadicListIF datadicListIF) {
        this.pageNo = 1;
        this.userInfo = userInfo;
        this.datadicListIF = datadicListIF;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL_PC + "/xdsapi/api/blocks/list";
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("officeid", userInfo.getOffice().getId())
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (datadicListIF != null) {
                    datadicListIF.datadicListResult(false, null);
                }

            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("findListPage......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<GridBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<GridBean>>() {
                                }.getType());

                        for (GridBean resDataBean : menuBeanList) {
//                            resDataBean.setFdTaskId(projectBean.getId());
//                            resDataBean.setFd_resmodelid(resmodelid);
                        }

                        if (datadicListIF != null) {

                            datadicListIF.datadicListResult(true, menuBeanList);
                        }

                    } else {
                        if (datadicListIF != null) {

                            datadicListIF.datadicListResult(false, null);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (datadicListIF != null) {

                        datadicListIF.datadicListResult(false, null);
                    }

                }
            }
        });
    }

    public interface DatadicListIF {
        void datadicListResult(boolean isOk, List<GridBean> menuBeanList);
    }
    public interface DatadicListCountIF {
        void datadicListResult(boolean isOk, List<GridBean> menuBeanList, int allCount);
    }
}
