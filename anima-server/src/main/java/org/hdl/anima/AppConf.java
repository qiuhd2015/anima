package org.hdl.anima;

import java.net.InetSocketAddress;

import org.hdl.anima.common.Configuration;
import org.hdl.anima.remoting.Constants;

/**
 * 应用配置
 * @author qiuhd
 * @since 2014-6-9
 * @version V1.0.0
 */
public class AppConf extends Configuration {

	public AppConf() {
		super();
	}

	public AppConf(AppConf other) {
		super(other);
	}
	
	public InetSocketAddress getLocalAddress() {
		String bindAddress = this.get(Constants.BIND_HOST);
		int bindPort = this.getInt(Constants.BIND_PORT,0);
		return new InetSocketAddress(bindAddress,bindPort);
	}
}
