package com.suntimes.cl.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;

public class CLLog {

	private static boolean LOGGABLE = true;
	private static boolean ISLOGTOFILE = false;

	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public static String SDCARD = Environment
			.getExternalStorageDirectory().getAbsolutePath() + "/CommonLib";

	/**
	 * Whether print log to the LogCat panel
	 * 
	 * @CreateDate 2013-4-24
	 */
	public static void setLogable(boolean loggable) {
		LOGGABLE = loggable;
	}

	/**
	 * Whether write the log to the log file in SD card
	 * 
	 * @CreateDate 2013-4-24
	 */
	public static void setLogToFile(boolean isLogToFile) {
		ISLOGTOFILE = isLogToFile;
	}

	public static void v(String tag, String msg) {
		if (LOGGABLE) {
			android.util.Log.v(tag, msg);
			toFiletoFile(tag, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (LOGGABLE) {
			android.util.Log.d(tag, msg);
			toFiletoFile(tag, msg);
		}
	}

	public static void i(String tag, String msg) {
		if (LOGGABLE) {
			android.util.Log.i(tag, msg);
			toFiletoFile(tag, msg);
		}
	}

	public static void e(String tag, String msg) {
		if (LOGGABLE) {
			android.util.Log.e(tag, msg);
			toFiletoFile(tag, msg);
		}
	}

	public static void e(String tag, String msg, Exception e) {
		if (LOGGABLE) {
			android.util.Log.e(tag, msg, e);
			toFiletoFile(tag, msg);
		}
	}

	public static void w(String tag, String msg) {
		if (LOGGABLE) {
			android.util.Log.w(tag, msg);
			toFiletoFile(tag, msg);
		}
	}

	public static void toFiletoFile(String tag, String msg) {
		String fileName = "log.txt";
		toFiletoFile(tag, fileName, msg);
	}

	public static void toFiletoFile(String tag, String fileName, String msg) {
		try {
			if (LOGGABLE && ISLOGTOFILE) {
				msg = "[" + SDF.format(new Date()) + " - " + tag + "]" + msg + "\n";

				CLFileUtil.writeFileEnd(SDCARD + "/" + fileName, msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
