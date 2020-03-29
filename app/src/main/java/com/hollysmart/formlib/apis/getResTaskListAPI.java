package com.hollysmart.formlib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.UserToken;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 *  采集任务数据list
 * Created by Lenovo on 2019/4/17.
 */

public class getResTaskListAPI implements INetModel {



    private ResTaskListIF resTaskListIF;
    private int pageNo;
    private String  token;
    private String fdTaskId;
    private String officeId;

    public getResTaskListAPI(String token,String fdTaskId,String officeId,int pageNo, ResTaskListIF resTaskListIF) {
        this.pageNo = pageNo;
        this.token = token;
        this.fdTaskId = fdTaskId;
        this.officeId = officeId;
        this.resTaskListIF = resTaskListIF;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {
            object.put("fdTaskId", fdTaskId);
            object.put("fOfficeId", officeId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Mlog.d("getResTaskListAPI----obj" + object.toString());
        String urlStr = Values.SERVICE_URL_FORM + "/admin/api/restask/get";
        Mlog.d("getResTaskListAPI----urlStr" + urlStr);
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", token)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                resTaskListIF.onResTaskListResult(false, null,null);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("项目管理列表:" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");


                    if ( status == 200){
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        JSONObject dataOBJ = new JSONObject(jsonObject.getString("data"));
                        ProjectBean projectBean = mGson.fromJson(dataOBJ.toString(),
                                new TypeToken<ProjectBean>() {}.getType());
                            projectBean.setUserinfoid(UserToken.getUserToken().getToken());

                        resTaskListIF.onResTaskListResult(true, projectBean,null);
                    }else {
                        String msg = jsonObject.getString("msg");

                        resTaskListIF.onResTaskListResult(false, null,msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    resTaskListIF.onResTaskListResult(false, null,null);
                }

            }
        });

    }

    public interface ResTaskListIF{
        void onResTaskListResult(boolean isOk, ProjectBean projectBeanList,String msg);
    }

}
