package com.hollysmart.formlib.adapters.viewholder;

import android.view.View;
import android.widget.TextView;

import com.hollysmart.bjwillowgov.R;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.views.linearlayoutforlistview.MyLinearLayoutForListView;

import java.util.List;

public class ImageContentViewHolder extends BaseViewHolder {

    public MyLinearLayoutForListView ll_jingdian_pic;
    public TextView tv_hint_tishi;

    public ImageContentViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);
        ll_jingdian_pic = itemView.findViewById(R.id.ll_jingdian_pic);

        tv_hint_tishi = itemView.findViewById(R.id.tv_hint_tishi);
    }


}
