package com.android.liveshooter.view;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.android.liveshooter.net.HttpConnector;
import com.android.liveshooter.utils.GIFFrame;

public class GIFView extends View implements Runnable {
	private GIFFrame mGifFrame = null;
	private HttpConnector http;
	private int width;
	private int height;
	
	public GIFView(Context context) {
		super(context);
	}

	public GIFView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void init(String request, int width, int height) {
		http = new HttpConnector(request);
		new Thread(this).start();
		this.width = width;
		this.height = height;
	}
	
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mGifFrame != null) {
			mGifFrame.nextFrame();
			Bitmap b = mGifFrame.getImage();
			if (b != null) canvas.drawBitmap(b, 10, 10, null);
		}
	}

	public void run() {
		mGifFrame = GIFFrame.CreateGifImage(fileConnect(http.getURLResponse()),width,height);
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
			postInvalidate();
		}
	}

	public byte[] fileConnect(InputStream is) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			int ch = 0;
			while ((ch = is.read()) != -1) {
				baos.write(ch);
			}
			byte[] datas = baos.toByteArray();
			baos.close();
			baos = null;
			is.close();
			is = null;
			return datas;
		} catch (Exception e) {
			return null;
		}
	}
}
