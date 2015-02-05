package org.hdl.anima.remoting;
/**
 * Constants
 * @author qiuhd
 *
 */
public interface Constants {
	
	public static final String 		SERVER_ID 						= "server.id";
	
	public static final String 		CONN_TIMEOUT					= "connect.timeout" ;
	
	public static final String 		CHANNEL_IDLE_TIMEOUT			= "channel.idle.timeout" ;
	
	public static final int 		DEFAULT_CHANNEL_IDLE_TIMEOUT	= Integer.MAX_VALUE;
	
	public static final String 		SUPPORT_MAX_CONNECT				= "connect.max" ;
	
	public static final String 		MAX_CLIENTS 					= "max.clients" ;
	
	public static final int 		DEFAULT_MAX_CLIENTS				= 10000;
	
	public static final String 		BIND_HOST						= "bind.host" ;

	public static final String 		DEFAULT_BIND_HOST				= "192.168.1.103" ;
	
	public static final String 		BIND_PORT						= "bind.port" ;
	
	public static final int 		DEFAULT_BIND_PORT				= 8601 ;
	
	public static final String 		IO_THREADS 						= "io.thread" ;
	
	public static final int 	    DEFAULT_IO_THREADS				= Runtime.getRuntime().availableProcessors() + 1;
	
	public static final int 	    DEFAULT_PROXY_IO_THREADS          =  1;
	
	public static final String 		SOCKET_RECEIVE_BUFFER			= "socket.receive.buffer" ;
	
	public static final int 		DEFAULT_RECEIVE_BUFFER 			= 1024 * 8 ;   //8k
	
	public static final String 		SOCKET_SEND_BUFFER				= "sokcet.send.buffer" ;
		
	public static final int 	    DEFAULT_SOCKET_SEND_BUFFER		= 1024 * 8 ;   //8k	
	
	public static final String 		THREAD_NAME_KEY 				= "threadName" ;
	
	public static final String 		DEFAULT_THREAD_NAME 			= "Game";
	
	public static final String 		CORE_THREADS_KEY				= "corethreads" ;
	
	public static final int 	    DEFAULT_CORE_THREADS		    = 0;   
	
	public static final String      QUEUES_KEY                      = "queues";
	
	public static final int 	    DEFAULT_QUEUES		   			= 200;
	
	public static final String      ALIVE_KEY                       = "alive";
	
	public static final int 	    DEFAULT_ALIVE		   			= 1000 * 60;   	
	
	public static final String 		THREADS_KEY 					= "threads" ;
	
	public static final int         DEFAULT_THREADS                  = 200;
	
	public static final String 		THREAD_QUEUE_KEY 				= "threadqueue" ;
	
	public static final int     	DEFAULT_THREAD_DUEUE    		= 60 * 1000;
	
	public static final String 		THREADPOOL_KEY 			        = "threadpool";
	
	public static final String 		DEFAULT_THREADPOOL				= "fixed";
	
	public static final String 		CACHED_THREADPOOL_KEY			= "cached";
	
	public static final String 		FIXED_THREADPOOL_KEY			= "fixed";
	
	public static final String 		LIMITED_THREADPOOL_KEY			= "limited";
	
	public static final String      PAYLOAD_KEY                     = "payload";
	
	public static final int 		DEFAULT_PAYLOAD					= 8 * 1024 * 1024;
	
	public static final String    	DEFAULT_CLIENT_THREADPOOL       = "cached";
	
	public static final String 		HEARTBEAT_KEY					= "heartbeat";
	
	public static final int 		DEFAULT_HEARTBEAT				= 1000 * 30;
	
	public static final String 		HEARTBEAT_TIMEOUT_KEY			= "heartbeat.timeout";
	
	public static final String 		RECONNECTION_SECOND_KEY			= "reconnection.time";
	
	public static final String 		SEND_RECONNECT_KEY			    = "send.reconnect" ;
	
	public static final String 		RECONNECT_PERIOD_KEY			= "reconnect.period.time";
	
	public static final int 		DEFAULT_RECONNECT_PERIOD		= 2000;
	
	public static final String 		CHECK_KEY						= "check";
	
}
