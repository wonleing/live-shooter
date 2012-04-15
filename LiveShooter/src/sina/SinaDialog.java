package sina;
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


import sina.http.AccessToken;
import sina.http.RequestToken;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
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

public class SinaDialog extends Dialog {

	public static final String SINA_WEIBO_TAG = "vgongyi://sina/weibo";
														
	private static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };

	private static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };

	private String mUrl;

	private SinaAuthRequestListener authListener;

	private WebView webView;

	private RelativeLayout content;
	
	private Activity activity;
	
	private ProgressDialog progress;	

	
	public SinaDialog(Activity activity,SinaAuthRequestListener listener) {
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
					
					CookieSyncManager.createInstance(activity);   
					CookieManager cookieManager = CookieManager.getInstance();  
					cookieManager.removeAllCookie();  
					
					
					System.setProperty("weibo4j.oauth.consumerKey", AppApiPreference.SINA_APP_KEY);
			    	System.setProperty("weibo4j.oauth.consumerSecret", AppApiPreference.SINA_APP_KEY_SECRET);
			    	
		              
					Weibo weibo = new Weibo();
					
	            	RequestToken requestToken =weibo.getOAuthRequestToken(SINA_WEIBO_TAG);
		    		OAuthConstant.getInstance().setRequestToken(requestToken);
					String url = requestToken.getAuthenticationURL()+ "&display=mobile";
					webView.loadUrl(url);
					
					

				} catch (Exception e) {
					authListener.onAuthRequestError(new SinaBlogException(e.getMessage()));
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
			
			private int index = 0;
			
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				super.onPageStarted(view, url, favicon);
		        
		        /**
		         *  url.contains(ConfigUtil.callBackUrl)
		         *  如果授权成功url中包含之前设置的callbackurl
		         *  		包含：授权成功
		         *
		         *index == 0
		         *由于该方法onPageStarted可能被多次调用造成重复跳转
		         *		则添加此标示
		         */
		        Log.i("msg","url = " + url);
		        if( url.startsWith(SINA_WEIBO_TAG)){
		        	try{
		        		hide();
		        		progress.show();
						view.setVisibility(View.INVISIBLE);

						Uri uri  =  Uri.parse(url);
						final String oauth_verifier = uri.getQueryParameter("oauth_verifier");	
						final String oauth_token = uri.getQueryParameter("oauth_token");

						new Thread(){
							
							public void run() {
								
								getToken(oauth_verifier, oauth_token);
								
							};
						}.start();
		        	}
		        	catch(Exception e){
		            	Log.i("info", "onPageFinished Exceptions :" + e.getMessage());
		            	if(progress!=null&&progress.isShowing())
		            		progress.dismiss();
		            	dismiss();
		        		authListener.onAuthRequestError(new SinaBlogException(e));
		        	}
		        	
	           }

					
			}
			
		});
		
		webView.requestFocus();
		webView.loadUrl(mUrl);//load  null  at  first
		webView.setSaveEnabled(false);
		webView.getSettings().setSaveFormData(false);
		webView.getSettings().setSavePassword(false);
		
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
			
			
	try{
			RequestToken requestToken= OAuthConstant.getInstance().getRequestToken();
			AccessToken accessToken=requestToken.getAccessToken(oauth_verifier);
			OAuthConstant.getInstance().setAccessToken(accessToken);
	
			Weibo weibo = OAuthConstant.getInstance().getWeibo();
			weibo.setToken(OAuthConstant.getInstance().getToken(), OAuthConstant.getInstance().getTokenSecret());
			User user = weibo.verifyCredentials();
			
			if(TextUtils.isEmpty(user.getName()))
				authListener.onAuthRequestError(new SinaBlogException("获取用户信息失败！"));
			else{
				BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
				preferenceService.setSinaBlogPreference(accessToken.getToken(), accessToken.getTokenSecret(), user.getName());
				authListener.onAuthRequestComplete(user.getName());
			}
				

	}catch (WeiboException e) {
			authListener.onAuthRequestError(new SinaBlogException(e));
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
