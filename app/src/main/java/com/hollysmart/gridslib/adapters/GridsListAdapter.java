package com.hollysmart.gridslib.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
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
import com.hollysmart.gridslib.beans.BlockAndStatusBean;
import com.hollysmart.gridslib.beans.BlockBean;
import com.hollysmart.utils.ACache;
import com.hollysmart.value.Values;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 表格列表的适配器
 */
public class GridsListAdapter extends CommonAdapter<BlockAndStatusBean> {

    private SlideManager manager;

    private List<BlockAndStatusBean> blockBeanList;
    private String TreeFormModelId;
    private String PcToken;
    private Context context;
    private ProjectBean projectBean;

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();

    private setMapBtnClickListener mapBtnClickListener;

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    Map<String, String> map = new HashMap<String , String>();

    public GridsListAdapter(String PcToken, Context context, String TreeFormModelId, List<BlockAndStatusBean> blockBeanList, ProjectBean projectBean, boolean ischeck) {
        super(context, blockBeanList, R.layout.adapter_grids_slide);
        this.context = context;
        this.TreeFormModelId = TreeFormModelId;
        this.PcToken = PcToken;
        this.blockBeanList = blockBeanList;
        manager = new SlideManager();
        this.projectBean = projectBean;
        this.ischeck = ischeck;
        isLogin();
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

    @Override
    public int getItemCount() {
        if (blockBeanList.size() == 0) {
            return 0;
        } else {

            return blockBeanList.size();
        }

    }

    @Override
    public void convert(final int position, CommonHolder holder, final BlockAndStatusBean item) {

        BlockAndStatusBean blockAndStatusBean = blockBeanList.get(position);
        BlockBean blockBean = blockAndStatusBean.getBlock();

        final SlideLayout slSlide = holder.getView(R.id.sl_slide);

        final TextView tv_delete = holder.getView(R.id.tv_delete);
        final TextView tv_gridNumTitle = holder.getView(R.id.tv_gridNumTitle);
        final TextView tv_check = holder.getView(R.id.tv_check);
        final TextView tv_gridNum = holder.getView(R.id.tv_gridNum);
        final TextView tv_area = holder.getView(R.id.tv_area);
        final Button btn_map = holder.getView(R.id.btn_map);

        tv_gridNum.setText(blockBean.getFdBlockCode());
        tv_area.setText(blockBean.getFdAreaName());

        if (ischeck) {
            tv_delete.setVisibility(View.GONE);
            tv_check.setText("查看");
        }

        if (blockAndStatusBean.getFdStatus().equals("1")) {
            tv_gridNumTitle.setTextColor(mContext.getResources().getColor(R.color.titleBg));
            tv_gridNum.setTextColor(mContext.getResources().getColor(R.color.titleBg));
        } else {
            tv_gridNumTitle.setTextColor(mContext.getResources().getColor(R.color.heise));
            tv_gridNum.setTextColor(mContext.getResources().getColor(R.color.heise));
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


        slSlide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, TreeListActivity.class);

                BlockAndStatusBean blockAndStatusBean = blockBeanList.get(position);
               final BlockBean resDataBean = blockAndStatusBean.getBlock();

                Activity activity = (Activity) context;
                intent.putExtra("projectBean", projectBean);
                intent.putExtra("blockBean", resDataBean);
                intent.putExtra("TreeFormModelId", TreeFormModelId);
                intent.putExtra("ischeck", ischeck);
                intent.putExtra("exter", (Serializable) map);
                intent.putExtra("PcToken", PcToken);
                intent.putExtra("position", position);
                activity.startActivityForResult(intent,7);
            }
        });


        tv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(context, RoadDetailsActivity.class);

                intent.putExtra("resDataBean", blockBeanList.get(position));
                intent.putExtra("formPicMap", (Serializable) formPicMap);
                intent.putExtra("ischeck", ischeck);
                Activity activity = (Activity) context;
                activity.startActivityForResult(intent, 4);

            }
        });


        btn_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mapBtnClickListener != null) {

                    BlockAndStatusBean blockAndStatusBean = blockBeanList.get(position);
                    final BlockBean blockBean = blockAndStatusBean.getBlock();

                    mapBtnClickListener.MapBtnClick(blockBean,position);
                }

            }
        });


    }


    @Override
    public long getItemId(int arg0) {
        return 0;
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


   public interface  setMapBtnClickListener{

        void MapBtnClick(BlockBean blockBean, int curPosition);

    }

}