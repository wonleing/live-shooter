package com.android.liveshooter.util;

import java.io.IOException;

import android.content.Context;
import android.media.MediaRecorder;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

public class RecorderPreview extends SurfaceView implements SurfaceHolder.Callback{
	
	private MediaRecorder recorder;
	
	private SurfaceHolder mHolder;

	public RecorderPreview(Context context, MediaRecorder recorder) {
		super(context);
		this.recorder = recorder;
		mHolder = getHolder();
		mHolder.addCallback(this);
		this.setVisibility(View.VISIBLE);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		if(mHolder.getSurface() == null){
			return;
		}
		mHolder = holder;	
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mHolder = holder;	
		recorder.setPreviewDisplay(holder.getSurface());
		try {
			recorder.prepare();
			//recorder.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		mHolder = null;
		recorder = null;
	}

}
