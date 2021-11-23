package uestc.lj.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import uestc.lj.common.utils.SerializationUtil;

import java.util.List;

/**
 * RpcDecoder解码器，用来将传输回来的信息进行解码、反序列化操作
 * 继承netty的解码器类
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class RpcDecoder extends ByteToMessageDecoder {
	/**
	 * 需要进行解码的类
	 */
	private Class<?> genericClass;

	public RpcDecoder(Class<?> genericClass) {
		this.genericClass = genericClass;
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		if (in.readableBytes() < 4) {
			/**
			 * ByteBuf分为两部分，一部分为消息头head，一部分为消息体body。
			 * head中保存的是可读信息的长度(一个int类型的值)
			 * 当可读信息小于4 byte时，认为消息头中没有可读信息的长度。即消息体body长度不存在或者ByteBuf结构异常，放弃解码。
			 */
			return;
		}
		//标记一下当前的读索引的位置
		in.markReaderIndex();
		//获取信息的总长度，需要注意的是ByteBuf的readInt()方法会让它的readIndex加4
		int dataLength = in.readInt();
		if (in.readableBytes() < dataLength) {
			/**
			 * 由于ByteBuf的readInt()方法会让他的readIndex增加4，此时可读区域就会比原来小4，这个时候就需要将可读索引恢复到4之前的位置
			 * 如果可读信息的长度小于信息的总长度，还原读索引的位置(resetReaderIndex这个方法是配合markReaderIndex使用的。把readIndex重置到mark的地方)
			 */
			in.resetReaderIndex();
			return;
		}
		byte[] data = new byte[dataLength];
		in.readBytes(data);
		//执行反序列化操作
		out.add(SerializationUtil.deserialize(data, genericClass));
	}
}
