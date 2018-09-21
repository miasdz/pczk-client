package itwatertop.core.beans.factory.config;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * zk{name}:name解析器
 * name配置支持String, JSON对象, JSON数组
 * */
public class PlaceholderMsg{
	private static final Pattern OBJECT_PATTERN = Pattern.compile("\\{(.+)\\}\\.(.+)");
	private static final Pattern ARRAY_PATTERN = Pattern.compile("(.+)\\[(\\d+)\\]");
	
	public static final int TYPE_OBJECT = 0;
	public static final int TYPE_ARRAY = 1;
	public static final int TYPE_STRING = 2;

	protected int type;
	protected String key;
	protected String subkey;
	
	protected String updateSpel;
	
	public static PlaceholderMsg getPlaceholderMsg(String placeholder) {
		PlaceholderMsg msg = new PlaceholderMsg();
		Matcher matcher = null;
		if((matcher = OBJECT_PATTERN.matcher(placeholder)).matches()) {
			msg.type = PlaceholderMsg.TYPE_OBJECT;
			msg.key = matcher.group(1);
			msg.subkey = matcher.group(2);
		}else if((matcher = ARRAY_PATTERN.matcher(placeholder)).matches()) {
			msg.type = PlaceholderMsg.TYPE_ARRAY;
			msg.key = matcher.group(1);
			msg.subkey = matcher.group(2);
		}else {
			msg.type = PlaceholderMsg.TYPE_STRING;
			msg.key = placeholder;
		}
		return msg;
	}

	@Override
	public String toString() {
		return "PlaceholderMsg [type=" + type + ", key=" + key + ", subkey=" + subkey + ", updateSpel=" + updateSpel + "]";
	}
}