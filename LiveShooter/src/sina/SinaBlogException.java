package sina;
public class SinaBlogException extends Exception {
	
	private static final long serialVersionUID = -2623309261327598087L;

	private int statusCode = -1;

    public SinaBlogException(String msg) {
        super(msg);
    }

    public SinaBlogException(Exception cause) {
        super(cause);
    }

    public SinaBlogException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public SinaBlogException(String msg, Exception cause) {
        super(msg, cause);
    }

    public SinaBlogException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
