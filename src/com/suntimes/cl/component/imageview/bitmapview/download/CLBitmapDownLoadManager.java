package com.suntimes.cl.component.imageview.bitmapview.download;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo;
import com.suntimes.cl.util.CLFileUtil;

/**
 * 
 * Download image from http with task
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-3-9
 */
public class CLBitmapDownLoadManager {
	private static final String TAG = "BitmapDownLoadManager";

	private static final int MAX_DOWNLOAD_THREAD = 4;// 同时下载图片的线程数量
	private static final int DOWNLOAD_TIMEOUT = 15 * 1000;

	private static final int MSG_DOWNLOAD_HANDLER = 0x00000001;
	private static final int MSG_CLEAR_DOWNLOAD_TASK = 0x00000002;

	private static CLBitmapDownLoadManager mDownLoadManagerInstance;
	private Context mContext;
	private final LinkedList<String> mDownloadOrder;
	private final HashMap<String, CLBitmapInfo> mPreDdownloadTasks;//key 为download image URL
	private final HashMap<String, CLBitmapInfo> mDownloadingTasks;//key 为download image URL
	private DownLoadManangerHandler mDownLoadManangerHandler;
	private HandlerThread mHandlerThread;

	private CLBitmapDownLoadManager() {
		Log.v(TAG, "init BitmapDownLoadManager");
		mDownloadingTasks = new HashMap<String, CLBitmapInfo>();
		mDownloadOrder = new LinkedList<String>();
		mPreDdownloadTasks = new HashMap<String, CLBitmapInfo>();
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getSimpleName(), Thread.MAX_PRIORITY);
			mHandlerThread.start();
			mDownLoadManangerHandler = new DownLoadManangerHandler(mHandlerThread.getLooper());
		}
	}

	/**
	 * set before use
	 * 
	 * @param context
	 */
	public void setContext(Context context) {
		this.mContext = context;
	}

	public static CLBitmapDownLoadManager getInstance() {
		if (mDownLoadManagerInstance == null) {
			mDownLoadManagerInstance = new CLBitmapDownLoadManager();
		}

		return mDownLoadManagerInstance;
	}

	private class DownLoadManangerHandler extends Handler {

		/**
		 * @param looper
		 */
		public DownLoadManangerHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_CLEAR_DOWNLOAD_TASK:
				synchronized (mPreDdownloadTasks) {
					mPreDdownloadTasks.clear();
					mDownloadOrder.clear();
				}
				break;
			case MSG_DOWNLOAD_HANDLER:
				CLBitmapInfo info = null;
				int downloadingSize = mDownloadingTasks.size();
				int downloadOrderSize = mDownloadOrder.size();
				synchronized (mPreDdownloadTasks) {
					if (downloadOrderSize > 0 && downloadingSize < MAX_DOWNLOAD_THREAD) {
						info = mPreDdownloadTasks.remove(mDownloadOrder.removeLast());
					}
				}
				if (info != null) {
					mDownloadingTasks.put(info.getDownLoadUrl(), info);
					DownloadThread dlt = new DownloadThread(info);
					dlt.start();
				}

				if (downloadOrderSize <= 0) {
					removeMessages(MSG_DOWNLOAD_HANDLER);
				} else {
					Log.d(TAG, "download image queue count>>>" + downloadOrderSize);
					// 每隔一秒check一次剩下的任务
					sendEmptyMessageDelayed(MSG_DOWNLOAD_HANDLER, 1000);
				}
				break;
			}
			super.handleMessage(msg);
		}
	}

	private class DownloadThread extends Thread {
		private CLBitmapInfo bitmapInfo;

		public DownloadThread(CLBitmapInfo bitmapInfo) {
			super();
			this.bitmapInfo = bitmapInfo;
		}

		@Override
		public void run() {
			boolean isDownLoadSuccess = downloadImage(bitmapInfo);
			mDownloadingTasks.remove(bitmapInfo.getDownLoadUrl());
			bitmapInfo.callBackDownload(bitmapInfo, isDownLoadSuccess);
			super.run();

		}
	}

	private final void quitMananger() {
		if (mDownLoadManangerHandler != null) {
			mDownLoadManangerHandler.getLooper().quit();
		}
		if (mHandlerThread != null) {
			mHandlerThread = null;
		}
	}

	public void addTask(CLBitmapInfo info) {
		Log.i(TAG, "add Task>>" + info.getDownLoadUrl());
		if (!mPreDdownloadTasks.containsKey(info.getDownLoadUrl())) {
			mDownloadOrder.add(info.getDownLoadUrl());
			mPreDdownloadTasks.put(info.getDownLoadUrl(), info);
			if (!mDownLoadManangerHandler.hasMessages(MSG_DOWNLOAD_HANDLER)) {
				mDownLoadManangerHandler.sendEmptyMessage(MSG_DOWNLOAD_HANDLER);
			}
		}
	}

	public void clearDownloadTask() {
		if (mDownLoadManangerHandler != null) {
			mDownLoadManangerHandler.removeMessages(MSG_CLEAR_DOWNLOAD_TASK);
			mDownLoadManangerHandler.removeMessages(MSG_DOWNLOAD_HANDLER);
			mDownLoadManangerHandler.sendEmptyMessage(MSG_CLEAR_DOWNLOAD_TASK);
		}
	}

	/**
	 * NOTE: be careful
	 * 
	 * @author jianfeng.lao Create at 2013-4-27
	 */
	public void destory() {
		clearDownloadTask();
		quitMananger();
		mDownLoadManagerInstance = null;
		Log.v(TAG, "Destory Download Manager");
	}

	private boolean downloadImage(CLBitmapInfo bitmapInfo) {
		Log.v(TAG, "downloadImage>>" + bitmapInfo.getDownLoadUrl());
		boolean result = false;
		final String link = bitmapInfo.getDownLoadUrl();
		final File file = new File(bitmapInfo.getFilePath());

		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}
		HttpURLConnection conn = null;
		InputStream inputStream = null;
		ByteArrayOutputStream outStream = null;

		FileOutputStream fileOutPutStream = null;
		try {
			URL url = new URL(link);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(DOWNLOAD_TIMEOUT);
			conn.setRequestMethod("GET");
			inputStream = conn.getInputStream();
			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK && inputStream != null) {
				outStream = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int len = 0;
				while ((len = inputStream.read(buffer)) != -1) {
					outStream.write(buffer, 0, len);
				}
				fileOutPutStream = new FileOutputStream(file);
				fileOutPutStream.write(outStream.toByteArray());
				fileOutPutStream.flush();
				result = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			CLFileUtil.closeIOStream(inputStream);
			CLFileUtil.closeIOStream(outStream);
			CLFileUtil.closeIOStream(fileOutPutStream);
		}
		return result;
	}

}
