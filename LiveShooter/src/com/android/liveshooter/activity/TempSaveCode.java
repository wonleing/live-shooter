package com.android.liveshooter.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.xmlrpc.XmlRpcClient;

import com.android.liveshooter.R;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class TempSaveCode extends Activity{
	private File myRecAudioFile;
	private SurfaceView mSurfaceView;
	private SurfaceHolder mSurfaceHolder;
	private Button buttonStart;
	private Button buttonStop;
	private File dir;
	private MediaRecorder recorder;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		setContentView(R.layout.video);
//		
//		mSurfaceView = (SurfaceView) findViewById(R.id.videoView);
//		mSurfaceHolder = mSurfaceView.getHolder();
//		mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//		
//		buttonStart = (Button) findViewById(R.id.start);
//		buttonStop = (Button) findViewById(R.id.stop);
//		
//		buttonStart.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//
//				try {
//					
//					MediaRecorder mediaRecorder = new MediaRecorder();
//					mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//					mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
//					mediaRecorder.setVideoSize(480, 320);
//					mediaRecorder.setVideoFrameRate(3); // 每秒3帧
//					mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT);
//					mediaRecorder.setPreviewDisplay(mSurfaceView.getHolder().getSurface());
//					Socket socket = new Socket("192.168.1.109", 8000);
//					ParcelFileDescriptor pfd = ParcelFileDescriptor.fromSocket(socket);
//					
//					mediaRecorder.setOutputFile(pfd.getFileDescriptor());
//					mediaRecorder.prepare();// 预期准备
//				    mediaRecorder.start();// 开始刻录
//					
//				    
//					XMLRpcExecutor();
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				
//			}
//		});

		buttonStop.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

			}
		});
	}
	
	private void XMLRpcExecutor() throws Exception{
		String filename = "/mnt/sdcard/Riders.mp4";
		
		
	    XmlRpcClient client = new XmlRpcClient( "http://192.168.1.109:8000/");
	    Vector params = new Vector();
	    String result = client.execute( "genFilename", params ).toString();
        String urlStr = "http://192.168.1.109/live-shooter/"+result+".html";
        System.out.println(urlStr);
        Log.i("msg","urlStr = " + urlStr);
        
        File file = new File(filename);
        InputStream in = new FileInputStream(file);
        boolean isSuccess = uploadFile("192.168.1.109", "21", "arashmen", "6711542845", "pub/", result+".mp4", in);
        Log.i("msg","isSuccess = " + isSuccess);
        
        Vector  paramsDemo = new Vector();
        paramsDemo.addElement(result);
        String finalResult = client.execute( "genSegment", paramsDemo ).toString();
        Log.i("msg","finalResult = " + finalResult);
        
        if(Boolean.valueOf(finalResult)){
        	Uri uri = Uri.parse(urlStr);
            Intent intent= new Intent();        
            intent.setAction(Intent.ACTION_VIEW);    
            intent.setData(uri);  
            startActivity(intent);
        }
	}
	
	public boolean uploadFile(String url, String port, String username, String password, String path, String filename, InputStream input) {
		  
        boolean returnValue = false;

        FTPClient ftp = new FTPClient();

        try {

            int reply;

            // 判断是否使用默认端口
            if ("21".equals(port)) {
                ftp.connect(url);
            } else {
                int portNO = Integer.parseInt(port);
                ftp.connect("arashmen", 21);// 连接FTP服务器
            }
 
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
            e.printStackTrace();
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

}
