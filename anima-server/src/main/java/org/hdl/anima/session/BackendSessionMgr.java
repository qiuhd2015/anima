package org.hdl.anima.session;

import java.net.InetSocketAddress;
import java.util.Map;

import org.hdl.anima.common.module.BasicModule;
import org.jboss.netty.util.internal.ConcurrentHashMap;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
/**
 * BackendSessionMgr.
 * @author qiuhd
 * @since  2014年8月15日
 */
public class BackendSessionMgr extends BasicModule{
	
	private Table<String/*fronentId*/,Integer/*sessionId*/,BackendSession> bsessionTableBySid;
	private Map<Integer, BackendSession> sessionByUid;
	
	private SessionCloseListener closeListener = new SessionCloseListener() {
		@Override
		public void onSessionClosed(Object session) {
			if (session != null) {
				BackendSession backendSession = (BackendSession) session;
				bsessionTableBySid.remove(backendSession.getServerId(), backendSession.getId());
				sessionByUid.remove(backendSession.getUserId());
			}
		}
	};
	
	public BackendSessionMgr(String moduleName) {
		super(moduleName);
		bsessionTableBySid = HashBasedTable.create();
		sessionByUid = new ConcurrentHashMap<Integer, BackendSession>();
	}
	
	/**
	 * 返回 session
	 * @param fronentId		前端服务器id
	 * @param sid           前端会话id
	 * @return
	 */
	public BackendSession getBySid(String fronentId,int sid) {
		return this.bsessionTableBySid.get(fronentId, sid);
	}
	/**
	 * 创建session
	 * @param serverId			所在前端服务器id
	 * @param serverType	          所在前端服务器类型
	 * @param sessionId			对应前端服务器的会话id	
	 * @param remoteIP			外网IP
	 * @param remotePort        外网port
	 * @param localIP           本地IP
	 * @param localPort         本地Port
	 * @param clientType        客户端类型  
	 * @return
	 */
	public BackendSession createSession(String serverId,String serverType,int sessionId,String remoteIP,int remotePort,String localIP,int localPort,String clientType) {
		BackendSession session = new BackendSession(this.application);
		session.setId(sessionId);
		session.setServerId(serverId);
		session.setServerType(serverType);
		session.setClientType(clientType);
		session.setRemoteAddress(new InetSocketAddress(remoteIP, remotePort));
		session.setLocalAddress(new InetSocketAddress(localIP,localPort));
		session.setlistener(closeListener);
		bsessionTableBySid.put(serverId, sessionId, session);
		return session;
	}
	
	/**
	 * 绑定userId 到 session中
	 * @param fronentId
	 * @param sid
	 * @param userId
	 */
	public void bind(String fronentId,int sid,int userId) {
		BackendSession session = getBySid(fronentId,sid);
		if (session == null) {
			throw new IllegalStateException("No binding user id to backend sesssion,Cause Not found backe session in cache.");
		}
		sessionByUid.put(userId, session);
	}
	
	public BackendSession getByUid(int userId) {
		return this.sessionByUid.get(userId);
	}

	@Override
	public void destroy() {
		if (bsessionTableBySid != null) {
			bsessionTableBySid.clear();
			bsessionTableBySid = null;
		}
		
		if (sessionByUid != null) {
			bsessionTableBySid.clear();
			bsessionTableBySid = null;
		}
	}
}

