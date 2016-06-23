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

package it.sayservice.platform.smartplanner.controllers;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.otp.OTPHandler;
import it.sayservice.platform.smartplanner.otp.OTPManager;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

@Controller
//@RequestMapping("/smart-planner")
public class ConfigCtrl {

	@Autowired
	OTPHandler otpHandler;
	@Autowired
	OTPManager otpManager;
	@Autowired
	RepositoryUtils repositoryUtils;
	@Autowired
	ConfigurationManager configurationManager;
	@Autowired
	MongoRouterMapper mongoRouterMapper;

	public ConfigCtrl() {
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{router}/configuration/init")
	public @ResponseBody Response<String> init(@PathVariable String router) throws SmartPlannerException {

		Response<String> response = new Response<String>();

		try {
			mongoRouterMapper.emptyCollections(router);
			repositoryUtils.init(configurationManager.getRouter(router));
			otpHandler.init(configurationManager.getRouter(router));
			otpManager.init(router);
			otpManager.preinit(true);
			response.setData(router + " data initialized successfully.");
			// its very important since agencyIds maps must be clear for e.g.
			// bologna have no agency 12.
			repositoryUtils.clean();
			otpHandler.clean();
			otpManager.clean();
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return response;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/configuration/clean")
	public @ResponseBody Response<String> clean(@PathVariable String router) throws SmartPlannerException {

		Response<String> response = new Response<String>();

		try {
			repositoryUtils.clean();
			otpHandler.clean();
			otpManager.clean();
			response.setData("variables cleaned successfully.");
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return response;

	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
