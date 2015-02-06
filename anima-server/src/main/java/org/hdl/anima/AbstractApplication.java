package org.hdl.anima;

import org.hdl.anima.common.ScheduledExecutor;

/**
 * Application Interface
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public abstract class AbstractApplication implements ScheduledExecutor{
	
	/**
	 * Return the application configuration
	 * @return
	 */
	public abstract AppConf getAppConf();
	
	public abstract ServerConfig getServerConifg();
	
	public abstract String getServerId();
	
	public abstract String getServerType();
	
	public abstract Version getVersion();
    
	public abstract boolean isFrontend();
	
	public abstract boolean isBackend();
    
	public abstract <T> T getMoulde(Class<T> clazz);
   
	public abstract ClassLoader getClassLoader();
}

