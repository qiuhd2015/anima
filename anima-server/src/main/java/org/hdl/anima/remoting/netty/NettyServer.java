package org.hdl.anima.remoting.netty;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hdl.anima.AppConf;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.dispatcher.ChannelHandlers;
import org.hdl.anima.remoting.support.AbstractServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * NettyServer
 * @author qiuhd
 * @since  2014-8-1
 * @version V1.0.0
 */
public class NettyServer extends AbstractServer {
	
	private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

	private ServerBootstrap   bootstrap;
	
	private Map<String, Channel> channels ;
	
	private org.jboss.netty.channel.Channel channel;
	
	public NettyServer(AppConf conf, ChannelHandler handler, Codec codec) throws RemotingException {
		super(conf, wrapChannelHandler(conf,handler),codec);
	}
	
	protected static ChannelHandler wrapChannelHandler(AppConf conf, ChannelHandler handler){
		String serverId = conf.get(Constants.SERVER_ID,"");
		String threadPoolName = SERVER_THREAD_POOL_NAME;
		if (!StringUtils.isEmpty(serverId)) {
			threadPoolName = SERVER_THREAD_POOL_NAME + "[" + serverId+"]";
		}
		conf.set(Constants.THREAD_NAME_KEY, threadPoolName);
		return ChannelHandlers.wrap(handler, conf);
	}
	
	@Override
	public void doOpen() throws Throwable {
	    ExecutorService boss = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerBoss", false));
        ExecutorService worker = Executors.newCachedThreadPool(new NamedThreadFactory("NettyServerWorker", true));
        int ioThread = conf.getInt(Constants.IO_THREADS,Constants.DEFAULT_IO_THREADS);
        ChannelFactory channelFactory = new NioServerSocketChannelFactory(boss, worker, ioThread);
        bootstrap = new ServerBootstrap(channelFactory);
        
        final NettyHandler nettyHandler = new NettyHandler(getConf(), this);
        channels = nettyHandler.getChannels();
        bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
            public ChannelPipeline getPipeline() {
                NettyCodecAdapter adapter = new NettyCodecAdapter(conf,getCodec(), NettyServer.this);
                ChannelPipeline pipeline = Channels.pipeline();
                pipeline.addLast("decoder", adapter.getDecoder());
                pipeline.addLast("encoder", adapter.getEncoder());
                pipeline.addLast("handler", nettyHandler);
                return pipeline;
            }
        });
        // bind
        channel = bootstrap.bind(getBindAddress());
	}

	@Override
	public void doClose() throws Throwable {
		try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            Collection<Channel> channels = getChannels();
            if (channels != null && channels.size() > 0) {
                for (Channel channel : channels) {
                    try {
                        channel.close();
                    } catch (Throwable e) {
                        logger.warn(e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) { 
                // release external resource.
                bootstrap.releaseExternalResources();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (channels != null) {
                channels.clear();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
	}

	@Override
	public boolean isBound() {
		return channel.isBound();
	}

	@Override
	public Collection<Channel> getChannels() {
		return this.channels.values() ;
//		Collection<Channel> chs = new HashSet<Channel>();
//		for (Channel channel : this.channels.values()) {
//			if (channel.isConnected()) {
//				chs.add(channel);
//			} else {
//				InetSocketAddress address = (InetSocketAddress) channel
//						.getRemoteAddress();
//				String key = address.getAddress().getHostAddress() + ":"
//						+ address.getPort();
//				channels.remove(key);
//			}
//		}
//		return chs;
	}
}

