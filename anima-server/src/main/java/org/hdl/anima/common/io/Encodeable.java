package org.hdl.anima.common.io;

import java.io.IOException;

/**
 * Encodeable
 * @author qiuhd
 * @since  2014年9月19日
 * @version V1.0.0
 */
public abstract class Encodeable extends RecordAdapter {

	@Override
	public void serialize(OutputArchive output) throws IOException {
		encode(output);
	}
	
	public abstract void encode(OutputArchive output) throws IOException;
}
