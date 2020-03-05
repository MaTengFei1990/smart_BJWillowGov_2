package com.hollysmart.formlib.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.hollysmart.db.ProjectDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.ResModelDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.formlib.activitys.NewAddFormResDataActivity;
import com.hollysmart.formlib.apis.GetNetResListAPI;
import com.hollysmart.formlib.apis.ResDataDeleteAPI;
import com.hollysmart.formlib.apis.ResDataGetAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.bjwillowgov.R;
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

/**
 * Created by Lenovo on 2019/3/7.
 */

public class ResDataManageAdapter extends BaseAdapter {

    private LayoutInflater inflater;

    private List<ResDataBean> mJingDians;
    private List<JDPicInfo> picList; // 当前景点图片集
    private List<String> soundList; // 当前景点录音集

    private String jqId = "";
    private String namestr;
    private Context context;
    private ProjectBean projectBean;
    private List<DongTaiFormBean> formBeanList=new ArrayList<>();// 当前资源的动态表单
    private List<DongTaiFormBean> newFormList;// 当前资源的动态表单

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();

    boolean ischeck =false; //是否只能查看 true  只能查看不能编辑；

    public ResDataManageAdapter(Context context, List<ResDataBean> mJingDians, List<JDPicInfo> picList, List<String> soundList, ProjectBean projectBean,List<DongTaiFormBean> newFormList,boolean ischeck) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.picList = picList;
        this.soundList = soundList;
        this.mJingDians = mJingDians;
        this.namestr = projectBean.getfTaskname();
        this.jqId = projectBean.getId();

        this.projectBean=projectBean;
        this.newFormList=newFormList;
        this.ischeck=ischeck;
        isLogin();
    }

    @Override
    public int getCount() {
        return mJingDians.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup arg2) {

        ViewHolder holder;
        if (convertView != null && convertView.getTag() != null) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = inflater.inflate(R.layout.item_jingqu, null);
            holder = new ViewHolder();
            holder.tv_name =  convertView
                    .findViewById(R.id.tv_jingquName);
            holder.ll_bianji =  convertView
                    .findViewById(R.id.ll_bianji);
            holder.ll_shangchuan =  convertView
                    .findViewById(R.id.ll_shangchuan);
            holder.ll_xiugai =  convertView
                    .findViewById(R.id.ll_xiugai);
            holder.ll_shanchu =  convertView
                    .findViewById(R.id.ll_shanchu);
            holder.tv_bianji =  convertView
                    .findViewById(R.id.tv_bianji);
            holder.ll_fenxiang =  convertView.findViewById(R.id.ll_fenxiang);
            convertView.setTag(holder);
        }
        final TextView mTv_bianji = holder.tv_bianji;
        final LinearLayout mLl_bianji = holder.ll_bianji;
        final LinearLayout ll_fenxiang = holder.ll_fenxiang;
        final LinearLayout ll_shangchuan = holder.ll_shangchuan;


        if (ischeck) {
            mTv_bianji.setVisibility(View.GONE);
            mLl_bianji.setVisibility(View.GONE);
            ll_fenxiang.setVisibility(View.GONE);
            ll_shangchuan.setVisibility(View.GONE);

        }

        holder.tv_name.setText(mJingDians.get(position).getFd_resname());
        mTv_bianji.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mTv_bianji.setVisibility(View.GONE);
                mLl_bianji.setVisibility(View.VISIBLE);
                ll_fenxiang.setVisibility(View.GONE);
                ll_shangchuan.setVisibility(View.GONE);
            }
        });

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                final Intent intent = new Intent(context, NewAddFormResDataActivity.class);


                final String formData = mJingDians.get(position).getFormData();

                if (Utils.isEmpty(formData)) {

                    new ResDataGetAPI(userInfo.getAccess_token(), mJingDians.get(position), new ResDataGetAPI.ResDataDeleteIF() {
                        @Override
                        public void onResDataDeleteResult(boolean isOk, ResDataBean resDataBen) {

                            if (isOk) {
                                String formData = resDataBen.getFormData();
                                formBeanList.clear();

                                try {
                                    JSONObject jsonObject = null;
                                    jsonObject = new JSONObject(formData);
                                    Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                                    List<DongTaiFormBean> oldFormList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                            new TypeToken<List<DongTaiFormBean>>() {}.getType());
                                    List<DongTaiFormBean> comparis = comparis(oldFormList, newFormList,mJingDians.get(position));
                                    formBeanList.addAll(comparis);
                                    getFormPicMap(formBeanList);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                intent.putExtra("formBeanList", (Serializable) formBeanList);
                                intent.putExtra("resDataBean", mJingDians.get(position));
                                intent.putExtra("formPicMap", (Serializable) formPicMap);
                                intent.putExtra("ischeck",  ischeck);
                                Activity activity = (Activity) context;
                                activity.startActivityForResult(intent, 4);

                            }

                        }
                    }).request();

                } else {
                    formBeanList.clear();
                    try {
                        JSONObject jsonObject = null;
                        jsonObject = new JSONObject(formData);
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<DongTaiFormBean> oldFormList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                                new TypeToken<List<DongTaiFormBean>>() {}.getType());
                        List<DongTaiFormBean> comparis = comparis(oldFormList, newFormList,mJingDians.get(position));
                        formBeanList.addAll(comparis);
                        getwgps2bd(formBeanList);
                        getFormPicMap(formBeanList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    intent.putExtra("formBeanList", (Serializable) formBeanList);
                    intent.putExtra("resDataBean", mJingDians.get(position));
                    intent.putExtra("formPicMap", (Serializable) formPicMap);
                    intent.putExtra("ischeck",  ischeck);
                    Activity activity = (Activity) context;
                    activity.startActivityForResult(intent, 4);
                }




            }
        });

        holder.ll_shangchuan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

            }
        });

        holder.ll_xiugai.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                Intent data = new Intent();
                data.putExtra("jdId", mJingDians.get(position).getId()); // 1代表修改
                data.putExtra("index", position);
                data.putExtra("resDataBean", mJingDians.get(position));
                data.putExtra("name", mJingDians.get(position).getFd_resname());
                Activity activity = (Activity) context;
                activity.setResult(1, data); // 1代表修改
                activity.finish();
            }
        });
        holder.ll_shanchu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                delJQ(mJingDians.get(position));
            }
        });
        return convertView;
    }

    class ViewHolder {
        TextView tv_name;
        LinearLayout ll_bianji;
        LinearLayout ll_shangchuan;
        LinearLayout ll_xiugai;
        LinearLayout ll_shanchu;
        LinearLayout ll_fenxiang;
        TextView tv_bianji;
    }


    private void getwgps2bd( List<DongTaiFormBean> formBeanList) {
        for (int i = 0; i < formBeanList.size(); i++) {

            DongTaiFormBean formBean = formBeanList.get(i);

            if (formBean.getJavaField().equals("location")) {

                String propertyLabel = formBean.getPropertyLabel();

                if (!Utils.isEmpty(propertyLabel)) {
                    String[] split = propertyLabel.split(",");

                    GPS gps = GPSConverterUtils.Gps84_To_bd09(Double.parseDouble(split[0]),
                            Double.parseDouble(split[1]));

                    formBean.setPropertyLabel(gps.getLat() + "," + gps.getLon());
                }


            }


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


    private List<DongTaiFormBean> comparis(List<DongTaiFormBean> oldFormList,List<DongTaiFormBean> newFormList, ResDataBean resDataBean) {

        if (oldFormList == null || oldFormList.size() == 0) {

            return null;
        }
        if (newFormList == null || newFormList.size() == 0) {

            return null;
        }

        boolean isNewForm = false;

        for (int s = 0; s < oldFormList.size(); s++) {

            DongTaiFormBean formBean = oldFormList.get(s);

            if (formBean.getJavaField().equals("name")) {
                isNewForm=true;
            }
            if (formBean.getJavaField().equals("number")) {
                isNewForm=true;
            }
            if (formBean.getJavaField().equals("location")) {
                isNewForm=true;
            }
        }


        if (isNewForm) {
            return oldFormList;

        } else {

            for (int i = 0; i < oldFormList.size(); i++) {

                DongTaiFormBean oldBean = oldFormList.get(i);

                if (oldBean.getShowType().equals("list") ) {

                    List<DongTaiFormBean> oldChildList = oldBean.getCgformFieldList();

                    if (oldChildList != null && oldChildList.size() >0) {

                        if ("是".equals(oldBean.getPropertyLabel())) {

                            oldBean.setPropertyLabel("1");
                        }

                        if ("否".equals(oldBean.getPropertyLabel())) {

                            oldBean.setPropertyLabel("0");
                        }
                    }


                }

                for (int j = 0; j < newFormList.size(); j++) {

                    DongTaiFormBean newBean = newFormList.get(j);


                    if (oldBean.getJavaField().equals(newBean.getJavaField())) {

                        newBean.setPropertyLabel(oldBean.getPropertyLabel());


                        if (oldBean.getShowType().equals("list") && newBean.getShowType().equals("switch")) {

                            List<DongTaiFormBean> oldChildList = oldBean.getCgformFieldList();
                            List<DongTaiFormBean> newchildList = newBean.getCgformFieldList();

                            if (oldChildList == null || oldChildList.size() == 0) {
                                break;
                            }
                            if (newchildList == null || newchildList.size() == 0) {
                                break;
                            }

                            for (int k = 0; k < oldChildList.size(); k++) {

                                DongTaiFormBean oldchildBean = oldChildList.get(k);


                                for (int m = 0; m < newchildList.size(); m++) {

                                    DongTaiFormBean newchildBean = newFormList.get(m);


                                    if (oldchildBean.getJavaField().equals(newchildBean.getJavaField())) {

                                        newchildBean.setPropertyLabel(oldchildBean.getPropertyLabel());
                                    }

                                }


                            }

                        }



                        if (oldBean.getShowType().equals("switch") && newBean.getShowType().equals("switch")) {

                            List<DongTaiFormBean> oldChildList = oldBean.getCgformFieldList();
                            List<DongTaiFormBean> newchildList = newBean.getCgformFieldList();

                            if (oldChildList == null || oldChildList.size() == 0) {
                                break;
                            }
                            if (newchildList == null || newchildList.size() == 0) {
                                break;
                            }

                            for (int k = 0; k < oldChildList.size(); k++) {

                                DongTaiFormBean oldchildBean = oldChildList.get(k);


                                for (int m = 0; m < newchildList.size(); m++) {

                                    DongTaiFormBean newchildBean = newchildList.get(m);


                                    if (oldchildBean.getJavaField().equals(newchildBean.getJavaField())) {

                                        newchildBean.setPropertyLabel(oldchildBean.getPropertyLabel());
                                    }

                                }


                            }

                        }


                    }

                    if (resDataBean != null) {

                        if (newBean.getJavaField().equals("number")) {

                            newBean.setPropertyLabel(resDataBean.getRescode());

                        }
                        if (newBean.getJavaField().equals("name")) {
                            newBean.setPropertyLabel(resDataBean.getFd_resname());
                        }
                        if (newBean.getJavaField().equals("location")) {

                            GPS gps = GPSConverterUtils.Gps84_To_bd09(Double.parseDouble(resDataBean.getLatitude()),
                                    Double.parseDouble(resDataBean.getLongitude()));

                            newBean.setPropertyLabel(gps.getLat() + "," + gps.getLon());

                        }
                    }



                }


            }


        }
        oldFormList.clear();
        oldFormList.addAll(newFormList);
        return oldFormList;






    }






    // 删除
    private void delJQ(final ResDataBean deleteBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("删除资源");
        builder.setMessage("确定要删除此资源吗？");
        builder.setPositiveButton("删除", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                delDb(deleteBean);

                new ResDataDeleteAPI(userInfo.getAccess_token(), deleteBean, new ResDataDeleteAPI.ResDataDeleteIF() {
                    @Override
                    public void onResDataDeleteResult(boolean isOk, String msg) {

                        if (isOk) {
                            selectDB(projectBean.getId());
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
    // 查询
        private void selectDB(final String jqId) {
            Mlog.d("jqId = " + jqId);
            mJingDians.clear();

            resModelList.clear();

            String classifyIds = projectBean.getfTaskmodel();
            if (classifyIds != null) {

                String[] ids = classifyIds.split(",");
                ResModelDao resModelDao = new ResModelDao(context);
                for(int i=0;i<ids.length;i++) {
                    ResModelBean resModelBean = resModelDao.getDatById(ids[i]);
                    if (resModelBean != null) {

                        resModelList.add(resModelBean);
                    }
                }


            }

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


                            new GetNetResListAPI(userInfo, projectBean, new GetNetResListAPI.DatadicListIF() {
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

                                                    j = j + 1;

                                                    projectBean.setNetCount(10);
                                                }
                                            }

                                            new ProjectDao(context).addOrUpdate(projectBean);
                                            ProjectBean dataByID = new ProjectDao(context).getDataByID(projectBean.getId());

                                            dataByID.getNetCount();
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
                List<ResDataBean> resDataBeans = resDataDao.getData(jqId + "");
                if (resDataBeans != null && resDataBeans.size() > 0) {

                    mJingDians.addAll(resDataBeans);
                }


                JDPicDao jdPicDao = new JDPicDao(context);
                for (int i = 0; i < mJingDians.size(); i++) {
                    ResDataBean resDataBean = mJingDians.get(i);

                    List<JDPicInfo> jdPicInfoList = jdPicDao.getDataByJDId(resDataBean.getId() + "");

                    resDataBean.setJdPicInfos(jdPicInfoList);

                }


                new GetNetResListAPI(userInfo, projectBean, new GetNetResListAPI.DatadicListIF() {
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

                                        j = j + 1;

                                        projectBean.setNetCount(10);
                                    }
                                }

                                new ProjectDao(context).addOrUpdate(projectBean);
                                ProjectBean dataByID = new ProjectDao(context).getDataByID(projectBean.getId());

                                dataByID.getNetCount();
                            }
                        }


                        notifyDataSetChanged();


                    }
                }).request();
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
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }


}
