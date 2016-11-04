package com.github.netty.http.bootstrap;

import java.net.InetSocketAddress;

import com.github.netty.http.handler.ChatServerInitializer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.ImmediateEventExecutor;

/**
 ** 文件名：ChatServer.java
 ** 主要作用：TODO
 *@author 囚徒困境
 *创建日期：2014年12月30日
 */
public class ChatServer {
	private final ChannelGroup group = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);
	//NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
	//第一个经常被叫做‘boss’，用来接收进来的连接。
	private final EventLoopGroup bossGroup = new NioEventLoopGroup();
	//第二个经常被叫做‘worker’，用来处理已经被接收的连接，一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上。
	private final EventLoopGroup workerGroup = new NioEventLoopGroup();
	
	private Channel channel;
	
	public ChannelFuture start(InetSocketAddress address){
		ServerBootstrap boot = new ServerBootstrap();
		boot.group(bossGroup, workerGroup)
		.channel(NioServerSocketChannel.class)
		.childHandler(createInitializer(group));
		
		ChannelFuture f = boot.bind(address).syncUninterruptibly();
		channel = f.channel();
		return f;
	}

	protected ChannelHandler createInitializer(ChannelGroup group) {
		return new ChatServerInitializer(group);
	}
	
	public void destroy(){
		if(channel != null)
			channel.close();
		group.close();
		workerGroup.shutdownGracefully();
	}
	
	public static void main(String[] args) {
		final ChatServer server = new ChatServer();
		//http://localhost:8888/
		ChannelFuture f = server.start(new InetSocketAddress(8888));
		System.out.println("server start................");
		Runtime.getRuntime().addShutdownHook(new Thread(){
			@Override
			public void run() {
				server.destroy();
			}
		});
		f.channel().closeFuture().syncUninterruptibly();
	}
	
}
