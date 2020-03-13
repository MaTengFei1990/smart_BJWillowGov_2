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
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import okhttp3.Call;

/**
 * Created by Lenovo on 2019/4/18.
 */

public class SearchGridsListPageAPI implements INetModel {


    private int page=0;
    private String  key;
    private UserInfo userInfo;
    private String id;
    private SearchDataIF searchDataIF;

    public SearchGridsListPageAPI(int page,String key, UserInfo userInfo,String id, SearchDataIF searchDataIF) {
        this.key = key;
        this.page = page;
        this.userInfo = userInfo;
        this.id = id;
        this.searchDataIF = searchDataIF;
    }

    @Override
    public void request() {

        String urlStr = Values.SERVICE_URL + "api/blocks/list";
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization", userInfo.getAccess_token())
                .addParams("officeid", userInfo.getOffice().getId())
                .addParams("key",key )
                .addParams("taskid", id)
                .addParams("page", page+"")
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (searchDataIF != null) {
                    searchDataIF.searchDatadicListResult(false, null,0);
                }

            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("searchBloks......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<BlockAndStatusBean> menuBeanList = mGson.fromJson(object.getString("data"),
                                new TypeToken<List<BlockAndStatusBean>>() {
                                }.getType());

                        if (searchDataIF != null) {
                            int count = object.getInt("count");

                            searchDataIF.searchDatadicListResult(true, menuBeanList, count);
                        }

                    } else {
                        if (searchDataIF != null) {

                            searchDataIF.searchDatadicListResult(false, null,0);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (searchDataIF != null) {

                        searchDataIF.searchDatadicListResult(false, null,0);
                    }

                }
            }
        });
    }

    public interface SearchDataIF {
        void searchDatadicListResult(boolean isOk, List<BlockAndStatusBean> menuBeanList, int count);
    }
}
