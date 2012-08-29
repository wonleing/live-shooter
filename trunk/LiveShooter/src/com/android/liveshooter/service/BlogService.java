package com.android.liveshooter.service;

import java.io.File;

import org.json.JSONObject;

import sina.SinaAuthRequestListener;
import sina.SinaBlogException;
import sina.SinaDialog;
import sina.SinaRequestListener;
import sina.Weibo;
import sina.WeiboException;
import sohu.SohuAuthRequestListener;
import sohu.SohuBlog;
import sohu.SohuBlogException;
import sohu.SohuDialog;
import sohu.SohuRequestListener;
import sohu.http.Response;
import tencent.activity.TencentDialog;
import tencent.api.T_API;
import tencent.beans.OAuth;
import tencent.utils.Configuration;
import tencent.utils.TencentAuthRequestListener;
import tencent.utils.TencentException;
import tencent.utils.TencentRequestListener;
import tencent.utils.Utils;
import wangyi.TBlog;
import wangyi.TBlogException;
import wangyi.WangyiAuthRequestListener;
import wangyi.WangyiDialog;
import wangyi.WangyiRequestListener;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.liveshooter.common.AppApiPreference;
import com.android.liveshooter.entity.BlogPreferenceEntity;

public class BlogService {
	
	public BlogService() {
		
	}
	
    
    public void onAuthSina( Activity activity ,SinaAuthRequestListener authListener){
		
    	new SinaDialog(activity, authListener).show();
    
    }

    public void sendFeedsToSinaWeibo(Activity activity,String picPath,String content,SinaRequestListener listener){
		
    	try{
			listener.onRequestStart();
		    	
		    System.setProperty("weibo4j.oauth.consumerKey", AppApiPreference.SINA_APP_KEY);
		   	System.setProperty("weibo4j.oauth.consumerSecret", AppApiPreference.SINA_APP_KEY_SECRET);
				
			Weibo weibo = new Weibo();
				
			BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
				
			BlogPreferenceEntity entity = preferenceService.getSinaBlogPreference();
				
			weibo.setToken(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			sina.Status status ;
			
			if(TextUtils.isEmpty(picPath) ){
			
				status = weibo.updateStatus(content);
				
			}else{
			
				status = weibo.uploadStatus(content, new File(picPath));
				
			}
			
			listener.onRequestComplete(status.toString());
				
			}catch (WeiboException e) {
				listener.onRequestError(new SinaBlogException(e));
			}catch (Throwable t) {
				listener.onRequestFault(t);
			}
			
    }

    public void sendCommentToSina( Activity activity, String weiboID, String weiboComment, SinaRequestListener requestListener){
	
		try {
			
			requestListener.onRequestStart();
			System.setProperty("weibo4j.oauth.consumerKey", AppApiPreference.SINA_APP_KEY);
		   	System.setProperty("weibo4j.oauth.consumerSecret", AppApiPreference.SINA_APP_KEY_SECRET);
				
			Weibo weibo = new Weibo();
				
			BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
				
			BlogPreferenceEntity entity = preferenceService.getSinaBlogPreference();
				
			weibo.setToken(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			sina.Comment comment = null;
	
			comment = weibo.updateComment(weiboComment, weiboID, null);
			
			requestListener.onRequestComplete(comment.toString());
			
		} catch (WeiboException e) {
			requestListener.onRequestError(new SinaBlogException(e));
		} catch (Throwable t) {
			requestListener.onRequestFault(t);
		}
		
    }
	
    

    public void onAuthTencent( Activity activity,TencentAuthRequestListener authListener){
	
		new TencentDialog(activity, authListener).show();
	
    }
    //
    public void sendFeedsToTecentWeibo(Activity activity, String picPath,String content, TencentRequestListener requestListener) {

    	try {
			requestListener.onRequestStart();
			
			// set app_key & key_secret
			OAuth oauth = new OAuth(TencentDialog.TENCENT_WEIBO_TAG); // 初始化OAuth请求令牌
			oauth.setOauth_consumer_key(AppApiPreference.TECENT_APP_KEY);
			oauth.setOauth_consumer_secret(AppApiPreference.TECENT_APP_KEY_SECRET);
			// get access token information
			BlogPreferenceService service = new BlogPreferenceService(activity);
			BlogPreferenceEntity entity = service.getTencentBlogPreference();

			oauth.setOauth_token(entity.getAccessToken());
			oauth.setOauth_token_secret(entity.getAccessTokenSecret());

			/* get ip */
			WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			Configuration.wifiIp = Utils.intToIp(ipAddress);

			T_API tapi = new T_API();

			String mContent = TextUtils.isEmpty(content) ? "" : content;

			String s = null;

			if (!TextUtils.isEmpty(picPath)) {

				s = tapi.add_pic(oauth, "json", mContent,Configuration.wifiIp, picPath);

			} else {

				s = tapi.add(oauth, "json", mContent, Configuration.wifiIp,"", "");

			}

			JSONObject jsonObj = new JSONObject(s);
			int errcode = jsonObj.getInt("errcode");
			if (errcode != 0)
				requestListener.onRequestError(new TencentException(errcode, jsonObj.getString("msg"), jsonObj.toString(), jsonObj.toString()));
			else
				requestListener.onRequestComplete(s);

		} catch (Exception e) {
			requestListener.onRequestError(new TencentException(e));
		} catch (Throwable t) {
			requestListener.onRequestFault(t);
		}

    }
    //
    public void sendCommentToTecentWeibo(Activity activity,  String weiboID,String weiboComment, TencentRequestListener requestListener) {
    	try {
			requestListener.onRequestStart();
	
			OAuth oauth = new OAuth(TencentDialog.TENCENT_WEIBO_TAG); // 初始化OAuth请求令牌
			oauth.setOauth_consumer_key(AppApiPreference.TECENT_APP_KEY);
			oauth.setOauth_consumer_secret(AppApiPreference.TECENT_APP_KEY_SECRET);
			
			BlogPreferenceService preferenceService = new BlogPreferenceService(activity);
			BlogPreferenceEntity entity = preferenceService.getTencentBlogPreference();
	
			oauth.setOauth_token(entity.getAccessToken());
			oauth.setOauth_token_secret(entity.getAccessTokenSecret());
	
			/* get ip */
			WifiManager wifiManager = (WifiManager) activity.getSystemService(Context.WIFI_SERVICE);
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			int ipAddress = wifiInfo.getIpAddress();
			Configuration.wifiIp = Utils.intToIp(ipAddress);
	
			T_API tapi = new T_API();
	
			String content = TextUtils.isEmpty(weiboComment) ? "": weiboComment;
			String s = null;
			s = tapi.comment(oauth, "json", content, Configuration.wifiIp,weiboID);
			JSONObject jsonObj = new JSONObject(s);
			int errcode = jsonObj.getInt("errcode");
			if (errcode != 0) {
				requestListener.onRequestError(new TencentException(s));
			} else {
				requestListener.onRequestComplete(s);
			}

		} catch (Exception e) {
			requestListener.onRequestError(new TencentException(e));
		} catch (Throwable t) {
			requestListener.onRequestFault(t);
		}

    }
	
    

	public void onAuthSohu(Activity activity,SohuAuthRequestListener authListener) {
	
		new SohuDialog(activity, authListener).show();
		
	}
	
	public void sendFeedsToSohuWeibo(Activity activity, String picPath,String content, SohuRequestListener requestListener) {
		
		try {
			requestListener.onRequestStart();
			SohuBlog blog = new SohuBlog();
			
			BlogPreferenceService service = new BlogPreferenceService(activity);
			BlogPreferenceEntity  entity  = service.getSohuBlogPreference();
			blog.setAccesTokenAndSecret(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			Response response = null;
			if (TextUtils.isEmpty(picPath)){
				response = blog.updateStatus(content);
			}else{
				File file = new File(picPath);
				response = blog.uploadStatus(content, file);
			}
			Log.i("msg","response = " +response.getResponseContent());
			requestListener.onRequestComplete(response.getResponseContent());
			
		} catch (WeiboException e) {
			requestListener.onRequestError(new SohuBlogException(e));
		}catch (Throwable e) {
			requestListener.onRequestFault(e);
		}
	}
	
    public void sendCommentToSohu( Activity activity, String weiboID, String weiboComment, SohuRequestListener requestListener){
    	
		try {
			
			requestListener.onRequestStart();
				
			SohuBlog blog = new SohuBlog();
			
			BlogPreferenceService service = new BlogPreferenceService(activity);
			BlogPreferenceEntity  entity  = service.getSohuBlogPreference();
			blog.setAccesTokenAndSecret(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			Response response = null;			
			response = blog.updateComment(weiboComment, weiboID);
			
			requestListener.onRequestComplete(response.getResponseContent());
			
		} catch (WeiboException e) {
			requestListener.onRequestError(new SohuBlogException(e));
		} catch (Throwable t) {
			requestListener.onRequestFault(t);
		}
		
    }
	
	

	public void onAuthWangyi(Activity activity,WangyiAuthRequestListener authListener) {
	
		new WangyiDialog(activity, authListener).show();
	
	}
	
	public void sendFeedsToWangyiWeibo(Activity activity, String picPath,String content, WangyiRequestListener requestListener) {
	
		try {
			requestListener.onRequestStart();
			System.setProperty("tblog4j.oauth.consumerKey",AppApiPreference.WANGYI_APP_KEY);
	    	System.setProperty("tblog4j.oauth.consumerSecret", AppApiPreference.WANGYI_APP_KEY_SECRET);
			
	    	// 暂时把debug关了。减少干扰信息
	    	System.setProperty("tblog4j.debug", "false");
			TBlog tblog = new TBlog();
			
			BlogPreferenceService service = new BlogPreferenceService(activity);
			BlogPreferenceEntity  entity  = service.getWangyiBlogPreference();
			tblog.setToken(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			wangyi.data.Status status = null;
			if (TextUtils.isEmpty(picPath))
				status = tblog.updateStatus(content);
			else {
				File file = new File(picPath);
				status = tblog.updateImage(content,file);
			}
			
			requestListener.onRequestComplete(status.toString());
			
		} catch (TBlogException e) {
			requestListener.onRequestError(new TBlogException(e));
		}catch (Throwable e) {
			requestListener.onRequestFault(e);
		}
	}

	public void sendCommentToWangyiWeibo(Activity activity, String weiboID,String comment, WangyiRequestListener requestListener) {
		
		try {
			requestListener.onRequestStart();
			
			// 设置 consumer key, consumer secret：
			// 也可以在 t4j.properties 中设置，这个文件应当放置在：源代码目录的根目录
			System.setProperty("tblog4j.oauth.consumerKey",AppApiPreference.WANGYI_APP_KEY);
	    	System.setProperty("tblog4j.oauth.consumerSecret", AppApiPreference.WANGYI_APP_KEY_SECRET);
			
	    	// 暂时把debug关了。减少干扰信息
	    	System.setProperty("tblog4j.debug", "false");
	    	
			TBlog tblog = new TBlog();
			BlogPreferenceService service = new BlogPreferenceService(activity);
			BlogPreferenceEntity  entity  = service.getWangyiBlogPreference();
			tblog.setToken(entity.getAccessToken(), entity.getAccessTokenSecret());
			
			wangyi.data.Status status = null;
			status = tblog.reply(Long.valueOf(weiboID), comment);
			requestListener.onRequestComplete(status.toString());
	
		} catch (TBlogException e) {
			requestListener.onRequestError(new TBlogException(e));
		} catch (Throwable t) {
			requestListener.onRequestFault(t);
		}
	}
		
}
