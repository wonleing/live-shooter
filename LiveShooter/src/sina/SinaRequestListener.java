package sina;

public interface SinaRequestListener {
	
	public void onRequestStart();

	public void onRequestComplete(String response);

	public void onRequestError(SinaBlogException exception);

	public void onRequestFault(Throwable fault);
}
