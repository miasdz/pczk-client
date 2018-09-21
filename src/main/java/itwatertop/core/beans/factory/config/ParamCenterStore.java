package itwatertop.core.beans.factory.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 参数中心相关信息存储
 * */
class ParamCenterStore {
	/** 存储从参数中心获取到的参数缓存，防止多次获取同一值*/
	private static final Map<String, String> params= new HashMap<String, String>();
	/** 存储使用该参数的bean表达式*/
	private static final Map<String, List<PlaceholderMsg>> callbackRegistor = new HashMap<String, List<PlaceholderMsg>>();
	
	/** 获取参数缓存值*/
	public static String getParam(String key) {
		return params.get(key);
	}
	public static void setParam(String key, String value) {
		params.put(key, value);
	}
	
	/**
	 * 注册使用该参数的bean
	 * @param paramName 参数名
	 * @param beanName 使用该参数的bean name
	 * */
	public static void callbackRegistor(String paramName, PlaceholderMsg msg) {
		if(callbackRegistor.get(paramName)!=null && !callbackRegistor.get(paramName).contains(msg)) {
			callbackRegistor.get(paramName).add(msg);
		}else {
			List<PlaceholderMsg> list = new ArrayList<PlaceholderMsg>();
			list.add(msg);
			callbackRegistor.put(paramName, list);
		}
	}
	/**
	 * 获取某一个参数的绑定的bean列表
	 * @param paramName 参数名
	 * @return bean列表
	 * */
	public static List<PlaceholderMsg> getCallbackBeans(String paramName){
		return callbackRegistor.get(paramName);
	}
}
