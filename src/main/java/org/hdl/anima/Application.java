package org.hdl.anima;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TimerTask;
import java.util.concurrent.Future;

import org.hdl.anima.backend.BackendServer;
import org.hdl.anima.blacklist.BlackListMgr;
import org.hdl.anima.channel.ChannelService;
import org.hdl.anima.client.AsyncClientMgr;
import org.hdl.anima.common.TaskEngine;
import org.hdl.anima.common.module.Module;
import org.hdl.anima.fronend.FrontendServer;
import org.hdl.anima.handler.RequestDispatcher;
import org.hdl.anima.handler.RequestMappingMethodHandler;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.route.RouteTable;
import org.hdl.anima.route.Router;
import org.hdl.anima.session.BackendSessionMgr;
import org.hdl.anima.session.ClientSessionMgr;
import org.hdl.anima.session.LocalSessionMgr;
import org.hdl.anima.surrogate.ServerSurrogateMgr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class Application extends AbstractApplication{
	
	private final static Logger LOGGER = LoggerFactory.getLogger(Application.class);
	private AppConf conf = new AppConf();
	private final Version version = new Version(0, 0, 1);
	private final ClassLoader classLoader ;
	protected ServerConfig serverConfig;
	private TaskEngine taskEngine  = TaskEngine.getInstance();
    private Map<Class<?>, Module> modules = new LinkedHashMap<Class<?>, Module>();
    private volatile boolean started = false;
    
	public static Application create() {
		return new Application() ;
	}
	
	public Application() {
		this.classLoader = Thread.currentThread().getContextClassLoader();
		try {
			init();
		}catch(Throwable e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	private void initAppConfig() {
		conf.set(Constants.BIND_HOST, serverConfig.getHost());
		conf.setInt(Constants.BIND_PORT, serverConfig.getPort());
		conf.set(Constants.SERVER_ID,serverConfig.getServerId());
		conf.setInt(Constants.THREADS_KEY, serverConfig.getThreads());
		conf.setInt(Constants.MAX_CLIENTS, serverConfig.getMaxConnects());
		if (serverConfig.getHeartbeat() > 0) {
			conf.setInt(Constants.HEARTBEAT_KEY, serverConfig.getHeartbeat());
		}
		if (serverConfig.getHeartbeatTimeout() > 0) {
			conf.setInt(Constants.HEARTBEAT_TIMEOUT_KEY, serverConfig.getHeartbeatTimeout());
		}
		if (serverConfig.getReconnectionSecond() > 0) {
			conf.setInt(Constants.RECONNECTION_SECOND_KEY, serverConfig.getReconnectionSecond());
		}
	}
	
	private void init() throws Exception {
		AppHelper.loadFromStaticXml(this);
		initAppConfig();
	}
	
	/**
	 * Load all modules
	 */
	private void loadModules() {
		if (serverConfig.isFrontend()) {
			loadModule(RequestMappingMethodHandler.class.getName());
			loadModule(RequestDispatcher.class.getName());
			loadModule(BlackListMgr.class.getName());
			loadModule(ServerSurrogateMgr.class.getName());
			loadModule(Router.class.getName());
			loadModule(RouteTable.class.getName());
			loadModule(ClientSessionMgr.class.getName());
			loadModule(FrontendServer.class.getName());
		} else {
			loadModule(RequestMappingMethodHandler.class.getName());
			loadModule(AsyncClientMgr.class.getName());
			loadModule(RequestDispatcher.class.getName());
			loadModule(LocalSessionMgr.class.getName());
			loadModule(BackendSessionMgr.class.getName());
			loadModule(ChannelService.class.getName());
			loadModule(BackendServer.class.getName());
		}
	}
	
	/**
     * Loads a module.
     *
     */
    private void loadModule(String module) {
        try {
            Class<?> modClass = classLoader.loadClass(module);
            Constructor<?> constructor = modClass.getConstructor(String.class);
            Module mod = (Module) constructor.newInstance(module);
            this.modules.put(modClass, mod);
        }
        catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Failed to load module,module name {}.cause :{}",module,e.getMessage(),e);
        }
    }
    
	private void initMoudles() {
		for (Module module : modules.values()) {
			try {
				module.initialize(this);
			} catch (Exception e) {
				e.printStackTrace();
	            LOGGER.error("Failed to init module,module name {},cause :{}",module,e.getMessage(),e);
			}
		}
	}

	private void startModules() throws Exception {
		for (Module module : modules.values()) {
			try {
				module.start();
			} catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("Failed to start module,module name {}.cause :{}",module,e.getMessage(),e);
				throw e;
			}
		}
	}
	
	public synchronized void start() {
		try {
			if (started) {
				throw new IllegalStateException("Failed to start server,Cause server is already  started!");
			}
			loadModules();
			initMoudles();
			startModules();
			started = true;
			LOGGER.info(
					"Started server,Server info :id {},type {},host {},port {}",
					serverConfig.getServerId(), serverConfig.getServerType(),
					serverConfig.getHost(), serverConfig.getPort());
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	public synchronized void stop() {
		if (started) {
			for (Module module : modules.values()) {
				try {
					module.stop();
				} catch (Exception e) {
					e.printStackTrace();
		            LOGGER.error("Failed to stop module,module name {}.cause :{}",module,e.getMessage(),e);
				}
			}
			started = false;
		}
	}
	
	public synchronized void destory() {
		stop();
		for (Module module : modules.values()) {
			try {
				module.destroy();
			} catch (Exception e) {
				e.printStackTrace();
	            LOGGER.error("Failed to destory module,module name {}.cause :{}",module,e.getMessage(),e);
			}
		}
		taskEngine.shutdown();
	}

	@Override
	public Future<?> submit(Runnable task) {
		return taskEngine.submit(task);
	}

	@Override
	public void schedule(TimerTask task, long delay) {
		taskEngine.schedule(task, delay);
	}

	@Override
	public void schedule(TimerTask task, Date time) {
		taskEngine.schedule(task, time);
	}

	@Override
	public void schedule(TimerTask task, long delay, long period) {
		taskEngine.schedule(task, delay, period);
	}

	@Override
	public void schedule(TimerTask task, Date firstTime, long period) {
		taskEngine.schedule(task, firstTime, period);
	}

	@Override
	public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
		taskEngine.schedule(task, delay,period);
	}

	@Override
	public void scheduleAtFixedRate(TimerTask task, Date firstTime, long period) {
		taskEngine.scheduleAtFixedRate(task, firstTime, period);
	}

	@Override
	public void cancelScheduledTask(TimerTask task) {
		taskEngine.cancelScheduledTask(task);
	}

	@Override
	public AppConf getAppConf() {
		return conf;
	}

	@Override
	public ServerConfig getServerConifg() {
		return this.serverConfig;
	}

	@Override
	public String getServerId() {
		return serverConfig.getServerId();
	}
	
	@Override
	public String getServerType() {
		return serverConfig.getServerType();
	}

	@Override
	public Version getVersion() {
		return version;
	}

	@Override
	public boolean isFrontend() {
		return serverConfig.isFrontend();
	}
	
	@Override
	public boolean isBackend() {
		return !isFrontend();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T getMoulde(Class<T> clazz) {
		return (T) modules.get(clazz);
	}

	@Override
	public ClassLoader getClassLoader() {
		return classLoader;
	}
}

