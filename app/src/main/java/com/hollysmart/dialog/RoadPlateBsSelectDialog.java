package com.hollysmart.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.hollysmart.beans.DictionaryBean;
import com.hollysmart.bjwillowgov.R;

import java.util.List;

/**
 * Created by cai on 2017/9/20
 */

public class RoadPlateBsSelectDialog {

    private BsSelectIF bsSelectIF;
    private AlertDialog dialog;
    public RoadPlateBsSelectDialog(BsSelectIF bsSelectIF) {
        this.bsSelectIF = bsSelectIF;
    }

    public void showPopuWindow(Context mContext, final int type, String title, List<DictionaryBean> beanList) {
        String strs[] = new String[beanList.size()];
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final AlertDialog alertDialog = builder.create();
        LayoutInflater from = LayoutInflater.from(mContext);
        View view = from.inflate(R.layout.layout_lingeman_lv, null);
        LinearLayout ll_lv0 =  view.findViewById(R.id.ll_lv0);
        LinearLayout ll_lv1 =  view.findViewById(R.id.ll_lv1);
        LinearLayout ll_lv2 =  view.findViewById(R.id.ll_lv2);
        LinearLayout ll_lv3 =  view.findViewById(R.id.ll_lv3);
        LinearLayout ll_lv5 =  view.findViewById(R.id.ll_lv5);

        alertDialog.setView(view);
        alertDialog.setTitle(title);

        ll_lv0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsSelectIF.onBsSelect(type,0);
                alertDialog.dismiss();

            }
        });
        ll_lv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsSelectIF.onBsSelect(type,1);
                alertDialog.dismiss();
            }
        });
        ll_lv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsSelectIF.onBsSelect(type,2);
                alertDialog.dismiss();
            }
        });
        ll_lv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsSelectIF.onBsSelect(type,3);
                alertDialog.dismiss();
            }
        });
        ll_lv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bsSelectIF.onBsSelect(type,4);
                alertDialog.dismiss();
            }
        });


        alertDialog.show();
    }

    public interface BsSelectIF {
        void onBsSelect(int type, int index);
    }



    public void dissmiss(){
        if (dialog != null) {
            dialog.dismiss();
        }

    }


}























