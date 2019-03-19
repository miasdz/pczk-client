package itwatertop.core.beans.factory.config.callback;

/**
 * 参数更新前需要调用回调方法时实现该接口
 * */
public interface ParamChangeBeforeCallback {
	/**
	 * 参数更新回调方法
	 * */
	public void beforeUpdate();
}

