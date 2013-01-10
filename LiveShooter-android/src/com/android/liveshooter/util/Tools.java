package com.android.liveshooter.util;

import java.io.File;
import java.io.FileFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.os.Environment;

public class Tools {
	
	public static String getSDDir(){
		return Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	public static String getPhotoDir(){
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videodemo/photo";
		return dir;
	}
	
	public static String getVideoDir(){
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videodemo/video";
		return dir;
	}
	
	public static String getStoreVideo(){
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videodemo/video/";
		if(!new File(dir).exists()){
			new File(dir).mkdirs();
		}
		return dir + "test.h264";
	}
	
	public static String getMp4file(){
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videodemo/video/";
		if(!new File(dir).exists()){
			new File(dir).mkdirs();
		}
		return dir + "video" + formatTime(new Date()) + ".mp4";
	}
	
	public static String getCameraPhoto(){
		if(!new File(getPhotoDir()).exists()){
			new File(getPhotoDir()).mkdirs();
		}
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + "/videodemo/photo/" + System.currentTimeMillis() + ".jpg";
		return dir;
	}
	
	public static List<String> getAllPhotos(){
		String dir = getPhotoDir();
		File file = new File(dir);
		if(!file.exists()){
			return null;
		}
		List<String> result = new ArrayList<String>();
		File[] files = file.listFiles();
		for(int i = 0; i < files.length; i++){
			result.add(files[i].getAbsolutePath());
		}
		return result;
	}
	
	public static List<String> getAllVideos(){
		String dir = getVideoDir();
		File file = new File(dir);
		if(!file.exists()){
			return null;
		}
		List<String> result = new ArrayList<String>();
		File[] files = file.listFiles(new FileFilter(){

			@Override
			public boolean accept(File pathname) {
				if(pathname.getName().endsWith("mp4")){
					return true;
				}
				return false;
			}
			
		});
		for(int i = 0; i < files.length; i++){
			result.add(files[i].getAbsolutePath());
		}
		return result;
	}
	
	public static String formatTime(Date date){
    	return new SimpleDateFormat("yyyyMMddHHmmss").format(date);
    }
	
	public static String currentTime(Date date){
    	return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
	
	public static String getProjectDir(){
		String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/live-shooter/";
		if(!new File(path).exists()){
			new File(path).mkdirs();
		}
		return path;
	}
	
	public static String getRecordVideoPath(){
		long time = Calendar.getInstance().getTimeInMillis();
		return getProjectDir() + time + ".mp4";
	}
	
	public static void invertArray(byte[] arr){
		int len = arr.length;
		for(int i = 0; i < arr.length/2; i++){
			byte c = arr[i];
			arr[i] = arr[len - 1 - i];
			arr[len - 1 - i] = c;
		}
	}
	
	/**
	 * translate the byte array into integer
	 * @param date
	 * @return
	 */
	public static int bytes2int(byte[] data, int len){
		int invertLen = data.length > 4 ? 4 : data.length;
		invertArray(data, invertLen);
		byte[] dest = new byte[4];
		if(len == 1){
			dest[0] = data[0];
			dest[1] = dest[2] = dest[3] = 0;
		}
		else if(len == 2){
			dest[2] = dest[3] = 0;
			System.arraycopy(data, 0, dest, 0, 2);
		}
		else
			System.arraycopy(data, 0, dest, 0, 4);
		int result = 0;
		for(int i = 0; i < 4; i++){
		   result += (dest[i] & 0xFF)<<(8*i);
		}
		if(len == 1 && (dest[0] & 0x80) != 0){
			return result - 256;
		}
		if(len == 2 && (dest[1]&0x80) != 0)
			return result - 65536;
	    return result;
	}

	/**
	 * translate the integer into byte array with specified length
	 * @param data
	 * @param number
	 * @return
	 */
	public static byte[] int2byte(int data,int number){
		if(number == 4){
			byte[] dest = new byte[4];
			dest[3] = (byte)((data>>24)&0xFF);
			dest[2] = (byte)((data>>16)&0xFF);
			dest[1] = (byte)((data>>8)&0xFF);
			dest[0] = (byte)(data&0xFF);
			invertArray(dest, 4);
			return dest;
		}
		else{
			byte[] dest = new byte[2];
			dest[1] = (byte)((data>>8)&0xFF);
			dest[0] = (byte)(data&0xFF);
			invertArray(dest, 2);
			return dest;
		}
	}
	
	/**
	 * Invert the array
	 * @param data
	 */
	private static void invertArray(byte[] data, int len){
		if(data == null)
			return;
		int n = len;
		for(int i = 0; i < n/2; i++){
			byte temp = data[i];
			data[i] = data[n - 1 - i];
			data[n - 1 - i] = temp;
		}
	}
	
	public static byte[] float2bytes(float adatata){
		int tem=Float.floatToIntBits(adatata);
		byte[] result=new byte[4];
		result[3]=(byte) ((tem>>>24)&0xFF);
		result[2]=(byte) ((tem>>>16)&0xFF);
		result[1]=(byte) ((tem>>>8)&0xFF);
		result[0]=(byte) ((tem>>>0)&0xFF);
		return result;		
	}
	
	public static byte[] double2bytes(double data){
		long tem=Double.doubleToLongBits(data);
		byte[] result=new byte[8];
		result[7]=(byte) ((tem>>>56)&0xFF);
		result[6]=(byte) ((tem>>>48)&0xFF);
		result[5]=(byte) ((tem>>>40)&0xFF);
		result[4]=(byte) ((tem>>>32)&0xFF);
		result[3]=(byte) ((tem>>>24)&0xFF);
		result[2]=(byte) ((tem>>>16)&0xFF);
		result[1]=(byte) ((tem>>>8)&0xFF);
		result[0]=(byte) ((tem>>>0)&0xFF);
		return result;
	}
	
	public static byte[] intToByte(int number) { 
        int temp = number; 
        byte[] b = new byte[4]; 
        for (int i = 0; i < b.length; i++) { 
            b[i] = new Integer(temp & 0xff).byteValue();// 将最低位保存在最低位 
            temp = temp >> 8; // 向右移8位 
        } 
        return b; 
    } 
}
