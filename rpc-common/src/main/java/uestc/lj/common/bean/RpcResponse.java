package uestc.lj.common.bean;

/**
 * 封装在RPC的情况下，Netty需要传输和接收的数据集对象
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcResponse {
	/**
	 * 远程调用独一无二的requestId
	 */
	private String requestId;
	/**
	 * 反馈的异常对象
	 */
	private Exception exception;
	/**
	 * 调用远程方法返回的具体对象
	 */
	private Object result;

	public boolean hasException() {
		return exception != null;
	}

	public String getRequestId() {
		return requestId;
	}

	public void setRequestId(String requestId) {
		this.requestId = requestId;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}

	public Object getResult() {
		return result;
	}

	public void setResult(Object result) {
		this.result = result;
	}
}
