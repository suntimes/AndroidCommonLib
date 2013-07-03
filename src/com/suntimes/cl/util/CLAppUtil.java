package com.suntimes.cl.util;

import android.content.Context;

/**
 * @author Sean Zheng
 * @version 1.0
 * @CreateDate 2013-5-13
 */
public class CLAppUtil {

	public static String getAppVersion(Context context) {
		try {
			return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
