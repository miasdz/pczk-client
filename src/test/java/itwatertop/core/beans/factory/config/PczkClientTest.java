package itwatertop.core.beans.factory.config;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PczkClientTest {
	private ClassPathXmlApplicationContext context;

	@Before
	public void Before() {
		context = new ClassPathXmlApplicationContext("applicationContext.xml");
	}
	
	@Test
	public void test() {
		try {
			Thread.sleep(300*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@After
	public void after() {
		context.close();
	}
}
