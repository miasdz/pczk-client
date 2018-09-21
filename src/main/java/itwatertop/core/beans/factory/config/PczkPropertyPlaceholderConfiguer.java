package itwatertop.core.beans.factory.config;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import itwatertop.core.beans.factory.config.PczkPropertyPlaceholderHelper.PczkPlaceholderResolver;

/**
 * Spring implementation of Parameter Center based on ZooKeeper
 * @author guojikai
 * */
public class PczkPropertyPlaceholderConfiguer implements 
		BeanFactoryPostProcessor, BeanNameAware, BeanFactoryAware, PriorityOrdered{
	private Logger logger = LoggerFactory.getLogger(PczkPropertyPlaceholderConfiguer.class);

	private int order = Ordered.LOWEST_PRECEDENCE;
	private String beanName;
	private BeanFactory beanFactory;
	private ZookeeperDataLoader zkDataLoader;
	private boolean trimValues = false;
	private String nullValue;

	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		PczkStringValueResolver valueResolver = new ZkStringValueResolver();
		PczkBeanDefinitionVisitor visitor = new PczkBeanDefinitionVisitor(valueResolver);

		String[] beanNames = beanFactory.getBeanDefinitionNames();
		for (String curName : beanNames) {
			if (!(curName.equals(this.beanName) && beanFactory.equals(this.beanFactory))) {
				BeanDefinition beanDefinition = beanFactory.getBeanDefinition(curName);
				try {
					StringBuilder beanName = new StringBuilder().append("@"+curName);
					visitor.visitBeanDefinition(beanName, beanDefinition);
				}
				catch (Exception ex) {
					throw new BeanDefinitionStoreException(beanDefinition.getResourceDescription(), curName, ex.getMessage(), ex);
				}
			}
		}
	}
	
	private final class ZkStringValueResolver implements PczkStringValueResolver {
		private final PczkPropertyPlaceholderHelper helper;
		private final PczkPlaceholderResolver resolver;
		
		public ZkStringValueResolver() {
			this.helper = new PczkPropertyPlaceholderHelper(
					"zk{", "}", null, true);
			this.resolver = new PropertyPlaceholderConfigurerResolver();
		}
		public String resolveStringValue(StringBuilder expression, String strVal) {
			String resolved = this.helper.replacePlaceholders(expression, strVal, this.resolver);
			if (trimValues) {
				resolved = resolved.trim();
			}
			return (resolved.equals(nullValue) ? null : resolved);
		}
	}
	
	private final class PropertyPlaceholderConfigurerResolver implements PczkPlaceholderResolver {
		public String resolvePlaceholder(StringBuilder expression, String placeholderName) {
			Map<Object, Object> context = new HashMap<Object, Object>();
			PlaceholderMsg placeholderMsg = PlaceholderMsg.getPlaceholderMsg(placeholderName);
			placeholderMsg.updateSpel = expression.toString();
			
			String res = null;
			if(ParamCenterStore.getParam(placeholderMsg.key)==null) {
				context.put(PczkConstants.DATA_KEY, placeholderMsg.key);
				zkDataLoader.process(context);
				res = (String) context.get(PczkConstants.OPERATE_RESULT);
				ParamCenterStore.setParam(placeholderMsg.key, res);
			}else {
				res = ParamCenterStore.getParam(placeholderMsg.key);
			}
			
			switch(placeholderMsg.type) {
			case PlaceholderMsg.TYPE_OBJECT:
				JSONObject jsonObject = JSON.parseObject(res);
				res = jsonObject.getString(placeholderMsg.subkey);
				break;
			case PlaceholderMsg.TYPE_ARRAY:
		        JSONArray jsonArray = JSON.parseArray(res);
		        res = jsonArray.getString(Integer.valueOf(placeholderMsg.subkey));
		        break;
			case PlaceholderMsg.TYPE_STRING:
				break;
			}
			
			if(placeholderMsg.updateSpel.indexOf(PczkConstants.IGNORE_EXP)==-1) {
				//支持实时更新的
				logger.info(placeholderMsg.toString());
				ParamCenterStore.callbackRegistor(placeholderMsg.key, placeholderMsg);
			}
			return res;
		}
	}
	public int getOrder() {
		return order;
	}
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
	public void setBeanName(String name) {
		this.beanName = name;
	}
	public void setTrimValues(boolean trimValues) {
		this.trimValues = trimValues;
	}
	public void setNullValue(String nullValue) {
		this.nullValue = nullValue;
	}
	public void setZkDataLoader(ZookeeperDataLoader zkDataLoader) {
		this.zkDataLoader = zkDataLoader;
	}
}
