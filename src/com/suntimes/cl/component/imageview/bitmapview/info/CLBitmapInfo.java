package com.suntimes.cl.component.imageview.bitmapview.info;

import java.util.Vector;

import android.graphics.Bitmap;

/**
 * 
 * Bitmap Info pojo
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-3-10
 */
public class CLBitmapInfo {
	private static final String TAG = "BitmapInfo";
	/**
	 * opt param
	 */
	private Options options;
	/**
	 * image path is store
	 */
	private String filePath;// 全路径
	/**
	 * download image url
	 */
	private String downLoadUrl;
	/**
	 */
	private Vector<BitmapInfoListener> listeners;
	/**
	 * 加载Bitmap的UI标志符
	 */
	private String UIFlag;
	/**
	 * 一定要set并且是要每一张Bitmap只有一个唯一key
	 */
	private String cacheKey;

	public CLBitmapInfo(String cacheKey, String downLoadUrl, String filePath, Options options) {
		super();
		this.cacheKey = cacheKey;
		this.downLoadUrl = downLoadUrl;
		this.filePath = filePath;
		this.options = options;
		listeners = new Vector<CLBitmapInfo.BitmapInfoListener>();
	}

	public interface BitmapInfoListener {
		public void onDownLoad(CLBitmapInfo info, boolean isSuccess);

		public void onBitmapLoad(CLBitmapInfo info, Bitmap bitmap);

		public void beforeRecyle(CLBitmapInfo info);
	}
	/**
	 * 
	 * 加载Image选项
	 * @author jianfeng.lao
	 * @version 1.0
	 * @CreateDate 2013-3-10
	 */
	public static class Options {
		/**
		 * unit pixel 如果>0,在加载图片时会用android的压缩方法加载
		 */
		public float width = -1;
		/**
		 * unit pixel 如果>0,在加载图片时会用android的压缩方法加载
		 */
		public float height = -1;
		/**
		 * will recycle bitmap after BitmapCache full
		 */
		public boolean notPullSoftCache = false;

		public Options() {
			super();
		}

		public Options(boolean notPullSoftCache) {
			super();
			this.notPullSoftCache = notPullSoftCache;
		}

		public Options(float width, float height) {
			super();
			this.width = width;
			this.height = height;
		}

		@Override
		public String toString() {
			return "Options --> witdh:" + width + " height:" + height;
		}

	}

	public Options getOptions() {
		return options;
	}

	public String getFilePath() {
		return filePath;
	}

	public String getDownLoadUrl() {
		return downLoadUrl;
	}

	public void setDownLoadUrl(String downLoadUrl) {
		this.downLoadUrl = downLoadUrl;
	}

	public void removeBitmapListener(BitmapInfoListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.removeElement(listener);
	}

	public void removeAllBitmapListener() {
		if (listeners == null) {
			return;
		}
		listeners.removeAllElements();
	}

	public void setOptions(Options options) {
		this.options = options;
	}

	public void addBitmapInfoListener(BitmapInfoListener listener) {
		if (listeners == null) {
			return;
		}
		if (!listeners.contains(listener)) {
			listeners.addElement(listener);
		}
	}

	public String getUiFlag() {
		return UIFlag;
	}

	public void setUiFlag(String uiFlag) {
		this.UIFlag = uiFlag;
	}

	public String getCacheKey() {
		return cacheKey;
	}

	public void setCacheKey(String cacheKey) {
		this.cacheKey = cacheKey;
	}

	public void setPath(String path) {
		this.filePath = path;
	}

	public boolean equals(String cacheKey) {
		if (getCacheKey().equals(cacheKey)) {
			return true;
		} else {
			return false;
		}
	}

	public void callBackOnLoad(Bitmap bitmap, CLBitmapInfo info) {
		if (listeners != null) {
			for (BitmapInfoListener listener : listeners) {
				listener.onBitmapLoad(info, bitmap);
			}
		}
	}

	public void callBackDownload(CLBitmapInfo info, boolean isSuccess) {
		if (listeners != null) {
			for (BitmapInfoListener listener : listeners) {
				listener.onDownLoad(info, isSuccess);
			}
		}
	}

	public void callBackBeforeRecycle(CLBitmapInfo info) {
		if (listeners != null) {
			for (BitmapInfoListener listener : listeners) {
				listener.beforeRecyle(info);
			}
		}
	}

	@Override
	public String toString() {
		return "BitmapInfo [cacheKey=" + cacheKey + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (o != null && o instanceof CLBitmapInfo) {
			CLBitmapInfo bi = (CLBitmapInfo) o;
			if (bi.getCacheKey().equals(getCacheKey())) {
				return true;
			}
		}
		return super.equals(o);
	}

}
