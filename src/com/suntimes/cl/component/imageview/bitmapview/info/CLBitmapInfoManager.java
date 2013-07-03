package com.suntimes.cl.component.imageview.bitmapview.info;

import android.util.Log;

import com.suntimes.cl.component.imageview.bitmapview.cache.CLLruCache;
/**
 * 
 * 管理BitmapInfo pojo
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-4-27
 */
public class CLBitmapInfoManager {
	private static final String TAG = "BitmapInfoManager";
	private static CLBitmapInfoManager instance;
	private static final int MAX_SIZE=100;

	private CLLruCache<String, CLBitmapInfo> bitmapInfos;//Key一定要同BitmapInfo中的cacheKey对应

	private CLBitmapInfoManager() {
		super();
		bitmapInfos = new CLLruCache<String, CLBitmapInfo>(MAX_SIZE);
	}

	public static final CLBitmapInfoManager getInstance() {
		if (instance == null) {
			instance = new CLBitmapInfoManager();
		}
		return instance;
	}

	public CLBitmapInfo getBitmapInfo(String cacheKey) {
		if (bitmapInfos != null) {
			return bitmapInfos.get(cacheKey);
		} else {
			return null;
		}
	}

	public void addBitmapInfo(CLBitmapInfo info) {
		if (info == null||info.getCacheKey()==null) {
			Log.i(TAG, "addBitmapInfo == null");
			return;
		}
		if (bitmapInfos != null) {
			bitmapInfos.put(info.getCacheKey(), info);
		}
	}
	
	public void removeBitmapInfo(CLBitmapInfo info){
		if (info == null) {
			Log.i(TAG, "removeBitmapInfo == null");
			return;
		}
		if (bitmapInfos != null) {
			bitmapInfos.remove(info.getCacheKey());
		}
		
	}

}
