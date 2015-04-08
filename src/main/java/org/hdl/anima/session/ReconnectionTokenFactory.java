package org.hdl.anima.session;

import java.util.Random;

import org.hdl.anima.common.utils.MD5;

/**
 * ReconnectionTokenFactory.
 * @author qiuhd
 * @since  2014年9月1日
 */
public class ReconnectionTokenFactory {

	private static ReconnectionTokenFactory instance;
	private Random random = new Random();
	private static final String DELIMITER = "__";	
	
	private ReconnectionTokenFactory() {} ;
	
	public static ReconnectionTokenFactory getInstance() {
		if (instance == null) {
			synchronized (ReconnectionTokenFactory.class) {
				if (instance == null) {
					instance = new ReconnectionTokenFactory();
				}
			}
		}
		return instance;
	}

	public String getUniqueSessionToken(String str) {
		String key = str + DELIMITER + String.valueOf(random.nextInt());
		return MD5.getInstance().getHash(key);
	}
}

