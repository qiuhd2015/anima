package org.hdl.anima.common.utils;

import java.io.IOException;
import java.io.InputStream;

import org.hdl.anima.common.io.LimitedInputStream;

/**
 * 
 * @author qiuhd
 * @since  2014-2-14
 * @version V1.0.0
 */
public class StreamUtils {

	private StreamUtils(){}

	public static InputStream limitedInputStream(final InputStream is, final int limit) throws IOException
	{
		return new LimitedInputStream(is, limit);
	}
	
	public static InputStream markSupportedInputStream(final InputStream is, final int markBufferSize) {
	    if(is.markSupported()) {
	        return is;
	    }

        return new InputStream() {
            byte[] mMarkBuffer;
            
            boolean mInMarked = false;
            boolean mInReset = false;
            private int mPosition = 0;
            private int mCount = 0;

            boolean mDry = false;
            
            @Override
            public int read() throws IOException {
                if(!mInMarked) {
                    return is.read();
                }
                else {
                    if(mPosition < mCount) {
                        byte b = mMarkBuffer[mPosition++];
                        return b & 0xFF;
                    }
                    
                    if(!mInReset) {
                        if(mDry) return -1;
                        
                        if(null == mMarkBuffer) {
                            mMarkBuffer = new byte[markBufferSize];
                        }
                        if(mPosition >= markBufferSize) {
                            throw new IOException("Mark buffer is full!");
                        }
                        
                        int read = is.read();
                        if(-1 == read){
                            mDry = true;
                            return -1;
                        }
                        
                        mMarkBuffer[mPosition++] = (byte) read;
                        mCount++;
                        
                        return read;
                    }
                    else {
                        // mark buffer is used, exit mark status!
                        mInMarked = false;
                        mInReset = false;
                        mPosition = 0;
                        mCount = 0;
                        
                        return is.read();
                    }
                }
            }

            /**
             * NOTE: the <code>readlimit</code> argument for this class
             *  has no meaning.
             */
            @Override
            public synchronized void mark(int readlimit) {
                mInMarked = true;
                mInReset = false;
                
                // mark buffer is not empty
                int count = mCount - mPosition;
                if(count > 0) {
                    System.arraycopy(mMarkBuffer, mPosition, mMarkBuffer, 0, count);
                    mCount = count;
                    mPosition = 0;
                }
            }
            
            @Override
            public synchronized void reset() throws IOException {
                if(!mInMarked) {
                    throw new IOException("should mark befor reset!");
                }
                
                mInReset = true;
                mPosition = 0;
            }
            
            @Override
            public boolean markSupported() {
                return true;
            }
            
            @Override
            public int available() throws IOException {
                int available = is.available();
                
                if(mInMarked && mInReset) available += mCount - mPosition;
                
                return available;
            }
        };
	}
	
	public static InputStream markSupportedInputStream(final InputStream is) {
	    return markSupportedInputStream(is, 1024);
	}

    public static void skipUnusedStream(InputStream is) throws IOException {
        if (is.available() > 0) {
            is.skip(is.available());
        }
    }
}

