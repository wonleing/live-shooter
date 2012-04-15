package wangyi;
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



import wangyi.data.User;
import wangyi.http.AccessToken;
import wangyi.http.RequestToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.android.liveshooter.common.AppApiPreference;
import com.android.liveshooter.service.BlogPreferenceService;

public class WangyiDialog extends Dialog {

	public static final String T163_WEIBO_TAG = "http://api.t.163.com/oauth/authenticate?username";
														
	private static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };

	private static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };

	private String mUrl;

	private WangyiAuthRequestListener authListener;

	private WebView webView;

	private RelativeLayout content;
	
	private Activity activity;
	
	private ProgressDialog progress;	
	private TBlog tblog;  
	private RequestToken requestToken;
	private String verifyURL; 

	public WangyiDialog(Activity activity,WangyiAuthRequestListener listener) {
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
		
		new Thread(){
			
			public void run() { 
				try {
					
					// 设置 consumer key, consumer secret：
					// 也可以在 t4j.properties 中设置，这个文件应当放置在：源代码目录的根目录
					System.setProperty("tblog4j.oauth.consumerKey",AppApiPreference.WANGYI_APP_KEY);
			    	System.setProperty("tblog4j.oauth.consumerSecret", AppApiPreference.WANGYI_APP_KEY_SECRET);
					
			    	// 暂时把debug关了。减少干扰信息
			    	System.setProperty("tblog4j.debug", "false");
			    	
					tblog = new TBlog();
					
					requestToken = tblog.getOAuthRequestToken();
					
					verifyURL = requestToken.getAuthenticationURL();
		             
					webView.loadUrl(verifyURL);

				} catch (Exception e) {
					authListener.onAuthRequestError(new TBlogException(e.getMessage()));
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
		webView.setWebViewClient(new WebViewClient(){

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				super.onPageStarted(view, url, favicon);
				Log.i("info", "onPageStarted :" + url);
				if(url.startsWith(T163_WEIBO_TAG)){
					
					try {
						
						progress.show();
						
						view.setVisibility(View.INVISIBLE);
						
						hide();
						
		            	AccessToken accessToken = tblog.getOAuthAccessToken(requestToken);
		            	
		            	final String token  = accessToken.getToken();
		            	final String token_secret = accessToken.getTokenSecret();
		            	
		        		new Thread(){
		        			public void run() {
		        				getToken(token, token_secret);
		        			};
		        		}.start();
		        		
		            } catch (TBlogException e) {
		            	progress.dismiss();
		            	dismiss();
		            	authListener.onAuthRequestError(e);
		            }        

					
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
	public void getToken(String token, String secret) {
			
			
	try{
			tblog.setToken(token, secret);
			User user = tblog.verifyCredentials();
			
			if(TextUtils.isEmpty(user.getName()))
				authListener.onAuthRequestError(new TBlogException("获取用户信息失败！"));
			else{
				BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
				preferenceService.setWangyitBlogPreference(token,secret, user.getName());
				authListener.onAuthRequestComplete(user.getName());
			}
				

	}catch (TBlogException e) {
			authListener.onAuthRequestError(new TBlogException(e));
	}finally{
		
		CookieSyncManager.createInstance(activity);   
		CookieManager cookieManager = CookieManager.getInstance();  
		cookieManager.removeAllCookie();  
		
		if(progress!=null&&progress.isShowing())
			progress.dismiss();
		dismiss();
	}
		
	}
	
	
	
}
