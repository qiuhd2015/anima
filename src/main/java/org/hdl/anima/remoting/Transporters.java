package org.hdl.anima.remoting;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.support.AbstractClient;
import org.hdl.anima.remoting.support.AbstractServer;
import org.hdl.anima.remoting.support.NettyTransporter;


/**
 * Transporters
 * @author qiuhd
 * @since  2014年8月13日
 */
public final class Transporters {

	private static Transporter transporter = new NettyTransporter();
	
	
	public static AbstractServer bind(AppConf conf,ChannelHandler handler,Codec codec) throws RemotingException{
		return transporter.bind(conf, handler,codec);
	}
	
	public static AbstractClient connect(AppConf conf,ChannelHandler handler, Codec codec,String remoteHost, int remotePort, int connectTimeout) throws RemotingException {
		return transporter.connect(conf, handler,codec,remoteHost,remotePort,connectTimeout);
	}
}

