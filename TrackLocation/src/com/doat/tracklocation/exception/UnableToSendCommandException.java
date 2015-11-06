package com.doat.tracklocation.exception;

public class UnableToSendCommandException extends Exception {

	private static final long serialVersionUID = -6387459722866798512L;

	public UnableToSendCommandException() {
		super();
	}

	public UnableToSendCommandException(String detailMessage, Throwable throwable) {
		super(detailMessage, throwable);
	}

	public UnableToSendCommandException(String detailMessage) {
		super(detailMessage);
	}

	public UnableToSendCommandException(Throwable throwable) {
		super(throwable);
	}

}
