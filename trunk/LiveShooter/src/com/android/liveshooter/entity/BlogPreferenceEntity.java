package com.android.liveshooter.entity;

public class BlogPreferenceEntity {
	
	private String accessToken;
	private String accessTokenSecret;
	private String nickName;
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public String getAccessTokenSecret() {
		return accessTokenSecret;
	}
	public void setAccessTokenSecret(String accessTokenSecret) {
		this.accessTokenSecret = accessTokenSecret;
	}
	public String getNickName() {
		return nickName;
	}
	public void setNickName(String userName) {
		this.nickName = userName;
	}
	
}
