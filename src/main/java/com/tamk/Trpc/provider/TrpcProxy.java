package com.tamk.Trpc.provider;

import com.tamk.Trpc.constants.Constants;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class TrpcProxy {
	public static final TrpcProxy INSTANCE = new TrpcProxy();

	private TrpcProxy() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
		EventLoopGroup workerGroup = new NioEventLoopGroup(20);
		try {
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new InvokerChildrenHandler());
			ChannelFuture f = bootstrap.bind(Constants.PROVIDER_PORT).sync();
			f.channel().close().sync();
		} catch (InterruptedException e) {
			
		} finally {
			bossGroup.shutdownGracefully();
			workerGroup.shutdownGracefully();
		}
	}
	
	public void init(){}

	private static class InvokerChildrenHandler extends ChannelInitializer<SocketChannel> {
		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			ch.pipeline().addLast(new TrpcDecoder());
			ch.pipeline().addLast(new TrpcProvider());
			ch.pipeline().addLast(new TrpcEncoder());
		}
	}
}
