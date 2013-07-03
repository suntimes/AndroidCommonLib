package com.suntimes.cl.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;


public class CLHttpClient {
	
private static final String TAG = "CommonHttpClient";
	
	protected static final String PREFIX = "--", LINEND = "\r\n";
	protected static final String MULTIPART_FROM_DATA = "multipart/form-data";
	protected static final String CHARSET = "UTF-8";

	protected String url;
	protected List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
	protected Map<String, File> fileParams = new HashMap<String, File>();
	protected Map<String, String> defaultParams = new HashMap<String, String>();
	protected List<Header> headerList = new ArrayList<Header>();
	protected HttpParams httpParams = new BasicHttpParams();
	protected List<Cookie> cookieList = new ArrayList<Cookie>();
	protected DefaultHttpClient client;
	protected HttpRequestBase request;
	protected Context context;
	protected int statusCode;
	
	/*-- Add by Tim 2012.12.13 --*/
	private RedirectHandler mRedirectHandler;

	public CLHttpClient(String url, Context context) {
		this.url = url;
		this.context = context;
	}

	public void addHeader(String name, String value) {
		Header header = new BasicHeader(name, value);
		headerList.add(header);
	}

	public void addParam(String name, Object value) {
		this.addParam(name, value, false);
	}

	public void addParamEncode(String name, Object value) {
		this.addParam(name, value, true);
	}

	private void addParam(String name, Object value, boolean needEncode) {
		if (name != null && value != null) {

			if (value instanceof File) {
				File fileValue = (File) value;
				this.fileParams.put(name, fileValue);
			} else {
				String paramName = name;
				String paramValue = null;
				if (value instanceof Collection<?>) {
					Collection<?> collection = (Collection<?>) value;
					if (collection.size() > 0) {
						StringBuffer str = new StringBuffer("");
						for (Object obj : collection) {
							if (!str.toString().equals("")) {
								str.append(",");
							}
							str.append(obj);
						}
						paramValue = str.toString();
					}
				} else {
					paramValue = value.toString();
				}
				if(paramValue!=null){
					if (needEncode) {
						try {
							paramValue = URLEncoder.encode(paramValue,
									"utf-8");
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}
					params.add(new BasicNameValuePair(paramName, paramValue));
				}
			}
		}
	}

	private void checkNAddDefaultParam() {
		List<String> nameList = new ArrayList<String>();
		for (NameValuePair pair : this.params) {
			String name = pair.getName();
			nameList.add(name);
		}

		Set<String> keySet = this.defaultParams.keySet();
		for (String key : keySet) {
			if (!nameList.contains(key)) {
				this.addParam(key, this.defaultParams.get(key));
			}
		}
	}

	protected void setDefaultParams() {
		HttpConnectionParams.setConnectionTimeout(httpParams, 30 * 1000);
		HttpConnectionParams.setSoTimeout(httpParams, 30 * 1000);
		HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
		HttpClientParams.setRedirecting(httpParams, true);
	}
	
	

	protected HttpResponse startGetConnection() throws CLConnectionException, CLInvalidNetworkException {
		HttpResponse response = null;
		if(!CLHttpClient.hasValidNetwork(context)){
			throw new CLInvalidNetworkException();
		}
		try {
			
			if(url.startsWith("https")){
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				CLSSLSocketFactory sslSocketFactory = new CLSSLSocketFactory("");
				sslSocketFactory.setHostnameVerifier(CLSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				
				
				schemeRegistry.register(new Scheme("https", sslSocketFactory , 443));
				schemeRegistry.register(new Scheme("http", sslSocketFactory , 80));
				
				
				HttpParams httpParams = new BasicHttpParams();
	
				SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
				
				client = new DefaultHttpClient(mgr, httpParams);
				//client.setRoutePlanner(new DefaultHttpRoutePlanner(schemeRegistry));
			}else{
				client = new DefaultHttpClient();
			}
			
			/*-- Add by Tim 2012.12.13 --*/
			if(mRedirectHandler != null) {
				client.setRedirectHandler(mRedirectHandler);
			}
			
			this.checkNAddDefaultParam();

			if (cookieList != null) {
				for (Cookie cookie : cookieList) {
					client.getCookieStore().addCookie(cookie);
				}
			}

			StringBuffer sb = new StringBuffer(this.url);
			for (BasicNameValuePair pair : params) {
				if (sb.toString().indexOf("?") >= 0) {
					sb.append("&");
				} else {
					sb.append("?");
				}
				sb.append(pair.getName() + "=" + pair.getValue());
				Log.i(TAG, "LOG:GET " + pair.getName() + " = " + pair.getValue());
			}
			if (sb.toString().indexOf("?") >= 0) {
				sb.append("&");
			} else {
				sb.append("?");
			}
			sb.append("t=" + System.currentTimeMillis());

			//Log.i("get", " url >>  " + sb.toString());

			request = new HttpGet();
			request.setURI(new URI(sb.toString().trim()));

			request.setHeaders(this.headerList
					.toArray(new Header[this.headerList.size()]));
			this.setDefaultParams();
			request.setParams(this.httpParams);

			response = client.execute(request);

		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
			//throw new ConnectionException(this.url, 404, context.getString(R.string.no_peer_certificate));
		} catch (SSLException e) {
			e.printStackTrace();
			//throw new ConnectionException(this.url, 404, context.getString(R.string.not_trusted_server_certificate));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		}
		return response;
	}

	public Bitmap getBitmap() throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		Bitmap bitmap = null;
		try {

			Log.i(TAG, "LOG: <getBitmap> URL:" + this.url);
			HttpResponse response = this.startGetConnection();
			
			Log.i(TAG, "LOG:Get<getBitmap> status:" + (response.getStatusLine().getStatusCode()));
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				if (in != null) {
					bitmap = BitmapFactory.decodeStream(in);
					Log.i(TAG, "LOG:Get <getBitmap> bitmap:" + bitmap);
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			closeHttpClient();
		}
		return bitmap;
	}

	public byte[] getByte() throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		byte[] byteArray = null;
		try {

			Log.i(TAG, "LOG: <getByte> URL:" + this.url);
			HttpResponse response = this.startGetConnection();

			Log.i(TAG, "LOG:Get  <getByte> status:" + (response.getStatusLine().getStatusCode()));
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				if (in != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = in.read(tmp)) != -1) {
						byteArrayOutputStream.write(tmp, 0, bytesRead);
					}
					byteArray = byteArrayOutputStream.toByteArray();
					Log.i(TAG, "LOG:Get <getBitmap> byteArray:" + byteArray);
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			closeHttpClient();
		}
		
		
		return byteArray;
	}

	public String getString() throws CLConnectionException, CLInvalidNetworkException {
		byte[] buffer = this.getByte();
		String str = null;
		str = new String(buffer);
		//Log.i("getString", " str >>>  " + str);
		return str;
	}

	public void get(String filePath) throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		FileOutputStream out = null;
		try {

			Log.i(TAG, "LOG: <get>URL:" + this.url);
			HttpResponse response = this.startGetConnection();

			Log.i(TAG, "LOG:Get <get> status:" + (response.getStatusLine().getStatusCode()));
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				out = new FileOutputStream(filePath);
				
				//ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				if (in != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = in.read(tmp)) != -1) {
						out.write(tmp, 0, bytesRead);
						/*try{
							byteArrayOutputStream.write(tmp, 0, bytesRead);
						}catch (Exception e) {
							e.printStackTrace();
						}*/
					}
					/*try{
						byte[] byteArray = null;
						byteArray = byteArrayOutputStream.toByteArray();
						String s = new String(byteArray);
						//Log.i("get", " return xml >>>  " + s);
					}catch (Exception e) {
						e.printStackTrace();
					}*/
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			closeHttpClient();

		}

	}
	
	private byte[] removeUTF8Bom(byte[] byteArray) {
		if (byteArray[0] == -17 && byteArray[1] == -69 && byteArray[2] == -65) {
			byte[] nbs = new byte[byteArray.length - 3];
			System.arraycopy(byteArray, 3, nbs, 0, nbs.length);
			return nbs;
		}
		return byteArray;
	}
	
	
	public InputStream get() throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		InputStream input = null;
		byte[] byteArray = null;
		try {

			HttpResponse response = this.startGetConnection();
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				input = response.getEntity().getContent();
				
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				if (input != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = input.read(tmp)) != -1) {
						byteArrayOutputStream.write(tmp, 0, bytesRead);
					}
					byteArray = removeUTF8Bom(byteArrayOutputStream.toByteArray());
				}
				
				in = new ByteArrayInputStream(byteArray);
				try {
					byteArrayOutputStream.close();
				} catch (IOException ioe) {
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			closeHttpClient();
		}
		return in;

	}

	protected HttpResponse startPostConnection() throws CLConnectionException, CLInvalidNetworkException {
		HttpResponse response = null;
		if(!CLHttpClient.hasValidNetwork(context)){
			throw new CLInvalidNetworkException();
		}
		try {
			
			if(url.startsWith("https")){
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				CLSSLSocketFactory sslSocketFactory = new CLSSLSocketFactory("");
				sslSocketFactory.setHostnameVerifier(CLSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				
				
				schemeRegistry.register(new Scheme("https", sslSocketFactory , 443));
				schemeRegistry.register(new Scheme("http", sslSocketFactory , 80));
				
				HttpParams httpParams = new BasicHttpParams();
	
				SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
				
				client = new DefaultHttpClient(mgr, httpParams);
			}else{
				client = new DefaultHttpClient();
			}
			
			this.checkNAddDefaultParam();

			//Log.i("post", " url >>  " + url.toString());
			for (BasicNameValuePair param : params) {
				//Log.i("post", "&" + param.getName() + "=" + param.getValue());
				Log.i(TAG, "LOG:POST :" + param.getName() + " = " + param.getValue());
			}

			HttpPost postRequest = new HttpPost(url);
			request = postRequest;
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,
					HTTP.UTF_8);
			postRequest.setEntity(entity);
			postRequest.setHeaders(this.headerList
					.toArray(new Header[this.headerList.size()]));
			this.setDefaultParams();
			response = client.execute(postRequest);
		} catch (SSLPeerUnverifiedException e) {
			e.printStackTrace();
//			throw new ConnectionException(this.url, 404, context.getString(R.string.no_peer_certificate));
		} catch (SSLException e) {
			e.printStackTrace();
//			throw new ConnectionException(this.url, 404, context.getString(R.string.not_trusted_server_certificate));
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		}

		return response;
	}

	public void post(String filePath) throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		FileOutputStream out = null;
		try {
			
			Log.i(TAG, "LOG: <post>URL:" + this.url);
			HttpResponse response = this.startPostConnection();
			
			Log.i(TAG, "LOG:Post <post> Status:" + (response.getStatusLine().getStatusCode()));
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				try{
					out = new FileOutputStream(filePath);
				}catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
				
				if (in != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					while ((bytesRead = in.read(tmp)) != -1) {
						out.write(tmp, 0, bytesRead);
						
						try{
							byteArrayOutputStream.write(tmp, 0, bytesRead);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					try{
						byte[] byteArray = null;
						byteArray = byteArrayOutputStream.toByteArray();
						String s = new String(byteArray);
						Log.i(TAG, "LOG:Post <post> XML:" + s);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			closeHttpClient();
		}

	}

	public byte[] postByte() throws CLConnectionException, CLInvalidNetworkException {
		InputStream in = null;
		byte[] byteArray = null;
		try {

			Log.i(TAG, "LOG:<post> URL:" + this.url);
			HttpResponse response = this.startPostConnection();
			
			Log.i(TAG, "LOG:Post <postByte> status:" + (response.getStatusLine().getStatusCode()));
			statusCode = response.getStatusLine().getStatusCode();
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				in = response.getEntity().getContent();
				ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
				if (in != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					while ((bytesRead = in.read(tmp)) != -1) {
						byteArrayOutputStream.write(tmp, 0, bytesRead);
					}
					byteArray = byteArrayOutputStream.toByteArray();
					Log.i(TAG, "LOG:Post <postByte> byteArray:" + byteArray);
				}
			} else {
				throw new CLConnectionException(this.url, 404);
			}
		} catch (CLConnectionException e) {
			e.printStackTrace();
			throw e;
		} catch (CLInvalidNetworkException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			//Log.e("ex", e.getMessage());
			throw new CLConnectionException(this.url, 404);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
			closeHttpClient();
		}
		return byteArray;
	}

	public String postString() throws CLConnectionException, CLInvalidNetworkException {
		byte[] buffer = this.postByte();
		String str = null;
		str = new String(buffer);
		//Log.i("getString", " str >>>  " + str);
		return str;
	}

	public void closeHttpClient() {
		if (this.client != null) {
			this.client.getConnectionManager().shutdown();
		}
		if (this.request != null) {
			this.request.abort();
		}
	}

	

	public String postUseingConnection() throws CLConnectionException, CLInvalidNetworkException {
		if(!CLHttpClient.hasValidNetwork(context)){
			throw new CLInvalidNetworkException();
		}
		checkNAddDefaultParam();

		String BOUNDARY = java.util.UUID.randomUUID().toString();
		
		InputStream responseInputStream = null;
		byte[] responseBody = null;
		DataOutputStream outStream = null;
		HttpURLConnection conn = null;

		try {
			//Log.i("CommonHttpClient", " url >>>>>  " + url);
			URL uri = new URL(this.url);
			conn = (HttpURLConnection) uri.openConnection();

			for (Header header : headerList) {
				conn.setRequestProperty(header.getName(), header.getValue());
			}

			//conn.setConnectTimeout(2 * 60 * 1000);
			conn.setConnectTimeout(30 * 1000);
			conn.setReadTimeout(60 * 1000); 
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setUseCaches(false); 
			conn.setRequestMethod("POST");
			
			
			
			if (this.fileParams.isEmpty()) {
				String postParam = "";
				if (params != null) {
					postParam = encodeParameters(params);
				}
				//Log.i("CommonHttpClient", " postParam >>>>>  " + postParam);
				byte[] bytes = postParam.getBytes("UTF-8");
				conn.setRequestProperty("Content-Length", Integer
						.toString(bytes.length));
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				outStream = new DataOutputStream(conn.getOutputStream());
				outStream.write(bytes);
			} else {
				conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
				outStream = new DataOutputStream(conn.getOutputStream());
				StringBuilder sb = new StringBuilder();
				for (BasicNameValuePair pair : params) {
					sb.append(PREFIX);
					sb.append(BOUNDARY);
					sb.append(LINEND);
					sb.append("Content-Disposition: form-data; name=\"" + pair.getName() + "\"" + LINEND);
					sb.append("Content-Type: text/plain; charset=US-ASCII" + LINEND);
					sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
					sb.append(LINEND);
					sb.append(pair.getValue());
					sb.append(LINEND);
				}

				outStream.write(sb.toString().getBytes());
				// �����ļ����
				if (fileParams != null) {
					for (Map.Entry<String, File> file : fileParams.entrySet()) {
						String filename = file.getValue().getName();
						String contentType = null;
						if (filename.lastIndexOf(".gif") > 0) {
							contentType = "image/gif";
						} else if (filename.lastIndexOf(".png") > 0) {
							contentType = "image/png";
						} else if (filename.lastIndexOf(".jpeg") > 0 || filename.lastIndexOf(".jpg") > 0) {
							contentType = "image/jpeg";
						}

						StringBuilder sb1 = new StringBuilder();
						sb1.append(PREFIX);
						sb1.append(BOUNDARY);
						sb1.append(LINEND);
						sb1.append("Content-Disposition: form-data; name=\"" + file.getKey() + "\"; filename=\"" + filename + "\"" + LINEND);
						sb1.append("Content-Type: " + contentType + "; charset=" + CHARSET + LINEND);
						sb1.append("Content-Transfer-Encoding: binary" + LINEND);
						sb1.append(LINEND);
						outStream.write(sb1.toString().getBytes());

						InputStream is = null;

						try {
							is = new FileInputStream(file.getValue());
							byte[] buffer = new byte[1024];
							int len = 0;
							while ((len = is.read(buffer)) != -1) {
								outStream.write(buffer, 0, len);
							}
						} catch (Exception e) {
							e.printStackTrace();
							throw new CLConnectionException(this.url, 404);
						} finally {
							if (is != null) {
								try {
									is.close();
								} catch (IOException e) {
									e.printStackTrace();
								}
							}
						}

						outStream.write(LINEND.getBytes());
						
						byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
						outStream.write(end_data);
						
					}
				}
			}

			
			outStream.flush();

			int res = conn.getResponseCode();
			
			//Log.i("res", " res >>>   " + res);
			if (res == 200) {
				responseInputStream = conn.getInputStream();
				if (responseInputStream != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					ByteArrayOutputStream buffer = new ByteArrayOutputStream(
							1024);
					while ((bytesRead = responseInputStream.read(tmp)) != -1) {
						buffer.write(tmp, 0, bytesRead);
					}
					responseBody = buffer.toByteArray();
				}
			} else {
				//Log.i("post", " response message >>> " + conn.getResponseMessage());
				throw new CLConnectionException(this.url, 404);
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new CLConnectionException(this.url, 404);
		} finally {
			if(responseInputStream!=null){
				try {
					responseInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(outStream!=null){
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if(conn!=null){
				conn.disconnect();
			}
		}
		String str = null;
		if(responseBody!=null){
			str = new String(responseBody);
		}
		//Log.i("CommonHttpClient", " str >>> " + str);
		
		return str;
	}

	public static String encodeParameters(List<BasicNameValuePair> postParams) {
		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < postParams.size(); j++) {
			if (j != 0) {
				buf.append("&");
			}
			try {
				buf.append(URLEncoder.encode(postParams.get(j).getName(), "UTF-8"));
				buf.append("=");
				buf.append(URLEncoder.encode(postParams.get(j).getValue(), "UTF-8"));
			} catch (java.io.UnsupportedEncodingException neverHappen) {
			}
		}
		return buf.toString();

	}
	
	public static boolean hasValidNetwork(Context context) {   
        ConnectivityManager connectivity = (ConnectivityManager) context   
                .getSystemService(Context.CONNECTIVITY_SERVICE);   
        if (connectivity != null) {   
            NetworkInfo info = connectivity.getActiveNetworkInfo();   
            if (info != null) {   
                if (info.getState() == NetworkInfo.State.CONNECTED) {   
                    return true;   
                }   
            }   
        }   
        return false;   
   }  
	
	
	public void postFile(String filePath) throws IOException, CLConnectionException, CLInvalidNetworkException {
		
		//Map<String, String> hearderParams = new HashMap<String, String>();
		Log.i(TAG, "LOG:<postFile> URL:" + this.url.toString());
		try{
			if(url.startsWith("https")){
				SchemeRegistry schemeRegistry = new SchemeRegistry();
				CLSSLSocketFactory sslSocketFactory = new CLSSLSocketFactory("");
				sslSocketFactory.setHostnameVerifier(CLSSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
				
				
				schemeRegistry.register(new Scheme("https", sslSocketFactory , 443));
				schemeRegistry.register(new Scheme("http", sslSocketFactory , 80));
				
				HttpParams httpParams = new BasicHttpParams();
	
				SingleClientConnManager mgr = new SingleClientConnManager(httpParams, schemeRegistry);
				
				client = new DefaultHttpClient(mgr, httpParams);
			}else{
				client = new DefaultHttpClient();
			}
		}catch(Exception e){
			Log.i(TAG, "LOG <postFile>Exception:" + e.getMessage());
			throw new CLConnectionException(this.url, 404);
		}
		HttpClient httpclient = client;
		
		//FileEntity fileEntity = null;
		MultipartEntity multipartEntity = null;
		
		for(Map.Entry<String, File> entry: fileParams.entrySet()){
			//Log.i("postFile", "  key >>>  " + entry.getKey() + "  value >>>  " + entry.getValue());
			//fileEntity = new FileEntity(entry.getValue(), "binary/octet-stream");
			FileBody bin = new FileBody(entry.getValue(), "image/jpeg");
			for(Map.Entry<String, String> entrys: bin.getContentTypeParameters().entrySet()){
				//Log.i("postFile", "  key 22 >>>  " + entrys.getKey() + "  value 22 >>>  " + entrys.getValue());
			}
			//Log.i("postFile", "  getFilename >>>  " + bin.getFilename() + "  MediaType >>>  " + bin.getMediaType());
			multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);  
			multipartEntity.addPart(entry.getKey(), bin);
			
		}
		
		StringBuffer sb = new StringBuffer(this.url);
		for (BasicNameValuePair pair : params) {
			if (sb.toString().indexOf("?") >= 0) {
				sb.append("&");
			} else {
				sb.append("?");
			}
			sb.append(pair.getName() + "=" + pair.getValue());
		}
		if (sb.toString().indexOf("?") >= 0) {
			sb.append("&");
		} else {
			sb.append("?");
		}
		sb.append("t=" + System.currentTimeMillis());
		
		//Log.i("HttpUtil", " postFile >> url >>>  " + sb.toString());
		
		HttpPost httppost = new HttpPost(sb.toString());
		InputStream in = null;
		FileOutputStream out = null;
		//httppost.addHeader("filename", headerValue);
		try{
			httppost.setEntity(multipartEntity);
			HttpResponse response = httpclient.execute(httppost);
			
			statusCode = response.getStatusLine().getStatusCode();
			if ( statusCode == HttpURLConnection.HTTP_OK) {
				in = response.getEntity().getContent();
				out = new FileOutputStream(filePath);
				if (in != null) {
					byte[] tmp = new byte[4096];
					int bytesRead = 0;
					
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					while ((bytesRead = in.read(tmp)) != -1) {
						out.write(tmp, 0, bytesRead);
						
						try{
							byteArrayOutputStream.write(tmp, 0, bytesRead);
						}catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					try{
						byte[] byteArray = null;
						byteArray = byteArrayOutputStream.toByteArray();
						String s = new String(byteArray);
						Log.i(TAG, "LOG<postFile>XML:" + s);
					}catch (Exception e) {
						e.printStackTrace();
					}
				}
			}else{
				throw new CLConnectionException(this.url, 404);
			}
		}catch (SSLPeerUnverifiedException e) {
			Log.i(TAG, "LOG:<postFile>SSLPeerUnverifiedException:" + e.getMessage());
			e.printStackTrace();
			//throw new ConnectionException(this.url, 404, context.getString(R.string.no_peer_certificate));
		}catch (SSLException e) {
			Log.i(TAG, "LOG:<postFile>SSLException:" + e.getMessage());
			e.printStackTrace();
			//throw new ConnectionException(this.url, 404, context.getString(R.string.not_trusted_server_certificate));
		}  catch (Exception e) {
			Log.i(TAG, "LOG:<postFile>Exception:" + e.getMessage());
			/*if(e instanceof ConnectionException){
				throw (ConnectionException)e;
			}*/
			throw new CLConnectionException(this.url, 404);
		}finally{
			try{
				if(in!=null){
					in.close();
				}
				if(out!=null){
					out.close();
				}
				httppost.abort();
				httpclient.getConnectionManager().shutdown();
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	/*-- Add by Tim 2012.12.13 --*/
	public void setRedirectHandler(RedirectHandler redirectHandler) {
		mRedirectHandler = redirectHandler;
	}
}
