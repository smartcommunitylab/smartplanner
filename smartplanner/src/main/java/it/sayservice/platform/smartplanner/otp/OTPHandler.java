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

package it.sayservice.platform.smartplanner.otp;

import it.sayservice.platform.smartplanner.cache.annotated.AnnotatedReader;
import it.sayservice.platform.smartplanner.cache.annotated.AnnotatedTrip;
import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;
import it.sayservice.platform.smartplanner.data.message.otpbeans.TransitStops;
import it.sayservice.platform.smartplanner.otp.schedule.Timetable;
import it.sayservice.platform.smartplanner.otp.schedule.TransitTimes;
import it.sayservice.platform.smartplanner.otp.schedule.TripSchedule;
import it.sayservice.platform.smartplanner.otp.schedule.TripTimeEntry;
import it.sayservice.platform.smartplanner.otp.schedule.TripTimes;
import it.sayservice.platform.smartplanner.otp.schedule.Trips;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayException;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayFilter;
import it.sayservice.platform.smartplanner.utils.Agency;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.HTTPConnector;
import it.sayservice.platform.smartplanner.utils.UnZip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.gdata.util.io.base.UnicodeReader;

@Component
@EnableConfigurationProperties(RouterConfig.class)
public class OTPHandler {

	public RouterConfig routerConfig;
	private AnnotatedReader annotatedReader;
	private String otpURL;

	private Map<String, Map<String, String>> routerTripsMap = new HashMap<String, Map<String, String>>();

	private Map<String, String> trips;
	private Map<String, String> tripsCalendar;

	public Map<String, String> agencyTripFile;
	public Map<String, String> agencyStopFile;

	public OTPHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public OTPHandler(String router) {
		annotatedReader = new AnnotatedReader(router, this);
	}

	// @PostConstruct
	public void init(RouterConfig routerConfig) throws IOException {
		this.routerConfig = routerConfig;
		this.otpURL = routerConfig.getOtpEndpoint() + routerConfig.getRouter() + Constants.API_TRANSIT;
		agencyStopFile = new HashMap<String, String>();
		agencyTripFile = new  HashMap<String, String>();
		for (String key : routerConfig.getPublicTransport().keySet()) {
			Agency agency = routerConfig.getPublicTransport().get(key);
			agencyTripFile.put(agency.getAgencyId(), agency.getTripTxt());
			agencyStopFile.put(agency.getAgencyId(), agency.getStopTxt());
			
			/** extract agency gtfs file to $OTP_HOME/cache/schedule folder. start **/
			try {
				UnZip.unZipIt(
						System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
								+ System.getProperty("file.separator") + Constants.GTFS_FOLDER_PATH
								+ System.getProperty("file.separator") + agency.getAgencyId() + ".zip",
						System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
								+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
								+ System.getProperty("file.separator") + agency.getAgencyId()
								+ System.getProperty("file.separator"));
			} catch (Exception e) {
				System.err.println("gtfs not extracted for agency id: " + agency.getAgencyId());
			}
			/** extract agency gtfs file to $OTP_HOME/cache/schedule folder. end **/

		}
		
		buildTrips(routerConfig.getRouter());

	}

	public void setOtpURL(String otpURL) {
		this.otpURL = otpURL;
	}

	/**
	 * TEST MODE.
	 * 
	 * @param otpURL
	 * @throws IOException
	 */
	public OTPHandler(String router, String otpURL) throws IOException {
		InputStream in = new FileInputStream(new File("src/main/resources/"+ router+ ".yml"));
		Yaml yaml = new Yaml();
		routerConfig = yaml.loadAs(in, RouterConfig.class);
		this.otpURL = routerConfig.getOtpEndpoint() + routerConfig.getRouter() + Constants.API_TRANSIT;
		agencyStopFile = new HashMap<String, String>();
		agencyTripFile = new  HashMap<String, String>();
		for (String key : routerConfig.getPublicTransport().keySet()) {
			Agency agency = routerConfig.getPublicTransport().get(key);
			agencyTripFile.put(agency.getAgencyId(), agency.getTripTxt());
			agencyStopFile.put(agency.getAgencyId(), agency.getStopTxt());
			/** extract agency gtfs file to $OTP_HOME/cache/schedule folder. start **/
			try {
				UnZip.unZipIt(
						System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
								+ System.getProperty("file.separator") + Constants.GTFS_FOLDER_PATH
								+ System.getProperty("file.separator") + agency.getAgencyId() + ".zip",
						System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
								+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
								+ System.getProperty("file.separator") + agency.getAgencyId()
								+ System.getProperty("file.separator"));
			} catch (Exception e) {
				System.err.println("gtfs not extracted for agency id: " + agency.getAgencyId());
			}
			/** extract agency gtfs file to $OTP_HOME/cache/schedule folder. end **/
		}

		buildTrips(routerConfig.getRouter());

	}

	public List<Route> getRoutes(String router) {
		List<Route> result = new ArrayList<Route>();

		try {
			String res = HTTPConnector.doGet(otpURL + Constants.OP_ROUTES, "", null, MediaType.APPLICATION_JSON);

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			ArrayList list = mapper.readValue(res, ArrayList.class);

			for (Object o : list) {
				Map<String, Object> tmpMap = mapper.convertValue(o, Map.class);
				// Route route = mapper.convertValue(tmpMap.get("RouteType"),
				// Route.class);
				// new version.
				String[] ids = tmpMap.get("id").toString().split(":");
				String agencyId = ids[0];
				String routeId = ids[1];

				Route route = new Route();
				Id id = new Id();
				id.setAgency(agencyId);
				id.setId(routeId);
				route.setId(id);
				if (tmpMap.containsKey("longName"))
				route.setRouteLongName(tmpMap.get("longName").toString());
				if (tmpMap.containsKey("shortName"))
				route.setRouteShortName(tmpMap.get("shortName").toString());

				result.add(route);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<TransitStops> getRouteData(String agencyId, String routeId) throws Exception {
		List<TransitStops> result = new ArrayList<TransitStops>();

		try {
			String routeIdOTP = agencyId + ":" + routeId;
			String res = HTTPConnector.doGet(otpURL + Constants.OP_ROUTES + "/" + routeIdOTP + "/stops", "",
					MediaType.APPLICATION_JSON, null);
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

			// new version.
			List routeStopsList = mapper.readValue(res, List.class);

			List<String> stopsId = new ArrayList<String>();
			for (Object rstopObject : routeStopsList) {
				Map stopObjectMap = (Map) rstopObject;
				String[] ids = stopObjectMap.get("id").toString().split(":");
				stopsId.add(ids[1]);

			}

			TransitStops trip = new TransitStops();
			trip.setAgency(agencyId);
			trip.setId(routeId);
			trip.getStopsId().addAll(stopsId);

			result.add(trip);
			// }

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<StopTime> getTimes(String router, String agencyId, String routeId, String stopId, long from, long to)
			throws Exception {
		List<StopTime> result = new ArrayList<StopTime>();

		try {
			Long timeInterval = Math.abs(to - from) / 1000; // otp expect
															// seconds since
															// midnight.

			String stopIdOTP = agencyId + ":" + stopId;
			String res = HTTPConnector.doGet(otpURL + Constants.OP_STOPS + "/" + stopIdOTP + Constants.OP_STOPTIMES,
					"startTime=" + from / 1000 + "&timeRange=" + timeInterval + "&numberOfDepartures=100", null,
					MediaType.APPLICATION_JSON);

			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();

			JsonNode root = mapper.readTree(res);

			List<String> used = new ArrayList<String>();

			ArrayNode rootList = mapper.convertValue(root, ArrayNode.class);

			for (JsonNode pattern : rootList) {
				ArrayNode times = mapper.convertValue(pattern.get("times"), ArrayNode.class);
				for (JsonNode timeNode : times) {
					String[] ids = timeNode.get("tripId").asText().split(":");
//					long time = SmartPlannerUtils.addSecondsToTimeStamp(from, timeNode.get("scheduledDeparture").asInt());
					long time = SmartPlannerUtils.computeDate(timeNode.get("scheduledDeparture").asInt(), timeNode.get("serviceDay").asLong() * 1000);
					Id id = new Id();
					id.setAgency(agencyId);
					id.setId(ids[1]);
					String u = time + "_" + id.getId() + id.getAgency();
					if (used.contains(u)) {
						continue;
					}

					String tripId = id.getId();
					String tripRouteId = routerTripsMap.get(router).get(agencyId + "_" + tripId);

					StopTime stopTime = new StopTime();
					stopTime.setTime(time);
					stopTime.setTrip(id);

					if (routeId == null || tripRouteId != null && tripRouteId.equals(routeId)) {
						result.add(stopTime);
						used.add(u);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Collections.sort(result);
		return result;
	}

	public Multimap<String, StopTime> getTimesByRoutes(String router, String agencyId, String stopId, long from,
			long to) throws Exception {
		Multimap<String, StopTime> result = ArrayListMultimap.create();

		Long timeInterval = Math.abs(to - from) / 1000; // otp expect seconds
														// since midnight.

		try {
			String stopIdOTP = agencyId + ":" + stopId;
			String res = HTTPConnector.doGet(otpURL + Constants.OP_STOPS + "/" + stopIdOTP + Constants.OP_STOPTIMES,
					"startTime=" + from / 1000 + "&timeRange=" + timeInterval + "&numberOfDepartures=100", null,
					MediaType.APPLICATION_JSON);
			com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
			JsonNode root = mapper.readTree(res);
			List<String> used = new ArrayList<String>();

			ArrayNode rootList = mapper.convertValue(root, ArrayNode.class);

			for (JsonNode pattern : rootList) {
				ArrayNode times = mapper.convertValue(pattern.get("times"), ArrayNode.class);
				for (JsonNode timeNode : times) {
					String[] ids = timeNode.get("tripId").asText().split(":");
//					long time = SmartPlannerUtils.addSecondsToTimeStamp(from, timeNode.get("scheduledDeparture").asInt());
					long time = SmartPlannerUtils.computeDate(timeNode.get("scheduledDeparture").asInt(), timeNode.get("serviceDay").asLong() * 1000);

					Id id = new Id();
					id.setAgency(agencyId);
					id.setId(ids[1]);
					String u = time + "_" + id.getId() + id.getAgency();
					if (used.contains(u)) {
						continue;
					}

					String tripId = id.getId();
					String tripRouteId = routerTripsMap.get(router).get(agencyId + "_" + tripId);

					if (tripRouteId != null) {
						StopTime stopTime = new StopTime();
						stopTime.setTime(time);
						stopTime.setTrip(id);
						result.put(tripRouteId, stopTime);
						used.add(u);
					} else {
						System.err.println("ERROR: missing tripId " + agencyId + "_" + tripId);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<Map<String, Object>> getStops(String router) throws Exception {

		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();

		for (String agencyId : agencyStopFile.keySet()) {
			List<String[]> lines = readCSV((System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
					+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH + System.getProperty("file.separator")
					+ agencyId + System.getProperty("file.separator") + Constants.GTFS_STOP));

			if (lines.get(0) != null) {

				int idIndex = getFieldIndex(Constants.STOP_ID, lines.get(0));
				int nameIndex = getFieldIndex(Constants.STOP_NAME, lines.get(0));
				int latIndex = getFieldIndex(Constants.STOP_LAT, lines.get(0));
				int lonIndex = getFieldIndex(Constants.STOP_LON, lines.get(0));
				int wheelChairIndex = getFieldIndex(Constants.STOP_WHEELCHAIR_BOARDING, lines.get(0));
			
				for (int i = 1; i < lines.size(); i++) {

					String[] words = lines.get(i);

					try {
						String id = words[idIndex];
						String name = words[nameIndex].trim();
						double latitude = Double.parseDouble(words[latIndex].trim());
						double longitude = Double.parseDouble(words[lonIndex].trim());

						int wheelChairBoarding = 0;
						if (wheelChairIndex > -1 && !words[wheelChairIndex].trim().isEmpty()) {
							wheelChairBoarding = Integer.parseInt(words[wheelChairIndex].trim());
						}

						Map<String, Object> stop = new TreeMap<String, Object>();

						stop.put("stopId", id);
						stop.put("name", name);
						double ll[] = new double[2];
						ll[0] = latitude;
						ll[1] = longitude;
						stop.put("coordinates", ll);
						stop.put("agencyId", agencyId);
						stop.put("wheelChairBoarding", wheelChairBoarding);
						result.add(stop);
					} catch (Exception e) {
						System.out.println("Error parsing stop: " + words[0]);
					}
				}
			}
		}

		return result;
	}

	private int getFieldIndex(String fieldName, String[] heading) {
		int index = -1;

		for (int i = 0; i < heading.length; i++) {
			if (fieldName.equalsIgnoreCase(heading[i].trim())) {
				index = i;
				break;
			}
		}
		return index;
	}

	public void buildTrips(String router) throws IOException {

		trips = new TreeMap<String, String>();
		tripsCalendar = new TreeMap<String, String>();

		for (String agencyId : agencyTripFile.keySet()) {
			List<String[]> lines = readCSV((System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
					+ Constants.SCHEDULES_FOLDER_PATH + System.getProperty("file.separator") + agencyId
					+ System.getProperty("file.separator") + Constants.GTFS_TRIPS));

			// route_id,service_id,trip_id,trip_headsign,direction_id,shape_id,wheelchair_accessible
			int routeIdIndex = getFieldIndex(Constants.TRIP_ROUTE_ID, lines.get(0));
			int tripIdIndex = getFieldIndex(Constants.TRIP_ID, lines.get(0));
			int tripServiceIdIndex = getFieldIndex(Constants.TRIP_SERVICE_ID, lines.get(0));
//			int tripWheelChairIndex = getFieldIndex(Constants.TRIP_WHEELCHAIR_ACCESSIBLE, lines.get(0));
		
			for (int i = 1; i < lines.size(); i++) {

				String[] words = lines.get(i);
			
				try {
					String id = words[tripIdIndex]; //2
					String calendar = words[tripServiceIdIndex]; // 1
					String route = words[routeIdIndex]; //0
					String newId = agencyId + "_" + id;
					trips.put(newId, route);
					tripsCalendar.put(newId, calendar);
				} catch (Exception e) {
					System.out.println("Error parsing trip: " + words[routeIdIndex]);
				}
			}

		}
		routerTripsMap.put(router, trips);
	}

	// starts from sunday
	private List<Integer> convertDays(boolean[] b) {
		List<Integer> days = new ArrayList<Integer>();
		for (int i = 0; i < 6; i++) {
			if (b[i]) {
				days.add((i + 2));
			}
		}
		if (b[6]) {
			days.add(1);
		}

		return days;
	}

	private Map<String, String> buildFileList(String format) {
		Map<String, String> result = new TreeMap<String, String>();
		// String[] ids = agencyIds.split(",");
		for (String agencyId : agencyStopFile.keySet()) {
			result.put(agencyId, String.format(format, agencyId));
		}
		return result;
	}

	public Map<String, String> getTrips() {
		return trips;
	}

	public Map<String, String> getTripsCalendar() {
		return tripsCalendar;
	}

	/**
	 * build transit trips
	 * 
	 * @param agencyId
	 * @param agency
	 * @return List<Timetable>
	 * @throws IOException
	 */
	public List<Timetable> buildTransitTripSchedules(String router, String agencyId) throws IOException {
		Collection<TransitTimes> busTimetable = buildTransitTripTimes(router, agencyId);
		List<Timetable> result = new ArrayList<Timetable>();

		for (TransitTimes btt : busTimetable) {
			Timetable timetable = new Timetable();
			timetable.setStopsIds(btt.getStopIds());
			timetable.setRouteId(btt.getRouteId());
			List<TripSchedule> schedules = new ArrayList<TripSchedule>();
			for (TripTimes tt : btt.getTimes()) {
				String times[] = new String[btt.getStopIds().size()];
				TripSchedule ts = new TripSchedule();
				ts.setAgencyId(agencyId);
				ts.setTimes(times);
				ts.setRouteId(btt.getRouteId());
				ts.setTripId(tt.getTripId());
				if (tt.getExceptions() != null) {
					ts.setDaysAdded(tt.getExceptions().getAdded());
					ts.setDaysRemoved(tt.getExceptions().getRemoved());
				}
				ts.setFromDate(tt.getFromDate());
				ts.setToDate(tt.getToDate());

				String lastTime = "";
				int index = 0;
				int assigned = 0;
				for (TripTimeEntry sched : tt.getTripTimes()) {
					boolean found = false;
					for (int i = index; i < btt.getStopIds().size(); i++) {
						if (btt.getStopIds().get(i).equals(sched.getStopId())) {
							if (ts.getTimes()[i] != null) {
								// System.err.println("WARN OVERWRITE: " +
								// ts.getRouteId());
								continue;
							}
							// if (index >= i && index != 0) {
							// System.err.println("WARN INDEX: " + index + " / "
							// + i);
							// }
							if (lastTime.length() > 0 && sched.getTime().compareTo(lastTime) < 0) {
								// System.err.println("WARN TIMES: " + lastTime
								// + " / " +
								// sched.getTime());
							}
							// System.out.println("CHECK: " + lastTime + " / " +
							// sched.getTime());
							index = i;
							found = true;
							break;
						}
					}
					if (!found) {
						for (int i = 0; i < index; i++) {
							if (btt.getStopIds().get(i).equals(sched.getStopId())) {
								if (ts.getTimes()[i] != null) {
									// System.err.println("WARN OVERWRITE: " +
									// ts.getRouteId());
									continue;
								}
								// if (index >= i && index != 0) {
								// System.err.println("WARN INDEX: " + index +
								// " / " + i);
								// }
								if (lastTime.length() > 0 && sched.getTime().compareTo(lastTime) < 0) {
									// System.err.println("WARN TIMES: " +
									// lastTime + " / " +
									// sched.getTime()+" "+btt.getRouteId() +
									// " "+tt.getTripId()
									// +" ");
								}
								// System.out.println("CHECK: " + lastTime +
								// " / " +
								// sched.getTime());
								index = i;
								found = true;
								break;
							}
						}
					}
					// if (assigned == tt.getTripTimes().size()) {
					// System.out.println("ENOUGH");
					// index = -1;
					// break;
					// }
					try {
						if (found) {
							ts.getTimes()[index] = sched.getTime();
							lastTime = sched.getTime();
							assigned++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// System.out.println("CHECK SIZE:" + assigned + " / " +
				// tt.getTripTimes().size());
				if (assigned != tt.getTripTimes().size()) {
					System.err.println("WARN SIZE " + btt.getRouteId() + " " + tt.getTripId() + " " + assigned + " / "
							+ tt.getTripTimes().size() + " / " + Arrays.asList(ts.getTimes()));
				}
				// System.out.println("CHECK SIZE:" + assigned + " / " +
				// tt.getTripTimes());

				ts.fill();
				// ts.checkComplete();
				ts.setDays(tt.getDays());

				// ts.check();
				schedules.add(ts);

			}

			timetable.setSchedules(schedules);
			result.add(timetable);
		}

		return result;
	}

	/**
	 * Build Transit Schedules.
	 * 
	 * @param agencyId
	 * @return
	 * @throws IOException
	 */
	private Collection<TransitTimes> buildTransitTripTimes(String router, String agencyId) throws IOException {
		System.out.println("Initializing agency " + agencyId);

		List<TripTimeEntry> schedules = readAgencySchedule(router, agencyId);
		Map<String, Trips> agencyTrips = readAgencyTrips(router, agencyId);
		Map<String, WeekdayFilter> weekdayFilter = readAgencyWeekDay(router, agencyId);
		Map<String, WeekdayException> weekdayException = readAgencyWeekDayExceptions(router, agencyId);

		Map<String, TripTimes> tripsTimes = new TreeMap<String, TripTimes>();

		for (TripTimeEntry schedule : schedules) {
			TripTimes tripTimes;
			String tripId = schedule.getTripId();
			if (tripsTimes.containsKey(tripId)) {
				tripTimes = tripsTimes.get(tripId);
			} else {
				tripTimes = new TripTimes();
				try {
					tripTimes.setRecurrence(agencyTrips.get(tripId).getTripRecurrence());
				} catch (Exception e) {
					e.printStackTrace();
				}
				tripTimes.setTripId(schedule.getTripId());
			}
			tripTimes.getTripTimes().add(schedule);
			tripsTimes.put(tripId, tripTimes);
		}

		Map<String, String> tripsByRoute = new TreeMap<String, String>();
		for (Trips trip : agencyTrips.values()) {
			for (String tripId : trip.getTripIds()) {
				tripsByRoute.put(tripId, trip.getRouteId());
			}
		}

		Map<String, TransitTimes> result = new TreeMap<String, TransitTimes>();
		for (String key : tripsByRoute.keySet()) {
			String route = tripsByRoute.get(key);
			TransitTimes timetable;
			if (result.containsKey(route)) {
				timetable = result.get(route);
			} else {
				timetable = new TransitTimes();
				timetable.setRouteId(tripsByRoute.get(key));
			}

			TripTimes tt = tripsTimes.get(key);
			Trips trip = agencyTrips.get(key);

			try {
				List<Integer> days = convertDays(weekdayFilter.get(trip.getTripRecurrence()).getDays());
				tt.setDays(days);
			} catch (Exception e) {
				e.printStackTrace();
			}

			WeekdayException ex = weekdayException.get(trip.getTripRecurrence());
			if (ex == null) {
				ex = new WeekdayException();
			}
			tt.setExceptions(ex);
			tt.setFromDate(weekdayFilter.get(trip.getTripRecurrence()).getFromDate());
			tt.setToDate(weekdayFilter.get(trip.getTripRecurrence()).getToDate());

			int b = timetable.getTimes().size();
			timetable.getTimes().add(tt);
			if (b == timetable.getTimes().size()) {
				System.err.println("ERROR: " + tt.getTripId());
			}

			result.put(route, timetable);
		}

		for (TransitTimes tt : result.values()) {
			Collections.sort(tt.getTimes());
			tt.buildStopIds();
		}

		return result.values();

	}

	public List<AnnotatedTrip> buildAnnotatedTrips(String router, String agencyId) throws Exception {
		String fileName = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + "cache" + System.getProperty("file.separator") + "client"
				+ System.getProperty("file.separator") + Constants.AUXILIARY_CACHE_DIR
				+ System.getProperty("file.separator") + agencyId + "_" + Constants.ANNOTATED_TRIPS + ".txt";
		File f = new File(fileName);

		List<AnnotatedTrip> annotatedTrips = Lists.newArrayList();
		if (f.exists()) {
			ObjectMapper mapper = new ObjectMapper();
			List maps = mapper.readValue(f, List.class);
			for (Object o : maps) {
				AnnotatedTrip at = mapper.convertValue(o, AnnotatedTrip.class);
				annotatedTrips.add(at);
			}
			return annotatedTrips;
		} else {
			return new ArrayList<AnnotatedTrip>();
		}
	}

	private List<String> readStopsOrderFromStopsFile(String agencyId, String routeId) throws IOException {
		String fileName = System.getenv("OTP_HOME") + System.getProperty("file.separator") + "cache"
				+ System.getProperty("file.separator") + Constants.CACHE_STOPS_ORDER
				+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator") + routeId
				+ ".txt";
		File file = new File(fileName);
		if (file.exists()) {
			System.out.println("OK for " + routeId);

			List<String[]> lines = readCSV(fileName);
			List<String> idOrder = Lists.newArrayList();
			for (String[] id : lines) {
				idOrder.add(id[0]);
			}

			return idOrder;
		}
		return null;
	}

	public Map<String, WeekdayException> readAgencyWeekDayExceptions(String router, String agencyId)
			throws IOException {
		Map<String, WeekdayException> entries = new TreeMap<String, WeekdayException>();

		List<String[]> lines = readCSV(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
				+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator")
				+ Constants.GTFS_CALENDAR_DATE);

		// service_id,date,exception_type
		if (!lines.isEmpty()) {
			int serviceIdIndex = getFieldIndex(Constants.CDATES_SERVICE_ID, lines.get(0));
			int dateIndex = getFieldIndex(Constants.CDATES_DATE, lines.get(0));
			int exceptionTypeIndex = getFieldIndex(Constants.CDATES_EXCEPTION_TYPE, lines.get(0));

			for (int i = 1; i < lines.size(); i++) {

				String[] words = lines.get(i);

				try {
					String name = agencyId + "_" + words[serviceIdIndex].trim(); // new
					String date = words[dateIndex].trim();
					String type = words[exceptionTypeIndex].trim();
					WeekdayException wde;
					if (entries.containsKey(name)) {
						wde = entries.get(name);
					} else {
						wde = new WeekdayException();
					}
					if ("1".equals(type)) {
						wde.getAdded().add(date);
					} else if ("2".equals(type)) {
						wde.getRemoved().add(date);
					}

					entries.put(name, wde);
				} catch (Exception e) {
					System.out.println("Error parsing weekdays exception");
					e.printStackTrace();
				}
			}
		}
		
		return entries;

	}

	public Map<String, WeekdayFilter> readAgencyWeekDay(String router, String agencyId) throws IOException {

		Map<String, WeekdayFilter> entries = new TreeMap<String, WeekdayFilter>();

		List<String[]> lines = null;

		try {
			lines = readCSV(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
					+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
					+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator")
					+ Constants.GTFS_CALENDAR);
			if (lines.size() <= 1) { // in case of empty calendar.txt file with just header throw exception
				throw new ArrayIndexOutOfBoundsException();
			}	
		} catch (Exception e) {

			// if calendar.txt is missing, construct entries using calendar_dates.txt
			List<String[]> linesEx = readCSV(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
					+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
					+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator")
					+ Constants.GTFS_CALENDAR_DATE);

			Map<String, String> serviceStartDate = new HashMap<String, String>();
			Map<String, String> serviceEndDate = new HashMap<String, String>();
			
			// service_id,date,exception_type
			int serviceIdIndex = getFieldIndex(Constants.CDATES_SERVICE_ID, linesEx.get(0));
			int dateIndex = getFieldIndex(Constants.CDATES_DATE, linesEx.get(0));
//			int exceptionTypeIndex = getFieldIndex(Constants.CDATES_EXCEPTION_TYPE, lines.get(0));
			boolean b[] = new boolean[7];

			for (int i = 1; i < linesEx.size(); i++) {

				String[] words = linesEx.get(i);

				try {
					String name = agencyId + "_" + words[serviceIdIndex].trim(); // new
					String date = words[dateIndex].trim();

					if (!serviceStartDate.containsKey(name)) {
						serviceStartDate.put(name, date);
					} else {
						serviceEndDate.put(name, date);
					}
				} catch (Exception e1) {
					System.out.println("Error parsing weekdays exception");
					e1.printStackTrace();
				}

			}

			for (String key : serviceStartDate.keySet()) {
				String name = key;
				String startDate = serviceStartDate.get(name);
				String endDate = null;
				if (serviceEndDate.containsKey(name)) {
					endDate = serviceEndDate.get(name);
				} else {
					endDate = startDate;
				}

				WeekdayFilter wdf = new WeekdayFilter();
				wdf.setName(name);
				wdf.setDays(b);
				wdf.setFromDate(startDate);
				wdf.setToDate(endDate);
				entries.put(name, wdf);

			}
		}

		if (lines != null) {
			
			// service_id,date,exception_type
			int serviceIdIndex = getFieldIndex(Constants.CAL_SERVICE_ID, lines.get(0));
//			int monIndex = getFieldIndex(Constants.CAL_MON, lines.get(0));
//			int tueIndex = getFieldIndex(Constants.CAL_TUE, lines.get(0));
//			int wedIndex = getFieldIndex(Constants.CAL_WED, lines.get(0));
//			int thrIndex = getFieldIndex(Constants.CAL_THR, lines.get(0));
//			int friIndex = getFieldIndex(Constants.CAL_FRI, lines.get(0));
//			int satIndex = getFieldIndex(Constants.CAL_SAT, lines.get(0));
//			int sunIndex = getFieldIndex(Constants.CAL_SUN, lines.get(0));
			int startDateIndex = getFieldIndex(Constants.CAL_START_DATE, lines.get(0));
			int endDateIndex = getFieldIndex(Constants.CAL_END_DATE, lines.get(0));
			
			for (int i = 1; i < lines.size(); i++) {

				String[] words = lines.get(i);
			
				try {
					String name = agencyId + "_" + words[serviceIdIndex]; // new
					boolean b[] = new boolean[7];
					for (int d = 1; d < 8; d++) {
						b[d - 1] = words[d].equals("1") ? true : false;
					}
					String startDate = words[startDateIndex].trim(); //8
					String endDate = words[endDateIndex].trim(); //9
					WeekdayFilter wdf = new WeekdayFilter();
					wdf.setName(name);
					wdf.setDays(b);
					wdf.setFromDate(startDate);
					wdf.setToDate(endDate);
					entries.put(name, wdf);
					
				} catch (Exception e) {
					System.out.println("Error parsing weekdays filter");
					e.printStackTrace();
				}
			}
		}

		return entries;

	}

	private Map<String, Trips> readAgencyTrips(String router, String agencyId) throws IOException {
		Map<String, Trips> entries = new TreeMap<String, Trips>();

		List<String[]> lines = readCSV(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
				+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator")
				+ Constants.GTFS_TRIPS);

		// route_id,service_id,trip_id,trip_headsign,direction_id,shape_id,wheelchair_accessible
		int routeIdIndex = getFieldIndex(Constants.TRIP_ROUTE_ID, lines.get(0));
		int tripIdIndex = getFieldIndex(Constants.TRIP_ID, lines.get(0));
		int tripServiceIdIndex = getFieldIndex(Constants.TRIP_SERVICE_ID, lines.get(0));

		for (int i = 1; i < lines.size(); i++) {

			String[] words = lines.get(i);
			try {
				String routeId = words[routeIdIndex].trim();
				String recurrence = agencyId + "_" + words[tripServiceIdIndex].trim(); // new
				String tripId = words[tripIdIndex].trim();
				Trips trips = new Trips();
				trips.setRouteId(routeId);
				trips.getTripIds().add(tripId);
				trips.setTripRecurrences(recurrence);
				entries.put(tripId, trips);
			} catch (Exception e) {
				System.out.println("Error parsing schedule " + words[4]);
				e.printStackTrace();
			}
		}

		return entries;
	}

	private List<TripTimeEntry> readAgencySchedule(String router, String agencyId) throws IOException {
		List<TripTimeEntry> entries = new ArrayList<TripTimeEntry>();

		List<String[]> lines = readCSV(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.SCHEDULES_FOLDER_PATH
				+ System.getProperty("file.separator") + agencyId + System.getProperty("file.separator")
				+ Constants.GTFS_STOPTIMES);

		String lastStopId = null;
		String lastTime = null;
		String lastTripId = null;
		int lastSequence = 1;
		
		// route_id,service_id,trip_id,trip_headsign,direction_id,shape_id,wheelchair_accessible
		int tripIdIndex = getFieldIndex(Constants.STIMES_TRIP_ID, lines.get(0));
		int stopIdIndex = getFieldIndex(Constants.STIMES_STOP_ID, lines.get(0));
		int stopSequenceIndex = getFieldIndex(Constants.STIMES_STOP_SEQ, lines.get(0));
		int arrivalTimeIndex = getFieldIndex(Constants.STIMES_ARRIVAL_TIME, lines.get(0));
		int departureTimeIndex = getFieldIndex(Constants.STIMES_DEPARTURE_TIME, lines.get(0));
	
		for (int i = 1; i < lines.size(); i++) {

			String[] words = lines.get(i);
			
			try {
				
				String tripId = words[tripIdIndex].trim(); // 0
				String time = words[arrivalTimeIndex].trim(); // 1
				
				if (time.length() == 8) {
					time = time.substring(0, 5);
				}
				
				String stopId = words[stopIdIndex].trim(); // 3
				
				int sequence = Integer.parseInt(words[stopSequenceIndex].trim()); //4
				
				TripTimeEntry entry = new TripTimeEntry();

				if (tripId.equals(lastTripId)) {
					lastSequence++;
				} else {
					lastSequence = 1;
				}

				entry.setSequence(lastSequence);
				entry.setStopId(stopId);
				entry.setTime(time);
				entry.setTripId(tripId);

				if (lastStopId == null || !lastStopId.equals(stopId) || !lastTime.equals(time) || !lastTripId.equals(tripId)) {
					entries.add(entry);
				}
				lastStopId = stopId;
				lastTime = time;
				lastTripId = tripId;
			} catch (Exception e) {
				System.out.println("Error parsing schedule " + words[stopSequenceIndex]);
				e.printStackTrace();
			}
		}


		return entries;
	}

	public List readAgencyAnnotatedInfo(String router, String agencyId)
			throws JsonParseException, JsonMappingException, IOException {
		String infoFile = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR + System.getProperty("file.separator") + Constants.AUXILIARY_CACHE_DIR
				+ System.getProperty("file.separator") + agencyId + "_info.txt";
		ObjectMapper mapper = new ObjectMapper();

		File f = new File(infoFile);
		if (!f.exists()) {
			return null;
		}
		List list = mapper.readValue(f, List.class);
		return list;
	}

	public Map readSymbolicTrips(String agencyId) throws JsonParseException, JsonMappingException, IOException {
		String infoFile = System.getenv("OTP_HOME") + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + Constants.CLIENT_CACHE_DIR
				+ System.getProperty("file.separator") + Constants.AUXILIARY_CACHE_DIR
				+ System.getProperty("file.separator") + agencyId + "_symbolic_trips.txt";
		ObjectMapper mapper = new ObjectMapper();

		File f = new File(infoFile);
		if (!f.exists()) {
			return null;
		}
		Map map = mapper.readValue(f, Map.class);
		return map;
	}

	private List<String[]> readCSV(String fileName) {
		List<String[]> lines = new ArrayList<String[]>();
		try {
			FileInputStream fis = new FileInputStream(new File(fileName));
			UnicodeReader ur = new UnicodeReader(fis, "UTF-8");
			for (CSVRecord record : CSVFormat.DEFAULT.parse(ur)) {
				String[] line = Iterables.toArray(record, String.class);
				lines.add(line);
			}
			lines.get(0)[0] = lines.get(0)[0].replaceAll(Constants.UTF8_BOM, "");
			return lines;
		} catch (IOException e) {
			return lines;
		}
	}

	public RouterConfig getRouterConfig() {
		return routerConfig;
	}

	public void clean() {
//		agencyTripFile.clear();
//		agencyStopFile.clear();
	}

	public List<Object> getAgencies(String router) throws Exception {
		
		List<Object> result = new ArrayList<Object>();
		String res = HTTPConnector.doGet(otpURL + Constants.AGENCIES, null, null, MediaType.APPLICATION_JSON);
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		ArrayList list = mapper.readValue(res, ArrayList.class);

		for (Object o : list) {
			result.add(o);
		}
		return result;
	}

}