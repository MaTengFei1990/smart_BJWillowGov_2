package com.hollysmart.formlib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.FormModelBean;
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
 * 查询单个资源采集数据
 * Created by Lenovo on 2019/4/18.
 */

public class ResDataGetAPI implements INetModel {


    private String access_token;

    private ResDataBean resDataBean;
    private ResDataDeleteIF resDataDeleteIF;

    private String fd_resmodelid;

    public ResDataGetAPI(String access_token, ResDataBean resDataBean, ResDataDeleteIF resDataDeleteIF) {
        this.resDataBean = resDataBean;
        this.access_token = access_token;
        fd_resmodelid = resDataBean.getFd_resmodelid();
        this.resDataDeleteIF = resDataDeleteIF;
    }

    @Override
    public void request() {


        JSONObject object = new JSONObject();

        try {
            object.put("id", resDataBean.getId());
            object.put("fd_restaskid", resDataBean.getFdTaskId());
            object.put("fd_resmodelid", resDataBean.getFd_resmodelid());
            object.put("fd_resmodelname", resDataBean.getFd_resmodelname());
        } catch (JSONException e) {
            e.printStackTrace();
        }


        Mlog.d("查询单个资源采集数据---object===" + object.toString());

        String urlStr = Values.SERVICE_URL_FORM + "/admin/api/resdata/get";
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", access_token)
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                resDataDeleteIF.onResDataDeleteResult(false, null);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("查询单个资源采集数据返回的数据:" + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    int status = jsonObject.getInt("status");


                    if ( status == 200){
                        if (Utils.isEmpty(jsonObject.getString("data"))) {
                            resDataDeleteIF.onResDataDeleteResult(false, null);
                            return;
                        }

                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        ResDataBean redata = mGson.fromJson(jsonObject.getString("data"),
                                new TypeToken<ResDataBean>() {}.getType());

                       resDataBean=redata;
                        resDataBean.setFd_resmodelid(fd_resmodelid);

                        FormModelBean formModel = resDataBean.getFormModel();


                        getwgps2bd(formModel);


                        String s = mGson.toJson(formModel);
                        resDataBean.setFormData(s);



                        resDataDeleteIF.onResDataDeleteResult(true, resDataBean);
                    }else {
                        resDataDeleteIF.onResDataDeleteResult(false, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    private void getwgps2bd(FormModelBean formModel) {
        List<DongTaiFormBean> formBeanList = formModel.getCgformFieldList();
        for (int i = 0; i < formBeanList.size(); i++) {

            DongTaiFormBean formBean = formBeanList.get(i);

            if (formBean.getJavaField().equals("location")) {

                String propertyLabel = formBean.getPropertyLabel();

                String[] gpsGroups = propertyLabel.split("\\|");

                String strplanes = "";

                for (int j = 0; j < gpsGroups.length; j++) {

                    String firstGps = gpsGroups[j];

                    String[] split = firstGps.split(",");

                    if (Utils.isEmpty(strplanes)) {
                        strplanes = new Double(split[0]) + "," +new Double(split[1]);
                    } else {
                        strplanes = strplanes + "|" + new Double(split[0]) + "," +new Double(split[1]);
                    }

                    if (j == 0) {
                        resDataBean.setLatitude(new Double(split[0])  + "");
                        resDataBean.setLongitude(new Double(split[1])  + "");
                        resDataBean.setFd_resposition(new Double(split[0]) + "," +new Double(split[1]));

                    }



                }

                formBean.setPropertyLabel(strplanes);




            }


        }


    }



    public interface ResDataDeleteIF{
        void onResDataDeleteResult(boolean isOk, ResDataBean resDataBean);
    }
}
