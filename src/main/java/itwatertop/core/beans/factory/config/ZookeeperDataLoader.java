package itwatertop.core.beans.factory.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.Assert;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import itwatertop.core.beans.factory.config.callback.ParamChangeCallback;

/**
 * zookeeper参数处理<br/>
 * @since 12.09.2018 初版
 * @since 03.15.2019 增加回调方法
 * */
public final class ZookeeperDataLoader extends BaseLoader implements BeanFactoryAware {
	private Logger logger = LoggerFactory.getLogger(ZookeeperDataLoader.class);
	private String rootLoc = "/pc/";
	
	private String system;
	private String mode;
	private boolean initFlag = false;
	//zookeeper系统参数路径
	private String sysLocation;
	private ZooKeeper zookeeper;
	
	private String authScheme = PczkConstants.DEFAULT_SCHEME;
	private String authInfo;
	
	private BeanFactory beanFactory;
	
	private String charset = "UTF-8";
	
	public ZookeeperDataLoader() {
		this.setSupportProtocol("zookeeper");
	}
	
	public void init() {
		Assert.notNull(system, "ZookeeperDataLoader must set system");
		Assert.notNull(mode, "ZookeeperDataLoader must set mode");
		Assert.notNull(getAddress(), "ZookeeperDataLoader must set address");
		
		sysLocation = rootLoc+system+"/"+mode+"/";
		try {
			zookeeper = new ZooKeeper(getAddress(), getSessionTimeout(), new Watcher() {
				public void process(WatchedEvent event) {
					logger.info(event.toString());
				}
			}, true);
			if(authInfo!=null) {
				zookeeper.addAuthInfo(authScheme, authInfo.getBytes());
			}
		} catch (IOException e) {
			logger.error("ZooKeeper client init faild", e);
			throw new RuntimeException(e);
		}
		initFlag = true;
	}
	
	@Override
	public void process(Map<Object, Object> context) {
		Assert.notNull(context, "process method parameters must not null");
		Assert.isTrue(initFlag, "ZookeeperDataLoader must first run init()");
		String key = (String)context.get(PczkConstants.DATA_KEY);
		// SpEL表达式解析
		final StandardEvaluationContext spelContext = new StandardEvaluationContext();
		spelContext.setBeanResolver(new BeanFactoryResolver(beanFactory));
		final ExpressionParser parser = new SpelExpressionParser();
		
		try {
			// zookeeper watch回调
			final Watcher watcher = new Watcher() {
				public void paramChangeCallback(String spel) {
					//获取使用该参数的bean
					String beanName = spel.substring(0, spel.indexOf('.'));
					Expression parseExpression = parser.parseExpression(beanName);
					//获取bean
					Object value = parseExpression.getValue(spelContext);
					//判断是否实现ParamChangeCallback接口
					if(ParamChangeCallback.class.isAssignableFrom(value.getClass())) {
						((ParamChangeCallback)(value)).update();
					}
				}
				public void process(WatchedEvent event) {
					logger.info(event.toString());
					try {
						// 再次注册事件并获取新更新数据
						byte[] newData = zookeeper.getData(event.getPath(), this, null);
						String strNewData = new String(newData, charset);
						String valName = event.getPath().substring(sysLocation.length());
						List<PlaceholderMsg> callbackBeans = ParamCenterStore.getCallbackBeans(valName);
						Iterator<PlaceholderMsg> iterator = callbackBeans.iterator();
						while(iterator.hasNext()) {
							PlaceholderMsg placeholderMsg = iterator.next();
							String setVal = null;
							switch(placeholderMsg.type) {
							case PlaceholderMsg.TYPE_OBJECT:
								JSONObject jsonObject = JSON.parseObject(strNewData);
								setVal = jsonObject.getString(placeholderMsg.subkey);
								break;
							case PlaceholderMsg.TYPE_ARRAY:
						        JSONArray jsonArray = JSON.parseArray(strNewData);
						        setVal = jsonArray.getString(Integer.valueOf(placeholderMsg.subkey));
						        break;
							case PlaceholderMsg.TYPE_STRING:
								setVal = strNewData;
								break;
							}
							// 将修改的新值更新到对应的bean属性内
							Expression parseExpression = parser.parseExpression(placeholderMsg.updateSpel);
							parseExpression.setValue(spelContext, setVal);
							logger.info("Parameter {} value updates to {}", placeholderMsg.updateSpel, setVal);
							paramChangeCallback(placeholderMsg.updateSpel);
						}
					} catch (Exception e) {
						logger.error("node data updated failed", e);
					}
				}
			}; 
			byte[] data = zookeeper.getData(sysLocation+key, watcher, null);
			context.put(PczkConstants.OPERATE_RESULT, new String(data, charset));
		} catch(KeeperException.InvalidACLException e) {
			logger.error("zookeeper acl invalid", e);
			throw new RuntimeException(e);
		} catch (Exception e) {
			logger.error("zookeeper get data faild", e);
		}
	}
	
	public String getSystem() {
		return system;
	}
	public void setSystem(String system) {
		this.system = system;
	}
	public String getMode() {
		return mode;
	}
	public void setMode(String mode) {
		this.mode = mode;
	}
	public void setRootLoc(String rootLoc) {
		this.rootLoc = rootLoc;
	}
	public void setAuthScheme(String authScheme) {
		this.authScheme = authScheme;
	}
	public void setAuthInfo(String authInfo) {
		this.authInfo = authInfo;
	}
	public void setCharset(String charset) {
		this.charset = charset;
	}
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}
}
