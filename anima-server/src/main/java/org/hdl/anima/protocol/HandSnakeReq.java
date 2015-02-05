package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;
/**
 * HandSnakeReq message.
 * @author q
 *
 */
public class HandSnakeReq implements Record{

	private String clientType;
	private String apiVersion;
	private String reconnectToken;
	
	@Override
	public void serialize(OutputArchive out) throws IOException {
		out.writeString(clientType);
		out.writeString(apiVersion);
		out.writeString(reconnectToken);
	}

	@Override
	public void deserialize(InputArchive in) throws IOException {
		this.clientType = in.readString();
		this.apiVersion = in.readString();
		this.reconnectToken = in.readString();
	}

	public String getClientType() {
		return clientType;
	}

	public void setClientType(String clientType) {
		this.clientType = clientType;
	}

	public String getApiVersion() {
		return apiVersion;
	}

	public void setApiVersion(String apiVersion) {
		this.apiVersion = apiVersion;
	}

	public String getReconnectToken() {
		return reconnectToken;
	}

	public void setReconnectToken(String reconnectToken) {
		this.reconnectToken = reconnectToken;
	}
}
