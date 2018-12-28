package itwatertop.core.beans.factory.config;

import org.springframework.beans.factory.BeanNameAware;

public class Bean implements BeanNameAware{
	private String name;
	private String beanName;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		System.out.println(beanName+":original="+this.name+",new="+name);
		this.name = name;
	}

	public void setBeanName(String name) {
		this.beanName = name;
	}
}
