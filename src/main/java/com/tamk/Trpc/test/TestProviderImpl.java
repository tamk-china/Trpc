package com.tamk.Trpc.test;

import java.util.Random;

import com.tamk.Trpc.exception.TrpcException;
import com.tamk.Trpc.provider.TrpcProvider;

public class TestProviderImpl implements TestProvider {
	@Override
	public User sayHello(int i) {
		User user = new User();
		user.setId(new Random().nextLong());
		user.setName("tamk");
		return user;
	}

	public static void main(String[] args) throws InterruptedException, TrpcException {
		TrpcProvider provider = new TrpcProvider();
		provider.setInterfaceName("com.tamk.Trpc.test.TestProvider");
		provider.setInvoker(new TestProviderImpl());
		provider.init();
		Thread.sleep(Integer.MAX_VALUE);
	}

}
