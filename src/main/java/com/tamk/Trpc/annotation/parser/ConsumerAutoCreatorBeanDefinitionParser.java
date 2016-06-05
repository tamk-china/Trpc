package com.tamk.Trpc.annotation.parser;

import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.w3c.dom.Element;

/**
 * there are no attributes on element, so it need not implement method doParse 
 * 
 * @author kuanqiang.tkq
 */
public class ConsumerAutoCreatorBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

	@Override
	protected Class<?> getBeanClass(Element element) {
		// implement on element
		return ConsumerAutoCreator.class;
	}

	@Override
	protected boolean shouldGenerateIdAsFallback() {
		return true;
	}
	
}
