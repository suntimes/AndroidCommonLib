package com.suntimes.cl.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Sean Zheng
 * @version 1.0
 * @CreateDate 2013-4-26
 */
public class CLDateUtil {
	
	public static final String TAG = "CLDateUtil";
	
	public static final String DATE_FORMAT_PATTERN_1 = "dd/MM/yyyy HH:mm";
	public static final String DATE_FORMAT_PATTERN_2 = "yyyy-MM-dd'T'HH:mm:ss+SSS";
	public static final String DATE_FORMAT_PATTERN_3 = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Returns the current date in a given format.
	 * eg.=> CLDateUtil.now("dd MMMMM yyyy")
	 * @param dateFormat a date format (See examples)
	 * @return the current formatted date/time
	 * @author Sean Zheng
	 * @CreateDate 2013-5-13
	 */
	public static String now(String dateFormat)
	{
		SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
		long now = System.currentTimeMillis();
		return sdf.format(new Date(now));
	}

	/**
	 * 
	 * @param dateString
	 * @param fromPattern
	 * @param toPattern
	 * @return
	 * @author Sean Zheng
	 * @CreateDate 2013-4-26
	 */
	public static String formatDate(String dateString, String fromPattern, String toPattern) {
		Date date = formatDate(dateString, fromPattern);
		String result = formatDate(date, toPattern);
		CLLog.i(TAG, String.format("date2String >>> from pattern = %s to pattern = %s, from = %s, to = %s", fromPattern, toPattern, dateString, result));
		return result;
	}
	
	/**
	 * 
	 * @param dateString
	 * @param pattern
	 * @return
	 * @author Sean Zheng
	 * @CreateDate 2013-4-26
	 */
	public static Date formatDate(String dateString, String pattern) {
		Date date = null;
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		try {
			date = sdf.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	/**
	 * 
	 * @param date
	 * @param pattern
	 * @return
	 * @author Sean Zheng
	 * @CreateDate 2013-4-26
	 */
	public static String formatDate(Date date, String pattern) {
		SimpleDateFormat dataFormat = new SimpleDateFormat(pattern);	
		return dataFormat.format(date);
	}
}
