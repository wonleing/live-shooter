package com.android.liveshooter.activity;

import java.util.ArrayList;

import sina.SinaAuthRequestListener;
import sina.SinaBlogException;
import sohu.SohuAuthRequestListener;
import sohu.SohuBlogException;
import tencent.utils.TencentAuthRequestListener;
import tencent.utils.TencentException;
import wangyi.TBlogException;
import wangyi.WangyiAuthRequestListener;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.liveshooter.R;
import com.android.liveshooter.common.AppConstant;
import com.android.liveshooter.entity.BlogPreferenceEntity;
import com.android.liveshooter.service.BlogPreferenceService;
import com.android.liveshooter.service.BlogService;

public class SettingActivity extends BaseActivity {

	private int[] imgs   = { R.drawable.weibosina,
							 R.drawable.weibotencent,
							 R.drawable.weibosohu,
							 R.drawable.weibo163 };
	
	private int[] weibos = { R.string.common_sina_blog, 
						     R.string.common_tencent_blog,
						     R.string.common_sohu_blog, 
						     R.string.common_wangyi_blog};
	
	
	
	private final int AUTH_SUCCESS = 10;
	private final int AUTH_FAILURE = 20;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			
			Toast.makeText(getApplicationContext(), msg.obj.toString(), Toast.LENGTH_LONG).show();
			
			if(msg.what == AUTH_SUCCESS){
				ListView settingListView = (ListView) findViewById(R.id.setting_lv);
				SettingAdapter adapter   = ((SettingAdapter)(settingListView.getAdapter()));
				adapter.setPreferences(new BlogPreferenceService(getApplicationContext()).getBlogPreferences());
				adapter.notifyDataSetChanged();
			}
			
		};
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.setting);
		setCommonTitle(this, R.string.common_3);
		
		init();
	}

	/**
	 * 初始化界面
	 */
	private void init() {
		
		final ListView settingListView          = (ListView) findViewById(R.id.setting_lv);
		final BlogPreferenceService blogService = new BlogPreferenceService(getApplicationContext());
		final SettingAdapter adapter            = new SettingAdapter(getApplicationContext(),blogService.getBlogPreferences());
		final BlogService service        = new BlogService();;
		
		settingListView.setAdapter(adapter);
		
		settingListView.setOnItemClickListener(new OnItemClickListener() {
			
			public void onItemClick(AdapterView<?> arg0, View view, int position,long arg3) {
				
				if(position == AppConstant.NO_0) service.onAuthSina(SettingActivity.this,sinaAuthListener);
						
				if(position == AppConstant.NO_1) service.onAuthTencent(SettingActivity.this,tencentAuthListener);
						
				if(position == AppConstant.NO_2) service.onAuthSohu(SettingActivity.this,sohuAuthListener);
						
				if(position == AppConstant.NO_3) service.onAuthWangyi(SettingActivity.this,wangyiAuthListener);

			}
		});
	}

	private class SettingAdapter extends BaseAdapter {
		
		private Context mContext;
		private ArrayList<BlogPreferenceEntity> arrayList;

		public SettingAdapter(Context context,ArrayList<BlogPreferenceEntity> arrayList) {
			
			this.mContext  = context;
			this.arrayList = arrayList;
			
		}
		
		public void setPreferences(ArrayList<BlogPreferenceEntity> arrayList){
			this.arrayList = arrayList;
		}

		public int getCount() {
			
			return weibos.length;
			
		}

		public Object getItem(int position) {

			return position;
			
		}

		public long getItemId(int position) {

			return position;
			
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			
			BlogPreferenceEntity entity= arrayList.get(position);
			
			ViewHolder viewholder;
			
			if (convertView == null) {
				
				convertView = LayoutInflater.from(mContext).inflate(R.layout.item_setting, null);
				
				viewholder          = new ViewHolder();
				viewholder.iv       = (ImageView) convertView.findViewById(R.id.item_setting_iv);
				viewholder.tv_weibo = (TextView)  convertView.findViewById(R.id.item_setting_tv_weibo);
				viewholder.tv_user  = (TextView)  convertView.findViewById(R.id.item_setting_tv_isbind);
				
				convertView.setTag(viewholder);
				
			} else {
				
				viewholder = (ViewHolder) convertView.getTag();
				
			}

			if (position == (weibos.length-AppConstant.NO_1)) convertView.setBackgroundResource(R.drawable.listview_first_bg);

			else if (position == AppConstant.NO_0) 			  convertView.setBackgroundResource(R.drawable.listview_last_bg);

			else											  convertView.setBackgroundResource(R.drawable.listview_bg);

			viewholder.iv.setBackgroundResource(imgs[position]);
			viewholder.tv_weibo.setText(mContext.getText(weibos[position]));
			viewholder.tv_user .setText(entity.getNickName());

			return convertView;
		}
		

		private class ViewHolder {
			public ImageView iv;
			public TextView tv_weibo;
			public TextView tv_user;
		}

	}
	


	private SinaAuthRequestListener sinaAuthListener = new SinaAuthRequestListener() {
		
		public void onAuthRequestStart() {
			
		}
		
		public void onAuthRequestFault(Throwable fault) {
			sendHandlerMessage(AUTH_FAILURE, fault.getMessage());
		}
		
		public void onAuthRequestError(SinaBlogException exception) {
			sendHandlerMessage(AUTH_FAILURE, exception.getMessage());
		}
		
		public void onAuthRequestComplete(String response) {
			sendHandlerMessage(AUTH_SUCCESS, response);
		}
	};
	
	private TencentAuthRequestListener tencentAuthListener = new TencentAuthRequestListener() {

		@Override
		public void onAuthRequestStart() {
			
		}

		@Override
		public void onAuthRequestError(TencentException exception) {
			sendHandlerMessage(AUTH_FAILURE, exception.getMessage());
		}

		@Override
		public void onAuthRequestFault(Throwable fault) {
			sendHandlerMessage(AUTH_FAILURE, fault.getMessage());
		}
		

		@Override
		public void onAuthRequestComplete(String response) {
			sendHandlerMessage(AUTH_SUCCESS, response);
		}

		
		
	};
	
	private SohuAuthRequestListener sohuAuthListener = new SohuAuthRequestListener() {
		
		public void onAuthRequestStart() {
			
		}
		
		public void onAuthRequestFault(Throwable fault) {

			sendHandlerMessage(AUTH_FAILURE, fault.getMessage());
		}
		
		public void onAuthRequestError(SohuBlogException exception) {
	
			sendHandlerMessage(AUTH_FAILURE, exception.getMessage());
		}
		
		public void onAuthRequestComplete(String response) {
			
			sendHandlerMessage(AUTH_SUCCESS, response);
		}
	};
	
	private WangyiAuthRequestListener wangyiAuthListener = new WangyiAuthRequestListener() {
		
		public void onAuthRequestStart() {
			
		}
		
		public void onAuthRequestFault(Throwable fault) {
			sendHandlerMessage(AUTH_FAILURE, fault.getMessage());
		}
		
		public void onAuthRequestError(TBlogException exception) {
			sendHandlerMessage(AUTH_FAILURE, exception.getMessage());
		}
		
		public void onAuthRequestComplete(String response) {
			sendHandlerMessage(AUTH_SUCCESS, response);
		}
	};
	
	private void sendHandlerMessage(int AuthStatus,String message){
		
		String info = null;
		
		if(AuthStatus == AUTH_FAILURE){
			
			info = new StringBuffer().append("认证失败，")
											.append(message)
											.toString();
			
		}else{
			info = new StringBuffer().append("Hi,")
									 .append(message)
									 .append("！欢迎你来到梦想微公益")
									 .toString();
		}
		
		Message msg = new Message();
		msg.what = AuthStatus;
		msg.obj  = info;
		mHandler.sendMessage(msg);

	}
	
}
