package com.tamk.Trpc.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

/**
 * @author kuanqiang.tkq
 */
public class SocketUtils {
	public static byte[] readObj(InputStream is) throws IOException {
		byte[] size = IOUtils.readFully(is, 4);
		return IOUtils.readFully(is, getUnsignedInt(size));
	}

	public static void writeObj(OutputStream os, byte[] obj) throws IOException {
		IOUtils.write(getBytes(obj.length), os);
		IOUtils.write(obj, os);
	}

	private static int getUnsignedInt(byte[] bytes) {
		if (null == bytes || bytes.length < 4) {
			throw new IllegalArgumentException();
		}

		return ((bytes[0] < 0 ? (256 + bytes[0]) : bytes[0]) << 24) + ((bytes[1] < 0 ? (256 + bytes[1]) : bytes[1]) << 16) + ((bytes[2] < 0 ? (256 + bytes[2]) : bytes[2]) << 8)
				+ (bytes[3] < 0 ? (256 + bytes[3]) : bytes[3]);
	}

	private static byte[] getBytes(int unsignedInt) {
		byte[] bytes = new byte[4];

		bytes[0] = (byte) ((unsignedInt >> 24) & 255);
		bytes[1] = (byte) ((unsignedInt >> 16) & 255);
		bytes[2] = (byte) ((unsignedInt >> 8) & 255);
		bytes[3] = (byte) (unsignedInt & 255);

		return bytes;
	}
}
