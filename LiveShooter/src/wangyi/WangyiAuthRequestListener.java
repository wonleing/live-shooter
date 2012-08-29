package wangyi;

import wangyi.TBlogException;

public interface WangyiAuthRequestListener {
	
	public void onAuthRequestStart();

	public void onAuthRequestComplete(String response);

	public void onAuthRequestError(TBlogException exception);

	public void onAuthRequestFault(Throwable fault);

}
