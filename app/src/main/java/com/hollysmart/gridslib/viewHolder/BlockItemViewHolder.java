package com.hollysmart.gridslib.viewHolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.d.lib.slidelayout.SlideLayout;
import com.d.lib.xrv.adapter.CommonHolder;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.UserInfo;
import com.hollysmart.gridslib.RoadDetailsActivity;
import com.hollysmart.gridslib.TreeListActivity;
import com.hollysmart.gridslib.apis.BlocksPropertyAPI;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.utils.Utils;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class BlockItemViewHolder extends CommonHolder {
    @BindView(R.id.sl_slide)
    SlideLayout slSlide;
    @BindView(R.id.tv_delete)
    TextView tv_delete;
    @BindView(R.id.tv_gridNumTitle)
    TextView tv_gridNumTitle;
    @BindView(R.id.tv_check)
    TextView tv_check;
    @BindView(R.id.tv_gridNum)
    TextView tv_gridNum;
    @BindView(R.id.tv_area)
    TextView tv_area;
    @BindView(R.id.btn_treelist)
    Button btn_treelist;
    @BindView(R.id.btn_Property)
    Button btn_Property;
    @BindView(R.id.tv_property)
    TextView tv_property;

    @BindView(R.id.tv_countOfTree)
    TextView tv_countOfTree;


    private Context mContext;


    public BlockItemViewHolder(Context mContext, View itemView) {
        super(mContext, itemView, R.layout.item_footer);
        this.mContext = mContext;
        ButterKnife.bind(this, itemView);
    }


    public void bind(BlockAndStatusBean blockAndStatusBean, boolean ischeck, UserInfo userInfo) {

        BlockBean blockBean = blockAndStatusBean.getBlock();

        tv_gridNum.setText(blockBean.getFdBlockCode());
        tv_area.setText(blockBean.getFdAreaName());

        if (ischeck) {
            tv_delete.setVisibility(View.GONE);
            tv_check.setText("查看");
            btn_Property.setVisibility(View.GONE);
        }else{

            btn_Property.setVisibility(View.VISIBLE);
        }

        if ("2".equals(userInfo.getUserType())) {
            btn_Property.setVisibility(View.VISIBLE);
        }else {
            btn_Property.setVisibility(View.GONE);
        }

        if (blockAndStatusBean.getFdStatus().equals("1")) {
            tv_gridNumTitle.setTextColor(mContext.getResources().getColor(R.color.titleBg));
            tv_gridNum.setTextColor(mContext.getResources().getColor(R.color.titleBg));
        } else {
            tv_gridNumTitle.setTextColor(mContext.getResources().getColor(R.color.heise));
            tv_gridNum.setTextColor(mContext.getResources().getColor(R.color.heise));
        }

        String[] positives={"该网格无法调查","该网格部分可调查","该网格无雌株"};

        if (!Utils.isEmpty(blockAndStatusBean.getBlockProperty())) {
            switch (blockAndStatusBean.getBlockProperty()) {

                case "1":
                    tv_property.setText(positives[0]);
                    break;
                case "2":
                    tv_property.setText(positives[1]);
                    break;
                case "3":
                    tv_property.setText(positives[2]);
                    break;
                default:
                    tv_property.setText("");
                    break;

            }

        }else {
            tv_property.setText("");
        }

        tv_countOfTree.setText("树木数量" + blockBean.getChildTreeCount() + "棵");

//        slSlide.setOnStateChangeListener(new SlideLayout.OnStateChangeListener() {
//            @Override
//            public void onChange(SlideLayout layout, boolean isOpen) {
////                item.isOpen = isOpen;
//                manager.onChange(layout, isOpen);
//            }
//
//            @Override
//            public boolean closeAll(SlideLayout layout) {
//                return manager.closeAll(layout);
//            }
//        });
//
//
//        btn_treelist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, TreeListActivity.class);
//
//                final BlockBean resDataBean = blockAndStatusBean.getBlock();
//
//                Activity activity = (Activity) mContext;
//                intent.putExtra("projectBean", projectBean);
//                intent.putExtra("blockBean", resDataBean);
//                intent.putExtra("TreeFormModelId", TreeFormModelId);
//                intent.putExtra("ischeck", ischeck);
//                intent.putExtra("exter", (Serializable) map);
//                intent.putExtra("PcToken", PcToken);
//                intent.putExtra("position", position);
//                activity.startActivityForResult(intent,7);
//            }
//        });
//
//        btn_Property.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(mContext, TreeListActivity.class);
//
//                final BlockBean resDataBean = blockAndStatusBean.getBlock();
//
//
//                String[] positives={"该网格无法调查","该网格部分可调查","该网格无雌株"};
//                AlertDialog.Builder builder=new AlertDialog.Builder(mContext);
//                builder.setSingleChoiceItems(positives,0, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        blockAndStatusBean.setBlockProperty(which + 1+"");
//
//
//
//                    }
//                });
//
//                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                        if (Utils.isEmpty(blockAndStatusBean.getBlockProperty())) {
//                            blockAndStatusBean.setBlockProperty("1");
//                        }
//
//                        new BlocksPropertyAPI(userInfo, map.get("id"), blockAndStatusBean, new BlocksPropertyAPI.BlocksScomplateIF() {
//                            @Override
//                            public void blocksScomplateResult(boolean isOk) {
//
//                                if (isOk) {
//                                    notifyDataSetChanged();
//
//                                    dialog.dismiss();
//
//                                }
//
//                            }
//                        }).request();
//
//                    }
//                });
//
//                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//
//                    }
//                });
//                builder.show();
//
//            }
//        });
//
//
//        tv_check.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final Intent intent = new Intent(mContext, RoadDetailsActivity.class);
//
//                intent.putExtra("resDataBean", blockAndStatusBean);
//                intent.putExtra("formPicMap", (Serializable) formPicMap);
//                intent.putExtra("ischeck", ischeck);
//                Activity activity = (Activity) mContext;
//                activity.startActivityForResult(intent, 4);
//
//            }
//        });
//
//
//        slSlide.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (mapBtnClickListener != null) {
//
//                    BlockAndStatusBean blockAndStatusBean = blockBeanList.get(position);
//                    final BlockBean blockBean = blockAndStatusBean.getBlock();
//
//                    mapBtnClickListener.MapBtnClick(blockBean,position);
//                }
//
//            }
//        });
    }

}