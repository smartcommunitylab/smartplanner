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

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.MongoRepositoryFactory;
import org.springframework.stereotype.Component;

import it.sayservice.platform.smartplanner.model.AreaPoint;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.DynamicBikeStation;
import it.sayservice.platform.smartplanner.model.DynamicCarStation;
import it.sayservice.platform.smartplanner.model.Stop;
import it.sayservice.platform.smartplanner.model.StreetLocation;
import it.sayservice.platform.smartplanner.model.TaxiStation;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.mongo.repos.BikeStationRepository;
import it.sayservice.platform.smartplanner.mongo.repos.CarStationRepository;
import it.sayservice.platform.smartplanner.mongo.repos.StreetLocationRepository;
import it.sayservice.platform.smartplanner.mongo.repos.TaxiStationRepository;
import it.sayservice.platform.smartplanner.utils.Constants;

@Component
public class MongoRouterMapper {

	@Autowired
	private ApplicationContext appContext;

	public Map<String, MongoRepositoryFactory> mongoFactoryMap = new HashMap<String, MongoRepositoryFactory>();

	public Map<String, MongoTemplate> mongoTemplateMap = new HashMap<String, MongoTemplate>();
	
	MongoTemplate template;
	
	public MongoRouterMapper() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public MongoRouterMapper(ApplicationContext appContext, Map<String, MongoRepositoryFactory> mongoFactoryMap,
			Map<String, MongoTemplate> mongoTemplateMap, MongoTemplate template) {
		this.appContext = appContext;
		this.mongoFactoryMap = mongoFactoryMap;
		this.mongoTemplateMap = mongoTemplateMap;
		this.template = template;
	}



	public MongoRouterMapper(MongoTemplate mongoTemplate, String router) {
		this.template = mongoTemplate;
		
		// drop existing database.
//		template.getDb().dropDatabase();
		
		// create collections.
		if (!template.collectionExists(BikeStation.class)) {
			template.createCollection(BikeStation.class);
		}
		if (!template.collectionExists(CarStation.class)) {
			template.createCollection(CarStation.class);
		}
		if (!template.collectionExists(TaxiStation.class)) {
			template.createCollection(TaxiStation.class);
		}
		if (!template.collectionExists(StreetLocation.class)) {
			template.createCollection(StreetLocation.class);
		}
		// configure the client ...
		template.indexOps(BikeStation.class).ensureIndex(new GeospatialIndex("location"));
		template.indexOps(CarStation.class).ensureIndex(new GeospatialIndex("location"));
		template.indexOps(TaxiStation.class).ensureIndex(new GeospatialIndex("location"));
		template.indexOps(AreaPoint.class).ensureIndex(new GeospatialIndex("location"));
		template.indexOps(StreetLocation.class).ensureIndex(new GeospatialIndex("location"));
		template.indexOps(Constants.STOPS).ensureIndex(new GeospatialIndex("coordinates"));
		template.indexOps(Stop.class).ensureIndex(new GeospatialIndex("coordinates"));

		MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
		// router-mongoFactory map.
		mongoFactoryMap.put(router, factory);
		// router-mongoTemplate map.
		mongoTemplateMap.put(router, template);

	}

	@PostConstruct
	public void init() {

		List<String> routers = Arrays.asList(appContext.getEnvironment().getProperty("routers").split(","));

		for (String router : routers) {
			template = (MongoTemplate) appContext.getBean(router);

			// drop existing database.
//			template.getDb().dropDatabase();
			
			// create collections.
			if (!template.collectionExists(BikeStation.class)) {
				template.createCollection(BikeStation.class);
			}
			if (!template.collectionExists(CarStation.class)) {
				template.createCollection(CarStation.class);
			}
			if (!template.collectionExists(TaxiStation.class)) {
				template.createCollection(TaxiStation.class);
			}
			if (!template.collectionExists(StreetLocation.class)) {
				template.createCollection(StreetLocation.class);
			}
			// configure the client ...
			template.indexOps(BikeStation.class).ensureIndex(new GeospatialIndex("location"));
			template.indexOps(CarStation.class).ensureIndex(new GeospatialIndex("location"));
			template.indexOps(TaxiStation.class).ensureIndex(new GeospatialIndex("location"));
			template.indexOps(AreaPoint.class).ensureIndex(new GeospatialIndex("location"));
			template.indexOps(StreetLocation.class).ensureIndex(new GeospatialIndex("location"));
			template.indexOps(Constants.STOPS).ensureIndex(new GeospatialIndex("coordinates"));
			template.indexOps(Stop.class).ensureIndex(new GeospatialIndex("coordinates"));

			MongoRepositoryFactory factory = new MongoRepositoryFactory(template);
			// router-mongoFactory map.
			mongoFactoryMap.put(router, factory);
			// router-mongoTemplate map.
			mongoTemplateMap.put(router, template);
		}

	}

	public CarStationRepository getCarStationRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			CarStationRepository carStationRepository = mongoFactoryMap.get(router)
					.getRepository(CarStationRepository.class);
			return carStationRepository;
		}

		return null;

	}

	public DynamicCarStation getDynamicCarStationRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			DynamicCarStation dynamicCarStation = mongoFactoryMap.get(router).getRepository(DynamicCarStation.class);
			return dynamicCarStation;	
		}
		
		return null;
	}

	public BikeStationRepository getBikeStationRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			BikeStationRepository bikeStationRepository = mongoFactoryMap.get(router)
					.getRepository(BikeStationRepository.class);
			return bikeStationRepository;	
		}
		
		return null;
	}

	public DynamicBikeStation getDynamicBikeStationRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			DynamicBikeStation dynamicBikeStation = mongoFactoryMap.get(router).getRepository(DynamicBikeStation.class);
			return dynamicBikeStation;	
		}
		
		return null;
	}

	public StreetLocationRepository getStreetLocationRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			StreetLocationRepository streetLocationRepository = mongoFactoryMap.get(router)
					.getRepository(StreetLocationRepository.class);
			return streetLocationRepository;	
		}
		
		return null;
		
	}

	public AreaPointRepository getAreaPointRepository(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			AreaPointRepository areaPointRepository = mongoFactoryMap.get(router).getRepository(AreaPointRepository.class);
			return areaPointRepository;	
		}
		
		return null;
	}

	public Map<String, MongoRepositoryFactory> getFactoryMap() {
		return mongoFactoryMap;
	}

	public Map<String, MongoTemplate> getMongoTemplateMap() {
		return mongoTemplateMap;
	}

	public TaxiStationRepository getTaxiStationRepo(String router) {

		if (mongoFactoryMap.containsKey(router)) {
			TaxiStationRepository taxiStationRepository = mongoFactoryMap.get(router)
					.getRepository(TaxiStationRepository.class);
			return taxiStationRepository;
		}

		return null;
	}
	
	public void emptyCollections(String router) {
		if (mongoTemplateMap.containsKey(router)) {
			mongoTemplateMap.get(router).remove(new Query(), BikeStation.class);
			mongoTemplateMap.get(router).remove(new Query(), CarStation.class);
			mongoTemplateMap.get(router).remove(new Query(), TaxiStation.class);
			mongoTemplateMap.get(router).remove(new Query(), StreetLocation.class);
			mongoTemplateMap.get(router).remove(new Query(), AreaPoint.class);
			mongoTemplateMap.get(router).remove(new Query(), Stop.class);
			
			mongoTemplateMap.get(router).remove(new Query(), Constants.ROUTES);
			mongoTemplateMap.get(router).remove(new Query(), Constants.STOPS);
			mongoTemplateMap.get(router).remove(new Query(), Constants.SCHEDULES);
			mongoTemplateMap.get(router).remove(new Query(), Constants.TIMETABLE);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ROUTE_STOPS);
			mongoTemplateMap.get(router).remove(new Query(), Constants.STOP_NAMES);
			mongoTemplateMap.get(router).remove(new Query(), Constants.TRIPS);
			mongoTemplateMap.get(router).remove(new Query(), Constants.STOPS);
			
			mongoTemplateMap.get(router).remove(new Query(), Constants.ALERT_DELAY_REPO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ALERT_STRIKE_REPO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ALERT_BIKE_REPO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ALERT_CAR_REPO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ALERT_ROAD_REPO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.AGENCY_INFO);
			mongoTemplateMap.get(router).remove(new Query(), Constants.ANNOTATED_TRIPS);
		}
	}

}
