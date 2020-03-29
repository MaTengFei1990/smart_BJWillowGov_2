package com.hollysmart.formlib.adapters.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.hollysmart.bjwillowgov.R;
import com.hollysmart.formlib.beans.DongTaiFormBean;

import java.util.List;

public class CheckBoxViewHolder extends BaseViewHolder {

    public LinearLayout ll_value;
    public ImageView iv_arrorw;

    public CheckBoxViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);

        ll_value = itemView.findViewById(R.id.ll_value);
        iv_arrorw = itemView.findViewById(R.id.iv_arrorw);
    }
}
