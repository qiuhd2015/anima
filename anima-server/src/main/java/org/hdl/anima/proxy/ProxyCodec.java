//package org.hdl.anima.proxy;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.hdl.anima.Application;
//import org.hdl.anima.common.io.Index;
//import org.hdl.anima.common.io.InputArchive;
//import org.hdl.anima.common.io.OutputArchive;
//import org.hdl.anima.common.io.Record;
//import org.hdl.anima.protocol.AbstractMessage;
//import org.hdl.anima.protocol.Broadcast;
//import org.hdl.anima.protocol.Kick;
//import org.hdl.anima.protocol.MessageFactory;
//import org.hdl.anima.protocol.Push;
//import org.hdl.anima.protocol.Request;
//import org.hdl.anima.protocol.Response;
//import org.hdl.anima.remoting.Channel;
//import org.hdl.anima.remoting.support.ExchangeCodec;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author qiuhd
// * @since 2014-2-11
// * @version V1.0.0
// */
//public class ProxyCodec extends ExchangeCodec {
//
//	private static final Logger logger = LoggerFactory.getLogger(ProxyCodec.class);
//	
//	public ProxyCodec(Application application) {
//		
//	}
//
//	@Override
//	protected Object decodeBody(Channel channel, InputStream is,
//			InputArchive input, byte[] pktHeader, int mid, byte type)
//			throws IOException {
//		if (type == AbstractMessage.TYPE_REQUEST || type == AbstractMessage.TYPE_NOTIFY) {
//			return decodeRequest(channel,is,input,pktHeader,mid,type);
//		}else if (type == AbstractMessage.TYPE_RESPONSE) {
//			return decodeResponseBody(channel,is,input,pktHeader,mid,type);
//		}else if (type == AbstractMessage.TYPE_PUSH) {
//			return decodePushBody(channel,is,input,pktHeader,mid,type);
//		}else if (type == AbstractMessage.TYPE_NOTIFY) {
//			return decodeBroadCaseBody(channel,is,input,pktHeader,mid,type);
//		}
//		throw new IOException("Unsupport message type:" + type);
//	}
//	
//	protected Request decodeRequest(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
//		Request request = new Request(mid,type);
//		//message sequence
//		int sequence = input.readInt();
//		//request <-> response or request->response
//		boolean twoWay = input.readBool();
//		//sender identity
//		int identity = input.readInt();
//		request.setSequence(sequence);
//		request.setTwoWay(twoWay);
//		request.setIdentity(identity);
//		
//		Record record = MessageFactory.getInstance().createMessage(mid);
//		if (record != null) {
//			//序列化请求体
//			record.deserialize(input);
//			request.setContent(record);
//		}else {
//			int readable = is.available();
//			byte[] body = new byte[readable];
//			is.read(body);
//			request.setContent(body);
//		}
//		return request;
//	}
//	
//	protected Response decodeResponseBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
//		Response response = new Response(mid);
//		//message sequence
//		int sequence = input.readInt();
//		//error code
//		int errorCode = input.readInt();
//		//error description
//		String errorDes = input.readString();
//		//receiver identity
//		int identity = input.readInt();
//		response.setSequence(sequence);
//		response.setErrorCode(errorCode);
//		response.setErrorDes(errorDes);
//		response.setIdentity(identity);
//		Record record = MessageFactory.getInstance().createMessage(mid);
//		if (record != null) {
//			//序列化请求体
//			record.deserialize(input);
//			response.setContent(record);
//		}else {
//			int readable = is.available();
//			byte[] body = new byte[readable];
//			is.read(body);
//			response.setContent(body);
//		}
//		return response;
//	}
//	
//	protected Push decodePushBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
//		Push push = new Push(mid);
//		//sender identity
//		int identity = input.readInt();
//		push.setIdentity(identity);
//		Record record = MessageFactory.getInstance().createMessage(mid);
//		if (record != null) {
//			//序列化请求体
//			record.deserialize(input);
//			push.setContent(record);
//		}else {
//			int readable = is.available();
//			byte[] body = new byte[readable];
//			is.read(body);
//			push.setContent(body);
//		}
//		return push;
//	}
//	
//	protected Broadcast decodeBroadCaseBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
//		Broadcast broadcast = new Broadcast(mid);
//		//sender identity
//		int identity = input.readInt();
//		broadcast.setIdentity(identity);
//		Index index = input.startVector();
//		List<Integer> receivers = new ArrayList<Integer>(index.size());
//		while(index.done()) {
//			int value = input.readInt();
//			receivers.add(value);
//			index.incr();			
//		}
//		broadcast.setReceivers(receivers);
//		Record record = MessageFactory.getInstance().createMessage(mid);
//		if (record != null) {
//			//序列化请求体
//			record.deserialize(input);
//			broadcast.setContent(record);
//		}else {
//			int readable = is.available();
//			byte[] body = new byte[readable];
//			is.read(body);
//			broadcast.setContent(body);
//		}
//		return broadcast;
//	}
//
//	@Override
//	public void encodeRequestBody(Channel channel, OutputStream os,
//			OutputArchive output, Request request) throws IOException {
//		int sequence = request.getSequence();
//		boolean twoway = request.isTwoWay();
//		int identity = request.getIdentity();
//		output.writeInt(sequence);
//		output.writeBool(twoway);
//		output.writeInt(identity);
//		
//		Object content = request.getContent();
//		if (content instanceof byte[]) {
//			os.write((byte[])content);
//		}else if (content instanceof Record) {
//			Record record = (Record) content;
//			record.serialize(output);
//		}
//	}
//
//	@Override
//	public void encodeResponseBody(Channel channel, OutputStream os,
//			OutputArchive output, Response response) throws IOException {
//		throw new UnsupportedOperationException("encodeResponseBody");
//	}
//
//	@Override
//	public void encodePushBody(Channel channel, OutputStream os,
//			OutputArchive output, Push push) throws IOException {
//		int sender = push.getIdentity();
//		output.writeInt(sender);
//		
//		Object content = push.getContent();
//		if (content instanceof byte[]) {
//			os.write((byte[])content);
//		}else if (content instanceof Record) {
//			Record record = (Record) content;
//			record.serialize(output);
//		}
//	}
//
//	@Override
//	public void encodeBroadcaseBody(Channel channel, OutputStream os,
//			OutputArchive output, Broadcast broad) throws IOException {
//		int sender = broad.getIdentity();
//		output.writeInt(sender);
//		output.startVector(broad.getReceivers());
//		for (int value : broad.getReceivers()) {
//			output.writeInt(value);
//		}
//		Object content = broad.getContent();
//		if (content instanceof byte[]) {
//			os.write((byte[])content);
//		}else if (content instanceof Record) {
//			Record record = (Record) content;
//			record.serialize(output);
//		}
//	}
//
//	@Override
//	public void encodeKick(Channel channel, OutputStream os,
//			OutputArchive output, Kick kick) throws IOException {
//		// TODO Auto-generated method stub
//		
//	}
//}
