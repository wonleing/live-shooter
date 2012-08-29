package wangyi;

public interface WangyiRequestListener {
	
	public void onRequestStart();

	public void onRequestComplete(String response);

	public void onRequestError(TBlogException exception);

	public void onRequestFault(Throwable fault);
}
