package com.tamk.test;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

/**
 * God Bless You! Author: Fangniude Date: 2013-07-15
 */
public class NettyServer {
	public void bind(int port) throws InterruptedException {
		EventLoopGroup boosGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(20);

		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(boosGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChildChannelHandler());
			ChannelFuture f = bootstrap.bind(port).sync();
			f.channel().closeFuture().sync();
		} finally {
			boosGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}

	private class ChildChannelHandler extends ChannelInitializer<SocketChannel> {

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new TimeServerHandler());
		}

	}

	public static void main(String[] args) throws InterruptedException {
		new NettyServer().bind(8080);
		Thread.sleep(Integer.MAX_VALUE);
	}
}
