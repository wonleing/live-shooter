package com.android.liveshooter.socket;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Vector;

import org.apache.xmlrpc.XmlRpcClient;
import org.apache.xmlrpc.XmlRpcException;

public class XMLRPCServer {

	private XmlRpcClient client = null;
	
	public XMLRPCServer(String url){
		try {
			client = new XmlRpcClient(url);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 用户登录
	 * @param username
	 * @param password
	 * @return
	 */
	public int login(String username, String sns, String nickname, String profile){
		if(client == null){
			return 0;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(username);
		params.add(sns);
		params.add(nickname);
		params.add(profile);
		try {
			Object iret = client.execute("loginUser", params);
			return (Integer)iret;
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
	}
	
	/**
	 * 获得上传文件名
	 * @return
	 */
	public String getFileName(){
		if(client == null){
			return null;
		}
		try {
			return (String) client.execute("genFilename", new Vector<Object>());
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * 完成视频上传
	 * @param videoId
	 * @return
	 */
	public Object finishUpload(String videoId){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(videoId);
		try {
			return client.execute("finishUpload", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		
	}
	
	/**
	 * 分享视频
	 * @param videoId
	 * @param snsId
	 */
	public boolean shareVideo(String videoId, String snsId){
		if(client == null){
			return false;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(videoId);
		params.add(snsId);
		try {
			client.execute("shareVideo", params);
			return true;
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * 喜欢/不喜欢视频
	 * @param videoId
	 * @param snsId
	 */
	public boolean likeVideo(int videoId, String snsId, boolean islike){
		if(client == null){
			return false;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(videoId);
		params.add(snsId);
		try {
			if(islike){
				return Boolean.valueOf((String)client.execute("likeVideo", params));
			}
			else {
				return Boolean.valueOf((String)client.execute("unlikeVideo", params));
			}
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 关注/不关注视频
	 * @param userid
	 * @param videoid
	 * @param isfollow
	 * @return
	 */
	public boolean followVideo(int userid, String videoid, boolean isfollow){
		if(client == null){
			return false;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userid);
		params.add(videoid);
		try {
			if(isfollow){
				return Boolean.valueOf((String)client.execute("followVideo", params));
			}
			else {
				return Boolean.valueOf((String)client.execute("unfollowVideo", params));
			}
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 关注/不关注用户
	 * @param userid
	 * @param targetid
	 * @param isfollow
	 * @return
	 */
	public boolean followUser(int userid, int targetid, boolean isfollow){
		if(client == null){
			return false;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userid);
		params.add(targetid);
		try {
			if(isfollow){
				return Boolean.valueOf((String)client.execute("followUser", params));
			}
			else {
				return Boolean.valueOf((String)client.execute("unfollowUser", params));
			}
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}
	
	/**
	 * 获得用户的关注人列表
	 * @param userId
	 * @return
	 */
	public Object getUserFollowing(int userId){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userId);
		try {
			return client.execute("getFollowing", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得用户的粉丝
	 * @param userId
	 * @return
	 */
	public Object getUserFollower(int userId){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userId);
		try {
			return client.execute("getFollower", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得用户profile信息
	 * @param userid
	 * @return
	 */
	public Object getUserProfile(int userid){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userid);
		try {
			return client.execute("getUserProfile", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得用户上传的视频
	 * @param userId
	 * @return
	 */
	public Object getUserVideo(int userId){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userId);
		try {
			return client.execute("getUserVideo", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得用户反馈
	 * @param userId
	 * @return
	 */
	public Object getUserFeed(int userId){
		if(client == null){
			return null;
		}
		Vector<Object> params = new Vector<Object>();
		params.add(userId);
		try {
			return client.execute("getFeed", params);
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得系统推荐用户
	 * @return
	 */
	public Object getRecommandUser(){
		if(client == null){
			return null;
		}
		try {
			return client.execute("getRecommandUser", new Vector(){});
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * 获得系统推荐视频
	 * @return
	 */
	public Object getRecommandVideo(){
		if(client == null){
			return null;
		}
		try {
			return client.execute("getRecommandVideo", new Vector(){});
		} catch (XmlRpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
