package com.hollysmart.formlib.adapters.viewholder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.hollysmart.formlib.beans.DongTaiFormBean;

import java.util.List;

//0  单行
public class DanhangViewHolder extends BaseViewHolder {
    public EditText et_value;

    public DanhangViewHolder(View itemView,List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView,biaoGeBeanList);
        et_value.addTextChangedListener(tw);
    }

    private TextWatcher tw = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            DongTaiFormBean itemStatusByPosition = ViewHolderUtils.getItemStatusByPosition(getLayoutPosition(),biaoGeBeanList);
            itemStatusByPosition.setPropertyLabel(s.toString());
        }
    };
}
