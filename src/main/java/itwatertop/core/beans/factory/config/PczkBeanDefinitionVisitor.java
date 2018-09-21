package itwatertop.core.beans.factory.config;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.beans.factory.config.RuntimeBeanNameReference;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * <p>Used by {@link PczkPropertyPlaceholderConfiguer} to parse "zk{}" String values
 * contained in a BeanDefinition, resolving bean property placeholders found.<br/>
 *
 * 实时参数更新的限制: <br/>
 * 1.仅支持String、Array、List和Map(key必须为String)的嵌套，集合基本元素为String，Map的key值不支持实时更新{@link PczkConstants.IGNORE_EXP}，对象属性使用zk{}表达式时需要有对应的getter,setter方法<br/>
 * eg.{@code String,String[],List<String>,Map<String,String>,Map<String,List<String>>}<br/>
 * 2.不支持Set<br/>
 * 
 * @see PropertyPlaceholderConfigurer
 * @author guojikai
 * @since 20.09.2018
 */
public class PczkBeanDefinitionVisitor {
	/** zk{}表达式解析器*/
	private PczkStringValueResolver valueResolver;

	/**
	 * Create a new BeanDefinitionVisitor, applying the specified
	 * value resolver to all bean metadata values.
	 * @param valueResolver the StringValueResolver to apply
	 */
	public PczkBeanDefinitionVisitor(PczkStringValueResolver valueResolver) {
		Assert.notNull(valueResolver, "StringValueResolver must not be null");
		this.valueResolver = valueResolver;
	}

	/**
	 * Create a new BeanDefinitionVisitor for subclassing.
	 * Subclasses need to override the {@link #resolveStringValue} method.
	 */
	protected PczkBeanDefinitionVisitor() {
	}


	/**
	 * Traverse the given BeanDefinition object and the MutablePropertyValues
	 * and ConstructorArgumentValues contained in them.
	 * @param beanDefinition the BeanDefinition object to traverse
	 * @see #resolveStringValue(String)
	 */
	public void visitBeanDefinition(StringBuilder beanName, BeanDefinition beanDefinition) {
		visitPropertyValues(beanName, beanDefinition.getPropertyValues());
	}

	protected void visitPropertyValues(StringBuilder spelExp, MutablePropertyValues pvs) {
		PropertyValue[] pvArray = pvs.getPropertyValues();
		for (PropertyValue pv : pvArray) {
			StringBuilder subExp = new StringBuilder().append(spelExp).append(".").append(pv.getName()); 
			Object newVal = resolveValue(subExp, pv.getValue());
			if (!ObjectUtils.nullSafeEquals(newVal, pv.getValue())) {
				pvs.add(pv.getName(), newVal);
			}
		}
	}

	@SuppressWarnings("rawtypes")
	protected Object resolveValue(StringBuilder expression, Object value) {
		if (value instanceof BeanDefinition) {
			visitBeanDefinition(expression, (BeanDefinition) value);
		}
		else if (value instanceof BeanDefinitionHolder) {
			// <property><bean/></property>
			visitBeanDefinition(expression, ((BeanDefinitionHolder) value).getBeanDefinition());
		}
		else if (value instanceof RuntimeBeanReference) {
			// <ref/>
			RuntimeBeanReference ref = (RuntimeBeanReference) value;
			String newBeanName = resolveStringValue(expression, ref.getBeanName());
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanReference(newBeanName);
			}
		}
		else if (value instanceof RuntimeBeanNameReference) {
			RuntimeBeanNameReference ref = (RuntimeBeanNameReference) value;
			String newBeanName = resolveStringValue(expression, ref.getBeanName());
			if (!newBeanName.equals(ref.getBeanName())) {
				return new RuntimeBeanNameReference(newBeanName);
			}
		}
		else if (value instanceof Object[]) {
			visitArray(expression, (Object[]) value);
		}
		else if (value instanceof List) {
			visitList(expression, (List) value);
		}
		else if (value instanceof Set) {
			visitSet(expression, (Set) value);
		}
		else if (value instanceof Map) {
			visitMap(expression, (Map) value);
		}
		else if (value instanceof TypedStringValue) {
			TypedStringValue typedStringValue = (TypedStringValue) value;
			String stringValue = typedStringValue.getValue();
			if (stringValue != null) {
				String visitedString = resolveStringValue(expression, stringValue);
				typedStringValue.setValue(visitedString);
			}
		}
		else if (value instanceof String) {
			return resolveStringValue(expression, (String) value);
		}
		return value;
	}

	protected void visitArray(StringBuilder expression, Object[] arrayVal) {
		for (int i = 0; i < arrayVal.length; i++) {
			Object elem = arrayVal[i];
			StringBuilder subExp = new StringBuilder(expression).append("["+i+"]");
			Object newVal = resolveValue(subExp, elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				arrayVal[i] = newVal;
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitList(StringBuilder expression, List listVal) {
		for (int i = 0; i < listVal.size(); i++) {
			Object elem = listVal.get(i);
			StringBuilder subExp = new StringBuilder(expression).append("["+i+"]");
			Object newVal = resolveValue(subExp, elem);
			if (!ObjectUtils.nullSafeEquals(newVal, elem)) {
				listVal.set(i, newVal);
			}
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitSet(StringBuilder expression, Set setVal) {
		Set newContent = new LinkedHashSet();
		boolean entriesModified = false;
		for (Object elem : setVal) {
			int elemHash = (elem != null ? elem.hashCode() : 0);
			StringBuilder subExp = new StringBuilder(expression).append(PczkConstants.IGNORE_EXP);
			Object newVal = resolveValue(subExp, elem);
			int newValHash = (newVal != null ? newVal.hashCode() : 0);
			newContent.add(newVal);
			entriesModified = entriesModified || (newVal != elem || newValHash != elemHash);
		}
		if (entriesModified) {
			setVal.clear();
			setVal.addAll(newContent);
		}
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void visitMap(StringBuilder expression, Map<?, ?> mapVal) {
		Map newContent = new LinkedHashMap();
		boolean entriesModified = false;
		for (Map.Entry entry : mapVal.entrySet()) {
			Object key = entry.getKey();
			int keyHash = (key != null ? key.hashCode() : 0);
			StringBuilder subKeyExp = new StringBuilder(expression).append(PczkConstants.IGNORE_EXP);
			Object newKey = resolveValue(subKeyExp, key);
			StringBuilder subExp = new StringBuilder(expression);
			if(newKey instanceof TypedStringValue) {
				String keyStr = ((TypedStringValue)newKey).getValue();
				subExp.append("['"+keyStr+"']");
			}else if(newKey instanceof String){
				subExp.append("['"+(String)newKey+"']");
			}else {
				subExp.append(PczkConstants.IGNORE_EXP);
			}
			int newKeyHash = (newKey != null ? newKey.hashCode() : 0);
			Object val = entry.getValue();
			Object newVal = resolveValue(subExp, val);
			newContent.put(newKey, newVal);
			entriesModified = entriesModified || (newVal != val || newKey != key || newKeyHash != keyHash);
		}
		if (entriesModified) {
			mapVal.clear();
			mapVal.putAll(newContent);
		}
	}

	/**
	 * Resolve the given String value, for example parsing placeholders.
	 * @param strVal the original String value
	 * @return the resolved String value
	 */
	protected String resolveStringValue(StringBuilder expression, String strVal) {
		if (this.valueResolver == null) {
			throw new IllegalStateException("No StringValueResolver specified - pass a resolver " +
					"object into the constructor or override the 'resolveStringValue' method");
		}
		String resolvedValue = this.valueResolver.resolveStringValue(expression, strVal);
		// Return original String if not modified.
		return (strVal.equals(resolvedValue) ? strVal : resolvedValue);
	}

}
