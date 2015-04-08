package org.hdl.anima.remoting;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.support.AbstractClient;
import org.hdl.anima.remoting.support.AbstractServer;

/**
 * Transporter
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public interface Transporter {
	
	/**
	 * Start server
	 * @param conf
	 * @param handler
	 * @param codec
	 * @return
	 * @throws RemotingException
	 */
	AbstractServer bind(AppConf conf,ChannelHandler handler,Codec codec) throws RemotingException ;
	/**
	 * Connect remote server
	 * @param conf
	 * @param handler
	 * @param codec
	 * @return
	 * @throws RemotingException
	 */
	AbstractClient connect(AppConf conf,ChannelHandler handler, Codec codec,String remoteHost, int remotePort, int connectTimeout) throws RemotingException ; 
}

