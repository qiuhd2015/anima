package org.hdl.anima.common.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author qiuhd
 * @since 2014-2-25
 * @version V1.0.0
 */
public final class MD5 {

	private static MD5 _instance = new MD5();
	private MessageDigest messageDigest;
	private final Logger logger = LoggerFactory.getLogger(MD5.class);

	private MD5() {
		try {
			this.messageDigest = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			this.logger.error("Could not instantiate the MD5 Message Digest!");
		}
	}

	public static MD5 getInstance() {
		return _instance;
	}

	public synchronized String getHash(String s) {
		byte[] data = s.getBytes();

		this.messageDigest.update(data);

		return toHexString(this.messageDigest.digest());
	}

	private String toHexString(byte[] byteData) {
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < byteData.length; i++) {
			String hex = Integer.toHexString(byteData[i] & 0xFF);
			if (hex.length() == 1) {
				hex = "0" + hex;
			}
			sb.append(hex);
		}

		return sb.toString();
	}
}
