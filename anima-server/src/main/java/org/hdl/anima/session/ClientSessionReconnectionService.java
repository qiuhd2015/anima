package org.hdl.anima.session;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TimerTask;

import org.hdl.anima.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ClientSessionReconnectionService
 * @author qiuhd
 * @since  2014-2-25
 * @version V1.0.0
 */
public class ClientSessionReconnectionService  {
	
	public Map<String,ClientSession> frozenSessions;
	private static final long DEFAULT_SESSION_CLEAR_TIME =  3 * 1000;
	private static final Logger logger = LoggerFactory.getLogger(ClientSessionReconnectionService.class);
	private final Application application;
	
	private TimerTask sessionReconnectionClear = new TimerTask() {
		@Override
		public void run() {
			ClientSessionReconnectionService.this.applySessionCleaning();
		}
	};
	
	public ClientSessionReconnectionService(Application application) {
		this.frozenSessions = new HashMap<String, ClientSession>();
		this.application = application ;
		application.schedule(sessionReconnectionClear, 5000, DEFAULT_SESSION_CLEAR_TIME);
	}

	public ClientSession reconnectSession(String reconnectionToken,ClientSession clientSession)throws ClientSessionReconnectionException {
		
		ClientSession session = this.frozenSessions.get(reconnectionToken);
		if (session == null) {
			//fire reconnect failure
			throw new ClientSessionReconnectionException("Session reconnect failure.The passed Session is not managed by the ReconnectionManager:" + clientSession.toString());
		}
		
		if (clientSession.isClosed()) {
			throw new ClientSessionReconnectionException("Sesssion reconnect failure.The new session is not connected:" + session.toString());
		}
		
		if (session.isReconnectTimeExpired()) {
			throw new ClientSessionReconnectionException("Session Reconnection failure. Time expired for Session:" + session.toString());
		}
		
		this.frozenSessions.remove(reconnectionToken);
		session.unfreeze();
		
		//fire reconnect successful event
		logger.debug("Session Reconnection successful.Token {}",reconnectionToken);
		return session;
	}

	public void freezeSession(ClientSession session) {
		
		if (frozenSessions.containsKey(session.getReconnectToken())) {
			throw new IllegalStateException("Unexpected:Session is already managed by ReconnectionManager." + session.toString());
		}
		
		if (session.getReconnectionSeconds() <= 0) {
			throw new IllegalStateException("Unexpected:Session can not be frozen." + session.toString());
		}
		
		if (session.getReconnectToken() == null && "".equals(session.getReconnectToken())) {
			throw new IllegalStateException("Unexpected:Session can not be frozen,Cause: session token id is empty"); 
		}
		
		this.frozenSessions.put(session.getReconnectToken(), session);
		session.freeze();
	}
	
	private void applySessionCleaning() {
		if (this.frozenSessions.size() > 0) {
			for (Iterator<ClientSession> iter = this.frozenSessions.values()
					.iterator(); iter.hasNext();) {
				ClientSession session = iter.next();
				if (session.isReconnectTimeExpired()) {
					iter.remove();
					session.setReconnectionSeconds(0);
					session.close();
					logger.debug("Removing expired reconnectable Session,Token  "+ session.getReconnectToken());
				}
			}
		}
	}

	public void destroy() {
		synchronized (this) {
			if (frozenSessions != null) {
				frozenSessions.clear();
				frozenSessions = null;
				application.cancelScheduledTask(sessionReconnectionClear);
			}
		}
	}
}

