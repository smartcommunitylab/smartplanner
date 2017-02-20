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

import it.sayservice.platform.smartplanner.cache.annotated.AnnotatedTrip;
import it.sayservice.platform.smartplanner.cache.annotated.SymbolicRouteDayInfo;
import it.sayservice.platform.smartplanner.cache.annotated.SymbolicRouteDayInfoHashCalendar;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;
import it.sayservice.platform.smartplanner.data.message.otpbeans.ExtendedTransitTimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.GeolocalizedStopRequest;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Id;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.StopTime;
import it.sayservice.platform.smartplanner.data.message.otpbeans.TransitStops;
import it.sayservice.platform.smartplanner.model.Stop;
import it.sayservice.platform.smartplanner.otp.schedule.StopNames;
import it.sayservice.platform.smartplanner.otp.schedule.Timetable;
import it.sayservice.platform.smartplanner.otp.schedule.TripSchedule;
import it.sayservice.platform.smartplanner.utils.Agency;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;

@Component
public class OTPManager {

	private static final long MINUTES_10 = 1000 * 60 * 10;
	@Autowired
	private OTPStorage storage;
	@Autowired
	private OTPHandler handler;
	@Autowired
	private MongoRouterMapper mongoRouterMapper;
	@Autowired
	private ConfigurationManager coManager;

	private Map<String, Boolean> routerInitialized = new HashMap<String, Boolean>();
	private Map<String, String> stopsNames;

	private ArrayList<String> agencyIds = new ArrayList<String>();
	private Multimap<String, String> symbolicRouteIds = HashMultimap.create();
	// this is very important map that identify the mapping of route in agencies.
	private Set<String> symbolicAgencyIds = new HashSet<String>();

	public boolean isInitialized(String router) {
		return routerInitialized.get(router);
	}

	public OTPManager() {
	}

	public OTPManager(OTPHandler handler, OTPStorage storage, MongoRouterMapper routerMapper,
			ConfigurationManager configurationManager) throws Exception {
		this.storage = storage;
		this.handler = handler;
		this.mongoRouterMapper = routerMapper;
		this.coManager = configurationManager;

		for (String key : handler.getRouterConfig().getPublicTransport().keySet()) {
			Agency agency = handler.getRouterConfig().getPublicTransport().get(key);
			agencyIds.add(agency.getAgencyId());
		}

	}

	//    @PostConstruct
	public synchronized void preinit(boolean store) throws Exception {
		System.out.println("Initializing OTPManager");
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		String router = handler.getRouterConfig().getRouter();

		for (String key : handler.getRouterConfig().getPublicTransport().keySet()) {
			Agency agency = handler.getRouterConfig().getPublicTransport().get(key);
			if (!agencyIds.contains(agency.getAgencyId())) {
				agencyIds.add(agency.getAgencyId());
			}
		}

		stopsNames = new TreeMap<String, String>();

		List<Map<String, Object>> stops = handler.getStops(router);
		ObjectMapper mapper = new ObjectMapper();
		for (Map<String, Object> stop : stops) {
			Map<String, String> stopMap = mapper.convertValue(stop, Map.class);
			// stopMap.remove("latitude");
			// stopMap.remove("longitude");
			if (store)
				storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), stopMap, Constants.STOPS);
			stopsNames.put((String) stop.get("stopId"), (String) stop.get("name"));
		}

		// fetch transit timetable per agency.
		List<Timetable> timetableTrains = Lists.newArrayList();
		List<AnnotatedTrip> annotatedTrips = Lists.newArrayList();
		for (String agency : agencyIds) {
			List<Timetable> schedule = handler.buildTransitTripSchedules(router, agency);
			timetableTrains.addAll(schedule);
			List<AnnotatedTrip> trips = handler.buildAnnotatedTrips(router, agency);
			if (!trips.isEmpty()) {
				symbolicAgencyIds.add(agency);
			}
			annotatedTrips.addAll(trips);
		}

		Multimap<String, String> tripSymbolicsMap = ArrayListMultimap.create();

		for (AnnotatedTrip at : annotatedTrips) {
			tripSymbolicsMap.put(at.getTripId(), at.getSymbolicRouteId());
			if (store)
				storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), at, Constants.ANNOTATED_TRIPS);
		}

		for (Timetable tt : timetableTrains) {
			StopNames stopNames = new StopNames();
			stopNames.setRouteId(tt.getRouteId());
			stopNames.setIds(tt.getStopsIds());
			List<String> names = buildStopNames(router, tt.getStopsIds());
			stopNames.setNames(names);

			if (stopNames.getIds().size() != stopNames.getNames().size()) {
				System.out.println("SIZE!!!");
			}
			if (store)
				storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), stopNames, Constants.STOP_NAMES);
		}

		Map<String, List> symbolicTrips = Maps.newTreeMap();
		for (String agency : agencyIds) {
			List<Map> agencyInfo = handler.readAgencyAnnotatedInfo(router, agency);
			if (agencyInfo != null) {
				for (Object obj : agencyInfo) {
					SymbolicRouteDayInfoHashCalendar srdihc = mapper.convertValue(obj,
							SymbolicRouteDayInfoHashCalendar.class);
					if (store)
						storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), srdihc,
								Constants.AGENCY_INFO);
				}
			}

			Map<String, List> agencySymbolicTrips = handler.readSymbolicTrips(agency);
			if (agencySymbolicTrips != null) {
				symbolicTrips.putAll(agencySymbolicTrips);
			}
		}

		for (Timetable tt : timetableTrains) {
			for (TripSchedule sched : tt.getSchedules()) {
				Collection<String> symbolIds = tripSymbolicsMap.get(sched.getTripId());
				if (symbolIds == null || symbolIds.isEmpty()) {
					symbolIds = Collections.singletonList(sched.getRouteId());
				}
				sched.setSymbolicRouteIds(Lists.newArrayList(symbolIds));
				symbolicRouteIds.putAll(sched.getAgencyId(), tripSymbolicsMap.get(sched.getTripId()));
				if (store)
					storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), sched, Constants.TIMETABLE);
			}
		}

		Map<String, String> trips = handler.getTrips();
		Map<String, String> tripsCalendar = handler.getTripsCalendar();
		for (String key : trips.keySet()) {
			Map<String, String> tm = new TreeMap<String, String>();
			String tid = key;
			int index = tid.indexOf('_');
			tid = tid.substring(index + 1);
			tm.put("tripId", tid);
			tm.put("routeId", trips.get(key));
			tm.put("calendarId", tripsCalendar.get(key));
			if (store)
				storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), tm, Constants.TRIPS);
		}

		Collection<Object> stopsIds = new TreeSet<Object>();
		for (Timetable timetable : timetableTrains) {
			stopsIds.addAll(timetable.getStopsIds());
		}
		System.out.println("Keeping " + stops.size() + " stops, removing unexisting ones.");
		if (store)
			storage.bulkDelete(mongoRouterMapper.getMongoTemplateMap().get(router), "stopId", stopsIds, "stops");
	}

	public synchronized void init(String router) throws Exception {

		handler.setOtpURL(coManager.getRouter(router).getOtpEndpoint() + router + Constants.API_TRANSIT);

		// check if router data initialized.
		if (routerInitialized.containsKey(router) && routerInitialized.get(router)) {
			return;
		}

		//		handler.init(coManager.getRouter(router));

		List<Route> routes = handler.getRoutes(router);

		for (Route route : routes) {
			storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), route, Constants.ROUTES);
		}

		for (Object r : routes) {
			Route route = (Route) r;

			List<TransitStops> trips = handler.getRouteData(route.getId().getAgency(), route.getId().getId());

			for (TransitStops trip : trips) {
				storage.store(mongoRouterMapper.getMongoTemplateMap().get(router), trip, Constants.ROUTE_STOPS);
			}
		}

		routerInitialized.put(router, true);
	}

	public String getRoutes(String router, String agencyId) throws Exception {
		// if (!initialized) {
		init(router);
		// }

		List<Object> res = storage.getObjectsByField(mongoRouterMapper.getMongoTemplateMap().get(router), "id.agency",
				agencyId, Constants.ROUTES, Route.class, "routeShortName");

		Set<String> ids = new HashSet<String>();
		for (Object o : res) {
			Route r = (Route) o;
			ids.add(r.getId().getId());
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(res);
	}

	public String getStops(String router, String agencyId, String routeId) throws Exception {
		// if (!initialized) {
		init(router);
		// }

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Map> resultMap = new TreeMap<String, Map>();
		List<Map> result = new ArrayList<Map>();

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("agency", agencyId);
		map.put("id", routeId);
		TransitStops trip = (TransitStops) storage.getObjectByFields(
				mongoRouterMapper.getMongoTemplateMap().get(router), map, Constants.ROUTE_STOPS, TransitStops.class);
		map = new TreeMap<String, Object>();
		map.put("routeId", routeId);
		StopNames stopNames = (StopNames) storage.getObjectByFields(mongoRouterMapper.getMongoTemplateMap().get(router),
				map, Constants.STOP_NAMES, StopNames.class);

		if (stopNames != null) {
			Set<String> stopIds = new HashSet<String>();
			stopIds.addAll(trip.getStopsId());

			for (String stopId : stopIds) {
				Stop stopObj = (Stop) storage.getObjectByField(mongoRouterMapper.getMongoTemplateMap().get(router),
						"stopId", stopId, Constants.STOPS, Stop.class, Arrays.asList("agencyId"));
				if (stopObj != null) {
					Map m = mapper.convertValue(stopObj, Map.class);
					m.put("id", m.get("stopId"));
					m.remove("stopId");
					m.remove("coordinates");
					// Map<String, Map> stopMap = mapper.convertValue(stopObj,
					// Map.class);
					// stopMap.remove("coordinates");
					resultMap.put(stopObj.getStopId(), m);
				}
			}
			for (String stopId : stopNames.getIds()) {
				if (!result.contains(resultMap.get(stopId))) {
					result.add(resultMap.get(stopId));
				}
			}

			result = sortStopsByAnnotatedStops(router, result, routeId, agencyId);

			return mapper.writeValueAsString(result);
		} else {
			return "[]";
		}

	}

	public String getStops(String router, String agencyId, String routeId, double lat, double lon, double radius)
			throws Exception {
		// if (!initialized) {
		init(router);
		// }

		ObjectMapper mapper = new ObjectMapper();
		Map<String, Map> resultMap = new TreeMap<String, Map>();
		List<Map> result = new ArrayList<Map>();

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("agency", agencyId);
		map.put("id", routeId);
		TransitStops trip = (TransitStops) storage.getObjectByFields(
				mongoRouterMapper.getMongoTemplateMap().get(router), map, Constants.ROUTE_STOPS, TransitStops.class);
		map = new TreeMap<String, Object>();
		map.put("routeId", routeId);
		StopNames stopNames = (StopNames) storage.getObjectByFields(mongoRouterMapper.getMongoTemplateMap().get(router),
				map, Constants.STOP_NAMES, StopNames.class);

		if (stopNames != null) {
			Set<String> stopIds = new HashSet<String>();
			stopIds.addAll(trip.getStopsId());

			Distance distance = new Distance(radius / 1000, Metrics.KILOMETERS);
			DBObject query = QueryBuilder.start("coordinates").near(lat, lon, distance.getValue()).and("stopId")
					.in(stopNames.getIds()).get();

			List stopResult = storage.getObjectsByQuery(mongoRouterMapper.getMongoTemplateMap().get(router), query,
					Constants.STOPS, Stop.class, null);

			for (Object stop : stopResult) {
				Map stopMap = mapper.convertValue(stop, Map.class);
				stopMap.remove("coordinates");
				resultMap.put((String) (stopMap).get("stopId"), stopMap);
			}

			for (String stopId : stopNames.getIds()) {
				if (resultMap.containsKey(stopId)) {
					Map stopMap = resultMap.get(stopId);
					if (!result.contains(stopMap)) {
						result.add(resultMap.get(stopId));
					}
				}
			}

			if (!result.isEmpty()) {
				result = sortStopsByAnnotatedStops(router, result, routeId, agencyId);

				return mapper.writeValueAsString(result);
			} else {
				return "[]";
			}
		} else {
			return "[]";
		}

	}

	private List<Map> sortStopsByAnnotatedStops(String router, List<Map> stops, String routeId, String agencyId) {
		if (stops.isEmpty()) {
			return stops;
		}

		List<Map> result = Lists.newArrayList();

		Map<String, Object> map = new TreeMap<String, Object>();
		map.put("routeId", routeId);
		map.put("agencyId", agencyId);
		SymbolicRouteDayInfoHashCalendar calendar = (SymbolicRouteDayInfoHashCalendar) storage.getObjectByFields(
				mongoRouterMapper.getMongoTemplateMap().get(router), map, Constants.AGENCY_INFO,
				SymbolicRouteDayInfoHashCalendar.class);

		if (calendar != null) {
			Calendar cal = new GregorianCalendar();
			cal.setTimeInMillis(System.currentTimeMillis());
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String date = sdf.format(cal.getTime());

			String hash = calendar.getCalendar().get(date);
			StopNames stopNames = calendar.getValues().get(hash).getStopNames();

			Map<String, Map> stopMaps = Maps.newTreeMap();
			for (Map m : stops) {
				String id = "";
				if (m.containsKey("id")) {
					id = (String) m.get("id");
				} else {
					id = (String) m.get("stopId");
				}
				stopMaps.put(id, m);
			}

			for (String id : stopNames.getIds()) {
				if (stopMaps.containsKey(id)) {
					result.add(stopMaps.get(id));
				}
			}

			return result;
		} else {
			return stops;
		}
	}

	public String getTimeTable(String router, String agencyId, String routeId, String stopId, Long fromTime) throws Exception {
		init(router);

		List<StopTime> result = handler.getTimes(router, agencyId, routeId, stopId,
				fromTime - MINUTES_10 * 6, fromTime + MINUTES_10 * 6);

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(result);
	}

	public String getLimitedTimeTable(String router, String agencyId, String stopId, long fromTime, int maxResults)
			throws Exception {

		init(router);

		Multimap<String, StopTime> times = handler.getTimesByRoutes(router, agencyId, stopId, fromTime,
				fromTime + RecurrentUtil.DAY);

		Map<String, List<StopTime>> shortMap = new TreeMap<String, List<StopTime>>();
		for (String key : times.keySet()) {
			List<StopTime> sortedStops = (List<StopTime>) times.get(key);
			Collections.sort(sortedStops);
			List<StopTime> stops = new ArrayList<StopTime>(
					sortedStops.subList(0, Math.min(maxResults, sortedStops.size())));
			shortMap.put(key, stops);
		}

		Map<String, Map<String, Object>> result = new TreeMap<String, Map<String, Object>>();

		for (String key : shortMap.keySet()) {
			Map<String, Map<String, String>> delays = new TreeMap<String, Map<String, String>>();
			Map<String, CreatorType> delaysTypes = new TreeMap<String, CreatorType>();
			Map<String, Object> map = new TreeMap<String, Object>();
			for (StopTime stopTime : shortMap.get(key)) {
				List<AlertDelay> dlist = getAlertDelay(router, key, stopTime.getTrip().getId(), true);
				Map<String, String> dm1 = new TreeMap<String, String>();
				String tripId = null;
				for (AlertDelay delay : dlist) {
					if (delay != null && delay.getDelay() > 0) {
						tripId = delay.getTransport().getTripId();
						dm1.put(delay.getCreatorType().toString(), "" + (delay.getDelay() / 60000));
					}
				}
				if (tripId != null) {
					delays.put(tripId, dm1);
				}

			}
			List<StopTime> tt = shortMap.get(key);
			map.put("times", tt);
			map.put("delays", delays);

			Route route = (Route) storage.getObjectByField(mongoRouterMapper.getMongoTemplateMap().get(router), "id.id",
					key, Constants.ROUTES, Route.class);
			map.put("name", route.getRouteLongName());
			map.put("route", route.getRouteShortName());
			result.put(key, map);
		}

		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(result);

	}

	// for cache building
	public final String getTransitSchedule(final String router, final String agencyId, final String routeId,
			final Long from, final Long to, TransitScheduleResults filter, final boolean tripsIds) throws Exception {
		ExtendedTransitTimeTable ett = getTransitScheduleNoDelays(router, agencyId, routeId, from, to, filter, tripsIds,
				false);
		// writeDelays(ett, from);
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(ett);

	}

	public final ExtendedTransitTimeTable buildTransitSchedule(final String router, final String agencyId,
			final String routeId, final Long from, final Long to, TransitScheduleResults filter, boolean tripsIds,
			boolean annotated) throws Exception {
		ExtendedTransitTimeTable ett = getTransitScheduleNoDelays(router, agencyId, routeId, from, to, filter, tripsIds,
				annotated);
		return ett;
	}

	// one day only
	public void writeDelays(String router, ExtendedTransitTimeTable timetable, long time) {
		Calendar cal = new GregorianCalendar();
		cal.setTimeInMillis(time);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		List<List<Map<String, String>>> dels = Lists.newArrayList();
		for (List<String> tids : timetable.getTripIds()) {
			List<Map<String, String>> ds = Lists.newArrayList();
			for (String tid : tids) {
				List<AlertDelay> delaysList = getAlertDelay(router, tid, false);
				Map<String, String> dm1 = Maps.newTreeMap();
				for (AlertDelay delay : delaysList) {
					if (delay != null && checkDelay(cal, delay.getFrom(), delay.getTo())) {
						dm1.put(delay.getCreatorType().toString(), "" + (delay.getDelay() / 60000));
					}
				}
				ds.add(dm1);
			}
			dels.add(ds);
		}

		timetable.setDelays(dels);
	}

	/**
	 * Get Transit Schedule.
	 * 
	 * @param agencyId
	 * @param routeId
	 * @param newFrom
	 * @param to
	 * @param filter
	 * @param tripsIds
	 * @param annotated
	 * @return
	 * @throws Exception
	 */
	public final ExtendedTransitTimeTable getTransitScheduleNoDelays(final String router, final String agencyId,
			final String routeId, final Long from, final Long to, TransitScheduleResults filter, boolean tripsIds,
			boolean annotated) throws Exception {
		init(router);

		boolean symbolic = false;
		if (symbolicAgencyIds.contains(agencyId)) {
			symbolic = true;
		}
		if (symbolic && !symbolicRouteIds.get(agencyId).contains(routeId)) {
			return null;
		}

		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(from);
		c.set(Calendar.HOUR, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long newTo = Math.min(to, c.getTimeInMillis());

		c = new GregorianCalendar();
		c.setTimeInMillis(from);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long newFrom = c.getTimeInMillis();

		List<List<TripSchedule>> allTrips = new ArrayList<List<TripSchedule>>();
		Calendar cal = new GregorianCalendar();
		Calendar calFrom = new GregorianCalendar();
		calFrom.setTimeInMillis(newFrom);
		Calendar calTo = new GregorianCalendar();
		calTo.setTimeInMillis(newTo);
		for (long i = newFrom; i <= newTo; i += RecurrentUtil.DAY) {
			cal.setTimeInMillis(i);
			int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String date = sdf.format(cal.getTime());

			List<TripSchedule> trips = getSchedule(router, agencyId, routeId, dayOfWeek, date, Constants.TIMETABLE,
					symbolic);
			// trips = filterSchedules(trips, calFrom, calTo, cal);
			allTrips.add(trips);
		}

		List<List<List<String>>> times = new ArrayList<List<List<String>>>();
		List<List<String>> tripIds = new ArrayList<List<String>>();
		long time = newFrom;
		for (List<TripSchedule> lts : allTrips) {
			List<List<String>> tl1 = new ArrayList<List<String>>();
			List<String> tid1 = new ArrayList<String>();
			List<Map<String, String>> alm1 = new ArrayList<Map<String, String>>();
			cal.setTimeInMillis(time);
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			for (TripSchedule ts : lts) {
				List<String> tl2 = new ArrayList<String>(Arrays.asList(ts.getTimes()));
				tl1.add(tl2);
				tid1.add(ts.getTripId());

			}

			List<List<String>> tl1s = new ArrayList<List<String>>(tl1);

			tid1 = swapTripIds(tl1, tl1s, tid1);

			tripIds.add(tid1);

			times.add(tl1);
			time += RecurrentUtil.DAY;
			cal.setTimeInMillis(time);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		String date = sdf.format(calFrom.getTime());
		SymbolicRouteDayInfoHashCalendar sdi = findDayInfo(router, agencyId, routeId, date, symbolic);

		ExtendedTransitTimeTable result = new ExtendedTransitTimeTable();
		if (annotated && sdi != null) {
			String hash = sdi.getCalendar().get(date);
			if (hash != null) {
				SymbolicRouteDayInfo srdi = sdi.getValues().get(hash);
				result.setShortDescription(srdi.getShortDescription());
				result.setLongDescription(srdi.getLongDescription());
				result.setFrequency(srdi.getFrequency());
				result.setInvisibles(srdi.getInvisibles());
				result.setLine(srdi.getLine());
				result.setSchedule(srdi.getSchedule());
				result.setValidity(srdi.getValidity());
				result.setRoutesIds(srdi.getRouteIds());
			}
		}

		if (filter.equals(TransitScheduleResults.ALL) || filter.equals(TransitScheduleResults.TIMES)) {
			if (sdi != null) {
				String hash = sdi.getCalendar().get(date);
				if (hash != null) {
					StopNames stopNames = sdi.getValues().get(hash).getStopNames();
					result.setStopsId(stopNames.getIds());
					result.setStops(stopNames.getNames());
				}
			} else {
				StopNames stopNames = getStopNames(router, routeId);
				result.setStopsId(stopNames.getIds());
				result.setStops(stopNames.getNames());
			}
			result.setTimes(times);
			if (!symbolic) {
				result = compressStops(result);
			}
		}
		if (tripsIds) {
			result.setTripIds(tripIds);
		}

		return result;
	}

	private SymbolicRouteDayInfoHashCalendar findDayInfo(String router, String agencyId, String routeId, String date,
			boolean symbolic) {
		if (symbolic) {

			QueryBuilder qb = QueryBuilder.start();
			qb.and("routeId").is(routeId);
			qb.and("agencyId").is(agencyId);

			List<SymbolicRouteDayInfoHashCalendar> symbolicInfos = (List) storage.getObjectsByQuery(
					mongoRouterMapper.getMongoTemplateMap().get(router), qb.get(), Constants.AGENCY_INFO,
					SymbolicRouteDayInfoHashCalendar.class, null);

			SymbolicRouteDayInfoHashCalendar si = symbolicInfos.get(0);

			return si;

		} else {
			return null;
		}
	}

	public ExtendedTransitTimeTable compressStops(ExtendedTransitTimeTable tt) {
		int stopsLen = tt.getStops().size();
		Map<Integer, Boolean> toRemove = new TreeMap<Integer, Boolean>();
		for (int i = 0; i < stopsLen; i++) {
			toRemove.put(i, true);
		}
		for (List<List<String>> days : tt.getTimes()) {
			for (List<String> trip : days) {
				for (int i = 0; i < stopsLen; i++) {
					if (trip.get(i).length() > 0) {
						toRemove.put(i, false);
					}
				}
			}
		}

		List<Integer> ids = new ArrayList<Integer>();
		for (Integer id : toRemove.keySet()) {
			if (toRemove.get(id).booleanValue()) {
				ids.add(id);
			}
		}

		Collections.reverse(ids);

		for (int ip : ids) {
			tt.getStops().remove(ip);
			tt.getStopsId().remove(ip);
			for (List<List<String>> days : tt.getTimes()) {
				for (List<String> trip : days) {
					trip.remove(ip);
				}
			}
		}

		return tt;

	}

	public List<String> getRouteCalendarEntries(String router, String routeId) throws Exception {
		// if (!initialized) {
		init(router);
		// }

		Set<String> result = new HashSet<String>();
		List<Map<String, String>> trips = (List) storage.getObjectsByField(
				mongoRouterMapper.getMongoTemplateMap().get(router), "routeId", routeId, Constants.TRIPS, Map.class,
				null);
		for (Map<String, String> trip : trips) {
			result.add(trip.get("calendarId"));
		}
		return new ArrayList<String>(result);
	}

	public List<Object> getGeolocalizedStops(String router, GeolocalizedStopRequest gsr) throws Exception {
		init(router);

		List<Object> result = new ArrayList<Object>();

		try {
			Criteria criteria = new Criteria();
			Circle circle = new Circle(gsr.getCoordinates()[0], gsr.getCoordinates()[1], gsr.getRadius());

			criteria.and("coordinates").within(circle);
			criteria.and("agencyId").is(gsr.getAgencyId());

			Query query = new Query(criteria).skip(gsr.getPageSize() * gsr.getPageNumber()).limit(gsr.getPageNumber());

			List objs = storage.getPagedObjectsByQuery(mongoRouterMapper.getMongoTemplateMap().get(router), query,
					Stop.class, Constants.STOPS, Map.class);

			ObjectMapper mapper = new ObjectMapper();
			for (Object obj : objs) {
				Map m = mapper.convertValue(obj, Map.class);
				m.remove("latitude");
				m.remove("longitude");
				m.put("id", m.get("stopId"));
				m.remove("stopId");
				result.add(m);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private List<Map<String, String>> swapDelays(List<List<String>> origTimes, List<List<String>> sortedTimes,
			List<Map<String, String>> delays) {
		Map<Integer, Integer> posMap = new TreeMap<Integer, Integer>();
		Map<String, String>[] sortedDelays = new TreeMap[delays.size()];
		for (List<String> orig : origTimes) {
			Integer key = origTimes.indexOf(orig);
			Integer value = sortedTimes.indexOf(orig);
			posMap.put(key, value);
		}

		for (Integer from : posMap.keySet()) {
			sortedDelays[posMap.get(from)] = delays.get(from);
		}

		return new ArrayList<Map<String, String>>(Arrays.asList(sortedDelays));
	}

	private List<String> swapTripIds(List<List<String>> origTimes, List<List<String>> sortedTimes,
			List<String> tripdIs) {
		Map<Integer, Integer> posMap = new TreeMap<Integer, Integer>();
		String[] sortedTripdIs = new String[tripdIs.size()];
		for (List<String> orig : origTimes) {
			Integer key = origTimes.indexOf(orig);
			Integer value = sortedTimes.indexOf(orig);
			posMap.put(key, value);
		}

		for (Integer from : posMap.keySet()) {
			sortedTripdIs[posMap.get(from)] = tripdIs.get(from);
		}

		return new ArrayList<String>(Arrays.asList(sortedTripdIs));
	}

	private List<TripSchedule> filterSchedules(List<TripSchedule> schedules, Calendar from, Calendar to, Calendar now) {
		List<TripSchedule> filteredTrips = new ArrayList<TripSchedule>();
		for (TripSchedule schedule : schedules) {
			String firstTime = findFirstTime(schedule);
			String hmsFirst[] = firstTime.split(":");
			Calendar calFirst = new GregorianCalendar();
			calFirst.setTime(now.getTime());
			calFirst.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hmsFirst[0]));
			calFirst.set(Calendar.MINUTE, Integer.parseInt(hmsFirst[1]));
			calFirst.set(Calendar.SECOND, 0);

			String lastTime = findLastTime(schedule);
			String hmsLast[] = lastTime.split(":");
			Calendar calLast = new GregorianCalendar();
			calLast.setTime(now.getTime());
			calLast.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hmsLast[0]));
			calLast.set(Calendar.MINUTE, Integer.parseInt(hmsLast[1]));
			calLast.set(Calendar.SECOND, 0);

			if (calLast.compareTo(from) >= 0 && calFirst.compareTo(to) <= 0) {
				filteredTrips.add(schedule);
			}
		}

		return filteredTrips;
	}

	private String findFirstTime(TripSchedule schedule) {
		String firstTime = null;
		for (int i = 0; i < schedule.getTimes().length; i++) {
			if (schedule.getTimes()[i].length() > 0) {
				firstTime = schedule.getTimes()[i];
				break;
			}
		}
		return firstTime;
	}

	private String findLastTime(TripSchedule schedule) {
		String lastTime = null;
		for (int i = schedule.getTimes().length - 1; i > 0; i--) {
			if (schedule.getTimes()[i].length() > 0) {
				lastTime = schedule.getTimes()[i];
				break;
			}
		}
		return lastTime;
	}

	private boolean checkDelay(Calendar now, long from, long to) {
		Calendar c1 = new GregorianCalendar();
		c1.setTimeInMillis(from);
		Calendar c2 = new GregorianCalendar();
		c2.setTimeInMillis(to);

		if (c1.get(Calendar.DAY_OF_YEAR) <= now.get(Calendar.DAY_OF_YEAR)
				&& c2.get(Calendar.DAY_OF_YEAR) >= now.get(Calendar.DAY_OF_YEAR)
				&& c1.get(Calendar.YEAR) <= now.get(Calendar.YEAR) && c2.get(Calendar.YEAR) >= now.get(Calendar.YEAR)) {
			return true;
		}
		return false;

	}

	private List<String> buildStopNames(String router, List<String> ids) {
		List<String> names = new ArrayList<String>();
		for (String id : ids) {
			Map stop = (Map) storage.getObjectByField(mongoRouterMapper.getMongoTemplateMap().get(router), "stopId", id,
					Constants.STOPS, Map.class);
			try {
				names.add((String) stop.get("name"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return names;
	}

	private StopNames getStopNames(String router, String routeId) {
		StopNames stopNames = (StopNames) storage.getObjectByField(mongoRouterMapper.getMongoTemplateMap().get(router),
				"routeId", routeId, Constants.STOP_NAMES, StopNames.class);
		return stopNames;
	}

	private List<AlertDelay> getAlertDelay(String router, String routeId, String tripId, boolean now) {
		QueryBuilder qb = new QueryBuilder();
		qb = qb.start("transport.routeId").is(routeId).and("transport.tripId").is(tripId);
		if (now) {
			long time = System.currentTimeMillis();
			qb = qb.and("from").lessThanEquals(time);
			qb = qb.and("to").greaterThanEquals(time);
		}

		List<AlertDelay> delays = (List) storage.getObjectsByQuery(mongoRouterMapper.getMongoTemplateMap().get(router),
				qb.get(), Constants.ALERT_DELAYS, AlertDelay.class, "delay");

		return delays;
	}

	private List<AlertDelay> getAlertDelay(String router, String tripId, boolean now) {
		QueryBuilder qb = new QueryBuilder();
		qb = qb.start("transport.tripId").is(tripId);
		if (now) {
			long time = System.currentTimeMillis();
			qb = qb.and("from").lessThanEquals(time);
			qb = qb.and("to").greaterThanEquals(time);
		}

		List<AlertDelay> delays = (List) storage.getObjectsByQuery(mongoRouterMapper.getMongoTemplateMap().get(router),
				qb.get(), Constants.ALERT_DELAYS, AlertDelay.class, "delay");

		return delays;
	}

	private List<TripSchedule> getSchedule(String router, String agencyId, String routeId, int day, String date,
			String collectionName, boolean symbolic) {
		QueryBuilder qb = QueryBuilder.start();

		if (symbolic) {
			qb.and("symbolicRouteIds").is(routeId);
		} else {
			qb = qb.and("routeId").is(routeId);
		}
		qb = qb.and("agencyId").is(agencyId);
		qb = qb.and("days").is(day);
		qb = qb.and("daysRemoved").notEquals(date);
		qb = qb.and("fromDate").lessThanEquals(date);
		qb = qb.and("toDate").greaterThanEquals(date);

		List<TripSchedule> schedules = (List) storage.getObjectsByQuery(
				mongoRouterMapper.getMongoTemplateMap().get(router), qb.get(), collectionName, TripSchedule.class,
				"routeId");

		qb = QueryBuilder.start();
		if (symbolic) {
			qb.and("symbolicRouteIds").is(routeId);
		} else {
			qb = qb.and("routeId").is(routeId);
		}
		qb = qb.and("agencyId").is(agencyId);
		qb = qb.and("daysAdded").is(date);
		qb = qb.and("fromDate").lessThanEquals(date);
		qb = qb.and("toDate").greaterThanEquals(date);

		List<TripSchedule> addedSchedules = (List) storage.getObjectsByQuery(
				mongoRouterMapper.getMongoTemplateMap().get(router), qb.get(), collectionName, TripSchedule.class,
				"routeId");
		for (TripSchedule toCheck : addedSchedules) {
			if (!schedules.contains(toCheck)) {
				schedules.add(toCheck);
			}
		}

		qb = QueryBuilder.start();
		if (symbolic) {
			qb.and("symbolicRouteIds").is(routeId);
			qb = qb.and("agencyId").is(agencyId);
			qb = qb.and("daysAdded").is(date);
			qb = qb.and("fromDate").lessThanEquals(date);
			qb = qb.and("toDate").greaterThanEquals(date);

			QueryBuilder fb = QueryBuilder.start();
			fb = fb.and("times").is("1");
			fb = fb.and("order").is("1");

			List<TripSchedule> toRemove = Lists.newArrayList();
			for (TripSchedule ts : schedules) {
				qb = QueryBuilder.start();
				qb = qb.and("agencyId").is(agencyId);
				qb = qb.and("tripId").is(ts.getTripId());

				List<AnnotatedTrip> annotatedTrips = (List) storage.getObjectsByQuery(
						mongoRouterMapper.getMongoTemplateMap().get(router), qb.get(), Constants.ANNOTATED_TRIPS,
						AnnotatedTrip.class, null, fb.get());
				if (!annotatedTrips.isEmpty()) {
					try {
						List<String> times = (List) annotatedTrips.get(0).getTimes();
						ts.setTimes(times.toArray(new String[times.size()]));
						ts.setOrder(annotatedTrips.get(0).getOrder());
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					System.err.println("Annotated trip not found for " + ts.getTripId());
					toRemove.add(ts);
				}

			}

			schedules.removeAll(toRemove);
		}
		Collections.sort(schedules);

		return schedules;
	}

	public void clean() {
		agencyIds.clear();
	}

	public List<Object> getAgencies(String router) throws Exception {
		init(router);
		return handler.getAgencies(router);
	}

}
