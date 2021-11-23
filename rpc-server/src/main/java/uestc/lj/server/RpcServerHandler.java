package uestc.lj.server;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uestc.lj.common.bean.RpcRequest;
import uestc.lj.common.bean.RpcResponse;
import uestc.lj.common.utils.StringUtil;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * RPC服务端处理器，用于处理RPC请求
 *
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);
	/**
	 * 服务名以及相关服务类的Map对象
	 */
	private final Map<String, Object> handlerMap;

	/**
	 * 接收前面RpcServer传来的已经获取好的暴露给外部的服务实现类Map
	 * @param handlerMap
	 */
	public RpcServerHandler(Map<String, Object> handlerMap) {
		this.handlerMap = handlerMap;
	}

	/**
	 * 处理RPC客户端发来的服务调用响应
	 *
	 * @param ctx
	 * @param msg
	 * @throws Exception
	 */
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
		//创建并初始化RPC响应对象
		RpcResponse response = new RpcResponse();
		//反馈发来的唯一的请求ID
		response.setRequestId(msg.getRequestId());
		try {
			Object result = handle(msg);
			response.setResult(result);
		} catch (Exception e) {
			//如果报错，首先记录日志，然后在返回对象中将报错信息放入
			LOGGER.error("handle result failure", e);
			response.setException(e);
		}
		//写入RPC响应对象并自动关闭连接
		ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
	}

	/**
	 * 拿到RPC客户端请求的服务信息，将相关服务（服务名、版本）加载出来，反馈给客户端
	 *
	 * @param msg
	 * @return
	 */
	private Object handle(RpcRequest msg) throws InvocationTargetException {

		//获取服务对象
		// 1. 获取请求的服务接口名
		String serviceName = msg.getInterfaceName();
		// 2.获取请求的服务版本
		String serviceVersion = msg.getServiceVersion();
		//如果版本不为空，则拼接
		if (StringUtil.isNotEmpty(serviceVersion)) {
			serviceName += "-" + serviceVersion;
		}
		//从加载出来的所有服务类中找到客户端请求的serviceName对应的具体Bean
		Object serviceBean = handlerMap.get(serviceName);
		//如果未找到请求的相关类，则直接报错
		if (serviceBean == null) {
			throw new RuntimeException(String.format("can not find service bean by key : %s", serviceName));
		}
		//如果请求的相关类存在，则通过反射获取调用该类相关请求方法所需的参数
		// 1.获取目标类的类型
		Class<?> serviceClass = serviceBean.getClass();
		// 2.获取请求调用的方法名
		String methodName = msg.getMethodName();
		// 3.获取请求调用的参数类型
		Class<?>[] parameterTypes = msg.getParameterTypes();
		// 4.获取请求调用的具体参数类
		Object[] parameters = msg.getParameters();
		//使用CGLib执行反射调用
		FastClass serviceFastClass = FastClass.create(serviceClass);
		FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
		//返回调用具体方法的结果
		return serviceFastMethod.invoke(serviceBean, parameters);
	}

	/**
	 * 读取响应时发生异常的处理方法
	 *
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		LOGGER.error("server caught exception", cause);
		ctx.close();
	}
}
