package org.hdl.anima.remoting;
/**
 * Channel handle delegate
 * @author qiuhd
 * @since  2014-7-24
 * @version V1.0.0
 */
public interface ChannelHandlerDelegate extends ChannelHandler{
	
	/**
	 * Return actual channel handler
	 * @return
	 */
	ChannelHandler getChannelHandler();
}

