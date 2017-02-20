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

import io.swagger.annotations.ApiParam;
import it.sayservice.platform.smartplanner.data.message.otpbeans.ExtendedTransitTimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.GeolocalizedStopRequest;
import it.sayservice.platform.smartplanner.otp.OTPCache;
import it.sayservice.platform.smartplanner.otp.OTPManager;
import it.sayservice.platform.smartplanner.otp.TransitScheduleResults;
import it.sayservice.platform.smartplanner.utils.Constants;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
//@RequestMapping("smart-planner")
public class TransitCtrl {

	@Autowired
	private OTPManager manager;
	@Autowired
	private OTPCache cache;

	public TransitCtrl() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TransitCtrl(OTPManager manager, OTPCache cache) {
		this.manager = manager;
		this.cache = cache;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getroutes/{agencyId}")
	public @ResponseBody void getRoutes(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId) {
		try {
			String routes = manager.getRoutes(router, agencyId);

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(routes);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getstops/{agencyId}/{routeId}")
	public @ResponseBody void getStops(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId) {
		try {
			String stops = manager.getStops(router, agencyId, routeId);

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(stops);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getstops/{agencyId}/{routeId}/{latitude}/{longitude}/{radius:.+}")
	public @ResponseBody void getStops(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId,
			@PathVariable double latitude, @PathVariable double longitude,
			@ApiParam(value = "radius in meters", required = true) @PathVariable double radius) {
		try {
			String stops = manager.getStops(router, agencyId, routeId, latitude, longitude, radius);

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(stops);
		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			e.printStackTrace();
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/gettimetable/{agencyId}/{routeId}/{stopId:.*}")
	public @ResponseBody void getTimeTable(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId,
			@PathVariable String stopId, @ApiParam(required=false) @RequestParam(required=false) Long fromTime) {
		try {
			if (fromTime == null) {
				fromTime = System.currentTimeMillis();
			}			

			String timetable = manager.getTimeTable(router, agencyId, routeId, stopId, fromTime);

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(timetable);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getlimitedtimetable/{agencyId}/{stopId:.*}/{maxElements}")
	public @ResponseBody void getLimitedTimeTable(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String stopId,
			@ApiParam(value = "max number of required trips for each stop.", required = true) @PathVariable Integer maxElements, @ApiParam(required=false) @RequestParam(required=false) Long fromTime) {
		try {
			if (fromTime == null) {
				fromTime = System.currentTimeMillis();
			}
			String timetable = manager.getLimitedTimeTable(router, agencyId, stopId, fromTime, maxElements);

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(timetable);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getTransitTimes/{agencyId}/{routeId}/{from}/{to}")
	public @ResponseBody void getTransitTimes(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId,
			@ApiParam(value = "Start time in milliseconds.", required = true) @PathVariable Long from,
			@ApiParam(value = "End time in milliseconds.", required = true) @PathVariable Long to) {
		try {
			String times = "";
			try {
				ExtendedTransitTimeTable ett = cache.getTransitSchedule(router, agencyId, routeId, from, to,
						TransitScheduleResults.ALL, true, false);
				if (times == null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				} else {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Include.NON_NULL);
					times = mapper.writeValueAsString(ett);
					times = fixTimes(times);
				}
			} catch (Exception e0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(times);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getTransitTimes/{agencyId}/{routeId}/{from}/{to}/extended")
	public @ResponseBody void getExtendedTransitTimes(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId,
			@ApiParam(value = "Start time in milliseconds.", required = true) @PathVariable Long from,
			@ApiParam(value = "End time in milliseconds.", required = true) @PathVariable Long to) {
		try {
			String times = "";
			try {
				ExtendedTransitTimeTable ett = cache.getTransitSchedule(router, agencyId, routeId, from, to,
						TransitScheduleResults.ALL, true, true);
				if (times == null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				} else {
					ObjectMapper mapper = new ObjectMapper();
					times = mapper.writeValueAsString(ett);
					times = fixTimes(times);
				}
			} catch (Exception e0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(times);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getTransitDelays/{agencyId}/{routeId}/{from}/{to}")
	public @ResponseBody void getTransitDelays(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router, @PathVariable String agencyId, @PathVariable String routeId,
			@ApiParam(value = "Start time in milliseconds.", required = true) @PathVariable Long from,
			@ApiParam(value = "End time in milliseconds.", required = true) @PathVariable Long to) {
		try {
			String times = "";
			try {
				ExtendedTransitTimeTable ett = cache.getTransitSchedule(router, agencyId, routeId, from, to,
						TransitScheduleResults.DELAYS, true, false);
				if (times == null) {
					response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
					return;
				} else {
					ObjectMapper mapper = new ObjectMapper();
					mapper.setSerializationInclusion(Include.NON_NULL);
					times = mapper.writeValueAsString(ett);
					times = fixTimes(times);
				}
			} catch (Exception e0) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			}

			response.setContentType("application/json; charset=utf-8");
			response.getWriter().write(times);

		} catch (Exception e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/getGeolocalizedStops")
	public @ResponseBody List<Object> getGeolocalizedStops(HttpServletRequest request, HttpServletResponse response,
			@RequestBody GeolocalizedStopRequest gsr, @PathVariable String router) {
		List<Object> result = null;
		try {
			result = manager.getGeolocalizedStops(router, gsr);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return result;
	}

	private String fixTimes(String s) {
		String r = s;
		for (int i = 0; i <= 5; i++) {
			r = r.replace("\"2" + (i + 4) + ":", "\"0" + i + ":");
		}
		return r;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/agencies")
	public @ResponseBody List<Object> getAgencies(HttpServletRequest request, HttpServletResponse response,
			@PathVariable String router) {
		List<Object> result = null;
		try {
			result = manager.getAgencies(router);

		} catch (Exception e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}

		return result;

	}

	@RequestMapping(value = "/{router}/rest/gtfs/{agencyId}", method = RequestMethod.GET)
	public void downloadFile(HttpServletResponse response, @PathVariable("router") String router,
			@PathVariable("agencyId") String agencyId) throws IOException {

		File file = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.SP_GTFS_FOLDER + System.getProperty("file.separator")
				+ agencyId + ".zip");

		if (!file.exists()) {
//			String errorMessage = "Sorry. The file you are looking for does not exist";
//			System.out.println(errorMessage);
//			OutputStream outputStream = response.getOutputStream();
//			outputStream.write(errorMessage.getBytes(Charset.forName("UTF-8")));
//			outputStream.close();
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}

		String mimeType = URLConnection.guessContentTypeFromName(file.getName());
		if (mimeType == null) {
			System.out.println("mimetype is not detectable, will take default");
			mimeType = "application/octet-stream";
		}

		System.out.println("mimetype : " + mimeType);

		response.setContentType(mimeType);

		response.setHeader("Content-Disposition", String.format("inline; filename=\"" + file.getName() + "\""));

		response.setContentLength((int) file.length());

		InputStream inputStream = new BufferedInputStream(new FileInputStream(file));

		FileCopyUtils.copy(inputStream, response.getOutputStream());
	}

}
