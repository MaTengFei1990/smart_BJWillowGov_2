package com.hollysmart.formlib;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hollysmart.bjwillowgov.R;
import com.hollysmart.formlib.apis.GetHisTreeInfoAPI;
import com.hollysmart.formlib.beans.HisTreeInfo;
import com.hollysmart.style.StyleAnimActivity;
import com.hollysmart.utils.Mlog;
import com.hollysmart.value.UserToken;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HisTreeInfoActivity extends StyleAnimActivity {

    @Override
    public int layoutResID() {
        return R.layout.activity_his_tree_info;
    }

    @BindView(R.id.ib_back)
    ImageView ib_back;

    @BindView(R.id.tv_id)
    TextView tv_id;
    @BindView(R.id.tv_fdTreeState)
    TextView tv_fdTreeState;
    @BindView(R.id.tv_fdTreeType)
    TextView tv_fdTreeType;


    @Override
    public void findView() {
        ButterKnife.bind(this);
        ib_back.setOnClickListener(this);
    }

    @Override
    public void init() {
        String treeId = getIntent().getStringExtra("treeid");

        String token = UserToken.getUserToken().getToken();
        Mlog.d("---------token" + token);
        Mlog.d("---------treeId" + treeId);

        new GetHisTreeInfoAPI(token, treeId, new GetHisTreeInfoAPI.GetHisTreeLsitIF() {
            @Override
            public void onResTaskListResult(boolean isOk, HisTreeInfo hisTreeInfo, String msg) {
                if (isOk) {
                    tv_id.setText(hisTreeInfo.getId());
                    tv_fdTreeType.setText(hisTreeInfo.getFdTreeType());
                    tv_fdTreeState.setText(hisTreeInfo.getFdTreeState());
                }

            }


        }).request();

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ib_back:
                finish();
                break;
        }

    }
}