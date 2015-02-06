package org.hdl.anima.backend;

import java.io.IOException;

import org.hdl.anima.AppConf;
import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.handler.RequestDispatcher;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.CloseClientSession;
import org.hdl.anima.protocol.FreezeClientSession;
import org.hdl.anima.protocol.OpenClientSession;
import org.hdl.anima.protocol.OpenLocalSession;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.UnFreezeClientSession;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.Transporters;
import org.hdl.anima.remoting.support.AbstractChannelHandlerDelegate;
import org.hdl.anima.remoting.support.AbstractServer;
import org.hdl.anima.remoting.support.ChannelHandlerAdapter;
import org.hdl.anima.session.BackendSession;
import org.hdl.anima.session.BackendSessionMgr;
import org.hdl.anima.session.ISession;
import org.hdl.anima.session.LocalSession;
import org.hdl.anima.session.LocalSessionMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * backend server 
 * @author qiuhd
 * @since  2014年8月15日
 */
public class BackendServer extends BasicModule {

	private static final Logger logger = LoggerFactory.getLogger(BackendServer.class);
	private AbstractServer server ;
	private AppConf conf ;
	private Application application;
	private LocalSessionMgr localSessionMgr;
	private BackendSessionMgr backendSessionMgr;
	private RequestDispatcher dispatcher;
	
	public BackendServer(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		this.conf = application.getAppConf();
		this.application = application;
		this.dispatcher = application.getMoulde(RequestDispatcher.class);
		this.localSessionMgr = this.application.getMoulde(LocalSessionMgr.class);
		this.backendSessionMgr = this.application.getMoulde(BackendSessionMgr.class);
	}
	
	@Override
	public void start() throws IllegalStateException {
		try {
			BackendCodec codec = new BackendCodec(application);
			server = Transporters.bind(conf, wrapChannelHandler(requestChannelHandler),codec);
		} catch (RemotingException e) {
			throw new IllegalStateException("Failed start backend server,cause :" + e.getMessage(),e);
		}
	}

	private ChannelHandler wrapChannelHandler(ChannelHandler channelHandler) {
		return new SystemMessageHandler(channelHandler);
	}
	
	@Override
	public void stop() {
		if (server != null) {
			server.close();
		}
	}

	@Override
	public void destroy() {
		stop();
		if (server != null) {
			server = null;
		}
	}
	
	/**
	 * 接受客户端请求消息
	 */
	private ChannelHandlerAdapter requestChannelHandler = new ChannelHandlerAdapter(){
		
		@Override
		public void connected(Channel channel) throws RemotingException {
			LocalSessionMgr.getOrCreateSession(channel);
		}
		
		@Override
		public void caught(Channel channel, Throwable cause) throws RemotingException {
			if (cause instanceof IOException) {
				//ignore
				logger.trace("Caugth exception:{}",cause.getMessage());
			}else {
				logger.error("Caugth exception:{}",cause.getMessage(),cause);
			}
		}

		@Override
		public void disconnected(Channel channel) throws RemotingException {
			LocalSession session = LocalSessionMgr.getOrCreateSession(channel);
			if (session != null) {
				session.close();
				logger.trace("Close Local session {}" ,session.toString());
			}
		}

		@Override
		public void received(Channel channel, Object message)
				throws RemotingException {
			LocalSession session = LocalSessionMgr.getOrCreateSession(channel);
			if (isRequestMessage(message)) {
				AbstractMessage msg = (AbstractMessage) message;
				int sid = msg.getSid();
				if (msg.isBackendMessage()) {
					dispatcher.dispatch((Request) msg, session);
				} else {
					BackendSession bSession = backendSessionMgr.getBySid(session.getServerId(), sid);
					if (bSession == null) {
						throw new IllegalStateException("Failed to dispatch request,Request info :" + msg + "Cause:Client channel already is closed!");
					} else {
						dispatcher.dispatch((Request) msg, bSession);
					}
				}
			} else {
				throw new IllegalStateException("Unsupported request: "+ message == null ? null : (message.getClass().getName() + ": " + message));
			}
		}
		
		private boolean isRequestMessage(Object message) {
			return (message instanceof AbstractMessage) ? true:false;
		}
	};
	
	/**
	 * 内部类，处理服务器之间消息
	 * @author qiuhd
	 *
	 */
	private final class SystemMessageHandler extends AbstractChannelHandlerDelegate {
		
		public SystemMessageHandler(ChannelHandler channelHandler) {
			super(channelHandler);
		}

		@Override
		public void received(Channel channel, Object message)
				throws RemotingException {
			LocalSession session = LocalSessionMgr.getOrCreateSession(channel);
			if (session == null)
				return ;
			
			if (isOpenLocalSession(message)) {
				OpenLocalSession req = (OpenLocalSession)message;
				String sid =  req.getServerId();
				String stype = req.getServetType();
				int sessionId = req.getSessionId();
				session.setStatus(ISession.STATUS_WORKING);
				session.setId(sessionId);
				localSessionMgr.addSession(sid, stype, req.getSessionId(),(LocalSession)session);
				logger.debug("Open local session {}",session.toString());
				return ;
			} else if (isOpenClientSession(message)) {
				OpenClientSession req = (OpenClientSession)message;
				BackendSession backendSession = backendSessionMgr.createSession(session.getServerId(),session.getServerType(),req.getSid(),
						req.getRemoteIP(), req.getRemotePort(), req.getLocalIP(), req.getLocalPort(),req.getClientType());
				backendSession.setStatus(ISession.STATUS_WORKING);
				logger.debug("Open client session {}",backendSession.toString());
				return ;
			} else if (isCloseClientSession(message)) {
				CloseClientSession req = (CloseClientSession)message;
				BackendSession backendSession =  backendSessionMgr.getBySid(session.getServerId(),req.getSid());
				if (backendSession != null) {
					backendSession.close();
					logger.debug("Close client session {}",backendSession.toString());
				}
				return ;
			} else if (isFreezeClientSession(message)){
				FreezeClientSession freezeClientSession = (FreezeClientSession)message;
				int sessionId = freezeClientSession.getSid();
				String fronentServerId = session.getServerId();
				BackendSession backendSession = backendSessionMgr.getBySid(fronentServerId, sessionId);
				
				if (backendSession != null) {
					backendSession.freeze();
					logger.debug("Freeze backend session {}",backendSession.toString());
				}
				return ;
			} else if (isUnFreezeClientSession(message)) {
				UnFreezeClientSession freezeClientSession = (UnFreezeClientSession)message;
				int sessionId = freezeClientSession.getSid();
				String fronentServerId = session.getServerId();
				BackendSession backendSession = backendSessionMgr.getBySid(fronentServerId, sessionId);
				
				if (backendSession != null) {
					backendSession.unfreeze();
					logger.debug("UnFreeze backend session {}",backendSession.toString());
				}
				return ;
			}
			
			handler.received(channel, message);
		}
		
		private boolean isOpenLocalSession(Object message) {
			return (message instanceof OpenLocalSession) ? true:false;
		}
		
		private boolean isOpenClientSession(Object message) {
			return (message instanceof OpenClientSession) ? true:false;
		}
		
		private boolean isCloseClientSession(Object message) {
			return (message instanceof  CloseClientSession) ? true:false;
		}
		
		private boolean isFreezeClientSession(Object message) {
			return (message instanceof  FreezeClientSession) ? true:false;
		}
		
		private boolean isUnFreezeClientSession(Object message) {
			return (message instanceof  UnFreezeClientSession) ? true:false;
		}
	}
}

