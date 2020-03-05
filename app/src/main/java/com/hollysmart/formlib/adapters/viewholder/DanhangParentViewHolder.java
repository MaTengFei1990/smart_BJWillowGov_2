package com.hollysmart.formlib.adapters.viewholder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

public class  DanhangParentViewHolder extends BaseViewHolder {
    public EditText et_value;

    public DanhangParentViewHolder(View itemView, List<DongTaiFormBean> biaoGeBeanList) {
        super(itemView, biaoGeBeanList);
        et_value =  itemView.findViewById(R.id.et_value);
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
           biaoGeBeanList.get(getLayoutPosition()).setPropertyLabel(s.toString());
       }
   };

}
