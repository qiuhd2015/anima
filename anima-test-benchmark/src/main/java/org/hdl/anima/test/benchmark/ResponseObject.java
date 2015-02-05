package org.hdl.anima.test.benchmark;

import java.io.IOException;

import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.common.io.InputArchive;

/**
 * 
 * @author qiuhd
 * @since  2014年11月5日
 * @version V1.0.0
 */
public class ResponseObject extends Decodeable {
	
	private byte[] bytes = null;
	
    public ResponseObject(){
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }
	
	@Override
	public void decode(InputArchive input) throws IOException {
		this.bytes = input.readBuffer();
	}
}
