package com.tamk.Trpc.ipcache;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import com.tamk.Trpc.exception.TrpcException;

/**
 * @author kuanqiang.tkq
 */
public class IpCache {
	public final static IpCache INSTANCE = new IpCache();

	private static final String ZK_CONNECTS = "192.168.0.102:2181,192.168.0.102:2182,192.168.0.102:2183";
	private static final String NAMESPACE = "Trpc";
	private static final String CONSUMER_PATH = "consumer";
	private static final String PROVIDER_PATH = "provider";

	private CuratorFramework curatorClient;
	private Map<String, List<String>> ipTables = new ConcurrentHashMap<String, List<String>>();
	private Map<String, AtomicInteger> roundRobinPos = new ConcurrentHashMap<String, AtomicInteger>();
	private Map<String, PathChildrenCache> watchers = new ConcurrentHashMap<String, PathChildrenCache>();

	private IpCache() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		curatorClient = CuratorFrameworkFactory.builder().connectString(ZK_CONNECTS).sessionTimeoutMs(5000).retryPolicy(retryPolicy).namespace(NAMESPACE).build();
		curatorClient.start();
	}

	public void register(final String interfaceName) throws TrpcException {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new IllegalArgumentException("interfaceName can not empty");
		}

		try {
			String localAddress = getLocalAddress();
			curatorClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath("/" + CONSUMER_PATH + "/" + interfaceName + "/" + localAddress);
		} catch (UnknownHostException e) {
			throw new TrpcException(String.format("can not get localAddress [interfaceName = %s]", interfaceName));
		} catch (Exception e) {
			throw new TrpcException(e);
		}
	}

	public void loadIpsAndRegisterWatcher(final String interfaceName) throws TrpcException {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new IllegalArgumentException("interfaceName can not empty");
		}

		List<String> ips = ipTables.get(interfaceName);
		if (null == ips || ips.isEmpty()) {
			List<String> providerIps = null;
			String path = "/" + PROVIDER_PATH + "/" + interfaceName;
			try {
				providerIps = curatorClient.getChildren().forPath(path);

				if (null == providerIps) {
					providerIps = new ArrayList<String>();
				}
				ipTables.put(interfaceName, providerIps);

				PathChildrenCache childrenCache = new PathChildrenCache(curatorClient, path, false);
				childrenCache.start(StartMode.POST_INITIALIZED_EVENT);

				childrenCache.getListenable().addListener(new PathChildrenCacheListener() {
					@Override
					public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
						List<String> ips = null;
						switch (event.getType()) {
						case CHILD_ADDED:
							ips = ipTables.get(interfaceName);
							if (null == ips) {
								ips = new ArrayList<String>();
								ipTables.put(interfaceName, ips);
							}
							ips.add(event.getData().getPath());
							break;
						case CHILD_REMOVED:
							ips = ipTables.get(interfaceName);
							if (null == ips) {
								ips = new ArrayList<String>();
								ipTables.put(interfaceName, ips);
							}
							ips.remove(event.getData().getPath());
							break;
						default:
							break;
						}

					}
				});

				watchers.put(interfaceName, childrenCache);
			} catch (Exception e) {
				throw new TrpcException(e);
			}
		}
	}

	public String getProviderIp(final String interfaceName) throws TrpcException {
		List<String> ips = ipTables.get(interfaceName);
		if (null == ips) {
			throw new TrpcException(String.format("none providers [interfaceName = %s]", interfaceName));
		}

		AtomicInteger pos = roundRobinPos.get(interfaceName);
		return ips.get(Math.abs(pos.incrementAndGet()) % ips.size());
	}

	private String getLocalAddress() throws UnknownHostException {
		return InetAddress.getLocalHost().getHostAddress();
	}

	public static void main(String[] args) {
		try {
			IpCache cache = IpCache.INSTANCE;
			Thread.sleep(20000);
			cache.register("test");
			cache.loadIpsAndRegisterWatcher("test");
			System.out.println(cache.getProviderIp("test"));
			System.out.println("**********************************");
		} catch (TrpcException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
