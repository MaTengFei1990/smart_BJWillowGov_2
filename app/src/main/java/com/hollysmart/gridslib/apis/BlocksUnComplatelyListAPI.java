package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
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

/***
 * 未完成采集的网格列表
 */

public class BlocksUnComplatelyListAPI implements INetModel {


    private BlockBean blockBean;
    private BLockListcompletelyIF bLockListcompletelyIF;
    private int page=0;
    private UserInfo userInfo;
    private  String id;
    Map<String, String> map;
    private boolean ischek;
    int total;

    public BlocksUnComplatelyListAPI(boolean ischek, Map<String, String> map, int page, UserInfo userInfo, String id, BLockListcompletelyIF bLockListcompletelyIF) {
        this.page = page;
        this.userInfo = userInfo;
        this.ischek = ischek;
        this.id = id;
        this.map = map;
        this.bLockListcompletelyIF = bLockListcompletelyIF;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL + "api/blocks/listuncompletely";
        GetBuilder getBuilder = OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("page", page+"")
                .addParams("taskid", id);
        if (ischek) {
            getBuilder.addParams("officeid", map.get("unitid"));

        } else {
            getBuilder.addParams("officeid", userInfo.getOffice().getId());
        }
        getBuilder.build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (bLockListcompletelyIF != null) {
                    bLockListcompletelyIF.datadicListResult(false, null,0,0,0);
                }

            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("未完成采集的网格列表......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<BlockAndStatusBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<BlockAndStatusBean>>() {
                                }.getType());

                        if (bLockListcompletelyIF != null) {
                            int count = object.getInt("pages");
                            Mlog.d("BlocksUnComplatelyListAPI--pages---" + count);

                            bLockListcompletelyIF.datadicListResult(true, menuBeanList, count,menuBeanList.size(),total);
                        }

                    } else {
                        if (bLockListcompletelyIF != null) {

                            bLockListcompletelyIF.datadicListResult(false, null,0,0,0);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (bLockListcompletelyIF != null) {

                        bLockListcompletelyIF.datadicListResult(false, null,0,0,0);
                    }

                }
            }
        });
    }

    public interface BLockListcompletelyIF {
        void datadicListResult(boolean isOk, List<BlockAndStatusBean> menuBeanList, int count, int okCount, int total);
    }
}
