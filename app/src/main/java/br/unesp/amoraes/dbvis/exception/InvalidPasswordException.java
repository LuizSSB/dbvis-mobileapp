package br.unesp.amoraes.dbvis.exception;

public class InvalidPasswordException extends LogicalException {

	private static final long serialVersionUID = -5738195293852372452L;

	public InvalidPasswordException(){
		super();
	}
	
	public InvalidPasswordException(String msg) {
		super(msg);
	}
	
	public InvalidPasswordException(String msg, Throwable t) {
		super(msg, t);
	}

}
