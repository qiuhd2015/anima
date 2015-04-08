package org.hdl.anima.remoting.support;

import static com.google.common.base.Preconditions.checkArgument;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.hdl.anima.AppConf;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Client;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract client
 * @author qiuhd
 * @since  2014年8月12日
 */
public abstract class AbstractClient extends AbstractEndpoint implements Client {
	
	private static final Logger logger = LoggerFactory.getLogger(AbstractClient.class);
	private String remoteHost;
	private int remotePort;
	private int connectTimeout;
	
    private static final ScheduledThreadPoolExecutor reconnectExecutorService = new ScheduledThreadPoolExecutor(2, new NamedThreadFactory("server-reconnect-timer", true));
    private volatile ScheduledFuture<?> reconnectExecutorFuture = null;
    protected volatile ExecutorService executor;
    private final boolean send_reconnect ;
    private final AtomicInteger reconnect_count = new AtomicInteger(0);
    //重连的error日志是否已经被调用过.
    private final AtomicBoolean reconnect_error_log_flag = new AtomicBoolean(false) ;
    //重连warning的间隔.(waring多少次之后，warning一次) //for test
    private final int reconnect_warning_period ;
    
	private final Lock connectLock = new ReentrantLock();
	
	public AbstractClient(AppConf conf, ChannelHandler handler,Codec codec) {
		super(conf, handler,codec);
		this.send_reconnect = conf.getBoolean(Constants.SEND_RECONNECT_KEY, false);
		this.reconnect_warning_period = conf.getInt("reconnect.waring.period", 1800);
	}
	
	public AbstractClient(AppConf conf,ChannelHandler hannder,Codec codec,String remoteHost,int remotePort,int connectTimeout)throws RemotingException {
		this(conf, hannder,codec);
		checkArgument(remoteHost != null,"Remote host can not be null!");
		
		this.remoteHost = remoteHost;
		this.remotePort = remotePort; 
		this.connectTimeout = connectTimeout;
		
		try {
            doOpen();
        } catch (Throwable t) {
            close();
            throw new RemotingException(null, "Failed to start " + getClass().getSimpleName() + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
		
        try {
            // connect.
            connect();
            if (logger.isInfoEnabled()) {
                logger.info("Start " + getClass().getSimpleName() + " connect to the server " + getRemoteAddress());
            }
        } catch (RemotingException t) {
            if (conf.getBoolean(Constants.CHECK_KEY, false)) {
                close();
                throw t;
            } else {
                logger.error("Failed to start " + getClass().getSimpleName() + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage());
            }
        } catch (Throwable t){
            close();
            throw new RemotingException(null, null, 
                    "Failed to start " + getClass().getSimpleName() + " connect to the server " + getRemoteAddress() + ", cause: " + t.getMessage(), t);
        }
	}
	
	 /**
     * init reconnect thread
     */
    private synchronized void initConnectStatusCheckCommand(){
        //reconnect=false to close reconnect 
        int reconnect = getReconnectParam(getConf());
        if(reconnect > 0 && (reconnectExecutorFuture == null || reconnectExecutorFuture.isCancelled())){
            Runnable connectStatusCheckCommand =  new Runnable() {
                public void run() {
                    try {
                        if (!isConnected()) {
                            connect();
                        }
                    } catch (Throwable t) { 
                        String errorMsg = "Reconnect to "+ remoteHost + ":" + remotePort +" error!";
                        // wait registry sync provider list
//                        if (System.currentTimeMillis() - lastConnectedTime > shutdown_timeout){
//                            if (!reconnect_error_log_flag.get()){
//                                reconnect_error_log_flag.set(true);
//                                logger.error(errorMsg, t);
//                                return ;
//                            }
//                        }
                        if ( reconnect_count.getAndIncrement() % reconnect_warning_period == 0){
                            logger.warn(errorMsg, t);
                        }
                    }
                }
            };
            reconnectExecutorFuture = reconnectExecutorService.scheduleWithFixedDelay(connectStatusCheckCommand, reconnect, reconnect, TimeUnit.MILLISECONDS);
        }
    }
    
    private synchronized void destroyConnectStatusCheckCommand(){
        try {
            if (reconnectExecutorFuture != null && ! reconnectExecutorFuture.isDone()){
                reconnectExecutorFuture.cancel(true);
                reconnectExecutorService.purge();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }
    
    /**
     * @param url
     * @return 0-false
     */
    private static int getReconnectParam(AppConf conf){
        int reconnect ;
        String param = conf.get(Constants.RECONNECT_PERIOD_KEY);
        if (param == null || param.length()==0 || "true".equalsIgnoreCase(param)){
            reconnect = Constants.DEFAULT_RECONNECT_PERIOD;
        }else if ("false".equalsIgnoreCase(param)){
            reconnect = 0;
        } else {
            try{
                reconnect = Integer.parseInt(param);
            }catch (Exception e) {
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:"+param);
            }
            if(reconnect < 0){
                throw new IllegalArgumentException("reconnect param must be nonnegative integer or false/true. input is:"+param);
            }
        }
        return reconnect;
    }
	
	public void connect() throws RemotingException{
		  connectLock.lock();
	        try {
	            if (isConnected()) {
	                return;
	            }
	            initConnectStatusCheckCommand();
	            doConnect();
	            if (!isConnected()) {
	                throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName());
	            } else {
	            	if (logger.isInfoEnabled()){
	            		logger.info("Successed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName());
	            	}
	            }
	            reconnect_count.set(0);
	            reconnect_error_log_flag.set(false);
	        } catch (RemotingException e) {
	            throw e;
	        } catch (Throwable e) {
	            throw new RemotingException(this, "Failed connect to server " + getRemoteAddress() + " from " + getClass().getSimpleName(), e);
	        } finally {
	            connectLock.unlock();
	        }
	}
	
	public void disconnect() {
        connectLock.lock();
        try {
        	destroyConnectStatusCheckCommand();
            try {
                Channel channel = getChannel();
                if (channel != null) {
                    channel.close();
                }
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
            try {
                doDisConnect();
            } catch (Throwable e) {
                logger.warn(e.getMessage(), e);
            }
        } finally {
            connectLock.unlock();
        }
    }

	@Override
	public InetSocketAddress getLocalAddress() {
		Channel channel = getChannel();
		if (channel == null) {
			return null; 
		}
		return channel.getLocalAddress();
	}

	@Override
	public void send(Object message) throws RemotingException {
		if (send_reconnect && !isConnected()) {
			connect();
		}
		Channel channel = getChannel();
		if (channel == null || !channel.isConnected()) {
			throw new RemotingException(this,"message can not send, because channel is closed");
		}
		channel.send(message);
	}

	@Override
	public boolean isConnected() {
		Channel channel = getChannel();
		if (channel == null) {
			return false; 
		}
		return channel.isConnected();
	}
	
	@Override
	public void close() {
        try {
            super.close();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
        	disconnect();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            doClose();
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
	}

	@Override
	public void close(int timeout) {
//		ExecutorUtil.gracefulShutdown(executor ,timeout);
	    close();
	}

	@Override
	public Object getAttribute(String key) {
		Channel channel = getChannel();
		if (channel == null) {
			return null; 
		}
		
		return channel.getAttribute(key);
	}

	@Override
	public void setAttribute(String key, Object object) {
		Channel channel = getChannel();
		if (channel == null) {
			return; 
		}
		
	    channel.setAttribute(key, object);
	}

	@Override
	public boolean contains(String key) {
		Channel channel = getChannel();
		if (channel == null) {
			return false; 
		}
		
		return channel.contains(key);
	}

	@Override
	public void removeAttribute(String key) {
		Channel channel = getChannel();
		if (channel == null) {
			return ; 
		}
		
		channel.removeAttribute(key);
	}

	@Override
	public InetSocketAddress getRemoteAddress() {
		Channel channel = getChannel();
		if (channel == null) {
			return new InetSocketAddress(this.remoteHost,this.remotePort);
		}
		
		return channel.getRemoteAddress();
	}

	@Override
	public void reconnect() throws RemotingException {
		disconnect();
		connect();
	}
	
	public InetSocketAddress getConnectAddress() {
		return new InetSocketAddress(this.remoteHost,this.remotePort);
	}
	
	@Override
	public int getConnectTimeout() {
		return this.connectTimeout;
	}
	
	 protected int getTimeout() {
	     return connectTimeout;
	 }
	
	public abstract Channel getChannel() ;
	
	public abstract void doOpen() throws Throwable;
	
	public abstract void doConnect() throws Throwable;
	
	public abstract void doClose() throws Throwable;
	
	public abstract void  doDisConnect() throws Throwable;
}

