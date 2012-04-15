package com.android.liveshooter.net;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import com.android.liveshooter.mapper.BaseRequest;

/**
 *Local interface for http connection management
 *@author Arashmen 
 **/
public class HttpConnector {
	
	private final int REPEATS = 3;
	private final int CONNECT_TIMEOUT = 5000;
	private final int READ_TIMEOUT = 10000;
	
	private String 		  urlStr;
	private URL 		  url;
	private URLConnection connection = null;
	
	@SuppressWarnings("unused")
	private HttpConnector() {}

	public HttpConnector(BaseRequest request) {
		try {
			this.urlStr = request.executeToREST();
			url = new URL(urlStr);
		} catch (MalformedURLException e) {
		}
	}
	public HttpConnector(String request) {
		try {
			urlStr = request;
			url = new URL(request);
		} catch (MalformedURLException e) {
		}
	}
	
	/**
	 * get data stream from net.
	 * @author Arashmen
	 * @throws VGongyiException .
	 * */
	public InputStream getURLResponse(){

		InputStream in = null;
		int count = 0;
		int responseCode = -1;
		while(count<REPEATS){
            try {
                connection = url.openConnection();
                connection.setConnectTimeout(CONNECT_TIMEOUT);
                connection.setReadTimeout(READ_TIMEOUT);
                responseCode = ((HttpURLConnection) connection).getResponseCode();

                if (responseCode != HttpURLConnection.HTTP_OK)
                    throw new RuntimeException();

                in = connection.getInputStream();

                if (null == in)
                    throw new RuntimeException();

                count = REPEATS; 
            } catch (IOException e) {
					count++;
				} catch (RuntimeException e) {
					count++; 
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
					}
				} catch (Exception e){
//					throw new VGongyiException(e.getLocalizedMessage(), e);
					e.printStackTrace();
				}
		}
		
		return in;
	}
	
	/**
	 * read the string of URL.
	 * */
	public String getUrltoString(){
		return urlStr;
	}
	
	public int getTotalSize() {
        return connection != null ? connection.getContentLength() : 0;
    }
}
