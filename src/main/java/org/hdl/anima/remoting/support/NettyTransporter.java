package org.hdl.anima.remoting.support;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.Transporter;
import org.hdl.anima.remoting.netty.NettyClient;
import org.hdl.anima.remoting.netty.NettyServer;

/**
 * NettyTransporter
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class NettyTransporter implements Transporter{

	public AbstractServer bind(AppConf conf, ChannelHandler handler, Codec codec)
			throws RemotingException {
		return new NettyServer(conf, handler, codec);
	}

	@Override
	public AbstractClient connect(AppConf conf,ChannelHandler handler, Codec codec,String remoteHost, int remotePort, int connectTimeout) throws RemotingException {
		return new NettyClient(conf, handler,codec,remoteHost, remotePort, connectTimeout);
	}
}

