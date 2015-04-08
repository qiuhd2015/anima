package org.hdl.anima.remoting.support;

import java.util.Collection;

import org.hdl.anima.protocol.HeartBeat;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.Client;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * HeartBeat Task
 * @author qiuhd
 * @since  2014年9月2日
 */
public class HeartBeatTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(HeartBeatTask.class);
	private ChannelProvider channelProvider ;
	private int heartbeat;
	private int heartbeatTimeout;
    public static String KEY_READ_TIMESTAMP = "READ_TIMESTAMP";
    public static String KEY_WRITE_TIMESTAMP = "WRITE_TIMESTAMP";
	
    public HeartBeatTask( ChannelProvider provider, int heartbeat, int heartbeatTimeout ) {
        this.channelProvider = provider;
        this.heartbeat = heartbeat;
        this.heartbeatTimeout = heartbeatTimeout;
    }
	
	@Override
	public void run() {
		try {
            long now = System.currentTimeMillis();
            for (Channel channel : channelProvider.getChannels()) {
                if (channel == null || channel.isClosed()) {
                    continue;
                }
                try {
                    Long lastRead = ( Long ) channel.getAttribute(KEY_READ_TIMESTAMP);
                    Long lastWrite = ( Long ) channel.getAttribute(KEY_WRITE_TIMESTAMP);
                    if ((lastRead != null && now - lastRead > heartbeat )
                            || ( lastWrite != null && now - lastWrite > heartbeat )) {
                    	HeartBeat req = new HeartBeat();
                        req.setTwoWay(true);
                        channel.send(req);
                        logger.debug( "Send heartbeat to remote channel " + channel.getRemoteAddress()
                                                  + ", cause: The channel has no data-transmission exceeds a heartbeat period: " + heartbeat + "ms" );
                    }
                    if (lastRead != null && now - lastRead > heartbeatTimeout) {
						logger.warn("Close channel " + channel+ ", because heartbeat read idle time out: "+ heartbeatTimeout + "ms");
						if (channelProvider.isClientSide()) {
							try {
								((Client) channel).reconnect();
							} catch (Exception e) {
								// do nothing
							}
						} else {
							channel.send(new Kick("heatbeat timeout"));
							channel.close();
						}
                    }
                } catch ( Throwable t ) {
                    logger.warn( "Exception when heartbeat to remote channel " + channel.getRemoteAddress(), t );
                }
            }
        } catch ( Throwable t ) {
            logger.warn( "Unhandled exception when heartbeat, cause: " + t.getMessage(), t );
        }
	}
	
	/**
	 * Channel provider
	 * @author qiuhd
	 *
	 */
	public interface ChannelProvider {
	     Collection<Channel> getChannels();
	     
	     boolean isClientSide();
	}
}

