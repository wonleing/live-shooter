package com.android.liveshooter.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageProcess {

	/**
	 * 获取远程图片
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public Bitmap getBitmap(String url) throws IOException{
		Bitmap bitmap = null;
		if(url.startsWith("http")){
			URL imageurl = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) imageurl.openConnection();
			conn.connect();
			InputStream is = conn.getInputStream();
			bitmap = BitmapFactory.decodeStream(is);
			is.close();
		}
		else{
			BitmapFactory.Options opts = new BitmapFactory.Options();
		    opts.inSampleSize = 4;
			bitmap = BitmapFactory.decodeFile(url, opts);
		}
		return bitmap;
	}
	
	
}
