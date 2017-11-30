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

package it.sayservice.platform.smartplanner.utils;

import it.sayservice.platform.smartplanner.areainfo.AreaInfoLoader;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.controllers.ReatTimeFeedCtrl;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertParking;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.Contact;
import it.sayservice.platform.smartplanner.model.DynamicBikeStation;
import it.sayservice.platform.smartplanner.model.DynamicCarStation;
import it.sayservice.platform.smartplanner.model.StreetLocation;
import it.sayservice.platform.smartplanner.model.TaxiStation;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.mongo.repos.BikeStationRepository;
import it.sayservice.platform.smartplanner.mongo.repos.CarStationRepository;
import it.sayservice.platform.smartplanner.mongo.repos.TaxiStationRepository;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RepositoryUtils {

	/** mongo template **/
	private MongoTemplate template;
	@Autowired
	private MongoRouterMapper routersMap;
	@Autowired
	private ConfigurationManager configurationManager;
	/** object mapper. **/
	private ObjectMapper mapper = new ObjectMapper();
	private static final transient Logger logger = LoggerFactory.getLogger(RepositoryUtils.class);

	private void buildAreaPointRepo(RouterConfig routerConfig, AreaPointRepository areaPointRepository)
			throws SmartPlannerException {
		try {
			new AreaInfoLoader(areaPointRepository).loadData(routerConfig);
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "areainfo repository is null.");
		}

	}

	private void buildTaxiRepo(RouterConfig routerConfig, TaxiStationRepository taxiStationRepository)
			throws SmartPlannerException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (routerConfig.getTaxi() != null && !routerConfig.getTaxi().isEmpty()) {

				for (String key : routerConfig.getTaxi().keySet()) {

					Agency agency = routerConfig.getTaxi().get(key);
					File jsonFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator")
							+ routerConfig.getRouter() + agency.getFilePath());
					if (jsonFile != null) {
						System.out.println(
								"------------ LOAD TAXI REPOSITORY - " + agency.getRegion() + " ---------------");
					}
					List list = mapper.readValue(jsonFile, List.class);
					for (Object obj : list) {
						TaxiStation taxiStation = mapper.convertValue(obj, TaxiStation.class);
						String id = taxiStation.getStationId().getId() + "@" + taxiStation.getStationId().getAgencyId();
						taxiStation.setId(id);
						if (taxiStationRepository.findOne(id) == null) {
							taxiStationRepository.save(taxiStation);
						}
						System.out.println("saved taxi station:" + taxiStation);
					}
				}
			}
		} catch (Exception e) {
			throw new SmartPlannerException(e.getMessage());
		}

	}

	public void buildBikeStationRepo(RouterConfig routerConfig, BikeStationRepository bikeStationRepository)
			throws SmartPlannerException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (routerConfig.getBikeSharing() != null && !routerConfig.getBikeSharing().isEmpty()) {
				for (String key : routerConfig.getBikeSharing().keySet()) {
					Agency agency = routerConfig.getBikeSharing().get(key);
					File jsonFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator")
							+ routerConfig.getRouter() + agency.getFilePath());
					if (jsonFile != null) {
						System.out.println("------------ LOAD BIKE SHARING REPOSITORY - " + agency.getRegion()
								+ " ---------------");
						List list = mapper.readValue(jsonFile, List.class);
						for (Object obj : list) {
							BikeStation bs = mapper.convertValue(obj, BikeStation.class);
							String id = bs.getStationId().getId() + "@" + bs.getStationId().getAgencyId();
							bs.setId(id);
							if (bikeStationRepository.findOne(id) == null) {
								bikeStationRepository.save(bs);
							}
							System.out.println("saved car sharing station:" + bs);
						}
					}
				}
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

	}

	public void buildCarParkingRepo(RouterConfig routerConfig, CarStationRepository carStationRepository)
			throws SmartPlannerException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (routerConfig.getCarParking() != null && !routerConfig.getCarParking().isEmpty()) {
				for (String key : routerConfig.getCarParking().keySet()) {
					Agency agency = routerConfig.getCarParking().get(key);
					File jsonFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator")
							+ routerConfig.getRouter() + agency.getFilePath());
					if (jsonFile != null) {
						System.out.println("------------ LOAD CAR PARKING REPOSITORY - " + agency.getRegion()
								+ " ---------------");
					}
					List list = mapper.readValue(jsonFile, List.class);
					for (Object obj : list) {
						CarStation cs = mapper.convertValue(obj, CarStation.class);
						String id = cs.getStationId().getId() + "@" + cs.getStationId().getAgencyId();
						cs.setId(id);
						if (carStationRepository.findOne(id) == null) {
							carStationRepository.save(cs);
						}
						System.out.println("saved car parking station:" + cs);
					}
				}
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

	}

	public void buildCarSharingRepo(RouterConfig routerConfig, CarStationRepository carStationRepository)
			throws SmartPlannerException {
		try {
			ObjectMapper mapper = new ObjectMapper();
			if (routerConfig.getCarSharing() != null && !routerConfig.getCarSharing().isEmpty()) {

				for (String key : routerConfig.getCarSharing().keySet()) {

					Agency agency = routerConfig.getCarSharing().get(key);
					File jsonFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator")
							+ routerConfig.getRouter() + agency.getFilePath());
					if (jsonFile != null) {
						System.out.println("------------ LOAD CAR SHARING REPOSITORY - " + agency.getRegion()
								+ " ---------------");
					}
					List list = mapper.readValue(jsonFile, List.class);
					for (Object obj : list) {
						CarStation cs = mapper.convertValue(obj, CarStation.class);
						String id = cs.getStationId().getId() + "@" + cs.getStationId().getAgencyId();
						cs.setId(id);
						if (carStationRepository.findOne(id) == null) {
							carStationRepository.save(cs);
						}
						System.out.println("saved car sharing station:" + cs);
					}
				}
			}
		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

	}

	public void clean() {
		// TODO Auto-generated method stub

	}

	/**
	 * check existence of location in street collection.
	 * @param lat
	 * @param lon
	 * @return
	 */
	public final StreetLocation existStreetLocation(String router, final double lat, final double lon) {
		StreetLocation existingLocation = null;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {

			List<StreetLocation> existLoclist = template.find(
					new Query(Criteria.where("lat").is(lat).and("lon").is(lon)), StreetLocation.class,
					"streetLocation");

			if (!existLoclist.isEmpty()) {
				existingLocation = existLoclist.get(0);
			}
		}
		return existingLocation;
	}

	/**
	 * Find DynamicCarStation within dynamic collection.
	 * @param id String.
	 * @return DynamicCarStation
	 */
	public DynamicCarStation findDynamicCarStation(String router, String id) {

		DynamicCarStation ds = null;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find
			ds = template.findOne(new Query(Criteria.where("id").is(id)), DynamicCarStation.class,
					Constants.ALERT_CAR_REPO);
		}
		return ds;
	}

	/**
	 * Find DynamicBikeStation within dynamic collection.
	 * @param id String
	 * @return DynamicBikeStation
	 */
	public DynamicBikeStation findDynamicBikeStation(String router, String id) {

		DynamicBikeStation ds = null;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find
			ds = template.findOne(new Query(Criteria.where("id").is(id)), DynamicBikeStation.class,
					Constants.ALERT_BIKE_REPO);
		}
		return ds;
	}

	public List<String> findMatchingTrainTripIds(String router, String tripId, boolean user) {
		List<String> ids = new ArrayList<String>();
		String expr;
		List<String> routes = configurationManager.getRouter(router).getGtfsTrainRouteIds(); // Arrays.asList(Constants.GTFS_TRAIN_ROUTE_IDS);
		List<Map> trips;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			if (user) {
				expr = tripId;
				trips = template.find(new Query(Criteria.where("tripId").is(expr).and("routeId").in(routes)), Map.class,
						"trips");
			} else {
//				expr = "^[\\D]*" + tripId.replaceAll("\\D*", "") + "\\$";
//				expr = "^[\\D]?" + tripId.replaceAll("[\\\\w]*", "").replace("$", "\\$") + "$";				
				expr = "^[\\D]*" + tripId.replaceAll("\\D*", "") + "\\$?[\\d]*$";

				trips = template.find(new Query(Criteria.where("tripId").regex(expr).and("routeId").in(routes)),
						Map.class, "trips");
				//				template.findAll(Map.class,"trips");
			}

			for (Map map : trips) {
				String tid = (String) map.get("tripId");
				ids.add(tid);
			}
		}

		return ids;
	}

	/**
	 * find car rental station from repo near to point.
	 * @param p Point
	 * @return List<CarStation>
	 */
	public List<CarStation> findCarRentalStationPositionByNear(String router, Point p, boolean searchRental,
			Map<String, Object> userRequest) {

		/** temporary list. **/
		List<CarStation> copiedList;
		List<CarStation> copiedAList;
		List<CarStation> locations = new ArrayList<CarStation>();

		CarStationRepository carStationRepository = routersMap.getCarStationRepository(router);

		if (carStationRepository != null) {

			locations = carStationRepository.findByLocationNear(p, new Distance(5, Metrics.KILOMETERS));
			// get only car-rental stations
			// make a copy to avoid concurrent modification exception.
			copiedAList = new ArrayList<CarStation>(locations);
			for (CarStation cs : copiedAList) {
				if (!cs.getType().equalsIgnoreCase("CAR-RENTAL")) {
					locations.remove(cs);
				} else {
					/** check for valid ones **/
					// make a copy to avoid concurrent modification exception.
					copiedList = new ArrayList<CarStation>(locations);
					// iterate over list and delete not valid ones from original.
					for (CarStation temp : copiedList) {
						if (!isValidStation(router, temp, searchRental, userRequest)) {
							locations.remove(temp);
						}
					}
					// clear after temporary check.
					copiedList.clear();
				}
			}
		}

		return locations;
	}

	/**
	 * Find car station near to point.
	 * @param p
	 * @param userRequest
	 * @param parkAndRideOnly
	 * @return
	 */
	public List<CarStation> findCarStationPositionByNear(String router, Point p, Map<String, Object> userRequest,
			boolean parkAndRideOnly) {
		List<CarStation> copiedList;
		List<CarStation> locations = new ArrayList<CarStation>();

		CarStationRepository carStationRepository = routersMap.getCarStationRepository(router);

		if (carStationRepository != null) {
			locations = carStationRepository.findByLocationNear(p, new Distance(5, Metrics.KILOMETERS));

			/** check for valid ones **/
			// make a copy to avoid concurrent modification exception.
			copiedList = new ArrayList<CarStation>(locations);
			// iterate over list and delete not valid ones from original.
			for (CarStation cs : copiedList) {
				if (cs.getType().equalsIgnoreCase("CAR-RENTAL")) {
					locations.remove(cs);
				} else {
					/** check for valid ones **/
					// make a copy to avoid concurrent modification exception.
					copiedList = new ArrayList<CarStation>(locations);
					// iterate over list and delete not valid ones from original.
					for (CarStation temp : copiedList) {
						if (!isValidStation(router, temp, false, userRequest)) {
							locations.remove(temp);
						} else if (parkAndRideOnly && !temp.isParkAndRide()) {
							locations.remove(temp);
						}
					}
					// clear after temporary check.
					copiedList.clear();
				}
			}
		}

		return locations;
	}

	public void init(RouterConfig routerConfig) throws SmartPlannerException {

		if (routerConfig != null) {

			CarStationRepository carStationRepository = routersMap.getCarStationRepository(routerConfig.getRouter());
			BikeStationRepository bikeStationRepository = routersMap.getBikeStationRepository(routerConfig.getRouter());
			AreaPointRepository areaPointRepository = routersMap.getAreaPointRepository(routerConfig.getRouter());
			TaxiStationRepository taxiStationRepository = routersMap.getTaxiStationRepo(routerConfig.getRouter());

			System.out.println("Initializing RepositoryManager for router: " + routerConfig.getRouter());

			// initialize repositories.
			if (bikeStationRepository != null) {
				if (bikeStationRepository.count() == 0) {
					buildBikeStationRepo(routerConfig, bikeStationRepository);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"bikeStation repository is null.");
			}

			if (carStationRepository != null) {
				if (carStationRepository.findByType(Constants.CAR_PARKING).size() == 0) {
					buildCarParkingRepo(routerConfig, carStationRepository);
				}

				if (carStationRepository.findByType(Constants.CAR_RENTAL).size() == 0) {
					buildCarSharingRepo(routerConfig, carStationRepository);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"carStation repository is null.");
			}

			if (areaPointRepository != null) {
				if (areaPointRepository.count() == 0) {
					buildAreaPointRepo(routerConfig, areaPointRepository);
				}
			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(),
						"areaStation repository is null.");
			}

			if (taxiStationRepository != null) {
				if (taxiStationRepository.count() == 0) {
					buildTaxiRepo(routerConfig, taxiStationRepository);
				}
			}

		} else {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "router configuration is null.");
		}

	}

	/**
	 * validate station w.r.t Alert Parking and Alert Strike.
	 * @param station Object
	 * @param searchRental Boolean
	 * @param userParams Map
	 * @return true (if valid)
	 */
	private boolean isValidStation(String router, Object station, boolean searchRental,
			Map<String, Object> userParams) {

		// definitions.
		Boolean valid = true;
		Long reqTime = new Long(0);

		// get requested date from user request.
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ITALY);
		String date = userParams.get("date") + " " + userParams.get(Constants.SP_RQ_DEPTIME);
		java.util.Date reqDate;
		try {
			reqDate = (java.util.Date) formatter.parse(date);
			reqTime = reqDate.getTime();
			Calendar calendar = Calendar.getInstance();
			calendar.setTimeInMillis(reqTime);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (station instanceof CarStation) {
			CarStation cs = (CarStation) station;
			DynamicCarStation dcs = findValidDynamicCarStation(router, cs.getId(), reqTime);
			if (dcs != null) {
				if (searchRental) {
					if (dcs.getCars() < 1) {
						valid = false;
					}
				} else { // PARKING SEARCH.
					if (dcs.getPosts() < 1) {
						valid = false;
					}
				}
			}

			// validate w.r.t Alert Strike.
			List<AlertStrike> as = queryAlertStrike(router, cs.getStationId().getId(), reqTime);
			if (as != null && !(as.isEmpty())) {
				// ignore.
				valid = false;
			}

		} else if (station instanceof BikeStation) {
			BikeStation bs = (BikeStation) station;
			DynamicBikeStation dbs = findValidDynamicBikeStation(router, bs.getId(), reqTime);
			if (dbs != null) {
				if (searchRental) {
					if (dbs.getBikes() < 1) {
						valid = false;
					}
				} else { // PARKING SEARCH
					if (dbs.getPosts() < 1) {
						valid = false;
					}
				}
			}
			// validate w.r.t Alert Strike.
			List<AlertStrike> as = queryAlertStrike(router, bs.getStationId().getId(), reqTime);
			if (as != null && !(as.isEmpty())) {
				valid = false;
			}
		}

		//
		return valid;
	}

	/**
	 * Find DynamicCarStation within dynamic collection.
	 * @param id
	 * @param reqTime
	 * @return
	 */
	public DynamicCarStation findValidDynamicCarStation(String router, String id, Long reqTime) {

		DynamicCarStation ds = null;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find
			ds = template.findOne(new Query(Criteria.where("id").is(id).and("duration").gte(reqTime)),
					DynamicCarStation.class, Constants.ALERT_CAR_REPO);
		}
		return ds;
	}

	/**
	 * Find DynamicBikeStation within dynamic collection.
	 * @param id
	 * @param reqTime
	 * @return
	 */
	public DynamicBikeStation findValidDynamicBikeStation(String router, String id, Long reqTime) {

		DynamicBikeStation ds = null;

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find
			ds = template.findOne(new Query(Criteria.where("id").is(id).and("duration").gte(reqTime)),
					DynamicBikeStation.class, Constants.ALERT_BIKE_REPO);
		}
		return ds;
	}

	//	/**
	//	 * geo-spatial search function for BikeStations.
	//	 * @param p
	//	 * @param isRental
	//	 * @param parameters
	//	 * @return
	//	 */
	//	public List<BikeStation> findBikeRentalStation(String router, Point p, boolean isRental, HashMap<String, Object> parameters) {
	//
	//		/** radius of scanned circle **/
	//		double value = 0.001;
	//		/** temporary list. **/
	//		List<BikeStation> copiedList;
	//		/** final list **/
	//		List<BikeStation> locations = new ArrayList<BikeStation>();
	//
	//		// check first within 1 KM distance.
	//		Distance dist = new Distance(1, Metrics.KILOMETERS);
	//		locations = template.find(
	//				new Query(
	//						Criteria.where("position").near(p).maxDistance(dist.getValue()).and("type").is("BIKE-RENTAL")),
	//				BikeStation.class, "bikeStation");
	//
	//		/** check for valid ones **/
	//		// make a copy to avoid concurrent modification exception.
	//		copiedList = new ArrayList<BikeStation>(locations);
	//		// iterate over list and delete not valid ones from original.
	//		for (BikeStation bs : copiedList) {
	//			if (!isValidStation(router, bs, isRental, parameters)) {
	//				locations.remove(bs);
	//			}
	//		}
	//		// clear after temporary check.
	//		copiedList.clear();
	//
	//		// nearest find within 5KM circle incrementally.
	//		while (locations.isEmpty() && value < 5) {
	//			Circle c = new Circle(p, value);
	//			locations = template.find(
	//					new Query(Criteria.where("position").withinSphere(c).and("type").is("BIKE-RENTAL")),
	//					BikeStation.class, "bikeStation");
	//			value += 0.001;
	//			/** check for valid ones **/
	//			// make a copy to avoid concurrent modification exception.
	//			copiedList = new ArrayList<BikeStation>(locations);
	//			for (BikeStation bs : copiedList) {
	//				if (!isValidStation(router, bs, isRental, parameters)) {
	//					locations.remove(bs);
	//				}
	//			}
	//			// clear after temporary check.
	//			copiedList.clear();
	//		}
	//		return locations;
	//	}

	/**
	 * find bike station from repo near to point.
	 * @param p Point
	 * @return List<BikeStation>
	 */
	public List<BikeStation> findBikeRentalStationPositionByNear(String router, Point p, Boolean searchRental,
			Map<String, Object> userRequest) {
		List<BikeStation> copiedList;
		List<BikeStation> locations = new ArrayList<BikeStation>();

		BikeStationRepository bikeStationRepository = routersMap.getBikeStationRepository(router);

		if (bikeStationRepository != null) {
			locations = bikeStationRepository.findByLocationNear(p, new Distance(5, Metrics.KILOMETERS));

			/** check for valid ones **/
			// make a copy to avoid concurrent modification exception.
			copiedList = new ArrayList<BikeStation>(locations);
			// iterate over list and delete not valid ones from original.
			for (BikeStation bs : copiedList) {
				if (!isValidStation(router, bs, searchRental, userRequest)) {
					locations.remove(bs);
				}
			}
			// clear after temporary check.
			copiedList.clear();
		}

		return locations;
	}

	/**
	 * create/update alert for car parking/rental station.
	 * @param ap AlertParking
	 * @param duration Long(life of update).
	 * @return result String
	 */
	public String updateCarStation(String router, AlertParking ap, long duration) {
		String result = "Created";

		MongoTemplate template = routersMap.getMongoTemplateMap().get(router);

		if (template != null) {
			// check if exist, update
			DynamicCarStation ds = template.findOne(
					new Query(Criteria.where("id").is(ap.getPlace().getId() + "@" + ap.getPlace().getAgencyId())),
					DynamicCarStation.class, Constants.ALERT_CAR_REPO);

			if (ds != null) {
				if (ap.getPlacesAvailable() != -1) {
					ds.setPosts(ap.getPlacesAvailable());
				}
				if (ap.getNoOfvehicles() != -1) {
					ds.setCars(ap.getNoOfvehicles());
				}
				ds.setDuration(duration);
				template.save(ds, Constants.ALERT_CAR_REPO);
				result = "Updated";
			} else { // create
				// get existing static station.
				CarStation cs = template.findOne(
						new Query(Criteria.where("id").is(ap.getPlace().getId() + "@" + ap.getPlace().getAgencyId())),
						CarStation.class, "carStation");

				if (cs != null) {
					// if both post, shared valid update
					if (ap.getNoOfvehicles() != -1 && ap.getPlacesAvailable() != -1) {
						ds = new DynamicCarStation(ap.getPlace().getAgencyId(), cs.getId(), ap.getPlacesAvailable(),
								ap.getNoOfvehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else if (ap.getNoOfvehicles() != -1) { // if only shared
																	// update.
						ds = new DynamicCarStation(ap.getPlace().getAgencyId(), cs.getId(), cs.getPosts(),
								ap.getNoOfvehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else if (ap.getPlacesAvailable() != -1) { // if only post
																	// update.
						ds = new DynamicCarStation(ap.getPlace().getAgencyId(), cs.getId(), ap.getPlacesAvailable(),
								cs.getAvailableSharingVehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else { // if both are negative.
						ds = new DynamicCarStation(ap.getPlace().getAgencyId(), cs.getId(), cs.getPosts(),
								cs.getAvailableSharingVehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					}
					template.save(ds, Constants.ALERT_CAR_REPO);
				} else {
					result = "station not present in repository.";
				}
			}
		} else {
			result = "template null";
		}

		return result;
	}

	/**
	 * create/update alert for bike rental station.
	 * @param ap AlertParking
	 * @param duration Long(life of update).
	 * @return result String
	 */
	public String updateBikeStation(String router, AlertParking ap, long duration) {

		String result = "Created";

		MongoTemplate template = routersMap.getMongoTemplateMap().get(router);

		if (template != null) {
			DynamicBikeStation ds = template.findOne(
					new Query(Criteria.where("id").is(ap.getPlace().getId() + "@" + ap.getPlace().getAgencyId())),
					DynamicBikeStation.class, Constants.ALERT_BIKE_REPO);

			if (ds != null) {
				if (ap.getPlacesAvailable() != -1) {
					ds.setPosts(ap.getPlacesAvailable());
				}
				if (ap.getNoOfvehicles() != -1) {
					ds.setBikes(ap.getNoOfvehicles());
				}
				ds.setDuration(duration);
				template.save(ds, Constants.ALERT_BIKE_REPO);
				result = "Updated";
			} else { // create
				// get existing static station.
				BikeStation bs = template.findOne(
						new Query(Criteria.where("id").is(ap.getPlace().getId() + "@" + ap.getPlace().getAgencyId())),
						BikeStation.class, "bikeStation");

				if (bs != null) {
					// if both post, bike valid update
					if (ap.getNoOfvehicles() != -1 && ap.getPlacesAvailable() != -1) {
						ds = new DynamicBikeStation(ap.getPlace().getAgencyId(), bs.getId(), ap.getPlacesAvailable(),
								ap.getNoOfvehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else if (ap.getNoOfvehicles() != -1) { // if only bikes
																	// update.
						ds = new DynamicBikeStation(ap.getPlace().getAgencyId(), bs.getId(), bs.getPosts(),
								ap.getNoOfvehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else if (ap.getPlacesAvailable() != -1) { // if only post
																	// update.
						ds = new DynamicBikeStation(ap.getPlace().getAgencyId(), bs.getId(), ap.getPlacesAvailable(),
								bs.getAvailableSharingVehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					} else { // if both are negative.
						ds = new DynamicBikeStation(ap.getPlace().getAgencyId(), bs.getId(), bs.getPosts(),
								bs.getAvailableSharingVehicles(), duration, ap.getCreatorId(), ap.getCreatorType());
					}
					template.save(ds, Constants.ALERT_BIKE_REPO);
				} else {
					result = "station not present in repository.";
				}
			}
		} else {
			result = "template null";
		}

		return result;
	}

	/**
	 * update alert strike.
	 * @param as AlertStrike
	 * @return String
	 */
	public String updateAlertStrike(String router, AlertStrike as) {
		String result = "Created";

		MongoTemplate template = routersMap.getMongoTemplateMap().get(router);

		if (template != null) {
			// find existing one.
			List<AlertStrike> alertS = template.find(new Query(Criteria.where("id").is(as.getId())), AlertStrike.class,
					Constants.ALERT_STRIKE_REPO);

			if (alertS != null && !alertS.isEmpty()) { // update.
				if (as.getFrom() != -1) {
					alertS.get(0).setFrom(as.getFrom());
				}
				if (as.getTo() != -1) {
					alertS.get(0).setTo(as.getTo());
				}
				template.save(alertS, Constants.ALERT_STRIKE_REPO);
				result = "Updated";
			} else { // create.
				// id = ${alertId}_startime_endtime.
				// were alertId a.k.a stationId or tripId.
				// as.setId(as.getId() + "_" + as.getFrom() + "_" + as.getTo());
				template.save(as, Constants.ALERT_STRIKE_REPO);
			}
		}

		return result;
	}

	/**
	 * create/update alert delay.
	 * @param ad
	 * @return
	 */
	public String updateAlertDelay(String router, AlertDelay ad) {
		String result = "Created";

		// find existing one.
		// get alertdelay, generate id =
		// Id+_creatorType+_startOfDay0400+_endofday2358
		// save it.
		// next time when you receive an alert get the trip Id
		// append _startime_endtime as above.
		// check if it exist.

		try {

			SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyhhmm", Locale.ITALY);

			Calendar calF = Calendar.getInstance();
			calF.set(Calendar.HOUR_OF_DAY, 4);
			calF.set(Calendar.MINUTE, 0);
			String from = formatter.format(calF.getTime());

			Calendar calT = Calendar.getInstance();
			calT.set(Calendar.HOUR_OF_DAY, 23);
			calT.set(Calendar.MINUTE, 58);
			String to = formatter.format(calT.getTime());
			// id.
			String id = ad.getTransport().getTripId() + "_" + ad.getCreatorType() + "_" + from + "_" + to;

			List<AlertDelay> aDelayList = template.find(new Query(Criteria.where("id").is(id)), AlertDelay.class,
					Constants.ALERT_DELAY_REPO);

			if (aDelayList != null && !aDelayList.isEmpty()) {
				AlertDelay alertD = aDelayList.get(0);
				if (ad.getFrom() != -1) {
					alertD.setFrom(ad.getFrom());
				}
				if (ad.getTo() != -1) {
					alertD.setTo(ad.getTo());
				}
				if (ad.getDelay() != -1) {
					alertD.setDelay(ad.getDelay());
				}
				logger.info("Delay Alert " + result + "for router " + router);
				logger.info("AlertDelay [id:  " + alertD.getId() + ", routeId: " + alertD.getTransport().getRouteId());
				template.save(alertD, Constants.ALERT_DELAY_REPO);
				result = "Updated";
			} else { // create.
				// Create AlertDelay.
				AlertDelay temp = new AlertDelay();
				temp.setCreatorId(ad.getCreatorId());
				temp.setCreatorType(ad.getCreatorType());
				temp.setDelay(ad.getDelay());
				temp.setDescription(ad.getDescription());
				temp.setFrom(ad.getFrom());
				temp.setTo(ad.getTo());
				temp.setPosition(ad.getPosition());
				temp.setTransport(ad.getTransport());
				temp.setType(ad.getType());
				if (ad.getEffect() != null) {
					temp.setEffect(ad.getEffect());
				}
				if (ad.getEntity() != null) {
					temp.setEntity(ad.getEntity());
				}
				if (ad.getNote() != null) {
					temp.setNote(ad.getNote());
				}
				// id.
				temp.setId(id);
				logger.info("Delay Alert " + result + "for router " + router);
				logger.info("AlertDelay [id:  " + temp.getId() + ", routeId: " + temp.getTransport().getRouteId());
				template.save(temp, Constants.ALERT_DELAY_REPO);
			}
		} catch (Exception e) {
			logger.error("", e);
			System.err.println(e.getMessage());
		}

		return result;
	}

	public String updateTrainsAlertDelay(String router, AlertDelay ad) {
		String result = "Created Train";

		// find existing one.
		// get alertdelay, generate id =
		// Id+_creatorType+_startOfDay0400+_endofday2358
		// save it.
		// next time when you receive an alert get the trip Id
		// append _startime_endtime as above.
		// check if it exist.

		try {

			SimpleDateFormat formatter = new SimpleDateFormat("MMddyyyyhhmm", Locale.ITALY);

			Calendar calF = Calendar.getInstance();
			calF.set(Calendar.HOUR_OF_DAY, 4);
			calF.set(Calendar.MINUTE, 0);
			String from = formatter.format(calF.getTime());

			Calendar calT = Calendar.getInstance();
			calT.set(Calendar.HOUR_OF_DAY, 23);
			calT.set(Calendar.MINUTE, 58);
			String to = formatter.format(calT.getTime());

			List<String> tids = findMatchingTrainTripIds(router, ad.getTransport().getTripId(),
					ad.getCreatorType().equals(CreatorType.USER));

			System.err.println(ad.getTransport().getTripId() + "/" + ad.getTransport().getRouteId() + " = " + tids);

			List<AlertDelay> alerts = new ArrayList<AlertDelay>();

			for (String tid : tids) {
				String id = tid + "_" + ad.getCreatorType() + "_" + from + "_" + to;
				List<AlertDelay> aDelayList = template.find(new Query(Criteria.where("id").is(id)), AlertDelay.class,
						Constants.ALERT_DELAY_REPO);

				if (aDelayList != null && !aDelayList.isEmpty()) {
					AlertDelay alert = aDelayList.get(0);
					alert.getTransport().setTripId(tid);
					alerts.add(alert);
				}
			}

			if (!alerts.isEmpty()) {
				for (AlertDelay alertD : alerts) {
					if (ad.getFrom() != -1) {
						alertD.setFrom(ad.getFrom());
					}
					if (ad.getTo() != -1) {
						alertD.setTo(ad.getTo());
					}
					if (ad.getDelay() != -1) {
						alertD.setDelay(ad.getDelay());
					}
					logger.info("Delay Alert " + result + "for router " + router);
					logger.info("AlertDelay [id:  " + alertD.getId() + ", routeId: " + alertD.getTransport().getRouteId());
					
					template.save(alertD, Constants.ALERT_DELAY_REPO);
					result = "Updated Train";
				}
			} else { // create.
				// Create AlertDelay.
				for (String tid : tids) {
					AlertDelay temp = new AlertDelay();
					temp.setCreatorId(ad.getCreatorId());
					temp.setCreatorType(ad.getCreatorType());
					temp.setDelay(ad.getDelay());
					temp.setDescription(ad.getDescription());
					temp.setFrom(ad.getFrom());
					temp.setTo(ad.getTo());
					temp.setPosition(ad.getPosition());
					temp.setTransport(ad.getTransport());
					temp.getTransport().setTripId(tid);
					temp.setType(ad.getType());
					if (ad.getEffect() != null) {
						temp.setEffect(ad.getEffect());
					}
					if (ad.getEntity() != null) {
						temp.setEntity(ad.getEntity());
					}
					if (ad.getNote() != null) {
						temp.setNote(ad.getNote());
					}
					String aId = tid + "_" + ad.getCreatorType() + "_" + from + "_" + to;
					temp.setId(aId);
					logger.info("Delay Alert " + result + "for router " + router);
					logger.info("AlertDelay [id:  " + temp.getId() + ", routeId: " + temp.getTransport().getRouteId());
					template.save(temp, Constants.ALERT_DELAY_REPO);
				}
			}
		} catch (Exception e) {
			logger.error("", e);
			System.err.println(e.getMessage());
		}

		return result;
	}

	public String updateAlertRoad(String router, AlertRoad ar) {
		String result = "Created";

		MongoTemplate template = routersMap.getMongoTemplateMap().get(router);

		if (template != null) {

			List<AlertRoad> aRoadList = template.find(new Query(Criteria.where("id").is(ar.getId())), AlertRoad.class,
					Constants.ALERT_ROAD_REPO);

			if (aRoadList != null && !aRoadList.isEmpty()) {
				result = "Updated";
			}

			template.save(ar, Constants.ALERT_ROAD_REPO);

		} else {
			result = "template is null";
		}

		return result;
	}

	/**
	 * find AlertDelay by Id,from,to.
	 * @param tp1 Transport.
	 * @param reqTime Long.
	 * @return AlertDelay.
	 */
	public List<AlertDelay> queryAlertDelay(String router, Transport tp1, Long reqTime) {

		List<AlertDelay> ad = new ArrayList<AlertDelay>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find all alerts starting with id.
			// and a.from lte requested time.
			// and a.to gte to requested time.
			String id = tp1.getTripId();
			// extra tripId.
			id = id.replaceAll("BT", "");
			id = id.replaceAll("TB", "");
			id = id.replaceAll("TA", "");
			id = id.replaceAll("AT", "");
			id = id.replaceAll("TR", "");
			id = id.replaceAll("RT", "");
			id = id.replaceAll("TV", "");
			id = id.replaceAll("VT", "");
			id = id.replaceAll("PREFESTIVI", "");

			if ("5".equals(tp1.getAgencyId()) || "6".equals(tp1.getAgencyId())) {
				//				String expr = "\\D*" + id.replaceAll("\\D*", "") + "\\D*_";
				id = id.replace("$", "\\$") + ".*$";
			}

			ad = template.find(new Query(Criteria.where("id").regex("^" + id, "i").and("from").lte(reqTime).and("to")
					.gte(reqTime).and("transport.agencyId").is(tp1.getAgencyId()).and("creatorType").is("SERVICE")
					.and("delay").gt(0)), AlertDelay.class, Constants.ALERT_DELAY_REPO);

			// if SERVICE type Alert is not found, check for USER type alert.
			if (ad.isEmpty()) {
				ad = template.find(new Query(Criteria.where("id").regex("^" + id, "i").and("from").lte(reqTime)
						.and("to").gte(reqTime).and("transport.agencyId").is(tp1.getAgencyId()).and("creatorType")
						.is("USER").and("delay").gt(0)), AlertDelay.class, Constants.ALERT_DELAY_REPO);
			}
		}

		return ad;
	}

	/**
	 * find AlertStrike by Id,from,to.
	 * @param id String
	 * @param reqTime Long
	 * @return AlertStrike.
	 */
	public List<AlertStrike> queryAlertStrike(String router, String id, Long reqTime) {

		List<AlertStrike> as = new ArrayList<AlertStrike>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			// find all alerts starting with id.
			// and a.from lte requested time.
			// and a.to gte to requested time.
			as = template.find(
					new Query(
							Criteria.where("id").regex("^" + id, "i").and("from").lte(reqTime).and("to").gte(reqTime)),
					AlertStrike.class, Constants.ALERT_STRIKE_REPO);
		}
		return as;
	}

	public String updateAlertAccident(String router, AlertAccident alertAccident) {
		String result = "Created";

		AlertAccident alertAccidentFound = findAlertAccident(router, alertAccident);
		if (alertAccidentFound != null) {
			result = "Updated";
		}

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			template.save(alertAccident, Constants.ALERT_ACCIDENT_REPO);
		}

		return result;
	}

	private AlertAccident findAlertAccident(String router, AlertAccident alertAccident) {
		// find
		AlertAccident alertAccidentFound = null;
		List<AlertAccident> alertAccidentList = null;
		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			if (alertAccident.getTransport().getTripId() != null) {
				alertAccidentList = template.find(
						new Query(Criteria.where("transport.tripId").is(alertAccident.getTransport().getTripId())),
						AlertAccident.class, Constants.ALERT_ACCIDENT_REPO);
			} else if (alertAccident.getTransport().getRouteId() != null) {
				alertAccidentList = template.find(
						new Query(Criteria.where("transport.routeId").is(alertAccident.getTransport().getRouteId())),
						AlertAccident.class, Constants.ALERT_ACCIDENT_REPO);
			} else if (alertAccident.getPosition().getStopId().getId() != null) {
				alertAccidentList = template.find(
						new Query(Criteria.where("position.stopId.id")
								.is(alertAccident.getPosition().getStopId().getId())),
						AlertAccident.class, Constants.ALERT_ACCIDENT_REPO);
			}
		}

		if (!alertAccidentList.isEmpty()) {
			alertAccidentFound = alertAccidentList.get(0);
		}

		return alertAccidentFound;
	}

	public List<Contact> getTaxiAgencyContacts(String router)
			throws JsonParseException, JsonMappingException, IOException {

		List<Contact> contacts = new ArrayList<Contact>();

		RouterConfig routerConfig = configurationManager.getRouter(router);

		if (routerConfig.getTaxi() != null && !routerConfig.getTaxi().isEmpty()) {

			for (String key : routerConfig.getTaxi().keySet()) {

				Agency agency = routerConfig.getTaxi().get(key);
				File jsonFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator")
						+ routerConfig.getRouter() + agency.getContactfilePath());
				if (jsonFile != null) {
					List list = mapper.readValue(jsonFile, List.class);

					for (Object obj : list) {
						Contact taxiContact = mapper.convertValue(obj, Contact.class);
						contacts.add(taxiContact);
					}
				}
			}
		}

		return contacts;
	}

}
