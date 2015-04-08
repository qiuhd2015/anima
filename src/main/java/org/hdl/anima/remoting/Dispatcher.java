package org.hdl.anima.remoting;

import org.hdl.anima.AppConf;

/**
 * Dispatcher
 * @author qiudh
 */
public interface Dispatcher {

    /**
     * Dispatch
     * @param handler
     * @param conf
     * @return channel handler
     */
    ChannelHandler dispatch(ChannelHandler handler, AppConf conf);
}