package com.tamk.Trpc.annotation.parser;

import java.lang.reflect.Field;

import javax.annotation.Resource;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.util.ReflectionUtils;

import com.tamk.Trpc.annotation.TrpcConsumer;
import com.tamk.Trpc.exception.TrpcException;

public class ConsumerAutoCreator implements BeanFactoryPostProcessor {
	private static final String INTERFACE_NAME = "interfaceName";

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
		final DefaultListableBeanFactory factory = (DefaultListableBeanFactory) beanFactory;
		for (String beanName : factory.getBeanDefinitionNames()) {
			Class<?> clazz = factory.getType(beanName);
			if (null != clazz) {
				ReflectionUtils.doWithFields(clazz, new ReflectionUtils.FieldCallback() {
					@Override
					public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
						if (field.isAnnotationPresent(TrpcConsumer.class)) {
							ReflectionUtils.makeAccessible(field);
							String beanName = getConsumerBeanName(field);
							doRegistConsumer(factory, beanName, field.getType());
						}
					}

				});
			}
		}

	}

	private void doRegistConsumer(DefaultListableBeanFactory factory, String beanName, Class<?> interfaceClass) {
		try {
			if (factory.containsBean(beanName)) {
				DefaultListableBeanFactory factoryTmp = factory;
				while (null != factoryTmp && !factoryTmp.containsBeanDefinition(beanName)) {
					factoryTmp = (DefaultListableBeanFactory) factoryTmp.getParentBeanFactory();
				}
				if (null == factoryTmp) {
					throw new TrpcException(String.format("there are no factory contain beanDefinition [beanDefinition = %s]", beanName));
				}

				BeanDefinition bd = factoryTmp.getBeanDefinition(beanName);
				if (bd.isAbstract()) {
					bd.setAutowireCandidate(true);
					PropertyValue interfaceNameValue = new PropertyValue(INTERFACE_NAME, new TypedStringValue(interfaceClass.getName()));
					bd.getPropertyValues().addPropertyValue(interfaceNameValue);
					factory.registerBeanDefinition(beanName, bd);
				}
			} else {
				GenericBeanDefinition bd = (GenericBeanDefinition) BeanDefinitionReaderUtils.createBeanDefinition(null, com.tamk.Trpc.consumer.TrpcConsumer.class.getName(), null);
				bd.setAutowireCandidate(true);
				bd.setInitMethodName("init");
				bd.setScope(ConfigurableBeanFactory.SCOPE_SINGLETON);
				PropertyValue interfaceNameValue = new PropertyValue(INTERFACE_NAME, new TypedStringValue(interfaceClass.getName()));
				bd.getPropertyValues().addPropertyValue(interfaceNameValue);
				factory.registerBeanDefinition(beanName, bd);
			}

			factory.getBean(beanName);
		} catch (TrpcException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private String getConsumerBeanName(Field field) {
		Qualifier qualifierAnnotation = field.getAnnotation(Qualifier.class);
		Resource resourceAnnotation = field.getAnnotation(Resource.class);

		if (null != qualifierAnnotation && StringUtils.isBlank(qualifierAnnotation.value())) {
			return qualifierAnnotation.value();
		} else if (null != resourceAnnotation) {
			if (StringUtils.isBlank(resourceAnnotation.name())) {
				return field.getName();
			} else {
				return resourceAnnotation.name();
			}
		} else {
			return ClassUtils.getShortClassName(field.getType());
		}
	}
}
