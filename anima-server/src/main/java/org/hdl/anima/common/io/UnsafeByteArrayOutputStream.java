package org.hdl.anima.common.io;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

/**
 * UnsafeByteArrayOutputStream.
 * 
 * @author qiuhd
 * @see ByteArrayOutputStream
 */

public class UnsafeByteArrayOutputStream extends OutputStream
{
	protected byte mBuffer[];

	protected int mCount;

	public UnsafeByteArrayOutputStream() {
		this(32);
    }

	public UnsafeByteArrayOutputStream(int size) {
		if( size < 0 )
			throw new IllegalArgumentException("Negative initial size: " + size);
		mBuffer = new byte[size];
	}

	public void write(int b) {
		int newcount = mCount + 1;
		if( newcount > mBuffer.length )
			mBuffer = Bytes.copyOf(mBuffer, Math.max(mBuffer.length << 1, newcount));
		mBuffer[mCount] = (byte)b;
		mCount = newcount;
	}

	public void write(byte b[], int off, int len) {
		if( ( off < 0 ) || ( off > b.length ) || ( len < 0 ) || ( ( off + len ) > b.length ) || ( ( off + len ) < 0 ) )
			throw new IndexOutOfBoundsException();
		if( len == 0 )
			return;
		int newcount = mCount + len;
		if( newcount > mBuffer.length )
			mBuffer = Bytes.copyOf(mBuffer, Math.max(mBuffer.length << 1, newcount));
		System.arraycopy(b, off, mBuffer, mCount, len);
		mCount = newcount;
	}
	
	/**
	 * 
	 */
	public void write(byte[] src,int srcOff,int destPos,int length) {
		if( ( srcOff < 0 ) || ( srcOff > src.length ) || ( length < 0 ) || ( ( srcOff + length ) > src.length ) || ( ( srcOff + length ) < 0 ) || (length > mCount))
			throw new IndexOutOfBoundsException();
		
		System.arraycopy(src, srcOff, mBuffer, destPos, length);
	}

	public int size() {
		return mCount;
	}

	public void reset() {
		mCount = 0;
	}

	public byte[] toByteArray() {
		return Bytes.copyOf(mBuffer, mCount);
	}

	public ByteBuffer toByteBuffer() {
		return ByteBuffer.wrap(mBuffer, 0, mCount);
	}

	public void writeTo(OutputStream out) throws IOException {
		out.write(mBuffer, 0, mCount);
	}

	public String toString() {
		return new String(mBuffer, 0, mCount);
	}

	public String toString(String charset) throws UnsupportedEncodingException {
		return new String(mBuffer, 0, mCount, charset);
	}

	public void close() throws IOException {
		
	}
}