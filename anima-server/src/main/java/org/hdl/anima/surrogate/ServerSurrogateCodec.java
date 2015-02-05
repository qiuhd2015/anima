package org.hdl.anima.surrogate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.BinaryInputArchive;
import org.hdl.anima.common.io.Index;
import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.support.ExchangeCodec;

/**
 * ServerSurrogateCodec.
 * @author qiuhd
 * @since 2014-2-11
 * @version V1.0.0
 */
public class ServerSurrogateCodec extends ExchangeCodec{

	public ServerSurrogateCodec(Application application) {
		
	}
	
	@Override
	public Kick decodeKickClient(Channel channel,InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		int identity = input.readInt();
		Kick kick = new Kick();
		kick.deserialize(input);
		kick.setIdentity(identity);
		return kick;
	}

	@Override
	protected Object decodeBody(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int mid, byte type)
			throws IOException {
		if (type == AbstractMessage.TYPE_REQUEST || type == AbstractMessage.TYPE_NOTIFY) {
			return decodeRequest(channel,is,input,pktHeader,mid,type);
		}else if (type == AbstractMessage.TYPE_RESPONSE) {
			return decodeResponseBody(channel,is,input,pktHeader,mid,type);
		}else if (type == AbstractMessage.TYPE_PUSH) {
			return decodePushBody(channel,is,input,pktHeader,mid,type);
		}else if (type == AbstractMessage.TYPE_BROADCAST) {
			return decodeBroadCaseBody(channel,is,input,pktHeader,mid,type);
		}
		throw new IOException("Unsupport message type:" + type);
	}
	
	protected Request decodeRequest(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
		Request request = new Request(mid,type);
		//message sequence
		int sequence = input.readInt();
		//request <-> response or request->response
		boolean twoWay = input.readBool();
		//sender identity
		int identity = input.readInt();
		request.setSequence(sequence);
		request.setTwoWay(twoWay);
		request.setSid(identity);
		int readable = is.available();
		byte[] body = new byte[readable];
		is.read(body);
		request.setContent(body);
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
		return request;
	}
	
	protected Response decodeResponseBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
		Response response = new Response(mid);
		//message sequence
		int sequence = input.readInt();
		//error code
		int errorCode = input.readInt();
		//error description
		String errorDes = input.readString();
		//receiver identity
		int identity = input.readInt();
		response.setSequence(sequence);
		response.setErrorCode(errorCode);
		response.setErrorDes(errorDes);
		response.setSid(identity);
		int readable = is.available();
		byte[] body = new byte[readable];
		is.read(body);
		response.setContent(body);
//		Record record = MessageFactory.getInstance().createMessage(mid);
//		if (record != null) {
//			//序列化请求体
//			record.deserialize(input);
//			response.setContent(record);
//		}else {
//			
//		}
		return response;
	}
	
	protected Push decodePushBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
		Push push = new Push(mid);
		//sender identity
		int senderId = input.readInt();
		push.setSid(senderId);
		
		Index index = input.startVector();
		List<Integer> receviers = new ArrayList<Integer>(index.size());
		while (!index.done()) {
			receviers.add(input.readInt());
			index.incr();
		}
		push.setReceivers(receviers);
		int readable = is.available();
		byte[] body = new byte[readable];
		is.read(body);
		push.setContent(body);
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
		return push;
	}
	
	protected Broadcast decodeBroadCaseBody(Channel channel, InputStream is,InputArchive input,byte[] pktHeader, int mid, byte type) throws IOException{
		Broadcast broadcast = new Broadcast(mid);
		//sender identity
		int identity = input.readInt();
		broadcast.setSid(identity);

		int readable = is.available();
		byte[] body = new byte[readable];
		is.read(body);
		broadcast.setContent(body);
		return broadcast;
	}

	@Override
	public void encodeRequestBody(Channel channel, OutputStream os,
			OutputArchive output, Request request) throws IOException {
		int sequence = request.getSequence();
		boolean twoway = request.isTwoWay();
		int identity = request.getSid();
		output.writeInt(sequence);
		output.writeBool(twoway);
		output.writeInt(identity);
		
		Object content = request.getContent();
		if (content instanceof byte[]) {
			os.write((byte[])content);
		}else if (content instanceof Record) {
			Record record = (Record) content;
			record.serialize(output);
		}
	}

	@Override
	public void encodeResponseBody(Channel channel, OutputStream os,
			OutputArchive output, Response response) throws IOException {
		throw new UnsupportedOperationException("encodeResponseBody");
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
		
	}
}
