package com.suntimes.cl.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.R;
import android.content.Context;
import android.util.Log;
import android.view.KeyEvent;
/**
 * 导出application下所有文件到/sdcard/debug_folder
 * 
 * @author jianfeng.lao
 * @version 1.0
 * @CreateDate 2013-5-8
 */
public final class CLDebugUtil {

	private static final String TAG = "CLDebugUtil";


	public static boolean exportDatabaseByClickMenuKey(Context context, int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			exportSystemFolder(context);
		}
		return false;
	}

	public static void exportSystemFolder(Context context) {
		String packageName = R.class.getPackage().getName();
		File systemFolder = new File("/data/data/" + packageName);
		if (!systemFolder.exists()) {
			return;
		}
		try {
			copyFolder(systemFolder, new File("/sdcard/debug_folder/" + packageName));
			Log.d(TAG, "Export system file success.");
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "export system file fail.");
		}
	}

	private static void copyFile(File from, File to) {
		if (!from.exists()) {
			return;
		}
		FileInputStream in = null;
		FileOutputStream out = null;
		try {
			in = new FileInputStream(from);
			out = new FileOutputStream(to);

			byte bt[] = new byte[1024];
			int c;
			while ((c = in.read(bt)) > 0) {
				out.write(bt, 0, c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
				if (out != null) {
					out.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}

	private static void copyFolder(File fromFolder, File toFolder) throws IOException {
		if (!fromFolder.exists()) {
			return;
		}
		if (toFolder.exists()) {
			deleteFileAndFolder(toFolder);
		}
		copyFolder(fromFolder.getAbsolutePath(), toFolder.getAbsolutePath());
	}

	public static void copyFolder(String sourceDir, String targetDir) throws IOException {
		// 新建目标目录
		(new File(targetDir)).mkdirs();
		// 获取源文件夹当前下的文件或目录
		File[] file = (new File(sourceDir)).listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				// 源文件
				File sourceFile = file[i];
				// 目标文件
				File targetFile = new File(new File(targetDir).getAbsolutePath() + File.separator + file[i].getName());
				copyFile(sourceFile, targetFile);
			}
			if (file[i].isDirectory()) {
				// 准备复制的源文件夹
				String dir1 = sourceDir + "/" + file[i].getName();
				// 准备复制的目标文件夹
				String dir2 = targetDir + "/" + file[i].getName();
				copyFolder(dir1, dir2);
			}
		}
	}

	private static void deleteFileAndFolder(File fileOrFolder) {
		if (fileOrFolder == null || !fileOrFolder.exists()) {
			return;
		}
		if (fileOrFolder.isDirectory()) {
			File[] children = fileOrFolder.listFiles();
			if (children != null) {
				for (File childFile : children) {
					deleteFileAndFolder(childFile);
				}
			}
		}
		else {
			fileOrFolder.delete();
		}
	}

}
