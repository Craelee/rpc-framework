package uestc.lj.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import uestc.lj.common.utils.SerializationUtil;

/**
 * 解码器RpcEncoder，用来将需要传输的信息进行编码、序列化操作
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcEncoder extends MessageToByteEncoder {
	private Class<?> genericClass;

	public RpcEncoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
		//检测这个object这个对象能不能被转换成为这个类
		if (genericClass.isInstance(msg)) {
			//将object对象序列化为byte
			byte[] data = SerializationUtil.serialize(msg);
			//标记写入信息的长度
			out.writeInt(data.length);
			//写入信息
			out.writeBytes(data);
		}
	}
}
