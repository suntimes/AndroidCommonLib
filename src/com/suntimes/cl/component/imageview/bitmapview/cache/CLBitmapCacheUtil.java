package com.suntimes.cl.component.imageview.bitmapview.cache;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;

import android.graphics.Bitmap;
import android.util.Log;

import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo.Options;

/**
 * Bitmap缓存
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-3-10
 */
public class CLBitmapCacheUtil {

	private static final String TAG = "BitmapCacheUtil";
	private int currentCacheSize = -1;

	public static final int CACHE_3M = 3 * 1024 * 1024;
	public static final int CACHE_4M = 4 * 1024 * 1024;
	public static final int CACHE_5M = 5 * 1024 * 1024;
	public static final int CACHE_8M = 8 * 1024 * 1024;
	public static final int CACHE_10M = 10 * 1024 * 1024;
	public static final int CACHE_16M = 16 * 1024 * 1024;

	private static final int SOFT_CACHE_CAPACITY = 1;

	private CLLruCache<String, CLBitmapCacheItem> mHardBitmapCache;
	private LinkedHashMap<String, SoftReference<CLBitmapCacheItem>> mSoftBitmapCache;

	public CLBitmapCacheUtil(int cacheSize) {
		super();
		this.currentCacheSize = cacheSize;
		init();
	}

	/**
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-3-10
	 */
	private void init() {
		Log.v(TAG, "init CLBitmapCacheUtil");
		mSoftBitmapCache = new LinkedHashMap<String, SoftReference<CLBitmapCacheItem>>(SOFT_CACHE_CAPACITY, 0.75f, true) {

			private static final long serialVersionUID = 1L;

			@Override
			protected boolean removeEldestEntry(Entry<String, SoftReference<CLBitmapCacheItem>> eldest) {
				if (size() > SOFT_CACHE_CAPACITY) {
					return true;
				}
				return false;
			}

		};
		mHardBitmapCache = new CLLruCache<String, CLBitmapCacheItem>(currentCacheSize) {
			@Override
			public int sizeOf(String key, CLBitmapCacheItem value) {
				return value.bitmap.getRowBytes() * value.bitmap.getHeight();
			}

			@Override
			protected void entryRemoved(boolean evicted, String key, CLBitmapCacheItem oldValue, CLBitmapCacheItem newValue) {
				Log.w(TAG, "硬引用缓存达到上限 , 推一个不经常使用的放到软缓存:" + key);
				if (oldValue != null) {
					if (oldValue.info != null) {
						CLBitmapInfo info = oldValue.info;
						Options options = info.getOptions();
						if (options != null && options.notPullSoftCache) {
							// notify bitmap prepare recycle
							info.callBackBeforeRecycle(info);
							if (oldValue.bitmap != null) {
								Bitmap bitmap = oldValue.bitmap;
								if (!bitmap.isRecycled()) {
									bitmap.recycle();
								}
							}
						} else {
							mSoftBitmapCache.put(key, new SoftReference<CLBitmapCacheItem>(oldValue));
						}
					} else {
						mSoftBitmapCache.put(key, new SoftReference<CLBitmapCacheItem>(oldValue));
					}
				}
			}
		};

	}

	/**
	 * 
	 * @param bitmap
	 * @param info
	 * @author jianfeng.lao
	 * @CreateDate 2013-3-10
	 */
	public void putBitmap(Bitmap bitmap, CLBitmapInfo info) {
		if (info == null || info.getCacheKey() == null) {
			Log.e(TAG, "putBitmap info == null or cache key ==null");
			return;
		}
		if (bitmap == null || bitmap.isRecycled()) {
			Log.e(TAG, "bitmap == null");
			return;
		}
		Log.i(TAG, "putBitmap>>");

		CLBitmapCacheItem bc = new CLBitmapCacheItem();
		bc.bitmap = bitmap;
		bc.info = info;
		mHardBitmapCache.put(info.getCacheKey(), bc);

	}

	/**
	 * @param info
	 * @return
	 * @author jianfeng.lao
	 * @CreateDate 2013-3-10
	 */
	public Bitmap getBitmap(CLBitmapInfo info) {
		if (info == null || info.getCacheKey() == null) {
			Log.v(TAG, "getBitmap == null or cache key ==null");
			return null;
		}
		String key = info.getCacheKey();
		synchronized (mHardBitmapCache) {
			final CLBitmapCacheItem bitmap = mHardBitmapCache.get(info.getCacheKey());
			if (bitmap != null && !bitmap.bitmap.isRecycled()) {
				return bitmap.bitmap;
			} else if (bitmap != null && bitmap.bitmap != null && !bitmap.bitmap.isRecycled()) {
				mHardBitmapCache.remove(key);
			} else {
				mHardBitmapCache.remove(key);
			}
		}
		// 硬引用缓存区间中读取失败，从软引用缓存区间读取
		synchronized (mSoftBitmapCache) {
			if (!mSoftBitmapCache.containsKey(key)) {
				return null;
			}
			SoftReference<CLBitmapCacheItem> bitmapReference = mSoftBitmapCache.get(key);
			if (bitmapReference != null) {
				final CLBitmapCacheItem bitmap = bitmapReference.get();
				if (bitmap != null && !bitmap.bitmap.isRecycled())
					return bitmap.bitmap;
				else {
					Log.v(TAG, "soft reference 已经被回收");
					mSoftBitmapCache.remove(key);
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-3-10
	 */
	public void destory() {
		if (mHardBitmapCache != null) {
			mHardBitmapCache.evictAll();
		}
		if (mSoftBitmapCache != null) {
			mSoftBitmapCache.clear();
		}
	}

	/**
	 * 
	 * @param info
	 * @author jianfeng.lao
	 * @CreateDate 2013-3-10
	 */
	public void destoryItem(CLBitmapInfo info) {
		if (info != null && info.getCacheKey() != null) {
			CLBitmapCacheItem item = mHardBitmapCache.remove(info.getCacheKey());
			if (item != null) {
				if (item.bitmap != null && !item.bitmap.isRecycled()) {
					info.callBackBeforeRecycle(info);
					item.bitmap.recycle();
				}
			}
		}
	}


}
