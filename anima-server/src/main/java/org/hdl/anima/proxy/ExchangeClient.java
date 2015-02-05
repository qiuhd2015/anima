//package org.hdl.anima.proxy;
//
//import static com.google.common.base.Preconditions.checkArgument;
//
//import org.hdl.anima.AppConf;
//import org.hdl.anima.remoting.Channel;
//import org.hdl.anima.remoting.ChannelHandler;
//import org.hdl.anima.remoting.Codec;
//import org.hdl.anima.remoting.RemotingException;
//import org.hdl.anima.remoting.Transporters;
//import org.hdl.anima.remoting.support.AbstractClient;
///**
// * 
// * @author qiuhd
// * @since  2014年8月13日
// */
//public class ExchangeClient {
//
//	private AbstractClient client ;
//	private final ServerProxyConfig config;
//	private final AppConf conf;
//	
//	public ExchangeClient(ServerProxy proxy,ChannelHandler handler,Codec codec) throws RemotingException {
//		checkArgument(proxy != null,"serverProxy == null");
//		this.config = proxy.getConifg();
//		this.conf = proxy.getAppConf();
//		client  = Transporters.connect(conf, handler, codec, config.getRemoteHost(), config.getRemotePort(), config.getConnectTimeout());
//	}
//	
//	public boolean isConnected() {
//		return client.isConnected();
//	}
//	
//	public void close() {
//		client.close();
//	}
//	
//	public void send(Object message) throws RemotingException {
//		client.send(message);
//	}
//	
//	public Channel getChannel() {
//		return client.getChannel();
//	}
//}
//
