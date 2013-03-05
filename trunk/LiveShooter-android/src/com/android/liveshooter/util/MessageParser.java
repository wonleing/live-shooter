package com.android.liveshooter.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.liveshooter.vo.Feed;
import com.android.liveshooter.vo.UserInfo;
import com.android.liveshooter.vo.VideoInfo;

public class MessageParser {

	/**
	 * 解析微博用户信息
	 * @param str
	 * @return
	 */
	public UserInfo parseUserInfo(String str){
		try {
			JSONObject json = new JSONObject(str);
			UserInfo info = new UserInfo();
			info.setId(json.getString("id"));
			info.setScreenName(json.getString("screen_name"));
			info.setName(json.getString("name"));
			info.setProvince(json.getString("province"));
			info.setCity(json.getString("city"));
			info.setLocation(json.getString("location"));
			info.setProfileImageUrl(json.getString("profile_image_url"));
			info.setFollowers(json.getInt("followers_count"));
			info.setFriends(json.getInt("friends_count"));
			info.setFavourites(json.getInt("favourites_count"));
			return info;
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 解析Feed信息
	 * @param str
	 * @return
	 */
	public List<Feed> parseFeedInfo(String str){
		List<Feed> result = new ArrayList<Feed>();
		if(str != null && str.length() > 2){
			str = str.substring(1, str.length() - 1); //去掉首位的[]
			List<String> subs = Tools.parseMultiItemString(str);
			for(int i = 0; i < subs.size(); i++){
				String s = subs.get(i).trim();
				String[] items = s.split(",");
				if(items.length > 0){
					Feed feed = new Feed();
					feed.setVideoId(items[0].trim());
					feed.setVideo_title(items[1].trim());
					feed.setSnsId(items[2].trim());
					feed.setScore(Integer.valueOf(items[3].trim()));
					feed.setCreateTime(items[4].trim());
					feed.setUserId(Integer.valueOf(items[5]));
					feed.setUser_name(items[6]);
					feed.setUser_profile(items[7]);
					feed.setSns(items[8]);
					feed.setUserType(items[9]);
					result.add(feed);
				}
			}
		}
		return result;
	}
	
	/**
	 * 解析用户上传的视频
	 * @param str
	 * @return
	 */
	public List<VideoInfo> parseUserVideoInfo(String str){
		List<VideoInfo> result = new ArrayList<VideoInfo>();
		if(str != null && str.trim().length() > 2){
			str = str.substring(1, str.length() - 1);
			List<String> subs = Tools.parseMultiItemString(str);
			for(int i = 0; i < subs.size(); i++){
				String s = subs.get(i).trim();
				String[] items = s.split(",");
				if(items.length > 0){
					VideoInfo video = new VideoInfo();
					video.setVideoId(items[0]);
					video.setVideoTitle(items[1]);
					video.setSnsId(items[2]);
					video.setScore(Integer.valueOf(items[3].trim()));
					video.setCreateDate(items[4]);
					result.add(video);
				}
			}
		}
		return result;
	}
}
