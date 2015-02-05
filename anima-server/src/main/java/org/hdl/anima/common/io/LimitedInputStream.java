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

public class LimitedInputStream extends InputStream implements Position {
	
	private InputStream is = null;
	private int mPosition = 0, mMark = 0,mLimit = 0;
	
	public LimitedInputStream(final InputStream is,int limit) throws IOException {
		this.is = is;
		mLimit = Math.min(limit, is.available());
	}

	public int read() throws IOException
	{
		if( mPosition < mLimit )
		{
			mPosition++;
			return is.read();
		}
		return -1;
    }

	public int read(byte b[], int off, int len) throws IOException
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

		is.read(b, off, len);
		mPosition += len;
		return len;
    }

	public long skip(long len) throws IOException
    {
		if( mPosition + len > mLimit )
			len = mLimit - mPosition;

		if( len <= 0 )
			return 0;

		is.skip(len);
		mPosition += len;
		return len;
    }

	public int available()
	{
		return mLimit - mPosition;
	}

	public boolean markSupported()
    {
    	return is.markSupported();
	}

	public void mark(int readlimit)
	{
		is.mark(readlimit);
		mMark = mPosition;
	}

	public void reset() throws IOException
	{
		is.reset();
		mPosition = mMark;
	}
	
	public int position() {
		return this.mPosition;
	}

	public void close() throws IOException
	{}
}