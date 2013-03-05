package com.android.liveshooter.activity;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import com.android.liveshooter.socket.XMLRPCServer;
import com.android.liveshooter.util.RecorderPreview;
import com.android.liveshooter.util.Tools;

public class VideoViewActivity extends Activity implements OnClickListener{
	
	private MediaRecorder videorecorder = null;
	
	private RecorderPreview recorderView;
	
	private int videoWidth = 352, videoHeight = 288;
	
	private int videoFrame = 15;
	
	private ImageButton startBn;
	
	private ImageButton stopBn;
	
	private boolean isControllerShow = false;   
	
    private View bnView;
    
    private String storeVideo = null;
  
    private final static int TIME = 6868; 
	
	private static final int TIME_MSG = 1;
	
	private final static int HIDE_CONTROLER = 2;
	
	Handler handler = new Handler(){
		
		public void handleMessage(Message msg){
			switch(msg.what){
			case TIME_MSG:
				break;
			case HIDE_CONTROLER:
				hideController();
				break;
			}
		}
	};
	
	private void hideController(){
		if(isControllerShow){
			isControllerShow = false;
			bnView.setVisibility(View.GONE);
		}
	}
	
	private void showController(){
		//controlerWindow.update(0,0,screenWidth, controlHeight);
		bnView.setVisibility(View.VISIBLE);
		isControllerShow = true;
	}
	
	private void hideControllerDelay(){
		handler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	private void cancelDelayHide(){
		handler.removeMessages(HIDE_CONTROLER);
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//
        setContentView(R.layout.videoview);
        
        startBn = (ImageButton)findViewById(R.id.startBn);
        stopBn = (ImageButton)findViewById(R.id.stopBn);
        
        startBn.setOnClickListener(this);
        stopBn.setOnClickListener(this);
        
        initVideoRecorder();
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
    		stopRecord();
    		this.finish();
    		return false;
    	}
    	return false;
    }
	
	private void initVideoRecorder(){
		if(videorecorder == null){
			videorecorder = new MediaRecorder();
		}
		else {
			videorecorder.reset();
		}
		//配置视频
		videorecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA); //
		videorecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		videorecorder.setVideoSize(videoWidth, videoHeight); //
		videorecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264); //
		videorecorder.setVideoFrameRate(videoFrame); //
		//配置音频
		videorecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		//videorecorder.setAudioChannels(2);
		videorecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
		
		storeVideo = Tools.getRecordVideoPath();
		videorecorder.setOutputFile(storeVideo);		
		
		FrameLayout preview = (FrameLayout)findViewById(R.id.imageView);
		recorderView = new RecorderPreview(this, videorecorder);
		preview.addView(recorderView);
		
		recorderView.setOnTouchListener(new OnTouchListener(){

        	public boolean onTouch(View view, MotionEvent event) {
				if(!isControllerShow){
					showController();
					hideControllerDelay();
				}else {
					cancelDelayHide();
					hideController();
				}
				return false;
			}
        	
        });
	}
	
	public void startRecord(){
		videorecorder.start();
		hideControllerDelay();
	}
	
	public void stopRecord(){
		if(videorecorder != null){
			videorecorder.stop();
		}
		//生成文件名
		String url = this.getResources().getString(R.string.rpcurl);
		String videoname = new XMLRPCServer(url).getFileName();
		String dir = new File(storeVideo).getParent();
		String newpath = dir + "/" + videoname; //上传文件不需要mp4后缀
		new File(storeVideo).renameTo(new File(newpath));
		//开始上传视频文件
//		UploadDialog dialog = new UploadDialog(this, newpath);
//		dialog.show();
		Intent intent = new Intent(this, UploadActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("path", newpath);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		if(v == startBn){
			startRecord();
		}
		else if(v == stopBn){
			stopRecord();
		}
	}
}
