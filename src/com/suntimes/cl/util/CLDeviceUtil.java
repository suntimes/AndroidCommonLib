package com.suntimes.cl.util;

import android.content.Context;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class CLDeviceUtil {

	public static final String TAG = "CLDeviceUtil";

	/**
	 * Convert px to dip value
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	public static int px2dip(Context context, int px) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (px / scale + 0.5f);
	}

	/**
	 * Convert dip to px value
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	public static int dip2px(Context context, int dip) {
		// return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip,
		// context.getResources().getDisplayMetrics());
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dip * scale + 0.5f);
	}

	/**
	 * Obtain the mac address
	 * @param context
	 * @return
	 * @author Dwyane Mo
	 * @CreateDate 2013-5-14 
	 */
	public static String getMac(Context context) {
		WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		String macAddress = wifiInfo.getMacAddress();
		return macAddress;
	}

	/**
	 * Detect whether SD card has mounted or not.
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-23
	 */
	public static boolean hasMountSDCard() {
		return android.os.Environment.getExternalStorageState().equals(
				android.os.Environment.MEDIA_MOUNTED);
	}

	/**
	 * Obtain SD card total size.
	 * 
	 * @return byte unit of SD card total size.
	 * @author Sean Zheng
	 * @CreateDate 2013-4-23
	 */
	public static long getExternalStorageTotalSize() {
		final StatFs statfs = new StatFs(Environment
				.getExternalStorageDirectory().getAbsolutePath());
		final long blockCount = statfs.getBlockCount();
		final long blockSize = statfs.getBlockSize();
		return blockCount * blockSize;
	}

	/**
	 * Obtain SD card free size.
	 * 
	 * @return byte unit of SD card free size.
	 */
	public static long getExternalStorageFreeSize() {
		final StatFs statfs = new StatFs(Environment
				.getExternalStorageDirectory().getAbsolutePath());
		final long availableBlocks = statfs.getAvailableBlocks();
		final long blockSize = statfs.getBlockSize();
		return availableBlocks * blockSize;
	}

	public static int getScreenWidth(Context context) {
		int screenSizeOne = -1;
		int screenSizeTwo = -1;

		if (screenSizeOne <= 0 || screenSizeTwo <= 0) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			if (wm != null) {
				wm.getDefaultDisplay().getMetrics(dm);
			}
			screenSizeOne = dm.widthPixels;
			screenSizeTwo = dm.heightPixels;
		}

		Configuration conf = context.getResources().getConfiguration();
		switch (conf.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			return screenSizeOne > screenSizeTwo ? screenSizeOne
					: screenSizeTwo;
		case Configuration.ORIENTATION_PORTRAIT:
			return screenSizeOne < screenSizeTwo ? screenSizeOne
					: screenSizeTwo;
		default:
			Log.e(TAG, "can't get screen width!");
		}
		return screenSizeOne;
	}

	public static int getScreenHeight(Context context) {
		int screenSizeOne = -1;
		int screenSizeTwo = -1;
		if (screenSizeOne <= 0 || screenSizeTwo <= 0) {
			DisplayMetrics dm = new DisplayMetrics();
			WindowManager wm = (WindowManager) context
					.getSystemService(Context.WINDOW_SERVICE);
			wm.getDefaultDisplay().getMetrics(dm);
			screenSizeOne = dm.widthPixels;
			screenSizeTwo = dm.heightPixels;
		}

		Configuration conf = context.getResources().getConfiguration();
		switch (conf.orientation) {
		case Configuration.ORIENTATION_LANDSCAPE:
			return screenSizeOne < screenSizeTwo ? screenSizeOne
					: screenSizeTwo;
		case Configuration.ORIENTATION_PORTRAIT:
			return screenSizeOne > screenSizeTwo ? screenSizeOne
					: screenSizeTwo;
		default:
			Log.e(TAG, "can't get screen height!");
		}
		return screenSizeOne;
	}

	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier(
				"status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	/**
	 * Obtain device wifi mac address(UDID)
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	public static String getDeviceUDID(Context context) {
		WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * Detect whether network is available or not
	 * 
	 * @return if WIFI or GRPS is available, return YES, else return NO
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	public static boolean isConnectNetWork(final Context context) {
		boolean result = false;
		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

		boolean isWifiConnected = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED ? true
				: false;
		NetworkInfo mobileState = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		boolean isGprsConnected = false;
		if (mobileState != null) {
			isGprsConnected = mobileState.getState() == NetworkInfo.State.CONNECTED ? true : false;
		}
		Log.d(TAG, "wifi connected:" + isWifiConnected + " gps connected:" + isGprsConnected);
		result = isWifiConnected || isGprsConnected ? true : false;
		return result;
	}

	/**
	 * Get Android System version, eg: 2.3
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-23
	 */
	public static String getOSVersion(Context context) {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * Get Device IMEI
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-5-13
	 */
	public static String getIMEI(Context context) {
		String imei = "";
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephonyManager != null) {
			imei = telephonyManager.getDeviceId();
			if (TextUtils.isEmpty(imei)) {
				imei = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
			}
		}
		return imei;
	}

}
