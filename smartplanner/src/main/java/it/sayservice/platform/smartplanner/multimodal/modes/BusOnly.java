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
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
import it.sayservice.platform.smartplanner.utils.ItineraryComparatorLegSize;
import it.sayservice.platform.smartplanner.utils.ItineraryComparatorWalk;
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;

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

/**
 * Bus Only independant mode.
 * 
 * @author nawazk
 * 
 */
public class BusOnly {

	/** list of itineraries. **/
	private List<Itinerary> output;
	/** list of recurrent legs. **/
	private List<Leg> rjOutput = new ArrayList<Leg>();
	/** Itinerary build helper. **/
	private ItineraryBuildHelper helper;
	/** connector object for OTP. **/
	private OTPConnector otpConnector;

	/**
	 * 
	 * @param otpCon
	 * @param itnHelper
	 */
	public BusOnly(OTPConnector otpCon, ItineraryBuildHelper itnHelper) {
		this.otpConnector = otpCon;
		this.helper = itnHelper;
	}

	/**
	 * constructor for testing.
	 */
	public BusOnly() {
	}

	/**
	 * Getter of List.
	 * 
	 * @return output List<Itinerary>
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
	public final List<Itinerary> getItinerary(String router, final HashMap<String, Object> parameters) throws IOException {

		// define input map for OTP.
		Map<String, String> otpMap = new HashMap<String, String>();
		// least changes preference.
		Boolean sortForLeastChanges = false;
		// least walking preference.
		Boolean sortForLeastWalking = false;

		// default values for OTP map
		otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.BUS.toString());
		otpMap.put(Constants.OTP_RQ_MAXWALK, Constants.TRANSIT_WALK);
		otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.QUICK.toString());
		otpMap.put(Constants.OTP_RQ_ITNS, "3");
		otpMap.put(Constants.OTP_RQ_ARRIVEBY, "false");

		// get SmartUser requirements, map it to OTP format.
		if (parameters.get(Constants.SP_RQ_FROM) != null) {
			Position from = (Position) parameters.get(Constants.SP_RQ_FROM);
			if (from != null) {
				if (!from.getLat().isEmpty() && !from.getLon().isEmpty()) {
					otpMap.put(Constants.OTP_RQ_FROM, from.getLon() + "," + from.getLat());
				} else if (!from.getStopCode().isEmpty()) {
					otpMap.put(Constants.OTP_RQ_FROM, from.getStopCode());
				}
			} else {
				// not defined from.
			}
		}

		if (parameters.get(Constants.SP_RQ_TO) != null) {
			Position to = (Position) parameters.get(Constants.SP_RQ_TO);
			if (to != null) {
				if (!to.getLat().isEmpty() && !to.getLon().isEmpty()) {
					otpMap.put(Constants.OTP_RQ_TO, to.getLon() + "," + to.getLat());
				} else if (!to.getStopCode().isEmpty()) {
					otpMap.put(Constants.OTP_RQ_TO, to.getStopCode());
				}
			} else {
				// not defined to.
			}
		}

		if (parameters.get(Constants.SP_RQ_DATE) != null) {
			otpMap.put(Constants.OTP_RQ_DATE, String.valueOf(parameters.get(Constants.SP_RQ_DATE)));
		} else {
			// not defined date.
		}

		if (parameters.get(Constants.SP_RQ_ARRTIME) != null) {
			otpMap.put(Constants.OTP_RQ_ARRIVEBY, "true");
			otpMap.put(Constants.OTP_RQ_TIME, String.valueOf(parameters.get(Constants.SP_RQ_ARRTIME)));
		} else if (parameters.get(Constants.SP_RQ_DEPTIME) != null) {
			otpMap.put(Constants.OTP_RQ_TIME, String.valueOf(parameters.get(Constants.SP_RQ_DEPTIME)));
		}

		if (parameters.get(Constants.SP_RQ_ROUTE_PREF) != null) {
			RType preference = (RType) parameters.get(Constants.SP_RQ_ROUTE_PREF);
			if (preference.equals(RType.fastest)) {
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.QUICK.toString());
			} else if (preference.equals(RType.greenest)) {
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.GREENWAYS.toString());
			} else if (preference.equals(RType.safest)) {
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.SAFE.toString());
			} else if (preference.equals(RType.leastChanges)) {
				sortForLeastChanges = true;
			} else if (preference.equals(RType.leastWalking)) {
				sortForLeastWalking = true;
			}
		}

		if (parameters.get(Constants.SP_RQ_NUM_ITNS) != null) {
			String numItineraries = String.valueOf(parameters.get(Constants.SP_RQ_NUM_ITNS));
			otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);

		}
		// maximum walking distance
		if (parameters.get(Constants.SP_RQ_MAXWALK) != null) {
			String maxWalkDistance = String.valueOf(parameters.get(Constants.SP_RQ_MAXWALK));
			otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
		}
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}			

		// connect and fetch data.
		String response = otpConnector.connect(router, otpMap);
		// takes station and mode information
		// to be used later.
		HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
		preProcessParams.put(Constants.SP_RQ_USERMODE, TType.BUS.name());
		preProcessParams.put(Constants.ROUTER, router);

		// process data to smart planner format
		output = generatePlan(router, response, preProcessParams);

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
	 * Generate formatted itinerary.
	 * @param router
	 * @param responseString
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	private List<Itinerary> generatePlan(String router, String responseString, HashMap<String, Object> parameters) throws IOException {

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
	 * Recurrent Legs Generator.
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
			userRequestWD.put(Constants.SP_RQ_NUM_ITNS, 25);

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
	 * Get recurrent leg within period.
	 * @param router
	 * @param parameters
	 * @param interval
	 * @return
	 * @throws IOException
	 */
	private List<Leg> getRecurrLegsWithinPeriod(String router, HashMap<String, Object> parameters, Long interval) throws IOException {

		Boolean search = true;
		List<Itinerary> tempList = new ArrayList<Itinerary>();
		List<Itinerary> finalList = new ArrayList<Itinerary>();
		List<Leg> recurrLegs = new ArrayList<Leg>();
		// create local copy of parameters to prevent original modification.
		HashMap<String, Object> localParams = (HashMap<String, Object>) parameters.clone();
		DateFormat formatter = new SimpleDateFormat("hh:mmaa", Locale.ITALY);

		try {
			// get maxReachTime in milliseconds.
			String requestedTime = localParams.get("date") + " " + localParams.get(Constants.SP_RQ_DEPTIME);
			DateFormat maxTimeformatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ITALY);
			Date reccurReqTime = (Date) maxTimeformatter.parse(requestedTime);

			// run while loop with search flag.
			while (search) {
				// get itinerary with time = starting time
				// and number of itns = 25.
				List<Itinerary> searchItn = getItinerary(router, localParams);
				// add itn to return list
				tempList.addAll(searchItn);
				// check if the last itn starting time is less than
				// mustReachTime.
				if (searchItn.isEmpty() || searchItn.size() < 25) {
					search = false;
					break;
				} else if (searchItn.get(searchItn.size() - 1).getStartime() >= (reccurReqTime.getTime() + interval)) {
					search = false;
					break;
				} else {
					// search again with with time = startTime of last itinerary
					// and num of Itn = 25
					Long time = searchItn.get(searchItn.size() - 1).getStartime();
					Date strDate = new Date(time);
					localParams.put(Constants.SP_RQ_DEPTIME, formatter.format(strDate));
				}
			}

			// remove the ones after the maxReachTime.
			finalList.addAll(tempList);
			for (Itinerary it : tempList) {
				if (it.getStartime() > (reccurReqTime.getTime() + interval)) {
					finalList.remove(it);
				}
			}

			// get legs.
			/** post process itineraries and get legs **/
			for (Itinerary it : finalList) {
				for (Leg leg : it.getLeg()) {
					if (!(leg.getTransport().getType().equals(TType.WALK))) {
						if (!recurrLegs.contains(leg) && (leg.getStartime() < (reccurReqTime.getTime() + interval))) {
							recurrLegs.add(leg);
						}
					}

				}
			}

		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return recurrLegs;
	}
}
