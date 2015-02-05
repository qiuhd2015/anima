package org.hdl.anima.remoting.support;

import org.hdl.anima.protocol.HeartBeat;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Heartbeat handler
 * @author qiuhd
 * @since 2014年9月3日
 */
public class HeartbeatHandler extends AbstractChannelHandlerDelegate {

	private static final Logger logger = LoggerFactory
			.getLogger(HeartbeatHandler.class);

	public static String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";

	public static String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";

	public HeartbeatHandler(ChannelHandler handler) {
		super(handler);
	}

	public void connected(Channel channel) throws RemotingException {
		setReadTimestamp(channel);
		setWriteTimestamp(channel);
		handler.connected(channel);
	}

	public void disconnected(Channel channel) throws RemotingException {
		clearReadTimestamp(channel);
		clearWriteTimestamp(channel);
		handler.disconnected(channel);
	}

	public void sent(Channel channel, Object message) throws RemotingException {
		setWriteTimestamp(channel);
		handler.sent(channel, message);
	}

	public void received(Channel channel, Object message)
			throws RemotingException {
		setReadTimestamp(channel);
		if (isHeartbeatRequest(message)) {
			HeartBeat req = (HeartBeat) message;
			if (req.isTwoWay()) {
				req.setTwoWay(false);
				channel.send(req);
				int heartbeat = channel.getConf()
						.getInt(Constants.HEARTBEAT_KEY, 0);
				if (logger.isDebugEnabled()) {
					logger.debug("Received heartbeat from remote channel "
							+ channel.getRemoteAddress()
							+ ", cause: The channel has no data-transmission exceeds a heartbeat period"
							+ (heartbeat > 0 ? ": " + heartbeat + "ms" : ""));
				}
			}
			return;
		}
		handler.received(channel, message);
	}

	private void setReadTimestamp(Channel channel) {
		channel.setAttribute(KEY_READ_TIMESTAMP, System.currentTimeMillis());
	}

	private void setWriteTimestamp(Channel channel) {
		channel.setAttribute(KEY_WRITE_TIMESTAMP, System.currentTimeMillis());
	}

	private void clearReadTimestamp(Channel channel) {
		channel.removeAttribute(KEY_READ_TIMESTAMP);
	}

	private void clearWriteTimestamp(Channel channel) {
		channel.removeAttribute(KEY_WRITE_TIMESTAMP);
	}

	private boolean isHeartbeatRequest(Object message) {
		return (message instanceof HeartBeat);
	}
}
