package com.tamk.Trpc.test;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Test {

	public static void main(String[] args) throws UnknownHostException, IOException {
		new Socket("127.0.0.1",8090);
	}

}
