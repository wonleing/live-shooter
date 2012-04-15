package com.android.liveshooter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.KeyEvent;

import com.android.liveshooter.R;
import com.android.liveshooter.activity.SettingActivity;

/**
 * Point before exiting , make sure user if want to exit or not.
 * @author Arashmen
 * */
public class TransferAuthDialog extends AlertDialog{

	public TransferAuthDialog(final Activity activity) {
		super(activity);
		this.setOwnerActivity(activity);
		/*Listener for positive button to exit application*/
		DialogInterface.OnClickListener positiveListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				
				Intent intent = new Intent(activity,SettingActivity.class);
				activity.startActivity(intent);
				
			}
		};
		
		/*Listener for negative button to get back to application*/
		DialogInterface.OnClickListener negativeListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		};
		
		/*Make sure search button and back button don't work*/
		DialogInterface.OnKeyListener keydownListener = new OnKeyListener() {
			
			public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
				if(keyCode == KeyEvent.KEYCODE_SEARCH||keyCode == KeyEvent.KEYCODE_BACK)
					return true;
				return false;
			}
		};
		
		this.setTitle(R.string.dialog_point);
		
		this.setMessage(activity.getText(R.string.transfer_auth));
		
		this.setButton(activity.getText(R.string.transfer_bind), positiveListener);
		
		this.setButton2(activity.getText(R.string.transfer_bind_later), negativeListener);
		
		this.setOnKeyListener(keydownListener);
	}
	
	
}
