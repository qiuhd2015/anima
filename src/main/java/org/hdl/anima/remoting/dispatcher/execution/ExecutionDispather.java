package org.hdl.anima.remoting.dispatcher.execution;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Dispatcher;


/**
 * 除发送全部使用线程池处理
 * 
 * @author qiuhd
 */
public class ExecutionDispather implements Dispatcher {
    
    public static final String NAME = "execution";

    public ChannelHandler dispatch(ChannelHandler handler, AppConf conf) {
        return new ExecutionChannelHandler(handler, conf);
    }
    
    private static ExecutionDispather INSTANCE = new ExecutionDispather();

    public static ExecutionDispather getInstance() {
        return INSTANCE;
    }
}