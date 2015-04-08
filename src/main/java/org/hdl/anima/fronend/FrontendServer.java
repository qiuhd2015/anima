package org.hdl.anima.fronend;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.hdl.anima.AppConf;
import org.hdl.anima.Application;
import org.hdl.anima.blacklist.BlackListMgr;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.handler.RequestDispatcher;
import org.hdl.anima.handler.RequestMappingMethodHandler;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.HandSnakeReq;
import org.hdl.anima.protocol.HandSnakeResp;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.Transporters;
import org.hdl.anima.remoting.support.AbstractChannelHandlerDelegate;
import org.hdl.anima.remoting.support.AbstractServer;
import org.hdl.anima.remoting.support.ChannelHandlerAdapter;
import org.hdl.anima.remoting.support.HeartBeatTask;
import org.hdl.anima.route.Router;
import org.hdl.anima.session.ClientSession;
import org.hdl.anima.session.ClientSessionMgr;
import org.hdl.anima.session.ISession;
import org.hdl.anima.session.ReconnectionTokenFactory;
import org.hdl.anima.session.ClientSessionReconnectionException;
import org.hdl.anima.surrogate.ServerSurrogateMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Frontend  Server
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class FrontendServer extends BasicModule {
	
	private static final Logger logger = LoggerFactory.getLogger(FrontendServer.class);
	private AbstractServer server;
	private AppConf conf ;
	private BlackListMgr blackListMgr;
	private ClientSessionMgr clientSessionMgr;
	private Router router;
	private ServerSurrogateMgr serverSurrogate;
	private RequestMappingMethodHandler requestMappingMethodHandler;
	private RequestDispatcher dispather;
	
    private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("FronentServer-heartbeat", true));
    // 心跳定时器
    private ScheduledFuture<?> heatbeatTimer;
    // 心跳超时，毫秒。缺省0，不会执行心跳。
    private int heartbeat;
    private int heartbeatTimeout;
    
	public FrontendServer(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		this.conf = application.getAppConf();
		this.application = application;
		this.blackListMgr = application.getMoulde(BlackListMgr.class);
		this.clientSessionMgr = application.getMoulde(ClientSessionMgr.class);
		this.heartbeat = conf.getInt(Constants.HEARTBEAT_KEY, Constants.DEFAULT_HEARTBEAT);
		this.heartbeatTimeout = conf.getInt(Constants.HEARTBEAT_TIMEOUT_KEY, heartbeat * 3);
		this.router = application.getMoulde(Router.class);
		this.serverSurrogate = application.getMoulde(ServerSurrogateMgr.class);
		this.requestMappingMethodHandler = application.getMoulde(RequestMappingMethodHandler.class);
		this.dispather = application.getMoulde(RequestDispatcher.class);
	}

	@Override
	public void start() throws IllegalStateException {
		try {
			FrontendCodec codec = new FrontendCodec(this.application);
			server = Transporters.bind(conf, new HandSnakeHandler(channelHandler),codec);
			startHeatbeatTimer();
		} catch (RemotingException e) {
			throw new IllegalStateException("Failed to start the fronent server,cause:"+ e.getMessage(),e);
		}
	}
	
	private void startHeatbeatTimer() {
		stopHeartbeatTimer();
		if (heartbeat > 0) {
			heatbeatTimer = scheduled.scheduleWithFixedDelay(new HeartBeatTask(
					new HeartBeatTask.ChannelProvider() {
						@Override
						public Collection<Channel> getChannels() {
							return clientSessionMgr.getChannels();
						}

						@Override
						public boolean isClientSide() {
							return false;
						}
					}, heartbeat, heartbeatTimeout), heartbeat, heartbeat,
					TimeUnit.MILLISECONDS);
		}
	}

    private void stopHeartbeatTimer() {
        try {
            ScheduledFuture<?> timer = heatbeatTimer;
            if (timer != null && ! timer.isCancelled()) {
                timer.cancel(true);
            }
        } catch (Throwable t) {
            logger.warn(t.getMessage(), t);
        } finally {
            heatbeatTimer =null;
        }
    }
   
	@Override
	public void stop() {
		stopHeartbeatTimer();
		if (server != null) {
			server.close();
		}
	}

	@Override
	public void destroy() {
		stop();
		this.server = null;
	}
	
	private ChannelHandlerAdapter channelHandler = new ChannelHandlerAdapter(){
		
		@Override
		public void connected(Channel channel) throws RemotingException {
			InetSocketAddress socketAddress = channel.getRemoteAddress();
			String address = socketAddress.getAddress().toString();
			boolean result = blackListMgr.contains(address);
			//黑名单处理
			if (result) {
				//通知客户端关闭连接
				channel.send(new Kick("BackList"));
			}
			ClientSessionMgr.createSession(channel);
		}
		
		@Override
		public void caught(Channel channel, Throwable cause) throws RemotingException {
			if (cause instanceof IOException) {
				//ignore
				logger.debug("Caugth exception:{}",cause.getMessage());
			}else {
				logger.error("Caugth exception:{}",cause.getMessage(),cause);
			}
		}

		@Override
		public void disconnected(Channel channel) throws RemotingException {
			ClientSession clientSession = ClientSessionMgr.getClientSession(channel);
			if (clientSession != null) {
				//冻结session
				if (clientSession.getReconnectionSeconds() > 0 && clientSession.isWorking()) {
					clientSessionMgr.freezeSession(clientSession);
				}else {
					clientSession.close();
				}
			}
		}

		@Override
		public void received(Channel channel, Object message)throws RemotingException {
			ClientSession session = ClientSessionMgr.getClientSession(channel);
			if (session != null) {
				if (message instanceof AbstractMessage) {
					Request request = (Request) message;
					request.setSid(session.getId());
					if (!requestMappingMethodHandler.supportRequest(request)) {
						router.route(request);
						return;
					}
					// 处理请求消息
					dispather.dispatch(request, session);
					return ;
				}
				throw new IllegalStateException("Unsupported request: "+ message == null ? null : (message.getClass().getName() + ": " + message));
			} else {
				logger.warn("Failed to dispatch message",new IllegalStateException("Client channel maybe closed :" + channel.toString()));
			}
		}
	};
	
	/**
	 * 
	 * @author qiuhd
	 * @since  2014年9月9日
	 * @version V1.0.0
	 */
	protected final class HandSnakeHandler extends AbstractChannelHandlerDelegate {
		
		public HandSnakeHandler(ChannelHandler channelHandler) {
			super(channelHandler);
		}

		@Override
		public void received(Channel channel, Object message) throws RemotingException {
			if (message instanceof HandSnakeReq) {
				ISession session = ClientSessionMgr.getClientSession(channel);

				if(session != null) {
					handleHandSnake((ClientSession)session,(HandSnakeReq)message);
				}
				return ;
			}
			handler.received(channel, message);
		}
		
		/**
		 * @param session
		 * @param req
		 * @throws RemotingException 
		 */
		private void handleHandSnake(ClientSession session,HandSnakeReq handsnake) throws RemotingException {
			String apiVersion = application.getVersion().getVersionString();
			if (!handsnake.getApiVersion().equals(apiVersion)) {
				HandSnakeResp response = new HandSnakeResp(false);
				session.getChannel().send(response);
				return ;
			}
			
			session.setClientType(handsnake.getClientType());
			session.setStatus(ISession.STATUS_WORKING);
			
			int heartbeat = application.getAppConf().getInt(Constants.HEARTBEAT_KEY, Constants.DEFAULT_HEARTBEAT);
			int payload  = application.getAppConf().getInt(Constants.PAYLOAD_KEY, Constants.DEFAULT_PAYLOAD);
			String reconnectToken = handsnake.getReconnectToken();
			if (StringUtils.isEmpty(reconnectToken)) {
				//通知所有后端服务器创建 BackenSession
				InetSocketAddress remoteAddress = session.getRemoteAddress();
				InetSocketAddress localAddress = session.getLocalAddress();
				int identity = session.getId();
				//通知所有后端服务器创建 BackenSession
				try {
					serverSurrogate.clientSessionCreated(identity,remoteAddress.getHostName(), remoteAddress.getPort(),
							localAddress.getHostName(), localAddress.getPort(),handsnake.getClientType());
				}catch(Exception e) {
					e.printStackTrace();
				}
				reconnectToken = ReconnectionTokenFactory.getInstance().getUniqueSessionToken(session.getRemoteAddress().toString());
				session.setReconnectToken(reconnectToken);
				clientSessionMgr.addSession(session);
			}else {	//实现断线重连
				try {
					logger.debug("Reconnecting on token {}",reconnectToken);
					ClientSession resumedSession = clientSessionMgr.reconnectSession(reconnectToken, session);
					reconnectToken = resumedSession.getReconnectToken();
					//通知后台服务器重连成功
					serverSurrogate.clientSessionUFreeze(resumedSession.getId());
				} catch (ClientSessionReconnectionException e) {
					HandSnakeResp response = new HandSnakeResp(false);
					session.getChannel().send(response);
					logger.error("Reconnection failure on token " + reconnectToken, e);
					return ;
				}
			}
			HandSnakeResp handSnakeResp = new HandSnakeResp(true);
			handSnakeResp.setHeartbeatTime(heartbeat);
			handSnakeResp.setReconnectToken(reconnectToken);
			handSnakeResp.setPayload(payload);
			session.getChannel().send((handSnakeResp));
		}
	}
}

