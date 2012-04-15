package sohu;
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

import sohu.data.OAuth;
import sohu.utils.ConfigUtil;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

public class SohuDialog extends Dialog {
	public static String accessToken = "";
	public static String accessTokenSecret = ""; 
														
	private static final float[] DIMENSIONS_LANDSCAPE = { 460, 260 };

	private static final float[] DIMENSIONS_PORTRAIT = { 280, 420 };

	private String mUrl;

	private SohuAuthRequestListener authListener;

	private WebView webView;

	private RelativeLayout content;
	
	private Activity activity;
	
	private ProgressDialog progress;	

	
	public SohuDialog(Activity activity,SohuAuthRequestListener listener) {
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
		              
		      	    //init
		      	    OAuth  oAuth = OAuth.getInstance();
		      	    oAuth.clear();
		      	    
		      	    //获取被操作app的key、secret
		      	    oAuth.setKeyAndSecret(AppApiPreference.SOHO_APP_KEY, AppApiPreference.SOHO_APP_KEY_SECRET);
		      	    
		      	    String url = oAuth.getAuthorizUrl();
		              
		            webView.loadUrl(url);

				} catch (Exception e) {
					authListener.onAuthRequestError(new SohuBlogException(e.getMessage()));
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
				Log.i("info", "onPageStarted :" + url);
		        
		        /**
		         *  url.contains(ConfigUtil.callBackUrl)
		         *  如果授权成功url中包含之前设置的callbackurl
		         *  		包含：授权成功
		         *
		         *index == 0
		         *由于该方法onPageStarted可能被多次调用造成重复跳转
		         *		则添加此标示
		         */
		        
		        if( url.contains(ConfigUtil.callBackUrl) && index == 0){
		        	try{
		        		////////////////////////Pre_doing
						progress.show();

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
		            	progress.dismiss();
		            	dismiss();
		        		authListener.onAuthRequestError(new SohuBlogException(e));
		        	}
		        	
	           }

					
			}
			
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
				Log.i("info", "onPageFinished :" + url);
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
	
	
	public void getToken(String oauth_verifier, String oauth_token) {
		
		OAuth oauth = OAuth.getInstance();
		oauth.setOauth_token(oauth_token);
		oauth.setOauthVerifier(oauth_verifier);
		String str = oauth.getAccessToken();
		String access_token = getParameter("oauth_token", str.split("&"));
		String oauth_token_secret = getParameter("oauth_token_secret",str.split("&"));
		accessToken = access_token;
		accessTokenSecret = oauth_token_secret;
		BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
		try {
			SohuBlog blog = new SohuBlog();
			String content = blog.verifyCredentials();
			Log.i("msg","content = " + content);
			JSONObject obj = new JSONObject(content);
			String nick = obj.getString("screen_name");
			preferenceService.setSohuBlogPreference(access_token, oauth_token_secret, nick);
			authListener.onAuthRequestComplete(nick);
			progress.dismiss();
			dismiss();
		} catch (Exception e) {
			authListener.onAuthRequestError(new SohuBlogException("获取用户资料失败!"));
			progress.dismiss();
			dismiss();
		}
		
	}
		
	 public String getParameter(String parameter,String[] responseStr) {
	    	String value = null;
	        for (String str : responseStr) {
	        	if (str.startsWith(parameter+'=')) {
	        		value = str.split("=")[1].trim();
	            	break;
	            }
	        }
	        return value;
	    }
	
	
}
