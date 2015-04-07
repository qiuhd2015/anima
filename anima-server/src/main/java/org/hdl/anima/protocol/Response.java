package org.hdl.anima.protocol;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;

/**
 * Response message.
 * @author qiuhd
 * @since  2014年9月11日
 * @version V1.0.0
 */
public class Response extends AbstractMessage{
	/**
     * ok.
     */
    public static final int OK = 200;
    /**
	 * bad Request
	 */
	public static final int BAD = 404 ;
    /**
     * service not found.
     */
    public static final int SERVICE_NOT_FOUND = 500;
    /**
     * service error.
     */
    public static final int SERVICE_ERROR = 600;
    
	private int sequence;
	
	private int errorCode = OK;
	
	private String errorDes;
	
	private Request request;
	
	public Response(int id) {
		super(id, AbstractMessage.TYPE_RESPONSE);
	}
	
	public Response(int id,int errorCode) {
		this(id);
		this.errorCode = errorCode;
	}
	
	public Response(int id,int errorCode,String errorDes) {
		this(id,errorCode);
		this.errorDes = errorDes;
	}
	
	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDes() {
		return errorDes;
	}

	public void setErrorDes(String errorDes) {
		this.errorDes = errorDes;
	}

	public boolean isOK() {
		return this.errorCode == 200 ? true : false;
	}
	
	public Request getRequest() {
		return request;
	}
	
	public void setRequst(Request request) {
		this.request = request;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("hid", id)
				.add("type", typeToString()).add("sessionId", sid)
				.add("sequence", this.sequence)
				.add("errorCode", errorCode)
				.add("errorDes", errorDes).toString();
	}
}
