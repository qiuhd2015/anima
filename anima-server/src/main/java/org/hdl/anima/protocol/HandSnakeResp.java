package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;

/**
 * HandSnakeResp message.
 * @author qiuhd
 * @since  2014-2-18
 * @version V1.0.0
 */
public class HandSnakeResp implements Record {
	
	private boolean ok ;
	/**
	 * 心跳定时发送时间
	 */
	private int heartbeatTime;
	private int payload;
	private String reconnectToken;
	
	public HandSnakeResp() {
		
	}
	
	public HandSnakeResp(boolean ok) {
		this.ok = ok;
	}
	
	@Override
	public void serialize(OutputArchive out) throws IOException{
		out.writeBool(ok);
		out.writeInt(heartbeatTime);
		out.writeInt(payload);
		out.writeString(this.reconnectToken);
	}

	@Override
	public void deserialize(InputArchive in) throws IOException{
		this.ok = in.readBool();
		this.heartbeatTime = in.readInt();
		this.payload = in.readInt();
		this.reconnectToken = in.readString();
	}

	public int getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(int heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public int getPayload() {
		return payload;
	}

	public void setPayload(int payload) {
		this.payload = payload;
	}

	public String getReconnectToken() {
		return reconnectToken;
	}

	public void setReconnectToken(String reconnectToken) {
		this.reconnectToken = reconnectToken;
	}

	public boolean isOk() {
		return ok;
	}

	public void setOk(boolean ok) {
		this.ok = ok;
	}
}

