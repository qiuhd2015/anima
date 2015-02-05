package org.hdl.anima.test.benchmark;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hdl.anima.common.io.BinaryInputArchive;
import org.hdl.anima.common.io.Bytes;
import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.UnsafeByteArrayInputStream;
import org.hdl.anima.common.utils.StreamUtils;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.Response;
import org.hdl.anima.remoting.Channel;
import org.hdl.anima.remoting.support.AbstractCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author qiuhd
 * @since  2014年11月5日
 * @version V1.0.0
 */
public class ExchangeClientCodec extends AbstractCodec {
	
	private static final Logger logger = LoggerFactory.getLogger(ExchangeClientCodec.class);
	
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
		InputArchive input = BinaryInputArchive.getArchive(is);
		int msgId = input.readInt();
		byte msgType = input.readByte();
		if(type == FLAG_MESSAGE && msgType == AbstractMessage.TYPE_RESPONSE) {
			Response response = new Response(msgId);
			//message sequence
			int sequence = input.readInt();
			//error code
			int errorCode = input.readInt();
			//error description
			String errorDes = input.readString();
			//receiver identity
			response.setSequence(sequence);
			response.setErrorCode(errorCode);
			response.setErrorDes(errorDes);
			if (errorCode == 200) {
				ResponseObject responseObject = new ResponseObject();
				responseObject.decode(input);
				response.setContent(responseObject);
			}
			return response;
		}else {
			logger.error("Failed to decode,cause : packet type not match");
		}
		return null;
	 }

	@Override
	public void encode(Channel channle, OutputStream os, Object message)
			throws IOException {
		
	}
}
