package org.hdl.anima.handler.method;

import java.lang.reflect.Method;

import org.hdl.anima.Application;
import org.springframework.util.Assert;
/**
 * MethodHandler .
 * @author qiuhd
 * @since  2014年9月23日
 * @version V1.0.0
 */
public class MethodHandler {

	private final Object bean;
	private final Method method;
	private final MethodParameter[] methodParameters;
	protected final Application application;
	
	public MethodHandler(Object bean,Application application,Method method) {
		this.bean = bean;
		this.method = method ;
		this.application = application;
		this.methodParameters = initMethodParameters();
	}
	
	/**
	 * Re-create HandlerMethod with the resolved handler.
	 */
	public MethodHandler(MethodHandler methodHandler) {
		Assert.notNull(methodHandler, "HandlerMethod is required");
		this.bean = methodHandler.bean;
		this.method = methodHandler.method;
		this.methodParameters = methodHandler.methodParameters;
		this.application = methodHandler.application;
	}
	
	private MethodParameter[] initMethodParameters() {
		int count = this.method.getParameterTypes().length;
		MethodParameter[] result = new MethodParameter[count];
		for (int i = 0; i < count; i++) {
			result[i] = new HandlerMethodParameter(i);
		}
		return result;
	}
	
	public Object getBean() {
		return bean;
	}

	public Method getMethod() {
		return method;
	}

	public MethodParameter[] getMethodParameters() {
		return methodParameters;
	}

	public Class<?> getBeanType() {
		return  this.bean.getClass();
	}
	
	public MethodHandler createWithResolvedBean() {
		return new MethodHandler(this);
	}
	
	/**
	 * Return the HandlerMethod return type.
	 */
	public MethodParameter getReturnType() {
		return new HandlerMethodParameter(-1);
	}
	
	/**
	 * Returns {@code true} if the method return type is void, {@code false} otherwise.
	 */
	public boolean isVoid() {
		return Void.TYPE.equals(getReturnType().getParameterType());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj != null && obj instanceof MethodHandler) {
			MethodHandler other = (MethodHandler) obj;
			return (this.bean.equals(other.bean) && this.method.equals(other.method));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.bean.hashCode() * 31 + this.method.hashCode();
	}

	@Override
	public String toString() {
		return this.method.toGenericString();
	}
	
	/**
	 * A MethodParameter with HandlerMethod-specific behavior.
	 */
	private class HandlerMethodParameter extends MethodParameter {

		public HandlerMethodParameter(int index) {
			super(MethodHandler.this.method, index);
		}
	}
}
