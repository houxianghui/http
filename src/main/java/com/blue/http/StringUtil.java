package com.blue.http;

import java.io.UnsupportedEncodingException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hello world!
 *
 */
public class StringUtil 
{
	public static String utfFormat(String source) {
		if (source.contains("\\u")) {
			StringBuffer buf = new StringBuffer();
			Matcher m = Pattern.compile("\\\\u([0-9A-Fa-f]{4})").matcher(source);
			while (m.find()) {
				try {
					int cp = Integer.parseInt(m.group(1), 16);
					m.appendReplacement(buf, "");
					buf.appendCodePoint(cp);
				} catch (NumberFormatException e) {
				}
			}
			m.appendTail(buf);
			String result = buf.toString();
			return result;
		}
		return source;
	}
	/**
	 * 16进制字符串转换为字符串
	 *
	 * @param charsetName 用于编码 String 的 Charset
	 */
	public static String hexStr2Str(String hexStr, String charsetName) {
		hexStr = hexStr.toUpperCase();
		// hexStr.replace(" ", "");
		String str = "0123456789ABCDEF";
		char[] hexs = hexStr.toCharArray();
		byte[] bytes = new byte[hexStr.length() / 2];
		int n;

		for (int i = 0; i < bytes.length; i++) {
			n = str.indexOf(hexs[2 * i]) * 16;
			n += str.indexOf(hexs[2 * i + 1]);
			bytes[i] = (byte) (n & 0xFF);
		}
		String returnStr = "";// 返回的字符串
		if (charsetName == null) {
			// 编译器默认解码指定的 byte 数组，构造一个新的 String,
			// 比如我的集成开发工具即编码器android studio的默认编码格式为"utf-8"
			returnStr = new String(bytes);
		} else {
			// 指定的 charset 解码指定的 byte 数组，构造一个新的 String
			// utf-8中文字符占三个字节，GB18030兼容GBK兼容GB2312中文字符占两个字节，ISO8859-1是拉丁字符（ASCII字符）占一个字节
			try {
				returnStr = new String(bytes, charsetName);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		// charset还有utf-8,gbk随需求改变
		return returnStr;
    }
}
