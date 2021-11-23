package uestc.lj.common.bean;

/**
 * 封装在RPC的情况下，Netty需要传输和接收的数据集对象
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcRequest {
	/**
	 * 请求ID
	 * 因为要区分每一次的调用，所以每一次远程调用有一个独一无二的requestId
	 */
	private String requestId;
	/**
	 * 接口名称
	 * 因为我们需要实现调用远程方法，所以这里要指定需要调用的接口名称interfaceName以及版本serviceVersion
	 */
	private String interfaceName;
	/**
	 * 服务版本
	 */
	private String serviceVersion;
	/**
	 * 需要调用的方法名
	 * 调用的是某个接口的具体方法，所以要制定调用的方法名methodName
	 */
	private String methodName;
	/**
	 * 方法参数类型
	 */
	private Class<?>[] parameterTypes;
	/**
	 * 方法参数
	 */
	private Object[] parameters;

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public String getInterfaceName() {
		return interfaceName;
	}

	public void setInterfaceName(String interfaceName) {
		this.interfaceName = interfaceName;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public Class<?>[] getParameterTypes() {
		return parameterTypes;
	}

	public void setParameterTypes(Class<?>[] parameterTypes) {
		this.parameterTypes = parameterTypes;
	}

	public Object[] getParameters() {
		return parameters;
	}

	public void setParameters(Object[] parameters) {
		this.parameters = parameters;
	}
}
