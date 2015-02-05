package org.hdl.anima.test.benchmark;

/**
 * nfs-rpc
 *   Apache License
 *   
 *   http://code.google.com/p/nfs-rpc (c) 2011
 */
import java.io.IOException;

import org.hdl.anima.common.io.Encodeable;
import org.hdl.anima.common.io.OutputArchive;

/**
 * Just for RPC Benchmark Test,request object
 * 
 * @author 
 */
public class RequestObject extends Encodeable {

	
	private byte[] bytes = null;
	
    public RequestObject(){
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public RequestObject(int size){
        bytes = new byte[size];
    }

    public byte[] getBytes() {
        return bytes;
    }

	@Override
	public void encode(OutputArchive output) throws IOException {
		output.writeBuffer(bytes);
	}
}
