package com.hollysmart.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


public class GetAssetsFiles {


    /**
     * 获取assets目录下的图片
     *
     * @param context
     * @param fileName
     * @return
     */
    public static Bitmap getImageFromAssetsFile(Context context, String fileName) {
        Bitmap image = null;
        AssetManager am = context.getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return image;
    }

    /**
     * 获取assets目录下的单个文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static File getFileFromAssetsFile(Context context, String fileName) {//这种方式不能用，只能用于webview加载，直接取路径是不行的

        String path = "file:///android_asset/" + fileName;
        File file = new File(path);
        return file;

    }

    /**
     * 获取所有文件
     *
     * @param path
     * @return
     */
    public static String[] getfilesFromAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list(path);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        for (String str : files) {
//            LogUtils.logInfoStar(str);
        }
        return files;

    }

    /**
     * 将assets下的文件放到sd指定目录下
     *
     * @param context    上下文
     * @param assetsPath assets下的路径
     * @param sdCardPath sd卡的路径
     */
    public static void putAssetsToSDCard(Context context, String assetsPath,
                                         String sdCardPath) {
        try {
            String mString[] = context.getAssets().list(assetsPath);
            if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件
                InputStream mIs = context.getAssets().open(assetsPath); // 读取流
                byte[] mByte = new byte[1024];
                int bt = 0;

                File sdfile = new File(sdCardPath);
                sdfile.createNewFile();
                FileOutputStream fos = new FileOutputStream(sdfile); // 写入流
                while ((bt = mIs.read(mByte)) != -1) { // assets为文件,从文件中读取流
                    fos.write(mByte, 0, bt);// 写入流到文件中
                }
                fos.flush();// 刷新缓冲区
                mIs.close();// 关闭读取流
                fos.close();// 关闭写入流

            } else { // 当mString长度大于0,说明其为文件夹
                sdCardPath = sdCardPath + File.separator + assetsPath;
                File file = new File(sdCardPath);
                if (!file.exists())
                    file.mkdirs(); // 在sd下创建目录
                for (String stringFile : mString) { // 进行递归
                    putAssetsToSDCard(context, assetsPath + File.separator
                            + stringFile, sdCardPath);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





//    public boolean CopyFiles(Context mContext,String oldPath, String newPath) throws IOException {
//        boolean isCopy = true;
//        AssetManager mAssetManger = mContext.getAssets();
//        String[] fileNames=mAssetManger.list(oldPath);// 获取assets目录下的所有文件及有文件的目录名
//
//        if (fileNames.length > 0) {//如果是目录,如果是具体文件则长度为0
//            File file = new File(newPath);
//            file.mkdirs();//如果文件夹不存在，则递归
//            for (String fileName : fileNames) {
//                if(oldPath=="")   //assets中的oldPath是相对路径，不能够以“/”开头
//                    CopyFiles(fileName,newPath+"/"+fileName);
//                else
//                    CopyFiles(oldPath+"/"+fileName,newPath+"/"+fileName);
//            }
//        }else {//如果是文件
//            InputStream is = mAssetManger.open(oldPath);
//            FileOutputStream fos = new FileOutputStream(new File(newPath));
//            byte[] buffer = new byte[1024];
//            int byteCount=0;
//            while((byteCount=is.read(buffer))!=-1) {//循环从输入流读取 buffer字节
//                fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
//            }
//            fos.flush();//刷新缓冲区
//            is.close();
//            fos.close();
//        }
//        return isCopy;
//    }





    /**
     * 将asset中的数据库文件拷贝到 databases目录下
     */
    public static boolean copyAssetAndWrite(Context context, String fileName){
        try {

            String path = context.getFilesDir().getPath() +"/" +fileName;

            File localfile = new File(path);

            if (!localfile.exists()){
                boolean res=localfile.createNewFile();
                if (!res){
                    return false;
                }
            }else {
                if (localfile.length()>10){//表示已经写入一次
                    return true;
                }
            }
            InputStream is=context.getAssets().open(fileName);
            FileOutputStream fos = new FileOutputStream(localfile);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
