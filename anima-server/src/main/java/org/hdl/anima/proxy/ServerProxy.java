//package org.hdl.anima.proxy;
//
//import static com.google.common.base.Preconditions.checkArgument;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.List;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import org.hdl.anima.AppConf;
//import org.hdl.anima.Application;
//import org.hdl.anima.protocol.AbstractMessage;
//import org.hdl.anima.protocol.CloseClientSession;
//import org.hdl.anima.protocol.OpenClientSession;
//import org.hdl.anima.remoting.Channel;
//import org.hdl.anima.remoting.ChannelHandler;
//import org.hdl.anima.remoting.Codec;
//import org.hdl.anima.remoting.Constants;
//import org.hdl.anima.remoting.RemotingException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
///**
// * 
// * @author qiuhd
// * @since  2014年8月13日
// */
//public class ServerProxy{
//	
//	private static final Logger logger = LoggerFactory.getLogger(ServerProxy.class);
//	private final Application application;
//	private ServerProxyConfig config;
//	private final ChannelHandler channelHandler;
//	private ExchangeClient[] clients;
//	private final AtomicInteger proxyIndex = new AtomicInteger();
//	private final AppConf conf;
//	
//	public ServerProxy(Application app,ServerProxyConfig  config) {
//		checkArgument(app != null, "app == null!");
//		checkArgument(config != null, "config == null!");
//		this.application = app;
//		this.config = config;
//		conf = new AppConf(application.getAppConf());
//		initAppConf(config);
//		channelHandler = new ServerProxyHanlder(application,this);
//		clients = new ExchangeClient[config.getConnects()];
//	}
//	
//	private void initAppConf(ServerProxyConfig config) {
//		this.conf.set(Constants.RECONNECT_PERIOD_KEY, config.getReconnect());
//		this.conf.setBoolean(Constants.SEND_RECONNECT_KEY, config.isSendReconnect());
//		this.conf.setInt(Constants.THREADS_KEY, 50);
//	}
//	
//	public void setApplication(Application application) {
//		checkArgument(application != null,"application == null");
//	}
//
//	public String getId() {
//		return this.config.getId();
//	}
//	
//	public void start() throws RemotingException{
//		for (int i= 0 ;i < config.getConnects();i++) {
//			Codec codec = new ProxyCodec(application);
//			ExchangeClient proxy =  new ExchangeClient(this, channelHandler, codec);
//			clients[i] = proxy;
//		}
//	}
//
//	public void stop() {
//		for (ExchangeClient proxy : clients) {
//			proxy.close();
//		}
//		clients = null;
//	}
//	
//	public void send(AbstractMessage message) {
//		checkArgument(message != null,"message == null");
//		try {
//			nextProxy().send(message);
//		} catch (RemotingException e) {
//			throw new IllegalStateException(e);
//		}
//	}
//	
//	public void openClientSession(int identity,String remoteIP,int remotePort,String localIP,int localPort,String clientType) {
//		OpenClientSession req = new OpenClientSession();
//		req.setIdentity(identity);
//		req.setClientType(clientType);
//		req.setRemoteIP(remoteIP);
//		req.setRemotePort(remotePort);
//		req.setLocalIP(localIP);
//		req.setLocalPort(localPort);
//		try {
//			nextProxy().send(req);
//		} catch (RemotingException e) {
//			throw new IllegalStateException(e);
//		}
//	}
//	
//	public void closeClientSession(int identity) {
//		try {
//			CloseClientSession req = new CloseClientSession(identity);
//			nextProxy().send(req);
//		} catch (RemotingException e) {
//			throw new IllegalStateException(e);
//		}
//	}
//
//	private ExchangeClient nextProxy() {
//	    return clients[Math.abs(proxyIndex.getAndIncrement() % clients.length)];
//	}
//
//	public boolean isAvailable() {
//		for (ExchangeClient proxy : clients) {
//			if (proxy.isConnected())
//				return true;
//		}
//		return false;
//	}
//
//	public ServerProxyConfig getConifg() {
//		return this.config;
//	}
//
//	public Application getApplication() {
//		return this.application;
//	}
//
//	public AppConf getAppConf() {
//		return this.conf;
//	}
//
//	public Collection<Channel> getChannels() {
//		List<Channel> channels = new ArrayList<Channel>(clients.length);
//		for (ExchangeClient client : clients) {
//			channels.add(client.getChannel());
//		}
//		return channels;
//	}
//}
//
