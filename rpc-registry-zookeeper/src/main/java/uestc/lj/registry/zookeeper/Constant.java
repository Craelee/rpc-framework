package uestc.lj.registry.zookeeper;

/**
 * 用来定义Zookeeper相关的常量
 *
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public interface Constant {
	/**
	 * 会话超时时间
	 */
	int ZO_SESSION_TIMEOUT = 5000;
	/**
	 * 连接超时时间
	 */
	int ZK_CONNECTION_TIMEOUT = 1000;
	/**
	 * 注册地址——zookeeper的注册目录地址
	 */
	String ZK_REGISTRY_PATH = "/registry";
}
