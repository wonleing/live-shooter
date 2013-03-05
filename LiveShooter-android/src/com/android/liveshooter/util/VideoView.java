package com.android.liveshooter.util;


import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.MediaController;
import android.widget.MediaController.MediaPlayerControl;

public class VideoView extends SurfaceView implements MediaPlayerControl{
	
	private MediaPlayer player;
	
	private Context ctx;
	
	private SurfaceHolder mSurfaceHolder = null;
	
	private MediaController mController;
	
	private int mVideoWidth;
	
    private int mVideoHeight;
    
    private int mSurfaceWidth;
    
    private int mSurfaceHeight;
    
    private boolean mPrepared = false;
    
    private boolean mStartWhenPrepared;
    
    private int mSeekWhenPrepared;
    
    private Uri  mUri;
    
    private MediaPlayer.OnPreparedListener mOnPreparedListener = new MediaPlayer.OnPreparedListener() {
		
		@Override
		public void onPrepared(MediaPlayer mp) {
			mPrepared = true;
			if(mController != null) {
				mController.setEnabled(true);
			}
			if(preparedListener != null){
				preparedListener.onPrepared(player);
			}
			mVideoWidth = mp.getVideoWidth();
	        mVideoHeight = mp.getVideoHeight();
	        if(mVideoWidth != 0 && mVideoHeight != 0){
	        	getHolder().setFixedSize(mVideoWidth, mVideoHeight);
	        	if (mSurfaceWidth == mVideoWidth && mSurfaceHeight == mVideoHeight) {
	        		if (mSeekWhenPrepared != 0) {
                        player.seekTo(mSeekWhenPrepared);
                        mSeekWhenPrepared = 0;
                    }
                    if (mStartWhenPrepared) {
                    	player.start();
                        mStartWhenPrepared = false;
                        if (mController != null) {
                        	mController.show();
                        }
                    } else if (!isPlaying() &&
                            (mSeekWhenPrepared != 0 || getCurrentPosition() > 0)) {
                       if (mController != null) {
                    	   mController.show(0);
                       }
                   }
	        	}
	        }
	        else {
	        	if (mSeekWhenPrepared != 0) {
                    player.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                if (mStartWhenPrepared) {
                	player.start();
                    mStartWhenPrepared = false;
                }
	        }
		}
	}; 
    
    private MediaPlayer.OnCompletionListener mOnCompletionListener = new MediaPlayer.OnCompletionListener() {
		
		@Override
		public void onCompletion(MediaPlayer mp) {
			if (mController != null) {
				mController.hide();
            }
		}
	}; 
	
	private MediaPlayer.OnVideoSizeChangedListener mSizeChangedListener =
	        new MediaPlayer.OnVideoSizeChangedListener() {
        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
            mVideoWidth = mp.getVideoWidth();
            mVideoHeight = mp.getVideoHeight();
            
            if (mVideoWidth != 0 && mVideoHeight != 0) {
            	getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            }
        }
};
	
	private MediaPlayer.OnErrorListener mOnErrorListener;
	
	private MediaPlayer.OnPreparedListener preparedListener;
	
	public void setErrorListener(MediaPlayer.OnErrorListener errorlistener){
		mOnErrorListener = errorlistener;
	}
	
	public void setPreparedListener(MediaPlayer.OnPreparedListener preparedlistener){
		preparedListener = preparedlistener;
	}
    
    private SurfaceHolder.Callback mCallback = new SurfaceHolder.Callback() {
		
		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			mSurfaceHolder = null;
			if(mController != null) {
				mController.hide();
				mController.destroyDrawingCache();
				mController = null;
			}
			if(player != null) {
				player.reset();
				player.release();
				player = null;
			}
		}
		
		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			mSurfaceHolder = holder;
			initPlayer();
		}
		
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			mSurfaceWidth = width;
            mSurfaceHeight = height;
            /*
            if (player != null && mPrepared && mVideoWidth == width && mVideoHeight == height) {
                if (mSeekWhenPrepared != 0) {
                	player.seekTo(mSeekWhenPrepared);
                    mSeekWhenPrepared = 0;
                }
                player.start();
                if (mController != null) {
                	mController.show();
                }
            }
            */
		}
	};
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        setMeasuredDimension(width,height);
    }
	
	public VideoView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        ctx = context;
		initVideo();
    }

    public VideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        ctx = context;
		initVideo();
    }
	
	public void setVideoSize(int width, int height){
		LayoutParams lp = getLayoutParams();
    	lp.height = height;
		lp.width = width;
		setLayoutParams(lp);
	}
	
	private void initVideo(){
		mVideoWidth = 0;
		mVideoHeight = 0;
		this.getHolder().addCallback(mCallback);
		this.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		this.setFocusable(true);
		this.setFocusableInTouchMode(true);
		this.requestFocus();
	}
	
	public void openVideo(String url){
		if(url == null || url.length() == 0){
			return;
		}
		setVideoUri(Uri.parse(url));
	}
	
	public void setVideoUri(Uri uri){
		mStartWhenPrepared = false;
        mSeekWhenPrepared = 0;
        mUri = uri;
        if(mSurfaceHolder != null){
        	initPlayer();
        }
        requestLayout();
        invalidate();
	}
	
	public void initPlayer(){
		if(player != null) {
			player.reset();
			player.release();
			player = null;
		}
		try {
			player = new MediaPlayer();
			mPrepared = false;
			//
			player.setDisplay(mSurfaceHolder);
			//
			player.setOnPreparedListener(mOnPreparedListener);
			player.setOnCompletionListener(mOnCompletionListener);
			player.setOnErrorListener(mOnErrorListener);
			player.setOnVideoSizeChangedListener(mSizeChangedListener);
			player.setAudioStreamType(AudioManager.STREAM_MUSIC);
			player.setScreenOnWhilePlaying(true);
			player.setDataSource(ctx, mUri); 
			player.prepareAsync();			
			attachMediaController();
		} catch(Exception e){
			
		}
	}
	
	public void stopVideo(){
		if(player != null){
			player.stop();
			player.release();
			player = null;
		}
	}
	
	private void attachMediaController(){
		if(player != null && mController != null) {
			mController.setMediaPlayer(this);
			View anchorView = this.getParent() instanceof View ?
                    (View)this.getParent() : this;
            mController.setAnchorView(anchorView);
			mController.setEnabled(mPrepared);
		}
	}
	
	public void touchMediaControlVisible(){
		if(mController.isShowing()) {
			mController.hide();
		}
		else {
			mController.show();
		}
	}
	
	/**
	 * 
	 */
	public boolean onTouchEvent(MotionEvent ev) {
		if (mPrepared && player != null && mController != null) {
			touchMediaControlVisible();
        }
        return false;
	}

	@Override
	public boolean canPause() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekBackward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canSeekForward() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int getBufferPercentage() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getCurrentPosition() {
		if(player != null && mPrepared){
			return player.getCurrentPosition();
		}
		return 0;
	}

	@Override
	public int getDuration() {
		if(player != null && mPrepared){
			return player.getDuration();
		}
		return 0;
	}

	@Override
	public boolean isPlaying() {
		if(player != null && mPrepared){
			return player.isPlaying();
		}
		return false;
	}

	

	@Override
	public void seekTo(int sec) {
		if (player != null && mPrepared) {
            player.seekTo(sec);
        } else {
            mSeekWhenPrepared = sec;
        }
	}

	@Override
	public void start() {
		if(player != null && mPrepared) {
			player.start();
			mStartWhenPrepared = false;
		}
		else {
			mStartWhenPrepared = true;
		}
	}
	
	@Override
	public void pause() {
		if(player != null && mPrepared) {
			if(player.isPlaying()) {
				player.pause();
			}
		}
		mStartWhenPrepared = false;
	}
}
