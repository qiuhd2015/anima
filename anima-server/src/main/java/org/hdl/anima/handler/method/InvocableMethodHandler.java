package org.hdl.anima.handler.method;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.session.ISession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

/**
 * InvocableMethodHandler.
 * @author qiuhd
 * @since  2014年9月29日
 * @version V1.0.0
 */
public class InvocableMethodHandler extends MethodHandler {

	private static final Logger logger = LoggerFactory.getLogger(InvocableMethodHandler.class);
	
	public InvocableMethodHandler(Object bean, Application application,
			Method method) {
		super(bean, application, method);
	}
	
	public InvocableMethodHandler(MethodHandler methodHandler) {
		super(methodHandler);
	}
	
	/**
	 * Invoke for 
	 * @param request
	 * @param session
	 * @param providedArgs
	 * @return
	 * @throws Exception
	 */
	public final Object invokeForRequest(Request request,Response response,ISession session) throws Exception {

		Object[] args = getMethodArgumentValues(request,response, session);
		if (logger.isTraceEnabled()) {
			StringBuilder sb = new StringBuilder("Invoking [");
			sb.append(getBeanType().getSimpleName()).append(".");
			sb.append(getMethod().getName()).append("] method with arguments ");
			sb.append(Arrays.asList(args));
			logger.trace(sb.toString());
		}
		Object returnValue = invoke(args);
		if (logger.isTraceEnabled()) {
			logger.trace("Method [" + getMethod().getName() + "] returned [" + returnValue + "]");
		}
		return returnValue;
	}
	
	
	/**
	 * Invoke the handler method with the given argument values.
	 */
	private Object invoke(Object... args) throws Exception {
		ReflectionUtils.makeAccessible(getMethod());
		try {
			return getMethod().invoke(getBean(), args);
		}
		catch (IllegalArgumentException ex) {
			throw new IllegalStateException(getInvocationErrorMessage(ex.getMessage(), args), ex);
		}
		catch (InvocationTargetException ex) {
			// Unwrap for HandlerExceptionResolvers ...
			Throwable targetException = ex.getTargetException();
			if (targetException instanceof RuntimeException) {
				throw (RuntimeException) targetException;
			}
			else if (targetException instanceof Error) {
				throw (Error) targetException;
			}
			else if (targetException instanceof Exception) {
				throw (Exception) targetException;
			}else {
				String msg = getInvocationErrorMessage("Failed to invoke controller method", args);
				throw new IllegalStateException(msg, targetException);
			}
		}
	}
	
	/**
	 * Get the method argument values for the current request.
	 */
	private Object[] getMethodArgumentValues(Request request,Response response,ISession session) throws Exception {
		MethodParameter[] parameters = getMethodParameters();
		Object[] args = new Object[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			MethodParameter parameter = parameters[i];
			if (Decodeable.class.isAssignableFrom(parameter.getParameterType())) {
				args[i] = request.getContent() ;
			}else if (ISession.class.isAssignableFrom(parameter.getParameterType())) {
				args[i] = session;
			}else if (Response.class.isAssignableFrom(parameter.getParameterType())) { 
				args[i] = response;
		    }else {
				String error = getArgumentResolutionErrorMessage("Unkown parameter type :",i);
				throw new IllegalStateException(error);
			}
		}
		return args;
	}
	
	private String getArgumentResolutionErrorMessage(String message, int index) {
		MethodParameter param = getMethodParameters()[index];
		message += " [" + index + "] [type=" + param.getParameterType().getName() + "]";
		return getDetailedErrorMessage(message);
	}
	
	/**
	 * Adds HandlerMethod details such as the controller type and method signature to the given error message.
	 * @param message error message to append the HandlerMethod details to
	 */
	protected String getDetailedErrorMessage(String message) {
		StringBuilder sb = new StringBuilder(message).append("\n");
		sb.append("HandlerMethod details: \n");
		sb.append("Action [").append(getBeanType().getName()).append("]\n");
		sb.append("Method [").append(getMethod().toGenericString()).append("]\n");
		return sb.toString();
	}
	
	private String getInvocationErrorMessage(String message, Object[] resolvedArgs) {
		StringBuilder sb = new StringBuilder(getDetailedErrorMessage(message));
		sb.append("Resolved arguments: \n");
		for (int i=0; i < resolvedArgs.length; i++) {
			sb.append("[").append(i).append("] ");
			if (resolvedArgs[i] == null) {
				sb.append("[null] \n");
			}
			else {
				sb.append("[type=").append(resolvedArgs[i].getClass().getName()).append("] ");
				sb.append("[value=").append(resolvedArgs[i]).append("]\n");
			}
		}
		return sb.toString();
	}
}
