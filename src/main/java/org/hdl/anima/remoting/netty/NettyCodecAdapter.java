package org.hdl.anima.remoting.netty;

import java.io.IOException;

import org.hdl.anima.AppConf;
import org.hdl.anima.common.io.Bytes;
import org.hdl.anima.common.io.UnsafeByteArrayInputStream;
import org.hdl.anima.common.io.UnsafeByteArrayOutputStream;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.CodecException;
import org.hdl.anima.remoting.Constants;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandler.Sharable;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

/**
 * NettyCodecAdapter.
 * 
 * @author qiuhd
 */
final class NettyCodecAdapter {

    private final ChannelHandler encoder = new InternalEncoder();
    
    private final ChannelHandler decoder = new InternalDecoder();

    private final Codec          codec;
    
    private final AppConf 	 	 conf;
    
    private final int            bufferSize;
    
    private final org.hdl.anima.remoting.ChannelHandler handler;

    public NettyCodecAdapter(AppConf conf,Codec codec, org.hdl.anima.remoting.ChannelHandler handler) {
        this.codec = codec;
        this.conf = conf;
        this.handler = handler;
		this.bufferSize = conf.getInt(Constants.SOCKET_RECEIVE_BUFFER,Constants.DEFAULT_RECEIVE_BUFFER);
    }

    public ChannelHandler getEncoder() {
        return encoder;
    }

    public ChannelHandler getDecoder() {
        return decoder;
    }

    @Sharable
    private class InternalEncoder extends OneToOneEncoder {

        @Override
        protected Object encode(ChannelHandlerContext ctx, Channel ch, Object msg) throws Exception {
            UnsafeByteArrayOutputStream os = new UnsafeByteArrayOutputStream(1024); 
            NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
            try {
            	codec.encode(channel, os, msg);
            }catch(IOException e) {
            	throw new CodecException(e);
            }finally {
                NettyChannel.removeChannelIfDisconnected(ch);
            }
            return ChannelBuffers.wrappedBuffer(os.toByteBuffer());
        }
    }

    private class InternalDecoder extends SimpleChannelUpstreamHandler {

        private int    mOffset = 0, mLimit = 0;
        private byte[] mBuffer = null;

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent event) throws Exception {
            Object o = event.getMessage();
            if (! (o instanceof ChannelBuffer)) {
                ctx.sendUpstream(event);
                return;
            }

            ChannelBuffer input = (ChannelBuffer) o;
            int readable = input.readableBytes();
            if (readable <= 0) {
                return;
            }

            int off, limit;
            byte[] buf = mBuffer;
            if (buf == null) {
                buf = new byte[bufferSize];
                off = limit = 0;
            } else {
                off = mOffset;
                limit = mLimit;
            }

            NettyChannel channel = NettyChannel.getOrAddChannel(conf,ctx.getChannel(), handler);
            boolean remaining = true;
            Object msg;
            UnsafeByteArrayInputStream bis;
            try {
                do {
                    // read data into buffer.
                    int read = Math.min(readable, buf.length - limit);
                    input.readBytes(buf, limit, read);
                    limit += read;
                    readable -= read;
                    bis = new UnsafeByteArrayInputStream(buf, off, limit - off); // 涓嶉渶瑕佸叧闂�                    // decode object.
                    do {
                        try {
                            msg = codec.decode(channel, bis);
                        } catch (IOException e) {
                            remaining = false;
                            throw new CodecException(e);
                        }
                        if (msg == Codec.NEED_MORE_INPUT) {
                            if (off == 0) {
                                if (readable > 0) {
                                    buf = Bytes.copyOf(buf, buf.length << 1);
                                }
                            } else {
                                int len = limit - off;
                                System.arraycopy(buf, off, buf, 0, len); // adjust buffer.
                                off = 0;
                                limit = len;
                            }
                            break;
                        } else {
                            int pos = bis.position();
                            if (off == pos) {
                                remaining = false;
                                throw new IOException("Decode without read data.");
                            }
                            if (msg != null) {
                                Channels.fireMessageReceived(ctx, msg, event.getRemoteAddress());
                            }
                            off = pos;
                        }
                    } while (bis.available() > 0);
                } while (readable > 0);
            } finally {
                if (remaining) {
                    int len = limit - off;
                    if (len < buf.length / 2) {
                        System.arraycopy(buf, off, buf, 0, len);
                        off = 0;
                        limit = len;
                    }
                    mBuffer = buf;
                    mOffset = off;
                    mLimit = limit;
                } else {
                    mBuffer = null;
                    mOffset = mLimit = 0;
                }
                NettyChannel.removeChannelIfDisconnected(ctx.getChannel());
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            ctx.sendUpstream(e);
        }
    }
}