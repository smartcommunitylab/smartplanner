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
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.core.adapter.OpenToProprietaryBikeStationAdapter;
import it.sayservice.platform.smartplanner.core.adapter.ProprietaryToOpenBikeStationAdapter;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.DynamicBikeStation;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.model.bikerental.CityBikesBikeStation;
import it.sayservice.platform.smartplanner.model.bikerental.JCDecauxBikeStation;
import it.sayservice.platform.smartplanner.mongo.repos.BikeStationRepository;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
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
public class BikeSharingCtrl {

	/** station repository manager. **/
	@Autowired
	private RepositoryUtils repositoryUtils;
	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private MongoRouterMapper mongoRouterMapper;
	private MongoTemplate template;
	private static final long PARKING_DATA_VALIDITY = 1000 * 60 * 60 * 24;

	// BIKE SHARING
	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/bikesharing")
	public @ResponseBody
	void deleteBikeSharing(@PathVariable String router) throws SmartPlannerException {

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (bikeStationRepository != null && template != null) {
			bikeStationRepository.deleteAll();
			repositoryUtils.buildBikeStationRepo(configurationManager.getRouter(router), bikeStationRepository);
			template.remove(new Query(), Constants.ALERT_BIKE_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/bikesharing/{agencyId}")
	public @ResponseBody
	void deleteBikeSharing(@PathVariable String agencyId, @PathVariable String router) throws SmartPlannerException {

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (bikeStationRepository != null && template != null) {
			bikeStationRepository.delete(bikeStationRepository.findByAgencyId(agencyId));
			repositoryUtils.buildBikeStationRepo(configurationManager.getRouter(router), bikeStationRepository);
			template.remove(new Query(), Constants.ALERT_BIKE_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/bikesharing")
	public @ResponseBody
	void addBikeSharing(@PathVariable String router, @ApiParam(value = "List of bike stations") @RequestBody List<BikeStation> lbs) {

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (bikeStationRepository != null && template != null) {
			for (BikeStation bs : lbs) {
				bs.setId(bs.getStationId().getId() + "@" + bs.getStationId().getAgencyId());
				bikeStationRepository.save(bs);
			}
//			template.remove(new Query(), Constants.ALERT_BIKE_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/bikesharing/JCDecaux")
	public @ResponseBody
	void updateBikeSharingJCDecaux(@PathVariable String router, @ApiParam(value = "List of bike stations in JCDecaux format")  @RequestBody List<JCDecauxBikeStation> jCDecauxbikeStationList) {

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (bikeStationRepository != null && template != null) {
			for (JCDecauxBikeStation jCDecauxBikeStation : jCDecauxbikeStationList) {
				BikeStation bikeStation = OpenToProprietaryBikeStationAdapter.getBikeStationFromJCDecaux(jCDecauxBikeStation);
				bikeStation.setId(bikeStation.getStationId().getId() + "@" + bikeStation.getStationId().getAgencyId());
				bikeStationRepository.save(bikeStation);
			}

			template.remove(new Query(), Constants.ALERT_BIKE_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/bikesharing/CityBikes")
	public @ResponseBody
	void updateBikeSharingCityBikes(@PathVariable String router, @ApiParam(value = "List of bike stations in City Bikes format")  @RequestBody List<CityBikesBikeStation> cityBikesStationList) {

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
			template = mongoRouterMapper.getMongoTemplateMap().get(router);
		}

		if (bikeStationRepository != null && template != null) {
			for (CityBikesBikeStation cityBikesBikeStation : cityBikesStationList) {
				BikeStation bikeStation = OpenToProprietaryBikeStationAdapter.getBikeStationFromCityBikes(cityBikesBikeStation);
				bikeStation.setId(bikeStation.getStationId().getId() + "@" + bikeStation.getStationId().getAgencyId());
				bikeStationRepository.save(bikeStation);
			}

			template.remove(new Query(), Constants.ALERT_BIKE_REPO);
		}
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharing")
	public @ResponseBody
	List<Parking> getBikeSharing(@PathVariable String router) throws SmartPlannerException {

		List<Parking> result = new ArrayList<Parking>();

		try {
			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {
				List<BikeStation> stations = bikeStationRepository.findAll();
				for (BikeStation station : stations) {
					QueryBuilder qb = new QueryBuilder();
					qb.start("_id").is(station.getId());
					Criteria criteria = new Criteria("_id").is(station.getId());
					Query query = new Query(criteria);
					DynamicBikeStation alert = template.findOne(query, DynamicBikeStation.class, Constants.ALERT_BIKE_REPO);
					if (alert != null) {
						int places = -1;
						if (!station.isMonitored()) {
							places = -2;
						} else if (alert != null) {
							places = alert.getPosts();
							if (places > station.getPosts() || (System.currentTimeMillis() - alert.getDuration() > PARKING_DATA_VALIDITY)) {
								places = -1;
							}
						}

						Parking parking = new Parking();
//						parking.setName(station.getStationId().getId());
						parking.setName(station.getBikeStationName());
						parking.setDescription(station.getFullName());
						// parking.setSlotsAvailable(alert.getPosts());
						parking.setSlotsAvailable(places);
						parking.setSlotsTotal(station.getPosts());
						parking.setPosition(station.getPosition());
						parking.setMonitored(station.isMonitored());

						parking.getExtra().put("bikes", alert.getBikes());

						result.add(parking);
					}
				}

			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}

		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharing/JCDecaux")
	public @ResponseBody
	List<JCDecauxBikeStation> getBikeSharingJCDecaux(@PathVariable String router) throws SmartPlannerException {

		List<JCDecauxBikeStation> result = new ArrayList<JCDecauxBikeStation>();

		try {
			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {
				List<BikeStation> stations = bikeStationRepository.findAll();
				for (BikeStation bikeStation : stations) {
					Parking parking = getParkingFromBikeStation(bikeStation, null);

					JCDecauxBikeStation jCDecauxBikeStation = ProprietaryToOpenBikeStationAdapter.getJCDecauxFromParking(parking, bikeStation);
					
					result.add(jCDecauxBikeStation);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharing/CityBikes")
	public @ResponseBody
	List<CityBikesBikeStation> getBikeSharingCityBikes(@PathVariable String router) throws SmartPlannerException {

		List<CityBikesBikeStation> result = new ArrayList<CityBikesBikeStation>();

		try {
			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {
				List<BikeStation> stations = bikeStationRepository.findAll();
				for (BikeStation bikeStation : stations) {
					Parking parking = getParkingFromBikeStation(bikeStation, null);

					CityBikesBikeStation cityBikesBikeStation = ProprietaryToOpenBikeStationAdapter.getCityBikesFromParking(parking, bikeStation);
					
					result.add(cityBikesBikeStation);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/bikeStationsNearPoint")
	public @ResponseBody
	List<Position> bikeStationsNearPoint(@PathVariable String router, @RequestParam("lat") Double lat, @RequestParam("lon") Double lon, @ApiParam(value = "radius in meters") @RequestParam("radius") Double radius) throws SmartPlannerException {

		List<Position> bikePositions = new ArrayList<Position>();

		try {

			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);// Repository(CarStationRepository.class);

			// validate.
			if (!lat.isNaN() && !lon.isNaN() && !radius.isNaN()) {
				Point p = new Point(lat, lon);
				Distance d = new Distance(radius / 1000, Metrics.KILOMETERS);
				// flow.
				List<BikeStation> bStations = bikeStationRepository.findByLocationNear(p, d);
				for (BikeStation b : bStations) {
					Position temp = new Position();
					temp.setName(b.getId());
					temp.setStopId(b.getStationId());
					temp.setStopCode(b.getType());
					temp.setLat(String.valueOf(b.getPosition()[0]));
					temp.setLon(String.valueOf(b.getPosition()[1]));
					bikePositions.add(temp);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "invalid input");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return bikePositions;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharingByAgency")
	public @ResponseBody
	List<Parking> getBikeSharingByAgency(@PathVariable String router, @RequestParam("agencyId") String agencyId) throws SmartPlannerException {

		List<Parking> result = new ArrayList<Parking>();

		try {

			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {

				List<BikeStation> stations = bikeStationRepository.findAll();
				for (BikeStation bikeStation : stations) {
					Parking parking = getParkingFromBikeStation(bikeStation, agencyId);

					if(parking!=null){
						result.add(parking);
					}
				}

			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharingByAgency/JCDecaux")
	public @ResponseBody
	List<JCDecauxBikeStation> getBikeSharingByAgencyJCDecaux(@PathVariable String router, @RequestParam("agencyId") String agencyId) throws SmartPlannerException {

		List<JCDecauxBikeStation> result = new ArrayList<JCDecauxBikeStation>();

		try {

			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {

				List<BikeStation> bikeStationList = bikeStationRepository.findAll();
				for (BikeStation bikeStation : bikeStationList) {
					Parking parking = getParkingFromBikeStation(bikeStation, agencyId);

					JCDecauxBikeStation jCDecauxBikeStation = ProprietaryToOpenBikeStationAdapter.getJCDecauxFromParking(parking, bikeStation);
					result.add(jCDecauxBikeStation);
				}

			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeSharingByAgency/CityBikes")
	public @ResponseBody
	List<CityBikesBikeStation> getBikeSharingByAgencyCityBikes(@PathVariable String router, @RequestParam("agencyId") String agencyId) throws SmartPlannerException {

		List<CityBikesBikeStation> result = new ArrayList<CityBikesBikeStation>();

		try {

			BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

			if (mongoRouterMapper.getMongoTemplateMap().containsKey(router)) {
				template = mongoRouterMapper.getMongoTemplateMap().get(router);
			}

			if (bikeStationRepository != null && template != null) {

				List<BikeStation> bikeStationList = bikeStationRepository.findAll();
				for (BikeStation bikeStation : bikeStationList) {
					Parking parking = getParkingFromBikeStation(bikeStation, agencyId);

					CityBikesBikeStation cityBikesBikeStation = ProprietaryToOpenBikeStationAdapter.getCityBikesFromParking(parking, bikeStation);
					result.add(cityBikesBikeStation);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router|template null");
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return result;
	}

	private Parking getParkingFromBikeStation(BikeStation bikeStation, String agencyId) {

		Parking parking = null;

		if (agencyId!=null && !bikeStation.getStationId().getAgencyId().equals(agencyId)) {
			return null;
		}

		QueryBuilder qb = new QueryBuilder();
		qb.start("_id").is(bikeStation.getId());
		Criteria criteria = new Criteria("_id").is(bikeStation.getId());
		Query query = new Query(criteria);
		DynamicBikeStation alert = template.findOne(query, DynamicBikeStation.class, Constants.ALERT_BIKE_REPO);

		if (alert != null) {

			int places = -1;
			if (!bikeStation.isMonitored()) {
				places = -2;
			} else if (alert != null) {
				places = alert.getPosts();
				if (places > bikeStation.getPosts() || (System.currentTimeMillis() - alert.getDuration() > PARKING_DATA_VALIDITY)) {
					places = -1;
				}
			}

			parking = new Parking();
			
//			parking.setName(bikeStation.getStationId().getId());
			parking.setName(bikeStation.getBikeStationName());
			parking.setDescription(bikeStation.getFullName());
			parking.setSlotsAvailable(places);
			parking.setSlotsTotal(bikeStation.getPosts());
			parking.setPosition(bikeStation.getPosition());
			parking.setMonitored(bikeStation.isMonitored());

			parking.getExtra().put("bikes", alert.getBikes());
		}

		return parking;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeStations/JCDecaux")
	public @ResponseBody
	List<JCDecauxBikeStation> getBikeStationsJCDecaux(@PathVariable String router) {

		List<JCDecauxBikeStation> jCDecauxbikeStationList = new ArrayList<JCDecauxBikeStation>();

		List<BikeStation> bikeStationList = getBikeStationList(router);

		for (BikeStation bikeStation : bikeStationList) {
			JCDecauxBikeStation jCDecauxBikeStation = ProprietaryToOpenBikeStationAdapter.getJCDecauxFromBikestation(bikeStation);
			jCDecauxbikeStationList.add(jCDecauxBikeStation);
		}

		return jCDecauxbikeStationList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeStations/CityBikes")
	public @ResponseBody
	List<CityBikesBikeStation> getBikeStationsCityBikes(@PathVariable String router) {

		List<CityBikesBikeStation> cityBikesBikeStationStationList = new ArrayList<CityBikesBikeStation>();

		List<BikeStation> bikeStationList = getBikeStationList(router);

		for (BikeStation bikeStation : bikeStationList) {
			CityBikesBikeStation cityBikesBikeStation = ProprietaryToOpenBikeStationAdapter.getCityBikesFromBikestation(bikeStation);
			cityBikesBikeStationStationList.add(cityBikesBikeStation);
		}

		return cityBikesBikeStationStationList;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeStations")
	public @ResponseBody
	List<BikeStation> getBikeStations(@PathVariable String router) {

		List<BikeStation> bikeStationList = getBikeStationList(router);

		return bikeStationList;
	}

	private List<BikeStation> getBikeStationList(String router) {
		List<BikeStation> bikeStationList = new ArrayList<BikeStation>();

		BikeStationRepository bikeStationRepository = mongoRouterMapper.getBikeStationRepository(router);

		if (bikeStationRepository != null) {
			bikeStationList = bikeStationRepository.findAll();
		}

		return bikeStationList;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody
	Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody() : new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
