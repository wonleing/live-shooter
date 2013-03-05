package com.android.liveshooter.util;

import android.app.Application;

/**
 * 该文件定义了系统的全局变量 
 * @author jianping
 *
 */
public class GlobalApp extends Application{

	private String serv_ip;
	
	private int serv_port;
	
	private String username;
	
	private int userId;
	
	private String snsId;

	public String getSnsId() {
		return snsId;
	}

	public void setSnsId(String snsId) {
		this.snsId = snsId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	private String password;
	
	private boolean logined;

	public boolean isLogined() {
		return logined;
	}

	public void setLogined(boolean logined) {
		this.logined = logined;
	}

	public String getServ_ip() {
		return serv_ip;
	}

	public void setServ_ip(String serv_ip) {
		this.serv_ip = serv_ip;
	}

	public int getServ_port() {
		return serv_port;
	}

	public void setServ_port(int serv_port) {
		this.serv_port = serv_port;
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
