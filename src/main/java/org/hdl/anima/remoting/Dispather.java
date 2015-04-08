package org.hdl.anima.remoting;

import org.hdl.anima.AppConf;

/**
 * ChannelHandlerWrapper
 * 
 * @author qiudh
 */
public interface Dispather {

    /**
     * dispath.
     * 
     * @param handler
     * @param conf
     * @return channel handler
     */
    ChannelHandler dispath(ChannelHandler handler, AppConf conf);
}