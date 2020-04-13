package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.builder.GetBuilder;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Lenovo on 2019/4/18.
 */

public class BlocksNearAPI implements INetModel {


    private int page=0;
    private UserInfo userInfo;
    private  String id;
    Map<String, String> map;
    private boolean ischek;
    private String lat;
    private String lng;
    private NearBlockListIF nearBlockListIF;

    public BlocksNearAPI(boolean ischek,  Map<String, String> map,int page, UserInfo userInfo, String id,String lat,String lng, NearBlockListIF nearBlockListIF) {
        this.page = page;
        this.userInfo = userInfo;
        this.ischek = ischek;
        this.id = id;
        this.map = map;
        this.lat = lat;
        this.lng = lng;
        this.nearBlockListIF = nearBlockListIF;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL + "api/blocks/near";
        GetBuilder getBuilder = OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("taskid", id)
                .addParams("lat",lat)
                .addParams("lng",lng);
        if (ischek) {
            getBuilder.addParams("officeid", map.get("unitid"));

        } else {
            getBuilder.addParams("officeid", userInfo.getOffice().getId());
        }
        getBuilder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (nearBlockListIF != null) {
                    nearBlockListIF.datadicListResult(false, null);
                }

            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("nearblocks......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<BlockAndStatusBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<BlockAndStatusBean>>() {
                                }.getType());

                        if (nearBlockListIF != null) {

                            nearBlockListIF.datadicListResult(true, menuBeanList);
                        }

                    } else {
                        if (nearBlockListIF != null) {

                            nearBlockListIF.datadicListResult(false, null);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (nearBlockListIF != null) {

                        nearBlockListIF.datadicListResult(false, null);
                    }

                }
            }
        });
    }

    public interface NearBlockListIF {
        void datadicListResult(boolean isOk, List<BlockAndStatusBean> nearByBeanList);
    }
}
