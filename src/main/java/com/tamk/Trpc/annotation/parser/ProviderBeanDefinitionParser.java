package com.tamk.Trpc.annotation.parser;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.config.TypedStringValue;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.context.annotation.ScannedGenericBeanDefinition;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.w3c.dom.Element;

import com.tamk.Trpc.annotation.TrpcProvider;
import com.tamk.Trpc.annotation.handler.TrpcBeanNameGenerator;
import com.tamk.Trpc.exception.TrpcException;

public class ProviderBeanDefinitionParser implements BeanDefinitionParser {
	private static final String PACKAGE_SEPARATOR = ",";
	private static final String AUTOWIRE = "autowired";
	private static final String SCAN_PACKAGE = "scan-packages";
	private static final String INTERFACE_NAME = "interfaceName";
	private static final String INVOKER = "invoker";

	private int autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
	private String[] packages;

	@SuppressWarnings("deprecation")
	private void checkAndParseElement(Element element) throws TrpcException {
		if (!element.hasAttribute(SCAN_PACKAGE)) {
			throw new TrpcException("scan-packages not exist for trpc:provider");
		}
		String scanPackagesStr = element.getAttribute(SCAN_PACKAGE);
		if (!StringUtils.isBlank(scanPackagesStr)) {
			throw new TrpcException("scan-packages blank for trpc:provider");
		}
		this.packages = scanPackagesStr.split(PACKAGE_SEPARATOR);

		if (element.hasAttribute(AUTOWIRE)) {
			String autowireStr = element.getAttribute(AUTOWIRE);
			if ("byName".equals(autowireStr)) {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;
			} else if ("byType".equals(autowireStr)) {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;
			} else if ("no".equals(autowireStr)) {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
			} else if ("audoDetected".equals(autowireStr)) {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;
			} else if ("constructor".equals(autowireStr)) {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;
			} else {
				this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
			}
		} else {
			this.autowireMode = AutowireCapableBeanFactory.AUTOWIRE_NO;
		}
	}

	@Override
	public BeanDefinition parse(Element element, ParserContext parserContext) {
		try {
			checkAndParseElement(element);

			ProviderBeanDefinitionScanner scanner = new ProviderBeanDefinitionScanner(parserContext.getRegistry(), false);
			scanner.setBeanNameGenerator(new TrpcBeanNameGenerator("trpc_", ""));
			scanner.setResourceLoader(parserContext.getReaderContext().getResourceLoader());
			scanner.setBeanDefinitionDefaults(parserContext.getDelegate().getBeanDefinitionDefaults());
			scanner.setAutowireCandidatePatterns(parserContext.getDelegate().getAutowireCandidatePatterns());
			scanner.resetFilters(false);
			scanner.addIncludeFilter(new AnnotationTypeFilter(TrpcProvider.class));

			Set<BeanDefinitionHolder> beanDefinitions = scanner.doScan(this.packages);
			for (BeanDefinitionHolder holder : beanDefinitions) {
				ScannedGenericBeanDefinition bdSource = (ScannedGenericBeanDefinition) holder.getBeanDefinition();

				GenericBeanDefinition bd = (GenericBeanDefinition) BeanDefinitionReaderUtils.createBeanDefinition(null, com.tamk.Trpc.provider.TrpcProvider.class.getName(), parserContext
						.getReaderContext().getBeanClassLoader());
				bd.setAutowireCandidate(false);
				bd.setInitMethodName("init");

				// set interface
				String[] interfaces = bdSource.getMetadata().getInterfaceNames();
				if (1 != interfaces.length) {
					throw new TrpcException(String.format("class should has a single interface [class = %s]", bdSource.getBeanClassName()));
				}
				TypedStringValue interfaceNameValue = new TypedStringValue(interfaces[0]);
				bd.getPropertyValues().addPropertyValue(new PropertyValue(INTERFACE_NAME, interfaceNameValue));

				// set invoker
				Map<String, Object> annoAttrs = bdSource.getMetadata().getAnnotationAttributes(TrpcProvider.class.getName());
				String invokerName = (String) annoAttrs.get(INVOKER);
				RuntimeBeanReference invokerRef = null;
				if (StringUtils.isBlank(invokerName)) {
					invokerRef = new RuntimeBeanReference(TrpcBeanNameGenerator.getBeanName(bdSource.getBeanClassName()));
				} else {
					invokerRef = new RuntimeBeanReference(invokerName);
				}
				bd.getPropertyValues().addPropertyValue(new PropertyValue(INVOKER, invokerRef));

				bd.setAutowireCandidate(true);
				bd.setAutowireMode(this.autowireMode);

				// beans can share the same bean name
				parserContext.getRegistry().registerBeanDefinition(holder.getBeanName(), bd);
				BeanDefinitionHolder bdHolder = new BeanDefinitionHolder(bd, holder.getBeanName(), null);
				// send bean definition registered event
				parserContext.getReaderContext().fireComponentRegistered(new BeanComponentDefinition(bdHolder));
			}
		} catch (TrpcException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}

		return null;
	}
}
