package com.android.liveshooter.activity;

import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

public class MainActivity extends TabActivity {
	
	private TabHost mTabHost;
	
	private void setupTabHost() {
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        setupTabHost();
        
        Intent intent = new Intent(this, FeedActivity.class);
        setupTab(new TextView(this), "Feed", R.drawable.movies_all, intent);
        
        Intent intent1 = new Intent(this, MovieYourActivity.class);
        setupTab(new TextView(this), "Yours", R.drawable.movies_your, intent1);
        
        Intent intent2 = new Intent(this, VideoRecordActivity.class);
        setupTab(new TextView(this), "Camera", R.drawable.camera, intent2);
        
        Intent intent3 = new Intent(this, PeopleActivity.class);
        setupTab(new TextView(this), "People", R.drawable.people, intent3);
        
        Intent intent4 = new Intent(this, SettingActivity.class);
        setupTab(new TextView(this), "Setting", R.drawable.settings, intent4);
        
        mTabHost.setOnTabChangedListener(new OnTabChangeListener(){

			@Override
			public void onTabChanged(String tabId) {
				Log.i("Hello", tabId);
			}
			
		});
    }
    
    private void setupTab(final View view, final String tag, int drawable,Intent intent) {
		View tabview = createTabView(mTabHost.getContext(), tag, drawable);
		TabSpec setContent = mTabHost.newTabSpec(tag).setIndicator(tabview).setContent(intent);
		mTabHost.addTab(setContent);
	}
    
    private static View createTabView(final Context context, final String text,int drawable) {
		View view = LayoutInflater.from(context).inflate(R.layout.tabs_bg, null);
		TextView tv = (TextView) view.findViewById(R.id.tabsText);
		tv.setText(text);
		ImageView iv = (ImageView)view.findViewById(R.id.icon);
		iv.setImageResource(drawable);
		return view;
	}

    
}
