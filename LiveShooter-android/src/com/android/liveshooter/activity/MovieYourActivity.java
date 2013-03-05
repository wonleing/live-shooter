package com.android.liveshooter.activity;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.android.liveshooter.socket.XMLRPCServer;
import com.android.liveshooter.util.GlobalApp;
import com.android.liveshooter.util.MessageParser;
import com.android.liveshooter.vo.VideoInfo;

public class MovieYourActivity extends Activity {
	
	private List<VideoInfo> videos;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp);
        
        GlobalApp app = (GlobalApp)getApplication();
        String userName = app.getUsername();
        TextView textView = (TextView)findViewById(R.id.ftitle);
        textView.setText(userName);
        int userId = app.getUserId();
        
        String rpcurl = this.getResources().getString(R.string.rpcurl);
        Object obj = new XMLRPCServer(rpcurl).getUserVideo(userId);
        videos = new MessageParser().parseUserVideoInfo((String)obj);
        
        init();
	}
	
	private void init(){
		
	}
}
