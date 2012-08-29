package com.android.liveshooter.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.view.KeyEvent;

import com.android.liveshooter.R;

/**
 * Point if the net work is not available.
 * @author Arashmen
 * */
public class NetAlertDialog extends AlertDialog{

	public NetAlertDialog(final Context context) {
		super(context);
		
		/*Listener for positive button to exit application*/
		DialogInterface.OnClickListener positiveListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		};
		
		/*Listener for negative button to get back to application*/
		DialogInterface.OnClickListener negativeListener = new OnClickListener() {
			
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent();
				intent.setAction(Settings.ACTION_WIRELESS_SETTINGS);
				context.startActivity(intent);
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
		
		this.setMessage(context.getText(R.string.dialog_network_unavailable));
		
		this.setButton(context.getText(R.string.dialog_sure), positiveListener);
		
		this.setButton2(context.getText(R.string.dialog_network_setting), negativeListener);
		
		this.setOnKeyListener(keydownListener);
		
	}
	
	
    /**
     * Start the dialog and display it on screen.  The window is placed in the
     * application layer and opaque.  Note that you should not override this
     * method to do initialization when the dialog is shown, instead implement
     * that in {@link #onStart}.
     * by wangsen for bad token 2011-07-20
     */
    public void show() {
    	
    	Activity act = this.getOwnerActivity();
    	if(null != act 
    			&&(act.isFinishing() || act.isRestricted()))
    	{
    		return;
    	}
    	
    	try {
			super.show();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }

	
}
