package org.hdl.anima.backend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.Decodeable;
import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;
import org.hdl.anima.handler.RequestMappingInfo;
import org.hdl.anima.handler.RequestMappingMethodHandler;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.support.ExchangeCodec;
/**
 * backend server side code
 * @author qiuhd
 * @since 2014-2-11
 * @version V1.0.0
 */
public class BackendCodec extends ExchangeCodec {
	
	private RequestMappingMethodHandler requestMapping ;
	
	public BackendCodec(Application application) {
		requestMapping = application.getMoulde(RequestMappingMethodHandler.class);
	}

	@Override
	protected Object decodeBody(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int mid, byte type)
			throws IOException {
		if (type == AbstractMessage.TYPE_REQUEST || type == AbstractMessage.TYPE_NOTIFY) {
			return decodeRequest(channel,is,input,pktHeader,mid,type);
		}
		throw new IOException("Unsupport message type:" + type);
	}
	
	protected Request decodeRequest(Channel channle, InputStream is,InputArchive input,byte[] pktHeader, int id, byte type) throws IOException{
		Request request = new Request(id, type);
		//message sequence
		int sequence = input.readInt();
		//request <-> response or request->response
		boolean twoWay = input.readBool();
		//sender identity
		int identity = input.readInt();
		
		request.setSequence(sequence);
		request.setTwoWay(twoWay);
		request.setSid(identity);
		
		RequestMappingInfo mappingInfo = requestMapping.getMappingInfo(request);
		try {
			if (mappingInfo != null) {
				Decodeable content = mappingInfo.getParameterObjectFactory()
						.createObject();
				if (content != null) {
					// 序列化请求体
					content.deserialize(input);
					request.setContent(content);
				}
			} else {
				request.setBroken(true);
				request.setContent(new Exception("Did not found request mapping info,Request info :" + request));
			}
		} catch (Throwable t) {
			request.setBroken(true);
			request.setContent(t);
		}
		return request;
	}
	
	@Override
	public void encodeRequestBody(Channel channel, OutputStream os,
			OutputArchive output, Request request) throws IOException {
		
	}

	@Override
	public void encodeResponseBody(Channel channel, OutputStream os,
			OutputArchive output, org.hdl.anima.protocol.Response response)
			throws IOException {
		int sequence = response.getSequence();
		int errorCode = response.getErrorCode();
		String errorDes = response.getErrorDes();
		int identity = response.getSid();
		output.writeInt(sequence);
		output.writeInt(errorCode);
		output.writeString(errorDes);
		output.writeInt(identity);
		//write response body
		if (errorCode == Response.OK) {
			Object content = response.getContent();
			if (content instanceof Record) {
				Record record = (Record) content;
				record.serialize(output);
			}else {
				throw new IOException("Serialize response body error,cause : Class type not macth!");
			}
		}
	}

	@Override
	public void encodePushBody(Channel channel, OutputStream os,
			OutputArchive output, Push push) throws IOException {
		int sender = push.getSid();
		output.writeInt(sender);
		
		List<Integer> receivers = push.getReceivers();
		output.startVector(receivers);
		for (Integer value : receivers) {
			output.writeInt(value);
		}
		Object content = push.getContent();
		if (content instanceof Record) {
			Record record = (Record) content;
			record.serialize(output);
		}else {
			throw new IOException("Serialize response body error,cause : Class type not macth!");
		}
	}

	@Override
	public void encodeBroadcaseBody(Channel channel, OutputStream os,
			OutputArchive output, Broadcast broad) throws IOException {
		int sender = broad.getSid();
		output.writeInt(sender);
		Object content = broad.getContent();
		if (content instanceof Record) {
			Record record = (Record) content;
			record.serialize(output);
		}else {
			throw new IOException("Serialize response body error,cause : Class type not macth!");
		}
	}

	@Override
	public void encodeKick(Channel channel, OutputStream os,
			OutputArchive output, Kick kick) throws IOException {
		output.writeInt(kick.getIdentity());
		kick.serialize(output);
	}
}
