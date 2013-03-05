package com.android.liveshooter.adapter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.android.liveshooter.util.ImageProcess;

/**
 * List View的Adapter
 * @author jianping
 *
 */
public class FeedAdapter extends SimpleAdapter {

	private int[] mTo;
    private String[] mFrom;
    private ViewBinder mViewBinder;

    private List<? extends Map<String, ?>> mData;

    private int mResource;
    private LayoutInflater mInflater;
    
    private Handler handler;
	
	public FeedAdapter(Context context, Handler handle,
			List<? extends Map<String, ?>> data, int resource, String[] from,
			int[] to) {
		super(context, data, resource, from, to);
		handler = handle;
		mData = data;
        mResource =resource;
        mFrom = from;
        mTo = to;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	
	
	 /**
     * @see android.widget.Adapter#getView(int, View, ViewGroup)
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, mResource);
    }

    private View createViewFromResource(int position, View convertView,
            ViewGroup parent, int resource) {
        View v = null;
        if (convertView == null) {
            v = mInflater.inflate(resource, parent, false);

            final int[] to = mTo;
            final int count = to.length;
            final View[] holder = new View[count];

            for (int i = 0; i < count; i++) {
                holder[i] = v.findViewById(to[i]);
            }

            v.setTag(holder);
        } else {
            v = convertView;
        }

        bindView(position, v);

        return v;
    }
    
    private void bindView(int position, View view) {
    	final int dpos = position;
        final Map dataSet = mData.get(position);
        if (dataSet == null) {
            return;
        }

        final ViewBinder binder = mViewBinder;
        final View[] holder = (View[]) view.getTag();
        final String[] from = mFrom;
        final int[] to = mTo;
        final int count = to.length;

        for (int i = 0; i < count; i++) {
            final View v = holder[i];
            if (v != null) {
                final Object data = dataSet.get(from[i]);
                String text = data == null ? "" : data.toString();
                if (text == null) {
                    text = "";
                }
                
                if(v instanceof ImageButton){ //如果是图片按钮（视频播放）
                	v.setOnClickListener(new OnClickListener(){

						@Override
						public void onClick(View v) {
							Message msg = new Message();
	                        msg.what = 1;
	                        Bundle bundle = new Bundle();
	                        bundle.putInt("pos", dpos);
	                        msg.setData(bundle);
	                        handler.sendMessage(msg);
						}
            			
            		});
                	continue;
                }

                boolean bound = false;
                if (binder != null) {
                    bound = binder.setViewValue(v, data, text);
                }

                if (!bound) {
                    if (v instanceof Checkable) {
                        if (data instanceof Boolean) {
                            ((Checkable) v).setChecked((Boolean) data);
                        } else {
                            throw new IllegalStateException(v.getClass().getName() +
                                    " should be bound to a Boolean, not a " + data.getClass());
                        }
                    } else if (v instanceof TextView) { //如果是TextView对象
                        // Note: keep the instanceof TextView check at the bottom of these
                        // ifs since a lot of views are TextViews (e.g. CheckBoxes).
//                    	if(mTo[i] == R.id.info3){
//                    		((TextView)v).setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
//                    		TextPaint tp = ((TextView)v).getPaint();  
//                    		tp.setFakeBoldText(true); //设置仿粗体
//                    	}
                    	if(text == null || text.length() == 0){
                    		((TextView)v).setVisibility(View.GONE);
                    	}
                    	else{
                    		((TextView)v).setVisibility(View.VISIBLE);
                    		setViewText((TextView) v, text);
                    	}
                    } else if (v instanceof ImageView) { //如果是图像对象
                        if(data == null){
                        	//((ImageView)v).setImageResource(R.drawable.nobody);
                        }
                        else if (data instanceof Integer) {
                            setViewImage((ImageView) v, (Integer) data);                            
                        } else {
                            setViewImage((ImageView) v, text);
                        }
                        v.setOnClickListener(new OnClickListener(){ //图像点击事件

							@Override
							public void onClick(View v) {
								Message msg = new Message();
		                        msg.what = 5;
		                        Bundle bundle = new Bundle();
		                        bundle.putInt("pos", dpos);
		                        msg.setData(bundle);
		                        handler.sendMessage(msg); //通过消息执行事件处理操作
							}
                        	
                        });
                   
                    } else {
                        throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                " view that can be bounds by this SimpleAdapter");
                    }
                }
            }
        }
    }
    
    /**
     * Called by bindView() to set the image for an ImageView but only if
     * there is no existing ViewBinder or if the existing ViewBinder cannot
     * handle binding to an ImageView.
     *
     * This method is called instead of {@link #setViewImage(ImageView, String)}
     * if the supplied data is an int or Integer.
     *
     * @param v ImageView to receive an image
     * @param value the value retrieved from the data set
     *
     * @see #setViewImage(ImageView, String)
     */
    public void setViewImage(ImageView v, int value) {
        v.setImageResource(value);
        v.setScaleType(ImageView.ScaleType.FIT_XY);
    }
    
    /**
     * 显示网络图像
     */
    public void setViewImage(ImageView v, String value) {
    	Bitmap bitmap = null;
		try {
			bitmap = new ImageProcess().getBitmap(value);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(bitmap != null){
			((ImageView) v).setImageBitmap(bitmap);
			((ImageView)v).setScaleType(ImageView.ScaleType.FIT_XY);
		}
    }

}
