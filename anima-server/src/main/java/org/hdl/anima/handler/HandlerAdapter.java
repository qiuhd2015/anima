package org.hdl.anima.handler;

import org.hdl.anima.protocol.Request;
import org.hdl.anima.session.ISession;


/**
 * Request handler adapter
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public interface HandlerAdapter {
	
	Object handle(Request requset,ISession session, Object handler) throws Exception;
}
