package com.hollysmart.formlib.adapters.viewholder;

import android.util.SparseArray;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.hollysmart.formlib.beans.DongTaiFormBean;

import java.util.List;

public class ViewHolderManager extends RecyclerView.ViewHolder {

    private static ViewHolderManager instance = new ViewHolderManager();

    private ViewHolderManager() {
        super(null);
        init();
    }

    public static ViewHolderManager getInstance() {
        return instance;
    }

    public TextView tv_bitian;
    public TextView tv_name;
    public TextView tv_value;
    public TextView tv_tishi;
    public EditText et_value;

    public List<DongTaiFormBean> biaoGeBeanList;

    private SparseArray<Class<? extends BaseViewHolder>> viewholders = new SparseArray<>();

    private void init() {
    }


    public void FieldMustInput(DongTaiFormBean bean, ViewHolderManager holder) {

        if (bean.getFieldMustInput()) {
            holder.tv_bitian.setVisibility(View.VISIBLE);
        } else {
            holder.tv_bitian.setVisibility(View.GONE);
        }

    }

    public void showTiShi(DongTaiFormBean bean, ViewHolderManager holder) {

        if (bean.isShowTiShi()) {
            holder.tv_tishi.setVisibility(View.VISIBLE);
        } else {
            holder.tv_tishi.setVisibility(View.GONE);
        }

    }

    public void setValue(DongTaiFormBean bean, ViewHolderManager holder) {

        if (bean.getPropertyLabel() != null) {

            holder.tv_value.setText(bean.getPropertyLabel());

        } else {
            holder.tv_value.setText("");
        }


    }


    public void setName(DongTaiFormBean bean, ViewHolderManager holder) {

        holder.tv_name.setText(bean.getContent());
    }
}
