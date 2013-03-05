package com.android.liveshooter.vo;

public class RecommandUser {

	private UserInfo userInfo;
	
	private int followedCount;

	public UserInfo getUserInfo() {
		return userInfo;
	}

	public void setUserInfo(UserInfo userInfo) {
		this.userInfo = userInfo;
	}

	public int getFollowedCount() {
		return followedCount;
	}

	public void setFollowedCount(int followedCount) {
		this.followedCount = followedCount;
	}
}
