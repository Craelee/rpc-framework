package uestc.lj.common.utils;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具类，用来进行类的序列化和反序列化的操作
 * 基于protostuff实现
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class SerializationUtil {
	private static Map<Class<?>, Schema<?>> cachedSchema = new ConcurrentHashMap<>();

	private static Objenesis objenesis = new ObjenesisStd(true);

	public SerializationUtil() {
	}

	/**
	 * 进行序列化操作（对象——>字节数组）
	 *
	 * @param obj 序列化的对象
	 * @param <T> 序列化对象的类型
	 * @return 字节数组
	 */
	public static <T> byte[] serialize(T obj) {
		Class<T> cls = (Class<T>) obj.getClass();
		LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
		try {
			Schema<T> schema = getSchema(cls);
			//类转换为字节流
			return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		} finally {
			buffer.clear();
		}
	}

	/**
	 * 反序列化（字节数组——>对象）
	 *
	 * @param data 字节数组
	 * @param cls  对象
	 * @param <T>  对象类型
	 * @return 反序列化的对象
	 */
	public static <T> T deserialize(byte[] data, Class<T> cls) {
		try {
			T message = objenesis.newInstance(cls);
			Schema<T> schema = getSchema(cls);
			//字节流转换为类
			ProtostuffIOUtil.mergeFrom(data, message, schema);
			return message;
		} catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

	/**
	 * 用来获取类的概要信息
	 * @param cls
	 * @param <T>
	 * @return
	 */
	private static <T> Schema<T> getSchema(Class<T> cls) {
		Schema<T> schema = (Schema<T>) cachedSchema.get(cls);
		if (schema == null) {
			schema = RuntimeSchema.createFrom(cls);
			cachedSchema.put(cls, schema);
		}
		return schema;
	}
}
