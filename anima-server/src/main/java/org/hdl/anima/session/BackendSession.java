package org.hdl.anima.session;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hdl.anima.Application;
import org.hdl.anima.protocol.AbstractMessage;

import com.google.common.base.Objects;

/**
 * Backend Session
 * @author qiuhd
 * @since  2014年8月15日
 */
public class BackendSession extends AbstractSession{
	
	private InetSocketAddress remoteAddress;
	private InetSocketAddress localAddress;
	private LocalSessionMgr localSessionMgr;
	private BackendSessionMgr backendSessionMgr;
	private String clientType;
	private int userId;
	
	/** 
	 * Attribute map
	 */
	private Map<String, Object> attributes ;
	
	public BackendSession(Application application) {
		this.localSessionMgr = application.getMoulde(LocalSessionMgr.class);
		this.backendSessionMgr = application.getMoulde(BackendSessionMgr.class); 
		attributes = new ConcurrentHashMap<String, Object>(3);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		return remoteAddress;
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}

	@Override
	public void setAttribute(String key, Object value) {
		if (value == null) { 
			attributes.remove(key);
		} else {
			attributes.put(key, value);
		}
	}

	@Override
	public boolean contains(String key) {
		return attributes.containsKey(key);
	}

	@Override
	public void removeAttribute(String key) {
		this.removeAttribute(key);
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return localAddress;
	}

	@Override
	public void send(AbstractMessage message){
		super.send(message);
		localSessionMgr.send(serverId, message);
	}

	@Override
	public void close() {
		super.close();
		if (attributes != null) {
			attributes.clear();
			attributes = null;
		}
	}

	@Override
	public void close(int timeout) {
		close();
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public LocalSessionMgr getLocalSessionMgr() {
		return localSessionMgr;
	}

	public void setLocalSessionMgr(LocalSessionMgr localSessionMgr) {
		this.localSessionMgr = localSessionMgr;
	}

	public void setRemoteAddress(InetSocketAddress remoteAddress) {
		this.remoteAddress = remoteAddress;
	}

	public void setLocalAddress(InetSocketAddress localAddress) {
		this.localAddress = localAddress;
	}
	
	public int getUserId() {
		return this.userId ;
	}
	
	/**
	 * @param userId
	 */
	public void bind(int userId) {
		backendSessionMgr.bind(this.serverId, this.sessionId, userId);
		this.userId = userId;
	}
	
	public void freeze() {
		setStatus(STATUS_FREEZE);
	}

	public boolean isFreeze() {
		return this.status == STATUS_FREEZE;
	}

	public void unfreeze() {
		setStatus(STATUS_WORKING);
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("fronetendServerId", this.serverId).add("sessionId", this.sessionId)
				.add("status", getStrForStatus()).add("remoteAddress", remoteAddress != null ? remoteAddress.toString()
						 : "N/A").toString();
	}
}

