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

package it.sayservice.platform.smartplanner.multimodal.modes;

import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.geocoder.GeocodeAPIsManager;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.StreetLocation;
import it.sayservice.platform.smartplanner.utils.CarStationComparator;
import it.sayservice.platform.smartplanner.utils.ComparatorUtils;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
import it.sayservice.platform.smartplanner.utils.ItineraryComparatorDuration;
import it.sayservice.platform.smartplanner.utils.ItineraryComparatorLegSize;
import it.sayservice.platform.smartplanner.utils.ItineraryComparatorWalk;
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * CAR WITH PARKING PLACE MODE.
 * 
 * @author nawazk
 *
 */
public class CarWithParkingPlaces {

	/** list of itineraries. **/
	private List<Itinerary> output = new ArrayList<Itinerary>();
	/** list of recurrent leg. **/
	private List<Leg> rjOutput = new ArrayList<Leg>();
	/** phase one output source->carstation. **/
	private List<Itinerary> stepOneOutput = new ArrayList<Itinerary>();
	/** phase two output carstation->destination. **/
	private List<Itinerary> stepTwoOutput = new ArrayList<Itinerary>();
	/** mongo template **/
	private MongoTemplate template;
	/** router mapper. **/
	private MongoRouterMapper routersMap;
	/** itinerary JSON parser. **/
	private ItineraryBuildHelper helper;
	/** connector. **/
	private OTPConnector otpConnector;
	/** geocoder api manager. **/
	private GeocodeAPIsManager apiManager;
	/** repository utils. **/
	private RepositoryUtils repositoryUtils;
	/** json mapper. **/
	ObjectMapper map = new ObjectMapper();

	/**
	 * 
	 * @param otpCon
	 * @param apisManager
	 * @param itnHelper
	 */
	public CarWithParkingPlaces(OTPConnector otpCon, GeocodeAPIsManager apisManager, ItineraryBuildHelper itnHelper,
			MongoRouterMapper routerMapper, RepositoryUtils repoUtils) {
		this.otpConnector = otpCon;
		this.helper = itnHelper;
		this.apiManager = apisManager;
		this.routersMap = routerMapper;
		this.repositoryUtils = repoUtils;
	}

	/**
	 * Constructor for testing.
	 */
	public CarWithParkingPlaces() {
	}

	/**
	 * Getter output.
	 * 
	 * @return List<Itinerary>
	 */
	public final List<Itinerary> getOutput() {
		return output;
	}

	/**
	 * 
	 * @param router
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public final List<Itinerary> getItinerary(String router, HashMap<String, Object> parameters) throws IOException {

		// define input map for OTP.
		Map<String, String> otpMap = new HashMap<String, String>();
		Map<String, String> otpMapCar = new HashMap<String, String>();
		// least changes preference.
		Boolean sortForLeastChanges = false;
		// least walking preference.
		Boolean sortForLeastWalking = false;
		// maximum stations.
		int stationIndex = 0;

		// define locations variable.
		List<CarStation> carStations = new ArrayList<CarStation>();
		// requested parameters
		String source = null;
		String destination = null;
		String date = null;
		String time = null;
		String arriveBy = "false";
		String optimize = Constants.OPTIMIZATION.QUICK.toString();
		String numItineraries = "3";
		// variables.
		String maxWalkDistance = Constants.LONG_WALK;
		double sX = 0;
		double sY = 0;
		double dX = 0;
		double dY = 0;
		/** parking station to destination. **/
		double distStationToTarget = 0;

		/** get list of nearest bike rental station locations. **/

		// clear output.
		output.clear();
		stepOneOutput.clear();
		stepTwoOutput.clear();

		// get source and destination
		if (parameters.get(Constants.SP_RQ_FROM) != null) {
			Position from = (Position) parameters.get(Constants.SP_RQ_FROM);
			if (from != null) {
				if (!from.getLat().isEmpty() && !from.getLon().isEmpty()) {
					source = from.getLon() + "," + from.getLat();
					// wrong name in inputs (lon refers to lat).
					sX = Double.parseDouble(from.getLon().toString());
					sY = Double.parseDouble(from.getLat().toString());
				} else if (!from.getStopCode().isEmpty()) {
					source = from.getStopCode();
				}
			} else {
			} // not defined from.
		}

		if (parameters.get(Constants.SP_RQ_TO) != null) {
			Position to = (Position) parameters.get(Constants.SP_RQ_TO);
			if (to != null) {
				if (!to.getLat().isEmpty() && !to.getLon().isEmpty()) {
					destination = to.getLon() + "," + to.getLat();
					dX = Double.parseDouble(to.getLon().toString());
					dY = Double.parseDouble(to.getLat().toString());
					carStations = repositoryUtils.findCarStationPositionByNear(router, new Point(dX, dY), parameters,
							false);
				} else if (!to.getStopCode().isEmpty()) {
					destination = to.getStopCode();
				}
			} else {
			} // not defined to.
		}

		if (parameters.get(Constants.SP_RQ_DATE) != null) {
			date = String.valueOf(parameters.get(Constants.SP_RQ_DATE));
		} else {
		} // not defined date.
		if (parameters.get(Constants.SP_RQ_ARRTIME) != null) {
			arriveBy = "true";
			time = String.valueOf(parameters.get(Constants.SP_RQ_ARRTIME));
		} else if (parameters.get(Constants.SP_RQ_DEPTIME) != null) {
			time = String.valueOf(parameters.get(Constants.SP_RQ_DEPTIME));
		}
		if (parameters.get(Constants.SP_RQ_ROUTE_PREF) != null) {
			RType preference = (RType) parameters.get(Constants.SP_RQ_ROUTE_PREF);
			if (preference.equals(RType.fastest)) {
				optimize = Constants.OPTIMIZATION.QUICK.toString();
			} else if (preference.equals(RType.greenest)) {
				optimize = Constants.OPTIMIZATION.GREENWAYS.toString();
			} else if (preference.equals(RType.safest)) {
				optimize = Constants.OPTIMIZATION.SAFE.toString();
			} else if (preference.equals(RType.leastChanges)) {
				sortForLeastChanges = true;
			} else if (preference.equals(RType.leastWalking)) {
				sortForLeastWalking = true;
			}
		}
		if (parameters.get(Constants.SP_RQ_NUM_ITNS) != null) {
			numItineraries = String.valueOf(parameters.get(Constants.SP_RQ_NUM_ITNS));
		}
		// maximum walking distance
		if (parameters.get(Constants.SP_RQ_MAXWALK) != null) {
			maxWalkDistance = String.valueOf(parameters.get(Constants.SP_RQ_MAXWALK));
		}
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}			

		/** invoke OTP **/

		Collections.sort(carStations, new CarStationComparator(helper, dX, dY));

		// invoke 'from' to list of all car station locations.
		for (CarStation cs : carStations) {

			distStationToTarget = helper.calculateHarvesineDistance(cs.getPosition()[0], cs.getPosition()[1], dX, dY);

			stationIndex++;
			// maximum number of parking itineraries.
			if (stationIndex > Integer.parseInt(numItineraries)) {
				break;
			}

			otpMapCar.put(Constants.OTP_RQ_DATE, date);
			otpMapCar.put(Constants.OTP_RQ_TIME, time);
			otpMapCar.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
			otpMapCar.put(Constants.OTP_RQ_OPTIMIZE, optimize);
			otpMapCar.put(Constants.OTP_RQ_ITNS, "1");
			otpMapCar.put(Constants.OTP_RQ_MAXWALK, "0");
			otpMapCar.put(Constants.OTP_RQ_MODE, Constants.MODES.CAR.toString());
			otpMapCar.put(Constants.OTP_RQ_FROM, source);
			otpMapCar.put(Constants.OTP_RQ_TO, cs.getPosition()[0] + "," + cs.getPosition()[1]);

			String responseOne = null;
			// if source is present within StreetLocation repository, get
			// suggested position
			StreetLocation existingLocation = repositoryUtils.existStreetLocation(router, sX, sY);
			if (existingLocation != null) {
				// update source.
				otpMapCar.put(Constants.OTP_RQ_FROM,
						existingLocation.getPosition()[0] + "," + existingLocation.getPosition()[1]);
				// connect and fetch data.
				responseOne = otpConnector.connect(router, otpMapCar);

			} else { // normal procedure as before.
				responseOne = otpConnector.connect(router, otpMapCar);
				// if response is empty.
				JsonNode rootNode = map.readValue(responseOne, JsonNode.class);
				JsonNode plan = rootNode.get("plan");
				if (plan == null) {
					// warning.
					System.err.println("plan is Null");
					// invoke google api to get nearby street.
					// prepare reqeust.
					String requestAPI = "location=" + sX + "," + sY + "&radius=50&sensor=false&types=route";
					Position suggestedPosition = apiManager.nearbySearchGoogle(router, requestAPI, null,
							MediaType.APPLICATION_JSON);
					// save suggested position in StreeLocation db.
					if (suggestedPosition != null) {
						StreetLocation temp = new StreetLocation(suggestedPosition.getStopId().getId(),
								suggestedPosition.getStopCode(), suggestedPosition.getName(), sX, sY,
								Double.valueOf(suggestedPosition.getLat()), Double.valueOf(suggestedPosition.getLon()));
						routersMap.getStreetLocationRepository(router).save(temp);
						// update source
						otpMapCar.put(Constants.OTP_RQ_FROM,
								suggestedPosition.getLat() + "," + suggestedPosition.getLon());
						// connect and fetch data.
						responseOne = otpConnector.connect(router, otpMapCar);
					} else {
						System.err.println("No suggestion from google api for point " + sX + ", " + sY);
					}
				}
			}

			// takes station and mode information
			// to be used later.
			HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
			preProcessParams.put(Constants.SP_RQ_TOSTATION, cs);
			preProcessParams.put(Constants.SP_RQ_USERMODE, TType.CARWITHPARKING.name());
			preProcessParams.put(Constants.ROUTER, router);

			// process data to smart planner format
			stepOneOutput = generatePlan(router, responseOne, preProcessParams);

			// create map for otp invocation from bike station to 'To'
			// invoke OTP and get List Itinerary step2
			otpMap.put(Constants.OTP_RQ_DATE, date);
			otpMap.put(Constants.OTP_RQ_TIME, time);
			otpMap.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
			otpMap.put(Constants.OTP_RQ_OPTIMIZE, optimize);
			otpMap.put(Constants.OTP_RQ_ITNS, "1");
			otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
			otpMap.put(Constants.OTP_RQ_MODE, parameters.containsKey("extraTransport")
					? (String) parameters.get("extraTransport") : Constants.MODES.TRANSIT.toString());
			otpMap.put(Constants.OTP_RQ_FROM, cs.getPosition()[0] + "," + cs.getPosition()[1]);
			otpMap.put(Constants.OTP_RQ_TO, destination);

			// update time with end time of first trip.
			for (Itinerary it : stepOneOutput) {
				otpMap.put(Constants.OTP_RQ_TIME,
						helper.convertMillisToTime(it.getEndtime()).replaceAll("\\s", "").toLowerCase());
				otpMap.put(Constants.OTP_RQ_DATE,
						helper.convertMillisToDate(it.getEndtime()).replaceAll("\\s", "").toLowerCase());
				// connect and fetch data.
				String response2 = otpConnector.connect(router, otpMap);
				// process data to smart planner format
				stepTwoOutput = generatePlan(router, response2, preProcessParams);

				// delayed plan check.
				if (distStationToTarget < (Double.valueOf(maxWalkDistance) / 1000)) {
					Collections.sort(stepTwoOutput, new ItineraryComparatorDuration());
					// if duration > 1 hr.
					if (!stepTwoOutput.isEmpty() && stepTwoOutput.get(0).getDuration() > 3600000) {
						// wrong solution, plan a walk journey.
						otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.WALK.toString());
						// connect and fetch data.
						response2 = otpConnector.connect(router, otpMap);
						// process data to smart planner format
						stepTwoOutput = generatePlan(router, response2, preProcessParams);
					}
				}

				// append legs of step2 to step1 and update other parameters
				output = postProcess(stepOneOutput, stepTwoOutput, output);
			}

		}

		/*
		 * // call normal car mode in case, // 1. no parking station found // 2.
		 * path not found exception between // (source ->car parking || car
		 * parking -> destination). if (carStations.isEmpty() &&
		 * output.isEmpty()) { otpMapCar.put("date", date);
		 * otpMapCar.put("time", time); otpMapCar.put("arriveBy", arriveBy);
		 * otpMapCar.put("optimize", "QUICK"); otpMapCar.put("numItineraries",
		 * "1"); otpMapCar.put("maxWalkDistance", "0"); otpMapCar.put("mode",
		 * "CAR"); otpMapCar.put("fromPlace", source); otpMapCar.put("toPlace",
		 * destination);
		 * 
		 * // connect and fetch data. String response =
		 * otpConnector.connect(otpMapCar); // takes station and mode
		 * information // to be used later. HashMap<String, Object>
		 * preProcessParams = (HashMap<String, Object>) parameters.clone();
		 * preProcessParams.put("userMode", TType.CAR.name()); // process data
		 * to smart planner format output = generatePlan(response,
		 * preProcessParams); }
		 */

		// least changes sorting.
		if (sortForLeastChanges) {
			Collections.sort(output, new ItineraryComparatorLegSize());
		}
		// least walk sorting.
		if (sortForLeastWalking) {
			Collections.sort(output, new ItineraryComparatorWalk());
		}
		// fix walk leg position strings.
		helper.fixNameString(output);

		return output;

	}

	/**
	 * 
	 * @param stepOneOutput
	 * @param stepTwoOutput
	 * @param output
	 * @return
	 */
	private List<Itinerary> postProcess(List<Itinerary> stepOneOutput, List<Itinerary> stepTwoOutput,
			List<Itinerary> output) {

		// TODO Auto-generated method stub
		for (Itinerary it : stepOneOutput) {

			for (Itinerary it2 : stepTwoOutput) {

				Itinerary temp = new Itinerary();
				List<Leg> tmpLeg = new ArrayList<Leg>();
				temp.setFrom(it.getFrom());
				temp.setTo(it2.getTo());
				temp.setDuration(it.getDuration() + it2.getDuration());
				temp.setWalkingDuration(it.getWalkingDuration() + it2.getWalkingDuration());
				temp.setStartime(it.getStartime());
				temp.setEndtime(it.getEndtime() + it2.getDuration());

				for (Leg stepOneLeg : it.getLeg()) {
					tmpLeg.add(stepOneLeg);
					helper.convertMillisToTime(stepOneLeg.getStartime());
					// helper.convertMillisToTime(stepOneLeg.getEndtime());
				}
				for (Leg stepTwoLeg : it2.getLeg()) {
					tmpLeg.add(stepTwoLeg);
				}

				temp.setLeg(tmpLeg);
				output.add(temp);
			}
		}

		return output;

	}

	/**
	 * 
	 * @param responseString
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	private List<Itinerary> generatePlan(String router, String responseString, HashMap<String, Object> parameters)
			throws IOException {

		List<Itinerary> tempOutput = null;
		List<Itinerary> processedData = new ArrayList<Itinerary>();
		JsonNode rootNode;

		rootNode = map.readValue(responseString, JsonNode.class);
		tempOutput = helper.buildItineraries(rootNode, parameters);

		// post process with real time alerts.
		processedData = helper.filterWithAlerts(router, tempOutput, parameters);

		return processedData;
	}

	/**
	 * 
	 * @param router
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public List<Leg> getLegs(String router, HashMap<String, Object> parameters) throws IOException {

		// definitions
		Long fromDate;
		Long toDate;
		Long requestedInterval = new Long(0);
		String wednesday = null;
		List<Leg> temp = new ArrayList<Leg>();
		rjOutput.clear();

		if (parameters.get(Constants.SP_RQ_FROMDATE) != null && parameters.get(Constants.SP_RQ_TODATE) != null) {
			fromDate = (Long) parameters.get(Constants.SP_RQ_FROMDATE);
			toDate = (Long) parameters.get(Constants.SP_RQ_TODATE);
			Date startDate = new Date(fromDate);
			Date endDate = new Date(toDate);

			long endTime = endDate.getTime();
			long curTime = startDate.getTime();
			requestedInterval = (Long) parameters.get(Constants.SP_RQ_INTERVAL);

			HashMap<String, Object> userRequestWD = new HashMap<String, Object>();
			userRequestWD.put(Constants.SP_RQ_FROM, parameters.get(Constants.SP_RQ_FROM));
			userRequestWD.put(Constants.SP_RQ_TO, parameters.get(Constants.SP_RQ_TO));
			userRequestWD.put(Constants.SP_RQ_DATE, wednesday);
			userRequestWD.put(Constants.SP_RQ_DEPTIME, parameters.get(Constants.SP_RQ_TIME));
			userRequestWD.put(Constants.SP_RQ_ROUTE_PREF, parameters.get(Constants.SP_RQ_ROUTE_PREF));
			userRequestWD.put(Constants.SP_RQ_NUM_ITNS, 1);

			List<String> reqs = RecurrentUtil.computeParameters(curTime, endTime,
					(String) parameters.get(Constants.SP_RQ_RECURRENCE));

			for (String req : reqs) {
				userRequestWD.put(Constants.SP_RQ_DATE, req);
				temp.addAll(getRecurrLegsWithinPeriod(router, userRequestWD, requestedInterval));
			}
		}

		rjOutput.addAll(temp);

		return rjOutput;

	}

	/**
	 * 
	 * @param router
	 * @param parameters
	 * @param interval
	 * @return
	 * @throws IOException
	 */
	private List<Leg> getRecurrLegsWithinPeriod(String router, HashMap<String, Object> parameters, Long interval)
			throws IOException {

		Boolean search = true;
		List<Itinerary> tempList = new ArrayList<Itinerary>();
		List<Itinerary> finalList = new ArrayList<Itinerary>();
		List<Leg> recurrLegs = new ArrayList<Leg>();
		// create local copy of parameters to prevent original modification.
		HashMap<String, Object> localParams = (HashMap<String, Object>) parameters.clone();
		DateFormat timeFormatter = new SimpleDateFormat("hh:mmaa", Locale.ITALY);
		DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ITALY);

		try {
			// get maxReachTime in milliseconds.
			String requestedTime = localParams.get("date") + " " + localParams.get(Constants.SP_RQ_DEPTIME);
			DateFormat maxTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ITALY);
			Date reccurReqTime = (Date) maxTimeformatter.parse(requestedTime);

			// run while loop with search flag.
			while (search) {
				// get itinerary with time = starting time
				// and number of itns = 1.
				List<Itinerary> searchItn = getItinerary(router, localParams);
				// add itn to return list
				tempList.addAll(searchItn);
				// check if the last itn endTime is less than
				// mustReachTime.
				if (searchItn.isEmpty() || searchItn.size() < 1) {
					search = false;
					break;
				} else if (searchItn.get(searchItn.size() - 1).getEndtime() >= (reccurReqTime.getTime() + interval)) {
					search = false;
					break;
				} else {
					// search again with with time = endTime of last itinerary
					// and num of Itn = 1
					Long time = searchItn.get(searchItn.size() - 1).getEndtime();
					Date strDate = new Date(time);
					localParams.put("date", dateFormatter.format(strDate));
					localParams.put(Constants.SP_RQ_DEPTIME, timeFormatter.format(strDate));
				}
			}

			// remove the ones after the maxReachTime.
			finalList.addAll(tempList);
			for (Itinerary it : tempList) {
				if (it.getEndtime() > (reccurReqTime.getTime() + interval)) {
					finalList.remove(it);
				}
			}

			// get legs.
			/** post process itineraries and get legs **/
			for (Itinerary it : finalList) {
				for (Leg leg : it.getLeg()) {
					if (!(leg.getTransport().getType().equals(TType.WALK))) {
						if (!recurrLegs.contains(leg) && (leg.getStartime() <= (reccurReqTime.getTime() + interval))) {
							recurrLegs.add(leg);
						}
					}

				}
			}

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// order list by startTime.
		Collections.sort(recurrLegs, new ComparatorUtils());

		return recurrLegs;
	}

}
