package org.hdl.anima.common.io;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.TreeMap;

/**
 *BinaryOutputArchive
 * @author qiuh
 */
public class BinaryOutputArchive implements OutputArchive {
    private ByteBuffer bb = ByteBuffer.allocate(1024);

    private DataOutput out;
    
    public static BinaryOutputArchive getArchive(OutputStream strm) {
        return new BinaryOutputArchive(new DataOutputStream(strm));
    }
    
    /** Creates a new instance of BinaryOutputArchive */
    public BinaryOutputArchive(DataOutput out) {
        this.out = out;
    }
    
    public void writeByte(byte b) throws IOException {
        out.writeByte(b);
    }
    
    public void writeBool(boolean b) throws IOException {
    	if (b) {
    		writeByte((byte)1);
    	}else {
    		writeByte((byte)0);
    	}
    }
    
    public void writeShort(short s) throws IOException {
    	 out.writeShort(s);
    }
    
    public void writeInt(int i) throws IOException {
        out.writeInt(i);
    }
    
    public void writeLong(long l) throws IOException {
        out.writeLong(l);
    }
    
    public void writeFloat(float f) throws IOException {
        out.writeFloat(f);
    }
    
    public void writeDouble(double d) throws IOException {
        out.writeDouble(d);
    }
    
    /**
     * create our own char encoder to utf8. This is faster 
     * then string.getbytes(UTF8).
     * @param s the string to encode into utf8
     * @return utf8 byte sequence.
     */
    final private ByteBuffer stringToByteBuffer(CharSequence s) {
        bb.clear();
        final int len = s.length();
        for (int i = 0; i < len; i++) {
            if (bb.remaining() < 3) {
                ByteBuffer n = ByteBuffer.allocate(bb.capacity() << 1);
                bb.flip();
                n.put(bb);
                bb = n;
            }
            char c = s.charAt(i);
            if (c < 0x80) {
                bb.put((byte) c);
            } else if (c < 0x800) {
                bb.put((byte) (0xc0 | (c >> 6)));
                bb.put((byte) (0x80 | (c & 0x3f)));
            } else {
                bb.put((byte) (0xe0 | (c >> 12)));
                bb.put((byte) (0x80 | ((c >> 6) & 0x3f)));
                bb.put((byte) (0x80 | (c & 0x3f)));
            }
        }
        bb.flip();
        return bb;
    }

    public void writeString(String s) throws IOException {
        if (s == null) {
            writeInt(-1);
            return;
        }
        ByteBuffer bb = stringToByteBuffer(s);
        writeInt(bb.remaining());
        out.write(bb.array(), bb.position(), bb.limit());
    }

    public void writeBuffer(byte barr[])throws IOException {
    	if (barr == null) {
    		out.writeInt(-1);
    		return;
    	}
    	out.writeInt(barr.length);
        out.write(barr);
    }
    
    public void writeRecord(Record r) throws IOException {
        r.serialize(this);
    }
    
    public void startRecord(Record r) throws IOException {}
    
    public void endRecord(Record r) throws IOException {}
    
    public void startVector(List<?> v) throws IOException {
    	if (v == null) {
    		writeInt(-1);
    		return;
    	}
        writeInt(v.size());
    }
    
    public void endVector(List<?> v) throws IOException {}
    
    public void startMap(TreeMap<?,?> v) throws IOException {
        writeInt(v.size());
    }
    
    public void endMap(TreeMap<?,?> v) throws IOException {}
    
}
