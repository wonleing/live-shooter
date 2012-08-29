package com.android.liveshooter.service;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.liveshooter.common.AppConstant;
import com.android.liveshooter.entity.BlogPreferenceEntity;

public class BlogPreferenceService {
	
	private final String VGONGYI_PREFERENCE_KEY              = "VGONGYI_PREFERENCE_KEY";
	
	private final String VGONGYI_SINA_ACCESS_TOKEN           = "VGONGYI_SINA_ACCESS_TOKEN";
	private final String VGONGYI_SINA_ACCESS_TOKEN_SECRET    = "VGONGYI_SINA_ACCESS_TOKEN_SECRET";
	private final String VGONGYI_SINA_USER_NAME              = "VGONGYI_SINA_USER_NAME";
	
	private final String VGONGYI_TENCENT_ACCESS_TOKEN        = "VGONGYI_TENCENT_ACCESS_TOKEN";
	private final String VGONGYI_TENCENT_ACCESS_TOKEN_SECRET = "VGONGYI_TENCENT_ACCESS_TOKEN_SECRET";
	private final String VGONGYI_TENCENT_USER_NAME           = "VGONGYI_TENCENT_USER_NAME";
	
	private final String VGONGYI_SOHU_ACCESS_TOKEN           = "VGONGYI_SOHU_ACCESS_TOKEN";
	private final String VGONGYI_SOHU_ACCESS_TOKEN_SECRET    = "VGONGYI_SOHU_ACCESS_TOKEN_SECRET";
	private final String VGONGYI_SOHU_USER_NAME              = "VGONGYI_SOHU_USER_NAME";
	
	private final String VGONGYI_WANGYI_ACCESS_TOKEN         = "VGONGYI_WANGYI_ACCESS_TOKEN";
	private final String VGONGYI_WANGYI_ACCESS_TOKEN_SECRET  = "VGONGYI_WANGYI_ACCESS_TOKEN_SECRET";
	private final String VGONGYI_WANGYI_USER_NAME            = "VGONGYI_WANGYI_USER_NAME";
	
	private final String VGONGYI_SINA_TOGGLE    = "VGONGYI_SINA_TOGGLE";
	private final String VGONGYI_TENCENT_TOGGLE = "VGONGYI_TENCENT_TOGGLE";
	private final String VGONGYI_SOHU_TOGGLE    = "VGONGYI_SOHU_TOGGLE";
	private final String VGONGYI_WANGYI_TOGGLE  = "VGONGYI_WANGYI_TOGGLE";
	
	private Context context;
	
	public BlogPreferenceService(Context context) {
		this.context = context;
	}
	
	public ArrayList<Boolean> getBlogTranserTogglePreferences(){
		
		ArrayList<Boolean> arrayList = new ArrayList<Boolean>();

		SharedPreferences preference =  this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		Boolean sinaToggle    = preference.getBoolean(VGONGYI_SINA_TOGGLE, false);
		Boolean tencentToggle = preference.getBoolean(VGONGYI_TENCENT_TOGGLE,false);
		Boolean sohuToggle    = preference.getBoolean(VGONGYI_SOHU_TOGGLE, false);
		Boolean wangyiToggle  = preference.getBoolean(VGONGYI_WANGYI_TOGGLE, false);
		
		arrayList.add(sinaToggle);
		arrayList.add(tencentToggle);
		arrayList.add(sohuToggle);
		arrayList.add(wangyiToggle);

		return arrayList;
	}
	
	public void setBlogTranserTogglePreferences(ArrayList<Boolean> arrayList){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		 
		editor.putBoolean(VGONGYI_SINA_TOGGLE,    arrayList.get(AppConstant.NO_0));
		editor.putBoolean(VGONGYI_TENCENT_TOGGLE, arrayList.get(AppConstant.NO_1));
		editor.putBoolean(VGONGYI_SOHU_TOGGLE,    arrayList.get(AppConstant.NO_2));
		editor.putBoolean(VGONGYI_WANGYI_TOGGLE,  arrayList.get(AppConstant.NO_3));
		
		editor.commit();
		
	}
	
	public void setBlogTranserTogglePreferences(boolean sinaToggle,boolean tencentToggle,boolean sohuToggle,boolean wangyiToggle){
		
		ArrayList<Boolean> arrayList = new ArrayList<Boolean>();
		arrayList.add(sinaToggle);
		arrayList.add(tencentToggle);
		arrayList.add(sohuToggle);
		arrayList.add(wangyiToggle);
		
		setBlogTranserTogglePreferences(arrayList);

		
	}
	
	
	public ArrayList<BlogPreferenceEntity> getBlogPreferences(){
		
		ArrayList<BlogPreferenceEntity> preferences = new ArrayList<BlogPreferenceEntity>();
		preferences.add(getSinaBlogPreference());
		preferences.add(getTencentBlogPreference());
		preferences.add(getSohuBlogPreference());
		preferences.add(getWangyiBlogPreference());
		
		return preferences;
	}
	
	public void setSinaBlogPreference(String accessToken,String tokenSecret,String userName){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.putString(VGONGYI_SINA_USER_NAME,    userName);
		editor.putString(VGONGYI_SINA_ACCESS_TOKEN, accessToken);
		editor.putString(VGONGYI_SINA_ACCESS_TOKEN_SECRET,  tokenSecret);
		
		editor.commit();
		
	}
	
	public void setSinaBlogPreference(BlogPreferenceEntity entity){
		
		setSinaBlogPreference(entity.getAccessToken(),entity.getAccessTokenSecret(),entity.getNickName());
		
	}
	
	public BlogPreferenceEntity getSinaBlogPreference(){
		
		BlogPreferenceEntity entity  = new BlogPreferenceEntity();

		SharedPreferences preference =  this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		String accessToken       = preference.getString(VGONGYI_SINA_ACCESS_TOKEN, null);
		String accessTokenSecret = preference.getString(VGONGYI_SINA_ACCESS_TOKEN_SECRET,null);
		String userName          = preference.getString(VGONGYI_SINA_USER_NAME, null);
		
		entity.setAccessToken(accessToken);
		entity.setAccessTokenSecret(accessTokenSecret);
		entity.setNickName(userName);
		
		return entity;
		
	}
	
	public void clearSinaBlogPreferences(){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.remove(VGONGYI_SINA_ACCESS_TOKEN);
		editor.remove(VGONGYI_SINA_ACCESS_TOKEN_SECRET);
		editor.remove(VGONGYI_SINA_USER_NAME);
		
		editor.commit();
	}
	
	public boolean isSinaBlogAuthorized(){
		
		BlogPreferenceEntity entity = getSinaBlogPreference();
		
		if(TextUtils.isEmpty(entity.getAccessToken()))       return false;
		if(TextUtils.isEmpty(entity.getAccessTokenSecret())) return false;
		if(TextUtils.isEmpty(entity.getNickName()))          return false;
		
		return true;
	}
	

	
	public void setTencentBlogPreference(String accessToken,String tokenSecret,String userName){
		
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.putString(VGONGYI_TENCENT_USER_NAME,    userName);
		editor.putString(VGONGYI_TENCENT_ACCESS_TOKEN, accessToken);
		editor.putString(VGONGYI_TENCENT_ACCESS_TOKEN_SECRET,  tokenSecret);
		
		editor.commit();
		
	}
	
	public void setTencentBlogPreference(BlogPreferenceEntity entity){
		
		setTencentBlogPreference(entity.getAccessToken(),entity.getAccessTokenSecret(),entity.getNickName());
		
	}
	
	public BlogPreferenceEntity getTencentBlogPreference(){
		
		BlogPreferenceEntity entity  = new BlogPreferenceEntity();

		SharedPreferences preference =  this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		String accessToken       = preference.getString(VGONGYI_TENCENT_ACCESS_TOKEN, null);
		String accessTokenSecret = preference.getString(VGONGYI_TENCENT_ACCESS_TOKEN_SECRET,null);
		String userName          = preference.getString(VGONGYI_TENCENT_USER_NAME, null);
		
		entity.setAccessToken(accessToken);
		entity.setAccessTokenSecret(accessTokenSecret);
		entity.setNickName(userName);
		
		return entity;
		
	}
	
	public void clearTencentBlogPreferences(){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.remove(VGONGYI_TENCENT_ACCESS_TOKEN);
		editor.remove(VGONGYI_TENCENT_ACCESS_TOKEN_SECRET);
		editor.remove(VGONGYI_TENCENT_USER_NAME);
		
		editor.commit();
	}
	
	public boolean isTencentBlogAuthorized(){
		
		BlogPreferenceEntity entity = getTencentBlogPreference();
		
		if(TextUtils.isEmpty(entity.getAccessToken()))       return false;
		if(TextUtils.isEmpty(entity.getAccessTokenSecret())) return false;
		if(TextUtils.isEmpty(entity.getNickName()))          return false;
		
		return true;
	}
	
	
	
	
	
	public void setSohuBlogPreference(String accessToken,String tokenSecret,String userName){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.putString(VGONGYI_SOHU_USER_NAME,    userName);
		editor.putString(VGONGYI_SOHU_ACCESS_TOKEN, accessToken);
		editor.putString(VGONGYI_SOHU_ACCESS_TOKEN_SECRET,  tokenSecret);
		
		editor.commit();
		
	}
	
	public void setSohuBlogPreference(BlogPreferenceEntity entity){
		
		setSohuBlogPreference(entity.getAccessToken(),entity.getAccessTokenSecret(),entity.getNickName());
		
	}
	
	public BlogPreferenceEntity getSohuBlogPreference(){
		
		BlogPreferenceEntity entity  = new BlogPreferenceEntity();

		SharedPreferences preference =  this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		String accessToken       = preference.getString(VGONGYI_SOHU_ACCESS_TOKEN, null);
		String accessTokenSecret = preference.getString(VGONGYI_SOHU_ACCESS_TOKEN_SECRET,null);
		String userName          = preference.getString(VGONGYI_SOHU_USER_NAME, null);
		
		entity.setAccessToken(accessToken);
		entity.setAccessTokenSecret(accessTokenSecret);
		entity.setNickName(userName);
		
		return entity;
		
	}
	
	public void cleaSohuBlogPreferences(){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.remove(VGONGYI_SOHU_ACCESS_TOKEN);
		editor.remove(VGONGYI_SOHU_ACCESS_TOKEN_SECRET);
		editor.remove(VGONGYI_SOHU_USER_NAME);
		
		editor.commit();
	}
	
	public boolean isSohuBlogAuthorized(){
		
		BlogPreferenceEntity entity = getSohuBlogPreference();
		
		if(TextUtils.isEmpty(entity.getAccessToken()))       return false;
		if(TextUtils.isEmpty(entity.getAccessTokenSecret())) return false;
		if(TextUtils.isEmpty(entity.getNickName()))          return false;
		
		return true;
	}
	

	public void setWangyitBlogPreference(String accessToken,String tokenSecret,String userName){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.putString(VGONGYI_WANGYI_USER_NAME,    userName);
		editor.putString(VGONGYI_WANGYI_ACCESS_TOKEN, accessToken);
		editor.putString(VGONGYI_WANGYI_ACCESS_TOKEN_SECRET,  tokenSecret);
		
		editor.commit();
		
	}
	
	public void setWangyiBlogPreference(BlogPreferenceEntity entity){
		
		setWangyitBlogPreference(entity.getAccessToken(),entity.getAccessTokenSecret(),entity.getNickName());
		
	}
	
	public BlogPreferenceEntity getWangyiBlogPreference(){
		
		BlogPreferenceEntity entity  = new BlogPreferenceEntity();

		SharedPreferences preference =  this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		String accessToken       = preference.getString(VGONGYI_WANGYI_ACCESS_TOKEN, null);
		String accessTokenSecret = preference.getString(VGONGYI_WANGYI_ACCESS_TOKEN_SECRET,null);
		String userName          = preference.getString(VGONGYI_WANGYI_USER_NAME, null);
		
		entity.setAccessToken(accessToken);
		entity.setAccessTokenSecret(accessTokenSecret);
		entity.setNickName(userName);
		
		return entity;
		
	}
	
	public void clearWangyiBlogPreferences(){
		
		SharedPreferences preference    = this.context.getSharedPreferences(VGONGYI_PREFERENCE_KEY,Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = preference.edit();
		
		editor.remove(VGONGYI_WANGYI_ACCESS_TOKEN);
		editor.remove(VGONGYI_WANGYI_ACCESS_TOKEN_SECRET);
		editor.remove(VGONGYI_WANGYI_USER_NAME);
		
		editor.commit();
	}
	
	public boolean isWangyiBlogAuthorized(){
		
		BlogPreferenceEntity entity = getWangyiBlogPreference();
		
		if(TextUtils.isEmpty(entity.getAccessToken()))       return false;
		if(TextUtils.isEmpty(entity.getAccessTokenSecret())) return false;
		if(TextUtils.isEmpty(entity.getNickName()))          return false;
		
		return true;
	}
	
}
