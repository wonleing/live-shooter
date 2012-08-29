package sohu;


public interface SohuAuthRequestListener {
	
	public void onAuthRequestStart();

	public void onAuthRequestComplete(String response);

	public void onAuthRequestError(SohuBlogException exception);

	public void onAuthRequestFault(Throwable fault);

}
