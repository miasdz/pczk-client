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
	public void update() {
		System.out.println(this);
		System.out.println("参数回调函数");
	}
}
