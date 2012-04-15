package com.android.liveshooter.common;

import android.app.Activity;

import com.android.liveshooter.R;


public class AppApiPreference
{

	
	public static boolean[] isbinds = {false,false,false,false};
	
	public static String[] weibos ;
	
	public static String[] getWeibos(Activity  activity){
		weibos = new String[]{
				  activity.getResources().getString(R.string.common_sina_blog),
				  activity.getResources().getString(R.string.common_tencent_blog),
				  activity.getResources().getString(R.string.common_sohu_blog),
				  activity.getResources().getString(R.string.common_wangyi_blog)};
		return weibos;
	}
	
	public static String[] weiboPlatform = {"sina","qq","sohu","netease"};
	
	public static boolean AD_ALWAYS_SHOW 	 = false;
	public static String  RATING_URL 		 = "";
	public static String  LATEST_BLOG_URL    = "http://test.ci77.com/weigongyi/";
   
	public static String SINA_APP_KEY          = "2503068724";//2503068724
	public static String SINA_APP_KEY_SECRET   = "3737b217357212704e6c985f6f78a329";	//3737b217357212704e6c985f6f78a329
		
	public static String TECENT_APP_KEY        = "801118385";
	public static String TECENT_APP_KEY_SECRET = "dc19bfb2df9b13cadfbe24f1c5a14d90";
	
	public static String SOHO_APP_KEY         = "bxBMm0SlWwdioDu65uNq";
	public static String SOHO_APP_KEY_SECRET  = "6$wGn0H)DS7MYTLl!kdHhlFQYaSxQMn5-4iF#ciS";
	
	public static String WANGYI_APP_KEY        = "Ix02JsiT6MbzAY6t";
	public static String WANGYI_APP_KEY_SECRET = "ev3qWp8jDavL0VWhjk4ZSEYn6e6PoMLv";
	
	
}
