package org.hdl.anima.session;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.remoting.Channel;
import org.slf4j.Logger;

import com.google.common.collect.LinkedListMultimap;
/**
 * LocalSessionMgr
 * @author qiuhd
 * @since  2014年8月15日
 */
public class LocalSessionMgr extends BasicModule{
	
	private LinkedListMultimap<String/*serverId*/, LocalSession> sessionBySid;
	private Map<String/*type*/,LinkedListMultimap<String/*serverId*/,LocalSession>> sessionByStype;
	private final Random random = new Random();
	private final static String SESSION_KEY = "session_key" ;
	private static final Logger logger = org.slf4j.LoggerFactory.getLogger(LocalSessionMgr.class);
	
	public LocalSessionMgr(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		sessionBySid = LinkedListMultimap.create();
		sessionByStype = new HashMap<String, LinkedListMultimap<String,LocalSession>>();
	}

	/**
	 * 会话关闭监听器
	 */
	private SessionCloseListener closeListener = new SessionCloseListener() {
		@Override
		public void onSessionClosed(Object session) {
			if (session != null) {
				LocalSession localSession = (LocalSession) session;
				if (localSession != null) {
					sessionBySid.remove(localSession.getServerId(), localSession);
					LinkedListMultimap<String,LocalSession> multimap = sessionByStype.get(localSession.getServerType());
					if (multimap != null) {
						multimap.remove(localSession.getServerId(), localSession);
					}
				}
			}
		}
	};
	
	/**
	 * 
	 * @param sid
	 * @param serverType
	 * @param sequence
	 * @param localSession
	 * @return
	 */
	public LocalSession addSession(String serverId,String serverType,int sessionId,LocalSession localSession) {
		if (localSession == null) {
			return null;
		}
		localSession.setServerId(serverId);
		localSession.setServerType(serverType);
		localSession.setId(sessionId);
		localSession.setlistener(this.closeListener);
		localSession.setStatus(ISession.STATUS_WORKING);
		sessionBySid.put(serverId, localSession);
		
		synchronized(sessionByStype) {
			LinkedListMultimap<String, LocalSession> multimap = sessionByStype.get(serverType);
			if (multimap == null) {
				multimap = LinkedListMultimap.create();
				sessionByStype.put(serverType, multimap);
			}
			multimap.put(serverId, localSession);
		}
		return localSession;
	}
	
	/**
	 * 返回指定服务器id {@code LocalSession}.
	 * 若存在多个{@code LocalSession}，随机返回一个实例
	 * @param sid
	 * @return
	 */
	private LocalSession getSessionById(String sid) {
		List<LocalSession> localSessionList = sessionBySid.get(sid);
		LocalSession localSession = null;
		if (localSessionList != null && localSessionList.size() > 0 ) {
			if (localSessionList.size() == 1) {
				return localSessionList.get(0);
			}
			int randowIndex = random.nextInt(sessionBySid.size());
			localSession =  localSessionList.get(randowIndex);
		}
		
		if (localSession == null) {
			throw new IllegalStateException("Failed to send the message,Cause:Did not found sid :" + sid);
		}
		return localSession;
	}
	
	public static LocalSession getOrCreateSession(Channel channel) {
		 if (channel == null) {
	        return null;
	     }
		 LocalSession session = (LocalSession) channel.getAttribute(SESSION_KEY);
	     if(session == null) {
	    	session = createSession(channel);
	    	channel.setAttribute(SESSION_KEY, session);
	     }
	     return session;
	}
	
	private static LocalSession createSession(Channel channel) {
		LocalSession session = new LocalSession(channel);
		session.setStatus(ISession.STATUS_CONNECTED);
		return session;
	}
	
	/**
	 * Send message to the specify server
	 * @param serverName
	 * @param message
	 */
	public void send(String sid,AbstractMessage message) {
		LocalSession localSession = getSessionById(sid);
		localSession.send(message);
	}
	
	/**
	 * 广播消息到指定服务器上
	 * @param stype		服务器类型
	 * @param message   消息
	 */
	public void broadcast(String stype,Broadcast message) {
		LinkedListMultimap<String, LocalSession> sessionMap =  sessionByStype.get(stype);
		if (sessionMap == null) {
			logger.error("Failed to broadcast message:" + message,new IllegalStateException("Could not found the fronent server with type " + stype));
			return ;
		}
		for (String serverId : sessionMap.keySet()) {
			LocalSession session = getSessionById(serverId);
			if (session != null) {
				session.send(message);
			}
		}
	}
	/**
	 * 推送消息：ServerId,sessionId,userId
	 */
	public void pushMessge(String serverId,Push push){
		LocalSession localSession = getSessionById(serverId); 
		if (localSession != null) {
			localSession.send(push);
		}
	}
}

