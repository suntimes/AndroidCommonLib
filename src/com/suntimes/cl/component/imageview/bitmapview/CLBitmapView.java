package com.suntimes.cl.component.imageview.bitmapview;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfoManager;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo.BitmapInfoListener;
import com.suntimes.cl.component.imageview.bitmapview.info.CLBitmapInfo.Options;
import com.suntimes.cl.component.imageview.bitmapview.loader.CLBitmapLoader;

/**
 * CLItemBitmapView是一个拥有异步下载,缓存,异步加载,销毁图片功能的ImageView
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-5-6
 */
public class CLBitmapView extends ImageView implements BitmapInfoListener {
	private final static String TAG = "ItemBitmapView";

	private ScaleType mLoadSuccessScaleType;
	private ScaleType mDefaultIconScaleType;
	/**
	 * 加载Bitmap的UI标志符
	 */
	private String mUIFlag;
	private CLBitmapInfo mBitmapInfo;
	private SetBitmapRunnable mSetBitmapRunnable;

	public CLBitmapView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public CLBitmapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public CLBitmapView(Context context) {
		super(context);
		init();
	}

	private void init() {
		mSetBitmapRunnable = new SetBitmapRunnable();
	}

	/**
	 * 设置UI flag
	 * 
	 * @param uiFlag 加载Bitmap的UI标志符
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void setUiFlag(String uiFlag) {
		this.mUIFlag = uiFlag;
	}

	/**
	 * 设置BitmapInfo并且从cache中获取bitmap
	 * 
	 * @param cacheKey 每个BitmapInfo只有唯一key
	 * @param url 下载image url
	 * @param filePath 保存路径
	 * @param options bitmap可选参数
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void setBitmapInfo(String cacheKey, String url, String filePath, Options options) {
		// Log.v(TAG,"setBitmapInfo>>"+cacheKey+"\n file Path>>"+filePath);
		CLBitmapInfo biCache = CLBitmapInfoManager.getInstance().getBitmapInfo(cacheKey);
		if (mBitmapInfo != null) {
			mBitmapInfo.removeBitmapListener(this);
		}
		if (mBitmapInfo != null && mBitmapInfo.equals(biCache)) {} else if (biCache != null) {
			this.mBitmapInfo = biCache;
		} else {
			mBitmapInfo = new CLBitmapInfo(cacheKey, url, filePath, options);
			CLBitmapInfoManager.getInstance().addBitmapInfo(mBitmapInfo);
		}
		mBitmapInfo.setUiFlag(mUIFlag);
		mBitmapInfo.addBitmapInfoListener(this);
		loadImageFromCache();
	}

	/**
	 * load image form cache
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void loadImageFromCache() {
		if (mBitmapInfo != null) {
			boolean success = CLBitmapLoader.getInstance().loadBitmapFromCache(mBitmapInfo);
			if (!success) {
				setDefalutBitmap();
			}
		}
	}

	public void setDefalutBitmap() {
		Log.d(TAG, "setDefalutBitmap info" + mBitmapInfo);
		setDefalutIconScaleType();
		setImageBitmap(CLBitmapLoader.getInstance().getDefaultBitmap());

	}

	/**
	 * recycle bitmap from cache
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-5-2
	 */
	public void destory() {
		if (mBitmapInfo != null) {
			CLBitmapLoader.getInstance().recycleBitmapInfo(mBitmapInfo);
		}
	}

	public interface CLItemBitmapViewInterface {
		public CLBitmapView getItemBitmapView();
	}

	public void setScaleType(ScaleType defaultIconScaleType, ScaleType loadSuccessScaleType) {
		this.mLoadSuccessScaleType = loadSuccessScaleType;
		this.mDefaultIconScaleType = defaultIconScaleType;
	}

	public void setViewPagerScaleType() {
		setScaleType(ScaleType.CENTER_INSIDE, ScaleType.FIT_CENTER);
	}

	private void setDefalutIconScaleType() {
		if (mDefaultIconScaleType != null) {
			super.setScaleType(mDefaultIconScaleType);
		}
	}

	private void setLoadImageSuccessScaleType() {
		if (mLoadSuccessScaleType != null) {
			super.setScaleType(mLoadSuccessScaleType);
		}
	}

	/**
	 * 
	 * 使用BitmapLoader加载图片
	 * 
	 * @author jianfeng.lao
	 * @CreateDate 2013-4-27
	 */
	public void loadImageOneByOne() {
		if (mBitmapInfo == null) {
			return;
		}
		if (!CLBitmapLoader.getInstance().loadBitmapFromCache(mBitmapInfo)) {
			mSetBitmapRunnable.setBitmap(null);
			post(mSetBitmapRunnable);
			CLBitmapLoader.getInstance().addLoaderTask(mBitmapInfo);
		}

	}

	@Override
	public void onDownLoad(CLBitmapInfo info, boolean isSuccess) {
		Log.v(TAG, "onDownLoad info url>>" + info.getDownLoadUrl());
		Log.v(TAG, "onDownLoad view url>>" + mBitmapInfo.getDownLoadUrl());
		if (isSuccess) {
			loadImageOneByOne();
		}
	}

	@Override
	public void onBitmapLoad(CLBitmapInfo info, final Bitmap bitmap) {
		Log.i(TAG, "onBitmapLoad>>" + info);
		if (mBitmapInfo != null && mBitmapInfo.equals(info)) {
			mSetBitmapRunnable.setBitmap(bitmap);
			post(mSetBitmapRunnable);
		} else {
			Log.i(TAG, "onBitmapLoad>>key not campare");
		}
	}

	@Override
	public void beforeRecyle(CLBitmapInfo info) {
		Log.d(TAG, "beforeRecyle>>" + info);
		if (mBitmapInfo != null && mBitmapInfo.equals(info)) {
			mSetBitmapRunnable.setBitmap(null);
			post(mSetBitmapRunnable);
		} else {
			Log.d(TAG, "cache key not available,do not set defalut icon");
		}
	}

	private class SetBitmapRunnable implements Runnable {
		private Bitmap bitmap;

		public void run() {
			setImage(bitmap);
		}

		public void setBitmap(Bitmap bitmap) {
			this.bitmap = bitmap;
		}

	}

	private void setImage(Bitmap bitmap) {
		if (bitmap == null) {
			setDefalutBitmap();
		} else {
			setLoadImageSuccessScaleType();
			setImageBitmap(bitmap);
		}
	}

}
