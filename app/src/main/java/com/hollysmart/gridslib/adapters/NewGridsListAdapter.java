package com.hollysmart.gridslib.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.d.lib.slidelayout.SlideLayout;
import com.d.lib.slidelayout.SlideManager;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.lib.xrv.adapter.CommonHolder;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.gridslib.RoadDetailsActivity;
import com.hollysmart.gridslib.TreeListActivity;
import com.hollysmart.gridslib.apis.BlocksPropertyAPI;
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.gridslib.viewHolder.BlockCategoryViewHolder;
import com.hollysmart.gridslib.viewHolder.BlockItemViewHolder;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 表格列表的适配器
 */
public class NewGridsListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private SlideManager manager;

    private List<BlockAndStatusBean> blockBeanList;
    private List<BlockAndStatusBean> nearbyblockBeanList;//附近的网格
    private String TreeFormModelId;
    private String PcToken;
    private Context context;
    private ProjectBean projectBean;

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();

    private setMapBtnClickListener mapBtnClickListener;

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    Map<String, String> map = new HashMap<String, String>();

    public NewGridsListAdapter(String PcToken, Context context, String TreeFormModelId, List<BlockAndStatusBean> blockBeanList, ProjectBean projectBean, boolean ischeck) {
//        super(context, blockBeanList, R.layout.adapter_grids_slide);
        this.context = context;
        this.TreeFormModelId = TreeFormModelId;
        this.PcToken = PcToken;
        this.blockBeanList = blockBeanList;
        manager = new SlideManager();
        this.projectBean = projectBean;
        this.ischeck = ischeck;
        isLogin();
    }


    public void setnearbyblocks(List<BlockAndStatusBean> nearbyblockBeanList) {
        this.nearbyblockBeanList = nearbyblockBeanList;
    }

    public setMapBtnClickListener getMapBtnClickListener() {
        return mapBtnClickListener;
    }

    public void setMapBtnClickListener(setMapBtnClickListener mapBtnClickListener) {
        this.mapBtnClickListener = mapBtnClickListener;
    }


    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }


    private void setview(int position, CommonHolder holder, BlockBean blockBean, BlockAndStatusBean blockAndStatusBean) {

        if (!Utils.isEmpty(blockAndStatusBean.getNullAddFlag()) && blockAndStatusBean.getNullAddFlag().equals("true")) {

            holder.setText(R.id.tv_gridNumTitle, "您所在的区域无网格，请尝试下拉刷新");

            holder.getView(R.id.rl_all1).setVisibility(View.GONE);
            holder.getView(R.id.tv_gridNum).setVisibility(View.GONE);
            holder.getView(R.id.ll_all2).setVisibility(View.GONE);
            holder.getView(R.id.ll_all3).setVisibility(View.GONE);
            holder.getView(R.id.sl_slide).setEnabled(false);

        } else {


            final SlideLayout slSlide = holder.getView(R.id.sl_slide);

            final TextView tv_delete = holder.getView(R.id.tv_delete);
            final TextView tv_gridNumTitle = holder.getView(R.id.tv_gridNumTitle);
            final TextView tv_check = holder.getView(R.id.tv_check);
            final TextView tv_gridNum = holder.getView(R.id.tv_gridNum);
            final TextView tv_area = holder.getView(R.id.tv_area);
            final Button btn_treelist = holder.getView(R.id.btn_treelist);
            final Button btn_Property = holder.getView(R.id.btn_Property);
            final TextView tv_property = holder.getView(R.id.tv_property);

            tv_gridNum.setText(blockBean.getFdBlockCode());
            tv_area.setText(blockBean.getFdAreaName());

            if (ischeck) {
                tv_delete.setVisibility(View.GONE);
                tv_check.setText("查看");
                btn_Property.setVisibility(View.GONE);
            } else {

                btn_Property.setVisibility(View.VISIBLE);
            }

            if ("2".equals(userInfo.getUserType())) {
                btn_Property.setVisibility(View.VISIBLE);
            } else {
                btn_Property.setVisibility(View.GONE);
            }

            if (blockAndStatusBean.getFdStatus().equals("1")) {
                tv_gridNumTitle.setTextColor(context.getResources().getColor(R.color.titleBg));
                tv_gridNum.setTextColor(context.getResources().getColor(R.color.titleBg));
            } else {
                tv_gridNumTitle.setTextColor(context.getResources().getColor(R.color.heise));
                tv_gridNum.setTextColor(context.getResources().getColor(R.color.heise));
            }

            String[] positives = {"该网格无法调查", "该网格部分可调查", "该网格无雌株"};

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

            } else {
                tv_property.setText("");
            }


            holder.setText(R.id.tv_countOfTree, "树木数量" + blockBean.getChildTreeCount() + "棵");

            slSlide.setOnStateChangeListener(new SlideLayout.OnStateChangeListener() {
                @Override
                public void onChange(SlideLayout layout, boolean isOpen) {
//                item.isOpen = isOpen;
                    manager.onChange(layout, isOpen);
                }

                @Override
                public boolean closeAll(SlideLayout layout) {
                    return manager.closeAll(layout);
                }
            });


            btn_treelist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TreeListActivity.class);


                    Activity activity = (Activity) context;
                    intent.putExtra("projectBean", projectBean);
                    intent.putExtra("blockBean", blockAndStatusBean.getBlock());
                    intent.putExtra("TreeFormModelId", TreeFormModelId);
                    intent.putExtra("ischeck", ischeck);
                    intent.putExtra("exter", (Serializable) map);
                    intent.putExtra("PcToken", PcToken);
                    intent.putExtra("position", position);
                    activity.startActivityForResult(intent, 7);
                }
            });

            btn_Property.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, TreeListActivity.class);

                    String[] positives = {"该网格无法调查", "该网格部分可调查", "该网格无雌株"};
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setSingleChoiceItems(positives, 0, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            blockAndStatusBean.setBlockProperty(which + 1 + "");


                        }
                    });

                    builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if (Utils.isEmpty(blockAndStatusBean.getBlockProperty())) {
                                blockAndStatusBean.setBlockProperty("1");
                            }

                            new BlocksPropertyAPI(userInfo, map.get("id"), blockAndStatusBean, new BlocksPropertyAPI.BlocksScomplateIF() {
                                @Override
                                public void blocksScomplateResult(boolean isOk) {

                                    if (isOk) {
                                        notifyDataSetChanged();

                                        dialog.dismiss();

                                    }

                                }
                            }).request();

                        }
                    });

                    builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    builder.show();

                }
            });


            tv_check.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Intent intent = new Intent(context, RoadDetailsActivity.class);


                    intent.putExtra("resDataBean", blockAndStatusBean);
                    intent.putExtra("formPicMap", (Serializable) formPicMap);
                    intent.putExtra("ischeck", ischeck);
                    Activity activity = (Activity) context;
                    activity.startActivityForResult(intent, 4);

                }
            });



            slSlide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (mapBtnClickListener != null) {

                        final BlockBean blockBean = blockAndStatusBean.getBlock();

                        mapBtnClickListener.MapBtnClick(blockBean,position);
                    }

                }
            });



        }
    }


    @Override
    public int getItemCount() {
        return 2 + (nearbyblockBeanList == null ? 0 : nearbyblockBeanList.size()) + (blockBeanList == null ? 0 : blockBeanList.size());
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == R.layout.block_item_category) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.block_item_category, parent, false);
            return new BlockCategoryViewHolder(context, view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_grids_slide, parent, false);
            RecyclerView.ViewHolder holder = new BlockItemViewHolder(context, view);
            return holder;
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        BlockBean blockBean = null;
        BlockAndStatusBean blockAndStatusBean = null;

        if (holder.getItemViewType() == R.layout.block_item_category) {
            if (position == 0) {
                ((BlockCategoryViewHolder) holder).bind("附近的网格");
            } else {
                ((BlockCategoryViewHolder) holder).bind("全部的网格");
            }
        } else {


            if (nearbyblockBeanList == null || nearbyblockBeanList.isEmpty()) {
                blockAndStatusBean = blockBeanList.get(position - 2);
                blockBean = blockAndStatusBean.getBlock();
                setview(position - 2, (CommonHolder) holder, blockBean, blockAndStatusBean);
            } else {
                if (position > nearbyblockBeanList.size()) {
                    blockAndStatusBean = blockBeanList.get(position - 2 - nearbyblockBeanList.size());
                    blockBean = blockAndStatusBean.getBlock();

                    setview(position - 2 - nearbyblockBeanList.size(), (CommonHolder) holder, blockBean, blockAndStatusBean);

                } else {
                    blockAndStatusBean = nearbyblockBeanList.get(position - 1);
                    blockBean = blockAndStatusBean.getBlock();
                    setview(position - 1, (CommonHolder) holder, blockBean, blockAndStatusBean);
                }
            }

        }

    }

    @Override
    public int getItemViewType(int position) {
        if (nearbyblockBeanList == null || nearbyblockBeanList.isEmpty()) {
            int type;
            switch (position) {
                case 0:
                case 1:
                    type = R.layout.block_item_category;
                    break;
                default:
                    type = R.layout.adapter_grids_slide;
                    break;
            }
            return type;
        } else {
            if (position == 0 || position == nearbyblockBeanList.size() + 1) {
                return R.layout.block_item_category;
            } else {
                return R.layout.adapter_grids_slide;
            }
        }
    }


    /**
     * 判断用户登录状态，登录获取用户信息
     */
    private UserInfo userInfo;

    public boolean isLogin() {
        if (userInfo != null)
            return true;
        try {
            String userPath = Values.SDCARD_FILE(Values.SDCARD_CACHE) + Values.CACHE_USER;
            Object obj = ACache.get(new File(userPath)).getAsObject(Values.CACHE_USERINFO);
            if (obj != null) {
                userInfo = (UserInfo) obj;
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public interface setMapBtnClickListener {

        void MapBtnClick(BlockBean blockBean, int curPosition);

    }

}