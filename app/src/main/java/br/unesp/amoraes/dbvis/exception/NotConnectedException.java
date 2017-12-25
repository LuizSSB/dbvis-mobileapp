package br.unesp.amoraes.dbvis.exception;

public class NotConnectedException extends LogicalException {

	private static final long serialVersionUID = -3559076952235558752L;
	
	public NotConnectedException(){
		super();
	}
	
	public NotConnectedException(String msg) {
		super(msg);
	}
	
	public NotConnectedException(String msg, Throwable t) {
		super(msg, t);
	}

}
