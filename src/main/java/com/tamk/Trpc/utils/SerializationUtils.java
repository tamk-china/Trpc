package com.tamk.Trpc.utils;

import java.io.Serializable;

/**
 * @author kuanqiang.tkq
 */
public class SerializationUtils {
	public static <T extends Serializable> byte[] serialize(T obj) {
		return org.apache.commons.lang3.SerializationUtils.serialize(obj);
	}

	public static <T extends Serializable> T deserialize(byte[] data) {
		return org.apache.commons.lang3.SerializationUtils.deserialize(data);
	}

	public static int getUnsignedInt(byte[] bytes) {
		if (null == bytes || bytes.length < 4) {
			throw new IllegalArgumentException();
		}

		return ((bytes[0] < 0 ? (256 + bytes[0]) : bytes[0]) << 24) + ((bytes[1] < 0 ? (256 + bytes[1]) : bytes[1]) << 16) + ((bytes[2] < 0 ? (256 + bytes[2]) : bytes[2]) << 8)
				+ (bytes[3] < 0 ? (256 + bytes[3]) : bytes[3]);
	}

	public static byte[] getBytes(int unsignedInt) {
		byte[] bytes = new byte[4];

		bytes[0] = (byte) ((unsignedInt >> 24) & 255);
		bytes[1] = (byte) ((unsignedInt >> 16) & 255);
		bytes[2] = (byte) ((unsignedInt >> 8) & 255);
		bytes[3] = (byte) (unsignedInt & 255);

		return bytes;
	}
}
