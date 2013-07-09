package com.suntimes.cl.http;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.http.cookie.SetCookie;

import android.os.Build;

import com.suntimes.cl.util.CLFileUtil;
import com.suntimes.cl.util.CLLog;

public class CLSimpleHttpClient {
	public static final String TAG = "CLSimpleHttpClient";
    private static final int TIME_OUT = 10000;
    private static final String  BOUNDARY = UUID.randomUUID().toString();  //边界标识   随机生成
    private static final String PREFIX = "--" , LINE_END = "\r\n"; 
    private static final String CONTENT_TYPE = "application/octet-stream";   //内容类型
    private static final String CHARSET = "utf-8";
    public static SetCookie cookie = null;
	static{
		/**
		 * Avoiding Bugs In Earlier Releases
			Prior to Android 2.2 (Froyo), this class had some frustrating bugs. In particular, calling close() on a readable InputStream could poison the connection pool. Work around this by disabling connection pooling
		 */
		if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
			System.setProperty("http.keepAlive", "false");
		}
	}

	private CLSimpleHttpClient() {

	}

	/**
	 * perform an Http POST to the supplied urlString with the supplied
	 * requestHeaders and formParameters
	 * 
	 * @return String the response contents
	 * @param urlString
	 *            the URL to post to
	 * @param requestHeaders
	 *            a Map of the request headernames and values to be placed into
	 *            the request
	 * @param formParameters
	 *            a Map of form parameters and values to be placed into the
	 *            request
	 * @throws MalformedURLException
	 *             reports problems with the urlString
	 * @throws ProtocolException
	 *             reports problems performing Http POST
	 * @throws IOException
	 *             reports I/O sending and/or retrieving data over Http
	 */
	public synchronized static String postForm(String urlString, Map<String,String> requestHeaders, Map<String,String> formParameters) throws Exception {
		return post(urlString, requestHeaders, formParameters);
	}
	
	public synchronized static final String post(String url) throws Exception{
		return post(url, null, null);
	}

	/**
	 * perform an Http POST to the supplied urlString with the supplied
	 * requestHeaders and formParameters
	 * 
	 * @return String the response contents
	 * @param urlString
	 *            the URL to post to
	 * @param requestHeaders
	 *            a Map of the request headernames and values to be placed into
	 *            the request
	 * @param formParameters
	 *            a Map of form parameters and values to be placed into the
	 *            request
	 * @param contents
	 *            the contents of the HTTP request
	 * @throws MalformedURLException
	 *             reports problems with the urlString
	 * @throws ProtocolException
	 *             reports problems performing Http POST
	 * @throws IOException
	 *             reports I/O sending and/or retrieving data over Http
	 */
	public synchronized static final String post(String urlString, Map<String,String> requestHeaders,Map<String,String> formParameters)throws Exception {
		long CLLogId = System.currentTimeMillis();
		long startTime = CLLogId;
		String params = null;
		// open url connection
		URL url = new URL(urlString);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();

		// set up url connection to post information and
		// retrieve information back
        con.setReadTimeout(TIME_OUT);
        con.setConnectTimeout(TIME_OUT);
		con.setRequestMethod("POST");
		con.setDoInput(true);
		con.setDoOutput(true);
		
		//set cookie
		if(cookie != null){
			String cookieStr = cookie.getName()+"="+cookie.getValue();
			con.setRequestProperty("Cookie", cookieStr);
			CLLog.d(TAG, "Cookie: "+cookieStr);
		}

		// add all the request headers
		if (requestHeaders != null && requestHeaders.size() > 0) {
			Set<String> headers = requestHeaders.keySet();
			for (Iterator<String> it = headers.iterator(); it.hasNext();) {
				String headerName = (String) it.next();
				String headerValue = (String) requestHeaders.get(headerName);
				con.setRequestProperty(headerName, headerValue);
			}
		}

		// add url form parameters
		DataOutputStream ostream = null;
		try {
			ostream = new DataOutputStream(con.getOutputStream());
			if (formParameters != null && formParameters.size() > 0) {
				Set<String> parameters = formParameters.keySet();
				Iterator<String> it = parameters.iterator();
				StringBuffer buf = new StringBuffer();

				for (int paramCount = 0; it.hasNext();) {
					String parameterName = (String) it.next();
					String parameterValue = (String) formParameters.get(parameterName);

					if (parameterValue != null) {
						parameterValue = URLEncoder.encode(parameterValue,"UTF-8");
						if (paramCount > 0) {
							buf.append("&");
						}
						buf.append(parameterName);
						buf.append("=");
						buf.append(parameterValue);
						++paramCount;
					}
				}

				params = buf.toString();
				ostream.writeBytes(buf.toString());
			}
		} finally {
			if (ostream != null) {
				ostream.flush();
				ostream.close();
			}
		}
		CLLog.d(TAG, ">> start "+CLLogId+"; "+url+(params==null? "" : "?"+params));
        int resCode = con.getResponseCode();
        String resMessage = con.getResponseMessage();
//        Map<String, List<String>> headerFieles = con.getHeaderFields();
//        for(String key : headerFieles.keySet()){
//        	CLLog.i(TAG, key+"="+headerFieles.get(key));
//        }
        CLLog.d(TAG, "resCode:"+resCode+"; resMessage="+resMessage);

		Object contents = con.getContent();
		InputStream is = (InputStream) contents;
		String result = null;
		ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
		try{
			byte [] buffer = new byte[1024];
			int readLen = -1;
			while ((readLen = is.read(buffer)) > 0) {
				byteOutput.write(buffer, 0, readLen);
			}
			result = new String(byteOutput.toByteArray());
			byteOutput.close();
			con.disconnect();
		}finally{
			if(is != null){
				try{
					is.close();
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		}
		CLLog.d(TAG, "<< end "+CLLogId+"; use time: "+(System.currentTimeMillis()-startTime));
		return result;
	}

	
	public synchronized static final String postFormWithFiles(String urlString, 
			Map<String,String> requestHeaders, 
			Map<String,String> formParameters, 
			Map<String,File> fileParameters) throws Exception{
		return postFormWithFiles(urlString, requestHeaders, formParameters, fileParameters, null);
	}
	
	public synchronized static final String postFormWithFiles(String urlString, 
			Map<String,String> requestHeaders, 
			Map<String,String> formParameters, 
			Map<String,File> fileParameters,
			IProgress progressListener) throws Exception{
		long CLLogId = System.currentTimeMillis();
		long startTime = CLLogId;
        String result = "";
        

		CLLog.d(TAG, ">> start "+CLLogId+"; "+urlString);
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(TIME_OUT);
        conn.setConnectTimeout(TIME_OUT);
        conn.setRequestMethod("POST");  //请求方式
        conn.setDoInput(true);  //允许输入流
        conn.setDoOutput(true); //允许输出流
        
        conn.setUseCaches(false);  //不允许使用缓存
        conn.setRequestProperty("Charset", CHARSET);  //设置编码
        conn.setRequestProperty("connection", "keep-alive");   
        conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
        
		//set cookie
		if(cookie != null){
			String cookieStr = cookie.getName()+"="+cookie.getValue();
			conn.setRequestProperty("Cookie", cookieStr);
		}
		
		// add all the request headers
		if (requestHeaders != null && requestHeaders.size() > 0) {
			Set<String> headers = requestHeaders.keySet();
			for (Iterator<String> it = headers.iterator(); it.hasNext();) {
				String headerName = (String) it.next();
				String headerValue = (String) requestHeaders.get(headerName);
				conn.setRequestProperty(headerName, headerValue);
			}
		}

        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        
        writeStringParams(dos, formParameters);
        publishProgress(progressListener, 10);
        writeFileParams(dos, fileParameters);
        publishProgress(progressListener, 50);
        dos.writeBytes(PREFIX+BOUNDARY+PREFIX+LINE_END);  
        dos.writeBytes(LINE_END);
        dos.flush();
        publishProgress(progressListener, 60);
        /**
         * 获取响应码  200=成功
         * 当响应成功，获取响应的流  
         */
        int resCode = conn.getResponseCode();
        String resMessage = conn.getResponseMessage();
        CLLog.d(TAG, "resCode:"+resCode+"; resMessage="+resMessage);
        if(resCode == 200){
        	InputStream input =  conn.getInputStream();
        	StringBuffer sb1= new StringBuffer();
        	int ss ;
        	while((ss=input.read())!=-1)
        	{
        		sb1.append((char)ss);
        	}
        	result = sb1.toString();
        }else{
        	result = "server is not response";
        }
		CLLog.d(TAG, "<< end "+CLLogId+"; use time: "+(System.currentTimeMillis()-startTime));
        publishProgress(progressListener, 100);
        return result;
	}
	
	/**
	 * 写入普通字符串数据
	 * @param ds
	 * @param stringParams
	 * @throws Exception
	 */
    private static final void writeStringParams(DataOutputStream ds,Map<String,String> stringParams) throws Exception {
    	if(ds != null && stringParams != null){
    		Set<String> keySet = stringParams.keySet();  
    		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
    			String name = it.next();  
    			String value = stringParams.get(name);  
    			writeUTF8Bytes(ds,PREFIX + BOUNDARY + LINE_END);
    			writeUTF8Bytes(ds,"Content-Disposition: form-data; name=\"" + name + "\""+LINE_END);  
    			writeUTF8Bytes(ds,LINE_END);  
    			writeUTF8Bytes(ds,value+ LINE_END); 
    			CLLog.d(TAG, "name="+name+"; value="+value);
    		}  
    	}
    }  
    
    /**
     * 写入文件数据
     * @param ds
     * @param fileParams
     * @throws Exception
     */
    private static final void writeFileParams(DataOutputStream ds,Map<String,File> fileParams) throws Exception {  
    	if(ds != null && fileParams != null){
    		Set<String> keySet = fileParams.keySet();  
    		for (Iterator<String> it = keySet.iterator(); it.hasNext();) {
    			String name = it.next();  
    			File value = fileParams.get(name);  
    			writeUTF8Bytes(ds,PREFIX + BOUNDARY + LINE_END);  
    			writeUTF8Bytes(ds,"Content-Disposition: form-data; name=\"" + name  
    					+ "\"; filename=\"" + value.getName() + "\""+LINE_END);  
    			writeUTF8Bytes(ds,"Content-Type: " + CONTENT_TYPE + LINE_END);  
    			writeUTF8Bytes(ds,LINE_END);  
    			ds.write(CLFileUtil.buildDataFromFile(value));  
    			writeUTF8Bytes(ds,LINE_END);  
    		}  
    	}
    }
    
    private static final void writeUTF8Bytes(DataOutputStream dos,String str) throws Exception{
    	dos.write(str.getBytes(CHARSET));
    }
    
    /**
     * 更新进度
     * @param listener
     * @param progress 百份比
     */
    private static void publishProgress(IProgress listener,int progress){
    	if(listener != null){
    		listener.onProgressBG(progress);
    	}
    }
    
    /**
     * 进度百分比
     * @author yaotian
     *
     */
    public static interface IProgress{
    	/**在后台线程回调该方法*/
    	public void onProgressBG(int progress);
    }
}
