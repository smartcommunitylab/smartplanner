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

import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.model.CarStation;
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

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.geo.Point;

/**
 * Car Rental mode.
 * 
 * @author nawazk
 * 
 */
public class CarRental {

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
	/** connector. **/
	private OTPConnector otpConnector;
	/** repository utils. **/
	private RepositoryUtils repositoryUtils;

	/**
	 * Constructor for testing.
	 */
	public CarRental() {
	}

	/**
	 * 
	 * @param rm
	 * @param otpCon
	 * @param itnHelper
	 */
	public CarRental(OTPConnector otpCon, ItineraryBuildHelper itnHelper, RepositoryUtils repoUtils) {
		this.otpConnector = otpCon;
		this.helper = itnHelper;
		this.repositoryUtils = repoUtils;
	}

	/**
	 * Getter output.
	 * @return List<Itinerary>
	 */
	public final List<Itinerary> getOutput() {
		return output;
	}

	/**
	 * Get Itinerary.
	 * @param router
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public List<Itinerary> getItinerary(String router, HashMap<String, Object> parameters) throws IOException {

		/**
		 * search a car rental station near to source using geo spatial
		 * calculation after validation(w.r.t to rental cars). check if the
		 * distance (using Harvesine formula) between source to station is
		 * greater than the distance from source to destination | '- Yes (give a
		 * transit plan from source to destination) | |- No (give rental plan
		 * from source to destination) | | | |- get transit plan from source to
		 * station. | |- get a car plan from station to destination `- merge the
		 * two plans to create one itinerary as output.
		 */

		// define input map for OTP.
		Map<String, String> otpMap = new HashMap<String, String>();
		// define locations variable.
		List<CarStation> carStations = new ArrayList<CarStation>();
		// least changes preference.
		Boolean sortForLeastChanges = false;
		// least walking preference.
		Boolean sortForLeastWalking = false;
		// requested source and destination
		String source = null;
		String destination = null;
		String date = null;
		String time = null;
		String arriveBy = "false";
		String optimize = Constants.OPTIMIZATION.QUICK.toString();
		String numItineraries = "3";
		String maxWalkDistance = Constants.LONG_WALK;
		double sX = 0;
		double sY = 0;
		double dX = 0;
		double dY = 0;
		double distToStation = 0;
		double distToDestination = 0;

		/** get list of nearest car rental station locations. **/

		// clear output.
		output.clear();
		stepOneOutput.clear();
		stepTwoOutput.clear();
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}			

		// get source and destination
		if (parameters.get(Constants.SP_RQ_FROM) != null) {
			Position from = (Position) parameters.get(Constants.SP_RQ_FROM);
			if (from != null) {
				if (!from.getLat().isEmpty() && !from.getLon().isEmpty()) {
					source = from.getLon() + "," + from.getLat();
					// wrong name in inputs (lon refers to lat).
					sX = Double.parseDouble(from.getLon().toString());
					sY = Double.parseDouble(from.getLat().toString());
					// search for nearby car station based on number of
					// available cars.
					carStations = repositoryUtils.findCarRentalStationPositionByNear(router, new Point(sX, sY), true,
							parameters);
					Collections.sort(carStations, new CarStationComparator(helper, sX, sY));
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

		if ((sX != 0 & sY != 0) && (dX != 0 & dY != 0)) {
			// distance from source to target.
			distToDestination = helper.calculateHarvesineDistance(sX, sY, dX, dY);
			if (carStations.size() > 0) {
				CarStation cs = carStations.get(0);
				/** perform analysis **/
				// distance from source to station.
				distToStation = helper.calculateHarvesineDistance(sX, sY, cs.getPosition()[0], cs.getPosition()[1]);
				if (distToDestination < distToStation) {
					/** get transit plan. **/
					otpMap.put(Constants.OTP_RQ_DATE, date);
					otpMap.put(Constants.OTP_RQ_TIME, time);
					otpMap.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
					otpMap.put(Constants.OTP_RQ_OPTIMIZE, optimize);
					otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);
					otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
					otpMap.put(Constants.OTP_RQ_MODE, parameters.containsKey("extraTransport")
							? (String) parameters.get("extraTransport") : Constants.MODES.TRANSIT.toString());
					otpMap.put(Constants.OTP_RQ_FROM, source);
					otpMap.put(Constants.OTP_RQ_TO, destination);
					// connect and fetch data.
					String response = otpConnector.connect(router, otpMap);
					HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
					preProcessParams.put(Constants.SP_RQ_USERMODE, TType.TRANSIT.name());
					preProcessParams.put(Constants.ROUTER, router);
					output = generatePlan(router, response, preProcessParams);
					// delayed plan check.
					if (distToDestination < (Double.valueOf(maxWalkDistance) / 1000)) {
						Collections.sort(output, new ItineraryComparatorDuration());
						// if duration > 1 hr.
						if (!output.isEmpty() && output.get(0).getDuration() > 3600000) {
							// wrong solution, plan a walk journey.
							otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.WALK.toString());
							// connect and fetch data.
							response = otpConnector.connect(router, otpMap);
							// process data to smart planner format
							output = generatePlan(router, response, preProcessParams);
						}
					}
				} else {
					/** get rental plan. **/
					// create map for otp invocation
					// invoke OTP and get List Itinerary step1
					// default values for OTP map
					otpMap.put(Constants.OTP_RQ_DATE, date);
					otpMap.put(Constants.OTP_RQ_TIME, time);
					otpMap.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
					otpMap.put(Constants.OTP_RQ_OPTIMIZE, optimize);
					otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);
					otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
					otpMap.put(Constants.OTP_RQ_MODE, parameters.containsKey("extraTransport")
							? (String) parameters.get("extraTransport") : Constants.MODES.TRANSIT.toString());
					otpMap.put(Constants.OTP_RQ_FROM, source);
					otpMap.put(Constants.OTP_RQ_TO, cs.getPosition()[0] + "," + cs.getPosition()[1]);
					// connect and fetch data.
					String responseOne = otpConnector.connect(router, otpMap);
					// takes station and mode information
					// to be used later.
					HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
					preProcessParams.put(Constants.SP_RQ_FROMSTATION, cs);
					preProcessParams.put(Constants.SP_RQ_USERMODE, TType.SHAREDCAR_WITHOUT_STATION.name());
					preProcessParams.put(Constants.ROUTER, router);
					// process data to smart planner format
					stepOneOutput = generatePlan(router, responseOne, preProcessParams);
					// delayed plan check.
					if (distToStation < (Double.valueOf(maxWalkDistance) / 1000)) {
						Collections.sort(stepOneOutput, new ItineraryComparatorDuration());
						// if duration > 1 hr.
						if (!stepOneOutput.isEmpty() && stepOneOutput.get(0).getDuration() > 3600000) {
							// wrong solution, plan a walk journey.
							otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.WALK.toString());
							// connect and fetch data.
							responseOne = otpConnector.connect(router, otpMap);
							// process data to smart planner format
							stepOneOutput = generatePlan(router, responseOne, preProcessParams);
						}
					}

					// create map for otp invocation from car station to 'To'
					// invoke OTP and get List Itinerary step2
					otpMap.put(Constants.OTP_RQ_ITNS, "1");
					otpMap.put(Constants.OTP_RQ_MAXWALK, Constants.NO_WALK);
					otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.CAR.toString());
					otpMap.put(Constants.OTP_RQ_FROM, cs.getPosition()[0] + "," + cs.getPosition()[1]);
					otpMap.put(Constants.OTP_RQ_TO, destination);
					// connect and fetch data.
					String response2 = otpConnector.connect(router, otpMap);
					// process data to smart planner format
					stepTwoOutput = generatePlan(router, response2, preProcessParams);
					// append legs of step2 to step1 and update other parameters
					output = postProcess(stepOneOutput, stepTwoOutput, output);
				}
			} else { // in case no car station is present, suggest transit plan
				/** get transit plan. **/
				otpMap.put(Constants.OTP_RQ_DATE, date);
				otpMap.put(Constants.OTP_RQ_TIME, time);
				otpMap.put(Constants.OTP_RQ_ARRIVEBY, arriveBy);
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, optimize);
				otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);
				otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
				otpMap.put(Constants.OTP_RQ_MODE, parameters.containsKey("extraTransport")
						? (String) parameters.get("extraTransport") : Constants.MODES.TRANSIT.toString());
				otpMap.put(Constants.OTP_RQ_FROM, source);
				otpMap.put(Constants.OTP_RQ_TO, destination);
				// connect and fetch data.
				String response = otpConnector.connect(router, otpMap);
				HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
				preProcessParams.put(Constants.SP_RQ_USERMODE, TType.TRANSIT.name());
				preProcessParams.put(Constants.ROUTER, router);
				output = generatePlan(router, response, preProcessParams);
				// delayed plan check.
				if (distToDestination < (Double.valueOf(maxWalkDistance) / 1000)) {
					Collections.sort(output, new ItineraryComparatorDuration());
					// if duration > 1 hr.
					if (!output.isEmpty() && output.get(0).getDuration() > 3600000) {
						// wrong solution, plan a walk journey.
						otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.WALK.toString());
						// connect and fetch data.
						response = otpConnector.connect(router, otpMap);
						// process data to smart planner format
						output = generatePlan(router, response, preProcessParams);
					}
				}
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

		return output;
	}

	/**
	 * Post Process Itineraries.
	 * @param stepOneOutput
	 * @param stepTwoOutput
	 * @param output
	 * @return
	 */
	private List<Itinerary> postProcess(List<Itinerary> stepOneOutput, List<Itinerary> stepTwoOutput,
			List<Itinerary> output) {

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
				temp.setFrom(it.getFrom());
				temp.setTo(it2.getTo());
				temp.setDuration(it.getDuration() + it2.getDuration());
				temp.setStartime(it.getStartime());
				temp.setEndtime(it.getEndtime() + (it2.getDuration() * 1000));
				Position[] greenPos = null;

				for (Leg stepOneLeg : it.getLeg()) {
					tmpLeg.add(stepOneLeg);
				}
				for (Leg stepTwoLeg : it2.getLeg()) {
					Leg step2LegCopy = new Leg();
					// deep copy.
					step2LegCopy.setAlertDelayList(stepTwoLeg.getAlertDelayList());
					step2LegCopy.setAlertParkingList(stepTwoLeg.getAlertParkingList());
					step2LegCopy.setAlertStrikeList(stepTwoLeg.getAlertStrikeList());
					step2LegCopy.setAlertRoadList(stepTwoLeg.getAlertRoadList());
					step2LegCopy.setAlertAccidentList(stepTwoLeg.getAlertAccidentList());
					step2LegCopy.setDuration(stepTwoLeg.getDuration());
					step2LegCopy.setLength(stepTwoLeg.getLength());
					step2LegCopy.setEndtime(stepTwoLeg.getEndtime() + it.getDuration() * 1000);
					step2LegCopy.setFrom(stepTwoLeg.getFrom());
					step2LegCopy.setLegGeometery(stepTwoLeg.getLegGeometery());
					step2LegCopy.setLegId(stepTwoLeg.getLegId());
					step2LegCopy.setStartime(stepTwoLeg.getStartime() + it.getDuration() * 1000);
					step2LegCopy.setTo(stepTwoLeg.getTo());
					step2LegCopy.setTransport(stepTwoLeg.getTransport());

					step2LegCopy.getTransport().setAgencyId(stepTwoLeg.getFrom().getStopId().getAgencyId());

					step2LegCopy.setStartime(it.getEndtime());
					step2LegCopy.setEndtime(it.getEndtime() + (stepTwoLeg.getDuration() * 1000));

					tmpLeg.add(step2LegCopy);
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

		ObjectMapper map = new ObjectMapper();
		JsonNode rootNode;

		rootNode = map.readValue(responseString, JsonNode.class);
		tempOutput = helper.buildItineraries(rootNode, parameters);

		// post process with real time alerts.
		processedData = helper.filterWithAlerts(router, tempOutput, parameters);

		return processedData;
	}

	/**
	 * Recurrent Journey Legs Generator.
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
					(String) parameters.get("recurrence"));

			for (String req : reqs) {
				userRequestWD.put(Constants.SP_RQ_DATE, req);
				temp.addAll(getRecurrLegsWithinPeriod(router, userRequestWD, requestedInterval));
			}
		}

		rjOutput.addAll(temp);

		return rjOutput;

	}

	/**
	 * Get Recurrent Legs within Period.
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
