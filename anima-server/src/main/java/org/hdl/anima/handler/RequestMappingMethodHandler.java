package org.hdl.anima.handler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.common.utils.ReflectionUtils.MethodFilter;
import org.hdl.anima.handler.action.annotation.Action;
import org.hdl.anima.handler.action.annotation.RequestMapping;
import org.hdl.anima.handler.action.annotation.RequestType;
import org.hdl.anima.handler.method.MethodHandler;
import org.hdl.anima.handler.method.MethodHandlerSelector;
import org.hdl.anima.protocol.Request;
import org.reflections.Reflections;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;

import com.google.common.base.Predicate;
/**
 * RequestMappingMethodHandler.
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public class RequestMappingMethodHandler extends BasicModule{

	private static final Logger logger = LoggerFactory.getLogger(RequestMappingMethodHandler.class);
	
	private Map<Class<?>,Object> handlerCache = new LinkedHashMap<Class<?>, Object>();
	
	private Map<RequestMappingInfo, MethodHandler> methodHandlers = new LinkedHashMap<RequestMappingInfo, MethodHandler>();
	
	private Map<Integer, RequestMappingInfo> idsMap = new LinkedHashMap<Integer,RequestMappingInfo>();
	
	private final String actionTypeName = Action.class.getName() ;
	
	private final List<Object> interceptors = new ArrayList<Object>();

	private final List<HandlerInterceptor> adaptedInterceptors = new ArrayList<HandlerInterceptor>();

	private final List<MappedInterceptor> mappedInterceptors = new ArrayList<MappedInterceptor>();

	
	public RequestMappingMethodHandler(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		loadBeanResources();
		initHandlerMethods();
		initInterceptors();
	}
	
	/**
	 * Support request
	 * @param request
	 * @return
	 */
	public boolean supportRequest(Request request) {
		RequestMappingInfo requestMappingInfo = idsMap.get(request.getId());
		return requestMappingInfo != null ? true : false;
	}
	
	/**
	 * 
	 * @param request
	 * @return
	 */
	public HandlerExecutionChain getHandler(Request request) {
		MethodHandler methodHandler = getHandlerInternal(request);
		
		if (methodHandler == null) 
			return null;
		
		HandlerExecutionChain chain = new HandlerExecutionChain(methodHandler);
		chain.addInterceptors(getAdaptedInterceptors());

		int lookupPath = request.getId();
		for (MappedInterceptor mappedInterceptor : mappedInterceptors) {
			if (mappedInterceptor.matches(lookupPath)) {
				chain.addInterceptor(mappedInterceptor.getInterceptor());
			}
		}

		return chain;
	}

	private MethodHandler getHandlerInternal(Request request) {
		int id = request.getId();
		if (logger.isDebugEnabled()) {
			logger.debug("Looking up handler method for id " + id);
		}
		
		RequestMappingInfo requestMappingInfo = idsMap.get(id);
		if (requestMappingInfo == null) {
			return null;
		}
		
		MethodHandler methodHandler = methodHandlers.get(requestMappingInfo);
		if (logger.isDebugEnabled()) {
			if (methodHandler != null) {
				logger.debug("Returning handler method [" + methodHandler + "]");
			}
			else {
				logger.debug("Did not find handler method for [" + id + "]");
			}
		}
		return (methodHandler != null ? methodHandler.createWithResolvedBean() : null);
	}

	/**
	 * Load all action instance from annotated type with the {@link Action}
	 */
	private void loadBeanResources() {
		String[] componentPackages = application.getServerConifg().getComponetPackages();
		
		TypeAnnotationsScanner typeScanner = new TypeAnnotationsScanner();
		typeScanner.setResultFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				if (input.equals(actionTypeName)) {
					return true;
				}else {
					return false;
				}
			}
		});
		
		Reflections reflections = new Reflections(componentPackages,typeScanner);
		Set<Class<?>> allActions = reflections.getTypesAnnotatedWith(Action.class);
		
		for (Class<?> handlerClass : allActions) {
			if (handlerCache.containsKey(handlerClass)) {
				continue;
			}
			Object handlerObject = null;
			try {
				try {
					Constructor<?> constructor = handlerClass.getConstructor(Application.class);
					handlerObject = constructor.newInstance(application);
				}catch(NoSuchMethodException e) {
					handlerObject = handlerClass.newInstance();
				}
				handlerCache.put(handlerClass, handlerObject);
			} catch (Exception e) {
				logger.error("Failed to instance object",e);
				continue;
			}
		}
	}
	
	private void initHandlerMethods() {
		for (final Class<?> handlerClass : handlerCache.keySet()) {
			Object handler = handlerCache.get(handlerClass);
			final Map<Method, RequestMappingInfo> mappings = new IdentityHashMap<Method, RequestMappingInfo>();
			Set<Method> methods = MethodHandlerSelector.selectMethods(handlerClass, new MethodFilter() {
				@Override
				public boolean matches(Method method) {
					RequestMappingInfo mapping = getMappingForMethod(method, handlerClass);
					if (mapping != null) {
						mappings.put(method, mapping);
						return true;
					}
					return false;
				}
			});
			
			for (Method method : methods) {
				registerHandlerMethod(handler,method,mappings.get(method)) ;
			}
		}
	}
	
	/**
	 * Initialize the specified interceptors, checking for {@link MappedInterceptor}s and adapting
	 * HandlerInterceptors where necessary.
	 * @see #setInterceptors
	 * @see #adaptInterceptor
	 */
	protected void initInterceptors() {
		if (!this.interceptors.isEmpty()) {
			for (int i = 0; i < this.interceptors.size(); i++) {
				Object interceptor = this.interceptors.get(i);
				if (interceptor == null) {
					throw new IllegalArgumentException("Entry number " + i + " in interceptors array is null");
				}
				if (interceptor instanceof MappedInterceptor) {
					mappedInterceptors.add((MappedInterceptor) interceptor);
				}
				else {
					adaptedInterceptors.add(adaptInterceptor(interceptor));
				}
			}
		}
	}
	
	protected HandlerInterceptor adaptInterceptor(Object interceptor) {
		if (interceptor instanceof HandlerInterceptor) {
			return (HandlerInterceptor) interceptor;
		}else {
			throw new IllegalArgumentException("Interceptor type not supported: " + interceptor.getClass().getName());
		}
	}
	
	public void setInterceptors(Object[] interceptors) {
		this.interceptors.addAll(Arrays.asList(interceptors));
	}
	
	protected final HandlerInterceptor[] getAdaptedInterceptors() {
		int count = adaptedInterceptors.size();
		return (count > 0) ? adaptedInterceptors.toArray(new HandlerInterceptor[count]) : null;
	}

	/**
	 * Return all configured {@link MappedInterceptor}s as an array.
	 * @return the array of {@link MappedInterceptor}s, or {@code null} if none
	 */
	protected final MappedInterceptor[] getMappedInterceptors() {
		int count = mappedInterceptors.size();
		return (count > 0) ? mappedInterceptors.toArray(new MappedInterceptor[count]) : null;
	}
	
	protected void registerHandlerMethod(Object handler, Method method, RequestMappingInfo mapping) {
		MethodHandler newHandlerMethod = new MethodHandler(handler,application, method);
		MethodHandler oldHandlerMethod = this.methodHandlers.get(mapping);
		if (oldHandlerMethod != null && !oldHandlerMethod.equals(newHandlerMethod)) {
			throw new IllegalStateException("Ambiguous mapping found. Cannot map '" + newHandlerMethod.getBean() +
					"' bean method \n" + newHandlerMethod + "\nto " + mapping + ": There is already '" +
					oldHandlerMethod.getBean() + "' bean method\n" + oldHandlerMethod + " mapped.");
		}

		this.methodHandlers.put(mapping, newHandlerMethod);
		if (logger.isInfoEnabled()) {
			logger.info("Mapped \"" + mapping + "\" onto " + newHandlerMethod);
		}

		int id = mapping.getId();
		idsMap.put(id, mapping);
	}
	
	public RequestMappingInfo getMappingInfo(Request request) {
		return this.idsMap.get(request.getId());
	}

	private RequestMappingInfo getMappingForMethod(Method method, Class<?> handlerType) {
		RequestMappingInfo info = null;
		RequestMapping methodAnnotation = AnnotationUtils.findAnnotation(method, RequestMapping.class);
		if (methodAnnotation != null) {
			int id = methodAnnotation.value();
			RequestType requetsType = methodAnnotation.type();
			Class<? extends Decodeable> param = methodAnnotation.param();
			Class<? extends HandlerInterceptor>[] interceptorClazzs = methodAnnotation.interceptor();
				for (Class<? extends HandlerInterceptor> interceptorClazz : interceptorClazzs) {
					HandlerInterceptor interceptor = null;
					try {
						interceptor = interceptorClazz.newInstance();
					}catch(Exception e) {
						logger.error("Instance HandlerInterceptor error",e);
						continue;
					}
					int[] includePatterns = new int[] {id};
					MappedInterceptor mappedInterceptor = new MappedInterceptor(includePatterns, interceptor);
					mappedInterceptors.add(mappedInterceptor);
				}
			
			info = new RequestMappingInfo(method,id, requetsType, param);
		}
		return info;
	}
}
