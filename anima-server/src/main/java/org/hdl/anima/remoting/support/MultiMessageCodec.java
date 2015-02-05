package org.hdl.anima.remoting.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hdl.anima.common.io.UnsafeByteArrayInputStream;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.Codec;
/**
 * MultiMessageCodec
 * @author qiuhd
 */
public final class MultiMessageCodec implements Codec {

    private Codec codec ;
    
    public MultiMessageCodec(Codec codec) {
    	this.codec = codec ;
    }

    public void encode(Channel channel, OutputStream output, Object msg) throws IOException {
        codec.encode(channel, output, msg);
    }

    public Object decode(Channel channel, InputStream input) throws IOException {
        UnsafeByteArrayInputStream bis = (UnsafeByteArrayInputStream)input;
        int beginIdx = bis.position();
        MultiMessage result = MultiMessage.create();
        do {
        	if (bis.available() == 0) {
        		break;
        	}
            Object obj = codec.decode(channel, bis);
            if (NEED_MORE_INPUT == obj) {
                bis.position(beginIdx);
                break;
            } else {
                result.addMessage(obj);
                beginIdx = bis.position();
            }
        } while (true);
        if (result.isEmpty()) {
            return NEED_MORE_INPUT;
        }
        if (result.size() == 1) {
            return result.get(0);
        }
        return result;
    }
}
