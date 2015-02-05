package org.hdl.anima.protocol;

import java.util.List;

/**
 * Broadcast message
 * @author qiuhd
 * @since  2014年9月12日
 * @version V1.0.0
 */
public class Broadcast extends AbstractMessage {

	private List<Integer> receivers;
	
	public Broadcast(int id) {
		super(id, AbstractMessage.TYPE_BROADCAST);
	}

	public List<Integer> getReceivers() {
		return receivers;
	}

	public void setReceivers(List<Integer> receivers) {
		this.receivers = receivers;
	}
}
