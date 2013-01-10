package com.android.liveshooter.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class LoginActivity extends Activity implements OnClickListener{
	
	private Button loginBn;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.login);
        
        loginBn = (Button)findViewById(R.id.loginBn);
        loginBn.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		if(v == loginBn){
			Intent intent = new Intent(this, MainActivity.class);
			startActivity(intent);
		}
	}
}
