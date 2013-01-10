package com.android.liveshooter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class VideoRecordActivity extends Activity implements OnClickListener{
	
	private ImageButton enterBn;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.video_record);
        
        enterBn = (ImageButton)findViewById(R.id.enterBn);
        enterBn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == enterBn){
			Intent intent = new Intent(this, VideoViewActivity.class);
			startActivity(intent);
		}
	}
}
