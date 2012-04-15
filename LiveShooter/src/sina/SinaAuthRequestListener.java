package sina;


public interface SinaAuthRequestListener {
	
	public void onAuthRequestStart();

	public void onAuthRequestComplete(String response);

	public void onAuthRequestError(SinaBlogException exception);

	public void onAuthRequestFault(Throwable fault);

}
