package com.hollysmart.value;

import android.Manifest;
import android.os.Environment;


public class Values {

//	public static final String SERVIXE_SHOUYE = "http://xds.bjylfw.cn/xdsapp";     // 北京绿地  正式
//	public static final String SERVICE_URL = "http://xds.bjylfw.cn/xdsapi/";
//	public static final String SERVICE_URL_PC = "http://xds.bjylfw.cn:8014/";         //北京绿地  正式


	public static final String SERVIXE_SHOUYE = "http://10.2.9.165:8081/xx.html";     // 北京绿地 测试
	public static final String SERVICE_URL = "http://10.2.9.165:8081/";     		  //北京绿地 测试
	public static final String SERVICE_URL_PC = "http://10.2.9.152:8668/";            //北京绿地 测试

	//集成表单需要的接口；
//	public static final String SERVICE_URL_FORM = "http://xds.bjylfw.cn";// 北京绿地 正式
	public static final String SERVICE_URL_FORM = "http://49.4.11.238:3012";//北京绿地 测试
	public static final String SERVICE_URL_ADMIN_FORM = SERVICE_URL_FORM + "/admin";//

	public static final String SERVICE_URL_VOICE = "39.106.172.189";//
	public static final int SERVICE_URL_VOICE_PORT =7080;//

	public static final String SDCARD_ROOT = "smart_greenSpace";
	public static final String RELOAD_DATA = "reload_data";
	public static final String SDCARD_FUJIAN = "fujian";
	public static final String SDCARD_PIC = "pic";
	public static final String SDCARD_PIC_WODE = SDCARD_PIC + "/wode";//我的
	public static final String SDCARD_DIR = Environment.getExternalStorageDirectory().toString() + "/" +SDCARD_ROOT + "/";

	public static String SDCARD_FILE(String FILENAME) {
		return SDCARD_DIR + FILENAME + "/";
	}
	public static final String CACHE_USER = "user";
	public static final String CACHE_USERINFO = "userInfo";
	public static final String CACHE_FUJIAN = "fuJianCache";
	public static final String CACHE_FUJIAN_IDS = "fuJianIds";

	public static final String CACHE_FRIST = "Frist";
	public static final String CACHE_ISFRIST = "isFrist";

	public static final String CACHE_SECOND = "Second";
	public static final String CACHE_ISECOND = "isSecond";

	public static final String CACHE_THRID = "Thrid";
	public static final String CACHE_ISTHRID = "isThrid";

	public static String QRCODEURL = "qrcode";



	public static final String SDCARD_CACHE = "cache";


	public static final String SDCARD_ICON = "icon";
	public static final String SDCARD_DOWNLOAD = "download";
	public static final String SDCARD_SOUNDS = "sounds";
	public static final String SDCARD_FILE = "file";
	public static final String SDCARD_LUXIAN = "luxian";

	public static final String[] PERMISSION = new String[]{Manifest.permission.CAMERA,
			Manifest.permission.WRITE_EXTERNAL_STORAGE,// 写入权限
			Manifest.permission.READ_EXTERNAL_STORAGE, // 读取权限
			Manifest.permission.READ_PHONE_STATE,
			Manifest.permission.VIBRATE,
			Manifest.permission.INTERNET
	};



	public static final String SUCCESS = "com.hollysmart.success.jinrong";
	public static final String EXITLOGIN = "com.hollysmart.ExitApp";
}


















