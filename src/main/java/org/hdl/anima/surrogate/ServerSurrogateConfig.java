package org.hdl.anima.surrogate;
/**
 * ServerSurrogateConfig
 * @author qiuhd
 * @since  2014年9月18日
 * @version V1.0.0
 */
public class ServerSurrogateConfig {
	
	private String serverId;					//后台服务器名称
	private String remoteHost;			//远程主机
	private int remotePort;				//远程对外接口
	private int connectTimeout;			//连接超时时间
	private int connects;				//连接数
	private String reconnect;			//连接间隔 
	private boolean sendReconnect;		//发送重连
	private int heartbeat;				//心跳时间
	private int heartbeatTimeout;		//心跳超时时间
	
	public ServerSurrogateConfig() {
		
	}
	
	public ServerSurrogateConfig(String serverId, String remoteHost,
			int remotePort, int connectTimout, int connects) {
		this.serverId = serverId;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.connectTimeout = connectTimout;
		this.connects = connects;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String id) {
		this.serverId = id;
	}

	public String getRemoteHost() {
		return remoteHost;
	}

	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public int getConnectTimeout() {
		return connectTimeout;
	}

	public void setConnectTimeout(int connectTimeout) {
		this.connectTimeout = connectTimeout;
	}

	public int getConnects() {
		return connects;
	}

	public void setConnects(int connects) {
		this.connects = connects;
	}

	public String getReconnect() {
		return reconnect;
	}

	public void setReconnect(String reconnect) {
		this.reconnect = reconnect;
	}

	public boolean isSendReconnect() {
		return sendReconnect;
	}

	public void setSendReconnect(boolean sendReconnect) {
		this.sendReconnect = sendReconnect;
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
}
