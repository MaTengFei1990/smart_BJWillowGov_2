package com.hollysmart.formlib.adapters.viewholder;

import android.view.View;
import android.widget.ImageView;

import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

public class SwitchContentViewHolder extends BaseViewHolder {
    public ImageView iv_switch;

    public SwitchContentViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);

        tv_bitian = itemView.findViewById(R.id.tv_bitian);
        iv_switch = itemView.findViewById(R.id.iv_switch);
    }
}
