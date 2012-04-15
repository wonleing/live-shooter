package tencent.utils;

public interface TencentAuthRequestListener {
	
	public void onAuthRequestStart();

	public void onAuthRequestComplete(String response);

	public void onAuthRequestError(TencentException exception);

	public void onAuthRequestFault(Throwable fault);

}
