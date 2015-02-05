package org.hdl.anima.test.benchmark;

/**
 * 
 * @author qiuhd
 * @since 2014年10月8日
 * @version V1.0.0
 */
public class ExchangeClientFactory {
	
	
	public ExchangeClientFactory() {
		
	}
	
	public ExchangeClient get(String targetIp,int targetPort,int timeout) throws Exception {
		return createClient(targetIp, targetPort, timeout);
	}
	
	private ExchangeClient createClient(String targetIp,int targetPort,int timeout) throws Exception {
		ExchangeClient client = new ExchangeClient(targetIp, targetPort, timeout);
		client.start();
		return client;
	}
}
