package org.hdl.anima;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.hdl.anima.client.AsyncClientConfig;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.common.utils.XMLFileHelper;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.surrogate.ServerSurrogateConfig;

/**
 * Application Helper
 * @author qiuhd
 * @since  2014-2-27
 * @version V1.0.0
 */
public final class AppHelper {
	
	private final static String CONF_PATH = "/" ;
	
	private final static String CONF_NAME_STRING = "Application.xml" ;
	
	@SuppressWarnings("unchecked")
	public static void loadFromStaticXml(Application application) throws DocumentException  {
		Document document= XMLFileHelper.getXMLFile(CONF_PATH + CONF_NAME_STRING);
		Element root = document.getRootElement();
		Element serversElement = root.element("servers");
		
		if (serversElement == null) {
			throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : No found servers element") ;
		}
		
		Iterator<Element> serverElementIt = serversElement.elementIterator();
		while(serverElementIt.hasNext()) {
			Element	serverElement = serverElementIt.next();
			String id = serverElement.attributeValue("id");
			
			if (StringUtils.isEmpty(id)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the id attribute is empty!") ;
			}
			
			String type = serverElement.attributeValue("type");
			
			if (StringUtils.isEmpty(type)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the type attribute is empty!") ;
			}
			
			String host = serverElement.attributeValue("host");
			
			if (StringUtils.isEmpty(host)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the host attribute is empty!") ;
			}
			
			String portStr = serverElement.attributeValue("port");
			
			if (StringUtils.isEmpty(portStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the port attribute is empty!") ;
			}
			
			int port = Integer.valueOf(portStr);
			
			String frontendStr = serverElement.attributeValue("frontend");
			
			if (StringUtils.isEmpty(frontendStr)) {
				throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the frontend attribute is empty!") ;
			}
			
			boolean frontend = Boolean.valueOf(frontendStr);
			
			String threadsStr = serverElement.attributeValue("threads");
			int threads = 0;
			if (!StringUtils.isEmpty(threadsStr)) {
				threads = Integer.valueOf(threadsStr);
			}else {
				threads = Constants.DEFAULT_THREADS;
			}
			
			String maxConnectsStr = serverElement.attributeValue("maxConnects");
			int maxConnects = 0;
			if (!StringUtils.isEmpty(maxConnectsStr)) {
				maxConnects = Integer.valueOf(maxConnects);
			}else {
				maxConnects = Constants.DEFAULT_MAX_CLIENTS;
			}
			
			String heartbeatStr = serverElement.attributeValue("heartbeat");
			int heartbeat = 0 ;
			if (!StringUtils.isEmpty(heartbeatStr)) {
				heartbeat = Integer.valueOf(heartbeatStr);
			}
			
			String heartbeatTimeoutStr = serverElement.attributeValue("heartbeatTimeout");
			int heartbeatTimeout = 0 ;
			if (!StringUtils.isEmpty(heartbeatTimeoutStr)) {
				heartbeatTimeout = Integer.valueOf(heartbeatTimeoutStr);
			}
			
			String reconnectionSecondStr = serverElement.attributeValue("reconnectionSecond");
			int reconnectionSecond = 0 ;
			if (!StringUtils.isEmpty(reconnectionSecondStr)) {
				reconnectionSecond = Integer.valueOf(reconnectionSecondStr);
			}
			
			application.serverConfig = new ServerConfig();

			application.serverConfig.setServerId(id);
			application.serverConfig.setServetType(type);
			application.serverConfig.setHost(host);
			application.serverConfig.setPort(port);
			application.serverConfig.setFrontend(frontend);
			application.serverConfig.setThreads(threads);
			application.serverConfig.setMaxConnects(maxConnects);
			application.serverConfig.setHeartbeat(heartbeat);
			application.serverConfig.setHeartbeatTimeout(heartbeatTimeout);
			application.serverConfig.setReconnectionSecond(reconnectionSecond);
			//server surrogate
			Element surrogatesEle = serverElement.element("frontend-to-backends");
			ServerSurrogateConfig surrogateConfig ;
		
			if (surrogatesEle != null) {
				Iterator<Element> surrogateEleIt = surrogatesEle.elementIterator("frontend-to-backend");
				List<ServerSurrogateConfig> surrogateConfigs = new ArrayList<ServerSurrogateConfig>();
				while (surrogateEleIt.hasNext()) {
					Element itemEle =  surrogateEleIt.next();
					String remoteName = itemEle.attributeValue("connect-server-id");
					if (StringUtils.isEmpty(remoteName)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connect-server-id attribute is empty!") ;
					}
					
					String remoteIP = itemEle.attributeValue("remoteIP");
					
					if (StringUtils.isEmpty(remoteIP)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remoteIP attribute is empty!") ;
					}
					
					String remotePortStr = itemEle.attributeValue("remotePort") ;
					
					if (StringUtils.isEmpty(remotePortStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remotePort attribute is empty!") ;
					}
					
					int remotePort = Integer.valueOf(remotePortStr);
					
					String connectsStr = itemEle.attributeValue("connects");
					
					if (StringUtils.isEmpty(connectsStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connects attribute is empty!") ;
					}
					
					int connects = Integer.valueOf(connectsStr);
					
					String connectTimeoutStr = itemEle.attributeValue("connectTimout");
					
					if (StringUtils.isEmpty(connectTimeoutStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connectTimout attribute is empty!") ;
					}
					
					int connectTimeout = Integer.valueOf(connectTimeoutStr);
					
					String reconnectStr = itemEle.attributeValue("reconnect");
					
					if (StringUtils.isEmpty(reconnectStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the reconnect attribute is empty!") ;
					}
					
					String sendReconectStr = itemEle.attributeValue("sendReconnect");
					boolean sendReconnect = false;
					if (!StringUtils.isEmpty(sendReconectStr)) {
						sendReconnect = Boolean.parseBoolean(sendReconectStr);
					}
					
					heartbeatStr = itemEle.attributeValue("heartbeat");
					if (!StringUtils.isEmpty(heartbeatStr)) {
						heartbeat = Integer.valueOf(heartbeatStr);
					}
					
					heartbeatTimeoutStr = itemEle.attributeValue("heartbeatTimeout");
					if (!StringUtils.isEmpty(heartbeatTimeoutStr)) {
						heartbeatTimeout = Integer.valueOf(heartbeatTimeoutStr);
					}
					
					surrogateConfig = new ServerSurrogateConfig(remoteName,remoteIP,remotePort,connectTimeout,connects);
					surrogateConfig.setReconnect(reconnectStr);
					surrogateConfig.setSendReconnect(sendReconnect);
					surrogateConfig.setHeartbeat(heartbeat);
					surrogateConfigs.add(surrogateConfig);
				}
				application.serverConfig.setSurrogateConfigs(surrogateConfigs);
			}
			
			Element bakendToBackendsElement = serverElement.element("backend-to-backends");
			AsyncClientConfig asyncClientConfig ;
			
			if (bakendToBackendsElement != null) {
				Iterator<Element> backendToBackendIt = bakendToBackendsElement.elementIterator("backend-to-backend");
				List<AsyncClientConfig> asyncClientConfigs = new ArrayList<AsyncClientConfig>();
				
				while (backendToBackendIt != null && backendToBackendIt.hasNext()) {
					Element itemEle =  backendToBackendIt.next();
					String remoteName = itemEle.attributeValue("connect-server-id");
					if (StringUtils.isEmpty(remoteName)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connect-server-id attribute is empty!") ;
					}
					
					String remoteIP = itemEle.attributeValue("remoteIP");
					
					if (StringUtils.isEmpty(remoteIP)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remoteIP attribute is empty!") ;
					}
					
					String remotePortStr = itemEle.attributeValue("remotePort") ;
					
					if (StringUtils.isEmpty(remotePortStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the remotePort attribute is empty!") ;
					}
					
					int remotePort = Integer.valueOf(remotePortStr);
					
					String connectsStr = itemEle.attributeValue("connects");
					
					if (StringUtils.isEmpty(connectsStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connects attribute is empty!") ;
					}
					
					int connects = Integer.valueOf(connectsStr);
					
					String connectTimeoutStr = itemEle.attributeValue("connectTimout");
					
					if (StringUtils.isEmpty(connectTimeoutStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the connectTimout attribute is empty!") ;
					}
					
					int connectTimeout = Integer.valueOf(connectTimeoutStr);
					
					String reconnectStr = itemEle.attributeValue("reconnect");
					
					if (StringUtils.isEmpty(reconnectStr)) {
						throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : the reconnect attribute is empty!") ;
					}
					
					String sendReconectStr = itemEle.attributeValue("sendReconnect");
					boolean sendReconnect = false;
					if (!StringUtils.isEmpty(sendReconectStr)) {
						sendReconnect = Boolean.parseBoolean(sendReconectStr);
					}
					
					heartbeatStr = itemEle.attributeValue("heartbeat");
					if (!StringUtils.isEmpty(heartbeatStr)) {
						heartbeat = Integer.valueOf(heartbeatStr);
					}
					
					heartbeatTimeoutStr = itemEle.attributeValue("heartbeatTimeout");
					if (!StringUtils.isEmpty(heartbeatTimeoutStr)) {
						heartbeatTimeout = Integer.valueOf(heartbeatTimeoutStr);
					}
					
					asyncClientConfig = new AsyncClientConfig(remoteName,remoteIP,remotePort,connectTimeout,connects);
					asyncClientConfig.setReconnect(reconnectStr);
					asyncClientConfig.setSendReconnect(sendReconnect);
					asyncClientConfig.setHeartbeat(heartbeat);
					asyncClientConfigs.add(asyncClientConfig);
				}
				application.serverConfig.setAsyncClientConfigs(asyncClientConfigs.size() > 0 ? asyncClientConfigs : null);
			}
			
			Element actionScanEle = serverElement.element("action-scan");
			if (actionScanEle != null) {
				String actionScanPackage = actionScanEle.attributeValue("base-packages");
				String[] packageArray;
				if (StringUtils.isEmpty(actionScanPackage)) {
					throw new IllegalStateException("Failed to load from " + CONF_NAME_STRING + ",cause : No found base-packages attribute in action-scan element") ;
				}
				
				if (actionScanPackage.indexOf(";") != -1) {
					packageArray = actionScanPackage.split(";");
				}else {
					packageArray = new String[1];
					packageArray[0] = actionScanPackage;
				}
				
				application.serverConfig.setComponetPackages(packageArray);
			}
		}
	}
	
	private AppHelper() {}
}

