package org.hdl.anima.remoting;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Code
 * @author qiuhd
 */
public interface Codec {
	
	/**
	 * Need more input poison.
	 * 
	 * @see #decode(Channel, InputStream)
	 */
	Object NEED_MORE_INPUT = new Object();
	/**
	 * Decode message 
	 * @param is			输入流
	 * @param channel   	当前通道
	 * @return 				两类结果：1.网络对象 ,2.解码失败原因
	 * @throws IOException
	 */
	Object decode(Channel channel ,InputStream is) throws IOException ;
	/**
	 * Encode message 
	 * @param os
	 * @param channle
	 * @throws IOException
	 */
	void encode(Channel channle,OutputStream os ,Object message) throws IOException;
}
