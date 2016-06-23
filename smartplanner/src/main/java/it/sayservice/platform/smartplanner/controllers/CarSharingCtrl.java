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
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.swagger.annotations.ApiParam;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.mongo.repos.CarStationRepository;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

@Controller
public class CarSharingCtrl {

	/** station repository manager. **/
	@Autowired
	private RepositoryUtils repositoryUtils;
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private MongoRouterMapper mongoRouterMapper;
	private MongoTemplate template;

	// CAR SHARING
	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/carsharing")
	public @ResponseBody void deleteCarSharing(@PathVariable String router) throws SmartPlannerException {

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (carStationRepository != null && template != null) {

			carStationRepository.deleteByType("CAR-RENTAL");
			repositoryUtils.buildCarSharingRepo(configurationManager.getRouter(router), carStationRepository);
			template.remove(new Query(), Constants.ALERT_CAR_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/carsharing/{agencyId}")
	public @ResponseBody void deleteCarSharing(@PathVariable String agencyId, @PathVariable String router)
			throws SmartPlannerException {

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (carStationRepository != null && template != null) {
			carStationRepository.delete(carStationRepository.findByAgencyIdAndType(agencyId, "CAR-RENTAL"));
			repositoryUtils.buildCarSharingRepo(configurationManager.getRouter(router), carStationRepository);
			template.remove(new Query(), Constants.ALERT_CAR_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/carsharing")
	public @ResponseBody void updateCarSharing(@PathVariable String router, @RequestBody List<CarStation> lcs) {

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (carStationRepository != null && template != null) {
			for (CarStation cs : lcs) {
				cs.setId(cs.getStationId().getId() + "@" + cs.getStationId().getAgencyId());
				carStationRepository.save(cs);
			}
			template.remove(new Query(), Constants.ALERT_CAR_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/carStationsNearPoint")
	public @ResponseBody List<Position> carStationsNearPoint(@PathVariable String router,
			@RequestParam("lat") Double lat, @RequestParam("lon") Double lon,
			@ApiParam(value = "radius in meters") @RequestParam("radius") Double radius) throws SmartPlannerException {

		List<Position> carPositions = new ArrayList<Position>();
		try {
			CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);
			if (carStationRepository != null) {
				// validate.
				if (!lat.isNaN() && !lon.isNaN() && !radius.isNaN()) {
					Point p = new Point(lat, lon);
					Distance d = new Distance(radius / 1000, Metrics.KILOMETERS);

					// flow.
					List<CarStation> cStations = carStationRepository.findByLocationNear(p, d);
					for (CarStation c : cStations) {
						Position temp = new Position();
						temp.setName(c.getId());
						temp.setStopId(c.getStationId());
						temp.setStopCode(c.getType());
						temp.setLat(String.valueOf(c.getPosition()[0]));
						temp.setLon(String.valueOf(c.getPosition()[1]));
						carPositions.add(temp);
					}
				} else {
					throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "invalid input");
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "repository is null");
			}

		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return carPositions;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getCarStations")
	public @ResponseBody List<CarStation> getCarStations(@PathVariable String router) {

		List<CarStation> carStations = new ArrayList<CarStation>();

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (carStationRepository != null) {
			carStations = carStationRepository.findAll();
		}

		return carStations;

	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
