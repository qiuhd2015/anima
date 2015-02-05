package org.hdl.anima.channel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hdl.anima.common.io.Encodeable;
/**
 * Channel 
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public class Channel {
	
	/** 管道 唯一标识 */
	private int id;
	/** 管道成员组  */
	private Map<Integer, Member> members;
	/** 管道id生存id */
	private static final AtomicInteger ID_GENERATOR = new AtomicInteger();
	private ChannelService service;
	
	public Channel(ChannelService service) {
		this.service = service;
		members = new HashMap<Integer, Member>(0);
		id = ID_GENERATOR.incrementAndGet();
	}
	
	/**
	 * 添加成员
	 * @param uid  用户唯一id
	 * @param sid  用户所在前端服务器id
	 */
	public void add(int uid,String sid) {
		if (!members.containsKey(uid)) {
			members.put(uid,new Member(uid, sid)) ;
		}
	}
	
	/**
	 * 移除成员
	 * @param uid 用户唯一id
	 */
	public void leave(int uid) {
		if (members.containsKey(uid)) {
			members.remove(uid);
		}
	}
	
	/**
	 * 返回{@ Channel}唯一id
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	public List<Member> getMembers() {
		return new ArrayList<Member>(members.values());
	}
	
	public Member getMember(int uid) {
		return this.members.get(uid);
	}
	
	/**
	 * 向管道里所有成员推送消息
	 * @param msgId
	 * @param message
	 */
	public void pushMessage(int msgId,Encodeable message) {
		if (members == null) {
			throw new IllegalStateException("Channel already destory");
		}
		Collection<Member> memberCollection = members.values();
		Member[] memberArray = new Member[memberCollection.size()];
		memberCollection.toArray(memberArray);
		service.pushMessageByUids(msgId, message, memberArray);
	}
	
	public void destory(){
		if (members != null) {
			service.destoryChannel(this.id);
			members.clear(); 
			members = null;
			service = null;
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Channel other = (Channel) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
