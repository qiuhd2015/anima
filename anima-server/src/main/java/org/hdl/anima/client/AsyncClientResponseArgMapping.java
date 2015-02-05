package org.hdl.anima.client;

import java.util.Map;

import org.hdl.anima.common.io.Decodeable;
import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * Async client response mapping
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public class AsyncClientResponseArgMapping {
	
	private Map<Integer, Class<? extends Decodeable>> responseArgMapping = new ConcurrentHashMap<Integer, Class<? extends Decodeable>>();
	
	/**
	 * Internal class
	 * @author qiuhd
	 *
	 */
	private static class InternalClass {
		public final static AsyncClientResponseArgMapping INSTANCE = new AsyncClientResponseArgMapping(); 
	}
	
	private AsyncClientResponseArgMapping() {} ;
	
	public static AsyncClientResponseArgMapping getInstance() {
		return InternalClass.INSTANCE;
	}
	
	public boolean contains(int msgId) {
		return responseArgMapping.containsKey(msgId);
	}
	
	public Decodeable getResponseArgMapping(int msgId) throws Exception{
		Class<? extends Decodeable> clazz = responseArgMapping.get(msgId);
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw e;
		}
	}
	
	public Class<?> getResponseArgClazz(int msgId) {
		return responseArgMapping.get(msgId);
	}
	
	public void addResponseArg(int msgId,Class<? extends Decodeable> clazz) {
	
		if (clazz == null) {
			throw new NullPointerException("clazz can not be empty!");
		}
		
		if (!contains(msgId)) {
			responseArgMapping.put(msgId, clazz);
		}
	}
}
