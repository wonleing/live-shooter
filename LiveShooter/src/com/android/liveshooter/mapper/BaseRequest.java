package com.android.liveshooter.mapper;

/**
 * Request for  REST url 
 * */
public abstract class BaseRequest {
	
	protected BaseRequest() {};
	
	protected final String CommonHeader = " ";//AppApiPreference.COMMON_HEADER


	/**
	 * fill the parameters to get the REST url
	 * */
	public abstract String executeToREST();
	
}
