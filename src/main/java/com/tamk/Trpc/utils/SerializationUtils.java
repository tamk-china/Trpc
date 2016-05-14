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

	public static int getInt(byte[] bytes) {
		if (null == bytes || bytes.length < 4) {
			throw new IllegalArgumentException();
		}

		return ((int)bytes[0] << 24) + ((int)bytes[1] << 16) + ((int)bytes[2] << 8) + (int)bytes[3];
	}

	public static byte[] getBytes(int data) {
		byte[] bytes = new byte[4];

		bytes[0] = (byte) ((data >> 24) & 255);
		bytes[1] = (byte) ((data >> 16) & 255);
		bytes[2] = (byte) ((data >> 8) & 255);
		bytes[3] = (byte)(data & 255);

		return bytes;
	}

	public static void main(String[] args) {
		System.out.println(getInt(getBytes(1231244)));
		
		int a = 255;
		System.out.println((byte)a);
	}
}
