package uestc.lj.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import uestc.lj.common.bean.RpcRequest;
import uestc.lj.common.bean.RpcResponse;
import uestc.lj.common.codec.RpcDecoder;
import uestc.lj.common.codec.RpcEncoder;
import uestc.lj.common.utils.StringUtil;
import uestc.lj.registry.ServiceRegistry;

import java.util.HashMap;
import java.util.Map;

/**
 * 用来发送处理好的Rpc请求
 * RPC服务器，用于发布发布RPC服务
 * 实现ApplicationContextAware是为了获取该类所在的Spring容器，以操作spring容器及其中的Bean实例。
 * 实现InitializingBean(提供了初始化方法的方式)是为了在初始化Bean之前加一些逻辑(afterPropertiesSet方法中)
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcServer implements ApplicationContextAware, InitializingBean {
	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

	/**
	 * 服务地址
	 */
	private String serviceAddress;

	/**
	 * 服务地址注册类，该类由注册中心实现
	 */
	private ServiceRegistry serviceRegistry;

	/**
	 * 存放服务名与服务对象之间的映射关系
	 */
	private Map<String, Object> handlerMap = new HashMap<>();

	public RpcServer(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}

	public RpcServer(String serviceAddress, ServiceRegistry serviceRegistry) {
		this.serviceAddress = serviceAddress;
		this.serviceRegistry = serviceRegistry;
	}

	/**
	 * 用来操作Spring容器中的类，这里会扫描带有RpcService注解的类
	 * 然后将获取的这些类的信息（服务名，服务对象本身）保存进handlerMap，用于后面获取服务直接使用
	 *
	 * @param applicationContext
	 * @throws BeansException
	 */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		//扫描带有RpcService注解的类，并初始化HandlerMap对象
		Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
		if (MapUtils.isNotEmpty(serviceBeanMap)) {
			//遍历带有RpcService注解的类
			for (Object serviceBean : serviceBeanMap.values()) {
				//获取注解对象
				RpcService rpcService = serviceBean.getClass().getAnnotation(RpcService.class);
				//通过注解对象，获取其修饰参数（serviceName服务名称，serviceVersion服务版本）
				String serviceName = rpcService.value().getName();
				String serviceVersion = rpcService.version();

				if (StringUtil.isNotEmpty(serviceVersion)) {
					//服务名与版本号拼接
					serviceName += "-" + serviceVersion;
				}

				//存放进映射map，供handler处理类操作
				handlerMap.put(serviceName, serviceBean);
			}
		}
	}


	/**
	 * 为了在初始化Bean之前加一些逻辑。
	 * afterPropertiesSet()在初始化所有Bean之前调用的方法。
	 * 因为我们要发布RPC服务，即将所有需要暴露给外部服务的服务实现类通过Netty发布出去，所以在所有Bean初始化之前，
	 * 将组装好的handlerMap中的服务发布出去，这里是注册到注册中心中。
	 * @throws Exception
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		NioEventLoopGroup bossGroup = new NioEventLoopGroup();
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			//创建并初始化Netty服务端Bootstrap对象
			ServerBootstrap bootstrap = new ServerBootstrap();
			bootstrap.group(bossGroup, workerGroup);
			bootstrap.channel(NioServerSocketChannel.class);
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					ChannelPipeline pipeline = ch.pipeline();
					//解码RPC请求
					pipeline.addLast(new RpcDecoder(RpcRequest.class));
					//编码RPC响应
					pipeline.addLast(new RpcEncoder(RpcResponse.class));
					//处理RPC请求
					pipeline.addLast(new RpcServerHandler(handlerMap));
				}
			});
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			//获取RPC服务器的IP地址和端口号
			String[] addressArray = StringUtil.split(serviceAddress, ":");
			String ip = addressArray[0];
			int port = Integer.parseInt(addressArray[1]);
			//启动RPC服务器
			ChannelFuture future = bootstrap.bind(ip, port).sync();
			//注册RPC服务地址
			if (serviceRegistry != null) {
				for (String interfaceName : handlerMap.keySet()) {
					serviceRegistry.register(interfaceName, serviceAddress);
					LOGGER.debug("register service：{}=>{}", interfaceName, serviceAddress);
				}
			}
			LOGGER.debug("server started on port {}", port);
			//关闭RPC服务器
			future.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}



}
