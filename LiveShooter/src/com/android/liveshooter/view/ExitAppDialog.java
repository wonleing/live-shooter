package com.android.liveshooter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;

import com.android.liveshooter.R;

/**
 * Point before exiting , make sure user if want to exit or not.
 * @author Arashmen
 * */
public class ExitAppDialog extends AlertDialog{

	public ExitAppDialog(final Activity activity) {
		super(activity);
		this.setOwnerActivity(activity);
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
		
		this.setMessage(activity.getText(R.string.dialog_make_sure_to_exit));
		
		this.setButton(activity.getText(R.string.dialog_exit), positiveListener);
		
		this.setButton2(activity.getText(R.string.dialog_cancel), negativeListener);
		
		this.setOnKeyListener(keydownListener);
	}
	
	
}
