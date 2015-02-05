package org.hdl.anima.surrogate;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hdl.anima.AppConf;
import org.hdl.anima.AppConstants;
import org.hdl.anima.Application;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.CloseClientSession;
import org.hdl.anima.protocol.FreezeClientSession;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.protocol.OpenClientSession;
import org.hdl.anima.protocol.OpenLocalSession;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.protocol.UnFreezeClientSession;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Client;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.Transporters;
import org.hdl.anima.remoting.support.AbstractClient;
import org.hdl.anima.remoting.support.ChannelHandlerAdapter;
import org.hdl.anima.remoting.support.HeartBeatTask;
import org.hdl.anima.session.ClientSession;
import org.hdl.anima.session.ClientSessionMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServerSurrogate.
 * @author qiuhd
 * @since  2014年9月17日
 * @version V1.0.0
 */
public class ServerSurrogate{
	
	private static final Logger logger = LoggerFactory.getLogger(ServerSurrogate.class);
	private String serverName;			//代表的服务器名
	private final AppConf appConf;
	private final Application application;
	private Client[] clients;
	private ClientSessionMgr clientSessionMgr;
	private final AtomicInteger COUNTER = new AtomicInteger();
	private ScheduledThreadPoolExecutor scheduled ;
	// 心跳定时器
	private ScheduledFuture<?> heatbeatTimer;
	// 心跳超时，毫秒。缺省0，不会执行心跳。
	private int heartbeat = Constants.DEFAULT_HEARTBEAT * 100;
	private int heartbeatTimeout =  heartbeat * 3;
	
	public ServerSurrogate(Application application,AppConf appConf) {
		checkArgument(appConf != null, "appConf == null");
		checkArgument(application != null, "application == null");
		this.appConf = appConf;
		this.application = application;
		
		this.init();
		this.start();
	}
	
	private void init() {
		serverName = appConf.get(AppConstants.SERVER_ID_KEY);
		scheduled = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("server-surrogate-heartbeat[" + serverName + "]" , true));
		clientSessionMgr = application.getMoulde(ClientSessionMgr.class);
		int connects = this.appConf.getInt(AppConstants.CONNECTS_KEY, 1);
		clients = new ServerSideClient[connects];
	}
	
	private void start() {
		startServerSideClient();
		startHeatbeatTimer();
	}
	
	/**
	 * Start server side client
	 */
	private void startServerSideClient() {
		int connects = this.appConf.getInt(AppConstants.CONNECTS_KEY, 1);
		for (int i = 0;i < connects;i ++) {
			try {
				Codec codec = new ServerSurrogateCodec(application);
				String remoteIP = appConf.get(AppConstants.REMOTE_IP_KEY);
				int remotePort = appConf.getInt(AppConstants.REMOTE_PORT_KEY, 0);
				int connectTimeout = appConf.getInt(AppConstants.CONNECT_TIMEOUT_KEY, AppConstants.DEFAULT_CONNECT_TIMEOUT);
				AbstractClient client = Transporters.connect(appConf, new ServerSideClientHandler(i+1), codec, remoteIP, remotePort, connectTimeout);
				clients[i] = new ServerSideClient(client);
			} catch (RemotingException e) {
				//ignore
				logger.error("Start server surrogate error!",e);
			}
		}
	}
	
	private void destroyServerSideCient() {
		if (clients != null) {
			for (Client client : clients) {
				client.close();
			}
		}
	}
	/**
	 * Return server side client
	 * @return
	 */
	private Client getCient() {
		if (clients.length == 1) {
			return clients[0];
		}
	    return clients[Math.abs(COUNTER.getAndIncrement() % clients.length)];
	}
	
	/**
	 * Return all server side client channel
	 * @return
	 */
	private Collection<Channel> getChannels() {
		Collection<Channel> channels = new ArrayList<Channel>();
		for (Client client : clients) {
			channels.add(client);
		}
		return channels;
	}
	/**
	 * Send message to background server
	 * @param message
	 */
	public void send(AbstractMessage message) {
		if (message == null)
			return ;
		try {
			getCient().send(message);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * Notify background server the client session already is created 
	 * @param sid
	 * @param remoteIP
	 * @param remotePort
	 * @param localIP
	 * @param localPort
	 * @param clientType
	 */
	public void clientSessionCreated(int sid,String remoteIP,int remotePort,String localIP,int localPort,String clientType) {
		OpenClientSession req = new OpenClientSession();
		req.setSid(sid);
		req.setClientType(clientType);
		req.setRemoteIP(remoteIP);
		req.setRemotePort(remotePort);
		req.setLocalIP(localIP);
		req.setLocalPort(localPort);
		try {
			getCient().send(req);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 *  Notify background server that the client session already is closed
	 * @param sid	
	 */
	public void clientSessionClosed(int sid) {
		try {
			CloseClientSession req = new CloseClientSession(sid);
			getCient().send(req);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	/**
	 * 通知后台服务器session已被冻结
	 * @param sid
	 */
	public void clientSessionFreezed(int sid) {
		try {
			FreezeClientSession req = new FreezeClientSession(sid);
			getCient().send(req);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * 通知后台服务器session冻结解除
	 * @param sid
	 */
	public void clientSessionUnFreezed(int sid) {
		try {
			UnFreezeClientSession req = new UnFreezeClientSession(sid);
			getCient().send(req);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Destroy server surrogate
	 */
	public void destroy() {
		destroyServerSideCient();
		stopHeartbeatTimer();
	}
	
	/**
	 * Start server surrogate heatbeat thread
	 */
	private void startHeatbeatTimer() {
		stopHeartbeatTimer();
		if (heartbeat > 0) {
			heatbeatTimer = scheduled.scheduleWithFixedDelay(
					new HeartBeatTask(new HeartBeatTask.ChannelProvider() {
						@Override
						public Collection<Channel> getChannels() {
							return ServerSurrogate.this.getChannels();
						}

						@Override
						public boolean isClientSide() {
							return true;
						}
					}, heartbeat, heartbeatTimeout), heartbeat, heartbeat,
					TimeUnit.MILLISECONDS);
		}
	}
	
	private void stopHeartbeatTimer() {
		try {
			ScheduledFuture<?> timer = heatbeatTimer;
			if (timer != null && !timer.isCancelled()) {
				timer.cancel(true);
			}
		} catch (Throwable t) {
			logger.warn(t.getMessage(), t);
		} finally {
			heatbeatTimer = null;
		}
	}
	
	/**
	 * Server side handler
	 * @author qiuhd
	 *
	 */
	private final class ServerSideClientHandler extends ChannelHandlerAdapter {
		/**
		 * 表示连接服务器多个连接序列号
		 */
		private int id;
		
		public ServerSideClientHandler(int id) {
			this.id = id;
		}
		
		@Override
		public void connected(Channel channel) throws RemotingException {
			OpenLocalSession req = new OpenLocalSession(application.getServerId(),application.getServerType(),id);
			channel.send(req);
		}

		@Override
		public void received(Channel channel, Object message) throws RemotingException {
			if (isKick(message)) {
				Kick kick = (Kick) message;
				int identity = kick.getIdentity();
				ClientSession session  = clientSessionMgr.get(identity);
				if (session != null && !session.isClosed()) {
					session.getChannel().send(kick);
				}
				return ;
			}
			
			if (isMessage(message)) {
				if (isBroadcase(message)) {
					Broadcast broadcast = (Broadcast) message;
					List<ClientSession> clientSessions = clientSessionMgr.getAll();
					for (ClientSession session : clientSessions) {
						if (session != null && !session.isClosed()) {
							session.send(broadcast);
							if (logger.isTraceEnabled()) {
								logger.trace("Broadcasting message to client identity {},message info {}",session.getId(),message);
							}
						}
					}
				}else if (isPush(message)) {
					Push push = (Push) message;
					List<Integer> receivers = push.getReceivers();
					if (receivers != null && receivers.size() > 0) {
						for (int sid : receivers) {
							ClientSession session  = clientSessionMgr.get(sid);
							if (session != null) {
								session.send(push);
								if (logger.isTraceEnabled()) {
									logger.trace("Transmit message to client id {},message info {}",session.getId(),message);
								}
							}else {
								logger.error("Failed to transmit message,mid :"+ push.getId() +",cause :client session already is cloesd!");
							}
						}
					}
				}else {
					AbstractMessage msg = (AbstractMessage)message;
					ClientSession session  = clientSessionMgr.get(msg.getSid());
					if (session != null) {
						session.send(msg);
						logger.trace("Transmit message to client identity {},Message info {}",session.getId(),message);
					}else {
						logger.error("Failed to transmit message,mid :"+ msg.getId() +",cause :client session already is cloesd!");
					}
				}
				return ;
			}
			
			throw new RemotingException(channel, "Failed to handle message,cause :unsupport message type: " + message.getClass().getName());
		}
		
		private boolean isKick(Object message) {
			return message instanceof Kick ? true : false;
		}
		
		private boolean isBroadcase(Object message) {
			return message instanceof Broadcast ? true : false;
		}
		
		private boolean isMessage(Object message) {
			return message instanceof AbstractMessage ? true : false;
		}
		
		private boolean isPush(Object message) {
			return message instanceof Push ? true : false;
		}
	}
	
	/**
	 * Server side client
	 * @author qiuhd
	 *
	 */
	private final class ServerSideClient implements Client {
		
		private final Client client ;
		private Channel channel ;
		
		public ServerSideClient(Client client) {
			checkArgument(client != null,"client == null");
			this.client = client;
			channel = client;
		}
		
		@Override
		public AppConf getConf() {
			return channel.getConf();
		}

		@Override
		public InetSocketAddress getLocalAddress() {
			return channel.getLocalAddress();
		}

		@Override
		public ChannelHandler getChannelHandler() {
			return channel.getChannelHandler();
		}

		@Override
		public void send(Object message) throws RemotingException {
			channel.send(message);
		}

		@Override
		public void close() {
			channel.close();
		}

		@Override
		public void close(int timeout) {
			channel.close(timeout);
		}

		@Override
		public boolean isClosed() {
			return channel.isClosed();
		}

		@Override
		public boolean isConnected() {
			return channel.isConnected();
		}

		@Override
		public InetSocketAddress getRemoteAddress() {
			return channel.getRemoteAddress();
		}

		@Override
		public Object getAttribute(String key) {
			return channel.getAttribute(key);
		}

		@Override
		public void setAttribute(String key, Object object) {
			this.setAttribute(key, object);
		}

		@Override
		public boolean contains(String key) {
			return channel.contains(key);
		}

		@Override
		public void removeAttribute(String key) {
			channel.removeAttribute(key);
		}

		@Override
		public void reconnect() throws RemotingException {
			client.reconnect();
		}

		@Override
		public int getConnectTimeout() {
			return client.getConnectTimeout();
		}
	}
}
