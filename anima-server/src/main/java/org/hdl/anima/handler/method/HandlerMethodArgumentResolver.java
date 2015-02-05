package org.hdl.anima.handler.method;

import org.hdl.anima.session.ISession;
import org.springframework.core.MethodParameter;
/**
 * HandlerMethodArgumentResolver.
 * @author qiuhd
 * @since  2014年9月29日
 * @version V1.0.0
 */
public interface HandlerMethodArgumentResolver {

	boolean supportsParameter(MethodParameter parameter);
	
	Object resolveArgument(MethodParameter parameter, ISession session) throws Exception;
}
