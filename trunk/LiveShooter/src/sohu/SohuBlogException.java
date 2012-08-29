package sohu;
public class SohuBlogException extends Exception {
	
	private static final long serialVersionUID = -2623309261327598087L;

	private int statusCode = -1;

    public SohuBlogException(String msg) {
        super(msg);
    }

    public SohuBlogException(Exception cause) {
        super(cause);
    }

    public SohuBlogException(String msg, int statusCode) {
        super(msg);
        this.statusCode = statusCode;

    }

    public SohuBlogException(String msg, Exception cause) {
        super(msg, cause);
    }

    public SohuBlogException(String msg, Exception cause, int statusCode) {
        super(msg, cause);
        this.statusCode = statusCode;

    }

    public int getStatusCode() {
        return this.statusCode;
    }
}
