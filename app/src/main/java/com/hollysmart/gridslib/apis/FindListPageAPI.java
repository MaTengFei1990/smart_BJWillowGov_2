package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
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

public class FindListPageAPI implements INetModel {


    private UserInfo userInfo;
    private ProjectBean projectBean;
    private DatadicListIF datadicListIF;
    private DatadicListCountIF datadicListCountIF;
    private String resmodelid;
    private String fd_parentid;
    private int pageNo;
    private int  pageSize;

    public FindListPageAPI(UserInfo userInfo, String resmodelid, ProjectBean projectBean,String fd_parentid, DatadicListIF datadicListIF) {
        this.pageNo = 1;
        if (Utils.isEmpty(fd_parentid)) {
            this.pageSize = 1000;
        }else {
            this.pageSize = 20;
        }
        this.userInfo = userInfo;
        this.resmodelid = resmodelid;
        this.projectBean = projectBean;
        this.fd_parentid = fd_parentid;
        this.datadicListIF = datadicListIF;
    }
    public FindListPageAPI(int pageSize,UserInfo userInfo, String resmodelid, ProjectBean projectBean,String fd_parentid, DatadicListIF datadicListIF) {
        this.pageNo = 1;
        this.pageSize = pageSize;
        this.userInfo = userInfo;
        this.resmodelid = resmodelid;
        this.projectBean = projectBean;
        this.fd_parentid = fd_parentid;
        this.datadicListIF = datadicListIF;
    }
    public FindListPageAPI(int pageNo ,int pageSize,UserInfo userInfo, String resmodelid, ProjectBean projectBean,String fd_parentid, DatadicListCountIF datadicListCountIF) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.userInfo = userInfo;
        this.resmodelid = resmodelid;
        this.projectBean = projectBean;
        this.fd_parentid = fd_parentid;
        this.datadicListCountIF = datadicListCountIF;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {
            object.put("pageNo", pageNo+"");
            object.put("pageSize", pageSize+"");
            object.put("fd_restaskid",projectBean.getId()) ;
            object.put("fd_resmodelid", resmodelid);
            object.put("fd_sort", "-1");

            if (!Utils.isEmpty(fd_parentid)) {
                object.put("fd_parentid", fd_parentid);

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String urlStr = Values.SERVICE_URL_FORM + "/admin/api/resdata/findListPage";
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", userInfo.getAccess_token())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                if (datadicListIF != null) {
                    datadicListIF.datadicListResult(false, null);
                }

                if (datadicListCountIF != null) {
                    datadicListCountIF.datadicListResult(false,null,0);
                }
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("findListPage......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 200) {

                        JSONObject jsData = object.getJSONObject("data");
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<ResDataBean> menuBeanList = mGson.fromJson(jsData.getString("list"),
                                new TypeToken<List<ResDataBean>>() {}.getType());

                        for (ResDataBean resDataBean : menuBeanList) {
                            resDataBean.setFdTaskId(projectBean.getId());
                            resDataBean.setFd_resmodelid(resmodelid);
                        }

                        if (datadicListIF != null) {

                            datadicListIF.datadicListResult(true, menuBeanList);
                        }

                        if (datadicListCountIF != null) {
                            int allcount = jsData.getInt("count");
                            datadicListCountIF.datadicListResult(true,menuBeanList,allcount);
                        }
                    } else {
                        if (datadicListIF != null) {

                            datadicListIF.datadicListResult(false, null);
                        }

                        if (datadicListCountIF != null) {
                            datadicListCountIF.datadicListResult(false,null,0);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    if (datadicListIF != null) {

                        datadicListIF.datadicListResult(false, null);
                    }

                    if (datadicListCountIF != null) {
                        datadicListCountIF.datadicListResult(false,null,0);
                    }
                }
            }
        });
    }

    public interface DatadicListIF {
        void datadicListResult(boolean isOk, List<ResDataBean> menuBeanList);
    }
    public interface DatadicListCountIF {
        void datadicListResult(boolean isOk, List<ResDataBean> menuBeanList,int allCount);
    }
}
