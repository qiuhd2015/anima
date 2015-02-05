//package org.hdl.anima.proxy;
//
//import org.hdl.anima.Application;
//import org.hdl.anima.protocol.AbstractMessage;
//import org.hdl.anima.protocol.OpenLocalSession;
//import org.hdl.anima.remoting.Channel;
//import org.hdl.anima.remoting.RemotingException;
//import org.hdl.anima.remoting.support.ChannelHandlerAdapter;
//import org.hdl.anima.session.ClientSessionMgr;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
///**
// * 
// * @author qiuhd
// * @since  2014年8月13日
// */
//public class ServerProxyHanlder extends ChannelHandlerAdapter {
//	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ServerProxyHanlder.class);
//	private ClientSessionMgr clientSessionMgr;
//	private final ServerProxy serverProxy;
//	private final Application application;
//	
//	public ServerProxyHanlder(Application application,ServerProxy serverProxy) {
//		this.application = application;
//		this.clientSessionMgr = application.getMoulde(ClientSessionMgr.class);
//		this.serverProxy = serverProxy;
//	}
//
//	@Override
//	public void connected(Channel channel) throws RemotingException {
//		OpenLocalSession req = new OpenLocalSession(application.getName());
//		channel.send(req);
//	}
//
//	@Override
//	public void caught(Channel channel, Throwable cause)throws RemotingException {
//		//LOGGER.error("Caught Excpetion:" + cause.getMessage(),cause);
//	}
//
//	@Override
//	public void disconnected(Channel channel) throws RemotingException {
//		
//	}
//
//	@Override
//	public void received(Channel channel, Object message) throws RemotingException {
//		if (message instanceof AbstractMessage) {
//			
//		}
//	}
//}
//
