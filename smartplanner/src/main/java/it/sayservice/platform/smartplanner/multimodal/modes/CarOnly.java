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

import it.sayservice.platform.smartplanner.areainfo.CostData;
import it.sayservice.platform.smartplanner.areainfo.SearchTime;
import it.sayservice.platform.smartplanner.areainfo.SearchTime.SearchTimeSlot;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.geocoder.GeocodeAPIsManager;
import it.sayservice.platform.smartplanner.model.AreaPoint;
import it.sayservice.platform.smartplanner.model.StreetLocation;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.ItineraryBuildHelper;
import it.sayservice.platform.smartplanner.utils.OTPConnector;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;
import it.sayservice.platform.smartplanner.utils.RepositoryUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;

/**
 * CarOnly independant Mode.
 * 
 * @author nawazk
 * 
 */
public class CarOnly {
	/** list of itineraries. **/
	private List<Itinerary> output;
	/** list of recurrent legs. **/
	private List<Leg> rjOutput = new ArrayList<Leg>();
	/** Itinerary build helper. **/
	private ItineraryBuildHelper helper;
	/** connector object for OTP. **/
	private OTPConnector otpConnector;
	/** geocoder api manager. **/
	private GeocodeAPIsManager apiManager;
	/** json mapper. **/
	ObjectMapper map = new ObjectMapper();
	/** router mapper. **/
	private MongoRouterMapper routersMap;
	/** repository Utils. **/
	private RepositoryUtils repositoryUtils;
	/** area point repository. **/
	private AreaPointRepository areaPointRepository;

	/**
	 * 
	 * @param rm
	 * @param otpCon
	 * @param apisManager
	 * @param itnHelper
	 * @param areaPointRepository
	 */
	public CarOnly(String router, OTPConnector otpCon, GeocodeAPIsManager apisManager, ItineraryBuildHelper itnHelper,
			MongoRouterMapper routeMapper, RepositoryUtils repoUtils) {
		this.otpConnector = otpCon;
		this.apiManager = apisManager;
		this.helper = itnHelper;
		this.routersMap = routeMapper;
		this.repositoryUtils = repoUtils;
		this.areaPointRepository = routersMap.getAreaPointRepository(router);
	}

	/**
	 * constructor for testing.
	 */
	public CarOnly() {
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
	 * Get itineray.
	 * @param router
	 * @param parameters
	 * @return
	 * @throws IOException
	 */
	public List<Itinerary> getItinerary(String router, HashMap<String, Object> parameters) throws IOException {

		// define input map for OTP.
		Map<String, String> otpMap = new HashMap<String, String>();

		// default values for OTP map
		otpMap.put(Constants.OTP_RQ_MODE, Constants.MODES.CAR.toString());
		otpMap.put(Constants.OTP_RQ_MAXWALK, Constants.NO_WALK);
		otpMap.put(Constants.OTP_RQ_OPTIMIZE, Constants.OPTIMIZATION.QUICK.toString());
		otpMap.put(Constants.OTP_RQ_ITNS, "1");

		// get SmartUser requirements, map it to OTP format.
		processRequestCoordinates(router, parameters, otpMap);

		if (parameters.get(Constants.SP_RQ_DATE) != null) {
			otpMap.put(Constants.OTP_RQ_DATE, String.valueOf(parameters.get(Constants.SP_RQ_DATE)));
		} else {
		} // not defined date.

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
		
		if (parameters.get(Constants.WHEELCHAIR) != null) {
			otpMap.put(Constants.WHEELCHAIR, String.valueOf(parameters.get(Constants.WHEELCHAIR)));
		}			
		
		// connect and fetch data.
		String response = otpConnector.connect(router, otpMap);
		response = fixRequestCoordinates(router, response, otpMap);

		// takes station and mode information
		// to be used later.
		HashMap<String, Object> preProcessParams = (HashMap<String, Object>) parameters.clone();
		preProcessParams.put(Constants.SP_RQ_USERMODE, TType.CAR.name());
		preProcessParams.put(Constants.ROUTER, router);
		// process data to smart planner format
		output = generatePlan(response, preProcessParams);

		return output;
	}

	private void processRequestCoordinates(String router, HashMap<String, Object> parameters, Map<String, String> otpMap) {
		Position from;
		Position to;

		try {
			if (parameters.get("from") != null) {
				from = (Position) parameters.get("from");
				if (from != null) {
					if (!from.getLat().isEmpty() && !from.getLon().isEmpty()) {
						StreetLocation existingLocation = repositoryUtils.existStreetLocation(router, Double.parseDouble(from.getLon()),
								Double.parseDouble(from.getLat()));
						if (existingLocation != null) {
							System.out.println("Using old suggested position for 'from' point " + from.getLat() + ", "
									+ from.getLon() + " -> " + existingLocation.getPosition()[0] + ","
									+ existingLocation.getPosition()[1]);
							otpMap.put("fromPlace",
									existingLocation.getPosition()[0] + "," + existingLocation.getPosition()[1]);
						} else {
							otpMap.put("fromPlace", from.getLon() + "," + from.getLat());
						}
					} else if (!from.getStopCode().isEmpty()) {
						otpMap.put("fromPlace", from.getStopCode());
					}
				} else {
				} // not defined from.
			}

			if (parameters.get("to") != null) {
				to = (Position) parameters.get("to");
				if (to != null) {
					if (!to.getLat().isEmpty() && !to.getLon().isEmpty()) {
						StreetLocation existingLocation = repositoryUtils.existStreetLocation(router, Double.parseDouble(to.getLon()),
								Double.parseDouble(to.getLat()));
						if (existingLocation != null) {
							System.out.println("Using old suggested position for 'to' point " + to.getLat() + ", "
									+ to.getLon() + " -> " + existingLocation.getPosition()[0] + ","
									+ existingLocation.getPosition()[1]);
							otpMap.put("toPlace",
									existingLocation.getPosition()[0] + "," + existingLocation.getPosition()[1]);
						} else {
							otpMap.put("toPlace", to.getLon() + "," + to.getLat());
						}
					} else if (!to.getStopCode().isEmpty()) {
						otpMap.put("toPlace", to.getStopCode());
					}
				} else {
				} // not defined to.
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String fixRequestCoordinates(String router, String response, Map<String, String> otpMap) {
		try {
			JsonNode rootNode = map.readValue(response, JsonNode.class);
			JsonNode plan = rootNode.get("plan");
			if (plan == null) {
				System.err.println("plan is Null");

				String coords[] = otpMap.get("fromPlace").split(",");
				double lat = Double.parseDouble(coords[0]);
				double lon = Double.parseDouble(coords[1]);

				Position suggestedPosition = queryLocation(router, lat, lon);
				if (suggestedPosition != null) {
					otpMap.put("fromPlace", suggestedPosition.getLat() + "," + suggestedPosition.getLon());
					System.out.println("Fixing using new suggested position for 'from' point " + lat + ", " + lon
							+ " -> " + suggestedPosition.getLat() + "," + suggestedPosition.getLon());
				} else {
					System.err.println("No suggestion from google api for 'from' point " + lat + ", " + lon);
				}

				coords = otpMap.get("toPlace").split(",");
				lat = Double.parseDouble(coords[0]);
				lon = Double.parseDouble(coords[1]);

				suggestedPosition = queryLocation(router, lat, lon);
				if (suggestedPosition != null) {
					otpMap.put("toPlace", suggestedPosition.getLat() + "," + suggestedPosition.getLon());
					System.out.println("Fixing using new suggested position for 'to' point " + lat + ", " + lon + " -> "
							+ suggestedPosition.getLat() + "," + suggestedPosition.getLon());
				} else {
					System.err.println("No suggestion from google api for 'to' point " + lat + ", " + lon);
				}

			}

			return otpConnector.connect(router, otpMap);
		} catch (Exception e) {
			e.printStackTrace();
			return response;
		}
	}

	private Position queryLocation(String router, double lat, double lon) {
		String requestAPI = "location=" + lat + "," + lon + "&radius=50&sensor=false&types=route";
		Position suggestedPosition = apiManager.nearbySearchGoogle(router, requestAPI, null, MediaType.APPLICATION_JSON);
		// save suggested position in StreeLocation db.
		if (suggestedPosition != null) {
			StreetLocation temp = new StreetLocation(suggestedPosition.getStopId().getId(),
					suggestedPosition.getStopCode(), suggestedPosition.getName(), lat, lon,
					Double.valueOf(suggestedPosition.getLat()), Double.valueOf(suggestedPosition.getLon()));
			routersMap.getStreetLocationRepository(router).save(temp);
			return suggestedPosition;
		}
		return null;
	}

	/**
	 * Generate formatted itinerary.
	 * @param responseString
	 * @param preProcessParams
	 * @return
	 * @throws IOException
	 */
	private List<Itinerary> generatePlan(String responseString, HashMap<String, Object> preProcessParams)
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
	 * 
	 * @param parameters
	 *            Map
	 * @return rjOutput List<Leg>
	 * @throws IOException
	 *             e
	 */
	public List<Leg> getLegs(String router, HashMap<String, Object> parameters) throws IOException {

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

			completeLegs(temp);
		}

		return rjOutput;
	}

	public void completeLegs(List<Itinerary> its) {
		ObjectMapper mapper = new ObjectMapper();

		for (Itinerary it : its) {
			for (Leg leg : it.getLeg()) {
				if (!(leg.getTransport().getType().equals(TType.WALK))) {
					if (!rjOutput.contains(leg)) {
						rjOutput.add(leg);
					}
				}

				// insert additional area info: if this is a car and it does not
				// arrive to a parking
				if (leg.getTransport().getType().equals(TType.CAR) && leg.getTo().getStopId() == null) {
					Point p = new Point(Double.parseDouble(leg.getTo().getLat()),
							Double.parseDouble(leg.getTo().getLon()));
					Distance d = new Distance(0.35, Metrics.KILOMETERS);
					// System.out.println("Can add extra near " + p + " / " +
					// d);
					List<AreaPoint> points = areaPointRepository.findByLocationNear(p, d);
					if (points != null && !points.isEmpty()) {
						Map<String, Object> extra = new HashMap<String, Object>();
						SearchTime st = mapper.convertValue((Map) points.get(0).getData().get("searchTime"),
								SearchTime.class);
						SearchTimeSlot slot = null;
						if (st != null && (slot = st.compute(leg.getEndtime())) != null) {
							extra.put("searchTime", slot);
						}
						CostData cd = points.get(0).getCostData();
						if (cd != null) {
							extra.put("costData", cd);
						}
						leg.setExtra(extra);
					}
				}
			}
		}
	}

}
