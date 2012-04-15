package com.android.liveshooter.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.android.liveshooter.common.AppConstant;

public class FileUtil {
//	private static final String TAG = "FileUtil";
	public static Bitmap readFile (String filePath,Context context) throws Exception{
		 FileInputStream is 		= context.openFileInput(filePath);
		 BitmapFactory.Options opts = new BitmapFactory.Options();
         opts.inSampleSize = 15;
		 return  BitmapFactory.decodeStream(is);
	}
	
	public static void cleanImage(ArrayList<String> list) throws IOException{
		File folder = new File(AppConstant.IMAGE_PATH);
		File[] files = folder.listFiles();
		for(int i = 0;i < files.length;i++){
			if(!list.contains(files[i].getCanonicalPath())){
				files[i].delete();
			}
		}
	}
}
