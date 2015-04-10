package org.hdl.anima;

import java.util.List;

import org.hdl.anima.client.AsyncClientConfig;
import org.hdl.anima.surrogate.ServerSurrogateConfig;
import static com.google.common.base.Preconditions.checkArgument;
/**
 * Server Configure
 * @author qiuhd
 * @since  2014年8月14日
 */
public class ServerConfig {
	/**
	 * 
	 * @author qiuhd
	 *
	 */
	public static class ActionConfig {
		
		private final InterceptorConfig[] interceptorConfigs;
		
		private final String[] componetScanPgs;
		
		public ActionConfig(InterceptorConfig[] icpConfigs,String[] componetScanPgs) {
			this.interceptorConfigs = icpConfigs;
			this.componetScanPgs = componetScanPgs;
		}
		
		public String[] getComponetScanPgs() {
			return componetScanPgs;
		}
		
		public InterceptorConfig[] getInterceptorConfigs() {
			return interceptorConfigs;
		}
		
		/**
		 * 
		 * @author qiuhd
		 *
		 */
		public static class InterceptorConfig {
			private final String className;
			private final int[] includes;
			private final int[] excludes;
			
			public InterceptorConfig(String className,int[] includes,int[] excludes) {
				checkArgument(className != null,"className can not be null!");
				this.className = className;
				this.includes = includes;
				this.excludes = excludes;
			}
			
			public String getClassName() {
				return className;
			}
			
			public int[] getExcludes() {
				return excludes;
			}
			
			public int[] getIncludes() {
				return includes;
			}
		}
	}

	private String sid ;									//服务器唯一名字
	private String stype;									//服务器类型
	private String host;									//服务器IP	
	private int port;										//服务器端口
	private boolean frontend;								//是否是前端服务器
	private int threads;									//业务线程数
	private int maxConnects;								//支持客户端最大连接
	private int heartbeat;									//心跳时间
	private int heartbeatTimeout;							//心跳超时时间
	private int reconnectionSecond;							//客户端重连时间
	private List<ServerSurrogateConfig> surrogateConfigs;	//前端服务器与后端服务器连接配置
	private List<AsyncClientConfig> asyncClientConfigs;		//后端服务器之间连接配置
	private ActionConfig actionConfig;						//Action 配置
	
	public ServerConfig() {}
	
	public ServerConfig(String sid,String stype ,String host, int port, boolean frontend,ActionConfig actionConfig) {
		this.sid = sid;
		this.stype = stype;
		this.host = host;
		this.port = port;
		this.frontend = frontend;
		this.actionConfig = actionConfig;
	}

	public String getServerId() {
		return sid;
	}

	public void setServerId(String id) {
		this.sid = id;
	}
	
	public void setServetType(String stype) {
		this.stype = stype;
	}
	
	public String getServerType() {
		return this.stype;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public boolean isFrontend() {
		return frontend;
	}

	public void setFrontend(boolean frontend) {
		this.frontend = frontend;
	}

	public int getHeartbeat() {
		return heartbeat;
	}

	public void setHeartbeat(int heartbeat) {
		this.heartbeat = heartbeat;
	}

	public int getHeartbeatTimeout() {
		return heartbeatTimeout;
	}

	public void setHeartbeatTimeout(int heartbeatTimeout) {
		this.heartbeatTimeout = heartbeatTimeout;
	}

	public List<ServerSurrogateConfig> getSurrogateConfigs() {
		return surrogateConfigs;
	}

	public void setSurrogateConfigs(List<ServerSurrogateConfig> surrogateConfigs) {
		this.surrogateConfigs = surrogateConfigs;
	}

	public int getReconnectionSecond() {
		return reconnectionSecond;
	}

	public void setReconnectionSecond(int reconnectionSecond) {
		this.reconnectionSecond = reconnectionSecond;
	}

	public List<AsyncClientConfig> getAsyncClientConfigs() {
		return asyncClientConfigs;
	}

	public void setAsyncClientConfigs(List<AsyncClientConfig> asyncClientConfigs) {
		this.asyncClientConfigs = asyncClientConfigs;
	}

	public int getThreads() {
		return threads;
	}

	public void setThreads(int threads) {
		this.threads = threads;
	}

	public int getMaxConnects() {
		return maxConnects;
	}

	public void setMaxConnects(int maxConnects) {
		this.maxConnects = maxConnects;
	}
	
	public void setActionConfig(ActionConfig actionConfig) {
		this.actionConfig = actionConfig;
	}
	
	public ActionConfig getActionConfig() {
		return actionConfig;
	}
}

