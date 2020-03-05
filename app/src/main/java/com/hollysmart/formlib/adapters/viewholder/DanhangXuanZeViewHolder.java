package com.hollysmart.formlib.adapters.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

public class DanhangXuanZeViewHolder extends BaseViewHolder {
    public TextView tv_value;
    public ImageView iv_arrorw;

    public DanhangXuanZeViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);
        iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
    }

}
