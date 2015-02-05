package org.hdl.anima.remoting.support;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;

/**
 * Abstract channel
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public abstract class AbstractChannel extends AbstractPeer implements Channel{

	
	public AbstractChannel(AppConf conf, ChannelHandler handler) {
		super(conf, handler);
	}

	public void send(Object message) throws RemotingException {
		if (isClosed()) {
			throw new RemotingException(this, "Failed to send message "
					+ (message == null ? "" : message.getClass().getName())
					+ ":" + message + ", cause: Channel closed. channel: "
					+ getLocalAddress() + " -> " + getRemoteAddress());
		}
	}

	@Override
	public String toString() {
		return getLocalAddress() + " -> " + getRemoteAddress();
	}
}

