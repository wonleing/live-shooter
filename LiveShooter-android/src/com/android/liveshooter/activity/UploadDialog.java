package com.android.liveshooter.activity;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import android.app.Dialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.android.liveshooter.socket.FTPServer;
import com.android.liveshooter.socket.XMLRPCServer;

public class UploadDialog extends Dialog implements FTPDataTransferListener{
	
	private String uploadFile;
	
	private Context ctx;
	
	private ProgressBar bar;
	
	private long count; //上传文件总大小

	public UploadDialog(Context context, String filepath) {
		super(context);
		ctx = context;
		uploadFile = filepath;
		
		File file = new File(uploadFile);
		count = file.length();
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.upload);
        
        bar  = (ProgressBar)findViewById(R.id.progress);
        
        String ftpurl = ctx.getResources().getString(R.string.ftpurl);
        String ftpuser = ctx.getResources().getString(R.string.ftpuser);
        String ftppass = ctx.getResources().getString(R.string.ftppas);
        
        UploadService service = new UploadService();
        service.execute(ftpurl, ftpuser, ftppass, uploadFile, this);
	}
	
	@SuppressWarnings("unused")
	private class UploadService extends AsyncTask<Object, Object, Boolean>{

		@Override
		protected Boolean doInBackground(Object... params) {
			String ftputl = (String)params[0];
			String username = (String)params[1];
			String password = (String)params[2];
			String filepath = (String)params[3];
			FTPDataTransferListener listener = (FTPDataTransferListener)params[4];
			FTPServer ftp = new FTPServer();
			ftp.setParams(ftputl, username, password);
			ftp.addFTPDataTransferListener(listener);
			boolean iret = ftp.startUpload(filepath);
			return iret;
		}
		
		protected void onPostExecute(Boolean result){
			
		}
		
	}

	@Override
	public void aborted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completed() {
		String url = ctx.getResources().getString(R.string.rpcurl);
		String name = new File(uploadFile).getName();
		int index = name.lastIndexOf('.');
		Object iret = new XMLRPCServer(url).finishUpload(name.substring(0, index));
	}

	@Override
	public void failed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void started() {
		bar.setMax((int) count);
	}

	@Override
	public void transferred(int done) {
		if(!bar.isIndeterminate()){
			bar.setProgress(done);
		}
	}

}
