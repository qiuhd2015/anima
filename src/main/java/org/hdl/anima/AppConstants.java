package org.hdl.anima;
/**
 * Application Constants
 * @author qiuhd
 * @since  2014年9月17日
 * @version V1.0.0
 */
public final class AppConstants {

	public static final String 	SERVER_ID_KEY = "server.id";
	
	public static final String 	REMOTE_IP_KEY = "server.remote.ip";
	
	public static final String 	REMOTE_PORT_KEY = "server.remote.port";
	
	public static final String 	CONNECTS_KEY = "connects";
	
	public static final String  CONNECT_TIMEOUT_KEY = "connect.timeout";
	
	public static final int 	DEFAULT_CONNECT_TIMEOUT= 3000;
		
	public static final String 	RECONNECT_PERIOD_KEY = "reconnect.period";
	
	public static final int    	DEFAULT_RECONNECT_PERIOD	= 2000;
	
	public static final String 	RECONNECTION_SECONDS_KEY = "reconnection.seconds";
	
	public static final int 	DEFAULT_RECONNECTION_SECONDS = 30 ;
	
	public static final String 	HEARTBEAT_KEY = "heartbeat";
	
	public static final int		DEFAULT_HEARTBEAT = 30000;
	
	public static final String 	HEARTBEAT_TIMEOUT_KEY = "heartbeat.timeout";
	
	public static final String 	SEND_RECONNECT_KEY	= "send.reconnect" ;
	
	private AppConstants() {};
}
