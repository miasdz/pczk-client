package itwatertop.core.beans.factory.config;

import org.springframework.beans.factory.BeanNameAware;

import itwatertop.core.beans.factory.config.callback.ParamChangeCallback;

public class Bean implements BeanNameAware, ParamChangeCallback{
	private String name;
	private String beanName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		System.out.println(beanName+":original="+this.name+",new="+name);
		this.name = name;
		System.out.println(this);
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}

	@Override
	public void beforeUpdate() {
		System.out.println(this+"参数更新前回调");
	}

	@Override
	public void afterUpdate() {
		System.out.println(this+"参数更新后回调");
	}
}
