package org.hdl.anima.protocol;

import com.google.common.base.Objects;


/**
 * 
 * Request message.
 * @author qiuhd
 * @since  2014年9月12日
 * @version V1.0.0
 */
public class Request extends AbstractMessage{
	
	private int sequence ;
	
	private boolean twoWay;
	
	private boolean mBroken   = false;
	
	public Request(int id) {
		super(id, TYPE_REQUEST);
	}
	
	public Request(int id, byte type) {
		super(id, type);
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public void setTwoWay(boolean twoWay) {
		this.twoWay = twoWay;
	}
	
	public boolean isTwoWay() {
		return twoWay;
	}
	
	public boolean isBroken() {
		return mBroken;
	}

	public void setBroken(boolean broken) {
		this.mBroken = broken;
	}

	public boolean isRequest() {
		return twoWay ;
	}
	
	public boolean isNotify() {
		return twoWay ? false : true;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("requestId", id)
				.add("type", typeToString()).add("sessionId", sid)
				.add("sequence", this.sequence)
				.add("twoWay", String.valueOf(twoWay))
				.add("broken", String.valueOf(mBroken)).toString();
	}
}
