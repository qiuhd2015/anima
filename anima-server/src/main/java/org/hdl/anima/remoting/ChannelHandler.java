package org.hdl.anima.remoting;
/**
 * Channel handler
 * @author qiuhd
 * @since  2014-7-24
 * @version V1.0.0
 */
public interface ChannelHandler {

	/**
	 * Channel connect
	 * @param channel		
	 * @throws RemotingException
	 */
	void connected(Channel channel) throws RemotingException;
	/**
	 * Channel disconnect
	 * @param channel		
	 * @throws RemotingException
	 */
	void disconnected(Channel channel) throws RemotingException;
	/**
	 * Send message to channel
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 */
	void sent(Channel channel,Object message) throws RemotingException;
	/**
	 * Receive message from channel
	 * @param channel
	 * @param message
	 * @throws RemotingException
	 */
	void received(Channel channel,Object message) throws RemotingException;
	/**
	 * Caught exception from channel
	 * @param channel
	 * @param t
	 * @throws RemotingException
	 */
	void caught(Channel channel,Throwable t) throws RemotingException;
}