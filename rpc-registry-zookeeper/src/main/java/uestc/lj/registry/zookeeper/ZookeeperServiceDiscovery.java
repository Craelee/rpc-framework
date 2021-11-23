package uestc.lj.registry.zookeeper;

import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uestc.lj.common.utils.CollectionUtil;
import uestc.lj.registry.ServiceDiscovery;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 实现RPC服务调用地址的发现
 * 基于zookeeper的服务发现接口实现
 *
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public class ZookeeperServiceDiscovery implements ServiceDiscovery {
	/**
	 * 日志对象
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

	/**
	 * zookeeper的地址
	 */
	private String zkAddress;

	public ZookeeperServiceDiscovery(String zkAddress) {
		this.zkAddress = zkAddress;
	}

	/**
	 * 获取传进来的服务名称serviceName，然后创建zkClient对象，进行服务发现操作。
	 * 首先获取service节点，然后获取相关service节点下的所有子节点，这些子节点保存了各个服务端(单体或集群)暴露出来的服务地址，
	 * 如果子节点只有一个，证明服务端是单体服务，只需返回这一个节点即可；
	 * 如果子节点有很多，则证明服务端是集群服务，此时的选择策略是随机选择一个节点，
	 * 然后使用客户端对象，将该节点的服务地址信息(serviceAddress)读取出来即可
	 *
	 * @param serviceName 服务名称
	 * @return
	 */
	@Override
	public String discover(String serviceName) {
		//创建zookeeper客户端
		ZkClient zkClient = new ZkClient(zkAddress, Constant.ZO_SESSION_TIMEOUT, Constant.ZK_CONNECTION_TIMEOUT);
		LOGGER.debug("connect zookeeper");

		try {
			//获取service节点
			String servicePath = Constant.ZK_REGISTRY_PATH + "/" + serviceName;
			if (!zkClient.exists(servicePath)) {
				//如果service不存在则报错
				throw new RuntimeException(String.format("can not find any service node on path: %s", servicePath));
			}
			//获取service节点下的所有子节点
			List<String> addressList = zkClient.getChildren(servicePath);

			if (CollectionUtil.isEmpty(addressList)) {
				//如果service节点下的所有子节点为空，则报错
				throw new RuntimeException(String.format("can not find any address node on path : %s", servicePath));
			}
			//获取address节点
			String address;
			int size = addressList.size();
			if (size == 1) {
				//只有一个地址，则直接获取地址
				address = addressList.get(0);
				LOGGER.debug("get only address node:{}", address);
			} else {
				//如果存在多个地址，则随机获取地址
				address = addressList.get(ThreadLocalRandom.current().nextInt(size));
				LOGGER.debug("get random address node: {}", address);
			}
			//获取address节点的值
			String addressPath = servicePath + "/" + address;
			return zkClient.readData(addressPath);
		} finally {
			//关闭zookeeper连接
			zkClient.close();
		}
	}
}
