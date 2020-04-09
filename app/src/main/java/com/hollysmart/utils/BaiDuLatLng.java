package com.hollysmart.utils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

public class BaiDuLatLng {
	public String[] bToG(double lat, double lng) {
		// x = 2 * x1 - x2;
		LatLng LatLng1 = new LatLng(lat, lng);
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(LatLng1);
		LatLng LatLng2 = converter.convert();

		double mlng = 2 * LatLng1.longitude - LatLng2.longitude;
		double mlat = 2 * LatLng1.latitude - LatLng2.latitude;
		String[] latlngs = { mlat + "", mlng + "" };
		return latlngs;
	}

	public double[] bToG_double(double lat, double lng) {
		// x = 2 * x1 - x2;
		LatLng LatLng1 = new LatLng(lat, lng);
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(LatLng1);
		LatLng LatLng2 = converter.convert();

		double mlng = 2 * LatLng1.longitude - LatLng2.longitude;
		double mlat = 2 * LatLng1.latitude - LatLng2.latitude;
		double[] latlngs = { mlat, mlng};
		return latlngs;
	}



	public String[] bToG(LatLng latLng) {
		// x = 2 * x1 - x2;
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(latLng);
		LatLng LatLng2 = converter.convert();
		
		double mlng = 2 * latLng.longitude - LatLng2.longitude;
		double mlat = 2 * latLng.latitude - LatLng2.latitude;
		String[] latlngs = { mlat + "", mlng + "" };
		return latlngs;
	}



	public double[] gaoDeToBaidu(double gd_lon, double gd_lat) {
		double[] bd_lat_lon = new double[2];
		double PI = 3.14159265358979324 * 3000.0 / 180.0;
		double x = gd_lon, y = gd_lat;
		double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * PI);
		double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * PI);
		bd_lat_lon[0] = z * Math.cos(theta) + 0.0065;
		bd_lat_lon[1] = z * Math.sin(theta) + 0.006;
		return bd_lat_lon;
	}





		public LatLng gToB(double lat, double lng){
		LatLng LatLng1 = new LatLng(lat, lng);
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(LatLng1);
		LatLng LatLng2 = converter.convert();
		return LatLng2;
	}
	public static LatLng gToB(LatLng latLng){
		// 将GPS设备采集的原始GPS坐标转换成百度坐标
		CoordinateConverter converter = new CoordinateConverter();
		converter.from(CoordType.GPS);
		// sourceLatLng待转换坐标
		converter.coord(latLng);
		LatLng LatLng2 = converter.convert();
		return LatLng2;
	}
}
