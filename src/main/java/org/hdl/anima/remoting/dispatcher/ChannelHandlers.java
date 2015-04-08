package org.hdl.anima.remoting.dispatcher;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.dispatcher.execution.ExecutionDispather;
import org.hdl.anima.remoting.support.HeartbeatHandler;
import org.hdl.anima.remoting.support.MultiMessageHandler;

/**
 * 
 * @author qiuhd
 * @since  2014年9月3日
 */
public class ChannelHandlers {

	public static ChannelHandler wrap(ChannelHandler handler, AppConf conf){
        return ChannelHandlers.getInstance().wrapInternal(handler, conf);
    }

    protected ChannelHandlers() {}

    protected ChannelHandler wrapInternal(ChannelHandler handler, AppConf conf) {
        return new MultiMessageHandler(new HeartbeatHandler(ExecutionDispather.getInstance().dispatch(handler, conf)));
    }

    private static ChannelHandlers INSTANCE = new ChannelHandlers();

    protected static ChannelHandlers getInstance() {
        return INSTANCE;
    }

    static void setTestingChannelHandlers(ChannelHandlers instance) {
        INSTANCE = instance;
    }
}

