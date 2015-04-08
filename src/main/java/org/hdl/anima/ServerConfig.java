package org.hdl.anima;

import java.util.List;

import org.hdl.anima.client.AsyncClientConfig;
import org.hdl.anima.surrogate.ServerSurrogateConfig;

/**
 * Server Configure
 * @author qiuhd
 * @since  2014年8月14日
 */
public class ServerConfig {

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
	private List<AsyncClientConfig> asyncClientConfigs;	//后端服务器之间连接配置
	private String[] componetPackages;
	
	public ServerConfig() {}
	
	public ServerConfig(String sid,String stype ,String host, int port, boolean frontend) {
		this.sid = sid;
		this.stype = stype;
		this.host = host;
		this.port = port;
		this.frontend = frontend;
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

	public String[] getComponetPackages() {
		return componetPackages;
	}

	public void setComponetPackages(String[] componetPackages) {
		this.componetPackages = componetPackages;
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
}

