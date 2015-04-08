package org.hdl.anima.session;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * SessionIdFactory
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class SessionIdFactory {
	
	private final AtomicInteger counter = new AtomicInteger(0);
	
	/**
	 * Internal class
	 * @author qiuhd
	 *
	 */
	private static class InternalClass {
		public final static SessionIdFactory INSTANCE = new SessionIdFactory(); 
	}
	
	private SessionIdFactory() {} ;
	
	public static SessionIdFactory getInstance() {
		return InternalClass.INSTANCE;
	}
	
	public int getId() {
		return counter.incrementAndGet();
	}
}

