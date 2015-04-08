package org.hdl.anima.session;

import org.hdl.anima.protocol.AbstractMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

/**
 * AbstractSession.
 * @author qiuhd
 * @since  2014年9月16日
 * @version V1.0.0
 */
public abstract class AbstractSession implements ISession {

	protected int sessionId	= -1;			//会话id
	protected String serverId;				//前端服务器id
	protected String serverType;			//服务器类型
	protected Object attachment;			//附件
	protected volatile int status = STATUS_CONNECTED;
	protected SessionCloseListener listener;//关闭监听
	private static final Logger logger = LoggerFactory.getLogger(AbstractMessage.class);
	
	@Override
	public void setlistener(SessionCloseListener listener) {
		if(this.listener != null) {
			throw new IllegalArgumentException("Failed to set up listner,cause :listener already set up");
		}
		
		this.listener = listener ;
	}
	
	/**
	 *  -1 表示server side
	 */
	@Override
	public int getId() {
		return sessionId;
	}

	@Override
	public void setId(int sid) {
		this.sessionId = sid;
	}

	@Override
	public int getStatus() {
		return this.status;
	}

	@Override
	public boolean isWorking() {
		return this.status == STATUS_WORKING;
	}

	@Override
	public void setStatus(int status) {
		this.status = status;
	}

	public String getServerType() {
		return serverType;
	}

	public void setServerType(String serverType) {
		this.serverType = serverType;
	}

	@Override
	public void setServerId(String sid) {
		this.serverId = sid;
	}
	
	@Override
	public String getServerId() {
		return this.serverId;
	}

	@Override
	public void setAttachment(Object attachment) {
		if (this.attachment == null) {
			Preconditions.checkArgument(attachment != null,"attachment == null");
		}
		
		this.attachment = attachment;
	}

	@Override
	public Object getAttachment() {
		return this.attachment;
	}

	@Override
	public void close() {
		try {
			if (listener != null) {
				listener.onSessionClosed(this);
			}
		}catch(Exception e) {
			logger.error("Close session error",e);
		}
		this.status = STATUS_CLOSED;
	}

	@Override
	public void close(int timeout) {
		close();
	}

	@Override
	public boolean isClosed() {
		return status == STATUS_CLOSED;
	}

	@Override
	public void send(AbstractMessage message) {
		Preconditions.checkArgument(message != null, "messsage == null");
		Preconditions.checkArgument(status == STATUS_WORKING,"Failed to send message,Cause session status:" + getStrForStatus());
	}
	/**
	 * 返回状态标识
	 * @return
	 */
	public String getStrForStatus() {
		if (STATUS_CLOSED == status) {
			return "STATUS_CLOSED" ;
		}else if (STATUS_CONNECTED == status) {
			return "STATUS_CONNECTED" ;
		}else if (STATUS_FREEZE == status) {
			return "STATUS_FREEZE" ;
		}else if (STATUS_HANDSNAKINGT == status) {
			return "STATUS_HANDSNAKINGT" ;
		}else if (STATUS_WORKING == status) {
			return "STATUS_WORKING" ;
		}
		return "N/A";
	}
}
