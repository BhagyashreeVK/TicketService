package com.walmart.ticketservice.exception;

/**
 * Application exception 
 * @author bkulkar
 *
 */
@SuppressWarnings("serial")
public class ApplicationException extends Exception {
	
	public ApplicationException() {
		super();
	}
	
	public ApplicationException(String message) {
		super(message);
	}

}
