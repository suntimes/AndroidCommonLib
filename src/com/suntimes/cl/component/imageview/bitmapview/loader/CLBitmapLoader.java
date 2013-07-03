package com.suntimes.cl.component.imageview.bitmapview.loader;

import java.io.File;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.suntimes.cl.component.imageview.bitmapview.cache.CLBitmapCacheUtil;
import com.suntimes.cl.component.imageview.bitmapview.download.CLBitmapDownLoadManager;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfoManager;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo.Options;

/**
 * 
 * 加载图片管理器
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-3-10
 */
public class CLBitmapLoader {
	private static final String TAG = "BitmapLoader";

	private static final int MSG_LOADER_HANDLER = 0x00000001;

	private static final int LOADER_DELAY = 90;
	private static CLBitmapLoader mBitmapLoaderInstance;

	private Context mContext;
	/**
	 * image cache
	 */
	private CLBitmapCacheUtil mBitmapCache;
	/**
	 * 
	 */
	private BitmapFactory.Options mBitmapFactory;
	/**
	 * UI标记符,判断当前UI一个偏移量
	 */
	private String mUIFlag;
	/**
	 * asynchronous load image Handler
	 */
	private LoaderHandler mLoaderHandler;
	/**
	 * load image order
	 */
	private LinkedList<String> mLoaderOrder;
	/**
	 * 
	 */
	private HandlerThread mHandlerThread;
	/**
	 * 默认位图
	 */
	private Bitmap mDefaultBitmap;

	public void setContext(Context context, int defaultIconId) {
		this.mContext = context;
		mDefaultBitmap = BitmapFactory.decodeResource(context.getResources(), defaultIconId);
	}

	public void setBitmapCache(CLBitmapCacheUtil cache) {
		this.mBitmapCache = cache;
	}

	private CLBitmapLoader() {
		Log.v(TAG, "init BitmapLoader");
		mLoaderOrder = new LinkedList<String>();
		mBitmapFactory = new BitmapFactory.Options();
		if (mHandlerThread == null) {
			mHandlerThread = new HandlerThread(this.getClass().getSimpleName(), Thread.MAX_PRIORITY);
			mHandlerThread.start();
			mLoaderHandler = new LoaderHandler(mHandlerThread.getLooper());
		}

	}

	public static CLBitmapLoader getInstance() {
		if (mBitmapLoaderInstance == null) {
			mBitmapLoaderInstance = new CLBitmapLoader();
		}
		return mBitmapLoaderInstance;
	}

	public boolean isImageExist(String path) {
		final File file = new File(path);
		return file.exists() && file.length() > 0;
	}

	public String getUiFlag() {
		return mUIFlag;
	}

	public void setUiFlag(String uiFlag) {
		this.mUIFlag = uiFlag;
	}

	/**
	 * 加载Bitmap, 先从Cache中判断Bitmap存在,<br>
	 * 如果Cache不存在则从本地文件加载,<br>
	 * 如果本地文件不存在则从网络download
	 * 
	 * @param info 加载图片参数
	 * @return
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	private synchronized Bitmap loadBitmap(CLBitmapInfo info) {
		Bitmap bitmap = null;
		if (info == null) {
			return null;
		}
		if (mBitmapCache != null) {
			bitmap = mBitmapCache.getBitmap(info);
		}
		if (bitmap != null) {
			Log.v(TAG, "bitmap not in cache!");
			return bitmap;
		} else {
			if (isImageExist(info.getFilePath())) {
				String path = info.getFilePath();
				Log.v(TAG, "Load image from file>>" + path);
				int hRatio = 0;
				int wRatio = 0;
				mBitmapFactory.inSampleSize = 1;
				mBitmapFactory.inJustDecodeBounds = true;
				synchronized (mBitmapFactory) {
					if (info.getOptions() != null) {
						bitmap = BitmapFactory.decodeFile(path, mBitmapFactory);
						Options options = info.getOptions();
						if (options.height > 0) {
							hRatio = (int) Math.ceil(mBitmapFactory.outHeight / options.height); // 图片高度是高度的几倍
						}
						if (options.width > 0) {
							wRatio = (int) Math.ceil(mBitmapFactory.outWidth / options.width); // 图片宽度是宽度的几倍
						}

						if (hRatio > 1 || wRatio > 1) {
							if (hRatio > wRatio) {
								mBitmapFactory.inSampleSize = hRatio;
							} else {
								mBitmapFactory.inSampleSize = wRatio;
							}
						}
					}
					mBitmapFactory.inJustDecodeBounds = false;
					try {
						bitmap = BitmapFactory.decodeFile(path, mBitmapFactory);
					} catch (Throwable e) {
						e.printStackTrace();
					}

					if (bitmap != null) {
						if (mBitmapCache != null) {
							mBitmapCache.putBitmap(bitmap, info);
						}
					} else {
						// bitmap not invalid , deleted local file and retry
						// download again bitmap
						if (mBitmapFactory.outHeight < 1 && mBitmapFactory.outWidth < 1) {
							// Log.i(TAG, "delete file:" + mergeFileName);
							File file = new File(path);
							file.delete();
							CLBitmapDownLoadManager.getInstance().addTask(info);
						}
					}
				}

			} else {
				CLBitmapDownLoadManager.getInstance().addTask(info);
			}

		}
		return bitmap;
	}

	/**
	 * 
	 * 异步加载图片Handler
	 * 
	 * @author jianfeng.lao
	 * @version 1.0
	 * @CreateDate 2013-3-10
	 */
	private class LoaderHandler extends Handler {

		public LoaderHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOADER_HANDLER:
				CLBitmapInfo info = null;
				String cacheKey;
				synchronized (mLoaderOrder) {
					if (mLoaderOrder.isEmpty()) {
						Log.d(TAG, "loader Task is empty");
						return;
					}
					cacheKey = mLoaderOrder.removeFirst();
					// get the BitmapInfo from BitmapInfoManager
					info = CLBitmapInfoManager.getInstance().getBitmapInfo(cacheKey);
				}
				if (isBitmapInfoAvailable(info)) {
					Bitmap bitmap = loadBitmap(info);
					info.callBackOnLoad(bitmap, info);
					if (!mLoaderOrder.isEmpty()) {
						sendEmptyMessageDelayed(MSG_LOADER_HANDLER, LOADER_DELAY);
					}
				} else {
					sendEmptyMessageDelayed(MSG_LOADER_HANDLER, 0);
				}
				break;
			}
			super.handleMessage(msg);
		}
	}

	public CLBitmapCacheUtil getBitmapCache() {
		return mBitmapCache;
	}

	/**
	 * add loader image task to orderList
	 * 
	 * @param info
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void addLoaderTask(CLBitmapInfo info) {
		if (isBitmapInfoAvailable(info)) {
			if (!mLoaderOrder.contains(info.getCacheKey())) {
				mLoaderOrder.add(info.getCacheKey());
				Log.v(TAG, "add bitmapinfo to loader task");
				if (!mLoaderHandler.hasMessages(MSG_LOADER_HANDLER)) {
					mLoaderHandler.sendEmptyMessageDelayed(MSG_LOADER_HANDLER, LOADER_DELAY);
				}
			}

		}
	}

	/**
	 * 判断BitmapInfo是否有效,注意如果BitmapInfo中uiFlag不相同会不加载这个BitmapInfo
	 * 
	 * @param info
	 *            the pojo of BitmapInfo
	 * @return true BitmapInfo available,false otherwise
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	private boolean isBitmapInfoAvailable(CLBitmapInfo info) {
		if (info != null
				&& (mUIFlag == null || (info.getUiFlag() != null && mUIFlag != null && info.getUiFlag().equals(mUIFlag)))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断缓存中是否存在BitmapInfo并且加载
	 * 
	 * @param info
	 * @return true is found from cache and load image,false otherwise
	 * @CreateDate 2013-4-27
	 */
	public boolean loadBitmapFromCache(CLBitmapInfo info) {
		if (info != null && mBitmapCache != null) {
			Bitmap bitmap = mBitmapCache.getBitmap(info);
			if (bitmap != null) {
				Log.v(TAG, "loadBitmapFromCache>>");
				info.callBackOnLoad(bitmap, info);
				return true;
			}
			Log.v(TAG, "bitmap not form cache>>");
		}
		return false;
	}

	/**
	 * clear load image order
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void clearLoaderTask() {
		mLoaderOrder.clear();
	}

	/**
	 * recycle BitmapInfo from cache
	 * 
	 * @param info
	 *            recycle BitmapInfo object
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void recycleBitmapInfo(CLBitmapInfo info) {
		if (mBitmapCache != null) {
			mBitmapCache.destoryItem(info);
		}
	}
	
	/**
	 *释放cache里面的所以资源
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void recycleBitmapCache() {
		clearLoaderTask();
		if (mBitmapCache != null) {
			mBitmapCache.destory();
		}
	}

	/**
	 * obtain default bitmap
	 * 
	 * @return null is not set
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public Bitmap getDefaultBitmap() {
		return mDefaultBitmap;
	}

	/**
	 * destory BitmapLoader,NOTE: use be careful<br>
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void destory() {
		if (mLoaderHandler != null) {
			mLoaderHandler.removeMessages(MSG_LOADER_HANDLER);
		}
		if (mLoaderHandler != null) {
			mLoaderHandler.getLooper().quit();
		}
		if (mHandlerThread != null) {
			mHandlerThread = null;
		}
		if (mLoaderOrder != null) {
			mLoaderOrder.clear();
		}
		mBitmapLoaderInstance = null;
		Log.v(TAG, "destory BitmapLoader");
	}
}
