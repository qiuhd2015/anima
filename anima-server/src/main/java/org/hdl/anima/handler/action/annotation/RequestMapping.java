package org.hdl.anima.handler.action.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.handler.HandlerInterceptor;

/**
 * Request Mapping 
 * @author qiuhd
 * @since  2014年9月19日
 * @version V1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {
	
	int value() default 0;
	
	RequestType type() default RequestType.REQUEST;
	
	Class<? extends Decodeable> param();
	
	Class<? extends HandlerInterceptor>[] interceptor()  default {};
}
