package com.clinicalinformatics.app.helper;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
public class SQLManagerException extends Exception {

	private static final long serialVersionUID = 1L;
	public SQLManagerException() { super(); }
	public SQLManagerException(String message) { super(message); }
	public SQLManagerException(String message, Throwable cause) { super(message, cause); }
	public SQLManagerException(Throwable cause) { super(cause); }
}
