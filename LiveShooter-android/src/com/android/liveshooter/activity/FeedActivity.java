package com.android.liveshooter.activity;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;
import android.widget.TextView;

import com.android.liveshooter.adapter.FeedAdapter;
import com.android.liveshooter.socket.XMLRPCServer;
import com.android.liveshooter.util.GlobalApp;
import com.android.liveshooter.util.MessageParser;
import com.android.liveshooter.vo.Feed;

public class FeedActivity extends Activity {
	
	private List<Feed> feeds;
	
	private ListView feedView;
	
	/**
	 * 消息处理
	 */
	Handler handler = new Handler(){
		
		public void handleMessage(Message msg) {
			if(msg.what == 1){ //点击播放视频
				
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feed);
        
        feedView = (ListView)findViewById(R.id.feeds);
        
        GlobalApp app = (GlobalApp)getApplication();
        String userName = app.getUsername();
        TextView textView = (TextView)findViewById(R.id.ftitle);
        textView.setText(userName);
        int userId = app.getUserId();
        
        //获取用户的Feed信息
        String rpcurl = this.getResources().getString(R.string.rpcurl);
        Object feedobj = new XMLRPCServer(rpcurl).getUserFeed(userId);
        feeds = new MessageParser().parseFeedInfo((String)feedobj);
        if(feeds.size() == 0){
        	//换个显示界面
        }
        
        init();
	}
	
	/**
	 * 初始化数据
	 */
	private void init(){
		/*
		Feed feed = new Feed();
		feed.setTime_ago("22 hours");
		feed.setTime_last("00:23");
		feed.setVideo_title("A Test video");
		feed.setUser_name("jianping");
		if(feeds == null){
			feeds = new ArrayList<Feed>();
		}
		feeds.add(feed);
		*/
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		if(feeds != null){
			for(int i = 0; i < feeds.size(); i++){
				Feed feed = feeds.get(i);
				Map<String, Object> map = new HashMap<String, Object>();
				if(feed.getVideo_poster() == null){
					map.put("videobg", R.drawable.itembg);
				}
				else {
					map.put("videobg", feed.getVideo_poster());
				}
				if(feed.getUser_profile() == null){
					map.put("publish_userprofile", R.drawable.img_person_default);
				}
				else {
					map.put("publish_userprofile", feed.getUser_profile());
				}
				map.put("title", feed.getVideo_title());
				map.put("publish_username", feed.getUser_name());
				map.put("time_ago", " " + feed.getTime_ago() + " ago");
				map.put("time_last", "(" + feed.getTime_last() + ")");
				list.add(map);
			}
		}
		FeedAdapter adapter = new FeedAdapter(FeedActivity.this, handler, list, R.layout.feeditem, new String[]{"videobg", "publish_userprofile", "title", "publish_username", "time_ago", "time_last"}, new int[]{R.id.videobg, R.id.publish_userprofile, R.id.title, R.id.publish_username, R.id.time_ago, R.id.time_last});
    	feedView.setAdapter(adapter);
	}
}
