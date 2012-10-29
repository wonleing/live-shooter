package com.android.liveshooter.activity;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.xmlrpc.XmlRpcClient;

import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;

import com.android.liveshooter.util.Tools;

public class LiveShooterService {
	public static final String IPaddr = "192.168.1.108";
	public static final int XMLRPCport = 8000;
	public static final int Socketport = 8001;
	public static final String Httpdir = "/live-shooter";
	public static final String FTPdir = "";
	public static final String FTPport = "21";
	public static final String FTPuser = "live";
	public static final String FTPpass = "shooter";
	public static final String SegmentExt = ".mp4";
	public static final String serverUrl = "http://" + IPaddr;
	private LocalSocket receiver, middleReceiver;
	private LocalSocket sender, endSender;
	private LocalServerSocket lss, transferServer;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder holder;
	private String VideoName;
	private InputStream fis, fis2;
	private OutputStream out;
	public LiveShooterService(SurfaceHolder holder) throws Exception{
		this.holder = holder;
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
	
	private void init() throws Exception {
		receiver = new LocalSocket();
		lss = new LocalServerSocket("VideoCamera");
		receiver.connect(new LocalSocketAddress("VideoCamera"));
		receiver.setReceiveBufferSize(50000000);
		receiver.setSendBufferSize(50000000);
		sender = lss.accept();
		sender.setReceiveBufferSize(50000000);
		sender.setSendBufferSize(50000000);
		
		//中转ServerSocket
		transferServer = new LocalServerSocket("Buffer");
		middleReceiver = new LocalSocket();
		middleReceiver.connect(new LocalSocketAddress("Buffer"));
		middleReceiver.setReceiveBufferSize(50000000);
		middleReceiver.setSendBufferSize(50000000);
		endSender = transferServer.accept();
		endSender.setReceiveBufferSize(50000000);
		endSender.setSendBufferSize(50000000);
		
		initializeVideo();
	}

	private boolean uploadFile(String url, String port, String username, String password, String path, String filename, InputStream input) {
		FTPClient ftp = new FTPClient();
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
			Log.i("msg", e.getMessage());
		} finally {
            try {
				ftp.disconnect();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return true;
    }

	private String execute(InputStream in) throws Exception {
		XmlRpcClient client = new XmlRpcClient(serverUrl + ":8000/");
		Vector<Object> params = new Vector<Object>();
		// TBD: input video title and description from UI
		params.add("My Video Title");
		params.add("My Video Description");
		VideoName = client.execute("startRecord", params).toString();
		String urlStr = serverUrl + "/live-shooter/" + VideoName + ".html";
		Log.i("msg", "Returned playback URL is: " + urlStr);
		if (uploadFile(IPaddr, FTPport, FTPuser, FTPpass, FTPdir, VideoName + SegmentExt, in)){
			return urlStr;
		} else {
		return "";
		}
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
		Log.i("msg", "finishRecord = "+ finalResult);
	}

	public void startVideoRecording() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread() {
			public void run() {
				int frame_size = 2048 * 2048;
				byte[] buffer = new byte[1024 * 2048];
				int num, number = 0;
				try {
					fis = receiver.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				number = 0;
				// 如果是H264或是MPEG_4_SP的就要在这里找到相应的设置参数的流
				// avcC box H264的设置参数
				// esds box MPEG_4_SP 的设置参数
				// 其实 如果分辨率 等数值不变的话，这些参数是不会变化的，
				// 那么我就只需要在第一次运行的时候确定就可以了
				/*
				while (true) {
					try {
						num = fis.read(buffer, number, frame_size);
						number += num;
						if (num < frame_size) {
							break;
						}
					} catch (Exception e) {
						break;
					}
				}
				number = 0;
				// 重新启动捕获，以获取视频流
				DataInputStream dis = new DataInputStream(fis);
				// 读取最前面的32个自己的空头
				try {
					dis.read(buffer, 0, 32);
					out = sender.getOutputStream();
				} catch (IOException e2) {
					e2.printStackTrace();
				}
				*/
				try {
					out = endSender.getOutputStream();
					fis2 = middleReceiver.getInputStream();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					if (execute(fis2)!=""){
						//Todo: Post video title, description etc to SNS.	
						//Todo: Show comment feed back. (Pop up?)
					} else {
						Log.i("err", "Fail to upload video stream");
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				// 这些参数要对应我现在的视频设置，如果想变化的话需要去重新确定，
				// //当然不知道是不是不同的机器是不是一样，我这里只有一个HTC G7做测试。
				byte[] h264sps  = { 0x67, 0x42, 0x00, 0x0C, (byte) 0x96, 0x54,0x0B, 0x04, (byte) 0xA2 };
				byte[] h264pps  = { 0x68, (byte) 0xCE, 0x38, (byte) 0x80 };
				byte[] h264head = { 0, 0, 0, 1 };
				byte[] temp = new byte[32];
				try {
					while(true){					
						fis.read(temp, 0, 8);
						int len = Tools.bytes2int(temp, 4);
						String type = new String(temp, 4, 4);
						Log.i("Type", type);
						if(len <= 0) {
							Thread.sleep(5);
							continue;
						}
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
							//'mdat'
							if(temp[4] == 'm' && temp[5] == 'd' && temp[6] == 'a' && temp[7] == 't'){ //find the mdat box
								break;
							}
							fis.skip(len - 8);
						}
					}
					
					out.write(h264head);
					out.write(h264sps);
					out.write(h264head);
					out.write(h264pps);
					
					while(true){
						try {
							//读取每个NAL的长度
							if(fis.available() < 4){
								continue;
							}
							fis.read(temp, 0, 4);
							int h264length = Tools.bytes2int(temp, 4);
							int offset = 0;
							while(offset < h264length)
							{
								int lost = h264length - offset;
								int k = 0;
								try {
									k = fis.read(buffer, offset, lost);
								} catch(Exception e){
									return;
								}
								if(k <= 0){
									continue;
								}
								offset += k;
							}
							
							out.write(buffer, 0, h264length + h264head.length);
			
						} catch (IOException e) {
							e.printStackTrace();
							break;
						} 
					}
				} catch (Exception e) {
					Log.i("exception", e.getMessage());
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
	}
}


