package com.suntimes.cl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;


import android.content.Context;
import android.os.Environment;
/**
 * 捕捉未catch异常,保存到sdcard下/crash_log/(application package name)
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-5-8
 */
public class CLCrashUtil implements UncaughtExceptionHandler {
	private Context mContext;
	private UncaughtExceptionHandler mUncaughtExceptionHandler;
	private static final String SDCARD_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

	private static CLCrashUtil mInstance;

	private CLCrashUtil() {
		super();
	}

	public static CLCrashUtil getInstance() {
		if (mInstance == null) {
			mInstance = new CLCrashUtil();
			
			
		}
		return mInstance;
	}

	/**
	 * 初始化方法
	 * 
	 * @param context 上下文对象
	 */
	public void init(Context context) {
		this.mContext = context;
		mUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		if (!handleException(thread, ex) && mUncaughtExceptionHandler != null && mContext != null) {
			mUncaughtExceptionHandler.uncaughtException(thread, ex);
		}
	}

	/**
	 * 保存异常
	 * @param thread
	 * @param ex
	 * @return
	 * @author jianfeng.lao
	 * @CreateDate 2013-5-8
	 */
	private boolean handleException(Thread thread, Throwable ex) {
		StringBuilder sb = new StringBuilder();
		long startTimer = System.currentTimeMillis();
		SimpleDateFormat formatter = new SimpleDateFormat(
				"yyyy-MM-dd_HH_mm_ss");
		Date firstDate = new Date(System.currentTimeMillis()); // 第一次创建文件，也就是开始日期
		String date = formatter.format(firstDate);
		sb.append(date+"\n"); // 把当前的日期写入到字符串中
		Writer writer = new StringWriter();
		PrintWriter pw = new PrintWriter(writer);
		ex.printStackTrace(pw);
		String errorresult = writer.toString();
		sb.append(errorresult);
		sb.append("\n");
		File fileDir = new File(SDCARD_PATH + "/crash_log/" + mContext.getPackageName());
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		FileOutputStream fileOutputStream = null;
		try {
			File files = new File(fileDir, date + ".log");
			if (!files.exists()) {
				files.createNewFile();
			}
			fileOutputStream = new FileOutputStream(files,
					true);
			fileOutputStream.write(sb.toString().getBytes());

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			CLFileUtil.closeIOStream(fileOutputStream);
		}
		mUncaughtExceptionHandler.uncaughtException(thread, ex);

		return true;
	}

}
