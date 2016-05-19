package com.tamk.Trpc.utils;

/**
 * @author kuanqiang.tkq
 */
public class ProtocolUtils {
	public static int getUnsignedInt(byte[] bytes) {
		if (null == bytes || bytes.length < 4) {
			throw new IllegalArgumentException();
		}

		return ((bytes[0] & 0xff) << 24) + ((bytes[1] & 0xff) << 16) + ((bytes[2] & 0xff) << 8) + (bytes[3] & 0xff);
	}

	public static byte[] getBytes(int unsignedInt) {
		byte[] bytes = new byte[4];

		bytes[0] = (byte) ((unsignedInt >> 24) & 0xff);
		bytes[1] = (byte) ((unsignedInt >> 16) & 0xff);
		bytes[2] = (byte) ((unsignedInt >> 8) & 0xff);
		bytes[3] = (byte) (unsignedInt & 0xff);

		return bytes;
	}

	public static void main(String[] args) {
		System.out.println(getUnsignedInt(getBytes(1)));
	}
}
