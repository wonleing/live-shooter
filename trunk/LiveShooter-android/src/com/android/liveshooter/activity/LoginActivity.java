package com.android.liveshooter.activity;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.liveshooter.socket.XMLRPCServer;
import com.android.liveshooter.util.GlobalApp;
import com.android.liveshooter.util.MessageParser;
import com.android.liveshooter.vo.UserInfo;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;
import com.weibo.net.WeiboParameters;

public class LoginActivity extends Activity implements OnClickListener{
	
	private Button loginBn;
	
	private TextView username;
	
	private TextView password;
	
	// 设置appkey及appsecret，如何获取新浪微博appkey和appsecret请另外查询相关信息，此处不作介绍
	private static final String CONSUMER_KEY = "997501600";// 替换为开发者的appkey，例如"1646212960";
	private static final String CONSUMER_SECRET = "f236cdb4c1fbc8d243ab580c115ac9e1";// 替换为开发者的appkey，例如"94098772160b6f8ffc1315374d8861f9";
	
//	public static Oauth2AccessToken accessToken;
	
	Handler handler = new Handler(){
		
		public void handleMessage(Message msg){
			switch(msg.what){
			case 1:
				break;
			case 2:
				break;
			case 3:
				break;
			case 4:
				break;
			}
		}
		
	};

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.login);
        
        loginBn = (Button)findViewById(R.id.loginBn);
        loginBn.setOnClickListener(this);
        
        username = (TextView)findViewById(R.id.email);
        password = (TextView)findViewById(R.id.pass);

	}

	@Override
	public void onClick(View v) {
		if(v == loginBn){
			Weibo weibo = Weibo.getInstance();
			weibo.setupConsumerConfig(CONSUMER_KEY, CONSUMER_SECRET);

			// Oauth2.0
			// 隐式授权认证方式
			weibo.setRedirectUrl("http://liveshooter.cn.mu/weibo_auth/callback.php");// 此处回调页内容应该替换为与appkey对应的应用回调页
			weibo.authorize(this,
					new AuthDialogListener());
		}
	}
	
	/**
	 * 获取用户信息
	 * @param accessToken
	 * @param uid
	 */
	public void queryUserInfo(String accessToken, String uid){
		 WeiboParameters bundle = new WeiboParameters();
		 bundle.add("uid", uid);
		 bundle.add("access_token", accessToken);
		 bundle.add("source", CONSUMER_KEY);
		 
		 Weibo weibo = Weibo.getInstance();
		 String url = "https://api.weibo.com/2/users/show.json";
		 AccessToken token = new AccessToken(accessToken, CONSUMER_SECRET);
		 try {
			String rlt = weibo.request(this, url, bundle, "GET", token);
			if(rlt != null && rlt.length() > 0){
				UserInfo info = new MessageParser().parseUserInfo(rlt);
				if(info != null){
					login(info.getName(), info.getName(), info.getProfileImageUrl());
				}
			}
		} catch (WeiboException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void login(String snsid, String nickname, String profile){
		String sns = this.getResources().getStringArray(R.array.sns)[0];
		//验证是否成功
		String url = this.getResources().getString(R.string.rpcurl);
		int iret = new XMLRPCServer(url).login(username.getText().toString(), sns, nickname, profile);
		if(iret == 0){
			Toast.makeText(this, "登录失败", Toast.LENGTH_LONG).show();
			return;
		}
		GlobalApp app = (GlobalApp)getApplication();
		app.setSnsId(snsid);
		app.setUsername(nickname);
		app.setUserId(iret);
		app.setLogined(true);
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
	
	class AuthDialogListener implements WeiboDialogListener {

		@Override
		public void onComplete(Bundle values) {
			String token = values.getString("access_token");
			String expires_in = values.getString("expires_in");
			String uid = values.getString("uid");

			AccessToken accessToken = new AccessToken(token, CONSUMER_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);
			
			//获取用户信息
			queryUserInfo(token, uid);
		}

		@Override
		public void onError(DialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			Toast.makeText(getApplicationContext(), "Auth cancel",
					Toast.LENGTH_LONG).show();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}
	
//	class AuthDialogListener implements WeiboAuthListener {
//		
//		public Handler handler;
//		
//		public AuthDialogListener(Handler h){
//			handler = h;
//		}
//
//        @Override
//        public void onComplete(Bundle values) {
//        	//看看是否有用户照片和昵称
//            String token = values.getString("access_token");
//            String expires_in = values.getString("expires_in");
//            LoginActivity.accessToken = new Oauth2AccessToken(token, expires_in);
//            if(LoginActivity.accessToken.isSessionValid()){
//            	AccessTokenKeeper.keepAccessToken(LoginActivity.this, accessToken);
//            	if(handler != null){
//            		Bundle bundle = new Bundle();
//            		bundle.putString("token", token);
//            		Message msg = new Message();
//            		msg.setData(bundle);
//            		msg.what = 1;
//            		handler.sendMessage(msg);
//            	}
//            }
//        }
//
//        @Override
//        public void onError(WeiboDialogError e) {
//        	if(handler != null){
//        		handler.sendEmptyMessage(2);
//        	}
//        }
//
//        @Override
//        public void onCancel() {
//        	if(handler != null){
//        		handler.sendEmptyMessage(3);
//        	}
//        }
//
//        @Override
//        public void onWeiboException(WeiboException e) {
//        	if(handler != null){
//        		handler.sendEmptyMessage(4);
//        	}
//        }
//
//    }
}
