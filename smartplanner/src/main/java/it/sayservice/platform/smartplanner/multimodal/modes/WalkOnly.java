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
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * WalkOnly independant mode.
 * 
 * @author nawazk
 * 
 */
public class WalkOnly {

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
	public WalkOnly(OTPConnector otpCon, ItineraryBuildHelper itnHelper) {
		this.otpConnector = otpCon;
		this.helper = itnHelper;
	}

	/**
	 * constructor for testing.
	 */
	public WalkOnly() {
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
	public final List<Itinerary> getItinerary(String router, final HashMap<String, Object> parameters)
			throws IOException {

		// define input map for OTP.
		Map<String, String> otpMap = new HashMap<String, String>();

		// default values for OTP map
		otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.WALK.toString());
		otpMap.put(Constants.OTP_RQ_MAXWALK, Constants.WALK_ONLY);
		otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.QUICK.name());
		otpMap.put(Constants.OTP_RQ_ITNS, "1");
		otpMap.put(Constants.OTP_RQ_ARRIVEBY, "false");
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}		

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
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.QUICK.name());
			} else if (preference.equals(RType.greenest)) {
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.GREENWAYS.name());
			} else if (preference.equals(RType.safest)) {
				otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.SAFE.name());
			}
		}

//		if (parameters.get(Constants.SP_RQ_NUM_ITNS) != null) {
//			String numItineraries = String.valueOf(parameters.get(Constants.SP_RQ_NUM_ITNS));
//			otpMap.put(Constants.OTP_RQ_ITNS, numItineraries);
//
//		}
		// maximum walking distance
		if (parameters.get(Constants.SP_RQ_MAXWALK) != null) {
			String maxWalkDistance = String.valueOf(parameters.get(Constants.SP_RQ_MAXWALK));
			otpMap.put(Constants.OTP_RQ_MAXWALK, maxWalkDistance);
		}

		// connect and fetch data.
		String response = otpConnector.connect(router, otpMap);
		// takes station and mode information
		// to be used later.
		HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
		preProcessParams.put(Constants.SP_RQ_USERMODE, TType.WALK.name());
		preProcessParams.put(Constants.ROUTER, router);
		// process data to smart planner format
		output = generatePlan(response, preProcessParams);

		return output;
	}

	/**
	 * Generate formatted list of Itinerary.
	 * @param responseString
	 * @param preProcessParams
	 * @return
	 * @throws IOException
	 */
	private List<Itinerary> generatePlan(final String responseString, HashMap<String, Object> preProcessParams)
			throws IOException {

		List<Itinerary> processedData = null;

		ObjectMapper map = new ObjectMapper();
		JsonNode rootNode;

		rootNode = map.readValue(responseString, JsonNode.class);
		processedData = helper.buildItineraries(rootNode, preProcessParams);

		return processedData;
	}

	/**
	 * Recurrent Journey Legs Generator.
	 * @param router
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public final List<Leg> getLegs(String router, final HashMap<String, Object> parameters) throws IOException {

		// definitions
		Long fromDate;
		Long toDate;
		String wednesday = null;
		List<Itinerary> temp = new ArrayList<Itinerary>();

		if (parameters.get(Constants.SP_RQ_FROMDATE) != null && parameters.get(Constants.SP_RQ_TODATE) != null) {
			fromDate = (Long) parameters.get(Constants.SP_RQ_FROMDATE);
			toDate = (Long) parameters.get(Constants.SP_RQ_TODATE);
			Date startDate = new Date(fromDate);
			Date endDate = new Date(toDate);

			long endTime = endDate.getTime();
			long curTime = startDate.getTime();

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
				temp.addAll(getItinerary(router, userRequestWD));
			}

			/** post process itineraries and get legs **/
			for (Itinerary it : temp) {
				for (Leg leg : it.getLeg()) {
					if (!(leg.getTransport().getType().equals(TType.WALK))) {
						if (!rjOutput.contains(leg)) {
							rjOutput.add(leg);
						}
					}

				}
			}
		}

		return rjOutput;

	}

}
