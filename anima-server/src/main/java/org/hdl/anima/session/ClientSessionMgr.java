package org.hdl.anima.session;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.surrogate.ServerSurrogateMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * ClientSessionMgr
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class ClientSessionMgr extends BasicModule{
	
	private String serverId;
	private final Map<Integer, ClientSession> sessions = new ConcurrentHashMap<Integer, ClientSession>();
	private final SessionCloseListener listener = new ClientSessionCloseListener();
	private final static String SESSION_KEY = "session_key" ;
	private ClientSessionReconnectionService reconnectionService;
	private final static Logger logger = LoggerFactory.getLogger(ClientSessionMgr.class);
	private ServerSurrogateMgr serverSurrogate ;
	
	/**
	 * Client session close listener
	 * @author qiuhd
	 */
	private class ClientSessionCloseListener implements SessionCloseListener {
		@Override
		public void onSessionClosed(Object session) {
			ClientSession clientSession = (ClientSession) session ;
			remove(clientSession);
			if (clientSession.isWorking() || clientSession.isFreeze()) {
				//通知后台服务器 client session 关闭
				serverSurrogate.clientSessionClosed(clientSession.getId());
			}
		}
	}
	
	public ClientSessionMgr(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		this.serverId = application.getServerId();
		this.reconnectionService = new ClientSessionReconnectionService(application);
		this.serverSurrogate = application.getMoulde(ServerSurrogateMgr.class);
	}

	public static ClientSession createSession(Channel channel) {
		int id = SessionIdFactory.getInstance().getId();
		ClientSession session = new ClientSession(id, channel);
		session.setStatus(ISession.STATUS_HANDSNAKINGT);
		channel.setAttribute(SESSION_KEY, session);
		return session;
	}
	
	public static ClientSession getClientSession(Channel channel) {
		checkArgument(channel != null, "channel can not be null");
		return (ClientSession) channel.getAttribute(SESSION_KEY) ;
	}
	
	public ClientSession get(int id) {
		return (ClientSession) sessions.get(id);
	}
	
	public List<ClientSession> getAll() {
		return new ArrayList<ClientSession>(sessions.values());
	}
	
	public void addSession(ClientSession session) {
		if (session != null) {
			session.setServerId(this.serverId);
			session.setlistener(listener);
			session.setReconnectionSeconds(application.getAppConf().getInt(Constants.RECONNECTION_SECOND_KEY,0));
			sessions.put(session.getId(), session);
		}
	}
	
	public Collection<Channel> getChannels() {
		Collection<ClientSession> c = sessions.values();
		Collection<Channel> channels = new ArrayList<Channel>(sessions.size());
		for (ClientSession session : c) {
			channels.add(session.getChannel());
		}
		return channels; 
	}
	
	public void freezeSession(ClientSession session) {
		if (session == null)
			return;
		
		this.sessions.remove(session.getId());
		try {
			reconnectionService.freezeSession(session);
			logger.debug("Freeze session on token {}",session.getReconnectToken());
			//通知后台服务器session 冻结
			serverSurrogate.clientSessionFreeze(session.getId());
		}catch(Exception e) {
			session.close();
		}
	}
	
	/**
	 * Reconnect session
	 * @param reconnecToken
	 * @param session
	 */
	public ClientSession reconnectSession(String reconnecToken,ClientSession tempSession) throws ClientSessionReconnectionException{
		ClientSession resumedSession = null;
		try {
			resumedSession = this.reconnectionService.reconnectSession(reconnecToken,tempSession);
		} catch (ClientSessionReconnectionException sre) {
			throw sre;
		}
		
		resumedSession.bindChannel(tempSession.getChannel());
		
		if (this.sessions.containsKey(tempSession.getId())) {
			//remove new session
			logger.debug("remove new session:" + tempSession.getId());
			this.sessions.remove(tempSession.getId());
		}
		
		//put old session
		this.sessions.put(resumedSession.getId(), resumedSession);
		return resumedSession;
	}
	
	public void remove(int id) {
		sessions.remove(id);
	}
	
	public void remove(ClientSession session) {
		if (session != null) {
			remove(session.getId()) ;
		}
	}
}


