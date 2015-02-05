package org.hdl.anima.proxy;
/**
 * 
 * @author qiuhd
 * @since  2014年8月12日
 */
public class ServerProxyConfig {
	
	private String id;			//代理服务器id 
	private String remoteHost;	//远程主机
	private int remotePort;		//远程对外接口
	private int connectTimeout;	//连接超时时间
	private int connects;		//连接数
	private String reconnect;	//连接间隔 
	private boolean sendReconnect;//发送重连
	private int heartbeat;			//心跳时间
	private int heartbeatTimeout;	//心跳超时时间
	
	public ServerProxyConfig() {
		
	}
	
	public ServerProxyConfig(String id,String remoteHost,int remotePort,int connectTimout,int connects) {
		this.id = id;
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
		this.connectTimeout = connectTimout;
		this.connects = connects;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
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

