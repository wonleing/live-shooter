package com.android.liveshooter.activity;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.xmlrpc.XmlRpcClient;

import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.SurfaceHolder;

import com.android.liveshooter.rtp.UDPProcessor;
import com.android.liveshooter.util.Tools;
import com.android.liveshooter.vo.NALU_t;

public class LiveShooterService {
	public static final String IPaddr = "192.168.188.2";
	public static final int XMLRPCport = 8000;
	public static final int Socketport = 8001;
	public static final String Httpdir = "/live-shooter";
	public static final String FTPdir = "";
	public static final String FTPport = "21";
	public static final String FTPuser = "live";
	public static final String FTPpass = "shooter";
	public static final String SegmentExt = ".h264";
	public static final String serverUrl = "http://" + IPaddr;
	private LocalSocket receiver, middleReceiver;
	private LocalSocket sender, endSender;
	private LocalServerSocket lss, transferServer;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder holder;
	private String VideoName;
	private InputStream fis, fis2;
	private OutputStream out;
	private FTPClient ftp;
	private Context ctx;
	
	//for Audio
	private AudioRecord audioRecord;
	
	private Thread innerThread;
	
	private int bufferSize;
	
	private byte[] buffer;
	
	private boolean stoping = false;
	
	private UDPProcessor processor;
	
	int seq_num = 0;
    int	bytes=0;
    // 时间戳增量
    float framerate = 25;
    int ts_current=0;
	int timestamp_increse=(int)(90000.0 / framerate); //+0.5);
	
	public LiveShooterService(SurfaceHolder holder, Context context) throws Exception{
		this.holder = holder;
		ctx = context;
	}
	
	private boolean initializeVideo(){
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		//mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);//MIC
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setVideoFrameRate(15);
		mMediaRecorder.setVideoSize(352, 288);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//MPEG_4_SP
		//mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);//AAC
		mMediaRecorder.setPreviewDisplay(holder.getSurface());
		mMediaRecorder.setMaxDuration(500000000);
		mMediaRecorder.setMaxFileSize(500000000);
		mMediaRecorder.setOutputFile(sender.getFileDescriptor());
		try {
			mMediaRecorder.prepare();
			mMediaRecorder.start();
		} catch (IOException exception) {
			releaseMediaRecorder();
			return false;
		}
		return true;
	}
	
	private void initializeAudio(){
		bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT);
		buffer = new byte[bufferSize];
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_CONFIGURATION_MONO, AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);
	}
	
	private void init() throws Exception {
		//for Video
		receiver = new LocalSocket();
		lss = new LocalServerSocket("VideoCamera");
		receiver.connect(new LocalSocketAddress("VideoCamera"));
		receiver.setReceiveBufferSize(50000000);
		receiver.setSendBufferSize(50000000);
		sender = lss.accept();
		sender.setReceiveBufferSize(50000000);
		sender.setSendBufferSize(50000000);
		//中转ServerSocket
		/*
		transferServer = new LocalServerSocket("Buffer");
		middleReceiver = new LocalSocket();
		middleReceiver.connect(new LocalSocketAddress("Buffer"));
		middleReceiver.setReceiveBufferSize(50000000);
		middleReceiver.setSendBufferSize(50000000);
		endSender = transferServer.accept();
		endSender.setReceiveBufferSize(50000000);
		endSender.setSendBufferSize(50000000);
		*/
	
		initializeVideo();
		initializeAudio();
	}
	
	public void prepare(){
		if(mMediaRecorder != null){
			try {
				mMediaRecorder.setPreviewDisplay(this.holder.getSurface());
				mMediaRecorder.prepare();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private boolean uploadFile(String url, String port, String username, String password, String path, String filename, InputStream input) {
		ftp = new FTPClient();
		try {
			int reply;
		    ftp.connect(url);
			ftp.login(username, password);
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return false;
			}
			ftp.changeWorkingDirectory(path);
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftp.storeFile(filename, input);
			input.close();
			ftp.logout();
		} catch (IOException e) {
			Log.e("liveshooter", e.getMessage());
		} finally {
            try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
    }
	
	private void releaseMediaRecorder() {
		if(mMediaRecorder !=null){
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}
	
	private void finishSegment()throws Exception{
		XmlRpcClient client = new XmlRpcClient(serverUrl + ":8000/");
		Vector<Object> params = new Vector<Object>();
		//Todo: Add oauth args to control if video upload to other video website
		params.addElement(VideoName);
		String finalResult = client.execute("finishRecord", params).toString();
		Log.i("liveshooter", "finishRecord = "+ finalResult);
	}
	
	/**
	 * 读取一个NAL单元
	 * @param nal
	 */
	public void readNAL(InputStream fis, UDPProcessor processor){
		byte[] h264head = { 0, 0, 0, 1 };
		byte[] temp = new byte[4];
    	
		while(true){
			try {
				//读取每个NAL的长度
				if(fis.available() < 4){
					continue;
				}
				fis.read(temp, 0, 4);
				int h264length = Tools.bytes2int(temp, 4);
				int offset = 0;
				System.arraycopy(h264head, 0, buffer, 0, h264head.length);
				while(offset < h264length)
				{
					int lost = h264length - offset;
					int k = 0;
					try {
						k = fis.read(buffer, offset + h264head.length, lost);
					} catch(Exception e){
						Log.e("liveshooter", e.getMessage());
					}
					if(k <= 0){
						continue;
					}
					offset += k;
				}
				Log.v("liveshooter", "h264headerlength: " + (h264length + h264head.length));
				//out.write(buffer, 0, h264length + h264head.length);
				//生成一个NAL
				NALU_t nal = new NALU_t();
				nal.len = h264length;
				nal.startcodeprefix_len = 4;
				System.arraycopy(buffer, 4, nal.buf, 0, nal.len);
				nal.forbidden_bit = nal.buf[0] & 0x80; //1 bit
				nal.nal_reference_idc = nal.buf[0] & 0x60; // 2 bit
				nal.nal_unit_type = (nal.buf[0]) & 0x1f;// 5 bit
				
				processNAL(nal, processor);
			} catch (IOException e) {
				e.printStackTrace();
				break;
			} 
		}
	}
	
	private void processNAL(NALU_t nal, UDPProcessor processor){
		//处理一个NAL
		int frame_size = 1500;
		byte[] sendbuf = new byte[frame_size];
		
		sendbuf[1] = (byte)(sendbuf[1]|96); // 负载类型号96
		sendbuf[0] = (byte)(sendbuf[0]|0x80); // 版本号,此版本固定为2
		sendbuf[1] = (byte)(sendbuf[1]&254); //标志位，由具体协议规定其值
		sendbuf[11] = 10;	//随即指定10，并在本RTP回话中全局唯一,java默认采用网络字节序号 不用转换
		if(nal.len <= 1400)
    	{
    		sendbuf[1] = (byte)(sendbuf[1]|0x80); // 设置rtp M位为1
    		//sendbug[2], sendbuf[3]赋值seq_num ++ 每发送一次rtp包增1
    		//sendbuf[3] = (byte) seq_num ++
    		System.arraycopy(Tools.intToByte(seq_num++), 0, sendbuf, 2, 2);
    		{
    			// 倒序
				byte tt = 0;
				tt = sendbuf[3];
				sendbuf[3] = sendbuf[2];
				sendbuf[2] = tt;
    		}
    		
    		// 设置NALU HEADER, 并将这个HEADER填入sendbuf[12]
    		sendbuf[12] =  (byte)(sendbuf[12]|((byte)nal.forbidden_bit)<<7);
    		sendbuf[12] =  (byte)(sendbuf[12]|((byte)(nal.nal_reference_idc>>5))<<5);
    		sendbuf[12] =  (byte)(sendbuf[12]|((byte)nal.nal_unit_type));
    		// 同理将sendbuf[13]赋给nalu_payload
    		System.arraycopy(nal.buf, 1, sendbuf, 13, nal.len-1);//去掉nalu头的nalu剩余类容写入sendbuf[13]开始的字符串
    		
    		ts_current = ts_current+timestamp_increse;
			//rtp_hdr.timestamp = ts_current;// htonl(ts_current) java默认网络字节序
    		System.arraycopy(Tools.intToByte(ts_current), 0, sendbuf, 4, 4);
    		{
    			// 倒序
				byte tt = 0;
				tt = sendbuf[4];
				sendbuf[4] = sendbuf[7];
				sendbuf[7] = tt;
				
				tt = sendbuf[5];
				sendbuf[5] = sendbuf[6];
				sendbuf[6] = tt;
    		}
			bytes = nal.len + 12 ;					//获sendbuf的长度,为nalu的长度(包含nalu头但取出起始前缀,加上rtp_header固定长度12个字节)
			//Send(sendbuf, bytes);//发送rtp包
		
    	}
    	else if(nal.len > 1400)
    	{
    		// 得到该nalu需要用多少长度为1400字节的rtp包来发送
    		int k = 0, l = 0;
    		k = nal.len/1400; //需要k个1400字节的rtp包
    		l = nal.len%1400; //最后一个rtp包需要装载的字节数
    		int t = 0; // 用于指示当前发送的第几个分片RTP包
    		ts_current = ts_current + timestamp_increse;
    		//rtp_hdr->timestamp=htonl(ts_current);
    		System.arraycopy(Tools.intToByte(ts_current), 0, sendbuf, 4, 4);
    		{
   				// 倒序
				byte tt = 0;
				tt = sendbuf[4];
				sendbuf[4] = sendbuf[7];
				sendbuf[7] = tt;
				
				tt = sendbuf[5];
				sendbuf[5] = sendbuf[6];
				sendbuf[6] = tt;
				
    		}
    		while(t <= k)
    		{
    			//rtp_hdr->seq_no = htons(seq_num ++);//序列号, 每发送一个rtp包增加1
    			//sendbuf[3] = (byte) seq_num ++;
    			System.arraycopy(Tools.intToByte(seq_num++), 0, sendbuf, 2, 2);
    			{
    				// 倒序
    				byte tt = 0;
    				tt = sendbuf[3];
    				sendbuf[3] = sendbuf[2];
    				sendbuf[2] = tt;
    			}
    			if(0 == t)
    			{
    				// 设置rtp M位
    				sendbuf[1] = (byte)(sendbuf[1]&0x7F); // M=0
    				// 设置FU INDICATOR,并将这个HEADER填入sendbuf[12]
            		sendbuf[12] =  (byte)(sendbuf[12]|((byte)nal.forbidden_bit)<<7);
            		sendbuf[12] =  (byte)(sendbuf[12]|((byte)(nal.nal_reference_idc>>5))<<5);
            		sendbuf[12] =  (byte)(sendbuf[12]|(byte)(28));
            		
            		// 设置FU HEADER,并将这个HEADER填入snedbuf[13]
            		sendbuf[13] = (byte)(sendbuf[13]&0xBF);//E=0
            		sendbuf[13] = (byte)(sendbuf[13]&0xDF);//R=0
            		sendbuf[13] = (byte)(sendbuf[13]|0x80);//S=1
            		sendbuf[13] = (byte)(sendbuf[13]|((byte)nal.nal_unit_type));
            		
            		// 同理将sendbuf[14]赋给nalu_playload
            		System.arraycopy(nal.buf, 1, sendbuf, 14, 1400);
            		bytes = 1400 + 14;
            		//Send(sendbuf, bytes);
            		processor.sendPacket(sendbuf, bytes);
            		t++;
    			}
    			// 发送一个需要分片的NALU的非第一个分片，清零FU HEADER 的S位，如果该分片是该NALU的最后一个分片，置FU HEADER的E位
    			else if(k == t) //发送的是最后一个分片，注意最后一个分片的长度可能超过1400字节（当l>1386时）
    			{
    				//  设置rtp M位,当前床书的是最后一个分片时该位置1
    				sendbuf[1] = (byte)(sendbuf[1]|0x80);
    				// 设置FU INDICATOR,并将这个HEADER填入sendbuf[12]
    				sendbuf[12] =  (byte)(sendbuf[12]|((byte)nal.forbidden_bit)<<7);
            		sendbuf[12] =  (byte)(sendbuf[12]|((byte)(nal.nal_reference_idc>>5))<<5);
            		sendbuf[12] =  (byte)(sendbuf[12]|(byte)(28));
            		
            		//设置FU HEADER,并将这个HEADER填入sendbuf[13]
            		sendbuf[13] = (byte) (sendbuf[13]&0xDF); //R=0
            		sendbuf[13] = (byte) (sendbuf[13]&0x7F); //S=0
            		sendbuf[13] = (byte) (sendbuf[13]|0x40); //E=1
            		sendbuf[13] = (byte) (sendbuf[13]|((byte)nal.nal_unit_type));
            		
            		// 将nalu的最后神域的l-1(去掉了一个字节的nalu头)字节类容写入sendbuf[14]开始的字符串
            		System.arraycopy(nal.buf, t*1400+1, sendbuf, 14, l-1);
            		bytes = l-1+14;
            		//Send(sendbuf, bytes);
            		processor.sendPacket(sendbuf, bytes);
            		t++;
    			}
    			else if(t < k && 0 !=t)
    			{
    				//设置rtp M位
    				sendbuf[1] = (byte)(sendbuf[1]&0x7F); // M=0
    				
    				// 设置FU INDICATOR,并将这个HEADER填入sendbuf[12]
    				sendbuf[12] =  (byte)(sendbuf[12]|((byte)nal.forbidden_bit)<<7);
            		sendbuf[12] =  (byte)(sendbuf[12]|((byte)(nal.nal_reference_idc>>5))<<5);
            		sendbuf[12] =  (byte)(sendbuf[12]|(byte)(28));
            		
            		//设置FU HEADER,并将这个HEADER填入sendbuf[13]
             		sendbuf[13] = (byte) (sendbuf[13]&0xDF); //R=0
            		sendbuf[13] = (byte) (sendbuf[13]&0x7F); //S=0
            		sendbuf[13] = (byte) (sendbuf[13]&0xBF); //E=0
            		sendbuf[13] = (byte) (sendbuf[13]|((byte)nal.nal_unit_type));
            		
            		System.arraycopy(nal.buf, t*1400+1, sendbuf, 14, 1400);//去掉起始前缀的nalu剩余内容写入sendbuf[14]开始的字符串。
					bytes=1400+14;						//获得sendbuf的长度,为nalu的长度（除去原NALU头）加上rtp_header，fu_ind，fu_hdr的固定长度14字节
					//Send(sendbuf, bytes);
            		processor.sendPacket(sendbuf, bytes);
					t++;
    			}
    		}
    	}
	}

	public void startVideoRecording() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			processor = new UDPProcessor();
			processor.init(IPaddr, Socketport);
		} catch (Exception e) {
			//if (!Sipdroid.release) e.printStackTrace();
			return;
		}		
		
		new Thread() {
			public void run() {
				
				try {
					fis = receiver.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
//				try {
//					out = endSender.getOutputStream();
//				} catch (IOException e1) {
//					e1.printStackTrace();
//				}
				// 这些固定参数仅适用于HTC G7, 将来要改成动态生成的
				byte[] h264sps  = { 0x67, 0x42, 0x00, 0x0C, (byte) 0x96, 0x54,0x0B, 0x04, (byte) 0xA2 };
				byte[] h264pps  = { 0x68, (byte) 0xCE, 0x38, (byte) 0x80 };
				byte[] h264head = { 0, 0, 0, 1 };
				byte[] temp = new byte[32];
				try {
					while(true){					
						fis.read(temp, 0, 8);
						int len = Tools.bytes2int(temp, 4);
						String type = new String(temp, 4, 4);
						Log.i("liveshooter", "Video type is: " + type);
						//if(len <= 0) {
						//	Thread.sleep(0.1);
						//	continue;
						//}
						if(temp[4] == 'a' && temp[5] == 'v' && temp[6] == 'c' && temp[7] == 'C'){ //find the avcC box
							fis.skip(6);
							fis.read(temp, 0, 2);
							int spslen = Tools.bytes2int(temp, 2);
							byte[] sps = new byte[spslen];
							fis.read(sps, 0, spslen);
							fis.skip(1);
							fis.read(temp, 0, 2);
							int ppslen = Tools.bytes2int(temp, 2);
							byte[] pps = new byte[ppslen];
							fis.read(pps, 0, ppslen);
							fis.skip(len - 8 - 8 - spslen - 3 - ppslen);
						}
						else{
							//find the mdat box
							if(temp[4] == 'm' && temp[5] == 'd' && temp[6] == 'a' && temp[7] == 't'){
								break;
							}
							fis.skip(len - 8);
						}
					}
					
					//先发送PPS和SPS的RTP
					NALU_t nal = new NALU_t();
					nal.len = h264sps.length + h264pps.length + 4;
					nal.startcodeprefix_len = 4;
					System.arraycopy(h264sps, 0, nal.buf, 0, h264sps.length);
					System.arraycopy(h264head, 0, nal.buf, h264sps.length, 4);
					System.arraycopy(h264pps, 0, nal.buf, h264sps.length + 4, h264pps.length);
					nal.forbidden_bit = nal.buf[0] & 0x80; //1 bit
					nal.nal_reference_idc = nal.buf[0] & 0x60; // 2 bit
					nal.nal_unit_type = (nal.buf[0]) & 0x1f;// 5 bit
					processNAL(nal, processor);
					//开始发送H264视频流
					readNAL(fis, processor);
					
//					int frame_size = 1400;
//					byte[] buffer = new byte[frame_size + 14];
//					buffer[12] = 4;
//					RtpPacket rtp_packet = new RtpPacket(buffer, 0);
//					int seqn = 0;
//					int num,number = 0,src,dest,len = 0,head = 0,lasthead = 0,lasthead2 = 0,cnt = 0,stable = 0;
//					long now,lasttime = 0;
//					double avgrate = 45000;
//					double avglen = avgrate/20;
//					
//					rtp_packet.setPayloadType(103);
//					while(true){
//						num = -1;
//						try {
//							num = fis.read(buffer,14+number,frame_size-number);
//						} catch (IOException e) {
//							break;
//						}
//						if (num < 0) {
//							try {
//								sleep(20);
//							} catch (InterruptedException e) {
//								break;
//							}
//							continue;							
//						}
//						number += num;
//						head += num;
//						try {
//							now = SystemClock.elapsedRealtime();
//							if (lasthead != head+fis.available() && ++stable >= 5 && now-lasttime > 700) {
//								if (cnt != 0 && len != 0)
//									avglen = len/cnt;
//								if (lasttime != 0) {
//									fps = (int)((double)cnt*1000/(now-lasttime));
//									avgrate = (double)((head+fis.available())-lasthead2)*1000/(now-lasttime);
//								}
//								lasttime = now;
//								lasthead = head+fis.available();
//								lasthead2 = head;
//								len = cnt = stable = 0;
//							}
//						} catch (IOException e1) {
//							break;
//						}
//						
//    					for (num = 14; num <= 14+number-2; num++)
//							if (buffer[num] == 0 && buffer[num+1] == 0) break;
//						if (num > 14+number-2) {
//							num = 0;
//							rtp_packet.setMarker(false);
//						} else {	
//							num = 14+number - num;
//							rtp_packet.setMarker(true);
//						}
//						
//						rtp_packet.setSequenceNumber(seqn++);
//			 			rtp_packet.setPayloadLength(number-num+2);
//			 			if (seqn > 10) try {
//			 				rtp_socket.send(rtp_packet);
//    			 			len += number-num;
//			 			} catch (IOException e) {
//			 				//if (!Sipdroid.release) e.printStackTrace();
//			 				break;
//			 			}
//						
//			 			if (num > 0) {
//				 			num -= 2;
//				 			dest = 14;
//				 			src = 14+number - num;
//				 			if (num > 0 && buffer[src] == 0) {
//				 				src++;
//				 				num--;
//				 			}
//				 			number = num;
//				 			while (num-- > 0)
//				 				buffer[dest++] = buffer[src++];
//							buffer[12] = 4;
//							
//							cnt++;
//							try {
//								if (avgrate != 0)
//									Thread.sleep((int)(avglen/avgrate*1000));
//							} catch (Exception e) {
//								break;
//							}
//    			 			rtp_packet.setTimestamp(SystemClock.elapsedRealtime()*90);
//			 			} else {
//			 				number = 0;
//							buffer[12] = 0;
//			 			}
//			 			if (change) {
//			 				change = false;
//			 				long time = SystemClock.elapsedRealtime();
//			 				
//	    					try {
//								while (fis.read(buffer,14,frame_size) > 0 &&
//										SystemClock.elapsedRealtime()-time < 3000);
//							} catch (Exception e) {
//							}
//			 				number = 0;
//							buffer[12] = 0;
//			 			}
//			 			
//					}
					
//					out.write(h264head);
//					out.write(h264sps);
//					out.write(h264head);
//					out.write(h264pps);
//					while(true){
//						try {
//							//读取每个NAL的长度
//							if(fis.available() < 4){
//								continue;
//							}
//							fis.read(temp, 0, 4);
//							int h264length = Tools.bytes2int(temp, 4);
//							int offset = 0;
//							System.arraycopy(h264head, 0, buffer, 0, h264head.length);
//							while(offset < h264length)
//							{
//								int lost = h264length - offset;
//								int k = 0;
//								try {
//									k = fis.read(buffer, offset + h264head.length, lost);
//								} catch(Exception e){
//									Log.e("liveshooter", e.getMessage());
//								}
//								if(k <= 0){
//									continue;
//								}
//								offset += k;
//							}
//							Log.v("liveshooter", "h264headerlength: " + (h264length + h264head.length));
//							out.write(buffer, 0, h264length + h264head.length);
//						} catch (IOException e) {
//							e.printStackTrace();
//							break;
//						} 
//					}
				} catch (Exception e) {
					Log.e("liveshooter", e.getMessage());
				}
			}
		}.start();
		
		innerThread = new Thread(new Runnable(){

			@Override
			public void run() {
				while(!stoping){
					int len = audioRecord.read(buffer, 0, bufferSize);
					if(len > 0){
						
					}
					else{
						
					}
				}
			}
			
		});
		
		//innerThread.start();
	}
	
	/**
	 * Use Thread to start Ftp transfer data for it is a block process
	 */
	public void startVideoUpload(){
		new Thread(){
			public void run(){
				try {
					fis2 = middleReceiver.getInputStream();
					XmlRpcClient client = new XmlRpcClient(serverUrl + ":8000/");
					Vector<Object> params = new Vector<Object>();
					// TBD: input video title and description from UI
					params.add("My Video Title");
					params.add("My Video Description");
					VideoName = client.execute("startRecord", params).toString();
					String urlStr = serverUrl + "/live-shooter/" + VideoName + ".html";
					Log.i("liveshooter", "Returned playback URL is: " + urlStr);
					uploadFile(IPaddr, FTPport, FTPuser, FTPpass, FTPdir, VideoName + SegmentExt, fis2);
				} catch (Exception e1) {
					Log.e("liveshooter", "Fail to upload video stream");
					e1.printStackTrace();
				}
			}
		}.start();
	}
	
	public void shutdownInput() throws Exception{
		receiver.shutdownInput();
		releaseMediaRecorder();
		finishSegment();
		lss.close();
		lss = null;
		sender.close();
		sender = null;
		receiver.close();
		receiver = null;
		if(ftp != null){
			ftp.logout();
		}
	}
}