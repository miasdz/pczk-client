package itwatertop.core.beans.factory.config;

import java.util.Map;

/**
 * 数据加载基类
 * @since 10.09.2018
 * */
public abstract class BaseLoader {
	/**
	 * 数据加载支持协议
	 * */
	private String supportProtocol;
	/**
	 * 数据加载地址
	 * */
	private String address;
	/**
	 * 远程连接默认超时时间
	 * */
	private int sessionTimeout = 60000;
	
	/**
	 * 数据处理
	 * @param context 数据处理参数配置
	 * */
	public abstract void process(Map<Object, Object> context);

	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getSupportProtocol() {
		return supportProtocol;
	}
	public void setSupportProtocol(String supportProtocol) {
		this.supportProtocol = supportProtocol;
	}
	public int getSessionTimeout() {
		return sessionTimeout;
	}
	public void setSessionTimeout(int sessionTimeout) {
		this.sessionTimeout = sessionTimeout;
	}
}
