package uestc.lj.server;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RPC服务注解（标注在服务实现类上）
 * 用来标注在相关暴露给其他服务调用的RPC类上。
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
//@Target用于定义在注解的上边，表明该注解可以使用的范围（这里为Type，即可用于接口、类、枚举、注解）
@Target({ElementType.TYPE})
//@Retention可以用来修饰注解，其中RententionPolicy指定注解的生命周期
//这里RUNTIME指注解不仅被保存到class文件中，jvm加载class文件之后，仍然存在
@Retention(RetentionPolicy.RUNTIME)
//添加该注解是可以被Spring扫描到
@Component
public @interface RpcService {
	/**
	 * 服务接口类
	 * 使用该注解类的实现类的接口的具体类型
	 * @return
	 */
	Class<?> value();

	/**
	 * 服务版本号
	 *
	 * @return
	 */
	String version() default "";
}
