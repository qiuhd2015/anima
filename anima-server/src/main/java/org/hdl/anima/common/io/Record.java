package org.hdl.anima.common.io;

import java.io.IOException;

/**
 * Interface that is implemented by generated classes.
 * 
 */
public interface Record {
	
	/**
	 * Serialize
	 * @param output
	 * @throws IOException
	 */
    public void serialize(OutputArchive output)throws IOException;
    /**
     * Deserialize
     * @param input
     * @throws IOException
     */
    public void deserialize(InputArchive input)throws IOException;
}
