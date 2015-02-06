package org.hdl.anima.client;

import org.hdl.anima.common.io.Record;

/**
 * 
 * @author qiuhd
 * @since  2014年11月3日
 * @version V1.0.0
 */
public final class AsyncClientHelper {
	
	private static AsyncClientMgr clientMgr ;
	
	public static void setAsyncCientMgr(AsyncClientMgr asyncClientMgr) {
		if (asyncClientMgr == null) {
			throw new NullPointerException("asyncClientMgr can not be empty!");
		}
		clientMgr = asyncClientMgr;
	}
	/**
	 * 向指定后台服务器发送请求
	 * @param serverId		后台服务id
	 * @param msgId         消息id
	 * @param requestArg    请求参数
	 * @param callback		响应回调
	 * @return
	 */
	public static <T> int request(String serverId,int msgId,Record requestArg,AsyncMethodCallback<T> callback) {
		return clientMgr.request(serverId, msgId, requestArg, callback);
	}
	
	/**
	 * 向指定后台服务器发送通知
	 * @param serverId		后台服务id
	 * @param msgId         消息id
	 * @param requestArg    请求参数
	 */
	public static void notify(String serverId,int msgId,Record requestArg) {
		clientMgr.notify(serverId, msgId, requestArg);
	}
	
	/**
	 * 向指定类型后台服务器发送通知
	 * @param serverId		后台服务id
	 * @param msgId         消息id
	 * @param requestArg    请求参数
	 */
	public static void notifyForType(String serverType,int msgId,Record requestArg) {
		clientMgr.notifyForType(serverType, msgId, requestArg);
	}
}
