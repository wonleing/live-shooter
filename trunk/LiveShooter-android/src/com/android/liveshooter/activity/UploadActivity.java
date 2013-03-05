package com.android.liveshooter.activity;

import java.io.File;

import com.android.liveshooter.socket.FTPServer;
import com.android.liveshooter.socket.XMLRPCServer;

import it.sauronsoftware.ftp4j.FTPDataTransferListener;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ProgressBar;

public class UploadActivity extends Activity implements FTPDataTransferListener{

	private ProgressBar bar;
	
	private String uploadFile;
	
	private long count; //上传文件总大小
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.upload);
        
        Bundle bundle = this.getIntent().getExtras();
        if(bundle != null){
        	uploadFile = bundle.getString("path");
        	
        	File file = new File(uploadFile);
    		count = file.length();
        }
        
        bar  = (ProgressBar)findViewById(R.id.progress);
        
        String ftpurl = this.getResources().getString(R.string.ftpurl);
        String ftpuser = this.getResources().getString(R.string.ftpuser);
        String ftppass = this.getResources().getString(R.string.ftppas);
        
        UploadService service = new UploadService();
        service.execute(ftpurl, ftpuser, ftppass, uploadFile, this);
	}

	@Override
	public void aborted() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void completed() {
		Log.i("Upload", "Completed");
		String url = this.getResources().getString(R.string.rpcurl);
		String name = new File(uploadFile).getName();
		//int index = name.lastIndexOf('.');
		Object iret = new XMLRPCServer(url).finishUpload(name);
		if(iret == (Boolean)false){
			
		}
	}

	@Override
	public void failed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void started() {
		bar.setProgress(0);
		bar.setMax((int) count);
		Log.i("Upload", count + "");
	}

	@Override
	public void transferred(int done) {
		Log.i("Done", done + "");
		if(!bar.isIndeterminate()){
			bar.setProgress(done);
			Log.i("Real Done", done + "");
		}
	}
	
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
}
