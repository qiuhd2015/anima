package org.hdl.anima.remoting.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hdl.anima.common.io.BinaryInputArchive;
import org.hdl.anima.common.io.BinaryOutputArchive;
import org.hdl.anima.common.io.Bytes;
import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.Record;
import org.hdl.anima.common.io.UnsafeByteArrayInputStream;
import org.hdl.anima.common.io.UnsafeByteArrayOutputStream;
import org.hdl.anima.common.utils.StreamUtils;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Broadcast;
import org.hdl.anima.protocol.CloseClientSession;
import org.hdl.anima.protocol.FreezeClientSession;
import org.hdl.anima.protocol.HandSnakeReq;
import org.hdl.anima.protocol.HandSnakeResp;
import org.hdl.anima.protocol.HeartBeat;
import org.hdl.anima.protocol.Kick;
import org.hdl.anima.protocol.OpenClientSession;
import org.hdl.anima.protocol.OpenLocalSession;
import org.hdl.anima.protocol.Push;
import org.hdl.anima.protocol.Request;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.protocol.UnFreezeClientSession;
import org.hdl.anima.remoting.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * ExchangeCodec
 * @author qiuhd
 */
public abstract class ExchangeCodec extends AbstractCodec {

	private static final Logger logger = LoggerFactory.getLogger(ExchangeCodec.class);

	private static final int HEADER_LENGTH = 8;

	// magic code.
	protected static final short MAGIC = (short) 0xdabb;

	protected static final byte MAGIC_HIGH = Bytes.short2bytes(MAGIC)[0];

	protected static final byte MAGIC_LOW = Bytes.short2bytes(MAGIC)[1];

	protected static final byte FLAG_HANDSNAKE = 0x10;

	protected static final byte FLAG_HEARTBEAT = 0x20;

	protected static final byte FLAG_MESSAGE = 0x30;

	protected static final byte FLAG_KICK = 0x40;

	protected static final byte FLAG_OPEN_LOCAL = 0x50;

	protected static final byte FLAG_OPEN_CLIENT = 0x60;

	protected static final byte FLAG_CLOSE_CLIENT = 0x61;
	
	protected static final byte FLAG_FREEZE_CLIENT = 0x62;
	
	protected static final byte FLAG_UNFREEZE_CLIENT = 0x63;
	
	protected static final byte COMPRESSED = 1;

	protected static final byte UNCOMPRESSED = 0;

	@Override
	public Object decode(Channel channel, InputStream is) throws IOException {
		int readable = is.available();
		byte[] header = new byte[Math.min(readable, HEADER_LENGTH)];
		is.read(header);
		return decode(channel, is, readable, header);
	}

	private Object decode(Channel channel, InputStream is, int readable,
			byte[] header) throws IOException {
		// check magic code.
		if (readable > 0 && header[0] != MAGIC_HIGH || readable > 1
				&& header[1] != MAGIC_LOW) {
			int length = header.length;
			if (header.length < readable) {
				header = Bytes.copyOf(header, readable);
				// buffer.readBytes(header, length, readable - length);
				is.read(header, length, readable - length);
			}
			for (int i = 1; i < header.length - 1; i++) {
				if (header[i] == MAGIC_HIGH && header[i + 1] == MAGIC_LOW) {
					UnsafeByteArrayInputStream bis = ((UnsafeByteArrayInputStream) is);
					bis.position(bis.position() - header.length + i);
					header = Bytes.copyOf(header, i);
					break;
				}
			}
			return decode(channel, is, readable, header);
		}

		// check length.
		if (readable < HEADER_LENGTH) {
			return NEED_MORE_INPUT;
		}

		// get data length.
		int len = Bytes.bytes2int(header, 4);
		checkPayload(channel, len);

		int tt = len + HEADER_LENGTH;
		if (readable < tt) {
			return NEED_MORE_INPUT;
		}
		// limit input stream.
		if (readable != tt)
			is = StreamUtils.limitedInputStream(is, len);
		try {
			return decodeBody(channel, is, header);
		} finally {
			if (is.available() > 0) {
				try {
					if (logger.isWarnEnabled()) {
						logger.warn("Skip input stream " + is.available());
					}
					StreamUtils.skipUnusedStream(is);
				} catch (IOException e) {
					logger.warn(e.getMessage(), e);
				}
			}
		}
	}

	protected Object decodeBody(Channel channel, InputStream is, byte[] header) throws IOException {
		 //TODO 解压缩
		int type = header[2];byte compressed = header[3];
		if (type == FLAG_HANDSNAKE) {
			return decodeHandSnake(channel, is);
		}else if (type == FLAG_HEARTBEAT) {
			return decodeHeartBeat(channel, is);
		}else if (type == FLAG_OPEN_LOCAL) {
			return decodeOpenLocalSession(channel, is);
		}else if (type == FLAG_OPEN_CLIENT) {
			return decodeOpenClientSession(channel, is);
		}else if (type == FLAG_CLOSE_CLIENT) {
			return decodeCloseClientSession(channel, is);
		}else if (type == FLAG_FREEZE_CLIENT) {
			return decodeFreezeClientSession(channel, is);
		}else if (type == FLAG_UNFREEZE_CLIENT) {
			return decodeUnFreezeClientSession(channel, is);
		}else if (type == FLAG_KICK) {
			return decodeKickClient(channel, is);
		}else if(type == FLAG_MESSAGE) {
			return decodeMessage(channel, is,header);
		}else {
			logger.error("Failed to decode,cause : packet type not match");
		}
		return null;
	 }

	protected HandSnakeReq decodeHandSnake(Channel channel, InputStream is)
			throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		HandSnakeReq req = new HandSnakeReq();
		req.deserialize(input);
		return req;
	}

	protected HeartBeat decodeHeartBeat(Channel channel, InputStream is)
			throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		HeartBeat req = new HeartBeat();
		req.deserialize(input);
		return req;
	}

	protected OpenLocalSession decodeOpenLocalSession(Channel channel,
			InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		OpenLocalSession message = new OpenLocalSession();
		message.deserialize(input);
		return message;
	}

	protected OpenClientSession decodeOpenClientSession(Channel channel,
			InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		OpenClientSession message = new OpenClientSession();
		message.deserialize(input);
		return message;
	}

	protected CloseClientSession decodeCloseClientSession(Channel channel,
			InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		CloseClientSession message = new CloseClientSession();
		message.deserialize(input);
		return message;
	}
	
	protected FreezeClientSession decodeFreezeClientSession(Channel channel,
			InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		FreezeClientSession message = new FreezeClientSession();
		message.deserialize(input);
		return message;
	}
	
	protected UnFreezeClientSession decodeUnFreezeClientSession(Channel channel,
			InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		UnFreezeClientSession message = new UnFreezeClientSession();
		message.deserialize(input);
		return message;
	}
	
	public Kick decodeKickClient(Channel channel,InputStream is) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		Kick kick = new Kick();
		kick.deserialize(input);
		return kick;
	}

	protected Object decodeMessage(Channel channel, InputStream is,
			byte[] pktHeader) throws IOException {
		InputArchive input = BinaryInputArchive.getArchive(is);
		int id = input.readInt();
		byte type = input.readByte();
		return decodeBody(channel, is, input, pktHeader, id, type);
	}

	@Override
	public void encode(Channel channle, OutputStream os, Object message)
			throws IOException {
		OutputArchive output = BinaryOutputArchive.getArchive(os);
		if (message instanceof HandSnakeResp) {
			encodePacketHeader(channle, output, FLAG_HANDSNAKE, false);
			Record record = (Record) message;
			record.serialize(output);
		} else if (message instanceof HeartBeat) {
			encodePacketHeader(channle, output, FLAG_HEARTBEAT, false);
			Record record = (Record) message;
			record.serialize(output);
		} else if (message instanceof Kick) {
			encodePacketHeader(channle, output, FLAG_KICK, false);
			Kick kick = (Kick) message;
			encodeKick(channle, os, output,kick);
		} else if (message instanceof OpenLocalSession) {
			encodePacketHeader(channle, output, FLAG_OPEN_LOCAL, false);
			Record record = (Record) message;
			record.serialize(output);
		} else if (message instanceof OpenClientSession) {
			encodePacketHeader(channle, output, FLAG_OPEN_CLIENT, false);
			Record record = (Record) message;
			record.serialize(output);
		} else if (message instanceof CloseClientSession) {
			encodePacketHeader(channle, output, FLAG_CLOSE_CLIENT, false);
			Record record = (Record) message;
			record.serialize(output);
		}else if (message instanceof FreezeClientSession) {
			encodePacketHeader(channle, output, FLAG_FREEZE_CLIENT, false);
			Record record = (Record) message;
			record.serialize(output);
		}else if (message instanceof FreezeClientSession) {
			encodePacketHeader(channle, output, FLAG_FREEZE_CLIENT, false);
			Record record = (Record) message;
			record.serialize(output);	
		} else if (message instanceof AbstractMessage) {
			encodePacketHeader(channle, output, FLAG_MESSAGE, false);
			encodeMessage(channle, os, output, (AbstractMessage) message);
		}
		UnsafeByteArrayOutputStream ubaos = (UnsafeByteArrayOutputStream) os;
		// 写入消息长度
		int msgLen = ubaos.size();
		int msgBodyLen = msgLen - HEADER_LENGTH;
		byte[] len = Bytes.int2bytes(msgBodyLen);
		ubaos.write(len, 0, 4, len.length);
	}

	protected void encodePacketHeader(Channel channle, OutputArchive output,
			byte type, boolean compressed) throws IOException {
		// write magic code
		output.writeByte(MAGIC_HIGH);
		output.writeByte(MAGIC_LOW);
		// write packet type
		output.writeByte(type);
		if (compressed) {
			output.writeByte(COMPRESSED);
		} else {
			output.writeByte(UNCOMPRESSED);
		}
		// write packet length
		output.writeInt(0);
	}

	protected void encodeMessage(Channel channle, OutputStream os,
			OutputArchive output, AbstractMessage message) throws IOException {
		// serialize message header
		output.writeInt(message.getId());
		output.writeByte(message.getType());

		if (message instanceof Request) {
			encodeRequestBody(channle, os, output, (Request) message);
		} else if (message instanceof Response) {
			encodeResponseBody(channle, os, output, (Response) message);
		} else if (message instanceof Push) {
			encodePushBody(channle, os, output, (Push) message);
		} else if (message instanceof Broadcast) {
			encodeBroadcaseBody(channle, os, output, (Broadcast) message);
		}
	}

	protected abstract Object decodeBody(Channel channel, InputStream is,
			InputArchive input, byte[] pktHeader, int id, byte type)
			throws IOException;

	public abstract void encodeKick(Channel channel, OutputStream os,
			OutputArchive output, Kick kick) throws IOException;
	
	public abstract void encodeRequestBody(Channel channel, OutputStream os,
			OutputArchive output, Request request) throws IOException;

	public abstract void encodeResponseBody(Channel channel, OutputStream os,
			OutputArchive output, Response response) throws IOException;

	public abstract void encodePushBody(Channel channel, OutputStream os,
			OutputArchive output, Push push) throws IOException;

	public abstract void encodeBroadcaseBody(Channel channel, OutputStream os,
			OutputArchive output, Broadcast broad) throws IOException;
}
