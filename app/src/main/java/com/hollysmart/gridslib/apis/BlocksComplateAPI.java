package com.hollysmart.gridslib.apis;

import com.hollysmart.db.UserInfo;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.value.Values;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.MediaType;

public class BlocksComplateAPI implements INetModel {


    private UserInfo userInfo;
    private String id;
    private BlockBean blockBean;
    private BlocksScomplateIF blocksScomplateIF;


    public BlocksComplateAPI(UserInfo userInfo, String  id, BlockBean blockBean, BlocksScomplateIF blocksScomplateIF) {
        this.userInfo = userInfo;
        this.id = id;
        this.blockBean = blockBean;
        this.blocksScomplateIF = blocksScomplateIF;
    }

    @Override
    public void request() {
        JSONObject object = new JSONObject();
        try {
            object.put("blockNum", blockBean.getFdBlockNum());
            object.put("blockCode", blockBean.getFdBlockCode());
            object.put("taskid",id);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        String urlStr = Values.SERVICE_URL + "api/blocks/complate";
        Mlog.d("object-------" + object.toString());
        Mlog.d("网格采集完成 urlStr-------" + urlStr);
        OkHttpUtils.postString().url(urlStr)
                .content(object.toString()).addHeader("Authorization", userInfo.getAccess_token())
                .mediaType(MediaType.parse("application/json; charset=utf-8"))
                .build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
                e.printStackTrace();
                blocksScomplateIF.blocksScomplateResult(false);
            }

            @Override
            public void onResponse(String response, int id) {
                Mlog.d("blocks/complate......... = " + response);
                response = response.replace("\"\"", "null");
                try {
                    JSONObject object = new JSONObject(response);
                    int status = object.getInt("status");
                    if (status == 1) {
                        blocksScomplateIF.blocksScomplateResult(true);
                    } else {
                        blocksScomplateIF.blocksScomplateResult(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    blocksScomplateIF.blocksScomplateResult(false);
                }
            }
        });
    }

    public interface BlocksScomplateIF {
        void blocksScomplateResult(boolean isOk);
    }
}
