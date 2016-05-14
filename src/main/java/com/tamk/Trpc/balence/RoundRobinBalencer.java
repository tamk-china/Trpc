package com.tamk.Trpc.balence;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;

/**
 * @author kuanqiang.tkq
 */
public class RoundRobinBalencer {
	public static final RoundRobinBalencer INSTANCE = new RoundRobinBalencer();

	private RoundRobinBalencer() {
	}

	private Map<String, AtomicLong> rrPos = new ConcurrentHashMap<String, AtomicLong>();

	public String getNextIp(final String interfaceName, final List<String> ips) {
		if (StringUtils.isEmpty(interfaceName) || null == ips) {
			throw new IllegalArgumentException();
		}

		return ips.get((int) ((rrPos.putIfAbsent(interfaceName, new AtomicLong(0)).incrementAndGet()) % ips.size()));
	}
}
