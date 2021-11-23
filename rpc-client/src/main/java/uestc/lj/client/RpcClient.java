package uestc.lj.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uestc.lj.common.bean.RpcRequest;
import uestc.lj.common.bean.RpcResponse;
import uestc.lj.common.codec.RpcDecoder;
import uestc.lj.common.codec.RpcEncoder;

/**
 * RpcClient 利用Netty进行tcp请求发送的类
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcResponse> {

	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	/**
	 * 传输信息的服务端的ip和端口
	 */
	private final String host;
	private final int port;

	/**
	 * 服务端反馈的response信息对象
	 */
	private RpcResponse response;

	/**
	 * 构造方法用于传输服务端的信息
	 */
	public RpcClient(String host, int port) {
		this.host = host;
		this.port = port;
	}


	/**
	 * 处理管道读取反馈的response对象
	 * 这里只需获取response对象即可
	 *
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
		this.response = msg;
	}

	/**
	 * 异常处理
	 *
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//日志记录异常原因
		LOGGER.error("api caught exception: ", cause);
		//关闭上下文对象
		ctx.close();
	}

	/**
	 * 使用Netty发送rpc请求
	 *
	 * @param request
	 * @return
	 */
	public RpcResponse send(RpcRequest request) throws InterruptedException {
		NioEventLoopGroup group = new NioEventLoopGroup();
		try {
			//创建并初始化Netty客户端Bootstrap对象
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(group);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					//注册编码器
					pipeline.addLast(new RpcEncoder(RpcRequest.class));
					//注册解码器
					pipeline.addLast(new RpcDecoder(RpcResponse.class));
					//注册客户端处理对象
					pipeline.addLast(RpcClient.this);
				}
			});
			//设置无延迟操作
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			//连接RPC服务器
			ChannelFuture future = bootstrap.connect(host, port).sync();
			//写入RPC请求数据并关闭连接
			Channel channel = future.channel();
			channel.writeAndFlush(request).sync();
			channel.closeFuture().sync();
			return response;
		} finally {
			group.shutdownGracefully();
		}
	}
}
