package com.android.liveshooter.activity;
import java.io.IOException;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;


public class VideoActivity extends Activity implements  SurfaceHolder.Callback{
	
    boolean mMediaRecorderRecording;  
    private SurfaceView mSurfaceView;  
    
    private SurfaceHolder holder;
    private ImageButton btn_start;
    private ImageButton btn_stop;
    private LiveShooterService service;
    
    private boolean isControllerShow = false;    
    private View bnView;
    private final static int HIDE_CONTROLER = 1;
    private final static int TIME = 6868; 
    
    @SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			switch(msg.what){
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
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);  
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);//隐去标题
        setContentView(R.layout.main);  
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surface_camera);  
        btn_start = (ImageButton)this.findViewById(R.id.startBn);
        btn_stop = (ImageButton)this.findViewById(R.id.stopBn);
        holder = mSurfaceView.getHolder();  
        holder.addCallback(this);  
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
        mSurfaceView.setVisibility(View.VISIBLE);  
        mSurfaceView.setOnTouchListener(new OnTouchListener(){

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
        bnView = findViewById(R.id.bns);
        try {
			service = new LiveShooterService(holder, this);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        btn_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				service.startVideoRecording();  
				//service.startVideoUpload();
				isControllerShow = true;
				hideControllerDelay();
			}
		});
        btn_stop.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
					try {
						service.shutdownInput();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}
					android.os.Process.killProcess(android.os.Process.myPid()) ;
			}
		});     
        
    }  
    
    @Override  
    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {  
    }  
    
    @Override  
    public void surfaceCreated(SurfaceHolder holder) {  
    	this.holder = holder;	
		service.prepare();
    }  
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {  
    } 
}
