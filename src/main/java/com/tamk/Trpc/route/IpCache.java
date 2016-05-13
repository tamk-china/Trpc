package com.tamk.Trpc.route;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kuanqiang.tkq
 */
public class IpCache {
	public final static IpCache INSTANCE = new IpCache();

	private Map<String, List<String>> ipTables = new ConcurrentHashMap<String, List<String>>();

	private IpCache() {
	}

	void put(final String interfaceName, final List<String> ips) {
		if (StringUtils.isEmpty(interfaceName) || null == ips) {
			throw new IllegalArgumentException();
		}

		ipTables.put(interfaceName, ips);
	}

	public List<String> get(final String interfaceName) {
		if (StringUtils.isEmpty(interfaceName)) {
			throw new IllegalArgumentException();
		}

		return ipTables.get(interfaceName);
	}

}
