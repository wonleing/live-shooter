package tencent.utils;

public class TencentException extends RuntimeException {

	private static final long serialVersionUID = -7614264285563089016L;

	private int mErrorCode;
	private String mRequest;
	private String response;

	public TencentException(String errorMessage) {
		super(errorMessage);
	}

	public TencentException(int errorCode, String errorMessage, String request, String response) {
		super(errorMessage);
		mErrorCode = errorCode;
		mRequest   = request;
		this.response   = response;
		
	}

	public TencentException(Exception e) {
		super(e);
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public String getRequest() {
		return mRequest;
	}

	@Override
	public String toString() {
		
		return "errorCode:" 
				+ mErrorCode 
				+ "\nerrorMessage:"
				+ this.getMessage() 
				+ "\nrequest:" 
				+ mRequest
				+ "\nresponse:"
				+response;
	}
}
