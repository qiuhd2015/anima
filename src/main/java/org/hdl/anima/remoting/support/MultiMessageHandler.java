package org.hdl.anima.remoting.support;

import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;

/**
 * Multiple message handler
 * @author qiuhd
 * @since  2014年9月3日
 */
public class MultiMessageHandler extends AbstractChannelHandlerDelegate{

	public MultiMessageHandler(ChannelHandler channelHandler) {
		super(channelHandler);
	}

	@Override
    public void received(Channel channel, Object message) throws RemotingException {
        if (message instanceof MultiMessage) {
            MultiMessage list = (MultiMessage)message;
            for(Object obj : list) {
            	handler.received(channel, obj);
            }
        } else {
        	handler.received(channel, message);
        }
    }
}

