package org.hdl.anima.common.io;

import java.io.IOException;

/**
 * Interface that all the Deserializers have to implement.
 *
 */
public interface InputArchive {
    public byte readByte() throws IOException;
    public boolean readBool() throws IOException;
    public short readShort() throws IOException;
    public int readInt() throws IOException;
    public long readLong() throws IOException;
    public float readFloat() throws IOException;
    public double readDouble() throws IOException;
    public String readString() throws IOException;
    public byte[] readBuffer() throws IOException;
    public void readRecord(Record r) throws IOException;
    public void startRecord() throws IOException;
    public void endRecord() throws IOException;
    public Index startVector() throws IOException;
    public void endVector() throws IOException;
    public Index startMap() throws IOException;
    public void endMap() throws IOException;
}
