package com.android.liveshooter.common;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.liveshooter.R;

/**
 * 自定义Toast
 * @author Ryan
 *
 */
public class CommonToast {

	public static void show(Context context){
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.toast, null);
		LinearLayout linearLayout = (LinearLayout) view.findViewById(R.id.layout_toast);
		Toast toast = new Toast(context);
		toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
		toast.setView(linearLayout);
		toast.show();
	}
	
}
