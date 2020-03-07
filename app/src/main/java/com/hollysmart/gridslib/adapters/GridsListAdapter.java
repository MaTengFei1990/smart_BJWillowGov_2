package com.hollysmart.gridslib.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.d.lib.slidelayout.SlideLayout;
import com.d.lib.slidelayout.SlideManager;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.lib.xrv.adapter.CommonHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.apis.ResModelListAPI;
import com.hollysmart.beans.GPS;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.beans.ResModelBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.DatabaseHelper;
import com.hollysmart.db.JDPicDao;
import com.hollysmart.db.ProjectDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.apis.ResDataDeleteAPI;
import com.hollysmart.formlib.apis.ResDataGetAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.FormModelBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.gridslib.RoadDetailsActivity;
import com.hollysmart.gridslib.TreeListActivity;
import com.hollysmart.gridslib.apis.FindListPageAPI;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.utils.ACache;
import com.hollysmart.utils.GPSConverterUtils;
import com.hollysmart.utils.Mlog;
import com.hollysmart.utils.Utils;
import com.hollysmart.value.Values;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/***
 * 表格列表的适配器
 */
public class GridsListAdapter extends CommonAdapter<GridBean> {

    private SlideManager manager;

    private List<GridBean> gridBeanList;
    private String TreeFormModelId;
    private String PcToken;
    private Context context;
    private ProjectBean projectBean;

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();

    boolean ischeck = false; //是否只能查看 true  只能查看不能编辑；

    public GridsListAdapter(String PcToken, Context context, String TreeFormModelId, List<GridBean> gridBeanList, ProjectBean projectBean, boolean ischeck) {
        super(context, gridBeanList, R.layout.adapter_grids_slide);
        this.context = context;
        this.TreeFormModelId = TreeFormModelId;
        this.PcToken = PcToken;
        this.gridBeanList = gridBeanList;
        manager = new SlideManager();
        this.projectBean = projectBean;
        this.ischeck = ischeck;
        isLogin();
    }


    @Override
    public int getItemCount() {
        if (gridBeanList.size() == 0) {
            return 0;
        } else {

            return gridBeanList.size();
        }

    }

    @Override
    public void convert(final int position, CommonHolder holder, final GridBean item) {

        GridBean gridBean = gridBeanList.get(position);
        final SlideLayout slSlide = holder.getView(R.id.sl_slide);

        final TextView tv_delete = holder.getView(R.id.tv_delete);
        final TextView tv_check = holder.getView(R.id.tv_check);
        final TextView tv_gridNum = holder.getView(R.id.tv_gridNum);

        tv_gridNum.setText(gridBean.getFdBlockCode());

        if (ischeck) {
            tv_delete.setVisibility(View.GONE);
            tv_check.setText("查看");
        }

        holder.setText(R.id.tv_countOfTree, "树木数量" + gridBeanList.get(position).getChildTreeCount() + "棵");

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
                final GridBean resDataBean = gridBeanList.get(position);
                Activity activity = (Activity) context;
                intent.putExtra("projectBean", projectBean);
                intent.putExtra("roadBean", resDataBean);
                intent.putExtra("TreeFormModelId", TreeFormModelId);
                intent.putExtra("ischeck", ischeck);
                intent.putExtra("PcToken", PcToken);
                activity.startActivity(intent);
            }
        });


        tv_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Intent intent = new Intent(context, RoadDetailsActivity.class);

                intent.putExtra("resDataBean", gridBeanList.get(position));
                intent.putExtra("formPicMap", (Serializable) formPicMap);
                intent.putExtra("ischeck", ischeck);
                Activity activity = (Activity) context;
                activity.startActivityForResult(intent, 4);

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

}