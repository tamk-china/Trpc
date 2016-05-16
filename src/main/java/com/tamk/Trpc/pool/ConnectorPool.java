package com.tamk.Trpc.pool;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalListener;
import com.google.common.cache.RemovalNotification;
import com.tamk.Trpc.constants.Constants;

public class ConnectorPool {
	public static final ConnectorPool INSTANCE = new ConnectorPool();

	// key = interfaceName_remoteIp, value = socket
	private Cache<String, Socket> pool = CacheBuilder.newBuilder().maximumSize(10240).expireAfterAccess(5000, TimeUnit.MILLISECONDS).initialCapacity(128).softValues()
			.removalListener(new RemovalListener<String, Socket>() {
				@Override
				public void onRemoval(RemovalNotification<String, Socket> notification) {
					Socket toBeClose = notification.getValue();
					if (null != toBeClose) {
						try {
							toBeClose.close();
						} catch (IOException e) {
						}
					}
				}
			}).build();

	private ConnectorPool() {
	}

	public Socket getSocket(final String ip) throws ExecutionException {
		if (StringUtils.isEmpty(ip)) {
			throw new IllegalArgumentException();
		}

		return pool.get(ip, new Callable<Socket>() {
			@Override
			public Socket call() throws Exception {
				Socket socket = new Socket(ip, Constants.PROVIDER_PORT);
				socket.setKeepAlive(true);
				socket.setPerformancePreferences(2, 3, 1);
				socket.setTcpNoDelay(true);

				return socket;
			}
		});
	}

	public static void main(String[] args) throws ExecutionException, InterruptedException {
		System.out.println(ConnectorPool.INSTANCE.getSocket("192.168.0.102"));
		System.out.println(ConnectorPool.INSTANCE.getSocket("192.168.0.102"));
		Thread.sleep(10000);
		System.out.println(ConnectorPool.INSTANCE.getSocket("192.168.0.102"));
	}
}
