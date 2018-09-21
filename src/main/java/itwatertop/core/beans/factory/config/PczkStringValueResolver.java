package itwatertop.core.beans.factory.config;

/**
 * zk{}表达式解析器
 * */
public interface PczkStringValueResolver {
	/**
	 * @param spel表达式
	 * @param strVal zk{}表达式
	 * */
	String resolveStringValue(StringBuilder expression, String strVal);
}
