package com.android.liveshooter.utils;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class IPUtil {	
	
	 private WifiInfo    mWifiInfo; 
	 private WifiManager mWifiManager;

	 
	 public IPUtil(Context context){

	    	//取得WifiManager对象   
	    	if(mWifiManager==null)  mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);  
	        //取得WifiInfo对象   
	        mWifiInfo = mWifiManager.getConnectionInfo();   
	        
	 }
	 
	 
	public  String getIp(){
		
		String ip;
		
		if(mWifiManager.isWifiEnabled())  ip = GetIPAddressByWIFi();
		
		else                              ip = getLocalIpAddress();
			
			
		return ip;
		
	}
	

    //得到IP地址   
    private  String GetIPAddressByWIFi()   
    {   
        return (mWifiInfo == null) ? "0" : intToIp(mWifiInfo.getIpAddress());   
    }   
	
	
	/**
	 * get local ip address
	 * @return: the local ip address
	 */
	private String getLocalIpAddress()
    {
        try
        {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
            {
               NetworkInterface intf = en.nextElement();
               for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
               {
                   InetAddress inetAddress = enumIpAddr.nextElement();
                   if (!inetAddress.isLoopbackAddress())
                   {
                       return inetAddress.getHostAddress().toString();
                   }
               }
           }
        }
        catch (SocketException ex)
        {
            Log.e("WifiPreference IpAddress", ex.toString());
        }
        return null;
    }
	
    private String intToIp(int i) { 

    	return (i & 0xFF ) + "." + 
    	((i >> 8 ) & 0xFF) + "." + 
    	((i >> 16 ) & 0xFF) + "." + 
    	( i >> 24 & 0xFF) ;
    }
	
	
}
