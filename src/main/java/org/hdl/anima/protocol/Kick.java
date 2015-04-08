package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;

/**
 * Kick message.
 * @author qiuhd
 * @since  2014年9月15日
 * @version V1.0.0
 */
public class Kick implements Record{

	private int identity;
	private String cause;
	
	public Kick() {
		
	}
	
	public Kick(String cause) {
		this.cause = cause;
	}
	
	public Kick(int identity,String cause) {
		this.identity = identity;
		this.cause = cause;
	}
	
	@Override
	public void serialize(OutputArchive output) throws IOException {
		output.writeString(cause);
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		this.cause = input.readString();
	}

	public int getIdentity() {
		return identity;
	}

	public void setIdentity(int identity) {
		this.identity = identity;
	}

	public String getCause() {
		return cause;
	}

	public void setCause(String cause) {
		this.cause = cause;
	}
}
