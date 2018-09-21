package itwatertop;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringHelloTest {
	@Test
	public void test() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		SpringHello bean = (SpringHello)context.getBean("SpringHello");
		bean.show();
	}
	
	@Test
	public void t1() {
		System.out.println("\"");
	}
}
