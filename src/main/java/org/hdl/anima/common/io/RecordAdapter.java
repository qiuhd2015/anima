package org.hdl.anima.common.io;

import java.io.IOException;

/**
 * RecordAdapter
 * @author qiuhd
 * @since  2014年9月19日
 * @version V1.0.0
 */
public abstract class RecordAdapter implements Record{

	@Override
	public void serialize(OutputArchive output) throws IOException {
		//do nothing
	}

	@Override
	public void deserialize(InputArchive input) throws IOException {
		//do nothing
	}
}
