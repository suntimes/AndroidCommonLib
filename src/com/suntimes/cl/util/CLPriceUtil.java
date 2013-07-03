package com.suntimes.cl.util;

import java.text.DecimalFormat;

/**
 * 格式化价格的工具
 * 
 * @author Sean Zheng
 * @version 1.0
 * @CreateDate 2013-5-10
 */
public class CLPriceUtil {
	
	public static final String PRICE_FORMAT_PATTERN_1 = "###,##0";
	public static final String PRICE_FORMAT_PATTERN_2 = "###,##0.0";
	public static final String PRICE_FORMAT_PATTERN_3 = "###,##0.00";
	public static final String PRICE_FORMAT_PATTERN_4 = "###,##0.000";
	public static final String PRICE_FORMAT_PATTERN_5 = "###,##0.0000";

	/**
	 * 格式化价格
	 * @param price
	 * @param pattern 格式化模板，如"###,##0",可直接用现有的模板 => PRICE_FORMAT_PATTERN_1, PRICE_FORMAT_PATTERN_2, PRICE_FORMAT_PATTERN_3, PRICE_FORMAT_PATTERN_4, PRICE_FORMAT_PATTERN_5
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static String priceFormat(double price, String pattern) {
		String str = "0";
		double anotherNan = Double.NaN;
		if (Double.compare(price, anotherNan) != 0) {
			DecimalFormat df1 = new DecimalFormat(pattern);
			str = df1.format(price);
		} else {
			DecimalFormat df1 = new DecimalFormat(pattern);
			str = df1.format(str);
		}
		return str;
	}
	
	/**
	 * 格式化价格
	 * @param price
	 * @param pattern 格式化模板，如"###,##0",可直接用现有的模板 => PRICE_FORMAT_PATTERN_1, PRICE_FORMAT_PATTERN_2, PRICE_FORMAT_PATTERN_3, PRICE_FORMAT_PATTERN_4, PRICE_FORMAT_PATTERN_5
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static String priceFormat(float price, String pattern) {
		String str = "0";
		double anotherNan = Double.NaN;
		if (Double.compare(price, anotherNan) != 0) {
			DecimalFormat df1 = new DecimalFormat(pattern);
			str = df1.format(price);
		} else {
			DecimalFormat df1 = new DecimalFormat(pattern);
			str = df1.format(str);
		}
		return str;
	}
}
