package com.hollysmart.formlib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.formlib.beans.HisTreeInfo;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * 采集任务数据list
 * Created by Lenovo on 2019/4/17.
 */

public class GetHisTreeInfoAPI implements INetModel {
    private GetHisTreeLsitIF getHisTreeLsitIF;
    private String token;
    private String id;

    public GetHisTreeInfoAPI(String token, String id, GetHisTreeLsitIF getHisTreeLsitIF) {
        this.token = token;
        this.id = id;
        this.getHisTreeLsitIF = getHisTreeLsitIF;
    }

    @Override
    public void request() {
        String urlStr = Values.SERVICE_URL + "api/treehisdata/infoById?id=" + id;
        Mlog.d("GetHisTreeInfoAPI----urlStr" + urlStr);
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", token)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                getHisTreeLsitIF.onResTaskListResult(false, null, null);
            }

            @Override
            public void onResponse(String response, int id) {

//                {"status":1,"data":{"id":60391,"fdTreeCode":"3649-011-001","fdLng":116.11784065798115,"fdLat":39.95210611853332,"fdTreeType":"杨树","fdTreeState":"好","fdTreeCount":1}}

                Mlog.d("GetHisTreeInfoAPI====:" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        JSONObject dataOBJ = new JSONObject(jsonObject.getString("data"));
                        HisTreeInfo hisTreeInfo = mGson.fromJson(dataOBJ.toString(),
                                new TypeToken<HisTreeInfo>() {
                                }.getType());
                        getHisTreeLsitIF.onResTaskListResult(true, hisTreeInfo, null);
                    } else {
                        String msg = jsonObject.getString("msg");

                        getHisTreeLsitIF.onResTaskListResult(false, null, msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    getHisTreeLsitIF.onResTaskListResult(false, null, null);
                }

            }
        });

    }

    public interface GetHisTreeLsitIF {
        void onResTaskListResult(boolean isOk, HisTreeInfo hisTreeInfo, String msg);
    }

}
