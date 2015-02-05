package org.hdl.anima.common.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * BinaryInputArchive
 */
public class BinaryInputArchive implements InputArchive {
    
    private DataInput in;
    
    static public BinaryInputArchive getArchive(InputStream strm) {
        return new BinaryInputArchive(new DataInputStream(strm));
    }
    
    static private class BinaryIndex implements Index {
        private int nelems;
        private int size ;
        BinaryIndex(int nelems) {
            this.nelems = nelems;
            this.size = nelems;
        }
        public boolean done() {
            return (nelems <= 0);
        }
        public void incr() {
            nelems--;
        }
        
        public int size() {
        	return this.size;
        }
    }
    /** Creates a new instance of BinaryInputArchive */
    public BinaryInputArchive(DataInput in) {
        this.in = in;
    }
    
    public byte readByte() throws IOException {
        return in.readByte();
    }
    
    public boolean readBool() throws IOException {
    	byte value = in.readByte();
    	if (value == 1) {
    		return true;
    	}else {
    		return false;
    	}
    }
    
    public int readInt() throws IOException {
        return in.readInt();
    }
    
    public short readShort() throws IOException {
    	return in.readShort();
    }
    
    public long readLong() throws IOException {
        return in.readLong();
    }
    
    public float readFloat() throws IOException {
        return in.readFloat();
    }
    
    public double readDouble() throws IOException {
        return in.readDouble();
    }
    
    public String readString() throws IOException {
    	int len = in.readInt();
    	if (len == -1) return null;
    	byte b[] = new byte[len];
    	in.readFully(b);
    	return new String(b, "UTF8");
    }
    
    static public final int maxBuffer = determineMaxBuffer();
    private static int determineMaxBuffer() {
        String maxBufferString = System.getProperty("jute.maxbuffer");
        try {
            return Integer.parseInt(maxBufferString);
        } catch(Exception e) {
            return 0xfffff;
        }
        
    }
    public byte[] readBuffer() throws IOException {
        int len = readInt();
        if (len == -1) return null;
        if (len < 0 || len > maxBuffer) {
            throw new IOException("Unreasonable length = " + len);
        }
        byte[] arr = new byte[len];
        in.readFully(arr);
        return arr;
    }
    
    public void readRecord(Record r) throws IOException {
        r.deserialize(this);
    }
    
    public void startRecord() throws IOException {}
    
    public void endRecord() throws IOException {}
    
    public Index startVector() throws IOException {
        int len = readInt();
        if (len == -1) {
        	return null;
        }
		return new BinaryIndex(len);
    }
    
    public void endVector() throws IOException {}
    
    public Index startMap() throws IOException {
        return new BinaryIndex(readInt());
    }
    
    public void endMap() throws IOException {}
    
}
