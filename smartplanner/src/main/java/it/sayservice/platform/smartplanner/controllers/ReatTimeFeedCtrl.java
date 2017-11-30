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

import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.core.adapter.AlertToGTFSAdapter;
import it.sayservice.platform.smartplanner.core.adapter.GTFsToAlertAdapter;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.FeedMessage;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TestModelClass;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertParking;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.geocoder.GeocodeAPIsManager;
import it.sayservice.platform.smartplanner.model.DynamicBikeStation;
import it.sayservice.platform.smartplanner.model.DynamicCarStation;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.utils.Agency;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.JsonFormat.ParseException;

@Controller
public class ReatTimeFeedCtrl {

	/** configuration manager. **/
	@Autowired
	private ConfigurationManager configurationManager;
	/** otp connection manager. **/
	@Autowired
	private OTPConnector otpConnector;
	/** geocode API manager. **/
	@Autowired
	private GeocodeAPIsManager geocodeAPIsManager;
	/** itinerary JSON parser. **/
	@Autowired
	private ItineraryBuildHelper helper;
	/** mongo template. **/
	private MongoTemplate template;
	@Autowired
	private RepositoryUtils repositoryUtils;
	@Autowired
	private MongoRouterMapper routersMap;

	private static final long PARKING_DATA_VALIDITY = 1000 * 60 * 60 * 24;

	private static final transient Logger logger = LoggerFactory.getLogger(ReatTimeFeedCtrl.class);

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAP")
	public @ResponseBody String updateAP(@PathVariable String router, @RequestBody AlertParking ap) {
		String result = "";

		// check creator id, type
		if (ap.getCreatorId() == null) {
			ap.setCreatorId("default");
		}

		if (ap.getCreatorType() == null) {
			ap.setCreatorType(CreatorType.DEFAULT);
		}

		RouterConfig routerConfig = configurationManager.getRouter(router);
		String mappedKeyAgencyId = ap.getPlace().getAgencyId();

		if (routerConfig.getBikeSharing().containsKey(mappedKeyAgencyId)) { // BIKE-RENTAL.
			String bikeRepoValidityInHrs = "1";

			Agency bikeAgency = routerConfig.getBikeSharing().get(mappedKeyAgencyId);

			if (bikeAgency.getSpecificProperties() != null
					&& bikeAgency.getSpecificProperties().containsKey("validity")) {
				bikeRepoValidityInHrs = bikeAgency.getSpecificProperties().get("validity");

			}
			int defaultPeriod = Integer.parseInt(bikeRepoValidityInHrs);
			long interval = defaultPeriod * 1000 * 60 * 60;
			Date date = new Date(System.currentTimeMillis());
			Long deadline = date.getTime() + interval;
			result = repositoryUtils.updateBikeStation(router, ap, deadline);

		} else { // CAR-RENTAL/CAR-PARKING.

			Agency carAgency = null;
			if (routerConfig.getCarSharing().containsKey(mappedKeyAgencyId)) {
				carAgency = routerConfig.getCarSharing().get(mappedKeyAgencyId);
			}
			if (carAgency == null && routerConfig.getCarParking().containsKey(mappedKeyAgencyId)) {
				carAgency = routerConfig.getCarParking().get(mappedKeyAgencyId);
			}

			if (carAgency != null) {

				String carRepoValidityInHrs = "1";

				if (carAgency.getSpecificProperties() != null
						&& carAgency.getSpecificProperties().containsKey("validity")) {
					carRepoValidityInHrs = carAgency.getSpecificProperties().get("validity");

				}

				int defaultPeriod = Integer.parseInt(carRepoValidityInHrs);
				long interval = defaultPeriod * 1000 * 60 * 60;
				Date date = new Date(System.currentTimeMillis());
				Long deadline = date.getTime() + interval;
				result = repositoryUtils.updateCarStation(router, ap, deadline);
			}
		}

		return ("Parking/Sharing Availability Alert " + result);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAS")
	public @ResponseBody String updateAS(@PathVariable String router, @RequestBody AlertStrike as) {
		String result = null;
		// check creator id, type
		if (as.getCreatorId() == null) {
			as.setCreatorId("default");
		}
		if (as.getCreatorType() == null) {
			as.setCreatorType(CreatorType.DEFAULT);
		}

		// update alert strike.
		result = repositoryUtils.updateAlertStrike(router, as);
		return ("Strike/Service Unavailabiliy Alert " + result);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAD")
	public @ResponseBody String updateAD(@PathVariable String router, @RequestBody AlertDelay ad) {
		String result = null;
		// check creator id, type
		if (ad.getCreatorId() == null) {
			ad.setCreatorId("default");
		}
		if (ad.getCreatorType() == null) {
			ad.setCreatorType(CreatorType.USER);
		}

		RouterConfig routerConfig = configurationManager.getRouter(router);

		List<String> trainAgencyIds = routerConfig.getGtfsTrainAgencyIds();

		if (trainAgencyIds.contains(ad.getTransport().getAgencyId())) {
			result = repositoryUtils.updateTrainsAlertDelay(router, ad);
		} else {
			result = repositoryUtils.updateAlertDelay(router, ad);
		}

		return ("Delay Alert " + result + "for router " + router);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateADGTFS")
	public @ResponseBody void updateADGTFS(@PathVariable String router, @ApiParam(value = "List of GTFS Alert Delay") @RequestBody FeedMessage feedMessage) {

    List<AlertDelay> alertDelayList = GTFsToAlertAdapter.getAlertDelayEntityListFromGTFS(feedMessage);

    for (AlertDelay alertDelay : alertDelayList) {
			updateAlertDelay(router, alertDelay);
		}
    
	}

	private String updateAlertDelay(String router, AlertDelay alertDelay) {
		String result = null;
		// check creator id, type
		if (alertDelay.getCreatorId() == null) {
			alertDelay.setCreatorId("default");
		}
		if (alertDelay.getCreatorType() == null) {
			alertDelay.setCreatorType(CreatorType.USER);
		}

		List<String> trainAgencyIds = configurationManager.getRouter(router).getGtfsTrainAgencyIds();

		if (trainAgencyIds.contains(alertDelay.getTransport().getAgencyId())) {
			result = repositoryUtils.updateTrainsAlertDelay(router, alertDelay);
		} else {
			result = repositoryUtils.updateAlertDelay(router, alertDelay);
		}
		
		return ("Delay Alert " + result + "for router " + router);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAEGTFS")
	public @ResponseBody String updateAEGTFS(@PathVariable String router, @ApiParam(value = "List of GTFS Alert Accident")  @RequestBody FeedMessage feedMessage) {

	    List<AlertAccident> alertAccidentList = GTFsToAlertAdapter.getAlertAccidentListFromGTFSServiceAlert(feedMessage);

	    for (AlertAccident alertAccident : alertAccidentList) {
	    	repositoryUtils.updateAlertAccident(router, alertAccident);
			}

	    return ("Accident/WorkInProgress/TrafficJam Alert Updated!!!");
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAE")
	public @ResponseBody String updateAE(@PathVariable String router, @RequestBody AlertAccident ae) {
		return ("Accident/WorkInProgress/TrafficJam Alert Updated!!!");

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateAR")
	public @ResponseBody String updateAR(@PathVariable String router, @RequestBody AlertRoad ar) {

		// check creator id, type
		if (ar.getCreatorId() == null) {
			ar.setCreatorId("default");
		}
		if (ar.getCreatorType() == null) {
			ar.setCreatorType(CreatorType.USER);
		}

		// update alert road.
		String result = repositoryUtils.updateAlertRoad(router, ar);
		return ("Road Alert " + result);

	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateARGTFS")
	public @ResponseBody String updateARGTFS(@PathVariable String router, @ApiParam(value = "List of GTFS Alert Road")  @RequestBody FeedMessage feedMessage) {

    List<AlertRoad> alertRoadList = GTFsToAlertAdapter.getAlertRoadListFromGTFS(feedMessage);

    String result = "";
    for (AlertRoad alertRoad : alertRoadList) {
  		// check creator id, type
  		if (alertRoad.getCreatorId() == null) {
  			alertRoad.setCreatorId("default");
  		}
  		if (alertRoad.getCreatorType() == null) {
  			alertRoad.setCreatorType(CreatorType.USER);
  		}

  		// update alert road.
  		result = repositoryUtils.updateAlertRoad(router, alertRoad);
		}

		return ("Road Alert " + result);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAllASGTFS")
	public @ResponseBody
	List<FeedMessage> getASGTFS(@PathVariable String router) {

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertStrikeGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertStrike> alestrStrikeList = template.findAll(AlertStrike.class, Constants.ALERT_STRIKE_REPO);

			alertStrikeGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertStrikeList(alestrStrikeList);
		}
		
		return alertStrikeGtfsList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getASGTFS")
	public @ResponseBody
	List<FeedMessage> getASGTFS(@PathVariable String router, @RequestParam("agencyId") String agencyId, @RequestParam("routeId") String routeId, @RequestParam("stopId") String stopId, @RequestParam("tripId") String tripId, @RequestParam("from") Long from,
			@RequestParam("to") Long to) {
		Criteria c = new Criteria();
		if (agencyId != null && !agencyId.isEmpty()){
			c.and("transport.agencyId").is(agencyId);
		}
		if (routeId != null && !routeId.isEmpty()){
			c.and("transport.routeId").is(routeId);
		}
		if (stopId != null && !stopId.isEmpty()){
			c.and("transport.stopId").is(stopId);
		}
		if (tripId != null && !tripId.isEmpty()){
			c.and("transport.tripId").is(tripId);
		}
		if (from != null) {
			if (to != null) {
				c.and("to").lte(to);
			}
			c.and("from").gt(from);
		}

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertStrikeGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertStrike> alestrStrikeList = template.find(new Query(c), AlertStrike.class, Constants.ALERT_STRIKE_REPO);
						
			alertStrikeGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertStrikeList(alestrStrikeList);
		}

		return alertStrikeGtfsList;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/updateASGTFS")
	public @ResponseBody String updateASGTFS(@PathVariable String router, @ApiParam(value = "List of GTFS Alert Strike")  @RequestBody FeedMessage feedMessage) {

    List<AlertStrike> alertStrikeList = GTFsToAlertAdapter.getAlertStrikeEntityListFromGTFSServiceAlert(feedMessage);
		
    for (AlertStrike alertStrike : alertStrikeList) {
    	repositoryUtils.updateAlertStrike(router, alertStrike);
		}

    return "OK";
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAS")
	public @ResponseBody List<AlertStrike> getAS(@PathVariable String router) {

		List<AlertStrike> alertStrikes = new ArrayList<AlertStrike>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			alertStrikes = template.findAll(AlertStrike.class, Constants.ALERT_STRIKE_REPO);
		}

		return alertStrikes;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getBikeAP")
	public @ResponseBody List<DynamicBikeStation> getBikeAP(@PathVariable String router) {

		List<DynamicBikeStation> dynamicBikeStations = new ArrayList<DynamicBikeStation>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			dynamicBikeStations = template.findAll(DynamicBikeStation.class, Constants.ALERT_BIKE_REPO);
		}
		return dynamicBikeStations;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getCarAP")
	public @ResponseBody List<DynamicCarStation> getCarAP(@PathVariable String router) {

		List<DynamicCarStation> dynamicCarStations = new ArrayList<DynamicCarStation>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			dynamicCarStations = template.findAll(DynamicCarStation.class, Constants.ALERT_CAR_REPO);
		}

		return dynamicCarStations;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAD")
	public @ResponseBody List<AlertDelay> getAD(@PathVariable String router) {

		List<AlertDelay> alertDelays = new ArrayList<AlertDelay>();

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			alertDelays = template.findAll(AlertDelay.class, Constants.ALERT_DELAY_REPO);
		}

		return alertDelays;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAllADGTFS")
	public @ResponseBody
	List<FeedMessage> getADGTFS(@PathVariable String router) {
		List<FeedMessage> alertDelayGtfsList = new ArrayList<FeedMessage>();
 
		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		if (template != null) {
			List<AlertDelay> alertDelayList = template.findAll(AlertDelay.class, Constants.ALERT_DELAY_REPO);
	
			alertDelayGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertDelayList(alertDelayList);
		}
		
		return alertDelayGtfsList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getADGTFS")
	public @ResponseBody
	List<FeedMessage> getADGTFS(@PathVariable String router, @RequestParam("agencyId") String agencyId, @RequestParam("routeId") String routeId, @RequestParam("tripId") String tripId, @RequestParam("stopId") String stopId, @RequestParam("from") Long from,
			@RequestParam("to") Long to) {
		Criteria c = new Criteria();
		if (agencyId != null && !agencyId.isEmpty()){
			c.and("agencyId").is(agencyId);
		}
		if (routeId != null && !routeId.isEmpty()){
			c.and("transport.routeId").is(routeId);
		}
		if (tripId != null && !tripId.isEmpty()){
			c.and("transport.tripId").is(tripId);
		}
		if (stopId != null && !stopId.isEmpty()){
			c.and("position.stopId").is(stopId);
		}
		if (from != null) {
			if (to != null) {
				c.and("to").lte(to);
			}
			c.and("from").gt(from);
		}

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertDelayGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertDelay> alertDelayList = template.find(new Query(c), AlertDelay.class, Constants.ALERT_DELAY_REPO);
			
			alertDelayGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertDelayList(alertDelayList);
		}		

		return alertDelayGtfsList;
	}


	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAllAEGTFS")
	public @ResponseBody
	List<FeedMessage> getAEGTFS(@PathVariable String router) {

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertAccidentGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertAccident> alertAccidentList = template.findAll(AlertAccident.class, Constants.ALERT_ACCIDENT_REPO);

			alertAccidentGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertAccidentList(alertAccidentList);
		}
		
		return alertAccidentGtfsList;
	}


	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAEGTFS")
	public @ResponseBody
	List<FeedMessage> getAEGTFS(@PathVariable String router, @RequestParam("agencyId") String agencyId, @RequestParam("routeId") String routeId, @RequestParam("stopId") String stopId, @RequestParam("tripId") String tripId, @RequestParam("from") Long from,
			@RequestParam("to") Long to) {
		Criteria c = new Criteria();
		if (agencyId != null && !agencyId.isEmpty()){
			c.and("transport.agencyId").is(agencyId);
		}
		if (routeId != null && !routeId.isEmpty()){
			c.and("transport.routeId").is(routeId);
		}
		if (stopId != null && !stopId.isEmpty()){
			c.and("position.stopId.id").is(stopId);
		}
		if (stopId != null && !stopId.isEmpty()){
			c.and("transport.tripId").is(stopId);
		}
		if (from != null) {
			if (to != null) {
				c.and("to").lte(to);
			}
			c.and("from").gt(from);
		}

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertAccidentGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertAccident> alertAccidentList = template.find(new Query(c), AlertAccident.class, Constants.ALERT_ACCIDENT_REPO);
					
			alertAccidentGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertAccidentList(alertAccidentList);
		}
		
		return alertAccidentGtfsList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAR")
	public @ResponseBody List<AlertRoad> getAR(@PathVariable String router, @RequestParam("agencyId") String agencyId,
			@RequestParam("from") Long from, @RequestParam("to") Long to) throws SmartPlannerException {

		List<AlertRoad> alertRoad = new ArrayList<AlertRoad>();

		try {

			if (routersMap.getMongoTemplateMap().containsKey(router)) {
				template = routersMap.getMongoTemplateMap().get(router);
			}

			if (template != null) {
				Criteria c = new Criteria();
				if (agencyId != null && !agencyId.isEmpty())
					c.and("agencyId").is(agencyId);
				if (from != null) {
					if (to != null) {
						c.and("from").lte(to);
					}
					c.and("to").gt(from);
				}

				alertRoad = template.find(new Query(c), AlertRoad.class, Constants.ALERT_ROAD_REPO);

			} else {
				throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), "mongo template is null");
			}

		} catch (Exception e) {
			throw new SmartPlannerException(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage());
		}

		return alertRoad;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getAllARGTFS")
	public @ResponseBody
	List<FeedMessage> getARGTFS(@PathVariable String router) {
  
		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertRoadGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertRoad> alertRoadList = template.findAll(AlertRoad.class, Constants.ALERT_ROAD_REPO);			
			
			alertRoadGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertRoadList(alertRoadList);
		}

		return alertRoadGtfsList;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/getARGTFS")
	public @ResponseBody
	List<FeedMessage> getARGTFS(@PathVariable String router, @RequestParam("agencyId") String agencyId, @RequestParam("routeId") String routeId, @RequestParam("stopId") String stopId, @RequestParam("from") Long from,
			@RequestParam("to") Long to) {
		Criteria c = new Criteria();
		if (agencyId != null && !agencyId.isEmpty()){
			c.and("agencyId").is(agencyId);
		}
		if (routeId != null && !routeId.isEmpty()){
			c.and("routeId").is(routeId);
		}
		if (stopId != null && !stopId.isEmpty()){
			c.and("stopId").is(stopId);
		}
		if (from != null) {
			if (to != null) {
				c.and("to").lte(to);
			}
			c.and("from").gt(from);
		}

		if (routersMap.getMongoTemplateMap().containsKey(router)) {
			template = routersMap.getMongoTemplateMap().get(router);
		}

		List<FeedMessage> alertRoadGtfsList = new ArrayList<FeedMessage>();

		if (template != null) {
			List<AlertRoad> alertRoadList = template.find(new Query(c), AlertRoad.class, Constants.ALERT_ROAD_REPO);
			
			alertRoadGtfsList = AlertToGTFSAdapter.getGTFSEntityListFromAlertRoadList(alertRoadList);
		}
		
		return alertRoadGtfsList;
	}


	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}
}
