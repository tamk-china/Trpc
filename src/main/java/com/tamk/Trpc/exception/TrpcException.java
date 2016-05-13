package com.tamk.Trpc.exception;

/**
 * @author kuanqiang.tkq
 */
public class TrpcException extends Exception {
	private static final long serialVersionUID = -2393186626797990229L;

	public TrpcException(String msg) {
		super(msg);
	}
}
