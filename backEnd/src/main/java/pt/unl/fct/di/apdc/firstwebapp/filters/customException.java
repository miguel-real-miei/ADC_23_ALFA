package pt.unl.fct.di.apdc.firstwebapp.filters;

import java.io.IOException;

import javax.ws.rs.core.Response.Status;

public class customException extends IOException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final Status code;
	
	public customException(Status code) {
		super();
		this.code = code;
	}
	
	public Status getCode() {
		return code;
	}

}
