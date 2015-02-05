package org.hdl.anima.handler;

import org.hdl.anima.protocol.Request;
import org.hdl.anima.session.ISession;

/**
 * HandlerInterceptorAdapter.
 * @author qiuhd
 * @since  2014年11月4日
 * @version V1.0.0
 */
public class HandlerInterceptorAdapter implements HandlerInterceptor {

	@Override
	public boolean preHandle(Request request, ISession session, Object handler)
			throws Exception {
		return false;
	}

	@Override
	public void postHandle(Request request, ISession session, Object handler,Object result)
			throws Exception {
	}
}
