package org.hdl.anima.handler.method;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hdl.anima.common.utils.ReflectionUtils;
import org.hdl.anima.common.utils.ReflectionUtils.MethodFilter;
/**
 * MethodHandlerSelector.
 * @author qiuhd
 * @since  2014年9月25日
 * @version V1.0.0
 */
public abstract class MethodHandlerSelector {

	/**
	 * Select handler methods for the given handler type.
	 * <p>Callers define handler methods of interest through the {@link MethodFilter} parameter.
	 * @param handlerType the handler type to search handler methods on
	 * @param handlerMethodFilter a {@link MethodFilter} to help recognize handler methods of interest
	 * @return the selected methods, or an empty set
	 */
	public static Set<Method> selectMethods(final Class<?> handlerType, final MethodFilter handlerMethodFilter) {
		final Set<Method> handlerMethods = new LinkedHashSet<Method>();
		Set<Class<?>> handlerTypes = new LinkedHashSet<Class<?>>();
		Class<?> specificHandlerType = null;
		if (!Proxy.isProxyClass(handlerType)) {
			handlerTypes.add(handlerType);
			specificHandlerType = handlerType;
		}
		handlerTypes.addAll(Arrays.asList(handlerType.getInterfaces()));
		for (Class<?> currentHandlerType : handlerTypes) {
			final Class<?> targetClass = (specificHandlerType != null ? specificHandlerType : currentHandlerType);
			ReflectionUtils.doWithMethods(currentHandlerType, new ReflectionUtils.MethodCallback() {
				public void doWith(Method method) {
					if (handlerMethodFilter.matches(method)) {
						handlerMethods.add(method);
					}
				}
			}, ReflectionUtils.USER_DECLARED_METHODS);
		}
		return handlerMethods;
	}
}
