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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.io.ByteStreams;

import io.swagger.annotations.ApiParam;
import it.sayservice.platform.smartplanner.cache.CacheManager;
import it.sayservice.platform.smartplanner.cache.RoutesDBHelper;
import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
import it.sayservice.platform.smartplanner.utils.Constants;

@Controller
//@RequestMapping("smart-planner")
public class ClientCacheCtrl {

	@Autowired
	private CacheManager manager;

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/getCacheStatus")
	public @ResponseBody Map<String, CacheUpdateResponse> getCacheStatus(@PathVariable String router,
			HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, String> versions) {
		try {
			Map<String, CacheUpdateResponse> status = manager.getStatus(router, versions);

			return status;

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/getPartialCacheStatus")
	public @ResponseBody Map<String, CacheUpdateResponse> getPartialCacheStatus(@PathVariable String router,
			HttpServletRequest request, HttpServletResponse response, @RequestBody Map<String, Map> par) {
		try {

			Map<String, CacheUpdateResponse> status = manager.getPartialStatus(router, par);

			if (status == null) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				return null;
			}

			return status;

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getCacheUpdate/{agencyId}/{fileName}")
	public @ResponseBody CompressedTransitTimeTable getCacheUpdate(@PathVariable String router,
			HttpServletRequest request, HttpServletResponse response, @PathVariable String agencyId,
			@ApiParam(value = "JS file under client folder.", required = true) @PathVariable String fileName) {
		try {
			CompressedTransitTimeTable update = manager.getUpdate(router, agencyId, fileName);

			return update;

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/resetCache")
	public @ResponseBody void resetCache(@PathVariable String router, HttpServletRequest request,
			HttpServletResponse response) {
		try {
			manager.init(router);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/routesDB/{appId}", produces = "application/zip")
	public @ResponseBody void getRoutesDB(HttpServletRequest request, @PathVariable String router,
			@PathVariable String appId, HttpServletResponse response) {
		try {
			getZipDB(response, router, appId, false);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/routesDB/{appId}/extended", produces = "application/zip")
	public @ResponseBody void getExtendedRoutesDB(HttpServletRequest request, @PathVariable String router,
			@PathVariable String appId, HttpServletResponse response) {
		try {
			getZipDB(response, router, appId, true);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	private void getZipDB(HttpServletResponse response, String router, String appId, boolean extended)
			throws FileNotFoundException, IOException {
		String fileName = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR + System.getProperty("file.separator") + RoutesDBHelper.DB_NAME + "_"
				+ appId + (extended ? "_extended" : "") + ".zip";

		response.setContentType("application/zip");
		response.setHeader("Content-Disposition", "attachment; filename=\"routesdb.zip\"");

		ByteStreams.copy(new FileInputStream(fileName), response.getOutputStream());
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/versions")
	public @ResponseBody Map<String, Long> getVersions(HttpServletRequest request, HttpServletResponse response,
			HttpSession session, @PathVariable String router) {
		try {
			return manager.getVersions(router);
		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			return null;
		}
	}

}
