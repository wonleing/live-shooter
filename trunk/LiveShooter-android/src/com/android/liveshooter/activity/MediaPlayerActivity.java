package com.android.liveshooter.activity;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue.IdleHandler;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.android.liveshooter.util.Tools;
import com.android.liveshooter.util.VideoView;


public class MediaPlayerActivity extends Activity{

	private VideoView vv;
	
	private SeekBar seekBar;
	
	private TextView timeview;
	
	private ImageButton playBn;
	
	private String videoPath;
	
	private static int screenWidth = 0;
	
	private static int screenHeight = 0;
	
	//private static int controlHeight = 0;  
	
	private View controlView = null;
	
	private PopupWindow controlerWindow = null;
	
	private View loadingView = null;
	
	private PopupWindow loadingWindow = null;
	
	private final static int TIME = 6868; 
	
	public static final int SET_ID = Menu.FIRST;
	
	public static final int CONTROL_ID = Menu.FIRST + 1;
	
	private static String filepath = Tools.getSDDir() + "/kenan.mp4";
	
	//Messages
	private final static int PROGRESS_CHANGED = 0;
	
    private final static int HIDE_CONTROLER = 1;
    
    //Variables
    private boolean isControllerShow = false;    
    
    private boolean isPaused = false;
	
	private Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			switch(msg.what){
			case PROGRESS_CHANGED:
				int i = vv.getCurrentPosition();
				seekBar.setProgress(i);
				/*
				if(isOnline){
					int j = vv.getBufferPercentage();
					seekBar.setSecondaryProgress(j * seekBar.getMax() / 100);
				}else{
					seekBar.setSecondaryProgress(0);
				}
				*/
				
				i/=1000;
				int minute = i/60;
				int hour = minute/60;
				int second = i%60;
				minute %= 60;
				timeview.setText(String.format("%02d:%02d:%02d", hour,minute,second));
				
				//sendEmptyMessageDelayed(PROGRESS_CHANGED, 1000);
				break;
			case HIDE_CONTROLER:
				hideController();
				break;
			}
		}
	};
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.player);
		
		vv = (VideoView)findViewById(R.id.vv);
		/*
		controlView = getLayoutInflater().inflate(R.layout.controler, null);
		controlerWindow = new PopupWindow(controlView);
		*/
		controlView = findViewById(R.id.control);
		playBn = (ImageButton)findViewById(R.id.button1);
		seekBar = (SeekBar)findViewById(R.id.seekbar);
		timeview = (TextView)findViewById(R.id.duration);
		
		loadingView = getLayoutInflater().inflate(R.layout.wait, null);
        loadingWindow = new PopupWindow(loadingView);      
		
		Looper.myQueue().addIdleHandler(new IdleHandler(){

			public boolean queueIdle() {
				
				// TODO Auto-generated method stub
				if (loadingWindow !=null && vv.isShown()){
				    loadingWindow.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
				    loadingWindow.update(0, 0, screenWidth, screenHeight);
				}
				
//				if(controlerWindow != null && vv.isShown()){
//					controlerWindow.showAtLocation(vv, Gravity.BOTTOM, 0, 0);
//					controlerWindow.update(0, 0, screenWidth, controlHeight);
//				}
				
				return false;  
			}
        });
		
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				vv.seekTo(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				handler.removeMessages(HIDE_CONTROLER);
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				handler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
			}
			
		});
		
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			videoPath = bundle.getString("video");
		}
		else {
			videoPath = filepath;
		}
		
		playBn.setAlpha(0xBB);
		playBn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(isPaused){
					vv.start();
					isPaused = false;
					playBn.setImageResource(R.drawable.pause);
				}
				else{
					vv.pause();
					playBn.setImageResource(R.drawable.play);
					isPaused = true;
				}
			}
			
		});
		
		Display display = getWindowManager().getDefaultDisplay();
        screenHeight = display.getHeight();
        screenWidth = display.getWidth();
        //controlHeight = screenHeight/4;
        
        vv.setPreparedListener(new OnPreparedListener(){

			@Override
			public void onPrepared(MediaPlayer mp) {
				if(isControllerShow){
					showController();
				}
				vv.setVideoSize(screenWidth, screenHeight);
				int i = vv.getDuration();
				seekBar.setMax(i);
				i/=1000;
				int minute = i/60;
				int hour = minute/60;
				int second = i%60;
				minute %= 60;
				timeview.setText(String.format("%02d:%02d:%02d", hour,minute,second));
				if(loadingWindow.isShowing()){
					loadingWindow.dismiss();
				}
				vv.start();
				playBn.setImageResource(R.drawable.pause);
				hideControllerDelay();
				handler.sendEmptyMessage(PROGRESS_CHANGED);
			}
        	
        });
        
        vv.setErrorListener(new OnErrorListener(){

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				vv.stopVideo();
				return false;
			}
        	
        });
        
        vv.setOnTouchListener(new OnTouchListener(){

			@Override
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
		
		startPlayer();
	}
	
	private void showController(){
		//controlerWindow.update(0,0,screenWidth, controlHeight);
		controlView.setVisibility(View.VISIBLE);
		isControllerShow = true;
	}
	
	private void startPlayer(){
		vv.openVideo(videoPath);
	}
	
	private void hideControllerDelay(){
		handler.sendEmptyMessageDelayed(HIDE_CONTROLER, TIME);
	}
	
	private void hideController(){
		/*
		if(controlerWindow.isShowing()){
			controlerWindow.update(0,0,0,0);
			isControllerShow = false;
		}
		*/
		if(isControllerShow){
			isControllerShow = false;
			controlView.setVisibility(View.GONE);
		}
	}
	
	private void cancelDelayHide(){
		handler.removeMessages(HIDE_CONTROLER);
	}
	
	/**
	 * 
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);  
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item) {
		return super.onOptionsItemSelected(item);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event){
    	if(keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
    		exitPlayer();
    		return false;
    	}
    	return false;
    }
	
	private void exitPlayer(){
		if(loadingWindow != null && loadingWindow.isShowing()){
			loadingWindow.dismiss();
		}
		if(controlerWindow != null && controlerWindow.isShowing()){
			controlerWindow.dismiss();
		}
		this.finish();
	}
}
