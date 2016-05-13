package com.tamk.Trpc.pool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * @author kuanqiang.tkq
 */
public class ThreadPool {
	public static final ThreadPool INSTANCE = new ThreadPool();

	private ExecutorService executorService;

	private ThreadPool() {
		executorService = Executors.newFixedThreadPool(20, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable arg0) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
}
