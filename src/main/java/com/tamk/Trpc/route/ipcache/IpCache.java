package com.tamk.Trpc.route.ipcache;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import com.tamk.Trpc.exception.TrpcException;

/**
 * @author kuanqiang.tkq
 */
public class IpCache {
	public final static IpCache INSTANCE = new IpCache();

	private static final String ZK_CONNECTS = "192.168.0.102:2181,192.168.0.102:2182,192.168.0.102:2183";
	private static final String NAMESPACE = "Trpc";

	private CuratorFramework client;
	private Map<String, List<String>> ipTables = new ConcurrentHashMap<String, List<String>>();
	private Map<String, AtomicInteger> roundRobinPos = new ConcurrentHashMap<String, AtomicInteger>();
	private Set<String> interfaceNames = new HashSet<String>();

	private IpCache() {
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
		client = CuratorFrameworkFactory.builder().connectString(ZK_CONNECTS).sessionTimeoutMs(5000).retryPolicy(retryPolicy).namespace(NAMESPACE).build();
		client.start();
	}
	
	public void register(String interfaceName){
		
	}

	public String getProviderIp(String interfaceName) throws TrpcException {
		List<String> ips = ipTables.get(interfaceName);
		if (null == ips) {
			throw new TrpcException(String.format("none providers [interfaceName = %s]", interfaceName));
		}

		AtomicInteger pos = roundRobinPos.get(interfaceName);
		return ips.get(Math.abs(pos.incrementAndGet()) % ips.size());
	}

}
