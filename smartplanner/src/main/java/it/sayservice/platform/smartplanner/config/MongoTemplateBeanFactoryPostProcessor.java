/**
 * Copyright 2011-2016 SAYservice s.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.sayservice.platform.smartplanner.config;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.util.StringUtils;

import com.mongodb.MongoClient;

public class MongoTemplateBeanFactoryPostProcessor implements BeanDefinitionRegistryPostProcessor {

	private List<String> routers = new ArrayList<String>();

	public MongoTemplateBeanFactoryPostProcessor() {
		super();
		// TODO Auto-generated constructor stub
	}

	public MongoTemplateBeanFactoryPostProcessor(String routerIds) {
		for (String router : Arrays.asList(routerIds.split(","))) {
			routers.add(router);
		}
	}

	public MongoTemplateBeanFactoryPostProcessor(Environment springEnvironment) {
		routers = parseRouters(springEnvironment.getProperty("routers"));
	}

	private static List<String> parseRouters(String routers) {
		if (StringUtils.isEmpty(routers)) {
			throw new IllegalArgumentException("Property 'routers' is undefined.");
		}
		return Collections.unmodifiableList(Arrays.asList(routers.split(",")));
	}

	@Override
	public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {

	}

	@Override
	public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
		try {
			for (String router : routers) {
				String dataSourceName = router;

				MongoDbFactory mongoDbFactory = new SimpleMongoDbFactory(new MongoClient(), dataSourceName);
				BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
						.genericBeanDefinition(MongoTemplate.class);
				definitionBuilder.addConstructorArgValue(mongoDbFactory);
				registry.registerBeanDefinition(dataSourceName, definitionBuilder.getBeanDefinition());
			}
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}