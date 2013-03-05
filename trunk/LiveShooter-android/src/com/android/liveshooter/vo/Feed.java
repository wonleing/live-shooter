package com.android.liveshooter.vo;

import java.util.List;

public class Feed {
	
	private String videoId;
	
	private String snsId;
	
	private int score;
	
	private String createTime;

	private String videoUrl;
	
	private int userId;
	
	private String sns;

	private String userType;
	
	private String video_poster;
	
	private String user_profile;
	
	private String user_name;

	private String video_title;
	
	private String time_ago;
	
	private String time_last;
	
	private List<String> friend_profiles;
	
	public String getVideoId() {
		return videoId;
	}

	public void setVideoId(String videoId) {
		this.videoId = videoId;
	}

	public String getSnsId() {
		return snsId;
	}

	public void setSnsId(String snsId) {
		this.snsId = snsId;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public String getSns() {
		return sns;
	}

	public void setSns(String sns) {
		this.sns = sns;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	private int forward_num;
	
	private int note_num;

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}

	public String getVideo_poster() {
		return video_poster;
	}

	public void setVideo_poster(String video_poster) {
		this.video_poster = video_poster;
	}

	public String getUser_profile() {
		return user_profile;
	}

	public void setUser_profile(String user_profile) {
		this.user_profile = user_profile;
	}
	
	public String getUser_name() {
		return user_name;
	}

	public void setUser_name(String user_name) {
		this.user_name = user_name;
	}

	public String getVideo_title() {
		return video_title;
	}

	public void setVideo_title(String video_title) {
		this.video_title = video_title;
	}

	public String getTime_ago() {
		return time_ago;
	}

	public void setTime_ago(String time_ago) {
		this.time_ago = time_ago;
	}

	public String getTime_last() {
		return time_last;
	}

	public void setTime_last(String time_last) {
		this.time_last = time_last;
	}

	public List<String> getFriend_profiles() {
		return friend_profiles;
	}

	public void setFriend_profiles(List<String> friend_profiles) {
		this.friend_profiles = friend_profiles;
	}

	public int getForward_num() {
		return forward_num;
	}

	public void setForward_num(int forward_num) {
		this.forward_num = forward_num;
	}

	public int getNote_num() {
		return note_num;
	}

	public void setNote_num(int note_num) {
		this.note_num = note_num;
	}
}
