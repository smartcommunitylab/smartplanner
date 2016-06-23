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

package it.sayservice.platform.smartplanner.configurations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import it.sayservice.platform.smartplanner.utils.Constants;

@Component
public class ConfigurationManager {

	@Autowired
	private ApplicationContext appContext;

	private Map<String, RouterConfig> configs = new HashMap<String, RouterConfig>();

	public ConfigurationManager() {
		super();
	}

	public ConfigurationManager(String... routerNames) throws FileNotFoundException {
		for (String routerName : routerNames) {
			InputStream in = new FileInputStream(new File("src/main/resources/" + routerName + ".yml"));
			Yaml yaml = new Yaml();
			RouterConfig conf = yaml.loadAs(in, RouterConfig.class);
			configs.put(routerName, conf);
		}
	}

	@PostConstruct
	public void init() throws FileNotFoundException {
		List<String> routers = Arrays.asList(appContext.getEnvironment().getProperty("routers").split(","));

		for (String routerName : routers) {
			InputStream in = new FileInputStream(new File(System.getenv("OTP_HOME")
					+ System.getProperty("file.separator") + routerName + System.getProperty("file.separator")
					+ Constants.CONFIG_YML + System.getProperty("file.separator") + routerName + ".yml"));
			Yaml yaml = new Yaml();
			RouterConfig conf = yaml.loadAs(in, RouterConfig.class);
			configs.put(routerName, conf);
		}

	}

	public RouterConfig getRouter(String router) {
		return configs.get(router);
	}
	
	public List<String> getRouterKeys() {
		return new ArrayList<String>(configs.keySet());
	}

}
