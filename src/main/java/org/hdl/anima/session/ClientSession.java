package org.hdl.anima.session;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;

import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.RemotingException;

import com.google.common.base.Preconditions;
/**
 * ClientSession
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class ClientSession extends AbstractSession {
	
	/**
	 * Channel
	 */
	private Channel channel;
	/**
	 * 客户端与服务器重连token
	 */
	private String reconnectToken;
	/**
	 * Client type
	 */
	private String clientType;
	
	private volatile int reconnectionSeconds = 30;
	
	private volatile long freezeTime = 0L;
	
	public ClientSession(int identity,Channel channel) {
		checkArgument(channel != null,"channel can not be null");
		this.channel = channel;
		this.sessionId = identity;
	}
	
	public String getReconnectToken() {
		return this.reconnectToken ;
	}
	
	public void setReconnectToken(String token) {
		this.reconnectToken = token;
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return channel.getRemoteAddress();
	}

	@Override
	public Object getAttribute(String key) {
		return channel.getAttribute(key);
	}

	@Override
	public void setAttribute(String key, Object object) {
		channel.setAttribute(key, object);
	}

	@Override
	public boolean contains(String key) {
		return channel.contains(key);
	}

	@Override
	public void removeAttribute(String key) {
		this.removeAttribute(key);
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return channel.getLocalAddress();
	}

	@Override
	public void send(AbstractMessage message) {
		super.send(message);
		try {
			channel.send(message);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public void close() {
		super.close();
		this.channel.close();
	}

	@Override
	public void close(int timeout) {
		super.close(timeout);
		this.channel.close(timeout);
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
	
	public Channel getChannel() {
		return this.channel;
	}
	
	public void setReconnectionSeconds(int seconds) {
		this.reconnectionSeconds = seconds;
	}

	public int getReconnectionSeconds() {
		return this.reconnectionSeconds;
	}
	
	public void freeze() {
		this.freezeTime = System.currentTimeMillis();
		setStatus(STATUS_FREEZE);
	}

	public boolean isFreeze() {
		return this.status == STATUS_FREEZE;
	}

	public void unfreeze() {
		this.freezeTime = 0L;
		setStatus(STATUS_WORKING);
	}
	
	public void bindChannel(Channel channel) {
		this.channel = channel;
	}
	
	public boolean isReconnectTimeExpired() {
		long expiry = this.freezeTime + 1000 * this.reconnectionSeconds;
		return System.currentTimeMillis() > expiry;
	}
}

