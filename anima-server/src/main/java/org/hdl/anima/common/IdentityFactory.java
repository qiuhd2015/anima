package org.hdl.anima.common;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class IdentityFactory {
	
	private static IdentityFactory instance;
	
//	private Random random = new Random();
	
	private final AtomicInteger counter = new AtomicInteger(0);
	
	private IdentityFactory() {} ;
	
	public static IdentityFactory getInstance() {
		if (instance == null) {
			synchronized (IdentityFactory.class) {
				if (instance == null) {
					instance = new IdentityFactory();
				}
			}
		}
		
		return instance;
	}
	
	public int getIdentity() {
		return counter.incrementAndGet();
	}
	
}

