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

import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.DynamicCarStation;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.mongo.repos.CarStationRepository;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
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

import com.mongodb.QueryBuilder;

@Controller
public class CarParkingCtrl {

	@Autowired
	private RepositoryUtils repositoryUtils;
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private MongoRouterMapper mongoRouterMapper;
	private MongoTemplate template;
	private static final long PARKING_DATA_VALIDITY = 1000 * 60 * 60 * 24;

	// CAR PARKING
	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/carparking")
	public @ResponseBody void deleteParkings(@PathVariable String router) throws SmartPlannerException {

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (carStationRepository != null && template != null) {
			carStationRepository.deleteByType("CAR-PARKING");
			repositoryUtils.buildCarParkingRepo(configurationManager.getRouter(router), carStationRepository);
			template.remove(new Query(), Constants.ALERT_CAR_REPO);
		} else {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "template|repository is null.");
		}

	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/carparking/{agencyId}")
	public @ResponseBody void deleteParkings(@PathVariable String agencyId, @PathVariable String router)
			throws SmartPlannerException {

		CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (carStationRepository != null && template != null) {
			carStationRepository.delete(carStationRepository.findByAgencyIdAndType(agencyId, "CAR-PARKING"));
			repositoryUtils.buildCarParkingRepo(configurationManager.getRouter(router), carStationRepository);
			template.remove(new Query(), Constants.ALERT_CAR_REPO);
		}

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/carparking")
	public @ResponseBody void updateParkings(@PathVariable String router, @RequestBody List<CarStation> lcs) {

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

	/**
	 * Get Parkings.
	 * @param router
	 * @return
	 * @throws SmartPlannerException 
	 */
	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getParkings")
	public @ResponseBody List<Parking> getParkings(@PathVariable String router) throws SmartPlannerException {

		List<Parking> result = new ArrayList<Parking>();

		try {

			CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);// Repository(CarStationRepository.class);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (carStationRepository != null && template != null) {
				List<CarStation> stations = carStationRepository.findAll();

				for (CarStation station : stations) {
					if (!"CAR-PARKING".equals(station.getType())) {
						continue;
					}
					QueryBuilder qb = new QueryBuilder();
					qb.start("_id").is(station.getId());
					Criteria criteria = new Criteria("_id").is(station.getId());
					Query query = new Query(criteria);
					DynamicCarStation alert = template.findOne(query, DynamicCarStation.class,
							Constants.ALERT_CAR_REPO);
					int places = -1;
					if (!station.isMonitored()) {
						places = -2;
					} else if (alert != null) {
						places = alert.getPosts();
						if (places > station.getPosts()
								|| (System.currentTimeMillis() - alert.getDuration() > PARKING_DATA_VALIDITY)) {
							places = -1;
						}
					}
					Parking parking = new Parking();
//					parking.setName(station.getStationId().getId());
					parking.setName(station.getCarStationName());
					parking.setDescription(station.getFullName());
					parking.setSlotsAvailable(places);
					parking.setSlotsTotal(station.getPosts());
					parking.setPosition(station.getPosition());
					parking.setMonitored(station.isMonitored());
					parking.getExtra().put("parkAndRide", station.isParkAndRide());
					result.add(parking);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getParkingsByAgency")
	public @ResponseBody List<Parking> getParkingsByAgency(@PathVariable String router,
			@RequestParam("agencyId") String agencyId) throws SmartPlannerException {

		List<Parking> result = new ArrayList<Parking>();

		try {

			CarStationRepository carStationRepository = mongoRouterMapper.getCarStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (carStationRepository != null && template != null) {

				List<CarStation> stations = carStationRepository.findAll();

				for (CarStation station : stations) {
					if (!"CAR-PARKING".equals(station.getType())) {
						continue;
					}
					if (!station.getStationId().getAgencyId().equals(agencyId)) {
						continue;
					}

					QueryBuilder qb = new QueryBuilder();
					qb.start("_id").is(station.getId());
					Criteria criteria = new Criteria("_id").is(station.getId());
					Query query = new Query(criteria);
					DynamicCarStation alert = template.findOne(query, DynamicCarStation.class,
							Constants.ALERT_CAR_REPO);

					int places = -1;
					if (!station.isMonitored()) {
						places = -2;
					} else if (alert != null) {
						places = alert.getPosts();
						if (places > station.getPosts()
								|| (System.currentTimeMillis() - alert.getDuration() > PARKING_DATA_VALIDITY)) {
							places = -1;
						}
					}

					Parking parking = new Parking();
//					parking.setName(station.getStationId().getId());
					parking.setName(station.getCarStationName());
					parking.setDescription(station.getFullName());
					parking.setSlotsAvailable(places);
					parking.setSlotsTotal(station.getPosts());
					parking.setPosition(station.getPosition());
					parking.setMonitored(station.isMonitored());
					parking.getExtra().put("parkAndRide", station.isParkAndRide());
					result.add(parking);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
