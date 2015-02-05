package org.hdl.anima.session;

import java.util.Random;

import org.hdl.anima.common.utils.MD5;

/**
 * @author qiuhd
 * @since  2014年9月1日
 */
public class TokenFactory {

	private static TokenFactory instance;
	private Random random = new Random();
	private static final String DELIMITER = "__";	
	
	private TokenFactory() {} ;
	
	public static TokenFactory getInstance() {
		if (instance == null) {
			synchronized (TokenFactory.class) {
				if (instance == null) {
					instance = new TokenFactory();
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

