package org.hdl.anima.common.io;

import java.util.Arrays;

/**
 * DumpFormatter
 * @author qiuhd
 * @since 2014-2-19
 * @version V1.0.0
 */
public class DumpFormatter {

	public static final char TOKEN_INDENT_OPEN = '{';
	public static final char TOKEN_INDENT_CLOSE = '}';
	public static final char TOKEN_DIVIDER = ';';

	public static String prettyPrintByteArray(byte[] bytes) {
		if (bytes == null) {
			return "Null";
		}
		return String.format("Byte[%s]",
				new Object[] { Integer.valueOf(bytes.length) });
	}

	public static String prettyPrintDump(String rawDump) {
		StringBuilder buf = new StringBuilder();
		int indentPos = 0;

		for (int i = 0; i < rawDump.length(); i++) {
			char ch = rawDump.charAt(i);

			if (ch == '{') {
				indentPos++;
				buf.append("\n").append(getFormatTabs(indentPos));
			} else if (ch == '}') {
				indentPos--;
				if (indentPos < 0) {
					throw new IllegalStateException(
							"Argh! The indentPos is negative. TOKENS ARE NOT BALANCED!");
				}
				buf.append("\n").append(getFormatTabs(indentPos));
			} else if (ch == ';') {
				buf.append("\n").append(getFormatTabs(indentPos));
			} else {
				buf.append(ch);
			}
		}

		if (indentPos != 0) {
			throw new IllegalStateException(
					"Argh! The indentPos is not == 0. TOKENS ARE NOT BALANCED!");
		}
		return buf.toString();
	}

	private static String getFormatTabs(int howMany) {
		return strFill('\t', howMany);
	}

	private static String strFill(char c, int howMany) {
		char[] chars = new char[howMany];
		Arrays.fill(chars, c);

		return new String(chars);
	}
}
