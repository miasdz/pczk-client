package itwatertop;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

public class SpelTest {
	@Test
	public void test() {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		SpringHello bean = (SpringHello)context.getBean("SpringHello");
		bean.show();
		StandardEvaluationContext spelContext = new StandardEvaluationContext();
		spelContext.setBeanResolver(new BeanFactoryResolver(context));
		ExpressionParser parser = new SpelExpressionParser();
		Expression exp = parser.parseExpression("@SpringHello.mapTest['list'][0]");
		Object value = exp.getValue(spelContext);
		System.out.println(value);
//		while(true);
	}
}
