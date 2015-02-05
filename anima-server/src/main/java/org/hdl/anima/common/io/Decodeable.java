package org.hdl.anima.common.io;

import java.io.IOException;

/**
 * Decodeable
 * @author qiuhd
 * @since  2014年9月19日
 * @version V1.0.0
 */
public abstract class Decodeable extends RecordAdapter {

	@Override
	public void deserialize(InputArchive input) throws IOException {
		decode(input);
	}
	
	public abstract void decode(InputArchive input) throws IOException;
}
