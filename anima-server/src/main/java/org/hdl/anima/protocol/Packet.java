package org.hdl.anima.protocol;
/**
 * Packet 
 * @author qiuhd
 * @since  2014年9月12日
 * @version V1.0.0
 */
public class Packet {
	/**
	 * 包压缩标志 1：表示压缩 0：表示未压缩
	 */
	private final byte compressed;
	/**
	 * 包类型
	 */
	private final byte type;
	/**
	 * 包体长度
	 */
	private final int length;
	/**
	 * 包体内容
	 */
	private Object content;
	
	public Packet(byte compressed,byte type,int length) {
		this.compressed = compressed;
		this.type = type;
		this.length = length;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object content) {
		this.content = content;
	}

	public byte getCompressed() {
		return compressed;
	}

	public byte getType() {
		return type;
	}

	public int getLength() {
		return length;
	}
}
