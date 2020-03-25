package com.jyroscope.util;

public class Hex {
	
	private static final char[] HEX = {
		'0', '1', '2', '3', '4', '5', '6', '7',
		'8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
	};
	
	public static String toHex(byte[] data) {
		StringBuilder builder = new StringBuilder(data.length * 2);
		for (int i=0; i<data.length; i++) {
			byte value = data[i];
			builder.append(HEX[(value & 0xf0) >> 4]);
			builder.append(HEX[value & 0x0f]);
		}
		return builder.toString();
	}
	
}
