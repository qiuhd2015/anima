package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;

import com.google.common.base.Objects;

/**
 * OpenLocalSession message
 * @author qiuhd
 * @since  2014年9月15日
 * @version V1.0.0
 */
public class OpenLocalSession implements Record{
	/**
	 * 服务器唯一
	 */
	private String serverId ;
	/**
	 * 服务器类型
	 */
	private String stype;
	/**
	 * {@LocalSession} id
	 */
	private int sessionId;
	
	public OpenLocalSession() {
		
	}
	
	public OpenLocalSession(String serverId,String stype,int sessionId) {
		this.serverId = serverId;
		this.stype = stype;
		this.sessionId = sessionId;
	}
	
	@Override
	public void serialize(OutputArchive output) throws IOException {
		output.writeString(this.serverId);
		output.writeString(this.stype);
		output.writeInt(sessionId);
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		this.serverId = input.readString();
		this.stype = input.readString();
		this.sessionId = input.readInt();
	}

	public String getServerId() {
		return serverId;
	}

	public String getServetType() {
		return stype;
	}

	public int getSessionId() {
		return sessionId;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("serverType", stype)
				.add(serverId, serverId).add("sessionId", sessionId).toString();
	}
}
