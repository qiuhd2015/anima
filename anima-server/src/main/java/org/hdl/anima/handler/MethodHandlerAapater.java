package org.hdl.anima.handler;

import org.hdl.anima.common.io.Encodeable;
import org.hdl.anima.handler.method.InvocableMethodHandler;
import org.hdl.anima.handler.method.MethodHandler;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.session.ISession;

/**
 * MethodHandlerAapater.
 * @author qiuhd
 * @since  2014年9月26日
 * @version V1.0.0
 */
public class MethodHandlerAapater implements HandlerAdapter {

	@Override
	public Object handle(Request request,  Response response,ISession session, Object handler)
			throws Exception {
		return handleInternal(request, response,session, (MethodHandler)handler);
	}
	
	private Object handleInternal(Request request, Response response,ISession session,
			MethodHandler handler) throws Exception {
		InvocableMethodHandler invokHandler = new InvocableMethodHandler(handler);
		Object returnValue = invokHandler.invokeForRequest(request,response,session);
		// assert request type
		if (request.isRequest()) {
			// assert return value type
			if (returnValue != null) {
				if (!Encodeable.class.isAssignableFrom(returnValue.getClass())) {
					throw new IllegalArgumentException("");
				}
				response.setContent(returnValue);
				session.send(response);
			}
		}
		return returnValue;
	}
}
