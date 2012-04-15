package com.android.liveshooter.service;

public class LiveShooterException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public LiveShooterException(String detailMessage) {
		// TODO Auto-generated constructor stub
		super(detailMessage);
	}
	
	public LiveShooterException(String errorMessage, Throwable t) {
		super(errorMessage, t);
	}
	
	
	public String getMessage(){
		return super.getMessage();
	}

}
