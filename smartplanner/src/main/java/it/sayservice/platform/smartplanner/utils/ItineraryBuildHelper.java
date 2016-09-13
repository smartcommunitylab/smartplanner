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

import it.sayservice.platform.smartplanner.areainfo.CostData;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.data.message.Geometery;
import it.sayservice.platform.smartplanner.data.message.Itinerary;
import it.sayservice.platform.smartplanner.data.message.Leg;
import it.sayservice.platform.smartplanner.data.message.LegGeometery;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.StopId;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertParking;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertType;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.CarStation;
import it.sayservice.platform.smartplanner.model.DynamicBikeStation;
import it.sayservice.platform.smartplanner.model.DynamicCarStation;
import it.sayservice.platform.smartplanner.otp.SmartPlannerUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;

/**
 * Helper class for constructing/filtering itinerary.
 * @author nawazk
 */

@Component
public class ItineraryBuildHelper {

	/** repository utils. **/
	@Autowired
	private RepositoryUtils repositoryUtils;
	/** configuration properties. **/
//	@Autowired
	private RouterConfig smartPlannerConfiguration;
	@Autowired
	private ConfigurationManager configurationManager;
	/** date formatter. **/
	private SimpleDateFormat formatter;

	ObjectMapper mapper = new ObjectMapper();

	/**
	 * builder method.
	 * 
	 * @param rootNode
	 *            JsonNode
	 * @param preProcessParams
	 *            Map.
	 * @return List<Itinerary>
	 */
	public List<Itinerary> buildItineraries(JsonNode rootNode, HashMap<String, Object> preProcessParams) {

		Position toPos = null;
		Position fromPos = null;
		List<Itinerary> itnList = new ArrayList<Itinerary>();
		List<Leg> legs = null;

		// plan
		JsonNode plan = rootNode.get("plan");

		// check for error.
		if (plan != null) {
			// To
			JsonNode to = plan.get("to");
			toPos = buildPosition(to, "", null, null, null, new Long(-1), null, false);
			// From
			JsonNode from = plan.get("from");
			fromPos = buildPosition(from, "", null, null, null, new Long(-1), null, true);

			// itineraries
			JsonNode Itineraries = plan.get("itineraries");

			// traverse each itinerary
			Iterator<JsonNode> elements = Itineraries.getElements();
			while (elements.hasNext()) {
				JsonNode node = elements.next();
				// fare.
				List<?> fareList = null;
				if (node.has("fare") && node.get("fare").has("details")
						&& node.get("fare").get("details").has("regular") && node.get("fare").has("fare")
						&& node.get("fare").get("fare").has("regular")
						&& node.get("fare").get("fare").get("regular").has("cents")
						&& SmartPlannerUtils.isNonNegativeDouble(
								node.get("fare").get("fare").get("regular").get("cents").getValueAsText())) {
					JsonNode fare = node.get("fare").get("details").get("regular");
					fareList = mapper.convertValue(fare, List.class);

				}
				legs = buildLegs(node.get("legs"), preProcessParams, fareList);
				Itinerary itn = new Itinerary(fromPos, toPos, node.get("startTime").getLongValue(),
						node.get("endTime").getLongValue(), node.get("duration").getLongValue(),
						node.get("walkTime").getLongValue(), legs);

				itnList.add(itn);
			}

		}
		// fix time stamps for fluid transit legs(WALK)
		// && NON TRANIST LEGS BICYCLE, CAR.
		int i = 0;
		for (Itinerary itn : itnList) {
			i = 0;
			for (Leg legW : itn.getLeg()) {
				String type = legW.getTransport().getType().name();
				if ((type.equalsIgnoreCase("BUS") || type.equalsIgnoreCase("TRAIN") || type.equalsIgnoreCase("GONDOLA")
						|| type.equalsIgnoreCase("TRANSIT"))) {
					i++;
					continue;
				} else if (i > 0) {
					Leg prev = itn.getLeg().get(i - 1);
					legW.setStartime(prev.getEndtime());
					legW.setEndtime(prev.getEndtime() + legW.getDuration() * 1000);
				}
				i++;
			}
		}

		int itnN = itnList.size();

		JsonNode request = rootNode.get("requestParameters");
		if (request != null) {
			if (request.has("numItineraries")) {
				itnN = Integer.parseInt(request.get("numItineraries").getValueAsText());
			}
		}		
		
		itnList = itnList.subList(0, Math.min(itnN, itnList.size()));
		
		return itnList;
	}

	/**
	 * Builder method for Itinerary leg.
	 * 
	 * @param legs
	 *            JsonNode.
	 * @param preProcessParams
	 *            Map.
	 * @return List<Leg>
	 */
	public List<Leg> buildLegs(JsonNode legs, HashMap<String, Object> preProcessParams, List<?> fareList) {
		List<Leg> legList = new ArrayList<Leg>();
		String mode = null;
		Transport transport = null;

		// traverse each leg
		Iterator<JsonNode> elements = legs.getElements();
		boolean first = true;
		while (elements.hasNext()) {
			if (first) {
				preProcessParams.put("first", Boolean.TRUE);
				first = false;
			}

			// initialize alert lists for each leg.
			List<AlertDelay> alertD = new ArrayList<AlertDelay>();
			List<AlertStrike> alertS = new ArrayList<AlertStrike>();
			List<AlertParking> alertP = new ArrayList<AlertParking>();
			List<AlertRoad> alertR = new ArrayList<AlertRoad>();
			List<AlertAccident> alertA = new ArrayList<AlertAccident>();

			JsonNode node = elements.next();

			if (!elements.hasNext()) {
				preProcessParams.put("last", Boolean.TRUE);
			}

			mode = node.get("mode").getValueAsText();
			String agencyId = node.has("agencyId") ? node.get("agencyId").getValueAsText() : null;
			String routeId = node.has("routeId") ? node.get("routeId").getValueAsText() : null;
			String routeShortName = node.has("routeShortName") ? node.get("routeShortName").getValueAsText() : null;
			String tripId = node.has("tripId") ? node.get("tripId").getValueAsText() : null;
			mode = node.get("mode").getValueAsText();
			transport = new Transport(TType.getMode(mode), agencyId, routeId, routeShortName, tripId);

			Long legStartTime = node.get("startTime").getLongValue();

			Leg leg = new Leg(agencyId + "_" + tripId, node.get("startTime").getLongValue(),
					node.get("endTime").getLongValue(), node.get("duration").getLongValue(),
					node.get("distance").getDoubleValue(),
					buildPosition(node.get("from"), mode, alertD, alertS, alertP, legStartTime, preProcessParams, true),
					buildPosition(node.get("to"), mode, alertD, alertS, alertP, legStartTime, preProcessParams, false),
					transport, buildLegGeometery(node.get("legGeometry")), alertD, alertS, alertP, alertR, alertA);

			// GTFS routes only.
			if (routeId != null && !routeId.isEmpty()) {
				if (fareList != null && !fareList.isEmpty()) {
					Map<String, Object> fareMap = new HashMap<String, Object>();
					for (int index = 0; index < fareList.size(); index++) {
						fareMap = mapper.convertValue(fareList.get(index), Map.class);
						if (fareMap.containsKey("routes")) {
							ArrayList<String> routes = mapper.convertValue(fareMap.get("routes"), ArrayList.class);
							if (routes.contains(agencyId + ":" + routeId)) {
								Map<String, Object> extraMap = new HashMap<String, Object>();
								extraMap.put("fare", fareMap.get("price"));
								extraMap.put("fareIndex", index);
								leg.setExtra(extraMap);
								break;
							}
						}
					}
				}
			}

			legList.add(leg);

			preProcessParams.remove("first");
			preProcessParams.remove("last");
		}

		return legList;
	}

	/**
	 * builder method for leg Geometry.
	 * 
	 * @param jsonNode
	 *            JsonNode
	 * @return LegGeometery
	 **/
	public LegGeometery buildLegGeometery(JsonNode jsonNode) {
		LegGeometery legGeom = new LegGeometery(jsonNode.has("length") ? jsonNode.get("length").getLongValue() : null,
				jsonNode.has("levels") ? jsonNode.get("levels").getValueAsText() : null,
				jsonNode.has("points") ? jsonNode.get("points").getValueAsText() : null);
		return legGeom;
	}

	/**
	 * builder for position node.
	 * 
	 * @param pos
	 *            JsonNode
	 * @param mode
	 *            String
	 * @param alertD
	 *            List<AlertDelay>
	 * @param alertS
	 *            List<AlertStrike>
	 * @param alertP
	 *            List<AlertParking>
	 * @param legTime
	 *            long.
	 * @param preProcessParams
	 *            Map.
	 * @param from
	 *            Boolean.
	 * @return Position
	 */
	public Position buildPosition(JsonNode pos, String mode, List<AlertDelay> alertD, List<AlertStrike> alertS,
			List<AlertParking> alertP, Long legTime, HashMap<String, Object> preProcessParams, boolean from) {

		// position name.
		String name = pos.get("name").getValueAsText();
		// position StopId
		StopId stopId = buildStopId(pos, mode, legTime, preProcessParams, from, alertD, alertS, alertP);
		// modify name based on mode.
		if (mode.equalsIgnoreCase("CAR") || mode.equalsIgnoreCase("BICYCLE")) {
			if (preProcessParams != null) {
				String userMode = String.valueOf(preProcessParams.get("userMode"));
				if (userMode.equalsIgnoreCase(TType.SHAREDCAR.name())
						|| userMode.equalsIgnoreCase(TType.SHAREDCAR_WITHOUT_STATION.name())
						|| userMode.equalsIgnoreCase(TType.SHAREDBIKE.name())
						|| userMode.equalsIgnoreCase(TType.SHAREDBIKE_WITHOUT_STATION.name())
						|| userMode.equalsIgnoreCase(TType.CARWITHPARKING.name())
						|| userMode.equalsIgnoreCase(TType.PARK_AND_RIDE.name())) {
					// if (stopId != null && !stopId.getAgencyId().isEmpty()) {
					if (stopId != null) {
						if (stopId.getExtra() != null && stopId.getExtra().containsKey("stationName")) {
							name = (String)stopId.getExtra().get("stationName");
							stopId.getExtra().remove("stationName");
						} else {
							name = stopId.getId();
						}
					}
				}
			}
		}
		// create position.
		Position temp = new Position(name, stopId, pos.has("stopCode") ? pos.get("stopCode").getValueAsText() : null,
				pos.get("lon").getValueAsText(), pos.get("lat").getValueAsText());
		return temp;
	}

	/**
	 * build for Position Geometry Node.
	 * 
	 * @param jsonNode
	 *            JsonNode
	 * @return Geometery
	 */
	public Geometery buildGeometery(JsonNode jsonNode) {
		Geometery geom = null;
		if (!jsonNode.isNull()) {
			geom = new Geometery(jsonNode.get("\'type\'").getValueAsText(),
					jsonNode.get("coordinates").getValueAsText());
			geom = new Geometery("", "");
		} else {
			geom = new Geometery("", "");
		}

		geom = new Geometery("", "");
		return geom;
	}

	/**
	 * 
	 * @param pos
	 * @param mode
	 * @param legTime
	 * @param preProcessParams
	 * @param from
	 * @param alertD
	 * @param alertS
	 * @param alertP
	 * @return
	 */
	public StopId buildStopId(JsonNode pos, String mode, long legTime, HashMap<String, Object> preProcessParams,
			Boolean from, List<AlertDelay> alertD, List<AlertStrike> alertS, List<AlertParking> alertP) {

		StopId stopId = null;
		String userMode = "";
		JsonNode jsonNode = null;
		String agencyId = null, id = null;
		if (pos.has("stopId")) {
			String[] ids = pos.get("stopId").getValueAsText().split(":");
			agencyId = ids[0];
			id = ids[1];
		}

		// fetch pre process information
		if (preProcessParams != null) {
			userMode = String.valueOf(preProcessParams.get("userMode"));
		}

		Map<String, Object> extra = new TreeMap<String, Object>();
		
		if (agencyId != null && id != null) {
			stopId = new StopId(agencyId, id);
		} else if (mode.equalsIgnoreCase("car")) {
			Boolean isCarSharing = false;
			// reset/create stopId by looking at requestedMode.
			stopId = null;
			CarStation cs = null;

			// if requested mode is Shared Car
			if (userMode.equalsIgnoreCase(TType.SHAREDCAR.name())) {
				if (from) {
					cs = (CarStation) preProcessParams.get("fromStation");
					stopId = cs.getStationId();
				} else {
					cs = (CarStation) preProcessParams.get("toStation");
					stopId = cs.getStationId();
				}
				isCarSharing = true;
			} else if (userMode.equalsIgnoreCase(TType.SHAREDCAR_WITHOUT_STATION.name()) && from) {
				// if requested mode is SHAREDCAR_WITHOUT_STATION
				// and position is from then only get StopId since
				// to is not required to be station.
				cs = (CarStation) preProcessParams.get("fromStation");
				stopId = cs.getStationId();
				isCarSharing = true;
			} else if (userMode.equalsIgnoreCase(TType.CARWITHPARKING.name()) && !from) {
				// if requested mode is CARWITHPARKING and position
				// is to then only get StopId since from is not
				// required to be station.
				cs = (CarStation) preProcessParams.get("toStation");
				stopId = cs.getStationId();
				if (cs.getFixedCost() != null && !cs.getFixedCost().isEmpty() && cs.getCostDefinition() != null
						&& !cs.getCostDefinition().isEmpty()) {
					CostData cd = new CostData();
					cd.setFixedCost(cs.getFixedCost());
					cd.setCostDefinition(cs.getCostDefinition());
					extra.put("costData", cd);
					stopId.setExtra(extra);
				}
				isCarSharing = false;
			}
			if (cs != null) {
				extra.put("parkAndRide", cs.isParkAndRide());
				extra.put("stationName", cs.getCarStationName());
			}

			if (stopId != null) {
				/** if required, create alert. **/
				DynamicCarStation dcs = repositoryUtils
						.findDynamicCarStation(String.valueOf(preProcessParams.get(Constants.ROUTER)), cs.getId());
				if (dcs != null) {

					int carStationThreshold = Constants.CS_THRESHOLD;

					smartPlannerConfiguration = configurationManager.getRouter(String.valueOf(preProcessParams.get(Constants.ROUTER)));
					
					if (smartPlannerConfiguration.getCarSharing().containsKey(dcs.getAgencyId())
							&& smartPlannerConfiguration.getCarSharing().get(dcs.getAgencyId()).getSpecificProperties()
									.containsKey("notification-threshold")) {
						carStationThreshold = Integer.parseInt(smartPlannerConfiguration.getCarSharing()
								.get(dcs.getAgencyId()).getSpecificProperties().get("notification-threshold"));
					}

					if (legTime < dcs.getDuration()) {
						boolean posts = dcs.getPosts() != -1 && dcs.getPosts() < carStationThreshold;
						boolean cars = dcs.getCars() != -1 && dcs.getCars() < carStationThreshold;

						if (posts || cars) {
//							String displayId = cs.getStationId().getId();
							String displayId = cs.getCarStationName();
							AlertParking alert = new AlertParking();
							alert.setId(cs.getId());
							if (isCarSharing) {
								alert.setDescription(
										"Car Station " + displayId + ": available cars = " + dcs.getCars());
							} else {
								alert.setDescription(
										"Car Station " + displayId + ": available parkings = " + dcs.getPosts());
							}

							if (dcs.getCreatorId() != null) {
								alert.setCreatorId(dcs.getCreatorId());
							}
							if (dcs.getCreatorType() != null) {
								alert.setCreatorType(dcs.getCreatorType());
							}
							alert.setType(AlertType.PARKING);
							// alertParking.
							alert.setPlace(cs.getStationId());
							alert.setNoOfvehicles(dcs.getCars());
							alert.setPlacesAvailable(dcs.getPosts());
							alertP.add(alert);
						}
					}
				}
			}
		} else if (mode.equalsIgnoreCase("bicycle")) {
			// reset/create stopId by looking at requestedMode.
			stopId = null;
			BikeStation bs = null;
			// if requested mode is Shared Bike
			if (userMode.equalsIgnoreCase(TType.SHAREDBIKE.name())) {
				if (from) {
					if (preProcessParams.containsKey("first")) {
						bs = (BikeStation) preProcessParams.get("fromStation");
						stopId = bs.getStationId();
					}
				} else {
					if (preProcessParams.containsKey("last")) {
						bs = (BikeStation) preProcessParams.get("toStation");
						stopId = bs.getStationId();
					}
				}
			} else if (userMode.equalsIgnoreCase(TType.SHAREDBIKE_WITHOUT_STATION.name())) {
				// if requested mode is SHAREDCAR_WITHOUT_STATION
				// and position is from then only get StopId since
				// to is not required to be station.
				if (from) {
					if (preProcessParams.containsKey("first")) {
						bs = (BikeStation) preProcessParams.get("fromStation");
						stopId = bs.getStationId();
					}
				}
			}
			
			if (bs != null) {
				extra.put("stationName", bs.getBikeStationName());
			}

			if (stopId != null) {
				/** if required, create alert. **/
				DynamicBikeStation dbs = repositoryUtils
						.findDynamicBikeStation(String.valueOf(preProcessParams.get(Constants.ROUTER)), bs.getId());
				if (dbs != null) {

					int bikeStationThreshold = Constants.BS_THRESHOLD;

					smartPlannerConfiguration = configurationManager.getRouter(String.valueOf(preProcessParams.get(Constants.ROUTER)));
					
					if (smartPlannerConfiguration.getBikeSharing().containsKey(dbs.getAgencyId())
							&& smartPlannerConfiguration.getBikeSharing().get(dbs.getAgencyId()).getSpecificProperties()
									.containsKey("notification-threshold")) {
						bikeStationThreshold = Integer.parseInt(smartPlannerConfiguration.getBikeSharing()
								.get(dbs.getAgencyId()).getSpecificProperties().get("notification-threshold"));
					}

					if (legTime < dbs.getDuration()) {
						boolean f = from && dbs.getBikes() < bikeStationThreshold;
						boolean t = !from && dbs.getPosts() < bikeStationThreshold;
						if (f || t) {
//							String displayId = bs.getStationId().getId();
							String displayId = bs.getBikeStationName();
							AlertParking alert = new AlertParking();
							alert.setId(dbs.getId());
							alert.setDescription("Bike Station " + displayId + ": available bikes = " + dbs.getBikes()
									+ ", available parkings = " + dbs.getPosts());
							if (dbs.getCreatorId() != null) {
								alert.setCreatorId(dbs.getCreatorId());
							}
							if (dbs.getCreatorType() != null) {
								alert.setCreatorType(dbs.getCreatorType());
							}
							alert.setType(AlertType.PARKING);
							// alert parking.
							alert.setPlace(bs.getStationId());
							alert.setNoOfvehicles(dbs.getBikes());
							alert.setPlacesAvailable(dbs.getPosts());
							alertP.add(alert);
						}
					}
				}
			}
		} else {
			stopId = new StopId("", "");
		}
		return stopId;
	}

	/**
	 * long to time converter utility method.
	 * 
	 * @param milliseconds
	 * @return time in String.
	 */
	public String convertMillisToTime(Long milliseconds) {

		String am_pm;
		String hrString = null;
		String minString = null;
		Date d = new Date(milliseconds);
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		int hr = c.get(Calendar.HOUR_OF_DAY);
		int min = c.get(Calendar.MINUTE);
		if (c.get(Calendar.AM_PM) == 0) {
			am_pm = "am";
		} else {
			am_pm = "pm";
		}
		int convHrs = (hr > 12) ? hr - 12 : hr;
		if (convHrs < 10) {
			hrString = "0" + String.valueOf(convHrs);
		} else {
			hrString = String.valueOf(convHrs);
		}
		if (min < 10) {
			minString = "0" + String.valueOf(min);
		} else {
			minString = String.valueOf(min);
		}

		String time = hrString + ":" + minString + am_pm;
		// System.out.println(time);

		return time;
	}

	/**
	 * long to date converter utility.
	 * 
	 * @param milliseconds
	 *            Long
	 * @return date
	 */
	public final String convertMillisToDate(final Long milliseconds) {

		String dateString = null;
		Date d = new Date(milliseconds);
		DateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy", Locale.ITALY);

		dateString = dateFormatter.format(d);

		return dateString;
	}

	/**
	 * Roundoff method.
	 * 
	 * @param Rval
	 *            double
	 * @param Rpl
	 *            Integer
	 * @return double
	 */
	public static double Round(double Rval, int Rpl) {
		float p = (float) Math.pow(10, Rpl);
		Rval = Rval * p;
		float tmp = Math.round(Rval);
		return (double) tmp / p;
	}

	/**
	 * main.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		ObjectMapper mapper = new ObjectMapper();
		// mapper.configure(org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS,
		// false);
		// double lat = 46.075360240893595;
		// lat = Round(lat, 4);
		// double lon = 11.12236033196787;
		// lon = Round(lon, 4);
		// System.out.println("Rounded data: " + lat + "," + lon);

		InputStream jsonFile = Thread.currentThread().getContextClassLoader().getResourceAsStream("bikeplan.json");
		String json = getStringFromInputStream(jsonFile);

		JSONObject result = new JSONObject();

		JSONObject modifiedPlan = new JSONObject();
		JSONObject modifiedItineraries = new JSONObject();
		JSONObject modifiedLeg = new JSONObject();

		try {
			JsonNode root = mapper.readTree(json);
			JsonNode Itineraries = root.get("plan").get("itineraries");
			Iterator<JsonNode> elements = Itineraries.getElements();

			long distance = 0;
			long duration = 0;
			ArrayList<String> points = new ArrayList<String>();
			List<Location> locations = new ArrayList<Location>();

			if (elements.hasNext()) {
				JsonNode node = elements.next();
				modifiedItineraries.put("duration", node.get("duration").getLongValue());
				modifiedItineraries.put("startTime", node.get("startTime").getLongValue());
				modifiedItineraries.put("endTime", node.get("endTime").getLongValue());
				modifiedItineraries.put("walkTime", new JSONObject().NULL);
				JSONObject fromItn = new JSONObject();
				Iterator<String> fNames = root.get("plan").get("from").getFieldNames();
				while (fNames.hasNext()) {
					String key = fNames.next();
					fromItn.put(key, root.get("plan").get("from").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("from").get(key));
				}

				modifiedItineraries.put("from", fromItn);

				JSONObject toItn = new JSONObject();
				Iterator<String> tNames = root.get("plan").get("to").getFieldNames();
				while (tNames.hasNext()) {
					String key = tNames.next();
					toItn.put(key, root.get("plan").get("to").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("to").get(key));
				}

				modifiedItineraries.put("to", toItn);

				JsonNode legs = node.get("legs");

				// distance, geometery.
				Iterator<JsonNode> legIterator = legs.getElements();
				while (legIterator.hasNext()) {
					JsonNode tmpLeg = legIterator.next();
					distance = distance + tmpLeg.get("distance").getLongValue();
					duration = duration + tmpLeg.get("duration").getLongValue();
					points.add(tmpLeg.get("legGeometry").get("points").getTextValue());
				}

				for (String geometery : points) {
					locations.addAll(PolylineEncoder.decode(geometery, 1E-5));
				}

				JsonNode firstLeg = legs.get(0);
				JsonNode lastLeg = legs.get(legs.size() - 1);

				modifiedLeg.put("startTime", firstLeg.get("startTime").getLongValue());
				modifiedLeg.put("endTime", lastLeg.get("endTime").getLongValue());
				modifiedLeg.put("agencyId", new JSONObject().NULL);
				modifiedLeg.put("routeId", new JSONObject().NULL);
				modifiedLeg.put("routeShortName", new JSONObject().NULL);
				modifiedLeg.put("tripId", new JSONObject().NULL);

				JSONObject fromLeg = new JSONObject();
				Iterator<String> fromLegNames = firstLeg.get("from").getFieldNames();
				while (fromLegNames.hasNext()) {
					String key = fromLegNames.next();
					fromLeg.put(key, firstLeg.get("from").get(key).isNull() ? new JSONObject().NULL
							: firstLeg.get("from").get(key));
				}

				modifiedLeg.put("from", fromLeg);

				JSONObject toLeg = new JSONObject();
				Iterator<String> toLegNames = firstLeg.get("to").getFieldNames();
				while (toLegNames.hasNext()) {
					String key = toLegNames.next();
					toLeg.put(key,
							firstLeg.get("to").get(key).isNull() ? new JSONObject().NULL : firstLeg.get("to").get(key));
				}

				modifiedLeg.put("to", toLeg);
				modifiedLeg.put("distance", distance);
				modifiedLeg.put("duration", duration);
				String legGeometery = PolylineEncoder.encode(locations);
				JSONObject legGeomNode = new JSONObject();
				legGeomNode.put("points", legGeometery);
				legGeomNode.put("levels", new JSONObject().NULL);
				legGeomNode.put("length", legGeometery.length());
				modifiedLeg.put("legGeometry", legGeomNode);
				modifiedLeg.put("mode", "BICYCLE");

				JSONArray jsonlegs = new JSONArray();
				jsonlegs.put(modifiedLeg);
				modifiedItineraries.put("legs", jsonlegs);
				// plan.
				modifiedPlan.put("date", root.get("plan").get("date").getLongValue());
				// from plan.
				JSONObject fromPlan = new JSONObject();
				Iterator<String> fromPlanFieldNames = root.get("plan").get("from").getFieldNames();
				while (fromPlanFieldNames.hasNext()) {
					String key = fromPlanFieldNames.next();
					fromPlan.put(key, root.get("plan").get("from").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("from").get(key));
				}

				modifiedPlan.put("from", fromPlan);
				// to plan.
				JSONObject toPlan = new JSONObject();
				Iterator<String> toPlanFieldNames = root.get("plan").get("to").getFieldNames();
				while (toPlanFieldNames.hasNext()) {
					String key = toPlanFieldNames.next();
					toPlan.put(key, root.get("plan").get("to").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("to").get(key));
				}

				modifiedPlan.put("to", toPlan);

				JSONArray itineraries = new JSONArray();
				itineraries.put(modifiedItineraries);
				modifiedPlan.put("itineraries", itineraries);
			}

			result.put("plan", modifiedPlan);
			System.out.println(result.toString());

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * convert stream to string.
	 * 
	 * @param is
	 * @return
	 */
	private static String getStringFromInputStream(InputStream is) {
		BufferedReader br = null;
		StringBuilder sb = new StringBuilder();
		String line;
		try {
			br = new BufferedReader(new InputStreamReader(is));
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					System.err.println(e.getMessage());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * real time update validation/filtering.
	 * 
	 * @param tempOutput
	 * @param requestedParams
	 *            Map<String,Object>
	 * @return List<Itinerary>
	 */
	public List<Itinerary> filterWithAlerts(String router, List<Itinerary> tempOutput,
			Map<String, Object> requestedParams) {

		// definition.
		List<Itinerary> filteredOutput = new ArrayList<Itinerary>();

		// validate and filter delay unavailability alert.
		filteredOutput = processWithAD(router, tempOutput, requestedParams);

		// validate and filter strike/service unavailability alert.
		filteredOutput = processWithAS(router, filteredOutput);

		return filteredOutput;
	}

	/**
	 * Analyze Alert Delay.
	 * 
	 * @param tempOutput
	 *            List<Itinerary>
	 * @param requestedParams
	 *            Map<String,Object>
	 * @return List<Itinerary>
	 */
	private List<Itinerary> processWithAD(String router, List<Itinerary> tempOutput,
			Map<String, Object> requestedParams) {
		// definition.
		List<Itinerary> output = new ArrayList<Itinerary>();
		Boolean isValidLeg = true;

		// loop through each itinerary in the list.
		// loop through each leg of itinerary.
		// for each leg validate.
		// ** REQUIREMENT JAN 21. ***
		// update the itinerary(propagate the time in leg)
		// only if the alert is signaled by the SERVICE.

		// TRANSIT LEG
		// 1. in case of transit leg, just check the consistency
		// with the previous leg.
		// 2. check for alert delay associated with leg, add/propagate it.

		// NON TRANSIT LEG
		// 3. in case of non transit, simply check existing alert and
		// ** REQUIREMENT JAN 21. ***
		// update the itinerary(propagate the time in leg)
		// only if the alert is signaled by the SERVICE.

		for (Itinerary it : tempOutput) {

			AlertDelay alertD = null;
			List<AlertDelay> adBT = new ArrayList<AlertDelay>();
			List<Leg> tempLegs = new ArrayList<Leg>();
			Itinerary tempItn = it;

			for (int i = 0; i < it.getLeg().size(); i++) {

				Leg lg = it.getLeg().get(i);
				isValidLeg = true;

				// get leg start time.
				Long reqTime = lg.getStartime();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(reqTime);

				if (lg.getTransport().getType().equals(TType.BUS) || lg.getTransport().getType().equals(TType.TRAIN)) {

					// 1. check if alert exist.
					adBT = repositoryUtils.queryAlertDelay(router, lg.getTransport(), reqTime);

					if (adBT != null && !(adBT.isEmpty())) {
						alertD = adBT.get(0);
						// update AlertDelay in Leg.
						List<AlertDelay> alertL = new ArrayList<AlertDelay>();
						alertL.add(adBT.get(0));
						lg.setAlertDelayList(alertL);

						/**
						 * NEW REQUIREMENT JAN 21, 2013. Propagate time only if
						 * alert is signaled by SERVICE. impact on
						 * transit/non-transit leg(line 694).
						 */

						if (alertD.getCreatorType().equals(CreatorType.SERVICE)) {
							// propagate time.
							lg.setStartime(lg.getStartime() + alertD.getDelay());
							lg.setEndtime(lg.getEndtime() + alertD.getDelay());
							// check for arriveBy requirement.
							if (requestedParams != null && (requestedParams.get("arrivalTime") != null)) {
								isValidLeg = validateArriveBy(tempItn, alertD, requestedParams);
								if (!isValidLeg) {
									break;
								}
							}
							// update Itinerary end time, duration.
							tempItn.setEndtime(tempItn.getEndtime() + adBT.get(0).getDelay());
							tempItn.setDuration(tempItn.getDuration() + adBT.get(0).getDelay());
							tempLegs.add(lg);
						} else { // User Generated Delay.
							// add delay as it is without propagating time.
							lg.getAlertDelayList().get(0).setDescription("Warning! delay notified by User.");
							tempLegs.add(lg);
						}

					} else { // add non-delayed transit leg as it is.
						alertD = null;
						tempLegs.add(lg);
					}
					// check consistency with previous leg.
					if (i > 0) {
						Leg prevLeg = it.getLeg().get(i - 1);
						if (prevLeg != null) {
							if (prevLeg.getEndtime() > lg.getStartime()) {
								isValidLeg = false;
								break;
							}
						}
					}
				} else if (alertD != null) { // NON-TRANSIT LEG.
					if (alertD.getCreatorType().equals(CreatorType.SERVICE)) {
						// propagate time delay to non transit legs.
						lg.setStartime(lg.getStartime() + alertD.getDelay());
						lg.setEndtime(lg.getEndtime() + alertD.getDelay());
						// lg.setDuration(lg.getDuration() + alertD.getDelay());
					}
					tempLegs.add(lg);
				} else {
					tempLegs.add(lg);
				}
			}
			// if leg is not valid.
			if (!isValidLeg) {
				continue;
			} else {
				tempItn.setLeg(tempLegs);
				output.add(tempItn);
			}
		}
		return output;
	}

	/**
	 * Perform Validation for arriveBy time req.
	 * @param tempItn
	 * @param alertD
	 * @param requestedParams
	 * @return
	 */
	private Boolean validateArriveBy(Itinerary tempItn, AlertDelay alertD, Map<String, Object> requestedParams) {
		Boolean valid = true;
		String str_date = requestedParams.get("date") + " " + requestedParams.get("arrivalTime");

		try {
			DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy hh:mmaa", Locale.ITALY);
			Date mustArriveByDate = (Date) formatter.parse(str_date);

			if (mustArriveByDate.getTime() < (tempItn.getEndtime() + alertD.getDelay())) {
				valid = false;
			}
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return valid;
	}

	/**
	 * Filter Alert Strike
	 * @param router
	 * @param tempOutput
	 * @return
	 */
	private List<Itinerary> processWithAS(String router, List<Itinerary> tempOutput) {
		// definition.
		List<Itinerary> output = new ArrayList<Itinerary>();
		Boolean isValidLeg = true;

		// loop through each itinerary in the list.
		// loop through each leg of itinerary.
		// process legs with mode BUS,TRAIN.
		// for each leg check for corresponding AS if exists.
		// validate leg, ignore in case of validation failure.

		for (Itinerary it : tempOutput) {

			for (Leg lg : it.getLeg()) {
				isValidLeg = true;

				// get leg start time.
				Long reqTime = lg.getStartime();
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(reqTime);

				if (lg.getTransport().getType().equals(TType.BUS) || lg.getTransport().getType().equals(TType.TRAIN)) {
					// System.out.println("requested time->" + reqTime + " = "
					// + formatter.format(calendar.getTime()));
					List<AlertStrike> asBT = repositoryUtils.queryAlertStrike(router, lg.getTransport().getTripId(),
							reqTime);
					if (asBT != null && !(asBT.isEmpty())) {
						isValidLeg = false;
						break;
					}
				}
			}
			// if leg is not valid.
			if (!isValidLeg) {
				continue;
			} else {
				output.add(it);
			}
		}
		return output;
	}

	/**
	 * Fix to, from position strings in fluent legs.
	 * @param output
	 */
	public void fixNameString(List<Itinerary> output) {
		// fix name for fluid transit legs(WALK)
		int i = 0;
		for (Itinerary itn : output) {
			i = 0;
			for (Leg legW : itn.getLeg()) {
				String type = legW.getTransport().getType().name();
				if ((type.equalsIgnoreCase("BUS") || type.equalsIgnoreCase("TRAIN")) || type.equalsIgnoreCase("CAR")
						|| type.equalsIgnoreCase("BICYCLE")) {
					i++;
					continue;
				} else {
					// FROM Position.
					if (i > 0) { // only if it is not the first leg.
						Leg prev = itn.getLeg().get(i - 1);
						Position fromPos = prev.getTo();
						legW.setFrom(fromPos);
					}
					// TO Position.
					if (i + 1 < itn.getLeg().size()) {
						Leg next = itn.getLeg().get(i + 1);
						Position toPos = next.getFrom();
						legW.setTo(toPos);
					}
				}
				i++;
			}
		}
	}

	/**
	 * Harvesine Formula to get distance between coordinates.
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return distance
	 */
	public double calculateHarvesineDistance(double lat1, double lon1, double lat2, double lon2) {
		/**
		 * R = earth’s radius (mean radius = 6,371km) Δlat = lat2− lat1 Δlong =
		 * long2− long1 a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2) c =
		 * 2.atan2(√a, √(1−a)) d = R.c
		 */
		double distance = 0;
		final int R = 6371; // Radius of the earth km.
		Double latDistance = toRad(lat2 - lat1);
		Double lonDistance = toRad(lon2 - lon1);
		Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
				+ Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
		Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		distance = R * c;
		return distance;
	}

	private static Double toRad(Double value) {
		return value * Math.PI / 180;
	}

	public void removeShortWalksElements(List<Itinerary> itineraries) {
		for (Itinerary it : itineraries) {
			List<Leg> toRemove = Lists.newArrayList();
			for (int i = 0; i < it.getLeg().size() - 2; i++) {
				Leg leg1 = it.getLeg().get(i);
				Leg leg2 = it.getLeg().get(i + 1);
				Leg leg3 = it.getLeg().get(i + 2);
				if (leg1.getTransport().getType().equals(TType.BICYCLE)
						&& leg2.getTransport().getType().equals(TType.WALK)
						&& leg3.getTransport().getType().equals(TType.BICYCLE)) {
					leg1.setTo(leg3.getTo());
					leg1.setEndtime(leg3.getEndtime());
					leg1.setDuration(leg1.getEndtime() - leg1.getStartime());
					leg1.setLength(leg1.getLength() + leg2.getLength() + leg3.getLength());
					leg1.getAlertParkingList().addAll(leg3.getAlertParkingList());
					toRemove.add(leg2);
					toRemove.add(leg3);
				}
			}
			it.getLeg().removeAll(toRemove);
		}
	}

	public static String fixBicycleResponse(String json) {

		String response = "";
		JSONObject result = new JSONObject();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(org.codehaus.jackson.map.SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);

		JSONObject modifiedPlan = new JSONObject();
		JSONObject modifiedItineraries = new JSONObject();
		JSONObject modifiedLeg = new JSONObject();

		try {
			JsonNode root = mapper.readTree(json);
			if (root == null || !root.has("plan")) {
				return null;
			}
			JsonNode Itineraries = root.get("plan").get("itineraries");
			Iterator<JsonNode> elements = Itineraries.getElements();

			long distance = 0;
			long duration = 0;
			ArrayList<String> points = new ArrayList<String>();
			List<Location> locations = new ArrayList<Location>();

			if (elements.hasNext()) {
				JsonNode node = elements.next();
				modifiedItineraries.put("duration", node.get("duration").getLongValue());
				modifiedItineraries.put("startTime", node.get("startTime").getLongValue());
				modifiedItineraries.put("endTime", node.get("endTime").getLongValue());
				modifiedItineraries.put("walkTime", new JSONObject().NULL);
				JSONObject fromItn = new JSONObject();
				Iterator<String> fNames = root.get("plan").get("from").getFieldNames();
				while (fNames.hasNext()) {
					String key = fNames.next();
					fromItn.put(key, root.get("plan").get("from").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("from").get(key));
				}

				modifiedItineraries.put("from", fromItn);

				JSONObject toItn = new JSONObject();
				Iterator<String> tNames = root.get("plan").get("to").getFieldNames();
				while (tNames.hasNext()) {
					String key = tNames.next();
					toItn.put(key, root.get("plan").get("to").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("to").get(key));
				}

				modifiedItineraries.put("to", toItn);

				JsonNode legs = node.get("legs");

				// distance, geometery.
				Iterator<JsonNode> legIterator = legs.getElements();
				while (legIterator.hasNext()) {
					JsonNode tmpLeg = legIterator.next();
					distance = distance + tmpLeg.get("distance").getLongValue();
					duration = duration + tmpLeg.get("duration").getLongValue();
					points.add(tmpLeg.get("legGeometry").get("points").getTextValue());
				}

				for (String geometery : points) {
					locations.addAll(PolylineEncoder.decode(geometery, 1E-5));
				}

				JsonNode firstLeg = legs.get(0);
				JsonNode lastLeg = legs.get(legs.size() - 1);

				modifiedLeg.put("startTime", firstLeg.get("startTime").getLongValue());
				modifiedLeg.put("endTime", lastLeg.get("endTime").getLongValue());
				modifiedLeg.put("agencyId", new JSONObject().NULL);
				modifiedLeg.put("routeId", new JSONObject().NULL);
				modifiedLeg.put("routeShortName", new JSONObject().NULL);
				modifiedLeg.put("tripId", new JSONObject().NULL);

				JSONObject fromLeg = new JSONObject();
				Iterator<String> fromLegNames = firstLeg.get("from").getFieldNames();
				while (fromLegNames.hasNext()) {
					String key = fromLegNames.next();
					fromLeg.put(key, firstLeg.get("from").get(key).isNull() ? new JSONObject().NULL
							: firstLeg.get("from").get(key));
				}

				modifiedLeg.put("from", fromLeg);

				JSONObject toLeg = new JSONObject();
				Iterator<String> toLegNames = firstLeg.get("to").getFieldNames();
				while (toLegNames.hasNext()) {
					String key = toLegNames.next();
					toLeg.put(key,
							firstLeg.get("to").get(key).isNull() ? new JSONObject().NULL : firstLeg.get("to").get(key));
				}

				modifiedLeg.put("to", toLeg);
				modifiedLeg.put("duration", duration);
				modifiedLeg.put("distance", distance);
				String legGeometery = PolylineEncoder.encode(locations);
				JSONObject legGeomNode = new JSONObject();
				legGeomNode.put("points", legGeometery);
				legGeomNode.put("levels", new JSONObject().NULL);
				legGeomNode.put("length", legGeometery.length());
				modifiedLeg.put("legGeometry", legGeomNode);
				modifiedLeg.put("mode", "BICYCLE");

				JSONArray jsonlegs = new JSONArray();
				jsonlegs.put(modifiedLeg);
				modifiedItineraries.put("legs", jsonlegs);
				// plan.
				modifiedPlan.put("date", root.get("plan").get("date").getLongValue());
				// from plan.
				JSONObject fromPlan = new JSONObject();
				Iterator<String> fromPlanFieldNames = root.get("plan").get("from").getFieldNames();
				while (fromPlanFieldNames.hasNext()) {
					String key = fromPlanFieldNames.next();
					fromPlan.put(key, root.get("plan").get("from").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("from").get(key));
				}

				modifiedPlan.put("from", fromPlan);
				// to plan.
				JSONObject toPlan = new JSONObject();
				Iterator<String> toPlanFieldNames = root.get("plan").get("to").getFieldNames();
				while (toPlanFieldNames.hasNext()) {
					String key = toPlanFieldNames.next();
					toPlan.put(key, root.get("plan").get("to").get(key).isNull() ? new JSONObject().NULL
							: root.get("plan").get("to").get(key));
				}

				modifiedPlan.put("to", toPlan);

				JSONArray itineraries = new JSONArray();
				itineraries.put(modifiedItineraries);
				modifiedPlan.put("itineraries", itineraries);
			}

			result.put("plan", modifiedPlan);
			response = result.toString();

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return response;

	}

}
