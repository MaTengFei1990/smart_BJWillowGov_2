package com.hollysmart.formlib.adapters.viewholder;

import com.hollysmart.formlib.adapters.TypeEnum;

public class ViewHolderFactory {

    public static BaseViewHolder getViewHolder(TypeEnum typeEnum) {

        BaseViewHolder baseViewHolder=null;

        try {
//            baseViewHolder = (BaseViewHolder) Class.forName(typeEnum.getvalue()).newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return baseViewHolder;
    }
}
