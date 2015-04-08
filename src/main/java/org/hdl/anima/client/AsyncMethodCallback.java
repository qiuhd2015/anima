package org.hdl.anima.client;

/**
 * Async method callback
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public interface AsyncMethodCallback<T> {

	void onCompleted(T responseArg);
	
	void onError(AsyncMethodCallbackException exception);
}
