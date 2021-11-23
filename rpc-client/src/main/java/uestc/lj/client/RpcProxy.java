package uestc.lj.client;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uestc.lj.common.bean.RpcRequest;
import uestc.lj.common.bean.RpcResponse;
import uestc.lj.common.utils.StringUtil;
import uestc.lj.registry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RpcProxy是封装rpc信息，并调用RpcClient客户端对象，将rpc请求信息发送至服务地址对应的服务器中。
 * Rpc代理，用于创建RPC服务代理
 * 使用Netty客户端发送rpc请求，并获取反馈信息，拿到相关的服务调调用类的相关方法调用结果
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcProxy {
	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcProxy.class);
	/**
	 * 服务地址
	 */
	private String serviceAddress;
	/**
	 * 服务地址发现类，该类由注册中心实现
	 */
	private ServiceDiscovery serviceDiscovery;

	/**
	 * 当不需要注册中心时，传入注册中心的服务地址发现类对象
	 */
	public RpcProxy(ServiceDiscovery serviceDiscovery) {
		this.serviceDiscovery = serviceDiscovery;
	}

	/**
	 * 创建类方法
	 *
	 * @param interfaceClass
	 * @param <T>
	 * @return
	 */
	public <T> T create(final Class<?> interfaceClass) {
		return create(interfaceClass, "");
	}

	/**
	 * 创建类方法，带有服务版本参数
	 *
	 * @param interfaceClass
	 * @param serviceVersion
	 * @param <T>
	 * @return
	 */
	public <T> T create(final Class<?> interfaceClass, final String serviceVersion) {
		//创建动态代理对象
		return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),//类加载器
				new Class<?>[]{interfaceClass},//代理类的类型
				new InvocationHandler() {//代理的处理类
					@Override
					public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
						//创建RPC请求对象并设置请求属性
						RpcRequest request = new RpcRequest();
						//唯一的请求ID
						request.setRequestId(UUID.randomUUID().toString());
						//要调用的类名
						request.setInterfaceName(method.getDeclaringClass().getName());
						//服务版本
						request.setServiceVersion(serviceVersion);
						//要调用的方法名称
						request.setMethodName(method.getName());
						//设置调用方法的参数类型
						request.setParameterTypes(method.getParameterTypes());
						//设置调用方法的参数
						request.setParameters(args);
						//获取RPC服务地址
						if (serviceDiscovery != null) {
							//当serviceDiscovery对象不为空时，说明需要从注册中心获取服务地址
							String serviceName = interfaceClass.getName();
							if (StringUtil.isNotEmpty(serviceName)) {
								//服务名称+版本号
								serviceName += "-" + serviceVersion;
							}
							//远程获取服务地址
							serviceAddress = serviceDiscovery.discover(serviceName);
							LOGGER.debug("discover service:{}=>{}", serviceName, serviceAddress);
						}
						//如果服务地址为空，则报错
						if (StringUtil.isEmpty(serviceAddress)) {
							throw new RuntimeException("server address is empty!");
						}
						//从RPC服务地址中解析主机名与端口号
						String[] array = StringUtils.split(serviceAddress, ":");
						String host = array[0];
						int port = Integer.parseInt(array[1]);
						//创建RPC客户端对象并发送RPC请求
						RpcClient client = new RpcClient(host, port);
						long time = System.currentTimeMillis();
						//获得RPC请求的反馈对象
						RpcResponse response = client.send(request);
						LOGGER.debug("time: {}ms", System.currentTimeMillis() - time);
						//如果反馈对象为空则报错
						if (response == null) {
							throw new RuntimeException("response is null");
						}
						//返回RPC响应结果
						if (response.hasException()) {
							//反馈的异常对象
							throw response.getException();
						} else {
							//调用远程方法返回的具体对象
							return response.getResult();
						}
					}
				});
	}


}
