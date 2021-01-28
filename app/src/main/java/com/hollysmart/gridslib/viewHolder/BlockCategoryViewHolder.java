package com.hollysmart.gridslib.viewHolder;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.d.lib.xrv.adapter.CommonHolder;
import com.hollysmart.bjwillowgov.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BlockCategoryViewHolder extends CommonHolder {
    @BindView(R.id.categoryTextView)
    TextView categoryTextView;

   public BlockCategoryViewHolder(Context mContext, View itemView) {
        super(mContext, itemView, R.layout.item_footer);
        ButterKnife.bind(this, itemView);
    }



    public void bind(String category) {
        categoryTextView.setText(category);
    }




}
