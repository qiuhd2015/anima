package org.hdl.anima.common.io;

import java.io.IOException;
import java.util.List;
import java.util.TreeMap;

/**
 * Interface that all the serializers have to implement.
 *
 */
public interface OutputArchive {
    public void writeByte(byte b) throws IOException;
    public void writeBool(boolean b) throws IOException;
    public void writeShort(short s) throws IOException;
    public void writeInt(int i) throws IOException;
    public void writeLong(long l) throws IOException;
    public void writeFloat(float f) throws IOException;
    public void writeDouble(double d) throws IOException;
    public void writeString(String s) throws IOException;
    public void writeBuffer(byte buf[])throws IOException;
    public void writeRecord(Record r) throws IOException;
    public void startRecord(Record r) throws IOException;
    public void endRecord(Record r) throws IOException;
    public void startVector(List<?> v) throws IOException;
    public void endVector(List<?> v) throws IOException;
    public void startMap(TreeMap<?,?> v) throws IOException;
    public void endMap(TreeMap<?,?> v) throws IOException;

}
