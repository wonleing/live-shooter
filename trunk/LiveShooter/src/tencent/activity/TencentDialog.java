package tencent.activity;
/*
 * Copyright 2010 Renren, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 

import org.json.JSONObject;

import tencent.api.User_API;
import tencent.beans.OAuth;
import tencent.utils.OAuthClient;
import tencent.utils.TencentAuthRequestListener;
import tencent.utils.TencentException;
import tencent.utils.TokenStore;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.liveshooter.common.AppApiPreference;
import com.android.liveshooter.service.BlogPreferenceService;

/**
 * 
 * 
 * @author zhuxiangjun@ifuninfo.com
 * 
 */
public class TencentDialog extends Dialog {

	public static final String TENCENT_WEIBO_TAG = "vgongyi://tecent/weibo";
	
	private static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };

	private static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };

	private String mUrl;

	private TencentAuthRequestListener authListener;

	private WebView webView;

	private RelativeLayout content;
	
	private Activity activity;
	
	private OAuth oauth;
	
	private OAuthClient auth;
	
	private ProgressDialog progress;	

	public TencentDialog(Activity activity,TencentAuthRequestListener listener) {
		super(activity);
		this.activity = activity;
		authListener = listener;
		
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		content = new RelativeLayout(getContext());
		content.setGravity(RelativeLayout.CENTER_IN_PARENT);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		progress = new ProgressDialog(getContext());
		progress.setMessage("认证中...");
		progress.setOnKeyListener(new OnKeyListener() {
			
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
			
				if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_SEARCH )
					return true;
				return false;
			}
		});
		
		setUpWebView();

		Display display = getWindow().getWindowManager().getDefaultDisplay();
		float scale = getContext().getResources().getDisplayMetrics().density;
		float[] dimensions = display.getWidth() < display.getHeight() ? DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
		addContentView(content, new FrameLayout.LayoutParams((int) (dimensions[0] * scale + 0.5f), (int) (dimensions[1] * scale + 0.5f)));
		
		requestToken();
	}
	
	private void requestToken(){
		
		oauth = new OAuth(TencentDialog.TENCENT_WEIBO_TAG); // 初始化OAuth请求令牌
		oauth.setOauth_consumer_key(AppApiPreference.TECENT_APP_KEY);
		oauth.setOauth_consumer_secret(AppApiPreference.TECENT_APP_KEY_SECRET);
		
		authListener.onAuthRequestStart();//do something outside
		
		new Thread(){
			
			public void run() {
				try {

					oauth = new OAuthClient().requestToken(oauth);//
					
					if (oauth.getStatus() == 1) {
						
						Log.i("msg","Get Request Token failed!");
						authListener.onAuthRequestError(new TencentException(oauth.getStatus(), oauth.getMsg(), "requestToken", oauth.getMsg()));
						dismiss();
						return;
						
					} else {
						
						String oauth_token = oauth.getOauth_token();
						
						String url = "http://open.t.qq.com/cgi-bin/authorize?oauth_token=" + oauth_token;
					
						webView.loadUrl(url);
					}

				} catch (Exception e) {
					authListener.onAuthRequestError(new TencentException(e.getMessage()));
					dismiss();
				}
			};
		}.start();
		
	}
	private void setUpWebView() {
		webView = new WebView(getContext());
		webView.setVerticalScrollBarEnabled(false);
		webView.setHorizontalScrollBarEnabled(false);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setSaveEnabled(false);
		webView.getSettings().setSaveFormData(false);
		webView.getSettings().setSavePassword(false);
		webView.setWebViewClient(new WebViewClient(){
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				Log.i("info", "...onPageFinished  begin .. "+url);
				if(url.startsWith(TENCENT_WEIBO_TAG)){
					
					//when URL have been loaded , we get oauth_token + oauth_verifier
					auth = new OAuthClient();
					
					view.setVisibility(View.INVISIBLE);	
					Uri uri  =  Uri.parse(url);
					final String oauth_verifier = uri.getQueryParameter("oauth_verifier");	
					final String oauth_token = uri.getQueryParameter("oauth_token");	
					
					new Thread(){
						public void run() {
							getToken(oauth_verifier, oauth_token);		
						};
					}.start();
					
					progress.show();
					
					hide();
					
				}
			}
		});
		
		webView.requestFocus();
		webView.loadUrl(mUrl);//load  null  at  first
		FrameLayout.LayoutParams fill = new FrameLayout.LayoutParams(
				ViewGroup.LayoutParams.FILL_PARENT,
				ViewGroup.LayoutParams.FILL_PARENT);
		webView.setLayoutParams(fill);
		content.addView(webView);
	}
	/**
	 * get token from verifier code
	 * @param oauth_verifier
	 * @param oauth_token
	 */
	public void getToken(String oauth_verifier, String oauth_token) {
		
		
		oauth.setOauth_verifier(oauth_verifier);
		oauth.setOauth_token(oauth_token);

		try {
			oauth = auth.accessToken(oauth);
		} catch (Exception e) {
			authListener.onAuthRequestFault(e);
			TokenStore.clear(activity);
			if(progress!=null&&progress.isShowing())
				progress.dismiss();
			dismiss();
			return;
		}

		if (oauth.getStatus() != 0) {
			authListener.onAuthRequestError(new TencentException(oauth.getStatus(), "Get Access Token failed!", "", oauth.getMsg()));
			TokenStore.clear(activity);
			if(progress!=null&&progress.isShowing())
				progress.dismiss();
			dismiss();
			return;
		} else {	
			
			try{
				BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
				User_API api = new User_API();
					
				String str = api.info(oauth, "json");
				JSONObject obj = new JSONObject(str);
				JSONObject data  = obj.getJSONObject("data");
	
				String nick = data.getString("nick");
				preferenceService.setTencentBlogPreference(oauth.getOauth_token(), oauth.getOauth_token_secret(), nick);
				
				authListener.onAuthRequestComplete(nick);

			}catch (Exception e) {
				authListener.onAuthRequestError(new TencentException("获取用户信息失败！"));
			}finally{
				if(progress!=null&&progress.isShowing())
					progress.dismiss();
				dismiss();
			}
		}
		
	}
	
	
}
