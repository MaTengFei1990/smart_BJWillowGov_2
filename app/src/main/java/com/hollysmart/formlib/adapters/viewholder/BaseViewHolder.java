package com.hollysmart.formlib.adapters.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

public abstract class BaseViewHolder extends RecyclerView.ViewHolder{

    public TextView tv_bitian;
    public TextView tv_name;
    public TextView tv_value;
    public TextView tv_tishi;
    public EditText et_value;

    public List<DongTaiFormBean> biaoGeBeanList;


    public BaseViewHolder(View itemView,List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView);
        this.biaoGeBeanList = biaoGeBeanList;
        tv_bitian = itemView.findViewById(R.id.tv_bitian);
        tv_name = itemView.findViewById(R.id.tv_name);
        tv_value = itemView.findViewById(R.id.tv_value);
        tv_tishi = itemView.findViewById(R.id.tv_tishi);
        et_value = itemView.findViewById(R.id.et_value);

    }



    public void FieldMustInput(DongTaiFormBean bean, BaseViewHolder holder){

        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

    }

    public void showTiShi(DongTaiFormBean bean, BaseViewHolder holder){

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

    }
    public void setValue(DongTaiFormBean bean, BaseViewHolder holder){

        if (bean.getPropertyLabel() != null) {

            holder.tv_value.setText(bean.getPropertyLabel());

        } else {
            holder.tv_value.setText("");
        }


    }


    public void setName(DongTaiFormBean bean, BaseViewHolder holder) {

        holder.tv_name.setText(bean.getContent());
    }
}
