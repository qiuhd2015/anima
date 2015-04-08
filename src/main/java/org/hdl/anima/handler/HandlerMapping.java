package org.hdl.anima.handler;

import org.hdl.anima.protocol.Request;

/**
 * Request Handler mapping
 * @author qiuhd
 * @since  2014年9月19日
 * @version V1.0.0
 */
public interface HandlerMapping {
	HandlerExecutionChain getHandler(Request request) ;
}
