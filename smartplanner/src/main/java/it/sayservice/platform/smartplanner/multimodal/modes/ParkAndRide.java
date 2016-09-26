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
import it.sayservice.platform.smartplanner.mongo.repos.StreetLocationRepository;
import it.sayservice.platform.smartplanner.utils.CarStationComparator;
import it.sayservice.platform.smartplanner.utils.ComparatorUtils;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
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

/**
 * CAR WITH PARKING PLACE MODE.
 * 
 * @author nawazk
 * 
 */
public class ParkAndRide {

	/** list of itineraries. **/
	private List<Itinerary> output = new ArrayList<Itinerary>();
	/** list of recurrent leg. **/
	private List<Leg> rjOutput = new ArrayList<Leg>();
	/** phase one output source->carstation. **/
	private List<Itinerary> stepOneOutput = new ArrayList<Itinerary>();
	/** phase two output carstation->destination. **/
	private List<Itinerary> stepTwoOutput = new ArrayList<Itinerary>();
	/** itinerary JSON parser. **/
	private ItineraryBuildHelper helper;
	/** bike rental **/
	private BikeRentalViaStations bikeRental;
	/** transit **/
	private Transit transit;
	/** connector. **/
	private OTPConnector otpConnector;
	/** geocoder api manager. **/
	private GeocodeAPIsManager apiManager;
	/** json mapper. **/
	private ObjectMapper map = new ObjectMapper();
	/** repository utils. **/
	private RepositoryUtils repositoryUtils;
	/** router mapper. **/
	private MongoRouterMapper mongoRouterMapper;

	/**
	 * 
	 * @param rm
	 * @param otpCon
	 * @param apisManager
	 * @param itnHelper
	 * @param bikeRental
	 * @param transit
	 */
	public ParkAndRide(OTPConnector otpCon, GeocodeAPIsManager apisManager, ItineraryBuildHelper itnHelper,
			BikeRentalViaStations bikeRental, Transit transit, MongoRouterMapper routerMapper,
			RepositoryUtils repoUtils) {
		this.otpConnector = otpCon;
		this.helper = itnHelper;
		this.apiManager = apisManager;
		this.bikeRental = bikeRental;
		this.transit = transit;
		this.mongoRouterMapper = routerMapper;
		this.repositoryUtils = repoUtils;
	}

	/**
	 * Constructor for testing.
	 */
	public ParkAndRide() {
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
		HashMap<String, Object> otpMap = new HashMap<String, Object>();
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
		String optimize = "QUICK";
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
		if (parameters.get("from") != null) {
			Position from = (Position) parameters.get("from");
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

		if (parameters.get("to") != null) {
			Position to = (Position) parameters.get("to");
			if (to != null) {
				if (!to.getLat().isEmpty() && !to.getLon().isEmpty()) {
					destination = to.getLon() + "," + to.getLat();
					dX = Double.parseDouble(to.getLon().toString());
					dY = Double.parseDouble(to.getLat().toString());
					carStations = repositoryUtils.findCarStationPositionByNear(router, new Point(dX, dY), parameters,
							true);
					Collections.sort(carStations, new CarStationComparator(helper, dX, dY));
					List<CarStation> toRemove = new ArrayList<CarStation>();
					for (CarStation cs : carStations) {
						if (!cs.isParkAndRide()) {
							toRemove.add(cs);
						}
					}
					carStations.removeAll(toRemove);
				} else if (!to.getStopCode().isEmpty()) {
					destination = to.getStopCode();
				}
			} else {
			} // not defined to.
		}

		if (parameters.get("date") != null) {
			date = String.valueOf(parameters.get("date"));
		} else {
			// not defined date.
		}
		if (parameters.get("arrivalTime") != null) {
			arriveBy = "true";
			time = String.valueOf(parameters.get("arrivalTime"));
		} else if (parameters.get(Constants.SP_RQ_DEPTIME) != null) {
			time = String.valueOf(parameters.get(Constants.SP_RQ_DEPTIME));
		}
		if (parameters.get("routePreferences") != null) {
			RType preference = (RType) parameters.get("routePreferences");
			if (preference.equals(RType.fastest)) {
				optimize = "QUICK";
			} else if (preference.equals(RType.greenest)) {
				optimize = "GREENWAYS";
			} else if (preference.equals(RType.safest)) {
				optimize = "SAFE";
			} else if (preference.equals(RType.leastChanges)) {
				sortForLeastChanges = true;
			} else if (preference.equals(RType.leastWalking)) {
				sortForLeastWalking = true;
			}
		}
		if (parameters.get("numItineraries") != null) {
			numItineraries = String.valueOf(parameters.get("numItineraries"));
		}
		// maximum walking distance
		if (parameters.get("maxWalkDistance") != null) {
			maxWalkDistance = String.valueOf(parameters.get("maxWalkDistance"));
		}
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}		

		/** invoke OTP **/

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
			otpMapCar.put(Constants.OTP_RQ_OPTIMIZE, "QUICK");
			otpMapCar.put(Constants.OTP_RQ_ITNS, "1");
			otpMapCar.put(Constants.OTP_RQ_MAXWALK, "0");
			otpMapCar.put(Constants.OTP_RQ_MODE, "CAR");
			otpMapCar.put(Constants.OTP_RQ_FROM, source);
			otpMapCar.put(Constants.OTP_RQ_TO, cs.getPosition()[0] + "," + cs.getPosition()[1]);


			String responseOne = null;
			// if source is present within StreetLocation repository, get
			// suggested position
			StreetLocation existingLocation = repositoryUtils.existStreetLocation(router, sX, sY);
			if (existingLocation != null) {
				// update source.
				otpMapCar.put(Constants.OTP_RQ_FROM, existingLocation.getPosition()[0] + "," + existingLocation.getPosition()[1]);
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
					StreetLocationRepository streetLocationRepository = mongoRouterMapper
							.getStreetLocationRepository(router);
					// save suggested position in StreeLocation db.
					if (suggestedPosition != null && streetLocationRepository != null) {
						StreetLocation temp = new StreetLocation(suggestedPosition.getStopId().getId(),
								suggestedPosition.getStopCode(), suggestedPosition.getName(), sX, sY,
								Double.valueOf(suggestedPosition.getLat()), Double.valueOf(suggestedPosition.getLon()));
						streetLocationRepository.save(temp);
						// update source
						otpMapCar.put("fromPlace", suggestedPosition.getLat() + "," + suggestedPosition.getLon());
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
			otpMap.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
			otpMap.put(Constants.OTP_RQ_OPTIMIZE, optimize);
			otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);
			otpMap.put(Constants.SP_RQ_MAXWALK, maxWalkDistance);
			otpMap.put(Constants.OTP_RQ_MODE, parameters.containsKey("extraTransport") ? (String) parameters.get("extraTransport")
					: Constants.MODES.TRANSIT.toString());
			otpMap.put(Constants.SP_RQ_FROM,
					new Position("", cs.getStationId(), "", "" + cs.getPosition()[0], "" + cs.getPosition()[1]));
			otpMap.put(Constants.SP_RQ_TO, (Position) parameters.get("to"));

			// update time with end time of first trip.
			for (Itinerary it : stepOneOutput) {
				String newTime = helper.convertMillisToTime(it.getEndtime()).replaceAll("\\s", "").toLowerCase();
				otpMap.put(Constants.SP_RQ_TIME, newTime);
				otpMap.put(Constants.SP_RQ_DEPTIME, newTime);
				otpMap.put(Constants.SP_RQ_DATE, helper.convertMillisToDate(it.getEndtime()).replaceAll("\\s", "").toLowerCase());

				Leg leg = it.getLeg().get(it.getLeg().size() - 1);

				stepTwoOutput = bikeRental.getItinerary(router, otpMap);
				List<Itinerary> stepTwoOutput2 = transit.getItinerary(router, otpMap);
				stepTwoOutput.addAll(stepTwoOutput2);
				// stepTwoOutput = stepTwoOutput2;

				// append legs of step2 to step1 and update other parameters
				output = postProcess(stepOneOutput, stepTwoOutput, output);
			}

		}

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
		// helper.removeShortWalksElements(output);

		return output;

	}

	/**
	 * Post process without leg modification.
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
				List<Leg> tmpLeg = new ArrayList<Leg>();
				Itinerary temp = new Itinerary();
				temp.setFrom(it.getFrom());
				temp.setTo(it2.getTo());
				temp.setDuration(it.getDuration() + it2.getDuration());
				temp.setWalkingDuration(it.getWalkingDuration() + it2.getWalkingDuration());
				temp.setStartime(it.getStartime());
				temp.setEndtime(it2.getEndtime());

				for (Leg stepOneLeg : it.getLeg()) {
					tmpLeg.add(stepOneLeg);
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
	 * Generate formatted itinerary.
	 * @param router
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
	 * Recurrent leg generator.
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
		String sunday = null;
		List<Leg> temp = new ArrayList<Leg>();
		rjOutput.clear();

		if (parameters.get("fromDate") != null && parameters.get("toDate") != null) {
			fromDate = (Long) parameters.get("fromDate");
			toDate = (Long) parameters.get("toDate");
			Date startDate = new Date(fromDate);
			Date endDate = new Date(toDate);

			long endTime = endDate.getTime();
			long curTime = startDate.getTime();
			requestedInterval = (Long) parameters.get("interval");

			HashMap<String, Object> userRequestWD = new HashMap<String, Object>();
			userRequestWD.put("from", parameters.get("from"));
			userRequestWD.put("to", parameters.get("to"));
			userRequestWD.put("date", wednesday);
			userRequestWD.put(Constants.SP_RQ_DEPTIME, parameters.get("time"));
			userRequestWD.put("routePreferences", parameters.get("routePreferences"));
			userRequestWD.put("numItineraries", 1);

			List<String> reqs = RecurrentUtil.computeParameters(curTime, endTime,
					(String) parameters.get("recurrence"));

			for (String req : reqs) {
				userRequestWD.put("date", req);
				temp.addAll(getRecurrLegsWithinPeriod(router, userRequestWD, requestedInterval));
			}
		}

		rjOutput.addAll(temp);
		return rjOutput;
	}

	/**
	 * Get recurrent legs within period.
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
