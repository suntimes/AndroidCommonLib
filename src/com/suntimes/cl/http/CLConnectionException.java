package com.suntimes.cl.http;

public class CLConnectionException extends Exception{
	
	private static final long serialVersionUID = 1L;
	private int statusCode;
	private String url;
	
	

	public int getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}

	public CLConnectionException(String url, int statusCode) {
		super();
		this.statusCode = statusCode;
		this.url = url;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
}
