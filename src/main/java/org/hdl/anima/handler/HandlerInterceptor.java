package org.hdl.anima.handler;

import org.hdl.anima.protocol.Request;
import org.hdl.anima.session.ISession;

/**
 * HandlerInterceptor.
 * @author qiuhd
 * @since  2014年11月4日
 * @version V1.0.0
 */
public interface HandlerInterceptor {

	public boolean preHandle(Request request, ISession session, Object handler)
			throws Exception ;
	
	
	public void postHandle(Request request, ISession session, Object handler,Object result)
			throws Exception ;
}
