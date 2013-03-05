package com.android.liveshooter.socket;

import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferListener;

import java.io.File;

import android.util.Log;

public class FTPServer{

	private FTPClient ftp;
	
	private String ftpDir = "/";
	
	private String ftpUrl;
	
	private String name;
	
	private String password;
	
	private FTPDataTransferListener listener;
	
	public FTPServer(){
		ftp = new FTPClient();
	}
	
	public void setParams(String url, String name, String pass){
		this.ftpUrl = url;
		this.name = name;
		this.password = pass;
	}
	
	public void addFTPDataTransferListener(FTPDataTransferListener l){
		listener = l;
	}
	
	public boolean startUpload(String filepath){
		try {
			ftp.connect(ftpUrl);
			ftp.setPassive(true);
			ftp.login(name, password);
			ftp.setType(FTPClient.TYPE_AUTO);
			//ftp.changeDirectory(ftpDir);
			ftp.upload(new File(filepath), listener);
			return true;
		} catch(Exception e){
			Log.i("Error", e.getMessage());
		}
//		try {
//			ftp.connect(ftpUrl);
//			ftp.login(name, password);
//			int reply = ftp.getReplyCode();
//			if (!FTPReply.isPositiveCompletion(reply)) {
//				ftp.disconnect();
//				return false;
//			}
//			ftp.changeWorkingDirectory(ftpDir);
//			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
//			InputStream is = new FileInputStream(filepath);
//			ftp.storeFile(new File(filepath).getName(), is);
//			ftp.logout();
//			is.close();
//			
//		} catch (SocketException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return false;
	}

}
