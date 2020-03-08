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


    private int page=0;
    private UserInfo userInfo;
    private DatadicListIF datadicListIF;

    public FindGridsListPageAPI(int page,UserInfo userInfo, DatadicListIF datadicListIF) {
        this.page = page;
        this.userInfo = userInfo;
        this.datadicListIF = datadicListIF;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL_PC + "/xdsapi/api/blocks/list";
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("officeid", userInfo.getOffice().getId())
                .addParams("page", page+"")
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
                        List<GridBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<GridBean>>() {
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
        void datadicListResult(boolean isOk, List<GridBean> menuBeanList,int count);
    }
}
