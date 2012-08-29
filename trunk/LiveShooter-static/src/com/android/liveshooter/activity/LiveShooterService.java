package com.android.liveshooter.activity;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Vector;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.xmlrpc.XmlRpcClient;
import android.media.MediaRecorder;
import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;
import android.view.SurfaceHolder;

public class LiveShooterService {
	public static final String IPaddr = "192.168.188.2";
	public static final int XMLRPCport = 8000;
	public static final int Socketport = 8001;
	public static final String Httpdir = "/live-shooter";
	public static final String FTPdir = "";
	public static final String FTPport = "21";
	public static final String FTPuser = "live";
	public static final String FTPpass = "shooter";
	public static final String SegmentExt = ".mp4";
	public static final String serverUrl = "http://" + IPaddr;
	private LocalSocket receiver;
	private LocalSocket sender;
	private LocalServerSocket lss;
	private MediaRecorder mMediaRecorder;
	private SurfaceHolder holder;
	private String SegmentName;
	private InputStream fis;
	private OutputStream out;
	private boolean flag;
	
	public LiveShooterService(SurfaceHolder holder) throws Exception{
		this.holder = holder;
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
		initializeVideo();
	}

	public boolean initializeVideo(){
		mMediaRecorder = new MediaRecorder();
		mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
		mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
		mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		mMediaRecorder.setVideoFrameRate(30);
		mMediaRecorder.setVideoSize(352, 288);
		mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
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

	public void releaseMediaRecorder() {
		if(mMediaRecorder !=null){
			mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	public void startVideoRecording() {
		try {
			init();
		} catch (Exception e3) {
			e3.printStackTrace();
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
				// 这些参数要对应我现在的视频设置，如果想变化的话需要去重新确定，
				// //当然不知道是不是不同的机器是不是一样，我这里只有一个HTC G7做测试。
				byte[] h264sps  = { 0x67, 0x42, 0x00, 0x0C, (byte) 0x96, 0x54,0x0B, 0x04, (byte) 0xA2 };
				byte[] h264pps  = { 0x68, (byte) 0xCE, 0x38, (byte) 0x80 };
				byte[] h264head = { 0, 0, 0, 1 };
				try {
					out.write(h264head);
					out.write(h264sps);
					out.write(h264head);
					out.write(h264pps);
					execute2(fis);
				} catch (Exception e) {
					Log.e("exception", e.getMessage());
				}
			}
		}.start();
	}
	
	private void execute2(InputStream in) throws Exception {
//		FileInputStream fileInput = new FileInputStream(new File("/mnt/sdcard/Arashmen/Bad Aibling.rar"));
		Log.i("msg","socket start...");
		Socket s = new Socket(IPaddr, Socketport);
		s.setKeepAlive(true);
		OutputStream netout = s.getOutputStream();
		OutputStream out = new DataOutputStream(new BufferedOutputStream(netout));
		Log.i("msg","out....");
		while(!flag){
			byte[] buff = new byte[in.available()];
			int num = in.read(buff);
			Log.i("msg", "num = " + num);
			if(num!=-1){
				out.write(buff);
			}else{
				flag = true;
			}
			Thread.sleep(100);
			Log.i("msg","success ");
		}
		out.flush();
		out.close();
		in.close();
		s.close();
	}

	private void execute(InputStream in) throws Exception {
		XmlRpcClient client = new XmlRpcClient(serverUrl + ":8000/");
		Vector<Object> params = new Vector<Object>();
		SegmentName = client.execute("genFilename", params).toString();
		String urlStr = serverUrl + "/live-shooter/" + SegmentName + ".html";
		Log.i("msg", "urlStr = " + urlStr);
		Vector<Object> paramsDemo = new Vector<Object>();
		paramsDemo.addElement(SegmentName);
		paramsDemo.add("Live Shooter");
		String finalResult = client.execute("genSegment", paramsDemo).toString();
		boolean isSuccess = uploadFile(IPaddr, FTPport, FTPuser, FTPpass, FTPdir, SegmentName + SegmentExt, in);
		int count = 0;
		while(!flag){
			Thread.sleep(5000);
			count ++;
			paramsDemo.addElement(SegmentName);
			paramsDemo.addElement(""+count);
			String finalResult1 = client.execute("updateSegment", paramsDemo).toString();
			boolean isSuccess1 = uploadFile(IPaddr, FTPport, FTPuser, FTPpass, FTPdir, SegmentName + SegmentExt, in);
		}
	}
	
	public boolean uploadFile(String url, String port, String username, String password, String path, String filename, InputStream input) {
		boolean returnValue = false;
		FTPClient ftp = new FTPClient();
		try {
			int reply;
		    ftp.connect(url);
			ftp.login(username, password);// 登录
			reply = ftp.getReplyCode();
			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
				return returnValue;
			}
			ftp.changeWorkingDirectory(path);
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			ftp.storeFile(filename, input);
			input.close();
			ftp.logout();
			returnValue = true;
		} catch (IOException e) {
			Log.i("msg", e.getMessage());
		} finally {
			if (ftp.isConnected()) {
				try {
					ftp.disconnect();
				} catch (IOException ioe) {
				}
			}
		}
		return returnValue;
    }
	
	public void shutdownInput() throws IOException{
		flag = true;
		receiver.shutdownInput();
		releaseMediaRecorder();
		lss.close();
		lss = null;
		sender.close();
		sender = null;
		receiver.close();
		receiver = null;
	}
	
	private void finishSegment()throws Exception{
		XmlRpcClient client = new XmlRpcClient(serverUrl + ":8000/");
		Vector<Object> paramsDemo = new Vector<Object>();
		paramsDemo.addElement(SegmentName);
		String finalResult = client.execute("finishRecord", paramsDemo).toString();
		Log.i("msg", "finishRecord = "+ finalResult);
	}
}
