package com.hollysmart.formlib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

/**
 * 采集任务数据list
 * Created by Lenovo on 2019/4/17.
 */

public class GetHisTreeListAPI implements INetModel {
    private GetHisTreeLsitIF getHisTreeLsitIF;
    private String token;

    public GetHisTreeListAPI(String token, GetHisTreeLsitIF getHisTreeLsitIF) {
        this.token = token;
        this.getHisTreeLsitIF = getHisTreeLsitIF;
    }

    @Override
    public void request() {
        String urlStr = Values.SERVICE_URL + "api/treehisdata/listMap";
        Mlog.d("getHisTreeListAPI----urlStr" + urlStr);
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
                Mlog.d("getHisTreeListAPI====:" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");
                    if (status == 1) {
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        JSONArray dataOBJ = new JSONArray(jsonObject.getString("data"));
                        List<String> ListDatas = mGson.fromJson(dataOBJ.toString(),
                                new TypeToken<List<String>>() {
                                }.getType());
                        getHisTreeLsitIF.onResTaskListResult(true, ListDatas, null);
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
        void onResTaskListResult(boolean isOk, List<String> ListDatas, String msg);
    }

}
