package com.android.liveshooter.activity;

import android.app.Activity;
import android.widget.TextView;
import android.widget.Toast;

import com.android.liveshooter.R;
import com.android.liveshooter.utils.SystemUtil;
/**
 * Especially for controlling exit application or not and something to reuse.
 * @author Arashmen
 * */
public class BaseActivity extends Activity{
	
	@Override
	protected void onResume() {
		super.onResume();
		if(!SystemUtil.checkNetwork(this)){
			Toast.makeText(this, R.string.no_available_network, Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * use resource id to set the title for current activity
	 * @param activity , current activity
	 * @param resource , the title id
	 * @author Arashmen 
	 * */
	protected void setCommonTitle(Activity activity,int resource){
		TextView tv = (TextView)activity.findViewById(R.id.Common_title);
		tv.setText(resource);
	}

	/**
	 * use resource id to set the title for current activity
	 * @param activity , current activity
	 * @param title , String for title.
	 * @author Arashmen 
	 * */
	protected void setCommonTitle(Activity activity,String title){
		TextView tv = (TextView)activity.findViewById(R.id.Common_title);
		tv.setText(title);
	}
	
}
