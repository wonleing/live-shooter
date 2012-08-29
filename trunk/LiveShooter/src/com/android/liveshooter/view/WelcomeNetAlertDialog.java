package com.android.liveshooter.view;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;

import com.android.liveshooter.R;


/**
 * Point if the net work is not available.
 * @author Arashmen
 * */
public class WelcomeNetAlertDialog extends NetAlertDialog{

	public WelcomeNetAlertDialog(final Context context) {
		super(context);
		
		/*Listener for positive button to exit application*/
		DialogInterface.OnClickListener positiveListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
			   
				android.os.Process.killProcess(android.os.Process.myPid());
			}
		};
		
		/*Listener for negative button to get back to application*/
		DialogInterface.OnClickListener negativeListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
			    dismiss();
			    Intent intent = new Intent();
                intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
                context.startActivity(intent);

			}
		};
		
		
		this.setButton(context.getText(R.string.dialog_sure), positiveListener);
		this.setButton2(context.getText(R.string.dialog_network_setting), negativeListener);
	}
	
	
}
