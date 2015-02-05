package org.hdl.anima.client;
/**
 * Async method callback exception
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public class AsyncMethodCallbackException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 532317997666530664L;

	private int errorCode;
	
	public AsyncMethodCallbackException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public AsyncMethodCallbackException(int errorCode,String message, Throwable t) {
		super(message, t);
		this.errorCode = errorCode;
	}

	public AsyncMethodCallbackException(int errorCode,String message) {
		super(message);
		this.errorCode = errorCode;
	}
	
	public int getErrorCode() {
		return errorCode;
	}
}
