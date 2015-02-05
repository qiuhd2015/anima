package org.hdl.anima.remoting.netty;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.hdl.anima.AppConf;
import org.hdl.anima.common.NamedThreadFactory;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.remoting.ChannelHandler;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.Constants;
import org.hdl.anima.remoting.RemotingException;
import org.hdl.anima.remoting.dispatcher.ChannelHandlers;
import org.hdl.anima.remoting.support.AbstractClient;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author qiuhd
 * @since 2014年8月12日
 */
public class NettyClient extends AbstractClient {

	private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

	private static final ChannelFactory channelFactory = new NioClientSocketChannelFactory(
			         Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientBoss", true)),
			         Executors.newCachedThreadPool(new NamedThreadFactory("NettyClientWorker", true)), Constants.DEFAULT_IO_THREADS);

	private ClientBootstrap bootstrap;
	private Channel channel ;
	
    protected static final String CLIENT_THREAD_POOL_NAME  ="ClientHandler";

	public NettyClient(AppConf conf, ChannelHandler handler,Codec codec) {
		super(conf, wrapChannelHandler(conf,handler),codec);
	}

	public NettyClient(AppConf conf, ChannelHandler handler, Codec codec,String remoteHost,
			int remotePort, int connectTimeout) throws RemotingException {
		super(conf, wrapChannelHandler(conf,handler),codec,remoteHost, remotePort, connectTimeout);
	}
	
	protected static ChannelHandler wrapChannelHandler(AppConf conf, ChannelHandler handler){
		String serverId = conf.get(Constants.SERVER_ID,"");
		String threadPoolName = CLIENT_THREAD_POOL_NAME;
		if (!StringUtils.isEmpty(serverId)) {
			threadPoolName = CLIENT_THREAD_POOL_NAME + "[" + serverId +"]";
		}
		conf.set(Constants.THREAD_NAME_KEY, threadPoolName);
	    conf.set(Constants.THREADPOOL_KEY, Constants.DEFAULT_CLIENT_THREADPOOL);
	    return ChannelHandlers.wrap(handler, conf);
	}

	@Override
	public org.hdl.anima.remoting.Channel getChannel() {
		return NettyChannel.getOrAddChannel(conf, channel, handler);
	}

	@Override
	public void doOpen() throws Throwable {
		bootstrap = new ClientBootstrap(channelFactory);
		bootstrap.setOption("keepAlive", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("connectTimeoutMillis", getConnectTimeout());
		final NettyHandler nettyHandler = new NettyHandler(getConf(), this);
		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {
			public ChannelPipeline getPipeline() {
				NettyCodecAdapter adapter = new NettyCodecAdapter(getConf(),getCodec(), NettyClient.this);
				ChannelPipeline pipeline = Channels.pipeline();
				pipeline.addLast("decoder", adapter.getDecoder());
				pipeline.addLast("encoder", adapter.getEncoder());
				pipeline.addLast("handler", nettyHandler);
				return pipeline;
			}
		});
	}

	@Override
	public void doConnect() throws Throwable {
		long start = System.currentTimeMillis();
        ChannelFuture future = bootstrap.connect(getConnectAddress());
        try{
            boolean ret = future.awaitUninterruptibly(getConnectTimeout(), TimeUnit.MILLISECONDS);
            
            if (ret && future.isSuccess()) {
                Channel newChannel = future.getChannel();
                newChannel.setInterestOps(Channel.OP_READ_WRITE);
                try {

                    Channel oldChannel = NettyClient.this.channel; 
                    if (oldChannel != null) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close old netty channel " + oldChannel + " on create new netty channel " + newChannel);
                            }
                            oldChannel.close();
                        } finally {
                            NettyChannel.removeChannelIfDisconnected(oldChannel);
                        }
                    }
                } finally {
                    if (NettyClient.this.isClosed()) {
                        try {
                            if (logger.isInfoEnabled()) {
                                logger.info("Close new netty channel " + newChannel + ", because the client closed.");
                            }
                            newChannel.close();
                        } finally {
                            NettyClient.this.channel = null;
                            NettyChannel.removeChannelIfDisconnected(newChannel);
                        }
                    } else {
                        NettyClient.this.channel = newChannel;
                    }
                }
            } else if (future.getCause() != null) {
                throw new RemotingException(this, "client failed to connect to server "
                        + getRemoteAddress() + ", error message is:" + future.getCause().getMessage(), future.getCause());
            } else {
                throw new RemotingException(this, "client failed to connect to server "
                        + getRemoteAddress() + " client-side timeout "
                        + getConnectTimeout() + "ms (elapsed: " + (System.currentTimeMillis() - start) + "ms) from netty client");
            }
        }finally{
            if (! isConnected()) {
                future.cancel();
            }
        }
	}

	@Override
	public void doDisConnect() throws Throwable {
		try {
			NettyChannel.removeChannelIfDisconnected(channel);
		} catch (Throwable t) {
			logger.warn(t.getMessage());
		}
	}
	
	@Override
	public void doClose() throws Throwable {
		if (this.channel != null) {
			this.channel.close();
		}
	}
}
