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

public class Constants {
	// OTP related.
	public static final String ROUTER = "router";
	public static final String API_PLAN = "/plan";
	public static final String API_TRANSIT = "/index"; // 0.15.x
	public static final String OP_ROUTES = "/routes";
	public static final String OP_ROUTEDATA = "/routeData";
	public static final String OP_STOPS = "/stops";
	public static final String AGENCIES = "/agencies";
	
	public static final String OP_STOPTIMES = "/stoptimes";
	public static final String OTP_RQ_ITNS = "numItineraries";
	public static final String OTP_RQ_FROM = "fromPlace";
	public static final String OTP_RQ_TO = "toPlace";
	public static final String OTP_RQ_MODE = "mode";
	public static final String OTP_RQ_TIME = "time";
	public static final String OTP_RQ_DATE = "date";
	public static final String OTP_RQ_ARRIVEBY = "arriveBy";
	public static final String OTP_RQ_OPTIMIZE = "optimize";

	public static final String OTP_RQ_T_SAFETY = "triangleSafetyFactor";
	public static final String OTP_RQ_T_SLOPE = "triangleSlopeFactor";
	public static final String OTP_RQ_T_TIME = "triangleTimeFactor";

	public static final String BATCH = "batch";
	public static final String WALK_SPEED = "walkSpeed";
	public static final String BIKE_SPEED = "bikeSpeed";
	public static final String OTP_RQ_MAXWALK = "maxWalkDistance";
	public static final String MIN_TRANSFERTIME = "minTransferTime";
	public static final String WALK_RELUCTANCE = "walkReluctance";
	public static final String WHEELCHAIR = "wheelchair";
	
	public static String NO_WALK = "0";
	public static String LONG_WALK = "3000";
	public static String TRANSIT_WALK = "840";
	public static String WALK_ONLY = "1600";
	public static String CAR_PARKING = "CAR-PARKING";
	public static String CAR_RENTAL = "CAR-RENTAL";
	
	// file system.
	public static final String CONFIG_YML = "config";
	
	public enum MODES {
		BICYCLE("BICYCLE"), CAR("CAR"), TRANSIT("TRANSIT,WALK"), WALK("WALK"), BUS("BUSISH,WALK"), TRAIN(
				"TRAINISH,WALK");
		private final String name;

		private MODES(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			return (otherName == null) ? false : name.equals(otherName);
		}

		public String toString() {
			return name;
		}
	}

	public enum OPTIMIZATION {
		QUICK("QUICK"), GREENWAYS("GREENWAYS"), SAFE("SAFE"), TRANSFERS("TRANSFERS"), TRIANGLE("TRIANGLE");
		private final String name;

		private OPTIMIZATION(String s) {
			name = s;
		}

		public boolean equalsName(String otherName) {
			return (otherName == null) ? false : name.equals(otherName);
		}

		public String toString() {
			return name;
		}
	}

	// SMARTPLANNER related.
	public static final String SP_RQ_FROM = "from";
	public static final String SP_RQ_TO = "to";
	public static final String SP_RQ_DATE = "date";
	public static final String SP_RQ_ARRTIME = "arrivalTime";
	public static final String SP_RQ_DEPTIME = "departureTime";
	public static final String SP_RQ_TIME = "time";
	public static final String SP_RQ_ROUTE_PREF = "routePreferences";
	public static final String SP_RQ_NUM_ITNS = "numItineraries";
	public static final String SP_RQ_MAXWALK = "maxWalkDistance";
	public static final String SP_RQ_USERMODE = "userMode";
	public static final String SP_RQ_FROMDATE = "fromDate";
	public static final String SP_RQ_TODATE = "toDate";
	public static final String SP_RQ_RECURRENCE = "recurrence";
	public static final String SP_RQ_INTERVAL = "interval";
	public static final String SP_RQ_FROMSTATION = "fromStation";
	public static final String SP_RQ_TOSTATION = "toStation";
	public static final String SP_GTFS_FOLDER = "gtfs";
	// MONGO.
	public static final String ALERT_DELAYS = "alertDelayRepo";
	public static final String ROUTES = "routes";
	public static final String STOPS = "stops";
	public static final String SCHEDULES = "schedules";
	public static final String TIMETABLE = "timetable";
	public static final String ROUTE_STOPS = "routeStops";
	public static final String STOP_NAMES = "stopNames";
	public static final String TRIPS = "trips";
	public static final String ALERT_DELAY_REPO = "alertDelayRepo";
	public static final String ALERT_STRIKE_REPO = "alertStrikeRepo";
	public static final String ALERT_BIKE_REPO = "dynamicBikeRepo";
	public static final String ALERT_CAR_REPO = "dynamicCarRepo";
	public static final String ALERT_ROAD_REPO = "alertRoadRepo";
	public static final String ALERT_ACCIDENT_REPO = "alertAccidentRepo";
	public static final String AGENCY_INFO = "agencyInfo";
	public static final String ANNOTATED_TRIPS = "annotatedTrips";
	// CACHE.
	public static final String CACHE_DIR = "cache";
	public static final String CLIENT_CACHE_DIR = "client";
	public static final String AUXILIARY_CACHE_DIR = "auxiliary";
	public static final String AREA_CACHE_DIR = "areainfo";
	public static final String AREA_SEPARATOR_KEY = "@";
	public static final String SCHEDULES_FOLDER_PATH = "/cache/schedules";
	public static final String GTFS_FOLDER_PATH = "/gtfs";
	public static final String NO_DATA_HASH = "da39a3ee5e6b4b0d3255bfef95601890afd80709";
	public static final String INDEX_SUFFIX = "_index.txt";
	public static final String CACHE_STOPS_ORDER = "stopsOrder";
	// BIKE.
	public static final int BS_THRESHOLD = 5;
	public static final String BS_AGENCY_TYPE = "BIKE-RENTAL";
	// CAR.
	public static final int CS_THRESHOLD = 5;
	public static final String CS_AGENCY_TYPE = "CAR-RENTAL";
	// GTFS
	public static final String GTFS_TRIPS = "trips.txt";
	public static final String GTFS_STOP = "stops.txt";
	public static final String GTFS_ROUTE = "routes.txt";
	public static final String GTFS_STOPTIMES = "stop_times.txt";
	public static final String GTFS_CALENDAR = "calendar.txt";
	public static final String GTFS_CALENDAR_DATE = "calendar_dates.txt";
	// trips.txt (route_id,service_id,trip_id,trip_headsign,direction_id,shape_id,wheelchair_accessible)
	public static final String TRIP_ROUTE_ID = "route_id";
	public static final String TRIP_SERVICE_ID = "service_id";
	public static final String TRIP_ID = "trip_id";
	public static final String TRIP_HEADSIGN= "headsign";
	public static final String TRIP_DIRECTION_ID = "direction_id";
	public static final String TRIP_SHAPE_ID = "shape_id";
	public static final String TRIP_WHEELCHAIR_ACCESSIBLE = "wheelchair_accessible";
	// stops.txt (stop_id,stop_code,stop_name,stop_desc,stop_lat,stop_lon,zone_id,wheelchair_boarding)
	public static final String STOP_ID = "stop_id";
	public static final String STOP_CODE = "stop_code";
	public static final String STOP_NAME = "stop_name";
	public static final String STOP_DESC = "stop_desc";
	public static final String STOP_LAT = "stop_lat";
	public static final String STOP_LON = "stop_lon";
	public static final String STOP_ZONE = "zone_id";
	public static final String STOP_WHEELCHAIR_BOARDING = "wheelchair_boarding";
	// (stoptimes.txt)trip_id,arrival_time,departure_time,stop_id,stop_sequence
	public static final String STIMES_TRIP_ID = "trip_id";
	public static final String STIMES_ARRIVAL_TIME = "arrival_time";
	public static final String STIMES_DEPARTURE_TIME = "departure_time";
	public static final String STIMES_STOP_ID = "stop_id";
	public static final String STIMES_STOP_SEQ = "stop_sequence";
	// calendar_dates.txt (service_id,date,exception_type)
	public static final String CDATES_SERVICE_ID = "service_id";
	public static final String CDATES_DATE = "date";
	public static final String CDATES_EXCEPTION_TYPE = "exception_type";
	// calendar.txt (service_id,monday,tuesday,wednesday,thursday,friday,saturday,sunday,start_date,end_date)
	public static final String CAL_SERVICE_ID = "service_id";
	public static final String CAL_MON = "monday";
	public static final String CAL_TUE = "tuesday";
	public static final String CAL_WED = "wednesday";
	public static final String CAL_THR = "thursday";
	public static final String CAL_FRI = "friday";
	public static final String CAL_SAT = "saturday";
	public static final String CAL_SUN = "sunday";
	public static final String CAL_START_DATE = "start_date";
	public static final String CAL_END_DATE = "end_date";

	// ENCODING.
	public static final String UTF8_BOM = "\uFEFF";
	// LOG.
	public static final String LOG_PATH_GEOCODE_REQUEST = "/geocoding_request.log";
	
}
