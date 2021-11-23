package uestc.lj.sample.client;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import uestc.lj.api.HelloService;
import uestc.lj.client.RpcProxy;

/**
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public class HelloClient {
	public static void main(String[] args) {
		ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("spring.xml");
		RpcProxy rpcProxy = context.getBean(RpcProxy.class);

		HelloService helloService = rpcProxy.create(HelloService.class);
		String result = helloService.hello("world");
		System.out.println(result);

		HelloService helloService2 = rpcProxy.create(HelloService.class, "sample.hello2");
		String result2 = helloService2.hello("世界");
		System.out.println(result2);

		System.exit(0);
	}
}
