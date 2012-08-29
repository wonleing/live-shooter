package tencent.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
 

public class TokenStore {
	public static String fileName = "qq_token_store";
	
	public static void store(Activity activity, tencent.beans.OAuth oauth) {
		SharedPreferences settings = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putString("oauth_token", oauth.getOauth_token());
		editor.putString("oauth_token_secret", oauth.getOauth_token_secret());	
		
		editor.commit();		
	}
	
	public static String[] fetch(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		
		String oauth_token = settings.getString("oauth_token", null);
		String oauth_token_secret = settings.getString("oauth_token_secret", null);
		
		return new String[] {oauth_token, oauth_token_secret};
	}
	
	public static boolean isSessionValid(Activity activity){
		String[] oauth_token_array = TokenStore.fetch(activity);
		String oauth_token = oauth_token_array[0];
		String oauth_token_secret = oauth_token_array[1];
		 
		if(oauth_token != null && oauth_token_secret != null)  // 已经有access token
				return true;
		return false;
	}
	
	public static void clear(Activity activity) {
		SharedPreferences settings = activity.getSharedPreferences(fileName, Context.MODE_PRIVATE);
		
		SharedPreferences.Editor editor = settings.edit();
		
		editor.clear();  
        editor.commit(); 
	}	
}
