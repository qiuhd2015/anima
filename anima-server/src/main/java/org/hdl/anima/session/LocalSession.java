package org.hdl.anima.session;

import java.net.InetSocketAddress;

import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.RemotingException;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * 服务器之间通信session
 * @author qiuhd
 * @since  2014年8月13日
 */
public class LocalSession extends AbstractSession{
	
	private final Channel channel;					//
	
	public LocalSession(Channel channel) {
		Preconditions.checkArgument(channel != null,"channel == null");
		this.channel = channel;
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
		Preconditions.checkArgument(message != null, "message == null");
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


	@Override
	public int hashCode() {
		return Objects.hashCode(serverId,serverType,sessionId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalSession other = (LocalSession) obj;
		if (sessionId != other.sessionId)
			return false;
		if (serverId != other.serverId) 
			return false;
		if (serverType != other.serverType)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("serverId", serverId).add("serverType", serverType).add("sessionId",sessionId).toString();
	}
}

