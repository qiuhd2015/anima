package org.hdl.anima.remoting;

import java.util.Collection;
/**
 * Server Interface
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public interface Server extends Endpoint,ChannelHandler{
	/**
	 * Is bound
	 * @return
	 */
	boolean isBound();
	/**
	 * Return all connected channel 
	 * @return
	 */
	Collection<Channel> getChannels();
}

