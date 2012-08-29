package sohu;


public interface SohuRequestListener {
	
	public void onRequestStart();

	public void onRequestComplete(String response);

	public void onRequestError(SohuBlogException exception);

	public void onRequestFault(Throwable fault);

}
