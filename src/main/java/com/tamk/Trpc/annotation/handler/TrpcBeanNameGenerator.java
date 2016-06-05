package com.tamk.Trpc.annotation.handler;

import java.beans.Introspector;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * @author kuanqiang.tkq
 */
public class TrpcBeanNameGenerator implements BeanNameGenerator {
	private String prefix;
	private String suffix;

	public TrpcBeanNameGenerator(String prefix, String suffix) {
		this.prefix = prefix;
		this.suffix = suffix;
	}

	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		String beanName = getBeanName(definition.getBeanClassName());

		return prefix + beanName + suffix;
	}

	private static final String PACKAGE_SEPARATOR = ".";
	private static final String INNER_CLASS_SEPARATOR = "$";
	private static final String CGLIB_CLASS_SEPARATOR = "$$";

	public static  String getBeanName(String className) {
		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("className blank");
		}

		String simpleClassName = className.substring(className.lastIndexOf(PACKAGE_SEPARATOR) + 1);
		int nameEndIndex = simpleClassName.lastIndexOf(CGLIB_CLASS_SEPARATOR);
		if (-1 == nameEndIndex) {
			nameEndIndex = simpleClassName.length();
		}

		String shortName = simpleClassName.substring(0, nameEndIndex);
		shortName.replace(INNER_CLASS_SEPARATOR, PACKAGE_SEPARATOR);

		return Introspector.decapitalize(shortName);
	}
}
