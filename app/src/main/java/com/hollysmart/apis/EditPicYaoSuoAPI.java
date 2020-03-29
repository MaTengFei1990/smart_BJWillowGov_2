package com.hollysmart.apis;

import android.content.Context;

import com.hollysmart.beans.PicBean;
import com.hollysmart.utils.taskpool.INetModel;
import com.hollysmart.utils.taskpool.OnNetRequestListener;
import com.hollysmart.value.Values;

import java.io.File;

import top.zibin.luban.Luban;
import top.zibin.luban.OnCompressListener;

public class EditPicYaoSuoAPI implements INetModel {

    private PicBean  info;
    private String filePath;
    private Context context;
    private OnNetRequestListener onNetRequestListener;

    public EditPicYaoSuoAPI(PicBean info, Context context, OnNetRequestListener onNetRequestListener ) {
        this.info = info;
        this.context = context;
        this.onNetRequestListener = onNetRequestListener;
    }


    @Override
    public void request() {
        String str[] = info.getPath().split("/");
        String picName = str[str.length-1];


        Luban.with(context)
                .load(info.getPath())
                .ignoreBy(100)
                .setTargetDir(Values.SDCARD_FILE(Values.SDCARD_PIC))
                .setCompressListener(new OnCompressListener() {
                    @Override
                    public void onStart() {
                        // TODO 压缩开始前调用，可以在方法内启动 loading UI
                    }

                    @Override
                    public void onSuccess(File file) {
                        // TODO 压缩成功后调用，返回压缩后的图片文件
                        info.setPath(file.getPath());
                        onNetRequestListener.OnNext();
                    }

                    @Override
                    public void onError(Throwable e) {
                        // TODO 当压缩过程出现问题时调用
                        onNetRequestListener.OnNext();
                    }
                }).launch();

    }


}

