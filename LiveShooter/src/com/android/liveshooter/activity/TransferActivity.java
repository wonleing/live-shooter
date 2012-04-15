package com.android.liveshooter.activity;

import java.util.ArrayList;

import sina.SinaBlogException;
import sina.SinaRequestListener;
import sohu.SohuBlogException;
import sohu.SohuRequestListener;
import tencent.utils.TencentException;
import tencent.utils.TencentRequestListener;
import wangyi.TBlogException;
import wangyi.WangyiRequestListener;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.android.liveshooter.R;
import com.android.liveshooter.common.AppApiPreference;
import com.android.liveshooter.common.AppConstant;
import com.android.liveshooter.entity.BlogPreferenceEntity;
import com.android.liveshooter.service.BlogPreferenceService;
import com.android.liveshooter.service.BlogService;
import com.android.liveshooter.view.TransferAuthDialog;

public class TransferActivity extends BaseActivity { 
	
	private Button backBtn;
	private Button transferBtn;
	private EditText transferContentEditText;
	private TextView textCountTextView;
	
	private ListView transferListView;
	private TransferAdapter adapter;
	private BlogService service = new BlogService();
	private int MAX_COUNT = 140;
	
	private BlogPreferenceService preferenceService  = new BlogPreferenceService(this);
	
	int[] imgs = {R.drawable.weibosina,
				  R.drawable.weibotencent,
				  R.drawable.weibosohu,
				  R.drawable.weibo163};
	
//	private static String weiboContent;
	private static String weiboImg;
	
	private Handler mHandler = new Handler(){
		public void handleMessage(android.os.Message msg) {
		
			if(msg.what == AppConstant.NO_0)
				Toast.makeText(getApplicationContext(), msg.obj.toString(),Toast.LENGTH_SHORT).show();
			if(msg.what == AppConstant.NO_1){
				findViewById(R.id.title_progress).setVisibility(View.GONE);
				findViewById(R.id.Common_refresh).setVisibility(View.VISIBLE);
			}
			if(msg.what == AppConstant.NO_3){
				Toast.makeText(getApplicationContext(),"字数太多了",Toast.LENGTH_SHORT).show();
			}
			
		};
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.transfer);

        setCommonTitle(this, R.string.transfer_title);
        Intent i = getIntent();
//        if(i.hasExtra("weiboContent"))	weiboContent = i.getStringExtra("weiboContent");
        if(i.hasExtra("weiboImg"))      weiboImg     = i.getStringExtra("weiboImg");
        
        textCountTextView = (TextView) this.findViewById(R.id.text_count);
        backBtn = (Button) this.findViewById(R.id.Common_back);
        transferBtn = (Button) this.findViewById(R.id.Common_refresh);
        transferListView = (ListView) this.findViewById(R.id.trans_list);
        backBtn.setText(R.string.tranfer_return);
        backBtn.setVisibility(View.VISIBLE);
        transferBtn.setText(R.string.tranfer_trans);
        transferBtn.setVisibility(View.VISIBLE);
        
        transferContentEditText = (EditText) this.findViewById(R.id.trans_content);
//        transferContentEditText.setText(weiboContent);
        
        textCountTextView.setText(String.valueOf(140-transferContentEditText.getText().toString().length()));
        
        transferContentEditText.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				textCountTextView.setText(String.valueOf(140-transferContentEditText.getText().toString().length()));
			
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			
			public void afterTextChanged(Editable s) {}
		});
        
        backBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {

				TransferActivity.this.finish();
			
			}
		});
        
        transferBtn.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				
				if(transferContentEditText.getText().toString().length() > MAX_COUNT){
					mHandler.sendEmptyMessage(AppConstant.NO_3);
					return;
				}else{
					findViewById(R.id.title_progress).setVisibility(View.VISIBLE);
					v.setVisibility(View.GONE);
				}
					
				
				new Thread(){
        			public void run() {
        				
        				transferFeeds();
        				
        				mHandler.sendEmptyMessage(AppConstant.NO_1);
        				
        				super.run();
        			}
        		}.start();
				
			}
		});

        adapter = new TransferAdapter(this,preferenceService.getBlogPreferences());
        transferListView.setAdapter(adapter);
        
        transferListView.setOnItemClickListener(new OnItemClickListener() {
          
        	public void onItemClick(AdapterView<?> arg0, View arg1, int position,long arg3) {
            	
        		new TransferAuthDialog(TransferActivity.this).show();
            	
            } 
        });
	}
	
	private void transferFeeds(){
		
		ArrayList<Boolean> toggleArrayList = preferenceService.getBlogTranserTogglePreferences();
		
		String content = transferContentEditText.getText().toString();
		
		if(toggleArrayList.get(AppConstant.NO_0)) service.sendFeedsToSinaWeibo(TransferActivity.this, weiboImg,content,sinaRequestListener);

		if(toggleArrayList.get(AppConstant.NO_1)) service.sendFeedsToTecentWeibo(TransferActivity.this, weiboImg,content,tencentRequestListener);
			
		if (toggleArrayList.get(AppConstant.NO_2)) service.sendFeedsToSohuWeibo(TransferActivity.this, weiboImg, content, sohuRequestListener);
		
		if (toggleArrayList.get(AppConstant.NO_3)) service.sendFeedsToWangyiWeibo(TransferActivity.this, weiboImg,content,wangyiRequestListener);

		toggleArrayList = null;
		content = null;
		
	}

	private class TransferAdapter extends BaseAdapter{
			
	        private Context mContext;
	        private ArrayList<BlogPreferenceEntity> arraylist;
	        private BlogPreferenceService service;
	        private ArrayList<Boolean> toggleArrayList;
	        
	        public TransferAdapter(Context context,ArrayList<BlogPreferenceEntity> arrayList) {
	            this.mContext = context;
	            this.arraylist = arrayList;
	            this.service = new BlogPreferenceService(context);
	            this.toggleArrayList = this.service.getBlogTranserTogglePreferences();
	        }
	
	        public int getCount() {
	            return AppApiPreference.getWeibos(TransferActivity.this).length;
	        }
	
	        public Object getItem(int position) {
	            return position;
	        }
	
	        public long getItemId(int position) {
	            return position;
	        }
	
	        public View getView(final int position, View convertView, ViewGroup parent) {
	        	
	        	BlogPreferenceEntity entity = arraylist.get(position);
	        	
	        	ViewHolder viewholder;
	        	
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_transfer, null);
                viewholder = new ViewHolder();
                viewholder.iv = (ImageView)convertView.findViewById(R.id.item_trans_iv);
                viewholder.tv_weibo  = (TextView)convertView.findViewById(R.id.item_trans_tv_weibo);
                viewholder.tv_weibo_nick  = (TextView)convertView.findViewById(R.id.item_trans_tv_weibo_nick);
                viewholder.bindView  = convertView.findViewById(R.id.bindLayout);
                viewholder.toggleBtn = (ToggleButton)convertView.findViewById(R.id.transferToggle);
                
                
	            if(position==3)	       convertView.setBackgroundResource(R.drawable.listview_first_bg);
	            else if(position == 0) convertView.setBackgroundResource(R.drawable.listview_last_bg);
	            else  				   convertView.setBackgroundResource(R.drawable.listview_bg);
	            
	            viewholder.iv.setBackgroundResource(imgs[position]);
	            viewholder.tv_weibo.setText(AppApiPreference.getWeibos(TransferActivity.this)[position]);
	            viewholder.tv_weibo_nick.setText(entity.getNickName());
	            
	            if(TextUtils.isEmpty(entity.getAccessToken())) {
	            	
	            	viewholder.bindView.setVisibility(View.VISIBLE);
	            	viewholder.toggleBtn.setVisibility(View.GONE);
	            	
	            }else{
	            	viewholder.bindView.setVisibility(View.GONE);
	            	viewholder.toggleBtn.setVisibility(View.VISIBLE);
	            	
	            	if(!toggleArrayList.get(position)) viewholder.toggleBtn.setChecked(true);
	            	else							   viewholder.toggleBtn.setChecked(false);
	            	
	            	OnCheckedChangeListener changeListener = new OnCheckedChangeListener() {
	            		private boolean clickKey;
						@Override
						public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
							if(clickKey) return;
							
							clickKey = !clickKey;
							
							toggleArrayList.set(position, !isChecked);
							Log.i("msg","isChecked = " + isChecked);
							service.setBlogTranserTogglePreferences(toggleArrayList);
							
							clickKey = !clickKey;
							
						}
					};
	            	viewholder.toggleBtn.setOnCheckedChangeListener(changeListener);
	            }
	            
	            return convertView;
	        }
	        
	        
	        private class ViewHolder{
	            	ImageView iv;
	            	TextView tv_weibo;
	            	TextView tv_weibo_nick;
	            	View bindView;
	            	ToggleButton toggleBtn;
	        }
        
    }
	
	private void sendHandlerMessage(String message){
		Message msg = new Message();
		msg.what = AppConstant.NO_0;
		msg.obj  = message;
		mHandler.sendMessage(msg);
	}
	
	private SinaRequestListener sinaRequestListener = new SinaRequestListener() {
		
		@Override
		public void onRequestStart() {
			
		}
		
		@Override
		public void onRequestFault(Throwable fault) {
			sendHandlerMessage("分享新浪失败");
			Log.i("msg","exception sina = " + fault.getMessage());
		}
		
		@Override
		public void onRequestError(SinaBlogException exception) {
			sendHandlerMessage("分享新浪失败");
			Log.i("msg","exception sina = " + exception.getMessage());
		}
		
		@Override
		public void onRequestComplete(String response) {
			sendHandlerMessage("分享新浪成功");
		}
	};
	
	private TencentRequestListener tencentRequestListener =  new TencentRequestListener() {
		
		public void onRequestStart() {
			
		}
		
		public void onRequestFault(Throwable fault) {
			
			sendHandlerMessage("分享腾讯失败");
			Log.i("msg","exception tencent = " + fault.getMessage());
		}
		
		public void onRequestError(TencentException exception) {
			
			sendHandlerMessage("分享腾讯失败");
			Log.i("msg","exception tencent = " + exception.getMessage());
		}
		
		public void onRequestComplete(String response) {
			sendHandlerMessage("分享腾讯成功");
		}
	};
	
	private SohuRequestListener sohuRequestListener =  new SohuRequestListener() {

		public void onRequestStart() {
			
		}

		@Override
		public void onRequestError(SohuBlogException exception) {
			sendHandlerMessage("分享搜狐失败");
			Log.i("msg","exception sohu = " + exception.getMessage());
		}

		@Override
		public void onRequestFault(Throwable fault) {
			sendHandlerMessage("分享搜狐失败");
			Log.i("msg","exception sohu = " + fault.getMessage());
		}
		

		@Override
		public void onRequestComplete(String response) {
			sendHandlerMessage("分享搜狐成功");
		}
	};
	
	
	
	private WangyiRequestListener wangyiRequestListener  = new WangyiRequestListener() {
		
		public void onRequestStart() {
			
		}
		
		public void onRequestFault(Throwable fault) {
			sendHandlerMessage("分享网易失败");
			Log.i("msg","exception wangyi = " + fault.getMessage());
		}
		
		public void onRequestError(TBlogException exception) {
			sendHandlerMessage("分享网易失败");
			Log.i("msg","exception wangyi = " + exception.getMessage());
		}
		
		public void onRequestComplete(String response) {
			sendHandlerMessage("分享网易成功");
	      	  	  
		}
	};
}
