package org.hdl.anima.session;
/**
 * ClientSessionReconnectionException
 * @author qiuhd
 * @since  2014-2-25
 * @version V1.0.0
 */
public class ClientSessionReconnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7120959149952251699L;

	public ClientSessionReconnectionException() {
		super();
	}

	public ClientSessionReconnectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ClientSessionReconnectionException(String message) {
		super(message);
	}

	public ClientSessionReconnectionException(Throwable cause) {
		super(cause);
	}
}

