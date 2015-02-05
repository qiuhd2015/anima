package org.hdl.anima.fronend;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
 * Front server code
 * @author qiuhd
 * @since 2014-2-11
 * @version V1.0.0 
 */
public class FrontendCodec extends ExchangeCodec {

//	private static final Logger logger = LoggerFactory.getLogger(FronentCodec.class);

	private RequestMappingMethodHandler requetMapping ;
	
	public FrontendCodec(Application application) {
		requetMapping = application.getMoulde(RequestMappingMethodHandler.class);
	}

	@Override
	protected Object decodeBody(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int id, byte type)
			throws IOException {
		if (type == AbstractMessage.TYPE_REQUEST || type == AbstractMessage.TYPE_NOTIFY) {
			return decodeRequest(channel,is,input,pktHeader,id,type);
		}
		throw new IOException("Unsupport message type:" + type);
	}

	protected Request decodeRequest(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int id, byte type)
			throws IOException {
		Request request = new Request(id, type);
		try {
			// message sequence
			int sequence = input.readInt();
			request.setSequence(sequence);
			// request <-> response or request->response
			boolean twoWay = input.readBool();
			request.setTwoWay(twoWay);
			RequestMappingInfo mappingInfo = requetMapping.getMappingInfo(request);
			if (mappingInfo != null) {
				Decodeable content = mappingInfo.getParameterObjectFactory()
						.createObject();
				// 序列化请求体
				content.deserialize(input);
				request.setContent(content);
			} else {
				int readable = is.available();
				byte[] body = new byte[readable];
				is.read(body);
				request.setContent(body);
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
		throw new UnsupportedOperationException("encodeRequestBody");
	}

	@Override
	public void encodeResponseBody(Channel channel, OutputStream os,
			OutputArchive output, Response response) throws IOException {
		int sequence = response.getSequence();
		int errorCode = response.getErrorCode();
		String errorDes = response.getErrorDes();
		output.writeInt(sequence);
		output.writeInt(errorCode);
		output.writeString(errorDes);
		//write response body
		if (errorCode == Response.OK) {
			Object content = response.getContent();
			if (content instanceof byte[]) {
				os.write((byte[])content);
			}else if (content instanceof Record) {
				Record record = (Record) content;
				record.serialize(output);
			}
		}
	}

	@Override
	public void encodePushBody(Channel channel, OutputStream os,
			OutputArchive output, Push push) throws IOException {
		int sender = push.getSid();
		output.writeInt(sender);
		Object content = push.getContent();
		if (content instanceof byte[]) {
			os.write((byte[])content);
		}else if (content instanceof Record) {
			Record record = (Record) content;
			record.serialize(output);
		}
	}

	@Override
	public void encodeBroadcaseBody(Channel channel, OutputStream os,
			OutputArchive output, Broadcast broad) throws IOException {
		int sender = broad.getSid();
		output.writeInt(sender);
		Object content = broad.getContent();
		if (content instanceof byte[]) {
			os.write((byte[])content);
		}else if (content instanceof Record) {
			Record record = (Record) content;
			record.serialize(output);
		}
	}

	@Override
	public void encodeKick(Channel channel, OutputStream os,
			OutputArchive output, Kick kick) throws IOException {
		kick.serialize(output);
	}
}
