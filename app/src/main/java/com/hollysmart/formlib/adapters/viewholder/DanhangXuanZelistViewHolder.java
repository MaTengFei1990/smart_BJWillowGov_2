package com.hollysmart.formlib.adapters.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

public class DanhangXuanZelistViewHolder extends BaseViewHolder {

    public TextView tv_value;
    public LinearLayout ll_value;
    public ImageView iv_arrorw;

    public DanhangXuanZelistViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);

        ll_value = itemView.findViewById(R.id.ll_value);
        iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
    }




}
