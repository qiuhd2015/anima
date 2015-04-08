package org.hdl.anima.common.io;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * UnsafeByteArrayInputStrem.
 * 
 * @author qiuhd
 * @see ByteArrayInputStream
 */

public class UnsafeByteArrayInputStream extends InputStream implements Position{
	protected byte mData[];

	protected int mPosition, mLimit, mMark = 0;

	public UnsafeByteArrayInputStream(byte buf[])
	{
		this(buf, 0, buf.length);
	}

	public UnsafeByteArrayInputStream(byte buf[], int offset)
	{
		this(buf, offset, buf.length-offset);
    }

	public UnsafeByteArrayInputStream(byte buf[], int offset, int length)
	{
    	mData = buf;
    	mPosition = mMark = offset;
        mLimit = Math.min(offset+length, buf.length);
    }

	public int read()
	{
		return ( mPosition < mLimit ) ? ( mData[mPosition++] & 0xff ) : -1;
    }

	public int read(byte b[], int off, int len)
	{
		if( b == null )
		    throw new NullPointerException();
		if( off < 0 || len < 0 || len > b.length - off )
		    throw new IndexOutOfBoundsException();
		if( mPosition >= mLimit )
		    return -1;
		if( mPosition + len > mLimit )
		    len = mLimit - mPosition;
		if( len <= 0 )
		    return 0;
		System.arraycopy(mData, mPosition, b, off, len);
		mPosition += len;
		return len;
    }

	public long skip(long len)
    {
		if( mPosition + len > mLimit )
			len = mLimit - mPosition;
		if( len <= 0 )
			return 0;
		mPosition += len;
		return len;
    }

	public int available()
	{
		return mLimit - mPosition;
	}

	public boolean markSupported()
    {
    	return true;
	}

	public void mark(int readAheadLimit)
	{
		mMark = mPosition;
	}

	public void reset()
	{
		mPosition = mMark;
	}

	public void close() throws IOException
	{}

	public int position()
	{
		return mPosition;
	}

	public void position(int newPosition)
	{
		mPosition = newPosition;
	}
	
	public int size() {
		return mData == null ? 0 : mData.length;
	}
}