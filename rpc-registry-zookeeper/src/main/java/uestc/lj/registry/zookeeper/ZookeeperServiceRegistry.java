package uestc.lj.registry.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uestc.lj.registry.ServiceRegistry;

/**
 * 用来实现RPC服务的注册
 * 基于Zookeeper的服务注册接口实现
 *
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public class ZookeeperServiceRegistry implements ServiceRegistry {
	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

	/**
	 * zookeeper连接客户端
	 */
	private final ZkClient zkClient;

	/**
	 * 构造方法，通过传入的zookeeper服务地址，创建zookeeper客户端
	 *
	 * @param zkAddress 服务地址
	 */
	public ZookeeperServiceRegistry(String zkAddress) {
		zkClient = new ZkClient(zkAddress, Constant.ZO_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
		LOGGER.debug("connect zookeeper");
	}


	/**
	 * 执行服务注册的功能
	 * 将传进来的服务名称serviceName以及服务地址serviceAddress进行处理，
	 * 首先是创建一个永久的registry节点，以后所有注册进来的服务地址，都位于该节点下；
	 * 然后创建service节点，所有有关该service的服务信息都位于该节点下；
	 * 最后是服务的地址节点，是一个临时节点，该服务的具体调用地址，位于该节点下
	 *
	 * @param serviceName    服务名称
	 * @param serviceAddress 服务地址
	 */
	@Override
	public void register(String serviceName, String serviceAddress) {
		//创建registry节点（持久）
		String registryPath = Constant.ZK_REGISTRY_PATH;

		//判断节点是否存在，节点不存在时，创建相关节点
		if (!zkClient.exists(registryPath)) {
			//创建持久化节点
			zkClient.createPersistent(registryPath);
			LOGGER.debug("create registry node: {}", registryPath);
		}
		//创建service节点（持久）
		String servicePath = registryPath + "/" + serviceName;
		if (!zkClient.exists(servicePath)) {
			//创建持久化节点
			zkClient.createPersistent(servicePath);
			LOGGER.debug("create service node: {}", servicePath);
		}
		//创建address节点（临时有序）
		String addressPath = servicePath + "/adress-";
		String adressNode = zkClient.createEphemeralSequential(addressPath, serviceAddress);
		LOGGER.debug("create address node: {} ", adressNode);
	}
}
