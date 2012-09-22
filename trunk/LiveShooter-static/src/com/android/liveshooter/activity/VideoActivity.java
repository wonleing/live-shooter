package com.android.liveshooter.activity;
import java.io.IOException;
import android.app.Activity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.liveshooter.R;

public class VideoActivity extends Activity implements  SurfaceHolder.Callback{
    boolean mMediaRecorderRecording;  
    private SurfaceView mSurfaceView;  
    private Button btn_start;
    private Button btn_stop;
    private LiveShooterService service;
    
    @Override  
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.main);  
        mSurfaceView = (SurfaceView) this.findViewById(R.id.surface_camera);  
        btn_start = (Button)this.findViewById(R.id.surface_btn_start);
        btn_stop = (Button)this.findViewById(R.id.surface_btn_stop);
        SurfaceHolder holder = mSurfaceView.getHolder();  
        holder.addCallback(this);  
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);  
        mSurfaceView.setVisibility(View.VISIBLE);  
        try {
			service = new LiveShooterService(holder);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
        btn_start.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				service.startVideoRecording();  
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
    }  
    @Override  
    public void surfaceDestroyed(SurfaceHolder holder) {  
    }  
}
