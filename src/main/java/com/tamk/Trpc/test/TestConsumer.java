package com.tamk.Trpc.test;

import java.lang.reflect.Proxy;

import com.tamk.Trpc.consumer.TrpcConsumer;
import com.tamk.Trpc.exception.TrpcException;

/**
 * @author kuanqiang.tkq
 */
public class TestConsumer {
	public static void main(String[] args) throws TrpcException {
		TrpcConsumer consumer= new TrpcConsumer();
		consumer.setInterfaceName("com.tamk.Trpc.test.TestProvider");
		consumer.init();
		TestProvider provider = (TestProvider) Proxy.newProxyInstance(TestProvider.class.getClassLoader(), new Class[]{TestProvider.class}, consumer);
		System.out.println(provider.sayHello(100));
	}
}
