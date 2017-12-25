package br.unesp.amoraes.dbvis.exception;

public class AlreadyConnectedException extends LogicalException {

	private static final long serialVersionUID = -3559076952235558752L;
	
	public AlreadyConnectedException(){
		super();
	}
	
	public AlreadyConnectedException(String msg) {
		super(msg);
	}
	
	public AlreadyConnectedException(String msg, Throwable t) {
		super(msg, t);
	}

}
