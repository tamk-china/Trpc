package com.tamk.Trpc.route;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import com.tamk.Trpc.exception.TrpcException;

/**
 * @author kuanqiang.tkq
 */
public class RouteManager {
	public static final RouteManager INSTANCE = new RouteManager();

	private static final String ZK_CONNECTS = "zk.tamk.com:2181,zk.tamk.com:2182,zk.tamk.com:2183";
	private static final String NAMESPACE = "Trpc";
	private static final String CONSUMER_PATH = "consumer";
	private static final String PROVIDER_PATH = "provider";

	private CuratorFramework curatorClient;
	private Map<String, PathChildrenCache> watchers = new ConcurrentHashMap<String, PathChildrenCache>();

	private RouteManager() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		curatorClient = CuratorFrameworkFactory.builder().connectString(ZK_CONNECTS).sessionTimeoutMs(5000).retryPolicy(retryPolicy).namespace(NAMESPACE).build();
		curatorClient.start();
	}

	public void registerProvider(final String interfaceName) throws TrpcException {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new IllegalArgumentException();
		}

		try {
			String localAddress = getLocalAddress();
			curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + PROVIDER_PATH + "/" + interfaceName + "/" + localAddress, localAddress.getBytes());
		} catch (UnknownHostException e) {
			throw new TrpcException(String.format("can not get localAddress [interfaceName = %s]", interfaceName));
		} catch (Exception e) {
			throw new TrpcException(e);
		}
	}

	public void registerConsumer(final String interfaceName) throws TrpcException {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new IllegalArgumentException();
		}

		try {
			PathChildrenCache childrenCache = new PathChildrenCache(curatorClient, "/" + PROVIDER_PATH + "/" + interfaceName, true);
			childrenCache.start(StartMode.POST_INITIALIZED_EVENT);
			childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch (event.getType()) {
					case CHILD_ADDED:
						updateIpCache(interfaceName, new String(event.getData().getData()), true);
						break;
					case CHILD_REMOVED:
						updateIpCache(interfaceName, new String(event.getData().getData()), false);
						break;
					default:
						break;
					}
				}
			});
			watchers.put(interfaceName, childrenCache);

			IpCache.INSTANCE.put(interfaceName, curatorClient.getChildren().forPath("/" + PROVIDER_PATH + "/" + interfaceName));

			String localAddress = getLocalAddress();
			curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + CONSUMER_PATH + "/" + interfaceName + "/" + localAddress, localAddress.getBytes());
		} catch (UnknownHostException e) {
			throw new TrpcException(String.format("can not get localAddress [interfaceName = %s]", interfaceName));
		} catch (Exception e) {
			throw new TrpcException(e);
		}

	}

	private synchronized void updateIpCache(final String interfaceName, final String data, final boolean isAdd) {
		if (StringUtils.isEmpty(interfaceName) || StringUtils.isEmpty(data)) {
			throw new IllegalArgumentException();
		}

		List<String> ips = IpCache.INSTANCE.get(interfaceName);
		if (null == ips) {
			ips = new ArrayList<String>();
		}

		if (isAdd) {
			if (!ips.contains(data)) {
				ips.add(data);
			}
		} else {
			ips.remove(data);
		}
		IpCache.INSTANCE.put(interfaceName, ips);
	}

	private String getLocalAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}
	
	public static void main(String[] args){
		try {
			final String interfaceName = "testProvider";
			RouteManager.INSTANCE.registerProvider(interfaceName);
			RouteManager.INSTANCE.registerConsumer(interfaceName);
			System.out.println(IpCache.INSTANCE.get(interfaceName));
		} catch (TrpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
