package com.android.liveshooter.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;

public class StringUtil {

	public static String converStreamToString(InputStream is) {
		if(is==null) return null;
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

//	public static String namePicture(String imageUrl,String img_extension) {
//		String url = imageUrl;
//		String suffixes;
//		if(img_extension == ""){
//			suffixes = ".jpg";
//		}else{
//			suffixes = "."+img_extension;
//		}
//		url = url.replace("/", "");
//		url = url.replace(".jpg", "");
//		url = url.replace(".jpeg", "");
//		url = url.replace(".gif", "");
//		url = url.replace(".png", "");
//		url = url.replace(".", "");
//		return url.substring(url.length() - 10) +suffixes;
//	}
	
	public static String namePicture(String imageUrl,String img_extension) {
		String url = imageUrl;
		String suffixes;
		if(img_extension == ""){
			suffixes = ".jpg";
		}else{
			suffixes = "."+img_extension;
		}
//		url = url.replace("/", "");
//		url = url.replace(".jpg", "");
//		url = url.replace(".jpeg", "");
//		url = url.replace(".gif", "");
//		url = url.replace(".png", "");	
//		url = url.replace(".", "");
//		return url.substring(url.length() - 10) +suffixes;
		return url.substring(url.lastIndexOf("/") + 1)+suffixes;
	}

	
	public static String getSpaceTime(String InsertTime){
		long currentTime = System.currentTimeMillis()/1000;	
		long insertTime  = Long.parseLong(InsertTime);
		
		long time = currentTime - insertTime;
		
		if(time >=604800){					
			Calendar calendar=Calendar.getInstance();
		    calendar.setTimeInMillis(insertTime);
		    int month =  calendar.get(Calendar.MONTH)+1;
	        int day = calendar.get(Calendar.DAY_OF_MONTH);		    
		    return month+"月"+day+"日";		    		       		
		}else if(time >=86400){
			int d = (int) (time/86400 +1);
			return d+"天";
		}else if(time >=3600){
			int h = (int) (time/3600 + 1);
		    return h+"小时";
		}else if(time >=60){
			int m = (int) (time/60 + 1);
			return m+"分钟";
		}else{
			return time+"秒";
		}				
	}
}
