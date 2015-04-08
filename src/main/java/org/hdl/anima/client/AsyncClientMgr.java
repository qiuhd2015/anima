package org.hdl.anima.client;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.hdl.anima.AppConf;
import org.hdl.anima.AppConstants;
import org.hdl.anima.Application;
import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.common.io.Record;
import org.hdl.anima.common.module.BasicModule;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.remoting.Constants;
import org.jboss.netty.util.internal.ConcurrentHashMap;
/**
 * Async client Manager
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public class AsyncClientMgr extends BasicModule {

	private List<AsyncClientConfig> asyncClientConfigs ;
	private Map<String, AsyncClient> asyncClientMap;
	private Map<String/*servetType*/,List<AsyncClient>> asyncClientsByType;
	private static AtomicInteger sequenceIdCounter = new AtomicInteger(0);
	private AsyncClientResponseArgMapping responseArgMapping ;
	private Map<Integer, AsyncMethodCall<?>> asyncMethodCalls;
	
	private final AsyncClient.OnReponseHandler responseHandler = new AsyncClient.OnReponseHandler() {
		@Override
		public void OnCompleted(String serverId, Response response) {
			int sequence = response.getSequence();
			AsyncMethodCall<?> call = asyncMethodCalls.get(sequence);
			if (call == null) {
				throw new IllegalStateException("Failed to handle async callback");
			}
			asyncMethodCalls.remove(sequence);
			call.callback(serverId, response);
		}
	};
	
	public AsyncClientMgr(String moduleName) {
		super(moduleName);
	}
	
	@Override
	public void initialize(Application application) {
		super.initialize(application);
		asyncClientConfigs = application.getServerConifg().getAsyncClientConfigs();
		if (asyncClientConfigs != null) {
			responseArgMapping = AsyncClientResponseArgMapping.getInstance();
			asyncMethodCalls = new ConcurrentHashMap<Integer, AsyncClientMgr.AsyncMethodCall<?>>();
			AsyncClientHelper.setAsyncCientMgr(this);
		}
	}
	
	@Override
	public void start() throws IllegalStateException {
		if (asyncClientConfigs != null && asyncClientConfigs.size() > 0) {
			asyncClientMap = new HashMap<String, AsyncClient>(asyncClientConfigs.size());
			asyncClientsByType = new HashMap<String, List<AsyncClient>>();
			for (int i = 0;i < asyncClientConfigs.size();i++) {
				AsyncClientConfig config = asyncClientConfigs.get(i);
				if (asyncClientMap.containsKey(config.getServerId())) {
					throw new IllegalStateException("Failed to start backend to server module,cause : include the same of server id " + config.getServerId());
				}
				
				AsyncClient asyncClient = new AsyncClient(application, createAppConf(config));
				asyncClient.setResponseHandler(this.responseHandler);
				asyncClientMap.put(config.getServerId(), asyncClient);
				String serverType = config.getServerType();
				List<AsyncClient> asyncClientList = asyncClientsByType.get(serverType);
				if (asyncClientList == null) {
					asyncClientList = new LinkedList<AsyncClient>();
					asyncClientsByType.put(serverType, asyncClientList);
				}
				asyncClientList.add(asyncClient);
			}
		}
	}
	
	/**
	 * Create application configures
	 * @param config
	 * @return
	 */
	private AppConf createAppConf(AsyncClientConfig config) {
		AppConf appConf = new AppConf();
		appConf.set(AppConstants.SERVER_ID_KEY,config.getServerId());
		appConf.set(AppConstants.RECONNECT_PERIOD_KEY, config.getReconnect());
		appConf.setBoolean(AppConstants.SEND_RECONNECT_KEY, config.isSendReconnect());
		appConf.set(AppConstants.REMOTE_IP_KEY,config.getRemoteHost());
		appConf.setInt(AppConstants.REMOTE_PORT_KEY, config.getRemotePort());
		appConf.setInt(AppConstants.CONNECTS_KEY, config.getConnects());
		appConf.setInt(AppConstants.CONNECT_TIMEOUT_KEY, config.getConnectTimeout());
		appConf.setInt(AppConstants.HEARTBEAT_KEY,config.getHeartbeat());
		appConf.setInt(Constants.THREADS_KEY, 50);
		return appConf;
	}
	
	@SuppressWarnings("unchecked")
	public <T> int request(String serverId,int msgId,Record requestArg,AsyncMethodCallback<T> callback) {
		if (StringUtils.isEmpty(serverId)) {
			throw new NullPointerException("ServerId can not be empty!");
		}
		
		if (!asyncClientMap.containsKey(serverId)) {
			throw new IllegalArgumentException("ServerId : " + serverId + " unfound ") ;
		}
		
		if (msgId <= 0) {
			throw new IllegalArgumentException("Request error,Cause : message id less than zero");
		}
		
//		if (requestArg == null) {
//			throw new NullPointerException("requestArgs can not empty!");
//		}
		
		if (callback == null) {
			throw new IllegalArgumentException("callback can not be null!");
		}
		
		Class<T> clazz = null;
		
		if (!responseArgMapping.contains(msgId)) {
			
			String msgIdStr = String.valueOf(msgId).intern();
			
			synchronized (msgIdStr) {
				if (!responseArgMapping.contains(msgId)) {
					Type[] types =  callback.getClass().getGenericInterfaces();
					Type type = types[0];
					if (type instanceof ParameterizedType) {
						ParameterizedType t = (ParameterizedType) type ;
						Type[] typeArray = t.getActualTypeArguments();
						if (typeArray == null || typeArray.length == 0) {
							throw new NullPointerException("AsyncMethodCallback parameter can not be empty!");
						}
						clazz = (Class<T>) typeArray[0];
						responseArgMapping.addResponseArg(msgId, (Class<? extends Decodeable>) clazz);
					}
				}
			}
		}
		
		int sequence =  sequenceIdCounter.incrementAndGet();
		Request request = new Request(msgId);
		request.setSequence(sequence);
		request.setContent(requestArg);
		request.setTwoWay(true);
		request.setSid(-1);		//表示服务器之间消息
		AsyncClient client = asyncClientMap.get(serverId);
		AsyncMethodCall<T> methodCall = new AsyncMethodCall<T>(client, request, callback);
		methodCall.call();
		asyncMethodCalls.put(sequence, methodCall);
		return sequence;
	}
	
	/**
	 * 通知指定服务器
	 * @param serverId
	 * @param msgId
	 * @param requestArg
	 */
	public void notify(String serverId,int msgId,Record requestArg) {
		if (StringUtils.isEmpty(serverId)) {
			throw new NullPointerException("serverId can not empty!");
		}
		
		if (!asyncClientMap.containsKey(serverId)) {
			throw new IllegalArgumentException("") ;
		}
		
		if (msgId <= 0) {
			throw new IllegalArgumentException("Request error,Cause : message id less than zero");
		}
		
		if (requestArg == null) {
			throw new NullPointerException("requestArgs can not empty!");
		}
		
		int sequence =  sequenceIdCounter.incrementAndGet();
		Request request = new Request(msgId);
		request.setSequence(sequence);
		request.setContent(requestArg);
		request.setTwoWay(false);
		request.setSid(-1);		//表示后台服务器
		AsyncClient client = asyncClientMap.get(serverId);
		client.send(request);
	}
	
	/**
	 * 通知指定类型服务器
	 * @param serverId
	 * @param msgId
	 * @param requestArg
	 */
	public void notifyForType(String serverType,int msgId,Record requestArg) {
		if (StringUtils.isEmpty(serverType)) {
			throw new NullPointerException("serverType can not be empty!");
		}
		
		if (!asyncClientsByType.containsKey(serverType)) {
			throw new IllegalArgumentException("ServerType : " + serverType + " unfound") ;
		}
		
		if (msgId <= 0) {
			throw new IllegalArgumentException("Request error,Cause : message id less than zero");
		}
		
//		if (requestArg == null) {
//			throw new NullPointerException("requestArgs can not empty!");
//		}
		
		int sequence =  sequenceIdCounter.incrementAndGet();
		Request request = new Request(msgId);
		request.setSequence(sequence);
		request.setContent(requestArg);
		request.setTwoWay(false);
		request.setSid(-1);		//表示后台服务器
		
		List<AsyncClient> asyncClients = this.asyncClientsByType.get(serverType);
		for (AsyncClient client : asyncClients) {
			client.send(request);
		}
	}
	
	/**
	 * 
	 * Async Method call
	 * @author qiuhd
	 *
	 */
	private final class AsyncMethodCall<T> {
		
		private final Request request ;
		
		private final AsyncMethodCallback<T> methodCallback;
		
//		private long startTime;
		
		private final AsyncClient asyncClient;
		
		public AsyncMethodCall(AsyncClient asyncClient,Request request,AsyncMethodCallback<T> methodCallback) {
//			if (asyncClient == null) {
//				throw new NullPointerException("asyncClient can not empty!") ;
//			}
//			
//			if (request == null) {
//				throw new NullPointerException("request can not be empty!");
//			}
//			
//			if (methodCallback == null) {
//				throw new NullPointerException("methodCallback can not be empty!");
//			}
			this.asyncClient = asyncClient;
			this.request = request;
			this.methodCallback = methodCallback;
		}
		
		public void call() {
			asyncClient.send(request);
//			startTime = System.currentTimeMillis();
		}
		
		@SuppressWarnings("unchecked")
		public void callback(String serverId,Response response) {
			if (response.isOK()) {
				Object content = response.getContent();
				methodCallback.onCompleted((T)content);
			}else {
				methodCallback.onError(new AsyncMethodCallbackException(response.getErrorCode(),response.getErrorDes()));
			}
		}

//		private long getStartTime() {
//			return startTime;
//		}
	}

	@Override
	public void destroy() {
		super.destroy();
		asyncClientConfigs = null;
		if (asyncClientMap != null) {
			asyncClientMap.clear();
			asyncClientMap = null;
		}
		if (this.asyncClientsByType != null) {
			asyncClientMap.clear();
			asyncClientMap = null;
		}
		
		if (responseArgMapping != null) {
			responseArgMapping.destroy();
		}
		
		if (this.asyncMethodCalls != null) {
			this.asyncMethodCalls.clear();
			this.asyncMethodCalls = null;
		}
	}
	
}
