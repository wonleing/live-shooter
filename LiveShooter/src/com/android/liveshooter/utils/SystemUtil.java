package com.android.liveshooter.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.DisplayMetrics;

public class SystemUtil {
	public static String getSystemTime() {
		Date time            = new Date(System.currentTimeMillis());
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String timeString 	 = sdf.format(time);
		return timeString;

	}
	public static boolean checkNetwork(Context context){
		ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = connectivityManager.getActiveNetworkInfo();
		if (info == null && !connectivityManager.getBackgroundDataSetting()) {
			return false;
		} else {
			return true;
		}
	}
	public static int getImageHeightSize(Context context,float height,float width){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		float screenWidth = dm.widthPixels;
//		float screenHeight = dm.heightPixels;
		return Math.round((height/width)*screenWidth);
	}
	public static int getImageWidthSize(Context context){
		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		float screenWidth = dm.widthPixels;
		return Math.round(screenWidth);
	}
	
}
