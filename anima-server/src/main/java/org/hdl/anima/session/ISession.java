package org.hdl.anima.session;

import java.net.InetSocketAddress;

import org.hdl.anima.protocol.AbstractMessage;
/**
 * Basic Session Interface 
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public interface ISession{
	
	public static final int STATUS_CLOSED = -1;
	
	public static final int STATUS_CONNECTED = 1;
	
	public static final int STATUS_HANDSNAKINGT = 2;
	
	public static final int STATUS_FREEZE  = 3;
	
	public static final int STATUS_WORKING = 4;
	
	/**
	 * 返回会话id
	 * @return
	 */
	int getId();
	/**
	 * 设置会话id
	 * @param id
	 */
	void setId(int id);
	/**
	 * 返回会话所属前端服务器id
	 * @return
	 */
	String getServerId();
	/**
	 * 设置会话所属前端服务器id
	 * @return
	 */
	void setServerId(String serverId);
	/**
	 * 设置会话断开监听器
	 * @param listener
	 */
	void setlistener(SessionCloseListener listener);
	
	int getStatus();
	
	void setStatus(int status);
	
	boolean isWorking();
	
	void setAttachment(Object attachment);
	
	Object getAttachment();
	/**
	 * Return remote address 
	 * @return
	 */
	InetSocketAddress getRemoteAddress();
	/**
	 * Return value of the key
	 * @param key
	 * @return
	 */
	Object getAttribute(String key);
	/**
	 * set up attribute
	 * @param key
	 * @param object
	 * @return
	 */
	void setAttribute(String key,Object object);
	/**
	 * contains attribute
	 * @param key
	 * @return
	 */
	boolean contains(String key);
    /**
     * remove attribute.
     * 
     * @param key key.
     */
    void removeAttribute(String key);
	/**
	 * Return local address
	 */
	InetSocketAddress getLocalAddress();
	/**
	 * Send message to the session
	 */
	void send(AbstractMessage message) ;
	/**
	 * Close session
	 */
	void close();
	/**
	 * Timeout close session
	 */
	void close(int timeout);
	/**
	 * Is closed
	 */
	boolean isClosed();
}

