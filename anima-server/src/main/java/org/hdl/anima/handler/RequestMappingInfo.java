package org.hdl.anima.handler;

import static com.google.common.base.Preconditions.checkArgument;

import java.lang.reflect.Method;

import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.handler.action.annotation.RequestType;

/**
 * RequestMappingInfo.
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public class RequestMappingInfo {
	
	private final int id;
	
	private final RequestType requestType;
	
	private final Class<? extends Decodeable> param;
	
	private final RequestParameterObjectFactory parameterObjectFactory;
	
	private final Method method ;

	public RequestMappingInfo(Method method ,int id, RequestType requestType, Class<? extends Decodeable> param) {
		checkArgument(id > 0,"id <= 0");
		checkArgument(requestType != null,"requestType is required");
		checkArgument(param != null,"param is required");
		this.method = method;
		this.id = id;
		this.requestType = requestType;
		this.param = param;
		parameterObjectFactory = new RequestParameterObjectFactory(param);
	}

	public int getId() {
		return id;
	}

	public RequestType getRequestType() {
		return requestType;
	}

	public Class<?> getParam() {
		return param;
	}
	
	public Method getMethod() {
		return this.method;
	}

	public RequestParameterObjectFactory getParameterObjectFactory() {
		return parameterObjectFactory;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RequestMappingInfo other = (RequestMappingInfo) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	/**
	 * Request parameter object factory
	 * @author qiuhd
	 */
	public final class RequestParameterObjectFactory {
		
		private Class<? extends Decodeable> parameterType;
		
		public RequestParameterObjectFactory(Class<? extends Decodeable> parameterType) {
			this.parameterType = parameterType;
		}
		
		public Decodeable createObject() {
			Decodeable object = null;
			try {
				object = parameterType.newInstance();
			} catch (Exception e) {
				throw new IllegalStateException("No default construct in " + method.toGenericString());
			}
			return object;
		}
	}
}
