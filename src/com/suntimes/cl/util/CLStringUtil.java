package com.suntimes.cl.util;

/**
 * @version 1.0
 * @author Sean Zheng
 * @CreateDate 2013-4-24
 */
public class CLStringUtil {

	/**
	 * 此方法将String类型对象转换成Integer对象,
	 * 失败的返回值为0。
	 * 
	 * @param str 
	 * @return	int数据
	 * @author Zhao Wang
	 * @CreateDate 2013-5-21
	 */
	public static int convertStringToInt(String str) {
		return convertStringToInt(str, 0);
	}
	
	/**
	 * 此方法将String类型对象转换成Integer对象，
	 * 并自定义转换失败的返回数值。
	 * 
	 * @param str
	 * @param failValue 失败返回值
	 * @return	int数据
	 * @author Zhao Wang
	 * @CreateDate 2013-5-21
	 */
	public static int convertStringToInt(String str, int failValue) {
		if (str == null) {
			return failValue;
		}
		try {
			return Integer.parseInt(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return failValue;
	}
	
	/**
	 * 此方法将String类型对象转换成Long对象，
	 * 并自定义转换失败的返回数值。
	 * 
	 * @param str
	 * @param failValue	失败返回值
	 * @return long数据
	 * @author Zhao Wang
	 * @CreateDate 2013-5-21
	 */
	public static long convertStringToLong(String str, long failValue) {
		if (str == null) {
			return failValue;
		}
		try {
			return Long.parseLong(str);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return failValue;
	}

	/**
	 * 将十六进制转成byte数组返回
	 *  (此方法存需要修改)？？？
	 *  input.length() = 1 时，存在问题，实际转换效果需要完善......
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static byte[] hexToByte(String input) {
		byte[] output = new byte[input.length() / 2];
		String input2 = input.toLowerCase();
		for (int i = 0; i < input2.length(); i += 2) {
			output[i / 2] = hexToByte(input2.charAt(i), input2.charAt(i + 1));
		}
		return output;
	}

	private static byte hexToByte(char char1, char char2) {
		// Returns hex String representation of byte b
		byte output = 0x00;
		if (char1 == '0') {
			output = 0x00;
		} else if (char1 == '1') {
			output = 0x10;
		} else if (char1 == '2') {
			output = 0x20;
		} else if (char1 == '3') {
			output = 0x30;
		} else if (char1 == '4') {
			output = 0x40;
		} else if (char1 == '5') {
			output = 0x50;
		} else if (char1 == '6') {
			output = 0x60;
		} else if (char1 == '7') {
			output = 0x70;
		} else if (char1 == '8') {
			output = (byte) 0x80;
		} else if (char1 == '9') {
			output = (byte) 0x90;
		} else if (char1 == 'a') {
			output = (byte) 0xa0;
		} else if (char1 == 'b') {
			output = (byte) 0xb0;
		} else if (char1 == 'c') {
			output = (byte) 0xc0;
		} else if (char1 == 'd') {
			output = (byte) 0xd0;
		} else if (char1 == 'e') {
			output = (byte) 0xe0;
		} else if (char1 == 'f') {
			output = (byte) 0xf0;
		}

		if (char2 == '0') {
			output = (byte) (output | (byte) 0x00);
		} else if (char2 == '1') {
			output = (byte) (output | (byte) 0x01);
		} else if (char2 == '2') {
			output = (byte) (output | (byte) 0x02);
		} else if (char2 == '3') {
			output = (byte) (output | (byte) 0x03);
		} else if (char2 == '4') {
			output = (byte) (output | (byte) 0x04);
		} else if (char2 == '5') {
			output = (byte) (output | (byte) 0x05);
		} else if (char2 == '6') {
			output = (byte) (output | (byte) 0x06);
		} else if (char2 == '7') {
			output = (byte) (output | (byte) 0x07);
		} else if (char2 == '8') {
			output = (byte) (output | (byte) 0x08);
		} else if (char2 == '9') {
			output = (byte) (output | (byte) 0x09);
		} else if (char2 == 'a') {
			output = (byte) (output | (byte) 0x0a);
		} else if (char2 == 'b') {
			output = (byte) (output | (byte) 0x0b);
		} else if (char2 == 'c') {
			output = (byte) (output | (byte) 0x0c);
		} else if (char2 == 'd') {
			output = (byte) (output | (byte) 0x0d);
		} else if (char2 == 'e') {
			output = (byte) (output | (byte) 0x0e);
		} else if (char2 == 'f') {
			output = (byte) (output | (byte) 0x0f);
		}
		return output;
	}

	/**
	 * 如果字符串超过指定的长度，则截取前面的部分+...的方式返回
	 * 
	 * @param limit 字符串超过的长度
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static String ellipsizeString(String string, int limit) {
		String retValue = "";
		if (string != null) {
			if (string.length() > limit) {
				retValue = string.substring(0, limit) + "...";
			}
			else {
				retValue = string;
			}
		}
		return retValue;
	}

	/**
	 * 判断字符串是否为null, 或者为空字符串
	 * 
	 * @param str
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static boolean isEmptyString(String str) {
		return str == null || str.trim().equals("");
	}

	/**
	 * 将string中的特殊转义字符全部转换成普通的字符串，如"&lt;"转换成 "<"
	 * 
	 * @author Sean Zheng
	 * @CreateDate 2013-5-10
	 */
	public static String replaceEscapeSequence(String input) {
		String output = null;
		if (input != null) {
			output = input.replaceAll("&lt;", "<");
			output = output.replaceAll("&gt;", ">");
			output = output.replaceAll("&amp;", "&");
			output = output.replaceAll("&apos;", "��");
			output = output.replaceAll("&quot;", "\"");
		}
		return output;
	}
}
