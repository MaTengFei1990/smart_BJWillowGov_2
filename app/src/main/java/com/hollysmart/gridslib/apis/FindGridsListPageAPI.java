package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.umeng.commonsdk.statistics.common.MLog;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

/**
 * Created by Lenovo on 2019/4/18.
 */

public class FindGridsListPageAPI implements INetModel {


    private int page=0;
    private UserInfo userInfo;
    private  String id;
    private DatadicListIF datadicListIF;

    public FindGridsListPageAPI(int page, UserInfo userInfo, String id, DatadicListIF datadicListIF) {
        this.page = page;
        this.userInfo = userInfo;
        this.id = id;
        this.datadicListIF = datadicListIF;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL + "api/blocks/list";
        MLog.d("网格列表-----" + urlStr);
        MLog.d("officeid---" + userInfo.getOffice().getId());
        MLog.d("taskid--" + id);
        MLog.d("page---" + page + "");
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("officeid", userInfo.getOffice().getId())
                .addParams("page", page+"")
                .addParams("taskid", id)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (datadicListIF != null) {
                    datadicListIF.datadicListResult(false, null,0);
                }

            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("findblocks......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<BlockAndStatusBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<BlockAndStatusBean>>() {
                                }.getType());

                        if (datadicListIF != null) {
                            int count = object.getInt("count");

                            datadicListIF.datadicListResult(true, menuBeanList, count);
                        }

                    } else {
                        if (datadicListIF != null) {

                            datadicListIF.datadicListResult(false, null,0);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (datadicListIF != null) {

                        datadicListIF.datadicListResult(false, null,0);
                    }

                }
            }
        });
    }

    public interface DatadicListIF {
        void datadicListResult(boolean isOk, List<BlockAndStatusBean> menuBeanList, int count);
    }
}
