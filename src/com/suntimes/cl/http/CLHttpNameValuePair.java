package com.suntimes.cl.http;

import java.io.File;
import java.net.URLEncoder;

import org.apache.http.message.BasicNameValuePair;

public class CLHttpNameValuePair extends BasicNameValuePair {
	
	public static enum Type{
		NORMAL, FILE
	}

	private boolean encode = false;
	private Type type = Type.NORMAL;
	private File file;
	
	public CLHttpNameValuePair(String name, Object value) {
		this(name, value, false);
	}
	
	public CLHttpNameValuePair(String name, Object value, boolean encode) {
		super(name, value.toString());
		this.encode = encode;
		if(value instanceof File){
			this.file = (File)value;
		}
	}

	public boolean isEncode() {
		return encode;
	}

	public void setEncode(boolean encode) {
		this.encode = encode;
	}
	
	public String getEncodeValue(){
		try{
			return URLEncoder.encode(this.getValue(), "utf-8");
		}catch (Exception e) {
			e.printStackTrace();
			return this.getValue();
		}
	}

	public Type getType() {
		return type;
	}

	public File getFile() {
		return file;
	}
	
	
}
