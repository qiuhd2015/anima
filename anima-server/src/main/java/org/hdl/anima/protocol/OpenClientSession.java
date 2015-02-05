package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;

/**
 * OpenClientSession message.
 * @author qiuhd
 * @since  2014年9月15日
 * @version V1.0.0
 */
public class OpenClientSession implements Record {
	/**
	 * 表示客户端session唯一身份
	 */
	private int sid ;
	/**
	 * 远程IP地址
	 */
	private String remoteIP;
	/**
	 * 远程端口
	 */
	private int remotePort;
	/**
	 * 客户端本地IP
	 */
	private String localIP;
	/**
	 * 客户端本地使用端口
	 */
	private int localPort;
	/**
	 * 客户端类型：Android、Unity3d、Flash
	 */
	private String clientType;
	
	public OpenClientSession() {
		
	}
	
	public OpenClientSession(int sid) {
		this.sid = sid;
	}
	
	@Override
	public void serialize(OutputArchive output) throws IOException {
		output.writeInt(sid);
		output.writeString(remoteIP);
		output.writeInt(remotePort);
		output.writeString(localIP);
		output.writeInt(localPort);
		output.writeString(clientType);
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		this.sid = input.readInt();
		this.remoteIP = input.readString();
		this.remotePort = input.readInt();
		this.localIP = input.readString();
		this.localPort = input.readInt();
		this.clientType = input.readString();
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public String getRemoteIP() {
		return remoteIP;
	}

	public void setRemoteIP(String remoteIP) {
		this.remoteIP = remoteIP;
	}

	public int getRemotePort() {
		return remotePort;
	}

	public void setRemotePort(int remotePort) {
		this.remotePort = remotePort;
	}

	public String getLocalIP() {
		return localIP;
	}

	public void setLocalIP(String localIP) {
		this.localIP = localIP;
	}

	public int getLocalPort() {
		return localPort;
	}

	public void setLocalPort(int localPort) {
		this.localPort = localPort;
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}
}
