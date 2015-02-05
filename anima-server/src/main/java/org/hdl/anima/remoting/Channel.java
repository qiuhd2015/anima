package org.hdl.anima.remoting;

import java.net.InetSocketAddress;
/**
 * Channel
 * @author qiuhd
 * @since  2014-7-24
 * @version V1.0.0
 */
public interface Channel extends Endpoint {
	/**
	 * Is connected
	 * @return 
	 */
	boolean isConnected();
	/**
	 * Return remote address
	 * @return
	 */
	InetSocketAddress getRemoteAddress();
	/**
	 * Return the value of key
	 * @param key
	 * @return
	 */
	Object getAttribute(String key);
	/**
	 * Set up attribute
	 * @param key
	 * @param object
	 * @return
	 */
	void setAttribute(String key,Object object);
	/**
	 * Return true if contain key
	 * @param key
	 * @return
	 */
	boolean contains(String key);
    /**
     * Remove attribute.
     * @param key key.
     */
    void removeAttribute(String key);
}

