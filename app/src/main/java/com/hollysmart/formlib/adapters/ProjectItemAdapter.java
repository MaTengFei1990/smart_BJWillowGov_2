package com.hollysmart.formlib.adapters;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.d.lib.slidelayout.SlideLayout;
import com.d.lib.slidelayout.SlideManager;
import com.d.lib.xrv.adapter.CommonAdapter;
import com.d.lib.xrv.adapter.CommonHolder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.hollysmart.apis.UpLoadFormPicAPI;
import com.hollysmart.apis.UpLoadSoundAPI;
import com.hollysmart.beans.JDPicInfo;
import com.hollysmart.beans.LuXianInfo;
import com.hollysmart.beans.PointInfo;
import com.hollysmart.beans.SoundInfo;
import com.hollysmart.bjwillowgov.R;
import com.hollysmart.db.JDPicDao;
import com.hollysmart.db.JDSoundDao;
import com.hollysmart.db.LuXianDao;
import com.hollysmart.db.ProjectDao;
import com.hollysmart.db.ResDataDao;
import com.hollysmart.db.UserInfo;
import com.hollysmart.dialog.LoadingProgressDialog;
import com.hollysmart.formlib.activitys.ResDataManageActivity;
import com.hollysmart.formlib.apis.RestaskDeleteAPI;
import com.hollysmart.formlib.apis.SaveResDataAPI;
import com.hollysmart.formlib.apis.SaveResTaskAPI;
import com.hollysmart.formlib.beans.DongTaiFormBean;
import com.hollysmart.formlib.beans.ProjectBean;
import com.hollysmart.formlib.beans.ResDataBean;
import com.hollysmart.tools.JSONTool;
import com.hollysmart.tools.KMLTool;
import com.hollysmart.utils.PicYasuo;
import com.hollysmart.utils.Utils;
import com.hollysmart.utils.taskpool.OnNetRequestListener;
import com.hollysmart.utils.taskpool.TaskPool;
import com.hollysmart.utils.zip.XZip;
import com.hollysmart.value.Values;
import com.qiniu.android.common.FixedZone;
import com.qiniu.android.http.ResponseInfo;
import com.qiniu.android.storage.Configuration;
import com.qiniu.android.storage.UpCompletionHandler;
import com.qiniu.android.storage.UploadManager;
import com.qiniu.util.Auth;
import com.umeng.commonsdk.statistics.common.MLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hollysmart.formlib.activitys.ProjectManagerActivity.ADD_RESDATA_FLAG;

/**
 * Created by Lenovo on 2019/4/9.
 */

public class ProjectItemAdapter extends CommonAdapter<ProjectBean> {
    private SlideManager manager;

    private List<PointInfo> pointInfos=new ArrayList<>();

    private UserInfo userInfo;

    private LoadingProgressDialog lpd;


    private boolean longClickState = false;

    private ProjectDao projectDao;

    private Context context;

    boolean move=false;

    private long downTime= 0;
    private List<ProjectBean> projectBeanList;

    private HashMap<String, List<JDPicInfo>> formPicMap = new HashMap<>();




    // 普通布局
    private final int TYPE_ITEM = 1;
    // 脚布局
    private final int TYPE_FOOTER = 2;
    // 当前加载状态，默认为加载完成
    private int loadState = 2;
    // 正在加载
    public final int LOADING = 1;
    // 加载完成
    public final int LOADING_COMPLETE = 2;
    // 加载到底
    public final int LOADING_END = 3;

    public LongclickListener longclickListener;
    public RefreshDataChangeListener refreshDataChangeListener;


    public ProjectItemAdapter(Context context, UserInfo userInfo, LoadingProgressDialog lpd, List<ProjectBean> datas, int layoutId) {
        super(context, datas, layoutId);
        this.context=context;
        this.userInfo=userInfo;
        this.projectBeanList=datas;
        this.lpd=lpd;
        manager = new SlideManager();
        projectDao = new ProjectDao(mContext);
    }

    public boolean isLongClickState() {
        return longClickState;
    }

    public void setLongClickState(boolean longClickState) {
        this.longClickState = longClickState;
    }


    public LongclickListener getLongclickListener() {
        return longclickListener;
    }

    public void setLongclickListener(LongclickListener longclickListener) {
        this.longclickListener = longclickListener;
    }

    public RefreshDataChangeListener getRefreshDataChangeListener() {
        return refreshDataChangeListener;
    }

    public void setRefreshDataChangeListener(RefreshDataChangeListener refreshDataChangeListener) {
        this.refreshDataChangeListener = refreshDataChangeListener;
    }

    @Override
    public int getItemViewType(int position) {
        return TYPE_ITEM;
    }

    @Override
    public int getItemCount() {
        if (projectBeanList.size() == 0) {
            return 0;
        } else {

            return projectBeanList.size();
        }

    }

    @Override
    public CommonHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == TYPE_FOOTER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_footer, parent, false);
            return new FootViewHolder(view);
        } else {
            return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(CommonHolder holder, int position) {

        if (holder instanceof FootViewHolder) {
            FootViewHolder footViewHolder = (FootViewHolder) holder;
            switch (loadState) {
                case LOADING: // 正在加载
                    footViewHolder.pbLoading.setVisibility(View.VISIBLE);
                    footViewHolder.tvLoading.setVisibility(View.VISIBLE);
                    footViewHolder.ll_loading.setVisibility(View.VISIBLE);
                    footViewHolder.llEnd.setVisibility(View.GONE);
                    break;

                case LOADING_COMPLETE: // 加载完成
                    footViewHolder.pbLoading.setVisibility(View.INVISIBLE);
                    footViewHolder.tvLoading.setVisibility(View.INVISIBLE);
                    footViewHolder.ll_loading.setVisibility(View.INVISIBLE);
                    footViewHolder.llEnd.setVisibility(View.GONE);
                    break;

                case LOADING_END: // 加载到底
                    footViewHolder.pbLoading.setVisibility(View.GONE);
                    footViewHolder.tvLoading.setVisibility(View.GONE);
                    footViewHolder.ll_loading.setVisibility(View.GONE);
                    footViewHolder.llEnd.setVisibility(View.VISIBLE);
                    break;

                default:
                    break;
            }
        } else {
            super.onBindViewHolder(holder, position);
        }




    }

    @Override
    public void convert(final int position, final CommonHolder holder, final ProjectBean item) {
        holder.setText(R.id.tv_projectTitle, item.getfTaskname());
        holder.setText(R.id.tv_startTime, item.getfBegindate());
        holder.setText(R.id.tv_endTime, item.getfEnddate());
        if ("1".equals(item.getfState())) {
            holder.setText(R.id.tv_state, "待办");
            holder.setImageResource(R.id.iv_state, R.mipmap.icon_daiban);
        }
        if ("2".equals(item.getfState())) {
            holder.setText(R.id.tv_state, "进行中");
            holder.setImageResource(R.id.iv_state, R.mipmap.icon_jinxingzhong);
        }
        if ("3".equals(item.getfState())) {
            holder.setText(R.id.tv_state, "已完成");
            holder.setImageResource(R.id.iv_state, R.mipmap.icon_wancheng);
        }


//        if (!Utils.isEmpty(userInfo.getRestaskDelete()) && userInfo.getRestaskDelete().equals("restask:delete")) {
//            holder.getView(R.id.tv_delete).setVisibility(VISIBLE);
//        } else {
//            holder.getView(R.id.tv_delete).setVisibility(GONE);
//        }
//
//        if (!Utils.isEmpty(userInfo.getRestaskFinish()) && userInfo.getRestaskFinish().equals("restask:finish")) {
//            holder.getView(R.id.tv_finish).setVisibility(VISIBLE);
//        } else {
//            holder.getView(R.id.tv_finish).setVisibility(GONE);
//        }


        holder.setText(R.id.tv_allCount, "总数量:"+item.getAllConunt()+"条");
        holder.setText(R.id.tv_syncCount, "已同步:"+item.getSyncCount()+"条");


        final SlideLayout slSlide = holder.getView(R.id.sl_slide);
        slSlide.setOpen(item.isOpen, false);

        if (longClickState) {
            (holder.getView(R.id.iv_gouxuankuang)).setVisibility(View.VISIBLE);
        } else {
            (holder.getView(R.id.iv_gouxuankuang)).setVisibility(View.GONE);
        }

        if (item.isSelect()) {
            ((ImageView)holder.getView(R.id.iv_gouxuankuang)).setImageResource(R.mipmap.xuanzhong);
        } else {
            ((ImageView)holder.getView(R.id.iv_gouxuankuang)).setImageResource(R.mipmap.gouxuankuang);
        }

        slSlide.setOnStateChangeListener(new SlideLayout.OnStateChangeListener() {
            @Override
            public void onChange(SlideLayout layout, boolean isOpen) {
                item.isOpen = isOpen;
                manager.onChange(layout, isOpen);
            }

            @Override
            public boolean closeAll(SlideLayout layout) {
                return manager.closeAll(layout);
            }
        });


        holder.getView(R.id.sl_slide).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    downTime = System.currentTimeMillis();

                    MLog.d("ACTION_DOWN time=="+downTime);

                }

                if (event.getAction() == MotionEvent.ACTION_UP) {

                    long upTime = System.currentTimeMillis();

                    MLog.d("ACTION_UP time=="+upTime);

                    long l = upTime - downTime;
                    MLog.d("ACTION_UP -ACTION_DOWN time=="+l);

                    if ((upTime - downTime) > 2000) {
                        if (move) {
                            move = false;
                            return false;

                        } else {

                            if (!item.isOpen) {
                                manager.closeAll(slSlide);
                                notifyDataSetChanged();
                                longClickState = true;
                                if (longclickListener != null) {
                                    longclickListener.longclick();
                                }

                            }
                        }

                    }


                }


                if (event.getAction() == MotionEvent.ACTION_MOVE) {

                    move=true;

                }


                return false;


            }
        });

        // 同步
        holder.setViewOnClickListener(R.id.tv_tongbu, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(mContext, NewAddProjectActivity.class);
//                intent.putExtra("projectBean", item);
//                intent.putExtra("editFlag", 2);
//                mContext.startActivity(intent);
            }
        });


        // 上传七牛云平台
        holder.setViewOnClickListener(R.id.tv_upload, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("确认上传项目？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        lpd.setMessage("上传中...");
                        lpd.show();

                        KMLTool kmlTool = new KMLTool(mContext);
                        biaodian(item);
                        if (kmlTool.createKML(item.getId(),
                                Values.SDCARD_FILE(Values.SDCARD_FILE) + "/"
                                        + item.getfTaskname(), item.getfTaskname(), pointInfos)) {
                            try {
                                XZip.ZipFolder(
                                        Values.SDCARD_FILE(Values.SDCARD_FILE)
                                                + "/"
                                                + item.getfTaskname(),
                                        Values.SDCARD_FILE(Values.SDCARD_FILE)
                                                + "/"
                                                + item.getfTaskname()
                                                + ".zip");
                            } catch (Exception e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            if (new JSONTool(mContext).createJSON(
                                    item.getId(),
                                    item.getfTaskname(),
                                    item.getCreateDate(),
                                    Values.SDCARD_FILE(Values.SDCARD_FILE) + "/"
                                            + item.getfTaskname())) {
                                Toast.makeText(mContext, "导出成功", Toast.LENGTH_LONG)
                                        .show();

                            } else {
                                Toast.makeText(mContext, "JSON导出失败",
                                        Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(mContext, "KML导出失败", Toast.LENGTH_LONG)
                                    .show();
                        }

                        File file = new File(Values.SDCARD_FILE(Values.SDCARD_FILE)
                                + "/" + item.getfTaskname() + ".zip");
                        System.out.println("file " + file.exists());
                        if (file.exists()) {
                            upLoadToqiNiu(file);

                        } else {
                            Utils.showToast(mContext,"分享失败，文件不存在");
                        }

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();












            }
        });






        // 删除
        holder.setViewOnClickListener(R.id.tv_delete, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final List<ProjectBean> delList = new ArrayList<>();
                delList.add(mDatas.get(position));
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("是否删除该项目？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        slSlide.close();

                        new RestaskDeleteAPI(userInfo.getAccess_token(), delList, new RestaskDeleteAPI.RestaskDeleteIF() {
                            @Override
                            public void onRestaskDeleteResult(boolean isOk, String msg) {

                                if (isOk) {
                                    lpd.cancel();
                                    projectDao.deletByProjectId(mDatas.get(position).getId());
                                    mDatas.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mDatas.size());
                                } else {
                                    Utils.showDialog(mContext,"项目删除失败");
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
                builder.create().show();




            }
        });

        // 分享
        holder.setViewOnClickListener(R.id.tv_share, new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast toast = Toast.makeText(mContext,
                        "正在打包分享中……", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                LinearLayout toastView = (LinearLayout) toast.getView();
                ImageView imageCodeProject = new ImageView(
                        mContext);
                imageCodeProject.setImageResource(R.drawable.ic_launcher_background);
                toastView.addView(imageCodeProject, 0);
                toast.show();

                KMLTool kmlTool = new KMLTool(mContext);
                biaodian(item);
                if (kmlTool.createKML(item.getId(),
                        Values.SDCARD_FILE(Values.SDCARD_FILE) + "/"
                                + item.getfTaskname(), item.getfTaskname()+".kml", pointInfos)) {
                    try {
                        XZip.ZipFolder(
                                Values.SDCARD_FILE(Values.SDCARD_FILE)
                                        + "/"
                                        + item.getfTaskname(),
                                Values.SDCARD_FILE(Values.SDCARD_FILE)
                                        + "/"
                                        + item.getfTaskname()
                                        + ".zip");
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    if (new JSONTool(mContext).createJSON(
                            item.getId(),
                            item.getfTaskname(),
                            item.getCreateDate(),
                            Values.SDCARD_FILE(Values.SDCARD_FILE) + "/"
                                    + item.getfTaskname())) {
                        Toast.makeText(mContext, "导出成功", Toast.LENGTH_LONG)
                                .show();

                    } else {
                        Toast.makeText(mContext, "JSON导出失败",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(mContext, "KML导出失败", Toast.LENGTH_LONG)
                            .show();
                }

                Intent share = new Intent(Intent.ACTION_SEND);
                File file = new File(Values.SDCARD_FILE(Values.SDCARD_FILE)
                        + "/" + item.getfTaskname() + ".zip");
                System.out.println("file " + file.exists());
                if (file.exists()) {
                    share.putExtra(Intent.EXTRA_STREAM, getFileUri(mContext, file));
                    share.setType("*/*");
                    Activity activity = (Activity) mContext;
                    activity.startActivity(Intent.createChooser(share, "发送"));

                } else {
                    Utils.showToast(mContext,"分享失败，文件不存在");
                }

                slSlide.close();
            }
        });


        // 完成
        holder.setViewOnClickListener(R.id.tv_finish, new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("确认完成该项目？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        item.setfState("3");

                        new SaveResTaskAPI(userInfo.getAccess_token(), item, new SaveResTaskAPI.SaveResTaskIF() {
                            @Override
                            public void onSaveResTaskResult(boolean isOk, ProjectBean projectBean) {
                                if (isOk) {
                                    Utils.showDialog(mContext,"项目已完成");

                                    if (refreshDataChangeListener != null) {
                                        refreshDataChangeListener.refreshDataChange();
                                    }
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
                builder.create().show();



            }
        });

        // 同步
        holder.setViewOnClickListener(R.id.tv_tongbu, new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context);
                builder.setTitle("确认同步该项目？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        submit();


                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create().show();






            }
        });

        holder.setViewOnClickListener(R.id.sl_slide, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slSlide.isOpen()) {
                    slSlide.close();
                    return;
                }

                if (longClickState) {
                    if (!item.isSelect()) {
                        ((ImageView)holder.getView(R.id.iv_gouxuankuang)).setImageResource(R.mipmap.xuanzhong);
                        item.setSelect(true);
                    } else {
                        ((ImageView)holder.getView(R.id.iv_gouxuankuang)).setImageResource(R.mipmap.gouxuankuang);
                        item.setSelect(false);
                    }

                } else {

                    if (slSlide.isOpen()) {
                        slSlide.close();
                    } else {
                        Intent intent = new Intent(mContext, ResDataManageActivity.class);
                        intent.putExtra("projectName", item.getfTaskname());
                        intent.putExtra("projectId", item.getId());
                        intent.putExtra("classifyIds", item.getfTaskmodel());
                        intent.putExtra("projectBean", item);

                        Activity activity = (Activity) mContext;
                        activity.startActivityForResult(intent, ADD_RESDATA_FLAG);
                    }

                }




            }
        });
    }


    /***
     * 同步
     */

    private void submit() {

        final ResDataDao formDao = new ResDataDao(mContext);
        JDPicDao jdPicDao = new JDPicDao(mContext);
        JDSoundDao jdSoundDao = new JDSoundDao(mContext);
        final TaskPool taskPool = new TaskPool();

        OnNetRequestListener listener= new OnNetRequestListener() {
            @Override
            public void onFinish() {
                lpd.cancel();
            }

            @Override
            public void OnNext() {
                taskPool.execute(this);
            }

            @Override
            public void OnResult(boolean isOk, String msg, Object object) {
                if (isOk) {
                    ResDataBean bean = (ResDataBean) object;
                    if (bean != null) {
                        formDao.addOrUpdate(bean);
                        taskPool.execute(this);

                        if (refreshDataChangeListener != null) {
                            refreshDataChangeListener.refreshDataChange();
                        }


                    }
                } else {
                    Utils.showToast(mContext,"同步失败");
                }
            }
        };

        List<ResDataBean> formBeens = formDao.getUnUpLoadDataList();

        if (formBeens != null && formBeens.size() == 0) {
            Utils.showToast(mContext,"暂无未同步的资源");
            return;
        }
        for (ResDataBean bean : formBeens) {

            List<JDPicInfo> picList = jdPicDao.getDataByJDId(bean.getId());

            for (JDPicInfo jdPicInfo : picList) {

                if (!Utils.isEmpty(jdPicInfo.getFilePath()) && Utils.isEmpty(jdPicInfo.getImageUrl())) {

                    taskPool.addTask(new PicYasuo(jdPicInfo, mContext,listener));
                }
            }
            for (JDPicInfo jdPicInfo : picList) {

                if (!Utils.isEmpty(jdPicInfo.getFilePath())&& Utils.isEmpty(jdPicInfo.getImageUrl()))
                    taskPool.addTask(new UpLoadFormPicAPI(userInfo.getAccess_token(), jdPicInfo, listener));
            }
            List<SoundInfo> soundInfoList = jdSoundDao.getDataByJDId(bean.getId());

            for (SoundInfo soundInfo : soundInfoList) {

                if (!Utils.isEmpty(soundInfo.getFilePath())&& Utils.isEmpty(soundInfo.getAudioUrl()))
                    taskPool.addTask(new UpLoadSoundAPI(userInfo.getAccess_token(), soundInfo, listener));
            }

            bean.setPic(picList);
            bean.setAudio(soundInfoList);

            getFormPicMap(bean);


            taskPool.addTask(new SaveResDataAPI(userInfo.getAccess_token(), bean,formPicMap, listener));



        }
        lpd.setMessage("正在同步资源，请稍等。。。");
        lpd.show();
        taskPool.execute(listener);

    }


    private void getFormPicMap(ResDataBean resDataBean) {

        String formData = resDataBean.getFormData();

        List<DongTaiFormBean> formBeanList = new ArrayList<>();

        try {
            JSONObject jsonObject = null;
            jsonObject = new JSONObject(formData);
            Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
            List<DongTaiFormBean> dictList = mGson.fromJson(jsonObject.getString("cgformFieldList"),
                    new TypeToken<List<DongTaiFormBean>>() {}.getType());
            formBeanList.addAll(dictList);
            getFormPicMap(formBeanList);
        } catch (JSONException e) {
            e.printStackTrace();
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




    /**
     * 上传到七牛
     */
    public void upLoadToqiNiu(File file) {


        String token = getToken();


        Configuration config = new Configuration.Builder()
                .zone(FixedZone.zone0)
                .build();
        UploadManager uploadManager = new UploadManager(config);
//        String token = <从服务端SDK获取>;
        uploadManager.put(file, file.getName(), token,
                new UpCompletionHandler() {
                    @Override
                    public void complete(String key, ResponseInfo info, JSONObject res) {
                        //res包含hash、key等信息，具体字段取决于上传策略的设置
                        if(info.isOK()) {
                            Utils.showDialog(mContext,"上传成功！");
                            lpd.cancel();
                        } else {
                            Log.i("qiniu", "Upload Fail");
                            Utils.showDialog(mContext,"上传失败！");
                            lpd.cancel();
                            //如果失败，这里可以把info信息上报自己的服务器，便于后面分析上传错误原因
                        }
                        Log.i("qiniu", key + ",\r\n " + info + ",\r\n " + res);
                    }
                }, null);





    }

    /**
     * 获取上传的token;
     */

    private String getToken() {


        String AccessKey = "28fPX_YqJodo-uikTzYZWDv0koVKcqvx1erzEHks";
        String SecretKey = "iZZJ7B1WFJ-Ulsry6xASIX6nL2itJAC7CkWQd-Mj";

        Auth auth = Auth.create(AccessKey, SecretKey);

        String token = auth.uploadToken("caidian");

        return token;


    }



    public class FootViewHolder extends CommonHolder {

        ProgressBar pbLoading;
        TextView tvLoading;
        LinearLayout llEnd;
        LinearLayout ll_loading;

        FootViewHolder(View itemView) {
            super(mContext, itemView, R.layout.item_footer);
            ll_loading = (LinearLayout) itemView.findViewById(R.id.ll_loading);
            pbLoading = (ProgressBar) itemView.findViewById(R.id.pb_loading);
            tvLoading = (TextView) itemView.findViewById(R.id.tv_loading);
            llEnd = (LinearLayout) itemView.findViewById(R.id.ll_end);
        }
    }

    /**
     * 设置上拉加载状态
     *
     * @param loadState 0.正在加载 1.加载完成 2.加载到底
     */
    public void setLoadState(int loadState) {
        this.loadState = loadState;
        notifyDataSetChanged();
    }





    private void biaodian(ProjectBean item) {
        File file = new File(Values.SDCARD_FILE(Values.SDCARD_FILE) + "/"
                + item.getfTaskname()+"/"
                + item.getfTaskname());


        if(file.exists()){

            file.delete();

        }

        LuXianDao luXianDao = new LuXianDao(mContext);
        List<LuXianInfo> listData = luXianDao.getData(item.getId() + "");

        try {
            ArrayList<PointInfo> pointInfoss = new ArrayList<PointInfo>();
            if (listData != null && listData.size() > 0) {


                for(int i=0;i<listData.size();i++) {
                    String luxianstr = listData.get(i).getName();
                    if (!Utils.isEmpty(luxianstr)) {
                        Gson mGson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm").create();
                        List<PointInfo> xianlulist = mGson.fromJson(luxianstr, new TypeToken<List<PointInfo>>() {
                        }.getType());
                        pointInfoss.addAll(xianlulist);
                    }


                }

            }
            pointInfos.addAll(pointInfoss);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }





    public static Uri getFileUri(Context context, File file){
        Uri uri;
        // 低版本直接用 Uri.fromFile
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            uri = Uri.fromFile(file);
        }else {
            uri = FileProvider.getUriForFile(context,"com.hollysmart.smart_newcaidian.fileprovider",file);

            ContentResolver cR = context.getContentResolver();
            if (uri != null && !TextUtils.isEmpty(uri.toString())) {
                String fileType = cR.getType(uri);
                // 使用 MediaStore 的 content:// 而不是自己 FileProvider 提供的uri，不然有些app无法适配
                if (!TextUtils.isEmpty(fileType)){
                    if (fileType.contains("video/")){
                        uri = getVideoContentUri(context, file);
                    }else if (fileType.contains("image/")){
                        uri = getImageContentUri(context, file);
                    }else if (fileType.contains("audio/")){
                        uri = getAudioContentUri(context, file);
                    }
                }
            }
        }
        return uri;
    }




    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context
     * @param imageFile
     * @return content Uri
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID }, MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context
     * @param videoFile
     * @return content Uri
     */
    public static Uri getVideoContentUri(Context context, File videoFile) {
        String filePath = videoFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Video.Media._ID }, MediaStore.Video.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/video/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (videoFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Video.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    /**
     * Gets the content:// URI from the given corresponding path to a file
     *
     * @param context
     * @param audioFile
     * @return content Uri
     */
    public static Uri getAudioContentUri(Context context, File audioFile) {
        String filePath = audioFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Audio.Media._ID }, MediaStore.Audio.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/audio/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (audioFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Audio.Media.DATA, filePath);
                return context.getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }


    public interface LongclickListener{
        void longclick();
    }


    public interface RefreshDataChangeListener{
        void refreshDataChange();
    }
}
