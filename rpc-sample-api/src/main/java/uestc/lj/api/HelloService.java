package uestc.lj.api;

/**
 * @Author:Crazlee
 * @Date:2021/11/23
 */
public interface HelloService {
	/**
	 * 传入String 字符串反馈一个新String字符串
	 *
	 * @param name
	 * @return
	 */
	String hello(String name);

	/**
	 * 传入一个Person对象，反馈一个String字符串
	 *
	 * @param person
	 * @return
	 */
	String hello(Person person);
}
