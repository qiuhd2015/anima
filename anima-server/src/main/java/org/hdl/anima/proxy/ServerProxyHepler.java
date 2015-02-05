package org.hdl.anima.proxy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.common.utils.XMLFileHelper;

public final class ServerProxyHepler {

	private final static String CONF_PATH = "/" ;
	
	private final static String CONF_NAME_STRING = "ServerProxys.xml" ;
	
	@SuppressWarnings("unchecked")
	public static List<ServerProxyConfig> loadFromLocal() throws Exception {
		Document document= XMLFileHelper.getXMLFile(CONF_PATH + CONF_NAME_STRING);
		List<ServerProxyConfig> configList = new ArrayList<ServerProxyConfig>();
		Element root = document.getRootElement();
		Iterator<Element> eleIt =  root.elementIterator();
		ServerProxyConfig config = null;
		while (eleIt.hasNext()) {
			Element itemEle =  eleIt.next();
			String id = itemEle.attributeValue("id");
			
			if (StringUtils.isEmpty(id)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the id attribute is empty!") ;
			}
			
			String remoteHost = itemEle.attributeValue("remoteHost");
			
			if (StringUtils.isEmpty(remoteHost)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remoteHost attribute is empty!") ;
			}
			
			String remotePortStr = itemEle.attributeValue("remotePort") ;
			
			if (StringUtils.isEmpty(remotePortStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remotePort attribute is empty!") ;
			}
			
			int remotePort = Integer.valueOf(remotePortStr);
			
			String connectsStr= itemEle.attributeValue("connects");
			
			if (StringUtils.isEmpty(connectsStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connects attribute is empty!") ;
			}
			
			int connects = Integer.valueOf(connectsStr);
			
			String connectTimeoutStr= itemEle.attributeValue("connectTimout");
			
			if (StringUtils.isEmpty(connectTimeoutStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connectTimout attribute is empty!") ;
			}
			
			int connectTimeout = Integer.valueOf(connectTimeoutStr);
			
			String reconectStr = itemEle.attributeValue("reconnect");
			
			if (StringUtils.isEmpty(reconectStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the reconnect attribute is empty!") ;
			}
			
			String sendReconectStr = itemEle.attributeValue("sendReconnect");
			boolean sendReconnect = false;
			if (!StringUtils.isEmpty(sendReconectStr)) {
				sendReconnect = Boolean.parseBoolean(sendReconectStr);
			}
			
			String heartbeatStr = itemEle.attributeValue("heartbeat");
			int heartbeat = 0 ;
			if (!StringUtils.isEmpty(heartbeatStr)) {
				heartbeat = Integer.valueOf(heartbeatStr);
			}
			
			String heartbeatTimeoutStr = itemEle.attributeValue("heartbeatTimeout");
			int heartbeatTimeout = 0 ;
			if (!StringUtils.isEmpty(heartbeatTimeoutStr)) {
				heartbeatTimeout = Integer.valueOf(heartbeatTimeoutStr);
			}
			
			config = new ServerProxyConfig(id,remoteHost,remotePort,connectTimeout,connects);
			config.setReconnect(reconectStr);
			config.setSendReconnect(sendReconnect);
			config.setHeartbeat(heartbeat);
			config.setHeartbeatTimeout(heartbeatTimeout);
			configList.add(config);
		}
		
		return configList;
	}
	
}
