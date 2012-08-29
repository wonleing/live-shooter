package com.android.liveshooter.view;


import java.util.Date;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.liveshooter.R;

public class PullListView extends ListView implements OnScrollListener {  
	private static final String TAG = "listview";

	private final static int RELEASE_To_REFRESH = 0;
	private final static int PULL_To_REFRESH = 1;
	private final static int PUSH_To_REFRESH = 1;
	private final static int REFRESHING = 2;
	private final static int DONE = 3;
	private final static int LOADING = 4;

	// 实际的padding的距离与界面上偏移距离的比例
	private final static int RATIO = 3;

	private LayoutInflater inflater;

	private LinearLayout headView;
	private TextView     head_tipsTextview;
	private TextView     head_lastUpdatedTextView;
	private ImageView    head_arrowImageView;
	private ProgressBar  head_progressBar;
	
	private LinearLayout footView;
	private TextView     foot_tipsTextview;
	private ImageView    foot_arrowImageView;
	private ProgressBar  foot_progressBar;


	private RotateAnimation animation;
	private RotateAnimation reverseAnimation;

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean head_isRecored;
    private boolean foot_isRecored;
	

	private int headContentWidth;
	private int headContentHeight;
	private int footContentWidth;
	private int footContentHeight;

	private int startY;
	private int firstItemIndex;
	private int itemSum;
	private boolean isLastItem;

	private int head_state;
	private int foot_state;

	private boolean head_isBack;
	private boolean foot_isBack;

	private OnRefreshListener refreshListener;
	private PushRefreshListener pushRefreshListener;

	private boolean head_isRefreshable;
	private boolean foot_isRefreshable;
	private boolean isPush;
	

	public PullListView(Context context) {
		super(context);
		init(context);
	}

	public PullListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
	
		initHeadView(context);	
		initFootView(context);
		addHeaderView(headView, null, false);
        addFooterView(footView, null, false);
        
		setOnScrollListener(this);

		animation = new RotateAnimation(0, -180,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		animation.setInterpolator(new LinearInterpolator());
		animation.setDuration(250);
		animation.setFillAfter(true);

		reverseAnimation = new RotateAnimation(-180, 0,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);
		reverseAnimation.setInterpolator(new LinearInterpolator());
		reverseAnimation.setDuration(250);
		reverseAnimation.setFillAfter(true);

		head_state = DONE;
		foot_state = DONE;
		head_isRefreshable = false;
		foot_isRefreshable = false;
		isPush = true;
	}

	public void onScroll(AbsListView arg0, int firstVisiableItem, int showItemSum,
			int itemSum) {
		firstItemIndex = firstVisiableItem;
		this.itemSum = itemSum;	
		int sum = itemSum - firstVisiableItem;
		
		if(showItemSum == sum){
			isLastItem = true;		
		}else{
			isLastItem = false;
		}

		
		
		if(firstItemIndex == 1 && !isPush){
			setSelection(0);
		}
	
	}

	public void onScrollStateChanged(AbsListView arg0, int arg1) {
	}

	public boolean onTouchEvent(MotionEvent event) {

		if (head_isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (firstItemIndex == 0 && !head_isRecored) {
					head_isRecored = true;
					isPush = true;
					startY = (int) event.getY();
					Log.v(TAG, "在down时候记录当前位置‘");
				}
				break;

			case MotionEvent.ACTION_UP:

				if (head_state != REFRESHING && head_state != LOADING) {
					if (head_state == DONE) {
						// 什么都不做
					}
					if (head_state == PULL_To_REFRESH) {
						head_state = DONE;
						changeHeaderViewByState();

						Log.v(TAG, "由下拉刷新状态，到done状态");
					}
					if (head_state == RELEASE_To_REFRESH) {
						head_state = REFRESHING;
						changeHeaderViewByState();
						onRefresh();

						Log.v(TAG, "由松开刷新状态，到done状态");
					}
				}

				head_isRecored = false;
				head_isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!head_isRecored && firstItemIndex == 0) {
					Log.v(TAG, "在move时候记录下位置");
					
					head_isRecored = true;
					startY = tempY;
				}

				if (head_state != REFRESHING && head_isRecored && head_state != LOADING) {

					// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

					// 可以松手去刷新了
					if (head_state == RELEASE_To_REFRESH) {

						setSelection(0);

						// 往上推了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((tempY - startY) / RATIO < headContentHeight)
								&& (tempY - startY) > 0) {
							head_state = PULL_To_REFRESH;
							changeHeaderViewByState();

							Log.v(TAG, "由松开刷新状态转变到下拉刷新状态");
						}
						// 一下子推到顶了
						else if (tempY - startY <= 0) {
							head_state = DONE;
							changeHeaderViewByState();

							Log.v(TAG, "由松开刷新状态转变到done状态");
						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (head_state == PULL_To_REFRESH) {

						setSelection(0);

						// 下拉到可以进入RELEASE_TO_REFRESH的状态
						if ((tempY - startY) / RATIO >= headContentHeight) {
							head_state = RELEASE_To_REFRESH;
							head_isBack = true;
							changeHeaderViewByState();

							Log.v(TAG, "由done或者下拉刷新状态转变到松开刷新");
						}
						// 上推到顶了
						else if (tempY - startY <= 0) {
							head_state = DONE;
							changeHeaderViewByState();
//							System.out.println("++++++++++++++++=上推到顶");					
							isPush = false;
							Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (head_state == DONE) {
						if (tempY - startY > 0) {
							head_state = PULL_To_REFRESH;
							changeHeaderViewByState();
						}
					}

					// 更新headView的size
					if (head_state == PULL_To_REFRESH) {
						headView.setPadding(0, -1 * headContentHeight
								+ (tempY - startY) / RATIO, 0, 0);

					}

					// 更新headView的paddingTop
					if (head_state == RELEASE_To_REFRESH) {
						headView.setPadding(0, (tempY - startY) / RATIO
								- headContentHeight, 0, 0);
					}

				}

				break;
			}
		}
		
		
		if (foot_isRefreshable) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				if (isLastItem && !foot_isRecored) {
					foot_isRecored = true;
					startY = (int) event.getY();
					Log.v(TAG, "在down时候记录当前位置‘");
				}
				break;

			case MotionEvent.ACTION_UP:

				if (foot_state != REFRESHING && foot_state != LOADING) {
					if (foot_state == DONE) {
						// 什么都不做
					}
					if (foot_state == PUSH_To_REFRESH) {
						foot_state = DONE;
						changeFootViewByState();
						Log.v(TAG, "由上推刷新状态，到done状态");
					}
					if (foot_state == RELEASE_To_REFRESH) {
						foot_state = REFRESHING;
						changeFootViewByState();
						pushRefresh();
						Log.v(TAG, "由松开刷新状态，到done状态");
					}
				}

				foot_isRecored = false;
				foot_isBack = false;

				break;

			case MotionEvent.ACTION_MOVE:
				int tempY = (int) event.getY();

				if (!foot_isRecored && isLastItem) {
					Log.v(TAG, "在move时候记录下位置");
					foot_isRecored = true;
					startY = tempY;
				}

				if (foot_state != REFRESHING && foot_isRecored && foot_state != LOADING) {

					
					// 保证在设置padding的过程中，当前的位置一直是在head，否则如果当列表超出屏幕的话，当在上推的时候，列表会同时进行滚动

					// 可以松手去刷新了
					if (foot_state == RELEASE_To_REFRESH) {

						this.setSelection(itemSum-1);

						// 往下拉了，推到了屏幕足够掩盖head的程度，但是还没有推到全部掩盖的地步
						if (((startY - tempY) / RATIO < footContentHeight)
								&& (startY - tempY) > 0) {
							foot_state = PUSH_To_REFRESH;
							changeFootViewByState();
							Log.v(TAG, "由松开刷新状态转变到上推刷新状态");
						}
						// 一下子下拉到顶了
						else if (startY - tempY <= 0) {
							foot_state = DONE;
							changeFootViewByState();
							Log.v(TAG, "由松开刷新状态转变到done状态");
						}
						// 往下拉了，或者还没有上推到屏幕顶部掩盖head的地步
						else {
							// 不用进行特别的操作，只用更新paddingTop的值就行了
						}
					}
					// 还没有到达显示松开刷新的时候,DONE或者是PULL_To_REFRESH状态
					if (foot_state == PUSH_To_REFRESH) {

						this.setSelection(itemSum-1);

						// 上推到可以进入RELEASE_TO_REFRESH的状态
						if ((startY - tempY) / RATIO >= footContentHeight) {
							foot_state = RELEASE_To_REFRESH;
							foot_isBack = true;
							changeFootViewByState();
							Log.v(TAG, "由done或者上推刷新状态转变到松开刷新");
						}
						// 下拉到顶了
						else if (startY - tempY <= 0) {
							foot_state = DONE;
							changeFootViewByState();
							Log.v(TAG, "由DOne或者下拉刷新状态转变到done状态");
						}
					}

					// done状态下
					if (foot_state == DONE) {
						if (startY - tempY > 0) {
							foot_state = PUSH_To_REFRESH;

							changeFootViewByState();
						}
					}

					// 更新headView的size
					if (foot_state == PUSH_To_REFRESH) {
											
						footView.setPadding(0, 0, 0, (startY - tempY) / RATIO- footContentHeight);

					}

					// 更新headView的paddingTop
					if (foot_state == RELEASE_To_REFRESH) {
						footView.setPadding(0, 0, 0, (startY - tempY) / RATIO- footContentHeight);
						
					}

				}

				break;
			}
		}

		
		
		
		
		
		
		
		
//		return true;
		return super.onTouchEvent(event);
	}

	// 当状态改变时候，调用该方法，以更新界面
	private void changeHeaderViewByState() {
		switch (head_state) {
		case RELEASE_To_REFRESH:
			head_arrowImageView.setVisibility(View.VISIBLE);
			head_progressBar.setVisibility(View.GONE);
			head_tipsTextview.setVisibility(View.VISIBLE);
			head_lastUpdatedTextView.setVisibility(View.VISIBLE);

			head_arrowImageView.clearAnimation();
			head_arrowImageView.startAnimation(animation);

			head_tipsTextview.setText("松开刷新");

			Log.v(TAG, "当前状态，松开刷新");
			break;
		case PULL_To_REFRESH:
			head_progressBar.setVisibility(View.GONE);
			head_tipsTextview.setVisibility(View.VISIBLE);
			head_lastUpdatedTextView.setVisibility(View.VISIBLE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (head_isBack) {
				head_isBack = false;
				head_arrowImageView.clearAnimation();
				head_arrowImageView.startAnimation(reverseAnimation);

				head_tipsTextview.setText("下拉刷新");
			} else {
				head_tipsTextview.setText("下拉刷新");
			}
			Log.v(TAG, "当前状态，下拉刷新");
			break;

		case REFRESHING:

			headView.setPadding(0, 0, 0, 0);

			head_progressBar.setVisibility(View.VISIBLE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView.setVisibility(View.GONE);
			head_tipsTextview.setText("正在刷新...");
			head_lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "当前状态,正在刷新...");
			break;
		case DONE:
			headView.setPadding(0, -1 * headContentHeight, 0, 0);

			head_progressBar.setVisibility(View.GONE);
			head_arrowImageView.clearAnimation();
			head_arrowImageView.setImageResource(R.drawable.pulltorefresh);
			head_tipsTextview.setText("下拉刷新");
			head_lastUpdatedTextView.setVisibility(View.VISIBLE);

			Log.v(TAG, "当前状态，done");
			break;
		}
	}
	private void changeFootViewByState(){
		switch (foot_state) {
		case RELEASE_To_REFRESH:
			foot_arrowImageView.setVisibility(View.VISIBLE);
			foot_progressBar.setVisibility(View.GONE);
			foot_tipsTextview.setVisibility(View.VISIBLE);

			foot_arrowImageView.clearAnimation();
			foot_arrowImageView.startAnimation(animation);

			foot_tipsTextview.setText("松开获取更多");

			Log.v(TAG, "当前状态，松开刷新");
			break;
		case PUSH_To_REFRESH:
			foot_progressBar.setVisibility(View.GONE);
			foot_tipsTextview.setVisibility(View.VISIBLE);
			foot_arrowImageView.clearAnimation();
			foot_arrowImageView.setVisibility(View.VISIBLE);
			// 是由RELEASE_To_REFRESH状态转变来的
			if (foot_isBack) {
				foot_isBack = false;
				foot_arrowImageView.clearAnimation();
				foot_arrowImageView.startAnimation(reverseAnimation);

				foot_tipsTextview.setText("上推获取更多");
			} else {
				foot_tipsTextview.setText("上推获取更多");
			}
			Log.v(TAG, "当前状态，上推获取更多");
			break;

		case REFRESHING:

			footView.setPadding(0, 0, 0, 0);

			foot_progressBar.setVisibility(View.VISIBLE);
			foot_arrowImageView.clearAnimation();
			foot_arrowImageView.setVisibility(View.GONE);
			foot_tipsTextview.setText("正在获取更多...");

			Log.v(TAG, "当前状态,正在获取更多...");
			break;
		case DONE:
			
			footView.setPadding(0, 0, 0, -1*footContentHeight);

			foot_progressBar.setVisibility(View.GONE);
			foot_arrowImageView.clearAnimation();
			foot_arrowImageView.setImageResource(R.drawable.pushtorefresh);
			foot_tipsTextview.setText("上推获取更多");

			Log.v(TAG, "当前状态，done");
			break;
		}
		
	}
	
	
	

	public void setonRefreshListener(OnRefreshListener refreshListener) {
		this.refreshListener = refreshListener;
		head_isRefreshable = true;
	}	
	public void setPushRefreshListener(PushRefreshListener refreshListener){
		this.pushRefreshListener = refreshListener;
		foot_isRefreshable = true;
	}
	

	public interface OnRefreshListener {
		
		public void onRefresh();
		
	}
	public interface PushRefreshListener{
		
		public void onRefresh();
		
	}

	
	public void onRefreshComplete() {
		head_state = DONE;
		head_lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		changeHeaderViewByState();
		invalidateViews();
		setSelection(0);
	}
    public void pushRefreshComplete(){
    	foot_state = DONE;
		changeFootViewByState();
		invalidateViews();
		setSelection(itemSum-1);
    }
	
	
	private void onRefresh() {
		
		if (refreshListener != null) {
			refreshListener.onRefresh();
		}
		
	}
	private void pushRefresh(){
		
		if (pushRefreshListener != null) {
			pushRefreshListener.onRefresh();
		}
	}
	
	
	public void clickToRefresh(){
		head_state = REFRESHING;
		changeHeaderViewByState();
	}
	public void clickPushRefresh(){
		foot_state = REFRESHING;
		changeFootViewByState();	
	}
	

	
	private void initHeadView(Context context){
		
		inflater        	= LayoutInflater.from(context);
		headView       		= (LinearLayout) inflater.inflate(R.layout.pulllist_head, null);
		head_arrowImageView 		= (ImageView) headView.findViewById(R.id.head_arrowImageView);
		head_progressBar    		= (ProgressBar) headView.findViewById(R.id.head_progressBar);
		head_tipsTextview   		= (TextView) headView.findViewById(R.id.head_tipsTextView);
		head_lastUpdatedTextView = (TextView) headView.findViewById(R.id.head_lastUpdatedTextView);
		
		measureView(headView);
		
		headContentHeight = headView.getMeasuredHeight();
		headContentWidth  = headView.getMeasuredWidth();
		
		headView.setPadding(0, -1 * headContentHeight, 0, 0);
		headView.invalidate();
		
		Log.v("size", "width:" + headContentWidth + " height:"
				+ headContentHeight);	
		
	}	
	private void initFootView(Context context){
		
		inflater        	     	= LayoutInflater.from(context);
		footView       				= (LinearLayout) inflater.inflate(R.layout.pulllist_foot, null);
		foot_arrowImageView 		= (ImageView) footView.findViewById(R.id.foot_arrowImageView);
		foot_progressBar    		= (ProgressBar) footView.findViewById(R.id.foot_progressBar);
		foot_tipsTextview   		= (TextView) footView.findViewById(R.id.foot_tipsTextView);
		
		measureView(footView);
		
		footContentHeight = footView.getMeasuredHeight();
		footContentWidth  = footView.getMeasuredWidth();
		
		footView.setPadding(0,0, 0,-1 * footContentHeight);
		footView.invalidate();
		
		Log.v("size", "width:" + footContentWidth + " height:"
				+ footContentHeight);	
		
	}
	
	
	
	
	
	
	// 此方法直接照搬自网络上的一个下拉刷新的demo，此处是“估计”headView的width以及height
	private void measureView(View child) {
		ViewGroup.LayoutParams p = child.getLayoutParams();
		if (p == null) {
			p = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
		}
		int childWidthSpec = ViewGroup.getChildMeasureSpec(0, 0 + 0, p.width);
		int lpHeight = p.height;
		int childHeightSpec;
		if (lpHeight > 0) {
			childHeightSpec = MeasureSpec.makeMeasureSpec(lpHeight,
					MeasureSpec.EXACTLY);
		} else {
			childHeightSpec = MeasureSpec.makeMeasureSpec(0,
					MeasureSpec.UNSPECIFIED);
		}
		child.measure(childWidthSpec, childHeightSpec);
	}

	public void setAdapter(BaseAdapter adapter) {
		head_lastUpdatedTextView.setText("最近更新:" + new Date().toLocaleString());
		super.setAdapter(adapter);
	}
}  
