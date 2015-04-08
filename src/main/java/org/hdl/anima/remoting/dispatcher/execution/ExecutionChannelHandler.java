package org.hdl.anima.remoting.dispatcher.execution;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.dispatcher.ChannelEventRunnable;
import org.hdl.anima.remoting.dispatcher.ChannelEventRunnable.ChannelState;
import org.hdl.anima.remoting.dispatcher.WrappedChannelHandler;

/**
 * Execution Channel Handler
 * @author qiuhd
 */
public class ExecutionChannelHandler extends WrappedChannelHandler {
    
    public ExecutionChannelHandler(ChannelHandler handler, AppConf conf) {
        super(handler, conf);
    }

    public void connected(Channel channel) throws RemotingException {
    	handler.connected(channel);
        //executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CONNECTED));
    }

    public void disconnected(Channel channel) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.DISCONNECTED));
    }

    public void received(Channel channel, Object message) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler, ChannelState.RECEIVED, message));
    }

    public void caught(Channel channel, Throwable exception) throws RemotingException {
        executor.execute(new ChannelEventRunnable(channel, handler ,ChannelState.CAUGHT, exception));
    }
}