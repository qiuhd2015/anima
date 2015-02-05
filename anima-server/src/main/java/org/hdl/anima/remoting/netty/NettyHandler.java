package org.hdl.anima.remoting.netty;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.hdl.anima.AppConf;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
/**
 * NettyHandler
 * @author qiuhd
 */
public class NettyHandler extends SimpleChannelHandler {

    private final Map<String, Channel> channels = new ConcurrentHashMap<String, Channel>();
    
    private final AppConf conf;
    
    private final ChannelHandler handler;
    
    public NettyHandler(AppConf conf, ChannelHandler handler){
        this.conf = conf;
        this.handler = handler;
    }

    public Map<String, Channel> getChannels() {
        return channels;
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
        try {
            if (channel != null) {
            	InetSocketAddress address = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
            	String key = address.getAddress().getHostAddress() + ":" + address.getPort();
            	channels.put(key, channel);
            }
            handler.connected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    @Override
    public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
        try {
        	InetSocketAddress address = (InetSocketAddress) ctx.getChannel().getRemoteAddress();
        	String key = address.getAddress().getHostAddress() + ":" + address.getPort();
            channels.remove(key);
            handler.disconnected(channel);
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
    
    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
        try {
            handler.received(channel, e.getMessage());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    @Override
    public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
        super.writeRequested(ctx, e);
        NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
        try {
            handler.sent(channel, e.getMessage());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
        NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
        try {
            handler.caught(channel, e.getCause());
        } finally {
            NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
        }
    }
}