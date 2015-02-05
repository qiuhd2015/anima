package org.hdl.anima.protocol;

import java.io.IOException;

import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;

/**
 * CloseClientSession message
 * @author qiuhd
 * @since  2014年9月15日
 * @version V1.0.0
 */
public class CloseClientSession implements Record {
	
	/**
	 * 会话id
	 */
	private int sid ;
	
	public CloseClientSession() {
	}
	
	public CloseClientSession(int sid) {
		this.sid = sid;
	}
	
	@Override
	public void serialize(OutputArchive output) throws IOException {
		output.writeInt(this.sid);
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		this.sid = input.readInt();
	}

	public int getSid() {
		return sid;
	}
}
