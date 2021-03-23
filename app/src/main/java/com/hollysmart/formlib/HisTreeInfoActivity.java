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

    @BindView(R.id.tv_code)
    TextView tv_code;

    @BindView(R.id.tv_fdTreeState)
    TextView tv_fdTreeState;

    @BindView(R.id.tv_fdTreeType)
    TextView tv_fdTreeType;

    @BindView(R.id.tv_area)
    TextView tv_area;

    @BindView(R.id.tv_treeinJect)
    TextView tv_treeinJect;
    @BindView(R.id.tv_TDiamree)
    TextView tv_TDiamree;


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
                    tv_code.setText(hisTreeInfo.getFdTreeCode());
                    tv_fdTreeType.setText(hisTreeInfo.getFdTreeType());
                    tv_fdTreeState.setText(hisTreeInfo.getFdTreeState());
                    tv_area.setText(hisTreeInfo.getFdAreaName());
                    tv_treeinJect.setText(hisTreeInfo.getFdTreeInject());
                    tv_TDiamree.setText(hisTreeInfo.getFdTreeDiam());
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