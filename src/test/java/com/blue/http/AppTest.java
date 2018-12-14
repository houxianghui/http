package com.blue.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

/**
 * Unit test for simple App.
 */
public class AppTest {
	@Test
	public void test() {
		String s = "\\u60a8\\u4f3c\\u4e4e\\u5df2\\u7ecf\\u7eed\\u547d\\u8fc7\\u4e86";
		convert(s);

	}

	void convert(String source) {

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
			System.out.println(result);
		}

	}
}
