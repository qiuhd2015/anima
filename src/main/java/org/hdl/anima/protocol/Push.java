package org.hdl.anima.protocol;

import java.util.List;

/**
 * Push message
 * @author qiuhd
 * @since  2014年9月12日
 * @version V1.0.0
 */
public class Push extends AbstractMessage{
	
	private List<Integer> receivers;
	
	public Push(int id) {
		super(id, AbstractMessage.TYPE_PUSH);
	}

	public List<Integer> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<Integer> receivers) {
		this.receivers = receivers;
	}
}
