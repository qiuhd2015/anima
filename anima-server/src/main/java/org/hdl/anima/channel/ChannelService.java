package org.hdl.anima.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.Encodeable;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.session.BackendSession;
import org.hdl.anima.session.BackendSessionMgr;
import org.hdl.anima.session.LocalSessionMgr;
import org.jboss.netty.util.internal.ConcurrentHashMap;

/**
 * Channel service
 * @author qiuhd
 * @since  2014年9月24日
 * @version V1.0.0
 */
public class ChannelService extends BasicModule{

	private Map<Integer, Channel> channels;
	private LocalSessionMgr localSessionMgr;
	private BackendSessionMgr backendSessionMgr;
	
	public ChannelService(String moduleName) {
		super(moduleName);
	}
	/**
	 * 创建管道
	 * @return
	 */
	public Channel createChannel() {
		Channel channel = new Channel(this);
		channels.put(channel.getId(), channel);
		return channel;
	}
	/**
	 * 返回指定id 管道，如果不存在则返回{@null}
	 * @param id
	 * @param create  若指定为{@true}，则创建管道
	 * @return
	 */
	public Channel getChannel(int id,boolean create){
		Channel channel = this.channels.get(id);
		if(channel == null) {
			return channel;
		}
		
		if (create) {
			return createChannel();
		}
		return null;
	}
	/**
	 * 销毁指定id 管道
	 * @param channelId
	 */
	public void destoryChannel(int channelId) {
		Channel channel = this.channels.get(channelId);
		if (channel != null) {
			channel.destory();
			this.channels.remove(channelId);
		}
	}
	/**
	 * 广播消息到所有连接的客户端
	 * @param stype
	 * @param broadcast
	 * @param message
	 */
	public void broadcast(String stype,int msgid,Encodeable message) {
		Broadcast broadcast = new Broadcast(msgid);
		broadcast.setSid(-1);
		broadcast.setContent(message);
		localSessionMgr.broadcast(stype, broadcast);
	}
	/**
	 * 
	 * @param msgid
	 * @param group
	 * @param message
	 */
	public void pushMessageByUids(int msgid,Encodeable message,Member...members) {
		if (members == null || members.length == 0) {
			throw new IllegalArgumentException("Failed to push message,Cause: members should  not be empty") ;
		}
		
		if (message == null) {
			throw new IllegalArgumentException("Failed to push message,Cause: message should  not be empty") ;
		}
		
		//按Fronent server id 分组
		Map<String, List<Member>> membersBySid = new HashMap<String, List<Member>>();
		for (Member member : members) {
			List<Member> memberList ;
			String fserverId = member.getFronentId();
			if (!membersBySid.containsKey(fserverId)) {
				memberList = new ArrayList<Member>();
				membersBySid.put(fserverId, memberList);
			}else {
				memberList = membersBySid.get(fserverId);
			}
			memberList.add(member);
		}
		Push push ;
		for (String fsid : membersBySid.keySet()) {
			push = new Push(msgid);
			push.setSid(-1);
			push.setContent(message);
			List<Member> memberList = membersBySid.get(fsid);
			List<Integer> sessionIds = new ArrayList<Integer>(memberList.size());
			for (Member member : memberList) {
				BackendSession backendSession = backendSessionMgr.getByUid(member.getUid());
				if (backendSession != null) {
					sessionIds.add(backendSession.getId());
				}
			}
			push.setReceivers(sessionIds);
			localSessionMgr.pushMessge(fsid,push);
		}
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		this.localSessionMgr = application.getMoulde(LocalSessionMgr.class);
		this.backendSessionMgr = application.getMoulde(BackendSessionMgr.class);
		if (this.channels == null) {
			this.channels = new ConcurrentHashMap<Integer, Channel>();
		}
	}
}
