//package org.hdl.anima.proxy;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ScheduledFuture;
//import java.util.concurrent.ScheduledThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import org.hdl.anima.common.NamedThreadFactory;
//import org.hdl.anima.common.module.BasicModule;
//import org.hdl.anima.remoting.Channel;
//import org.hdl.anima.remoting.Constants;
//import org.hdl.anima.remoting.RemotingException;
//import org.hdl.anima.remoting.support.HeartBeatTask;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * 
// * @author qiuhd
// * @since  2014年8月13日
// */
//public class ServerProxys extends BasicModule {
//	
//	private static final Logger logger = LoggerFactory.getLogger(ServerProxys.class) ;
//	private Map<String, ServerProxy> proxyCache;
//	private List<ServerProxyConfig> serverProxyConfigs;
//	private static final ScheduledThreadPoolExecutor scheduled = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory("ServerProxy-heartbeat", true));
//	// 心跳定时器
//	private ScheduledFuture<?> heatbeatTimer;
//	// 心跳超时，毫秒。缺省0，不会执行心跳。
//	private int heartbeat = Constants.DEFAULT_HEARTBEAT * 100;
//	private int heartbeatTimeout =  heartbeat * 3;
//	
//	public ServerProxys(String moduleName) {
//		super(moduleName);
//		proxyCache = new HashMap<String, ServerProxy>();
//	}
//	
//	public ServerProxy getServerProxy(String serverId) {
//		return proxyCache.get(serverId);
//	}
//	
//	@Override
//	public void start() throws IllegalStateException {
//		try {
//			serverProxyConfigs =  ServerProxyHepler.loadFromLocal();
//		} catch (Exception e) {
//			throw new IllegalStateException("Failed to load from the static server proxy xml file");
//		}
//		
//		ServerProxy proxy = null;
//		for (ServerProxyConfig config : serverProxyConfigs) {
//			proxy = new ServerProxy(application,config);
//			try {
//				proxy.start();
//				logger.info("Success in Starting to server Proxy,Proxy id :{}",config.getId());
//			} catch (RemotingException e) {
//				throw new IllegalStateException("Failed to start server proxy,proxy id:" + config.getId()+",cause :" + e.getMessage(),e);
//			}
//			proxyCache.put(proxy.getId(), proxy);
//		}
//		
//		startHeatbeatTimer();
//	}
//	
//	
//	private void startHeatbeatTimer() {
//		stopHeartbeatTimer();
//		if (heartbeat > 0) {
//			heatbeatTimer = scheduled.scheduleWithFixedDelay(
//					new HeartBeatTask(new HeartBeatTask.ChannelProvider() {
//						@Override
//						public Collection<Channel> getChannels() {
//							Collection<Channel> channels = new ArrayList<Channel>();
//							for (ServerProxy proxy : proxyCache.values()) {
//								channels.addAll(proxy.getChannels());
//							}
//							return channels;
//						}
//
//						@Override
//						public boolean isClientSide() {
//							return true;
//						}
//					}, heartbeat, heartbeatTimeout), heartbeat, heartbeat,
//					TimeUnit.MILLISECONDS);
//		}
//	}
//	    
//	private void stopHeartbeatTimer() {
//		try {
//			ScheduledFuture<?> timer = heatbeatTimer;
//			if (timer != null && !timer.isCancelled()) {
//				timer.cancel(true);
//			}
//		} catch (Throwable t) {
//			logger.warn(t.getMessage(), t);
//		} finally {
//			heatbeatTimer = null;
//		}
//	}
//	
//	public void openClientSession(int identity,String remoteAddress,int remotePort,String localAddress,int localPort,String clientType) {
//		for (ServerProxy proxy :proxyCache.values()) {
//			proxy.openClientSession(identity, remoteAddress, remotePort, localAddress, localPort, clientType);
//		}
//	}
//	
//	public void closeClientSession(int identity) {
//		for (ServerProxy proxy :proxyCache.values()) {
//			proxy.closeClientSession(identity);
//		}
//	}
//
//	@Override
//	public void stop() {
//		stopHeartbeatTimer();
//		if (proxyCache != null) {
//			for (ServerProxy proxy : proxyCache.values()) {
//				proxy.stop();
//			}
//		}
//	}
//
//	@Override
//	public void destroy() {
//		stop();
//		if (proxyCache != null) {
//			proxyCache.clear();
//			proxyCache = null;
//		}
//		
//		if (serverProxyConfigs != null) {
//			serverProxyConfigs.clear();
//			serverProxyConfigs = null;
//		}
//	}
//}
//
