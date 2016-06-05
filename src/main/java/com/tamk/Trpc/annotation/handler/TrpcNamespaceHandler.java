package com.tamk.Trpc.annotation.handler;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.tamk.Trpc.annotation.parser.ConsumerAutoCreatorBeanDefinitionParser;
import com.tamk.Trpc.annotation.parser.ProviderBeanDefinitionParser;

public class TrpcNamespaceHandler extends NamespaceHandlerSupport {

	@Override
	public void init() {
		registerBeanDefinitionParser("provider", new ProviderBeanDefinitionParser());
		registerBeanDefinitionParser("annotation-consumer", new ConsumerAutoCreatorBeanDefinitionParser());
	}

}
