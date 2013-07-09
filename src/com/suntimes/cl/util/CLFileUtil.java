package com.suntimes.cl.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

/**
 * 
 * @author Sean Zheng
 * @version 1.0 Create at 2013-4-23
 */
public class CLFileUtil {

	public static final String TAG = "CLFileUtil";

	public static File createFile(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			boolean flag = file.getParentFile().mkdirs();
			CLLog.i(TAG, "createFile flag:[" + flag + "] createFile >>>  " + filePath);
		}
		return file;
	}

	public static void deleteFile(String filePath) {
		if (filePath != null) {
			File file = new File(filePath);
			deleteFile(file);
		}
	}

	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else {
				File[] files = file.listFiles();
				if (files != null) {
					for (File f : files) {
						deleteFile(f);
					}
				}
				file.delete();
			}
		}
	}

	public static void copyFile(File from, File to) {
		FileInputStream in = null;
		FileOutputStream out = null;

		try {
			if (!to.getParentFile().exists()) {
				to.getParentFile().mkdirs();
			}
			CLLog.i(TAG, "copy file from " + from.getPath() + "  to " + to.getPath());

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
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static void copyFile(InputStream in, OutputStream out) {

		try {

			byte bt[] = new byte[1024];
			int c;
			while ((c = in.read(bt)) > 0) {
				out.write(bt, 0, c);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

	public static void copyFloder(File from, File to) {
		File[] files = from.listFiles();
		for (File file : files) {
			String toPath = to.getPath() + "/" + file.getName();
			if (file.isFile()) {
				File toFile = new File(toPath);
				if (!toFile.exists()) {
					copyFile(file, new File(toPath));
				}
			} else {
				copyFloder(file, new File(toPath));
			}
		}
	}

	public static String createCacheFile(String fileName, Context context) {
		File cacheDir = context.getCacheDir();
		String path = cacheDir.getPath() + "/" + fileName;
		File file = new File(path);
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}
		return path;
	}

	public static String readFile(String path) {
		StringBuffer str = new StringBuffer("");
		try {
			InputStreamReader ir = new InputStreamReader(new FileInputStream(path), "UTF8");
			BufferedReader br = new BufferedReader(ir);
			String data = br.readLine();

			while (data != null) {
				str.append(data);
				data = br.readLine();
			}
			br.close();
			ir.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return str.toString();
	}

	public static List<String> readFileOfList(String path) {
		List<String> list = new ArrayList<String>();
		InputStreamReader ir = null;
		BufferedReader br = null;
		try {
			ir = new InputStreamReader(new FileInputStream(path), "UTF8");
			br = new BufferedReader(ir);
			String data = br.readLine();

			while (data != null) {
				if (!"".equals(data)) {
					list.add(data);
				}
				data = br.readLine();
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (ir != null) {
				try {
					ir.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
		return list;
	}

	public static void writeFileEnd(String filepath, String text) {
		OutputStreamWriter wr = null;
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			wr = new OutputStreamWriter(new FileOutputStream(filepath, true),
					"UTF8");
			wr.write(text);
			wr.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wr != null) {
				try {
					wr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void writeFile(String filepath, String text) {
		OutputStreamWriter wr = null;
		try {
			File file = new File(filepath);
			if (!file.exists()) {
				file.getParentFile().mkdirs();
			}
			wr = new OutputStreamWriter(new FileOutputStream(filepath, false),
					"UTF8");
			wr.write(text);
			wr.flush();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (wr != null) {
				try {
					wr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void saveBitmapToFile(Bitmap bitmap, String path) {
		CompressFormat format = Bitmap.CompressFormat.PNG;
		int quality = 100;
		OutputStream stream = null;
		try {

			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			stream = new FileOutputStream(path);
			bitmap.compress(format, quality, stream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void saveInputStreamAsFile(InputStream inputStream, File target) throws IOException {
		final FileOutputStream fos = new FileOutputStream(target);
		writeFile(inputStream, fos);
		closeIOStream(fos);
		closeIOStream(inputStream);
	}

	private static void writeFile(InputStream inputStream, OutputStream outputStream) throws IOException {
		final byte[] buf = new byte[10240];
		int len;
		while ((len = inputStream.read(buf)) != -1) {
			outputStream.write(buf, 0, len);
		}
		outputStream.flush();
		closeIOStream(outputStream);
		closeIOStream(inputStream);
	}

	public static void remove(String filePath) {
		try {
			File file = new File(filePath);
			remove(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void remove(File file) {
		if (file != null) {
			if (!file.isDirectory()) {
				file.delete();
			}
			else {
				String[] children = file.list();
				int size = children.length;
				for (int i = 0; i < size; i++) {
					remove(new File(file, children[i]));
				}
				file.delete();
			}
		}
	}

	/**
	 * 
	 * @param file
	 * @param newFile Must contain full path.
	 * @param isDeleteExistDestFile
	 * @return
	 */
	public static boolean renameFile(File file, File newFile, boolean isDeleteExistDestFile) {
		if (newFile.exists()) {
			if (isDeleteExistDestFile) {
				newFile.delete();
			}
		}
		return file.renameTo(newFile);
	}

	public static void closeIOStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Create file or directory.
	 * 
	 * @param name The name for directory or file.Directories are denoted with a trailing slash "/" .
	 * @param autoBuildDir boolean indicating whether super-folder should be create or not.
	 * @param deleteExist boolean indicating whether delete the exist file or not.
	 * @return true if the file is created successfully,false otherwise(Maybe the file is already exist,or other
	 *         problems).
	 */
	public static boolean createFile(String fcPath, boolean autoBuildDir,
			boolean deleteExist) {
		File file = new File(fcPath);
		try {
			if (deleteExist) {
				if (file.exists()) {
					file.delete();
					file = new File(fcPath);
				}
			}
			if (autoBuildDir) {
				file.getParentFile().mkdirs();
			}
			if (fcPath.endsWith("/")) {
				return file.mkdir();
			}
			return file.createNewFile();
		} catch (Exception e) {
			CLLog.e(TAG, "createFile>>>e=" + e.toString());
		}
		return false;
	}

	public static boolean isExistFile(String name) {
		File file = new File(name);
		return file.exists();
	}

	public static void readSomeDataFromFile(String path, byte[] data) {
		InputStream is = null;
		try {
			File file = new File(path);
			is = new FileInputStream(file);
			is.read(data);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static byte[] buildDataFromFile(File file) {
		return buildDataFromFile(file.getAbsolutePath());
	}

	public static byte[] buildDataFromFile(String path) {
		return buildDataFromFile(path, 1024);
	}

	public static byte[] buildDataFromFile(String path, int bufferSize) {
		InputStream is = null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		try {
			is = new FileInputStream(file);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		ByteArrayOutputStream baos = null;
		if (is != null) {
			try {
				baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[bufferSize];
				int i = 0;
				while ((i = is.read(buffer)) != -1) {
					baos.write(buffer, 0, i);
				}
				baos.flush();
				return baos.toByteArray();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if (baos != null) {
					try {
						baos.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return null;
	}

	public static boolean writeStringToFile(String text, String filePath) {
		OutputStream os = null;
		try {
			File file = new File(filePath);
			if (file.exists()) {
				file.delete();
			}
			file.createNewFile();
			os = new FileOutputStream(file);
			byte[] data = text.getBytes("UTF-8");
			os.write(data);
			os.flush();
			os.close();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static File[] listFilesByDirectory(String directoryPath, boolean isOnlyFile) {
		File dir = new File(directoryPath);
		if (!dir.exists()) {
			return null;
		}
		File[] files = null;
		if (isOnlyFile) {
			files = dir.listFiles(new FileFilter() {
				@Override
				public boolean accept(File pathname) {
					if (pathname.isFile()) {
						return true;
					}
					return false;
				}
			});
		}
		else {
			files = dir.listFiles();
		}
		return files;
	}

	/**
	 * 将zip文件解压到指定的目录下面
	 * 
	 * @param folerPath zip文件解压后的路径
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	public static void unZipFile(File zipFile, String folderPath) throws ZipException, IOException {
		ZipFile zfile = new ZipFile(zipFile);
		Enumeration<? extends ZipEntry> zList = zfile.entries();
		ZipEntry ze = null;
		byte[] buf = new byte[1024];
		while (zList.hasMoreElements()) {
			ze = (ZipEntry) zList.nextElement();
			if (ze.isDirectory()) {
				String dirstr = folderPath + ze.getName();
				dirstr = new String(dirstr.getBytes("8859_1"), "UTF-8");
				File f = new File(dirstr);
				f.mkdir();
				continue;
			}
			OutputStream os = new BufferedOutputStream(new FileOutputStream(getZipFile(folderPath, ze.getName())));
			InputStream is = new BufferedInputStream(zfile.getInputStream(ze));
			int readLen = 0;
			while ((readLen = is.read(buf, 0, 1024)) != -1) {
				os.write(buf, 0, readLen);
			}
			is.close();
			os.close();
		}
		zfile.close();
		CLLog.d("unZipFile", "unZip successful.");
	}

	/**
	 * 给定根目录，返回一个相对路径所对应的实际文件
	 * 
	 * @param baseDir 指定根目录
	 * @param absFileName 相对路径名，来自于ZipEntry中的name
	 * @return File 实际的文件
	 * @author Sean Zheng
	 * @CreateDate 2013-4-24
	 */
	private static File getZipFile(String baseDir, String absFileName) {
		String[] dirs = absFileName.split("/");
		File ret = new File(baseDir);
		String substr = null;
		if (dirs.length > 1) {
			for (int i = 0; i < dirs.length - 1; i++) {
				substr = dirs[i];
				try {
					substr = new String(substr.getBytes("8859_1"), "GB2312");

				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
				ret = new File(ret, substr);

			}
			if (!ret.exists())
				ret.mkdirs();
			substr = dirs[dirs.length - 1];
			try {
				substr = new String(substr.getBytes("8859_1"), "GB2312");
				CLLog.d("unZipFile", "substr = " + substr);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			ret = new File(ret, substr);
			CLLog.d("unZipFile", "2ret = " + ret);
			return ret;
		}
		return ret;
	}
}
