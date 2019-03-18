package itwatertop.core.beans.factory.config.callback;

/**
 * 参数更新时需要调用回调方法时实现该接口
 * */
public interface ParamChangeCallback {
	/**
	 * 参数更新回调方法
	 * */
	public void update();
}

