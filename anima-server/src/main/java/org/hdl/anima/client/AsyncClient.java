package org.hdl.anima.client;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.hdl.anima.AppConf;
import org.hdl.anima.AppConstants;
import org.hdl.anima.Application;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.OpenLocalSession;
import org.hdl.anima.protocol.Response;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Async client
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public class AsyncClient {

	private final Logger logger = LoggerFactory.getLogger(AsyncClient.class);
	private String serverId;								//连接后台服务器id
	private final AppConf appConf;
	private final Application application;
	private Client[] clients;
	private final AtomicInteger COUNTER = new AtomicInteger();
	private ScheduledThreadPoolExecutor scheduled ;
	// 心跳定时器
	private ScheduledFuture<?> heatbeatTimer;
	// 心跳超时，毫秒。缺省0，不会执行心跳。
	private int heartbeat = Constants.DEFAULT_HEARTBEAT ;
	private int heartbeatTimeout =  heartbeat * 3;
	
	private OnReponseHandler responseHandler;
	
	/**
	 * Response handler interface
	 * @author qiuhd
	 *
	 */
	public interface OnReponseHandler {
		public void OnCompleted(String serverId,Response response);
	}
	
	public AsyncClient(Application application,AppConf appConf) {
		checkArgument(appConf != null, "appConf == null");
		checkArgument(application != null, "application == null");
		this.appConf = appConf;
		this.application = application;
		
		this.init();
		this.start();
	}
	
	private void init() {
		serverId = appConf.get(AppConstants.SERVER_ID_KEY);
		scheduled = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("async-client-heartbeat[" + serverId + "]" , true));
		int connects = this.appConf.getInt(AppConstants.CONNECTS_KEY, 1);
		clients = new BackgroundServerSideClient[connects];
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
				Codec codec = new AsyncClientCodec(application);
				String remoteIP = appConf.get(AppConstants.REMOTE_IP_KEY);
				int remotePort = appConf.getInt(AppConstants.REMOTE_PORT_KEY, 0);
				int connectTimeout = appConf.getInt(AppConstants.CONNECT_TIMEOUT_KEY, AppConstants.DEFAULT_CONNECT_TIMEOUT);
				AbstractClient client = Transporters.connect(appConf, new BackendServerSideClientHandler(i+1), codec, remoteIP, remotePort, connectTimeout);
				clients[i] = new BackgroundServerSideClient(client);
			} catch (RemotingException e) {
				//ignore
				logger.error("Start server async client error!",e);
			}
		}
	}
	
	/**
	 * Destroy server surrogate
	 */
	public void destroy() {
		destroyServerSideCient();
		stopHeartbeatTimer();
	}
	
	private void destroyServerSideCient() {
		if (clients != null) {
			for (Client client : clients) {
				client.close();
			}
		}
	}
	
	/**
	 * Start server surrogate heatbeat thread
	 */
	private void startHeatbeatTimer() {
		stopHeartbeatTimer();
		if (heartbeat > 0) {
			heatbeatTimer = scheduled.scheduleWithFixedDelay(new HeartBeatTask(
					new HeartBeatTask.ChannelProvider() {
						@Override
						public Collection<Channel> getChannels() {
							return AsyncClient.this.getChannels();
						}
						@Override
						public boolean isClientSide() {
							return true;
						}
					}, heartbeat, heartbeatTimeout), heartbeat, heartbeat,
					TimeUnit.MILLISECONDS);
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
	 * Send message to background server
	 * @param message
	 */
	public void send(AbstractMessage message) {
		try {
			getCient().send(message);
		} catch (RemotingException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Return all server side client channel
	 * @return
	 */
	private Collection<Channel> getChannels() {
		Collection<Channel> channels = new ArrayList<Channel>(clients.length);
		for (Client client : clients) {
			channels.add(client);
		}
		return channels;
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
	
	public void setResponseHandler(OnReponseHandler responseHandler) {
		checkArgument(responseHandler != null,"responseHandler can not be empty!");
		this.responseHandler = responseHandler;
	}

	/**
	 * Server side handler
	 * @author qiuhd
	 *
	 */
	private final class BackendServerSideClientHandler extends ChannelHandlerAdapter {
		/**
		 * 表示连接服务器多个连接序列号
		 */
		private int id;
		
		public BackendServerSideClientHandler(int id) {
			this.id = id;
		}
		
		@Override
		public void connected(Channel channel) throws RemotingException {
			OpenLocalSession req = new OpenLocalSession(application.getServerId(),application.getServerType(),id);
			channel.send(req);
		}

		@Override
		public void received(Channel channel, Object message) throws RemotingException {
			if (isMessage(message)) {
				AbstractMessage abstractMessage = (AbstractMessage) message;
				if(isResponse(abstractMessage)){
					if (responseHandler != null) {
						Response response = (Response) message;
						responseHandler.OnCompleted(serverId, response);
					}else {
						logger.error("Handle respone error",new NullPointerException("responseHandler can not be empty!"));
					}
				}else {
					logger.error("Failed to handle the message of  backend server",new IllegalStateException("UnSupport message type " + abstractMessage.getType()));
				}
				return ;
			}
			throw new RemotingException(channel, "Failed to handle message,cause :unsupport message type: " + message.getClass().getName());
		}
		
		private boolean isMessage(Object message) {
			return message instanceof AbstractMessage ? true : false;
		}
		
		private boolean isResponse(AbstractMessage message) {
			return message.getType() == AbstractMessage.TYPE_RESPONSE;
		}
	}
	
	/**
	 * Background server client
	 * @author qiuhd
	 *
	 */
	private final class BackgroundServerSideClient implements Client {
		
		private final Client client ;
		private Channel channel ;
		
		public BackgroundServerSideClient(Client client) {
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
