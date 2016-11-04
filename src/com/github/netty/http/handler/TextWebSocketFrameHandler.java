package com.github.netty.http.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;

/**
 ** 文件名：TextWebSocketFrameHandler.java
 ** 主要作用：TODO
 *@author 囚徒困境
 *创建日期：2014年12月30日
 */
public class TextWebSocketFrameHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
	
	private final ChannelGroup group;
	
	public TextWebSocketFrameHandler(ChannelGroup group) {
		super();
		this.group = group;
	}
	
	/*
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
			throws Exception {
		
		if(evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE){
			ctx.pipeline().remove(HttpRequestHandler.class);
			
			group.writeAndFlush(new TextWebSocketFrame("Client "+ctx.channel()+" joined!"));
			
			group.add(ctx.channel());
		}else{
			super.userEventTriggered(ctx, evt);
		}
		
	}*/

	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			TextWebSocketFrame msg) throws Exception {
		//group.writeAndFlush(msg.retain());
		//XXX: 没有调用 retain
		channelWrite(ctx, msg);
		
	}
	private void channelWrite(ChannelHandlerContext ctx, TextWebSocketFrame msg) {
		Channel incoming = ctx.channel();
        for (Channel channel : group) {
            if (channel != incoming){
                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "]" + msg.text()));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame("[you]" + msg.text() ));
            }
        }
	}
	
	/*
	 * 事件处理方法是当出现 Throwable 对象才会被调用，即当 Netty 由于 IO 错误或者处理器在处理事件时抛出的异常时。
	 * 在大部分情况下，捕获的异常应该被记录下来并且把关联的 channel 给关闭掉。
	 * 然而这个方法的处理方式会在遇到不同异常的情况下有不同的实现，比如你可能想在关闭连接之前发送一个错误码的响应消息。
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		ctx.close();
		cause.printStackTrace();
	}
	
	
	/*
	 * 
	 *  Client:/127.0.0.1:59452join
		Client:/127.0.0.1:59452Active
		Client:/127.0.0.1:59452Inactive
		Client:/127.0.0.1:59452removed
	 * */
	
	 @Override
	    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)加入
	        Channel incoming = ctx.channel();
	        for (Channel channel : group) {
	            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " join"));
	        }
	        group.add(ctx.channel());
	        System.out.println("Client:"+incoming.remoteAddress() +"join");
	    }

	    @Override
	    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)离开
	        Channel incoming = ctx.channel();
	        for (Channel channel : group) {
	            channel.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " removed"));
	        }
	        System.out.println("Client:"+incoming.remoteAddress() +"removed");
	        group.remove(ctx.channel());
	    }

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)在线
	        Channel incoming = ctx.channel();
	        System.out.println("Client:"+incoming.remoteAddress()+"Active");
	    }

	    @Override
	    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)掉线
	        Channel incoming = ctx.channel();
	        System.out.println("Client:"+incoming.remoteAddress()+"Inactive");
	    }
	    
}
