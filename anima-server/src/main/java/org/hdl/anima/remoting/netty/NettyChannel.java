package org.hdl.anima.remoting.netty;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.support.AbstractChannel;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NettyChannel.
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class NettyChannel extends AbstractChannel {
	
	private static final Logger logger = LoggerFactory.getLogger(NettyChannel.class);
	
	private static final ConcurrentMap<org.jboss.netty.channel.Channel, NettyChannel> channelMap = new ConcurrentHashMap<org.jboss.netty.channel.Channel, NettyChannel>();
	
	private final org.jboss.netty.channel.Channel channel;
	
	private final Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();
	
	public NettyChannel(AppConf conf,Channel channel,ChannelHandler handler) {
		super(conf, handler);
		
		checkArgument(channel != null,"Channel can not be null");
		
		this.channel = channel ;
	}

	static NettyChannel getOrAddChannel(AppConf conf,Channel channel, ChannelHandler handler) {
		if (channel == null) {
			return null;
		}
		NettyChannel ret = channelMap.get(channel);
		if (ret == null) {
			NettyChannel nc = new NettyChannel(conf, channel, handler);
			if (channel.isConnected()) {
				ret = channelMap.putIfAbsent(channel, nc);
			}
			if (ret == null) {
				ret = nc;
			}
		}
		return ret;
	}
	
	static void removeChannelIfDisconnected(org.jboss.netty.channel.Channel ch) {
		if (ch != null && !ch.isConnected()) {
			channelMap.remove(ch);
		}
	}
	
	@Override
	public boolean isConnected() {
		return channel.isConnected();
	}
	
	@Override
	public void close() {
		super.close();
		
		try {
            removeChannelIfDisconnected(channel);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            attributes.clear();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            channel.close();
            logger.debug("Close netty channel " + channel);
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
        }
	}
	
	@Override
	public void send(Object message) throws RemotingException {
		super.send(message);
        try {
            channel.write(message);
        } catch (Throwable e) {
            throw new RemotingException(this, "Failed to send message " + message + " to " + getRemoteAddress() + ", cause: " + e.getMessage(), e);
        }
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
	public void removeAttribute(String key) {
		attributes.remove(key);
	}

	@Override
	public Object getAttribute(String key) {
		return attributes.get(key);
	}
	
	@Override
	public boolean contains(String key) {
		return attributes.containsKey(key);
	}

	@Override
	public InetSocketAddress getLocalAddress() {
		return (InetSocketAddress) this.channel.getLocalAddress();
	}
	
	@Override
	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress) channel.getRemoteAddress();
	}
}

