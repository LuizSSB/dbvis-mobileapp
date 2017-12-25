package br.unesp.amoraes.dbvis.exception;

public abstract class LogicalException extends Exception {

	private static final long serialVersionUID = 186545842541316627L;
	
	public LogicalException(){
		super();
	}
	
	public LogicalException(String msg){
		super(msg);
	}
	
	public LogicalException(String msg, Throwable t){
		super(msg,t);
	}
	
}
