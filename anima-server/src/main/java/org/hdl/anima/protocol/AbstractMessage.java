package org.hdl.anima.protocol;
/**
 * AbstractMessage.
 * @author qiuhd
 * @since  2014年9月12日
 * @version V1.0.0
 */
public abstract class AbstractMessage {
	/**
	 * Request type
	 */
	public static final byte TYPE_REQUEST = 0x10;
	/**
	 * Notify type
	 */
	public static final byte TYPE_NOTIFY = 0x20;
	/**
	 * Response type
	 */
	public static final byte TYPE_RESPONSE = 0x30;
	/**
	 * Push type
	 */
	public static final byte TYPE_PUSH = 0x40;
	/**
	 * Broadcast type
	 */
	public static final byte TYPE_BROADCAST = 0x50;
	/**
	 * 消息id
	 */
	protected final int id;
	/**
	 * 消息类型
	 */
	protected final byte type;
	/**
	 * 会话id，后台服务器之间消息-1表示
	 */
	protected int sid = -1;
	/**
	 * 消息内容长度
	 */
	private int length = 0;
	/**
	 * 消息内容
	 */
	private Object content;
	
	public AbstractMessage(int id,byte type) {
		this.id = id;
		this.type = type;
	}

	public Object getContent() {
		return content;
	}

	public void setContent(Object object) {
		this.content = object;
	}

	public int getId() {
		return id;
	}

	public byte getType() {
		return type;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}
	
	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String typeToString() {
		if (type == TYPE_REQUEST) {
			return "request";
		}else if (type == TYPE_NOTIFY) {
			return "notify";
		}else if (type == TYPE_BROADCAST) {
			return "broadcast";
		}else if (type == TYPE_RESPONSE) {
			return "response";
		}else if (type == TYPE_PUSH) {
			return "push";
		}
		return "N/A" ;
	}
	
	public boolean isBackendMessage() {
		return this.sid == -1;
	}
}
