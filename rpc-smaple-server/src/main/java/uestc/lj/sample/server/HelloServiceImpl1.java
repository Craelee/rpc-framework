package uestc.lj.sample.server;

import uestc.lj.api.HelloService;
import uestc.lj.api.Person;
import uestc.lj.server.RpcService;

/**
 * @Author:Crazlee
 * @Date:2021/11/23
 */
//RpcService用于暴露服务至注册中心
@RpcService(HelloService.class)
public class HelloServiceImpl1 implements HelloService {
	@Override
	public String hello(String name) {
		return "Hello！" + name;
	}

	@Override
	public String hello(Person person) {
		return "Hello!" + person.getFirstName() + " " + person.getLastName();
	}
}
