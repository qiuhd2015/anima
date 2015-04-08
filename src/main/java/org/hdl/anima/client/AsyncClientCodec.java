package org.hdl.anima.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hdl.anima.Application;
import org.hdl.anima.common.io.Decodeable;
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
 * Async client Code
 * @author qiuhd
 * @since 2014-2-11
 * @version V1.0.0
 */
public class AsyncClientCodec extends ExchangeCodec{

	public AsyncClientCodec(Application application) {
		
	}
	
	@Override
	public Kick decodeKickClient(Channel channel,InputStream is) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Object decodeBody(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int mid, byte type)
			throws IOException {
		if (type == AbstractMessage.TYPE_RESPONSE) {
			return decodeResponseBody(channel,is,input,pktHeader,mid,type);
		}
		throw new IOException("Unsupport message type:" + type);
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
		try {
			Decodeable decodeable = AsyncClientResponseArgMapping.getInstance().getResponseArgMapping(mid);
			decodeable.deserialize(input);
			response.setContent(decodeable);
		}catch(Exception e) {
			throw new IOException("Decode error",e);
		}
		return response;
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void encodeResponseBody(Channel channel, OutputStream os,
			OutputArchive output, Response response) throws IOException {
		throw new UnsupportedOperationException();
	}
}
