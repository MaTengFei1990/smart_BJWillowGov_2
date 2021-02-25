package com.hollysmart.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * @Title: BaseApplication.java
 * @Copyright: roushengxian
 * @Description: SharedPreferenced工具类
 * @author: zhenghuan
 * @data: 2017年12月12日
 * @version: V2.0
 */

public class SharedPreferencedUtils {

    //islogin    boolean    是否登陆
    //token      String     用户token
    //isFirst    boolean    判断用户是否首次进入  用于区分是否展示欢迎页

    //address    String     详细地址
    //district   String     区域信息（区）
    //city       String     市
    //province   String     省
    //road       String     路
    //street     String     街道
    //aoiName    String     aoi 名称
    //y          String     longitude经度
    //x          String     latitude纬度

    //id         String     用户唯一ID
    //account    String     登陆账号
    //nickname   String     昵称
    //photo      String     头像
    //openid     String     微信OpenId
    //invite     String     用户邀请人编号


    public static SharedPreferences mPreference;

    public static SharedPreferences getPreference(Context context) {
        if (mPreference == null)
            mPreference = PreferenceManager
                    .getDefaultSharedPreferences(context);
        return mPreference;
    }

    public static void setInteger(Context context, String name, int value) {
        getPreference(context).edit().putInt(name, value).commit();
    }

    public static int getInteger(Context context, String name, int default_i) {
        return getPreference(context).getInt(name, default_i);
    }

    /**
     * 设置字符串类型的配置
     */
    public static void setString(Context context, String name, String value) {
        getPreference(context).edit().putString(name, value).commit();
    }

    public static String getString(Context context, String name) {
        return getPreference(context).getString(name, null);
    }

    /**
     * 获取字符串类型的配置
     */
    public static String getString(Context context, String name, String defalt) {
        return getPreference(context).getString(name, defalt);
    }

    /**
     * 获取boolean类型的配置
     *
     * @param context
     * @param name
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(Context context, String name, boolean defaultValue) {
        return getPreference(context).getBoolean(name, defaultValue);
    }

    /**
     * 设置boolean类型的配置
     *
     * @param context
     * @param name
     * @param value
     */
    public static void setBoolean(Context context, String name, boolean value) {
        getPreference(context).edit().putBoolean(name, value).commit();
    }

    public static void setFloat(Context context, String name, Float value) {
        getPreference(context).edit().putFloat(name, value).commit();
    }

    public static Float getFloat(Context context, String name, Float value) {
        return getPreference(context).getFloat(name, 0);
    }

    public static void setLong(Context context, String name, Long value) {
        getPreference(context).edit().putLong(name, value).commit();
    }

    public static Long getLong(Context context, String name, Long defaultValue) {
        return getPreference(context).getLong(name, defaultValue);
    }


}
