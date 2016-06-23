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

package it.sayservice.platform.smartplanner.cache;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.io.ByteStreams;

import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
import it.sayservice.platform.smartplanner.data.message.otpbeans.ExtendedCompressedTransitTimeTable;
import it.sayservice.platform.smartplanner.utils.Constants;

public class RoutesDBHelper {

	public static final String DB_NAME = "routesdb";

	public final static String DB_TABLE_CALENDAR = "calendar";
	public final static String DB_TABLE_ROUTE = "route";
	public final static String DB_TABLE_VERSION = "version";

	public final static String AGENCY_ID_KEY = "agencyID";
	public final static String LINEHASH_KEY = "linehash";
	public final static String ROUTE_KEY = "route";
	public static final String CAL_KEY = "calendar";

	public final static String STOPS_IDS_KEY = "stopsIDs";
	public final static String STOPS_NAMES_KEY = "stopsNames";
	public final static String TRIPS_IDS_KEY = "tripIds";
	public final static String ROUTE_IDS_KEY = "routeIds";
	public final static String INVISIBLES_KEY = "invisibles";
	public final static String LINE_KEY = "line";
	public final static String FREQUENCY_KEY = "frequency";
	public final static String SHORT_DESCRIPTION_KEY = "shortDescription";
	public final static String LONG_DESCRIPTION_KEY = "longDescription";
	public final static String VALIDITY_KEY = "validity";
	public final static String SCHEDULE_KEY = "schedule";
	public final static String COMPRESSED_TIMES_KEY = "times";
	public final static String AGENCY_ID = "agencyId";

	public final static String VERSION_KEY = "version";

	private static final String CREATE_CALENDAR_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_CALENDAR + " ("
			+ AGENCY_ID_KEY + " text not null, " + CAL_KEY + " text not null, " + ROUTE_KEY + " text not null);";

	private static final String CREATE_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ROUTE + " ("
			+ LINEHASH_KEY + " text primary key, " + STOPS_IDS_KEY + " text, " + STOPS_NAMES_KEY + " text,"
			+ TRIPS_IDS_KEY + " text," + COMPRESSED_TIMES_KEY + " text," + ROUTE_IDS_KEY + " text," + AGENCY_ID
			+ " text );";

	private static final String CREATE_EXTENDED_ROUTE_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_ROUTE + " ("
			+ LINEHASH_KEY + " text primary key, " + STOPS_IDS_KEY + " text, " + STOPS_NAMES_KEY + " text,"
			+ TRIPS_IDS_KEY + " text," + COMPRESSED_TIMES_KEY + " text," + ROUTE_IDS_KEY + " text," + INVISIBLES_KEY
			+ " text," + LINE_KEY + " text," + FREQUENCY_KEY + " text," + SHORT_DESCRIPTION_KEY + " text,"
			+ LONG_DESCRIPTION_KEY + " text," + VALIDITY_KEY + " text," + SCHEDULE_KEY + " text," + AGENCY_ID
			+ " text );";

	private static final String CREATE_VERSION_TABLE = "CREATE TABLE IF NOT EXISTS " + DB_TABLE_VERSION + " ("
			+ AGENCY_ID_KEY + " integer primary key, " + VERSION_KEY + " integer not null default 0);";

	private static final String DELETE_TABLE = "DROP TABLE IF EXISTS %s";

	private Connection connection;

	private String appId;

	private boolean annotated;

	public RoutesDBHelper(String router, String appId, boolean annotated) throws Exception {
		this.appId = appId;
		this.annotated = annotated;
		init(router);
	}

	private void init(String router) throws Exception {
		Class.forName("org.sqlite.JDBC");
		String cache = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR + System.getProperty("file.separator") + DB_NAME + "_" + appId
				+ extendedPostfix();
		connection = DriverManager.getConnection("jdbc:sqlite:" + cache);

		Statement statement = connection.createStatement();
		statement.setQueryTimeout(30);
		statement.executeUpdate(String.format(DELETE_TABLE, DB_TABLE_CALENDAR));
		statement.executeUpdate(String.format(DELETE_TABLE, DB_TABLE_ROUTE));
		statement.executeUpdate(String.format(DELETE_TABLE, DB_TABLE_VERSION));
		statement.executeUpdate(CREATE_CALENDAR_TABLE);
		if (annotated) {
			statement.executeUpdate(CREATE_EXTENDED_ROUTE_TABLE);
		} else {
			statement.executeUpdate(CREATE_ROUTE_TABLE);
		}
		statement.executeUpdate(CREATE_VERSION_TABLE);
		statement.close();
	}

	public void optimize() throws Exception {
		Statement statement = connection.createStatement();
		statement.executeUpdate("PRAGMA auto_vacuum = 1;");
		statement.executeUpdate("VACUUM;");
		statement.close();
	}

	public void zip(String router) throws Exception {
		String dbName = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR + System.getProperty("file.separator") + DB_NAME + "_" + appId
				+ extendedPostfix();
		FileOutputStream dest = new FileOutputStream(dbName + ".zip");
		ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
		out.setLevel(Deflater.DEFAULT_COMPRESSION);
		File f = new File(dbName);
		FileInputStream fi = new FileInputStream(f);
		BufferedInputStream origin = new BufferedInputStream(fi, 2048);

		ZipEntry entry = new ZipEntry(f.getName());
		out.putNextEntry(entry);

		ByteStreams.copy(fi, out);

		origin.close();
		out.closeEntry();
		out.close();
	}

	public void update(String router, List<String> agencyIds) throws Exception {
		updateCalendars(router, agencyIds);
		updateRoutes(router, agencyIds);
		updateVersions(router, agencyIds);
	}

	private void updateCalendars(String router, List<String> agencyIds) throws Exception {
		Map<String, Map<String, CompressedCalendar>> calendars = getCalendars(router, agencyIds);
		List<String> cal = buildCalendarInserts(calendars);
		Statement statement = connection.createStatement();
		for (String c : cal) {
			statement.execute(c);
		}
		statement.close();
	}

	private void updateRoutes(String router, List<String> agencyIds) throws Exception {
		for (String agencyId : agencyIds) {
			Map<String, ExtendedCompressedTransitTimeTable> updates = getUpdates(router, agencyId);

			for (String linehash : updates.keySet()) {
				updateRoute(updates.get(linehash), linehash, agencyId);
			}
		}
	}

	private void updateVersions(String router, List<String> agencyIds) throws Exception {
		Statement statement = connection.createStatement();
		List<String> qs = buildVersionsInserts(router, agencyIds);
		for (String s : qs) {
			statement.execute(s);
		}
		statement.close();
	}

	private void updateRoute(ExtendedCompressedTransitTimeTable ctt, String linehash, String agencyId)
			throws Exception {
		Statement statement = connection.createStatement();
		String s = buildTimetableInserts(ctt, linehash, agencyId);
		statement.execute(s);
		statement.close();
	}

	private List<String> buildCalendarInserts(Map<String, Map<String, CompressedCalendar>> calendars) throws Exception {
		List<String> results = Lists.newArrayList();
		ObjectMapper mapper = new ObjectMapper();
		for (String agencyId : calendars.keySet()) {
			Map<String, CompressedCalendar> routes = calendars.get(agencyId);
			for (String routeId : routes.keySet()) {
				String s = "INSERT INTO calendar VALUES(";
				s += "\"" + agencyId + "\",";
				String json = escape(mapper.writeValueAsString(routes.get(routeId)));
				s += "\"" + json + "\",";
				s += "\"" + routeId + "\")";
				results.add(s);
			}
		}

		return results;
	}

	private String buildTimetableInserts(ExtendedCompressedTransitTimeTable ctt, String linehash, String agencyId) {
		String s = "INSERT INTO route VALUES(";
		String stopIds = escape(ctt.getStopsId());
		String stopNames = escape(ctt.getStops());

		String invisibles = escape(ctt.getInvisibles());
		String line = escape(ctt.getLine());
		String frequency = escape(ctt.getFrequency());
		String shortDescription = escape(ctt.getShortDescription());
		String longDescription = escape(ctt.getLongDescription());
		String validity = escape(ctt.getValidity());
		String schedule = escape(ctt.getSchedule());

		String tripIds = "  ";
		if (ctt.getTripIds() != null) {
			tripIds = escape(ctt.getTripIds());
		}
		String routeIds = escape(ctt.getRoutesIds());
		s += "\"" + linehash + "\",";
		s += "\"" + removeBrackets(stopIds) + "\",";
		s += "\"" + removeBrackets(stopNames) + "\",";
		s += "\"" + removeBrackets(tripIds) + "\",";
		s += "\"" + ctt.getCompressedTimes() + "\",";
		s += "\"" + routeIds + "\",";

		if (annotated) {
			s += "\"" + removeBrackets(invisibles) + "\",";
			s += "\"" + removeBrackets(line) + "\",";
			s += "\"" + removeBrackets(frequency) + "\",";
			s += "\"" + shortDescription + "\",";
			s += "\"" + longDescription + "\",";
			s += "\"" + validity + "\",";
			s += "\"" + schedule + "\",";
		}

		s += "\"" + agencyId + "\")";
		return s;
	}

	private String escape(Object o) {
		return ((o != null) ? o.toString().replace("\"", "\"\"") : "");
	}

	private String removeBrackets(String s) {
		return ((s.length() > 0) ? (s.substring(1, s.length() - 1)) : "");
	}

	private List<String> buildVersionsInserts(String router, List<String> agencyIds) throws Exception {
		List<String> results = Lists.newArrayList();
		ObjectMapper mapper = new ObjectMapper();
		String s = "INSERT INTO version VALUES(";

		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR;

		for (String agencyId : agencyIds) {
			File file = new File(d, agencyId + Constants.INDEX_SUFFIX);
			AgencyCacheIndex aci = null;
			if (file.exists()) {
				aci = mapper.readValue(file, AgencyCacheIndex.class);
			}

			String q = s + "\"" + agencyId + "\",\"" + aci.getVersion() + "\")";
			results.add(q);
		}

		return results;
	}

	private Map<String, Map<String, CompressedCalendar>> getCalendars(String router, List<String> agencyIds) throws Exception {
		Map<String, Map<String, CompressedCalendar>> results = Maps.newTreeMap();
		ObjectMapper mapper = new ObjectMapper();

		String d0 = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + Constants.CLIENT_CACHE_DIR;

		for (String agencyId : agencyIds) {
			AgencyCacheIndex aci = new AgencyCacheIndex(agencyId);
			Map<String, CompressedCalendar> agencyMap = Maps.newTreeMap();

			String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router + System.getProperty("file.separator") +  Constants.CACHE_DIR
					+ System.getProperty("file.separator") + Constants.CLIENT_CACHE_DIR
					+ System.getProperty("file.separator") + agencyId;
			File dir = new File(d);

			for (File f : dir.listFiles()) {
				if (f.length() != 0) {
					if (f.getName().contains("calendar")) {
						String id = f.getName().replace(".js", "");
						CompressedCalendar cCal = aci.loadCalendar(router, id);
						id = id.replace("calendar_", "");
						agencyMap.put(id, cCal);
					}
				}
			}

			results.put(agencyId, agencyMap);
		}

		return results;
	}

	private Map<String, ExtendedCompressedTransitTimeTable> getUpdates(String router, String agencyId) throws Exception {
		Map<String, ExtendedCompressedTransitTimeTable> results = Maps.newTreeMap();
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String d0 = System.getenv("OTP_HOME") + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + Constants.CLIENT_CACHE_DIR;
		File dir0 = new File(d0);

		Set<String> calendars = Sets.newHashSet();
//		for (String agencyId : agencyIds) {

			String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router + System.getProperty("file.separator") +  Constants.CACHE_DIR
					+ System.getProperty("file.separator") + Constants.CLIENT_CACHE_DIR
					+ System.getProperty("file.separator") + agencyId;
			File dir = new File(d);

			for (File f : dir.listFiles()) {
				if (f.length() != 0) {
					FileInputStream fis = new FileInputStream(f);
					InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

					if (f.getName().contains("calendar")) {
						Map<String, String> cal = mapper.readValue(isr, Map.class);
						String id = f.getName().replace("calendar_", "").replace(".js", "");
						for (String val : cal.values()) {
							if (!val.equals("null")) {
								calendars.add(id + "_" + val);
							}
						}
					} else {

						try {
							ExtendedCompressedTransitTimeTable result = mapper.readValue(isr,
									ExtendedCompressedTransitTimeTable.class);
							results.put(f.getName().replace(".js", ""), result);
						} catch (Exception e) {
							System.out.println("EX: " + e.getMessage() + " -> " + f.getName());
						}
					}
				}
			}
//		}

		Set<String> keys = Sets.newHashSet(results.keySet());
		for (String key : keys) {
			if (!calendars.contains(key)) {
				results.remove(key);
			}
		}

		return results;
	}

	private String extendedPostfix() {
		return annotated ? "_extended" : "";
	}

}
