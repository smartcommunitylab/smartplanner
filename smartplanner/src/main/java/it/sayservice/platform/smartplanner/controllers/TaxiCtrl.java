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

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiParam;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.Contact;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.model.TaxiStation;
import it.sayservice.platform.smartplanner.mongo.repos.TaxiStationRepository;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

@Controller
public class TaxiCtrl {

	@Autowired
	private MongoRouterMapper routersMap;
	@Autowired
	private RepositoryUtils repositoryUtils;

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/taxis")
	public @ResponseBody List<TaxiStation> getTaxiStation(@PathVariable String router) {

		List<TaxiStation> taxiStations = new ArrayList<TaxiStation>();

		TaxiStationRepository taxiStationRepository = routersMap.getTaxiStationRepo(router);

		if (taxiStationRepository != null) {
			taxiStations = taxiStationRepository.findAll();
		}

		return taxiStations;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/taxis/{agencyId}")
	public @ResponseBody List<TaxiStation> getTaxiStationsByAgency(@PathVariable String router,
			@PathVariable String agencyId) {
		List<TaxiStation> taxiStations = new ArrayList<TaxiStation>();

		TaxiStationRepository taxiStationRepository = routersMap.getTaxiStationRepo(router);

		if (taxiStationRepository != null) {
			for (TaxiStation taxiStation : taxiStationRepository.findAll()) {
				if (taxiStation.getStationId().getAgencyId().equalsIgnoreCase(agencyId)) {
					taxiStations.add(taxiStation);
				}
			}
		}

		return taxiStations;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/taxisNearPoint")
	public @ResponseBody List<TaxiStation> getTaxiStationsNearPoint(@PathVariable String router,
			@RequestParam("lat") Double lat, @RequestParam("lon") Double lon,
			@ApiParam(value = "radius in meters") @RequestParam("radius") Double radius) {

		List<TaxiStation> taxiStations = new ArrayList<TaxiStation>();

		TaxiStationRepository taxiStationRepository = routersMap.getTaxiStationRepo(router);

		if (taxiStationRepository != null) {
			if (!lat.isNaN() && !lon.isNaN() && !radius.isNaN()) {
				Point p = new Point(lat, lon);
				Distance d = new Distance(radius / 1000, Metrics.KILOMETERS);
				taxiStations.addAll(taxiStationRepository.findByLocationNear(p, d));

			}
		}

		return taxiStations;

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/taxi/station")
	public @ResponseBody String addTaxiStation(@PathVariable String router, @RequestBody List<TaxiStation> taxiStations)
			throws SmartPlannerException {
		String result = null;

		try {
			TaxiStationRepository taxiStationRepository = routersMap.getTaxiStationRepo(router);

			if (taxiStationRepository != null) {

				for (TaxiStation taxiStation : taxiStations) {
					taxiStationRepository.save(taxiStation);
				}

				result = "taxi stations added successfully";
			}
		} catch (Exception e) {
			throw new SmartPlannerException(e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/taxi/contacts")
	public @ResponseBody List<Contact> getAllTaxiAgencyContacts(@PathVariable String router) throws SmartPlannerException {

		List<Contact> result = new ArrayList<Contact>();

		try {
			result = repositoryUtils.getTaxiAgencyContacts(router);

		} catch (Exception e) {
			throw new SmartPlannerException(e.getMessage());
		}

		return result;

	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/taxi/contacts/{agencyId}")
	public @ResponseBody List<Contact> getTaxiAgencyContacts(@PathVariable String router, @PathVariable String agencyId) throws SmartPlannerException {

		List<Contact> contacts = new ArrayList<Contact>();

		try {
			List<Contact> temp = repositoryUtils.getTaxiAgencyContacts(router);

			for (Contact contact : temp) {
				if (contact.getAgencyId().equalsIgnoreCase(agencyId)) {
					contacts.add(contact);
				}
			}

		} catch (Exception e) {
			throw new SmartPlannerException(e.getMessage());
		}

		return contacts;

	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
