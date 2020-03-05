package com.hollysmart.gridslib.apis;

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

/**
 * Created by Lenovo on 2019/4/18.
 */

public class GetTreeNumAPI implements INetModel {


    private String PcToken;
    private ResDataBean roadBean;
    private GetTreeNumAPIIF datadicListIF;

    public GetTreeNumAPI(String PcToken,ResDataBean roadBean, GetTreeNumAPIIF datadicListIF) {
        this.PcToken = PcToken;
        this.roadBean = roadBean;
        this.datadicListIF = datadicListIF;
    }

    @Override
    public void request() {

        FormModelBean formModel = roadBean.getFormModel();

        String city ="";
        String town ="";
        String objname ;

        objname = roadBean.getFd_resname();

        if (formModel != null) {
            List<DongTaiFormBean> cgformFieldList = formModel.getCgformFieldList();

            if (cgformFieldList != null && cgformFieldList.size() > 0) {


                for (int i = 0; i < cgformFieldList.size(); i++) {

                    DongTaiFormBean formBean = cgformFieldList.get(i);

                    if (formBean.getJavaField().equals("road_area")) {

                        String propertyLabel = formBean.getPropertyLabel();

                        Mlog.d("road_area====="+propertyLabel);

                        String[] s = propertyLabel.split(" ");

                        city= s[0];
                        town= s[1];
                    }

                }

            }

        }else {

            roadBean.getFormData();
            if (!Utils.isEmpty(roadBean.getFormData())) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(roadBean.getFormData());
                    Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                    List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                            new TypeToken<List<DongTaiFormBean>>() {}.getType());
                    for (DongTaiFormBean dongTaiFormBean : dictList) {

                        if (dongTaiFormBean.getJavaField().equals("road_area")) {

                            String propertyLabel  = dongTaiFormBean.getPropertyLabel();

                            String[] s = propertyLabel.split(" ");

                            city= s[0];
                            town= s[1];

                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }


        }


        String urlStr = Values.SERVICE_URL_PC + "admin/api/infoProtectionRoadway/getTreeCode";
        OkHttpUtils.get().url(urlStr)
                .addHeader("Authorization",PcToken)
                .addParams("city",city)
                .addParams("town",town)
                .addParams("objname",objname)
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                datadicListIF.getTreeNumber(false, null);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("getTreeNumber......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 200) {

                        String  treeNum = object.getString("data");

                        datadicListIF.getTreeNumber(true, treeNum);
                    } else {
                        datadicListIF.getTreeNumber(false, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    datadicListIF.getTreeNumber(false, null);
                }
            }
        });
    }

    public interface GetTreeNumAPIIF {
        void getTreeNumber(boolean isOk, String treeNumber);
    }
}
