package uestc.lj.common.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 处理String字符串的工具类
 *
 * @Author:Crazlee
 * @Date:2021/11/22
 */
public class StringUtil {
	/**
	 * 判断字符串是否为空
	 *
	 * @param str 字符串
	 * @return 是否为空，是返回true
	 */
	public static boolean isEmpty(String str) {
		if (str != null) {
			str = str.trim();
		}
		return StringUtils.isEmpty(str);
	}

	/**
	 * 判断字符串是否非空
	 *
	 * @param str 字符串
	 * @return 非空返回true
	 */
	public static boolean isNotEmpty(String str) {
		return !isEmpty(str);
	}

	/**
	 * 分割固定格式的字符串
	 *
	 * @param str       字符串
	 * @param separator 分隔符
	 * @return 分割后的字符串
	 */
	public static String[] split(String str, String separator) {
		return StringUtils.splitByWholeSeparator(str, separator);
	}
}
