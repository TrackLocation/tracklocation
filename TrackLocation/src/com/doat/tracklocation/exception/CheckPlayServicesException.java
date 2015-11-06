package com.doat.tracklocation.exception;

public class CheckPlayServicesException extends Exception {

	private static final long serialVersionUID = -5419500632416004950L;
	private int resultCode;

	public CheckPlayServicesException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CheckPlayServicesException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
		// TODO Auto-generated constructor stub
	}

	public CheckPlayServicesException(String detailMessage) {
		super(detailMessage);
		// TODO Auto-generated constructor stub
	}

	public CheckPlayServicesException(String detailMessage, int resultCode) {
		super(detailMessage);
		this.resultCode = resultCode;
	}

	public int getResultCode() {
		return resultCode;
	}

	public CheckPlayServicesException(Throwable throwable) {
		super(throwable);
		// TODO Auto-generated constructor stub
	}

}
