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
}
