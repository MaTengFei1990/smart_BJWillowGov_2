package com.hollysmart.roadlib.apis;

import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.utils.taskpool.OnNetRequestListener;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.MediaType;

/**
 * Created by Lenovo on 2019/4/18.
 */

public class GetNetTreeListCountAPI implements INetModel {


    private UserInfo userInfo;
    private ResDataBean roadBean;
    private OnNetRequestListener onNetRequestListener;
    private String  parentId;
    private String  resmodelid;


    public GetNetTreeListCountAPI(UserInfo userInfo, String resmodelid, ResDataBean roadBean, OnNetRequestListener onNetRequestListener) {this.userInfo = userInfo;
        this.roadBean = roadBean;
        this.onNetRequestListener = onNetRequestListener;
        this.parentId = roadBean.getId();
        this.resmodelid = resmodelid;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {
            object.put("pageNo", "1");
            object.put("pageSize", "1000");
            object.put("fd_restaskid", roadBean.getFdTaskId()) ;
            object.put("fd_resmodelid", resmodelid);

            if (!Utils.isEmpty(parentId)) {

                object.put("fd_parentid", parentId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String urlStr = Values.SERVICE_URL_FORM + "/admin/api/resdata/datadicList";
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", userInfo.getAccess_token())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                onNetRequestListener.OnNext();
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("GetNetTreeList......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 200) {

                        JSONObject jsData = object.getJSONObject("data");

                        JSONArray list = jsData.getJSONArray("list");
                        List<ResDataBean> menuBeanList = new ArrayList<>();

                        for (int i = 0; i < list.length(); i++) {
                            JSONObject objDataBen = (JSONObject) list.get(i);
                            ResDataBean resDataBean = new ResDataBean();
                            resDataBean.setId(objDataBen.getString("fd_resid"));
                            resDataBean.setFd_resmodelid(objDataBen.getString("fd_resmodelid"));
                            resDataBean.setFd_resmodelname(objDataBen.getString("fd_resmodelname"));
                            resDataBean.setFdTaskId(objDataBen.getString("fd_restaskid"));
                            resDataBean.setFd_restaskname(objDataBen.getString("fd_restaskname"));
                            resDataBean.setFd_resdate(objDataBen.getString("fd_resdate"));
                            resDataBean.setRescode(objDataBen.getString("fd_rescode"));
                            resDataBean.setFd_resposition(objDataBen.getString("fd_resposition"));
                            resDataBean.setFd_resname(objDataBen.getString("fd_resname"));
                            resDataBean.setFd_parentid(objDataBen.getString("fd_parentid"));

                            menuBeanList.add(resDataBean);
                        }
                        roadBean.setChildTreeCount(menuBeanList.size());

                        onNetRequestListener.OnNext();
                    } else {
                        onNetRequestListener.OnNext();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    onNetRequestListener.OnNext();
                }
            }
        });
    }

    public interface DatadicListIF {
        void datadicListResult(boolean isOk, List<ResDataBean> menuBeanList);
    }
}
