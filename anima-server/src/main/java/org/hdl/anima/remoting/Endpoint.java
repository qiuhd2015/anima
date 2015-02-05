package org.hdl.anima.remoting;

import java.net.InetSocketAddress;

import org.hdl.anima.AppConf;

/**
 * Endpoint
 * @author qiuhd
 * @since  2014-7-24
 * @version V1.0.0
 */
public interface Endpoint {
	/**
	 * Return {@linkplain AppConf}
	 * @return
	 */
	AppConf getConf();
	/**
	 * Return local address
	 * @return
	 */
	InetSocketAddress getLocalAddress();
	/**
	 * Return channel handler
	 * @return
	 */
	ChannelHandler getChannelHandler();
	/**
	 * Send message to this channel
	 * @param message
	 * @throws RemotingException
	 */
	void send(Object message) throws RemotingException;
	/**
	 * Close this channel
	 */
	void close();
	/**
	 * Close delay
	 * @param timeout
	 */
	void close(int timeout);
	/**
	 * Is closed
	 * @return
	 */
	boolean isClosed();
}

