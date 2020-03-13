package com.hollysmart.gridslib.apis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
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
import okhttp3.MediaType;

public class BlocksComplateAPI implements INetModel {


    private UserInfo userInfo;
    private ProjectBean projectBean;
    private BlockBean blockBean;
    private BlocksScomplateIF blocksScomplateIF;


    public BlocksComplateAPI(UserInfo userInfo, ProjectBean projectBean, BlockBean blockBean, BlocksScomplateIF blocksScomplateIF) {
        this.userInfo = userInfo;
        this.projectBean = projectBean;
        this.blockBean = blockBean;
        this.blocksScomplateIF = blocksScomplateIF;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {
            object.put("blockNum ", blockBean.getFdBlockNum());
            object.put("blockCode ", blockBean.getFdBlockCode());
            object.put("taskid ", projectBean.getId());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String urlStr = Values.SERVICE_URL + "/api/blocks/complate";
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", userInfo.getAccess_token())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                blocksScomplateIF.blocksScomplateResult(false, null);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("blocks/complate......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 200) {
                        blocksScomplateIF.blocksScomplateResult(true, null);
                    } else {
                        blocksScomplateIF.blocksScomplateResult(false, null);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    blocksScomplateIF.blocksScomplateResult(false, null);
                }
            }
        });
    }

    public interface BlocksScomplateIF {
        void blocksScomplateResult(boolean isOk, List<ResDataBean> menuBeanList);
    }
}
