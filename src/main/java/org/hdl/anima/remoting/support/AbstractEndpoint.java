package org.hdl.anima.remoting.support;

import static com.google.common.base.Preconditions.checkArgument;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Codec;
/**
 * AbstractEndpoint
 * @author qiuhd
 * @since  2014-7-24
 * @version V1.0.0
 */
public abstract class AbstractEndpoint extends AbstractPeer{
	
	protected Codec codec ;
	
	public AbstractEndpoint(AppConf conf,ChannelHandler handler,Codec codec) {
		super(conf, handler);
		checkArgument(codec != null,"codec == null");
		this.codec = new MultiMessageCodec(codec);
	}

	public Codec getCodec() {
		return codec;
	}
}

