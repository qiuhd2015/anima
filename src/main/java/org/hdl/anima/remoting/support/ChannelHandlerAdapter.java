package org.hdl.anima.remoting.support;

import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;

/**
 * Channel handler adapter
 * @author qiuhd
 */
public class ChannelHandlerAdapter implements ChannelHandler {

	@Override
	public void caught(Channel channel,Throwable cause ) throws RemotingException {
	}

	@Override
	public void connected(Channel channel) throws RemotingException {
		//do nothing
	}

	@Override
	public void disconnected(Channel channel) throws RemotingException {
		//do nothing
	}

	@Override
	public void received(Channel channel,Object message) throws RemotingException {
		//do nothing
	}

	@Override
	public void sent(Channel channel,Object message) throws RemotingException {
		//do nothing
	}
}
