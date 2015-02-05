package org.hdl.anima.surrogate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hdl.anima.AppConf;
import org.hdl.anima.AppConstants;
import org.hdl.anima.Application;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.remoting.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ServerSurrogateMgr
 * @author qiuhd
 * @since  2014年9月17日
 * @version V1.0.0
 */
public class ServerSurrogateMgr extends BasicModule{
	
	private static final Logger logger = LoggerFactory.getLogger(ServerSurrogateMgr.class);
	private List<ServerSurrogateConfig> surrogateConfigs;
	private Map<String,ServerSurrogate> surrogates;
	
	public ServerSurrogateMgr(String moduleName) {
		super(moduleName);
	}

	@Override
	public void initialize(Application application) {
		super.initialize(application);
		surrogateConfigs = application.getServerConifg().getSurrogateConfigs();
		surrogates = new HashMap<String, ServerSurrogate>(surrogateConfigs.size());
	}

	@Override
	public void start() throws IllegalStateException {
		for (int i = 0;i < surrogateConfigs.size();i++) {
			ServerSurrogateConfig config = surrogateConfigs.get(i);
			if (surrogates.containsKey(config.getServerId())) {
				throw new IllegalStateException("Failed to start server surrogate module,cause : include the same of server surrogate name " + config.getServerId());
			}
			ServerSurrogate serverSurrogate = new ServerSurrogate(application, createAppConf(config));
			surrogates.put(config.getServerId(), serverSurrogate);
		}
	}

	/**
	 * Return server surrogate by the remote server name
	 * @param name
	 * @return
	 */
	public ServerSurrogate getSurrogate(String servername) {
		return surrogates.get(servername);
	}
	
	/**
	 * Send message to background server
	 * @param servername
	 * @param message
	 */
	public void send(String servername,AbstractMessage message) {
		ServerSurrogate surrogate = getSurrogate(servername);
		if (surrogate != null) {
			surrogate.send(message);
		}else {
			logger.error("Failed to send message to remote server,message id :" + message.getId(),new IllegalStateException("Cause:Server surrogate name " + servername + " no exsits!"));
		}
	}
	
	/**
	 * Send message to background server
	 * @param servername
	 * @param message
	 */
	public void send(List<String> servernames,AbstractMessage message) {
		for (String servername : servernames) {
			send(servername,message);
		}
	}
	
	/**
	 * Notify all background server the client session already is created 
	 * @param sid	session identity
	 * @param remoteIP
	 * @param remotePort
	 * @param localIP
	 * @param localPort
	 * @param clientType
	 */
	public void clientSessionCreated(int sid,String remoteIP,int remotePort,String localIP,int localPort,String clientType) {
		for (ServerSurrogate surrogate : surrogates.values()) {
			surrogate.clientSessionCreated(sid, remoteIP, remotePort, localIP, localPort, clientType);
		}
	}
	
	/**
	 *  Notify all background server that the client session already is closed
	 * @param identity	
	 */
	public void clientSessionClosed(int sid) {
		for (ServerSurrogate surrogate : surrogates.values()) {
			surrogate.clientSessionClosed(sid);
		}
	}
	
	/**
	 * session 冻结 ，通知后台所有服务器
	 * @param sid
	 */
	public void clientSessionFreeze(int sid) {
		for (ServerSurrogate surrogate : surrogates.values()) {
			surrogate.clientSessionFreezed(sid);
		}
	}
	
	/**
	 * session 冻结已解除， 通知后台所有服务器
	 * @param sid
	 */
	public void clientSessionUFreeze(int sid) {
		for (ServerSurrogate surrogate : surrogates.values()) {
			surrogate.clientSessionUnFreezed(sid);
		}
	}
	
	/**
	 * Create application configure
	 * @param config
	 * @return
	 */
	private AppConf createAppConf(ServerSurrogateConfig config) {
		AppConf appConf = new AppConf();
		appConf.set(AppConstants.SERVER_ID_KEY,config.getServerId());
		appConf.set(AppConstants.RECONNECT_PERIOD_KEY, config.getReconnect());
		appConf.setBoolean(AppConstants.SEND_RECONNECT_KEY, config.isSendReconnect());
		appConf.set(AppConstants.REMOTE_IP_KEY,config.getRemoteHost());
		appConf.setInt(AppConstants.REMOTE_PORT_KEY, config.getRemotePort());
		appConf.setInt(AppConstants.CONNECTS_KEY, config.getConnects());
		appConf.setInt(AppConstants.CONNECT_TIMEOUT_KEY, config.getConnectTimeout());
		appConf.setInt(AppConstants.HEARTBEAT_KEY,config.getHeartbeat());
		appConf.setInt(AppConstants.HEARTBEAT_TIMEOUT_KEY, config.getHeartbeatTimeout());
		appConf.setInt(Constants.THREADS_KEY, 50);
		return appConf;
	}

	@Override
	public void destroy() {
		if (surrogates != null) {
			for (ServerSurrogate surrogate : surrogates.values()) {
				surrogate.destroy();
				surrogate = null;
			}
			surrogates.clear();
			surrogates = null;
		}
		
		if (surrogateConfigs != null) {
			surrogateConfigs.clear();
			surrogateConfigs = null;
		}
	}
}
