package org.hdl.anima.common.io;

/**
 * Interface that acts as an iterator for deserializing maps.
 * The deserializer returns an instance that the record uses to
 * read vectors and maps. An example of usage is as follows:
 *
 * <code>
 * Index idx = startVector(...);
 * while (!idx.done()) {
 *   .... // read element of a vector
 *   idx.incr();
 * }
 * </code>
 */
public interface Index {
	
	public int size();
    public boolean done();
    public void incr();
}
