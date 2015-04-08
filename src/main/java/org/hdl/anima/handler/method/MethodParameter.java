package org.hdl.anima.handler.method;

import java.lang.reflect.Method;

/**
 * MethodParameter.
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public class MethodParameter {
	
	private final Method method;
	private final int parameterIndex;
	private Class<?> parameterType;
	
	/**
	 * Create an instance from the given method
	 * @param method
	 * @param parameterIndex
	 */
	public MethodParameter(Method method,int parameterIndex) {
		this.method = method ;
		this.parameterIndex = parameterIndex;
	}
	
	/**
	 * Set a resolved (generic) parameter type.
	 */
	void setParameterType(Class<?> parameterType) {
		this.parameterType = parameterType;
	}

	/**
	 * Return the type of the method/constructor parameter.
	 * @return the parameter type (never <code>null</code>)
	 */
	public Class<?> getParameterType() {
		if (this.parameterType == null) {
			this.parameterType = (this.method != null ?
					this.method.getParameterTypes()[this.parameterIndex] :null);
		}
		return this.parameterType;
	}
}
