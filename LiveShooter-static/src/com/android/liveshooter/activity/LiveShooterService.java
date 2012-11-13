package com.android.liveshooter.activity;
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
import android.util.Log;
import android.view.SurfaceHolder;
import com.android.liveshooter.util.Tools;

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

	public void startVideoRecording() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread() {
			public void run() {
				int frame_size = 10240;
				byte[] buffer = new byte[frame_size];
				try {
					fis = receiver.getInputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					out = endSender.getOutputStream();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
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
							out.write(buffer, 0, h264length + h264head.length);
						} catch (IOException e) {
							e.printStackTrace();
							break;
						} 
					}
				} catch (Exception e) {
					Log.e("liveshooter", e.getMessage());
				}
			}
		}.start();
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