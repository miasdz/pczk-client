package itwatertop;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpringHello {
	Logger logger = LoggerFactory.getLogger(SpringHello.class);
	private String name;
	private List listTest;
	private TestBean t1;
	private TestBean t2;
	private Map mapTest;
	private Set setTest;
	
	public void show() {
		logger.info("logback test");
		System.out.println("S:"+name+",L:"+listTest+",M:"+mapTest+",S:"+setTest);
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TestBean getT1() {
		return t1;
	}
	public void setT1(TestBean t1) {
		this.t1 = t1;
	}
	public TestBean getT2() {
		return t2;
	}
	public void setT2(TestBean t2) {
		this.t2 = t2;
	}
	public void setListTest(List listTest) {
		this.listTest = listTest;
	}
	public void setMapTest(Map mapTest) {
		this.mapTest = mapTest;
	}
	public void setSetTest(Set setTest) {
		this.setTest = setTest;
	}
	public List getListTest() {
		return listTest;
	}
	public Map getMapTest() {
		return mapTest;
	}
	public Set getSetTest() {
		return setTest;
	}
}
