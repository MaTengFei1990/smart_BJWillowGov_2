package com.hollysmart.gridslib.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
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
import com.hollysmart.db.DatabaseHelper;
import com.hollysmart.db.JDPicDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.apis.ResDataDeleteAPI;
import com.hollysmart.formlib.apis.ResDataGetAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.FormModelBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.gridslib.TreeDetailsActivity;
import com.hollysmart.gridslib.apis.FindListPageAPI;
import com.hollysmart.gridslib.beans.GridBean;
import com.hollysmart.utils.ACache;
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

public class TreeListAdapter extends CommonAdapter<ResDataBean> {


    private LayoutInflater inflater;

    private List<ResDataBean> mJingDians;
    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集

    private String namestr;

    private Context context;
    private List<DongTaiFormBean> formBeanList=new ArrayList<>();// 当前资源的动态表单
    private List<DongTaiFormBean> newFormList;// 当前资源的动态表单

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();

    boolean ischeck =false; //是否只能查看 true  只能查看不能编辑；
    private String TreeFormModelId;
    private GridBean roadbean;
    private ProjectBean projectBean;

    private SlideManager manager;

    public TreeListAdapter(Context context, String TreeFormModelId, ProjectBean projectBean, GridBean roadbean, List<ResDataBean> mJingDians, boolean ischeck) {
        super(context, mJingDians, R.layout.item_tree);
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.mJingDians = mJingDians;
        this.TreeFormModelId = TreeFormModelId;
        this.roadbean = roadbean;
        this.projectBean = projectBean;
        manager = new SlideManager();
        this.ischeck = ischeck;
        isLogin();
    }



    @Override
    public int getItemCount() {
        if (mJingDians.size() == 0) {
            return 0;
        } else {

            return mJingDians.size();
        }

    }


    @Override
    public void convert(final int position, CommonHolder holder, ResDataBean item) {

        ResDataBean resDataBean = mJingDians.get(position);
        FormModelBean formModel = resDataBean.getFormModel();

        if (ischeck) {
            holder.getView(R.id.tv_delete).setVisibility(View.GONE);
            holder.setText(R.id.tv_check,"查看");
        }


        holder.setText(R.id.tv_jingquName, resDataBean.getFd_resname());

        String resName = resDataBean.getFd_resname();

        String showName = "";
        try{

            if (!Utils.isEmpty(resName)) {

                String[] resNames = resName.split("-");
                showName = resNames[2] + "-" + resNames[3];

                holder.setText(R.id.tv_jingquName, showName);

            }

        }catch (Exception e){

                e.fillInStackTrace();
        }

        List<DongTaiFormBean> cgformFieldList = new ArrayList<>();
        if (formModel != null) {
           cgformFieldList = formModel.getCgformFieldList();

        } else {
            if (!Utils.isEmpty(resDataBean.getFormData())) {
                String strformData = resDataBean.getFormData();
                try {
                    JSONObject jsonObject = null;
                    jsonObject = new JSONObject(strformData);
                    Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                    cgformFieldList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                            new TypeToken<List<DongTaiFormBean>>() {}.getType());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

        }

        if (cgformFieldList != null && cgformFieldList.size() > 0) {

            for (int i = 0; i < cgformFieldList.size(); i++) {

                DongTaiFormBean formBean = cgformFieldList.get(i);

                if (formBean.getJavaField().equals("tree_species")) {

                    holder.setText(R.id.tv_treetype, formBean.getPropertyLabel());
                }
                if (formBean.getJavaField().equals("tree_growing")) {

                    holder.setText(R.id.tv_growing, formBean.getPropertyLabel());
                }
                if (formBean.getJavaField().equals("tree_dangerous")) {

                    holder.setText(R.id.tv_deagerTree, formBean.getPropertyLabel());
                }

            }

        }



        holder.getView(R.id.sl_slide).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(context, TreeDetailsActivity.class);
                final String formData = mJingDians.get(position).getFormData();

                formBeanList.clear();

                if (Utils.isEmpty(formData)) {

                    new ResDataGetAPI(userInfo.getAccess_token(), mJingDians.get(position), new ResDataGetAPI.ResDataDeleteIF() {
                        @Override
                        public void onResDataDeleteResult(boolean isOk, ResDataBean resDataBen) {

                            if (isOk) {
                                String formData = resDataBen.getFormData();

                                startDetailActivity(intent, formData, position);

                            }

                        }
                    }).request();

                } else {
                    startDetailActivity(intent, formData, position);
                }
            }
        });

        holder.getView(R.id.tv_delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delJQ(mJingDians.get(position));
            }
        });


        final SlideLayout slSlide = holder.getView(R.id.sl_slide);
        slSlide.setOnStateChangeListener(new SlideLayout.OnStateChangeListener() {
            @Override
            public void onChange(SlideLayout layout, boolean isOpen) {
                manager.onChange(layout, isOpen);
            }

            @Override
            public boolean closeAll(SlideLayout layout) {
                return manager.closeAll(layout);
            }
        });





    }

    private void startDetailActivity(Intent intent, String formData, int position) {
        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(formData);
            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            List<DongTaiFormBean> oldFormList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                    new TypeToken<List<DongTaiFormBean>>() {}.getType());
            formBeanList.addAll(oldFormList);
            getFormPicMap(formBeanList);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        intent.putExtra("formBeanList", (Serializable) formBeanList);
        intent.putExtra("resDataBean", mJingDians.get(position));
        intent.putExtra("formPicMap", (Serializable) formPicMap);
        intent.putExtra("roadbean", roadbean);
        intent.putExtra("projectBean", projectBean);
        intent.putExtra("ischeck",  ischeck);
        Activity activity = (Activity) context;
        activity.startActivityForResult(intent, 4);
    }

//    class ViewHolder {
//        TextView tv_name;
//        LinearLayout ll_bianji;
//        LinearLayout ll_shangchuan;
//        LinearLayout ll_xiugai;
//        LinearLayout ll_shanchu;
//        LinearLayout ll_fenxiang;
//        TextView tv_treetype;
//    }




    // 删除
    private void delJQ(final ResDataBean deleteBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除资源");
        builder.setMessage("确定要删除此资源吗?删除该树木后编号将丢失，可能会造成断号现象;");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                delDb(deleteBean);

                new ResDataDeleteAPI(userInfo.getAccess_token(), deleteBean, new ResDataDeleteAPI.ResDataDeleteIF() {
                    @Override
                    public void onResDataDeleteResult(boolean isOk, String msg) {

                        if (isOk) {
//                            selectDB(roadbean.getFdTaskId());
                        }

                    }
                }).request();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
            }
        });
        builder.create().show();
    }

    private void delDb(ResDataBean deleteBean) {
        SQLiteDatabase db = new DatabaseHelper(context).getWritableDatabase();
        //资源数据库删除
        ResDataDao resDataDao = new ResDataDao(context);
        boolean resDatadelFlag = resDataDao.deletDataById(deleteBean.getId());

        //资源数据库删除
        JDPicDao jdPicDao = new JDPicDao(context);
        boolean resPicdelFlag = jdPicDao.deletByResId(deleteBean.getId());


        if (resDatadelFlag) {

            if (resPicdelFlag) {
                Mlog.d("图片删除成功");
            }
            String file = Values.SDCARD_FILE(Values.SDCARD_FILE) + namestr + "/" + Values.SDCARD_PIC;
            String file3 = Values.SDCARD_FILE(Values.SDCARD_FILE) + namestr + "/" + Values.SDCARD_SOUNDS;
            picFile(file, deleteBean.getFd_resname());
            soundFile(file3, deleteBean.getFd_resname());

            if (picList.size() > 0) {
                for (int i = 0; i < picList.size(); i++) {
                    File file2 = new File(file + "/" + picList.get(i));
                    if (file2.exists()) {
                        file2.delete();
                    }
                }
            }
            if (soundList.size() > 0) {
                for (int j = 0; j < soundList.size(); j++) {
                    File file4 = new File(file3 + "/" + soundList.get(j));
                    if (file4.exists()) {
                        file4.delete();
                    }
                }
            }
            Toast.makeText(context, "资源删除成功", Toast.LENGTH_SHORT).show();

        } else {
            Toast.makeText(context, "资源删除失败", Toast.LENGTH_SHORT).show();
        }
        db.close();
    }






    // 查询
    private void selectDB(final String jqId) {
        Mlog.d("jqId = " + jqId);
        mJingDians.clear();

        resModelList.clear();


        if (resModelList == null || resModelList.size() == 0) {

            new ResModelListAPI(userInfo.getAccess_token(), new ResModelListAPI.ResModelListIF() {
                @Override
                public void onResModelListResult(boolean isOk, List<ResModelBean> projectBeanList) {

                    if (isOk) {
                        ResModelDao resModelDao = new ResModelDao(context);
                        resModelList.clear();
                        resModelList.addAll(projectBeanList);
                        resModelDao.addOrUpdate(resModelList);

                        ResModelBean resModelBean = resModelList.get(0);


                        String formData = resModelBean.getfJsonData();
                        formBeanList.clear();
                        try {
                            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                            List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                                    new TypeToken<List<DongTaiFormBean>>() {}.getType());
                            formBeanList.addAll(dictList);
                        } catch (JsonIOException e) {
                            e.printStackTrace();
                        }

                        JDPicDao jdPicDao = new JDPicDao(context);
                        for (int i = 0; i < mJingDians.size(); i++) {
                            ResDataBean resDataBean = mJingDians.get(i);

                            List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                            resDataBean.setJdPicInfos(jdPicInfoList);

                        }


                        new FindListPageAPI(userInfo,TreeFormModelId, projectBean,roadbean.getId(), new FindListPageAPI.DatadicListIF() {
                            @Override
                            public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {


                                List<String> idList = new ArrayList<>();

                                for (ResDataBean resDataBean : mJingDians) {

                                    idList.add(resDataBean.getId());
                                }


                                if (isOk) {
                                    if (netDataList != null && netDataList.size() > 0) {
                                        int j = 0;

                                        for (int i = 0; i < netDataList.size(); i++) {

                                            ResDataBean resDataBean = netDataList.get(i);

                                            if (!idList.contains(resDataBean.getId())) {
                                                String fd_resposition = resDataBean.getFd_resposition();

                                                if (!Utils.isEmpty(fd_resposition)) {

                                                    String[] split = fd_resposition.split(",");
                                                    resDataBean.setLatitude(split[0]);
                                                    resDataBean.setLongitude(split[1]);

                                                }


                                                mJingDians.add(resDataBean);


                                            }
                                        }


                                    }
                                }


                                notifyDataSetChanged();


                            }
                        }).request();


                    }


                }
            }).request();


        } else {
            ResModelBean resModelBean = resModelList.get(0);


            String formData = resModelBean.getfJsonData();
            formBeanList.clear();
            try {
                Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                List<DongTaiFormBean> dictList = mGson.fromJson(formData,
                        new TypeToken<List<DongTaiFormBean>>() {}.getType());
                formBeanList.addAll(dictList);
            } catch (JsonIOException e) {
                e.printStackTrace();
            }


            ResDataDao resDataDao = new ResDataDao(context);
            List<ResDataBean> resDataBeans = resDataDao.getData(jqId + "",true);
            if (resDataBeans != null && resDataBeans.size() > 0) {

                mJingDians.addAll(resDataBeans);
            }


            JDPicDao jdPicDao = new JDPicDao(context);
            for (int i = 0; i < mJingDians.size(); i++) {
                ResDataBean resDataBean = mJingDians.get(i);

                List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                resDataBean.setJdPicInfos(jdPicInfoList);

            }


            new FindListPageAPI(userInfo,TreeFormModelId, projectBean, roadbean.getId(),new FindListPageAPI.DatadicListIF() {
                @Override
                public void datadicListResult(boolean isOk, List<ResDataBean> netDataList) {


                    List<String> idList = new ArrayList<>();

                    for (ResDataBean resDataBean : mJingDians) {

                        idList.add(resDataBean.getId());
                    }


                    if (isOk) {
                        if (netDataList != null && netDataList.size() > 0) {
                            int j = 0;

                            for (int i = 0; i < netDataList.size(); i++) {

                                ResDataBean resDataBean = netDataList.get(i);

                                if (!idList.contains(resDataBean.getId())) {
                                    String fd_resposition = resDataBean.getFd_resposition();

                                    if (!Utils.isEmpty(fd_resposition)) {

                                        String[] split = fd_resposition.split(",");
                                        resDataBean.setLatitude(split[0]);
                                        resDataBean.setLongitude(split[1]);

                                    }


                                    mJingDians.add(resDataBean);


                                }
                            }


                        }
                    }


                    notifyDataSetChanged();


                }
            }).request();
        }

    }










    private void getFormPicMap(List<DongTaiFormBean> formBeans) {

        for (int i = 0; i < formBeans.size(); i++) {
            DongTaiFormBean formBean = formBeans.get(i);

            if (formBean.getPic() != null && formBean.getPic().size() > 0) {
                formPicMap.put(formBean.getJavaField(), formBean.getPic());

            }else {

                if (formBean.getShowType().equals("image")) {

                    if (!Utils.isEmpty(formBean.getPropertyLabel())) {
                        String[] split = formBean.getPropertyLabel().split(",");
                        List<JDPicInfo> picInfos = new ArrayList<>();

                        for (int k = 0; k < split.length; k++) {

                            JDPicInfo jdPicInfo = new JDPicInfo();

                            jdPicInfo.setImageUrl(split[k]);
                            jdPicInfo.setIsDownLoad("true");
                            jdPicInfo.setIsAddFlag(0);

                            picInfos.add(jdPicInfo);
                        }
                        if (picInfos != null && picInfos.size() > 0) {

                            formPicMap.put(formBean.getJavaField(), picInfos);
                        }


                    }


                }

            }

            if (formBean.getCgformFieldList() != null && formBean.getCgformFieldList().size() > 0) {

                List<DongTaiFormBean> childList = formBean.getCgformFieldList();

                for (int j = 0; j < childList.size(); j++) {

                    DongTaiFormBean childbean = childList.get(j);

                    if (childbean.getPic() != null && childbean.getPic().size() > 0) {
                        formPicMap.put(childbean.getJavaField(), childbean.getPic());

                    }else {

                        if (childbean.getShowType().equals("image")) {

                            if (!Utils.isEmpty(childbean.getPropertyLabel())) {
                                String[] split = childbean.getPropertyLabel().split(",");
                                List<JDPicInfo> picInfos = new ArrayList<>();

                                for (int k = 0; k < split.length; k++) {

                                    JDPicInfo jdPicInfo = new JDPicInfo();

                                    jdPicInfo.setImageUrl(split[k]);
                                    jdPicInfo.setIsDownLoad("true");
                                    jdPicInfo.setIsAddFlag(0);

                                    picInfos.add(jdPicInfo);
                                }
                                if (picInfos != null && picInfos.size() > 0) {

                                    formPicMap.put(childbean.getJavaField(), picInfos);
                                }


                            }


                        }


                    }



                }

            }

        }

    }



    //递归查找的所有音频文件
    private void soundFile(String strPath, String name) {
        soundList = new ArrayList<String>();
        String filename;//文件名
        String suf;//文件后缀
        File dir = new File(strPath);//文件夹dir
        File[] files = dir.listFiles();//文件夹下的所有文件或文件夹

        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {

            if (files[i].isDirectory()) {
                soundFile(files[i].getAbsolutePath(), name);//递归文件夹！！！

            } else {
                filename = files[i].getName();
                int j = filename.lastIndexOf(".");
                int k = filename.lastIndexOf("-");
                suf = filename.substring(k + 1, j);//得到文件后缀


                if (suf.equalsIgnoreCase(name))//判断是不是后缀的文件
                {
//		                    String strFileName = files[i].getAbsolutePath().toLowerCase();
                    soundList.add(files[i].getName());//对于文件才把它加到list中
                }
            }

        }
    }

    //递归查找的所有图片文件
    private void picFile(String strPath, String name) {
        picList = new ArrayList<>();
        String filename;//文件名
        String suf;//文件后缀
        File dir = new File(strPath);//文件夹dir
        File[] files = dir.listFiles();//文件夹下的所有文件或文件夹
        if (files == null)
            return;

        for (int i = 0; i < files.length; i++) {

            if (files[i].isDirectory()) {
                picFile(files[i].getAbsolutePath(), name);//递归文件夹！！！

            } else {
//		                filename = files[i].getName();
//		                int j = filename.lastIndexOf(".");
//		                suf = filename.substring(j+1);//得到文件后缀
                filename = files[i].getName();
                int j = filename.lastIndexOf(".");
                int k = filename.lastIndexOf("-");
                suf = filename.substring(k + 1, j);//得到文件后缀

//		                if(suf.equalsIgnoreCase("jpg")||suf.equalsIgnoreCase("png"))
                if (suf.equalsIgnoreCase(name))//判断是不是后缀的文件
                {
//		                    String strFileName = files[i].getAbsolutePath().toLowerCase();
//                    picList.add(new JdP);//对于文件才把它加到list中
                }
            }

        }
    }


    private List<ResModelBean> resModelList = new ArrayList<ResModelBean>();


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
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
