package com.suntimes.cl.component.imageview.bitmapview.cache;


public class CLDefalutBitmapCache extends CLBitmapCacheUtil {
	private static CLDefalutBitmapCache instance;

	private CLDefalutBitmapCache() {
		super(CACHE_10M);
	}

	public static CLDefalutBitmapCache getInstance() {
		if (instance == null) {
			instance = new CLDefalutBitmapCache();
		}
		return instance;
	}


}
