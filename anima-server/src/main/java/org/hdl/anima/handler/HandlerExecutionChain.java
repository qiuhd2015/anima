package org.hdl.anima.handler;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hdl.anima.protocol.Request;
import org.hdl.anima.session.ISession;

/**
 * HandlerExecutionChain.
 * @author qiuhd
 * @since  2014年9月22日
 * @version V1.0.0
 */
public class HandlerExecutionChain {

	private final Object handler;
	
	private HandlerInterceptor[] interceptors;

	private List<HandlerInterceptor> interceptorList;

	public HandlerExecutionChain(Object handler) {
		checkArgument(handler != null,"handler is require");
		this.handler = handler;
	}
	
	public void addInterceptor(HandlerInterceptor interceptor) {
		initInterceptorList();
		this.interceptorList.add(interceptor);
	}

	public void addInterceptors(HandlerInterceptor[] interceptors) {
		if (interceptors != null) {
			initInterceptorList();
			this.interceptorList.addAll(Arrays.asList(interceptors));
		}
	}

	private void initInterceptorList() {
		if (this.interceptorList == null) {
			this.interceptorList = new ArrayList<HandlerInterceptor>();
		}
		if (this.interceptors != null) {
			this.interceptorList.addAll(Arrays.asList(this.interceptors));
			this.interceptors = null;
		}
	}
	
	/**
	 * Return the array of interceptors to apply (in the given order).
	 * @return the array of HandlerInterceptors instances (may be {@code null})
	 */
	public HandlerInterceptor[] getInterceptors() {
		if (this.interceptors == null && this.interceptorList != null) {
			this.interceptors = this.interceptorList.toArray(new HandlerInterceptor[this.interceptorList.size()]);
		}
		return this.interceptors;
	}
	
	/**
	 * Apply preHandle methods of registered interceptors.
	 * @return {@code true} if the execution chain should proceed with the
	 * next interceptor or the handler itself. Else, DispatcherServlet assumes
	 * that this interceptor has already dealt with the response itself.
	 */
	boolean applyPreHandle(Request request, ISession session) throws Exception {
		if (getInterceptors() != null) {
			for (int i = 0; i < getInterceptors().length; i++) {
				HandlerInterceptor interceptor = getInterceptors()[i];
				if (!interceptor.preHandle(request, session, this.handler)) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * Apply postHandle methods of registered interceptors.
	 */
	void applyPostHandle(Request request, ISession session,Object result) throws Exception {
		if (getInterceptors() == null) {
			return;
		}
		for (int i = getInterceptors().length - 1; i >= 0; i--) {
			HandlerInterceptor interceptor = getInterceptors()[i];
			interceptor.postHandle(request, session, this.handler, result);
		}
	}
	
	public Object getHandler() {
		return handler;
	}
	
	/**
	 * Delegates to the handler's {@code toString()}.
	 */
	@Override
	public String toString() {
		if (this.handler == null) {
			return "HandlerExecutionChain with no handler";
		}
		StringBuilder sb = new StringBuilder();
		sb.append("HandlerExecutionChain with handler [").append(this.handler).append("]");
		if (this.interceptorList != null && this.interceptorList.size() > 0) {
			sb.append(" and ").append(this.interceptorList.size()).append(" interceptor");
			if (this.interceptorList.size() > 1) {
				sb.append("s");
			}
		}
		return sb.toString();
	}
	
}
