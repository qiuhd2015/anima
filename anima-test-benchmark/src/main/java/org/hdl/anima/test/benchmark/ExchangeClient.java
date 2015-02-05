package org.hdl.anima.test.benchmark;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.hdl.anima.common.TaskEngine;
import org.hdl.anima.common.io.BinaryInputArchive;
import org.hdl.anima.common.io.BinaryOutputArchive;
import org.hdl.anima.common.io.Bytes;
import org.hdl.anima.common.io.InputArchive;
import org.hdl.anima.common.io.OutputArchive;
import org.hdl.anima.common.io.UnsafeByteArrayInputStream;
import org.hdl.anima.common.io.UnsafeByteArrayOutputStream;
import org.hdl.anima.common.utils.StringUtils;
import org.hdl.anima.protocol.AbstractMessage;
import org.hdl.anima.protocol.HandSnakeReq;
import org.hdl.anima.protocol.HandSnakeResp;
import org.hdl.anima.protocol.HeartBeat;
import org.hdl.anima.remoting.Codec;
import org.hdl.anima.remoting.support.MultiMessageCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author qiuhd
 * @since  2014年10月8日
 * @version V1.0.0
 */
public class ExchangeClient {
	
	private static final Logger logger = LoggerFactory.getLogger(ExchangeClient.class);
	private String targetIp;
	private int targetPort;
	private int rpcTimeout;
	private Socket socket ;
	private OutputStream os;
	private InputStream is;
	private final HeartBeatTask heartBeatTask = new HeartBeatTask();
	private volatile boolean started = false;
	private AtomicInteger sequenceIncrementCounter = new AtomicInteger(0);
    // magic code.
    protected static final short MAGIC              = (short) 0xdabb;
    
    protected static final byte MAGIC_HIGH         = Bytes.short2bytes(MAGIC)[0];
    
    protected static final byte MAGIC_LOW          = Bytes.short2bytes(MAGIC)[1];
    
    protected static final byte FLAG_HANDSNAKE     = 0x10;

    protected static final byte FLAG_HEARTBEAT     = 0x20;

    protected static final byte FLAG_MESSAGE       = 0x30;

    protected static final byte FLAG_KICK          = 0x40;
    
    protected static final byte FLAG_OPEN_LOCAL    = 0x50;
    
    protected static final byte FLAG_OPEN_CLIENT   = 0x60;
    
    protected static final byte FLAG_CLOSE_CLIENT  = 0x70;
    
	private static final int HEADER_LENGTH = 8;
    
	/**
	 * Request type
	 */
	public static final byte TYPE_REQUEST = 0x10;
	/**
	 * Notify type
	 */
	public static final byte TYPE_NOTIFY = 0x20;
	/**
	 * Response type
	 */
	public static final byte TYPE_RESPONSE = 0x30;
	/**
	 * Push type
	 */
	public static final byte TYPE_PUSH = 0x40;
	/**
	 * Broadcast type
	 */
	public static final byte TYPE_BROADCAST = 0x50;
    
    protected static final byte COMPRESSED = 1;
    
    protected static final byte UNCOMPRESSED = 0;
    
    private static String reconnectToken ;
    
    private int bufferSize = 1024 * 8;
    
    private Codec codec ;
    
    public interface ResponseCallback<T> {
    	public void onResponseComleted(T response) ;
    }
	
	public ExchangeClient(String targetIp,int targetPort,int rpcTimeout) {
		checkArgument(!StringUtils.isEmpty(targetIp),"targetIp can not be empty!");
		this.targetIp = targetIp;
		this.targetPort = targetPort;
		this.rpcTimeout = rpcTimeout;
		codec = new MultiMessageCodec(new ExchangeClientCodec());
	}
	
	public void start() throws Exception{
		try {
			if (started == false) {
				socket = new Socket();
				socket.setSoLinger(false, 0);
				socket.setTcpNoDelay(true);
				socket.setSoTimeout(rpcTimeout);
				InetSocketAddress addresses = new InetSocketAddress(targetIp,targetPort);
				socket.connect(addresses,rpcTimeout);
				os = new DataOutputStream(socket.getOutputStream());
				is= new DataInputStream(socket.getInputStream());
				sendHandsnakeReq(new ResponseCallback<HandSnakeResp>(){
					@Override
					public void onResponseComleted(HandSnakeResp response) {
						started = true;
					}
				});
			}
		} catch (Exception e) {
			throw e;
		}
	}
	
	
	public boolean isConnnectd() {
		if (socket != null && socket.isConnected()) {
			return true;
		}
		return false;
	}
	
	public void destroy() {
		
		if (started == true) {
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
			
				}
				socket = null;
			}
			TaskEngine.getInstance().cancelScheduledTask(heartBeatTask);
			started = false;
		}
	}
	
	private static byte[] buffer = new byte[1024 * 4];
	
	private void sendHandsnakeReq(ResponseCallback<HandSnakeResp> callback) throws Exception {
		UnsafeByteArrayOutputStream outputStream = new UnsafeByteArrayOutputStream(1024);
		OutputArchive output = BinaryOutputArchive.getArchive(outputStream);
		output.writeByte(MAGIC_HIGH);
		output.writeByte(MAGIC_LOW);
		output.writeByte(FLAG_HANDSNAKE);
		output.writeByte(UNCOMPRESSED);
		output.writeInt(0);
		HandSnakeReq req = new HandSnakeReq();
		req.setClientType("Unity");
		req.setReconnectToken(reconnectToken);
		req.setApiVersion("0.0.1");
		req.serialize(output);
		//写入消息长度
		int msgLen = outputStream.size();
		int msgBodyLen = msgLen - 8;
		byte[] len = Bytes.int2bytes(msgBodyLen);
		outputStream.write(len, 0, 4, len.length);
		os.write(outputStream.toByteArray());
		os.flush();
		while (true) {
			int readable = is.read(buffer);
			UnsafeByteArrayInputStream inputStream = new UnsafeByteArrayInputStream(buffer, 0, readable);
			BinaryInputArchive inputArchive = BinaryInputArchive.getArchive(inputStream);
			if (MAGIC == inputArchive.readShort()) {
	    		byte type = inputArchive.readByte();
	    		if (type == FLAG_HEARTBEAT) {
	        		continue;
	        	}
	    		inputArchive.readByte();
	        	inputArchive.readInt();
	        	HandSnakeResp resp = new HandSnakeResp();
	        	resp.deserialize(inputArchive);
	        	if (resp.isOk()) {
	        		//TaskEngine.getInstance().schedule(heartBeatTask, resp.getHeartbeatTime(), resp.getHeartbeatTime());
	        		callback.onResponseComleted(resp);
	        		break;
	        	}
	    	}
		}
	}
	
	private class HeartBeatTask extends TimerTask {

		@Override
		public void run() {
			try {
				if (started) {
					UnsafeByteArrayOutputStream outputStream = new UnsafeByteArrayOutputStream(1024);
					OutputArchive output = BinaryOutputArchive.getArchive(outputStream);
					output.writeByte(MAGIC_HIGH);
					output.writeByte(MAGIC_LOW);
					output.writeByte(FLAG_HEARTBEAT);
					output.writeByte(UNCOMPRESSED);
					output.writeInt(0);
					HeartBeat req = new HeartBeat();
					req.setTwoWay(true);
					req.serialize(output);
					//写入消息长度
					int msgLen = outputStream.size();
					int msgBodyLen = msgLen - 8;
					byte[] len = Bytes.int2bytes(msgBodyLen);
					outputStream.write(len, 0, 4, len.length);
					os.write(outputStream.toByteArray());
					os.flush();
				}
			}catch(Exception e) {
				//ignore
			}
		}
	}
	
	private long requestTime;
	
	private long responseTime;
	
	private int lastSequence ;
	
	private int lastRequestId;
	
	public ResponseObject request(int msgId,RequestObject request) throws Exception{
//		requestTime = System.currentTimeMillis();
		UnsafeByteArrayOutputStream outputStream = new UnsafeByteArrayOutputStream(request.getBytes().length + 100);
		OutputArchive output = BinaryOutputArchive.getArchive(outputStream);
		output.writeByte(MAGIC_HIGH);
		output.writeByte(MAGIC_LOW);
		output.writeByte(FLAG_MESSAGE);
		output.writeByte(UNCOMPRESSED);
		output.writeInt(0);
		//message id 
		output.writeInt(msgId);
		//message type
		output.writeByte(TYPE_REQUEST);
		lastRequestId = msgId;
		lastSequence = sequenceIncrementCounter.incrementAndGet();
//		System.err.println(Thread.currentThread().getName() +":"+ sequence);
		output.writeInt(lastSequence);
		output.writeBool(true);
		request.encode(output);
		//写入消息长度
		int msgLen = outputStream.size();
		int msgBodyLen = msgLen - 8;
		byte[] len = Bytes.int2bytes(msgBodyLen);
		outputStream.write(len, 0, 4, len.length);
		os.write(outputStream.toByteArray());
		os.flush();
		ResponseObject responseObject = null;
		responseObject = readMessage();
		return responseObject;
	}
    
	private ResponseObject readMessage() throws Exception {
		InputArchive inputArchive = new BinaryInputArchive(new DataInputStream(is));
		//读取包头
		short magic = inputArchive.readShort();
		if (magic != MAGIC) {
			throw new IllegalStateException("Read magic code error,expect value :" + MAGIC + ",but actual value :" + magic);
		}
		byte packetType = inputArchive.readByte();
		
		if (packetType != FLAG_MESSAGE) {
			throw new IllegalStateException("Read packet type error,expect value :" + FLAG_MESSAGE + ",but actual value :" + packetType);
		}
		inputArchive.readByte();
		inputArchive.readInt();
		//读取包内容
		int msgId = inputArchive.readInt();
		if(msgId != lastRequestId) {
			throw new IllegalStateException("Read message id error,expect value :"+ lastRequestId + ",but actual value:" + msgId);
		}
		
		byte msgType = inputArchive.readByte();
		if(msgType != AbstractMessage.TYPE_RESPONSE) {
			throw new IllegalStateException("Read message type error,expect value :" + AbstractMessage.TYPE_RESPONSE + ",but actual value :" + msgType);
		}
		
		//message sequence
		int sequence = inputArchive.readInt();
		//error code
		int errorCode = inputArchive.readInt();
		//error description
		String errorDes = inputArchive.readString();
		ResponseObject responseObject = null;
		if (errorCode == 200) {
			responseObject = new ResponseObject();
			responseObject.decode(inputArchive);
		}else {
			throw new IllegalStateException("Read response object error,Error code :"+ errorCode + ",Error description:" + errorDes);
		}
		if (sequence != lastSequence) {
			throw new IllegalStateException("Read message sequence error,expect value :" + lastSequence + ",but actual value :" + sequence);
		}
		return responseObject;
	}
	
	public static void main(String[] args) throws Exception {
		ExchangeClient client = new ExchangeClient("172.19.60.134", 8601, 3000);
		client.start();
		ResponseObject responseObject = client.request(1, new RequestObject(1024));
		System.out.println(responseObject.getBytes().length);
		Thread.sleep(Integer.MAX_VALUE);
	}
}
