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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.common.collect.Sets;

import io.swagger.annotations.ApiParam;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.SimpleLeg;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.geocoder.GeocodeAPIsManager;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.multimodal.modes.BicyleOnly;
import it.sayservice.platform.smartplanner.multimodal.modes.BikeRental;
import it.sayservice.platform.smartplanner.multimodal.modes.BikeRentalViaStations;
import it.sayservice.platform.smartplanner.multimodal.modes.BusOnly;
import it.sayservice.platform.smartplanner.multimodal.modes.CarOnly;
import it.sayservice.platform.smartplanner.multimodal.modes.CarRental;
import it.sayservice.platform.smartplanner.multimodal.modes.CarRentalViaStations;
import it.sayservice.platform.smartplanner.multimodal.modes.CarWithParkingPlaces;
import it.sayservice.platform.smartplanner.multimodal.modes.ParkAndRide;
import it.sayservice.platform.smartplanner.multimodal.modes.TrainOnly;
import it.sayservice.platform.smartplanner.multimodal.modes.Transit;
import it.sayservice.platform.smartplanner.multimodal.modes.WalkOnly;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;
import springfox.documentation.annotations.ApiIgnore;

/**
 * Planner Controller.
 * 
 * @author nawazk
 * 
 */

@Controller
//@RequestMapping("/smart-planner")
public class PlannerCtrl {

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

	private static final transient Logger logger = LoggerFactory.getLogger(PlannerCtrl.class);

	/**
	 * constructor.
	 * 
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PlannerCtrl() throws FileNotFoundException, IOException {
	}

	public PlannerCtrl(ConfigurationManager confManager, OTPConnector otpConn, GeocodeAPIsManager geoManager,
			ItineraryBuildHelper itnHelper, MongoTemplate mongoTemplate, RepositoryUtils repoUtils,
			MongoRouterMapper routerMapper) {

		this.configurationManager = confManager;
		this.otpConnector = otpConn;
		this.geocodeAPIsManager = geoManager;
		this.helper = itnHelper;
		this.template = mongoTemplate;
		this.repositoryUtils = repoUtils;
		this.routersMap = routerMapper;

	}

	@ApiIgnore
	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/ping")
	public @ResponseBody String greeting(@RequestParam("name") String name, @RequestParam("place") String place) {
		String response = "";
		try {
			response = "Welcome " + name + " to " + place;
		} catch (Exception e) {
		}
		return response;
	}

	/**
	 * Getter OTP Connection Manager.
	 * @return OTPConnector
	 */
	public OTPConnector getOtpConnector() {
		return otpConnector;
	}

	/**
	 * Setter OTP Connection Manager.
	 * @param otpConnector OTPConnector
	 */
	public void setOtpConnector(OTPConnector otpConnector) {
		this.otpConnector = otpConnector;
	}

	/**
	 * Getter ItineraryBuildHelper.
	 * @return ItineraryBuildHelper
	 */
	public ItineraryBuildHelper getHelper() {
		return helper;
	}

	/**
	 * Setter ItineraryBuildHelper.
	 * @param helper ItineraryBuildHelper
	 */
	public void setHelper(ItineraryBuildHelper helper) {
		this.helper = helper;
	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/plan")
	public @ResponseBody List<Itinerary> plan(@PathVariable String router,
			@ApiParam(value = "lat,lon", required = true) @RequestParam("from") String from,
			@ApiParam(value = "lat,lon", required = true) @RequestParam("to") String to,
			@ApiParam(value = "departure date mm/dd/yyyy", required = true) @RequestParam("date") String date,
			@RequestParam(required = false) String arrivalTime,
			@ApiParam(value = "departure time hh:mmss", required = true) @RequestParam("departureTime") String departureTime,
			@ApiParam(defaultValue = "TRANSIT", allowableValues = "TRANSIT,CAR,SHAREDBIKE,SHAREDBIKE_WITHOUT_STATION,GONDOLA,CARWITHPARKING,SHAREDCAR,SHAREDCAR_WITHOUT_STATION,BUS,TRAIN,WALK,PARK_AND_RIDE", required = true) @RequestParam("transportType") String transportType,
			@ApiParam(value = "wheelChair", required = false) @RequestParam(required = false) Boolean wheelchair,
			@ApiParam(defaultValue = "fastest", allowableValues = "fastest,healthy,leastWalking,leastChanges,greenest,safest", required = false) @RequestParam(required = false) String routeType,
			@ApiParam(defaultValue = "3", required = true) @RequestParam("numOfItn") String numOfItn,
			@ApiParam(value = "The maximum distance (in meters) the user is willing to walk.", required = false) @RequestParam(required = false) String maxWalkDistance,
			@ApiParam(value = "Threshold distance (in meters) user is willing to walk in total itinerary.", required = false) @RequestParam(required = false) String maxTotalWalkDistance,
			@ApiParam(value = "Additional mode user is willing to take from parking station(bike/car).", required = false) @RequestParam(required = false) String extraTransport,
			@ApiParam(value = "The maximum number of transfers during the trip.", required = false) @RequestParam(required = false) String maxChanges) {

		List<Itinerary> output = new ArrayList<Itinerary>();
		RType routeT;
		TType modeT;

		try {
			if (transportType != null) {
				modeT = TType.valueOf(transportType);
				if (modeT == null) {
					modeT = TType.TRANSIT;
				}
			} else {
				modeT = TType.TRANSIT;
			}

			if (routeType != null) {
				routeT = RType.valueOf(routeType);
				if (routeT == null) {
					routeT = RType.fastest;
				}
			} else {
				routeT = RType.fastest;
			}
			
			if (wheelchair != null && wheelchair) {
				wheelchair = true;
			} else {
				wheelchair = false;
			}

			output = planTrip(router, from, to, date, arrivalTime, departureTime, modeT, routeT, wheelchair, numOfItn,
					maxWalkDistance, maxTotalWalkDistance, extraTransport, maxChanges);

		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return output;

	}

	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/recurrentJourney")
	public @ResponseBody List<SimpleLeg> recurrentJourney(@PathVariable String router,
			@ApiParam(value = "weekdays 1-7", required = false) @RequestParam("recurrence") String recurrence,
			@ApiParam(value = "lat,lon", required = true) @RequestParam("from") String from,
			@ApiParam(value = "lat,lon", required = true) @RequestParam("to") String to,
			@ApiParam(value = "departure time hh:mmss", required = true) @RequestParam("time") String time,
			@ApiParam(defaultValue = "7200000", value = "interval in hours", required = true) @RequestParam("interval") Long interval,
			@ApiParam(defaultValue = "TRANSIT", allowableValues = "TRANSIT,CAR,SHAREDBIKE,SHAREDBIKE_WITHOUT_STATION,GONDOLA,CARWITHPARKING,SHAREDCAR,SHAREDCAR_WITHOUT_STATION,BUS,TRAIN,WALK,PARK_AND_RIDE", required = true) @RequestParam("transportType") String transportType,
			@ApiParam(defaultValue = "fastest", allowableValues = "fastest,healthy,leastWalking,leastChanges,greenest,safest", required = false) @RequestParam(required = false) String routeType,
			@ApiParam(value = "Start date in milliseconds.", required = true) @RequestParam("fromDate") Long fromDate,
			@ApiParam(value = "End date in milliseconds.", required = true) @RequestParam("toDate") Long toDate) {

		List<Leg> rjOutput = new ArrayList<Leg>();
		/** to, from validation **/
		Position fromPos = null;
		Position toPos = null;

		try {

			if (from != null) {
				String[] point = from.split(",", 2);
				fromPos = new Position("", null, "", point[0], point[1]);
			}

			if (to != null) {
				String[] point = to.split(",", 2);
				toPos = new Position("", null, "", point[0], point[1]);
			}

			/** transport, interval,routes, recurrence validation **/
			RType routeT;
			TType modeT;

			if (transportType != null) {
				modeT = TType.valueOf(transportType);
				if (modeT == null) {
					modeT = TType.TRANSIT;
				}
			} else {
				modeT = TType.TRANSIT;
			}

			if (routeType != null) {
				routeT = RType.valueOf(routeType);
				if (routeT == null) {
					routeT = RType.fastest;
				}
			} else {
				routeT = RType.fastest;
			}

			if (interval == null) {
				interval = new Long(2 * 60 * 60 * 1000);
			}

			/**
			 * time, date, validation inside corresponding Modes.java
			 **/

			// mandatory condition
			if (fromPos != null && toPos != null) {
				HashMap<String, Object> userRequest = new HashMap<String, Object>();
				userRequest.put(Constants.SP_RQ_RECURRENCE, recurrence);
				userRequest.put(Constants.SP_RQ_FROM, fromPos);
				userRequest.put(Constants.SP_RQ_TO, toPos);
				userRequest.put(Constants.SP_RQ_TIME, time);
				userRequest.put(Constants.SP_RQ_INTERVAL, interval);
				userRequest.put(Constants.SP_RQ_ROUTE_PREF, routeT);
				userRequest.put(Constants.SP_RQ_FROMDATE, fromDate);
				userRequest.put(Constants.SP_RQ_TODATE, Math.min(toDate, fromDate + RecurrentUtil.DAY * 6));

				if (modeT.equals(TType.TRANSIT)) {
					Transit trans = new Transit(otpConnector, helper);
					rjOutput = trans.getLegs(router, userRequest);
				} else if (modeT.equals(TType.CAR)) {
					CarOnly carO = new CarOnly(router, otpConnector, geocodeAPIsManager, helper, routersMap,
							repositoryUtils);
					rjOutput = carO.getLegs(router, userRequest);
				} else if (modeT.equals(TType.BICYCLE)) {
					BicyleOnly bikeO = new BicyleOnly(otpConnector, helper);
					rjOutput = bikeO.getLegs(router, userRequest);
				} else if (modeT.equals(TType.BUS)) {
					BusOnly busOnly = new BusOnly(otpConnector, helper);
					rjOutput = busOnly.getLegs(router, userRequest);
				} else if (modeT.equals(TType.TRAIN)) {
					TrainOnly trainOnly = new TrainOnly(otpConnector, helper);
					rjOutput = trainOnly.getLegs(router, userRequest);
				} else if (modeT.equals(TType.CARWITHPARKING)) {
					CarWithParkingPlaces carWPM = new CarWithParkingPlaces(otpConnector, geocodeAPIsManager, helper,
							routersMap, repositoryUtils);
					rjOutput = carWPM.getLegs(router, userRequest);
				} else if (modeT.equals(TType.SHAREDBIKE_WITHOUT_STATION)) {
					BikeRental bikeR = new BikeRental(otpConnector, helper, repositoryUtils);
					rjOutput = bikeR.getLegs(router, userRequest);

				} else if (modeT.equals(TType.SHAREDCAR_WITHOUT_STATION)) {
					CarRental carR = new CarRental(otpConnector, helper, repositoryUtils);
					rjOutput = carR.getLegs(router, userRequest);

				} else if (modeT.equals(TType.SHAREDCAR)) {
					CarRentalViaStations carRVS = new CarRentalViaStations(otpConnector, helper, repositoryUtils);
					rjOutput = carRVS.getLegs(router, userRequest);
				} else if (modeT.equals(TType.SHAREDBIKE)) {
					BikeRentalViaStations bikeRVS = new BikeRentalViaStations(otpConnector, helper, repositoryUtils);
					rjOutput = bikeRVS.getLegs(router, userRequest);
				}
			}
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		List<SimpleLeg> filteredLegs = filterLegs(rjOutput);
		return filteredLegs;

	}

	private List<SimpleLeg> filterLegs(List<Leg> legs) {
		List<Transport> transports = new ArrayList<Transport>();
		List<SimpleLeg> result = new ArrayList<SimpleLeg>();
		for (Leg leg : legs) {
			if (!transports.contains(leg.getTransport())) {
				String from = new String(leg.getFrom().getName());
				String to = new String(leg.getTo().getName());
				result.add(new SimpleLeg(from, to, leg.getTransport()));
				transports.add(leg.getTransport());

			}
		}

		return result;
	}

	/**
	 * Plan
	 * @param router String
	 * @param from String
	 * @param to String
	 * @param date String
	 * @param arrivalTime String
	 * @param departureTime String
	 * @param transportType TType
	 * @param routeType RType
	 * @param numOfItn String
	 * @param extra String
	 * @return
	 * @throws IOException
	 */
	public List<Itinerary> planTrip(String router, String from, String to, String date, String arrivalTime,
			String departureTime, TType transportType, RType routeType, Boolean wheelChair, String numOfItn,
			String... extra) throws IOException {

		List<Itinerary> output = new ArrayList<Itinerary>();

		Position fromPos = null;
		Position toPos = null;
		String maxWalkDistance = null;
		Long maxTotalWalkDistance = null;
		String extraTransport = "TRANSIT,WALK";
		Integer maxChanges = 3;

		if (extra.length > 0 && extra[0] != null) {
			maxWalkDistance = extra[0];
		}
		if (extra.length > 1 && extra[1] != null) {
			maxTotalWalkDistance = Long.parseLong(extra[1]);
		}
		if (extra.length > 2 && extra[2] != null) {
			extraTransport = extra[2];
		}
		if (extra.length > 3 && extra[3] != null) {
			maxChanges = Integer.parseInt(extra[3]);
		}

		if (from != null) {
			String[] point = from.split(",", 2);
			fromPos = new Position("", null, "", point[0], point[1]);
		}

		if (to != null) {
			String[] point = to.split(",", 2);
			toPos = new Position("", null, "", point[0], point[1]);
		}

		// mandatory condition
		if (fromPos != null && toPos != null) {

			HashMap<String, Object> userRequest = new HashMap<String, Object>();
			userRequest.put(Constants.SP_RQ_FROM, fromPos);
			userRequest.put(Constants.SP_RQ_TO, toPos);
			userRequest.put(Constants.SP_RQ_DATE, date);
			userRequest.put(Constants.SP_RQ_DEPTIME, departureTime);
			userRequest.put(Constants.SP_RQ_ARRTIME, arrivalTime);
			userRequest.put(Constants.SP_RQ_ROUTE_PREF, routeType);
			userRequest.put(Constants.SP_RQ_NUM_ITNS, numOfItn);
			userRequest.put(Constants.WHEELCHAIR,  wheelChair);

			if (maxWalkDistance != null) {
				userRequest.put("maxWalkDistance", maxWalkDistance);
			}
			if (extraTransport != null) {
				userRequest.put("extraTransport", extraTransport);
			}

			if (transportType.equals(TType.TRANSIT)) {
				Transit trans = new Transit(otpConnector, helper);
				output = trans.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.CAR)) {
				CarOnly carO = new CarOnly(router, otpConnector, geocodeAPIsManager, helper, routersMap,
						repositoryUtils);
				output = carO.getItinerary(router, userRequest);
				carO.completeLegs(output);

			} else if (transportType.equals(TType.BICYCLE)) {
				BicyleOnly bikeO = new BicyleOnly(otpConnector, helper);
				output = bikeO.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.SHAREDBIKE_WITHOUT_STATION)) {
				BikeRental bikeR = new BikeRental(otpConnector, helper, repositoryUtils);
				output = bikeR.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.SHAREDCAR_WITHOUT_STATION)) {
				CarRental carR = new CarRental(otpConnector, helper, repositoryUtils);
				output = carR.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.CARWITHPARKING)) {
				CarWithParkingPlaces carWPM = new CarWithParkingPlaces(otpConnector, geocodeAPIsManager, helper,
						routersMap, repositoryUtils);
				output = carWPM.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.SHAREDCAR)) {
				CarRentalViaStations carRVS = new CarRentalViaStations(otpConnector, helper, repositoryUtils);
				output = carRVS.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.SHAREDBIKE)) {
				BikeRentalViaStations bikeRVS = new BikeRentalViaStations(otpConnector, helper, repositoryUtils);
				output = bikeRVS.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.BUS)) {
				BusOnly busOnly = new BusOnly(otpConnector, helper);
				output = busOnly.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.TRAIN)) {
				TrainOnly trainOnly = new TrainOnly(otpConnector, helper);
				output = trainOnly.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.WALK)) {
				WalkOnly walkOnly = new WalkOnly(otpConnector, helper);
				output = walkOnly.getItinerary(router, userRequest);

			} else if (transportType.equals(TType.PARK_AND_RIDE)) {
				BikeRentalViaStations bikeR = new BikeRentalViaStations(otpConnector, helper, repositoryUtils);
				Transit trans = new Transit(otpConnector, helper);
				ParkAndRide parkAndRide = new ParkAndRide(otpConnector, geocodeAPIsManager, helper, bikeR, trans,
						routersMap, repositoryUtils);
				output = parkAndRide.getItinerary(router, userRequest);

			}
		}

		for (Itinerary it : output) {
			Leg lastLeg = it.getLeg().get(it.getLeg().size() - 1);
			it.setEndtime(lastLeg.getEndtime());
			it.setDuration(it.getEndtime() - it.getStartime());
		}

		Set<Itinerary> toRemove = Sets.newHashSet();
		for (Itinerary it : output) {
			double walk = 0;
			double changes = 0;
			for (Leg leg : it.getLeg()) {
				if (leg.getTransport().getType() == TType.WALK) {
					walk += leg.getLength();
				} else {
					changes++;
				}
			}
			if (maxTotalWalkDistance != null && walk > maxTotalWalkDistance) {
				toRemove.add(it);
			}
			if (maxChanges != null && changes > maxChanges) {
				toRemove.add(it);
			}
		}
		output.removeAll(toRemove);

		return output;
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}