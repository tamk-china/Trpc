package com.tamk.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NettyClient {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Socket socket = new Socket("127.0.0.1", 8080);

		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try {
			bis = new BufferedInputStream(socket.getInputStream());
			bos = new BufferedOutputStream(socket.getOutputStream());

			bos.write("hello netty".getBytes());
			bos.flush();

			byte[] result = new byte[1024];
			bis.read(result);
			System.out.print(new String(result, "UTF-8"));
			System.out.println("1-" + socket.isClosed());
		} finally {
			System.out.println("2-" + socket.isClosed());
			if (null != bis) {
				bis.close();
			}
			System.out.println("3-" + socket.isClosed());
			if (null != bos) {
				bos.close();
			}

			System.out.println("4-" + socket.isClosed());
		}
	}
}
