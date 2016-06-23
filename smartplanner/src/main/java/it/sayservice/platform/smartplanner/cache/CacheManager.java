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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.hash.Hashing;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
import it.sayservice.platform.smartplanner.data.message.cache.PartialAgencyRequest;
import it.sayservice.platform.smartplanner.data.message.otpbeans.CompressedTransitTimeTable;
import it.sayservice.platform.smartplanner.data.message.otpbeans.Route;
import it.sayservice.platform.smartplanner.data.message.otpbeans.TransitTimeTable;
import it.sayservice.platform.smartplanner.otp.OTPHandler;
import it.sayservice.platform.smartplanner.otp.OTPManager;
import it.sayservice.platform.smartplanner.otp.TransitScheduleResults;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayException;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayFilter;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;

@Component
public class CacheManager {

	@Autowired
	private OTPHandler handler;
	@Autowired
	private OTPManager manager;

	private boolean initialized = false;

	public Map<String, AgencyCacheIndex> indexes;
	public Map<String, CompressedTransitTimeTable> fileCache;

	private String destinationDirectory = Constants.CLIENT_CACHE_DIR;

	public CacheManager() {
		super();
		// TODO Auto-generated constructor stub
	}

	public CacheManager(String router) {
		checkDestinationDirectory(router);
	}

	public CacheManager(String router, OTPManager manager, OTPHandler handler) throws Exception {
		this.manager = manager;
		this.handler = handler;
		checkDestinationDirectory(router);
	}

	public CacheManager(String router, OTPManager manager, OTPHandler handler, String destinationDirectory)
			throws Exception {
		this.manager = manager;
		this.handler = handler;
		this.destinationDirectory = destinationDirectory;
		checkDestinationDirectory(router);
	}

	public void checkDestinationDirectory(String router) {
		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ destinationDirectory + System.getProperty("file.separator");
		File destDir = new File(d);
		if (!destDir.exists()) {
			destDir.mkdir();
		}
	}

	public void init(String router) throws Exception {
		indexes = new TreeMap<String, AgencyCacheIndex>();
		fileCache = new TreeMap<String, CompressedTransitTimeTable>();
		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ destinationDirectory + System.getProperty("file.separator");
		File dir = new File(d);
		File files[] = dir.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File file, String name) {
				boolean isNumber = true;
				try {
					Integer.parseInt(name);
				} catch (NumberFormatException e) {
					isNumber = false;
				}
				return file.isDirectory() && isNumber;
			}
		});

		List<String> agencyDirs = new ArrayList<String>();
		for (File file : files) {
			agencyDirs.add(file.getName());
		}

		ObjectMapper mapper = new ObjectMapper();
		for (String agencyDir : agencyDirs) {
			File file = new File(d + agencyDir + Constants.INDEX_SUFFIX);
			AgencyCacheIndex aci = null;
			if (file.exists()) {
				aci = mapper.readValue(file, AgencyCacheIndex.class);
			} else {
				System.out.println("Index file for " + agencyDir + " not found, generating it.");
				aci = createIndex(agencyDir);
				File f = new File(d, agencyDir + Constants.INDEX_SUFFIX);
				mapper.writeValue(f, aci);
			}
			indexes.put(aci.getAgencyId(), aci);
		}

		initialized = true;
	}

	private AgencyCacheIndex createIndex(String agencyId) {
		AgencyCacheIndex aci = new AgencyCacheIndex(agencyId);
		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + destinationDirectory + System.getProperty("file.separator")
				+ agencyId;
		File dir = new File(d);
		File files[] = dir.listFiles();
		List<String> newFiles = new ArrayList<String>();
		for (File file : files) {
			newFiles.add(file.getName().replace(".js", ""));
		}
		aci.update(newFiles, newFiles, null);
		return aci;
	}

	public void updateCache(String router, String agencyId, boolean tripsIds, boolean old) throws Exception {
		updateCache(router, agencyId, tripsIds, false, old);
	}

	public void updateCache(String router, String agencyId, boolean tripsIds, boolean csv, boolean old)
			throws Exception {
		if (!initialized) {
			init(router);
		}

		System.out.println("Updating cache for " + agencyId);

		Map<CacheEntryStatus, Collection<String>> written;
		if (old) {
			written = oldBuildCache(router, agencyId, tripsIds, csv);
		} else {
			written = buildCache(router, agencyId, tripsIds, csv);
		}
		AgencyCacheIndex aci;
		if (indexes.containsKey(agencyId)) {
			aci = indexes.get(agencyId);
		} else {
			aci = new AgencyCacheIndex(agencyId);
		}
		aci.update(written.get(CacheEntryStatus.WRITTEN), written.get(CacheEntryStatus.ADDED),
				written.get(CacheEntryStatus.REMOVED));

		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ destinationDirectory + System.getProperty("file.separator");
		File f = new File(d, agencyId + Constants.INDEX_SUFFIX);
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(f, aci);

		indexes.put(agencyId, aci);

		System.out.println("Updated cache for " + agencyId);
	}

	private Map<CacheEntryStatus, Collection<String>> buildCache(String router, String agencyId, boolean tripsIds)
			throws Exception {
		return buildCache(router, agencyId, tripsIds, false);
	}

	private Map<CacheEntryStatus, Collection<String>> oldBuildCache(String router, String agencyId, boolean tripsIds,
			boolean csv) throws Exception {
		List<String> updated = new ArrayList<String>();
		List<String> written = new ArrayList<String>();

		Multimap<CacheEntryStatus, String> result = ArrayListMultimap.create();

		ObjectMapper mapper = new ObjectMapper();

		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + destinationDirectory + System.getProperty("file.separator")
				+ agencyId;
		String d2 = System.getenv("OTP_HOME") + System.getProperty("file.separator") + Constants.CACHE_DIR
				+ System.getProperty("file.separator") + "csv" + System.getProperty("file.separator") + agencyId;

		File dir = new File(d);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (csv) {
			File dir2 = new File(d2);
			if (!dir2.exists()) {
				dir2.mkdir();
			}
		}

		Map<String, WeekdayFilter> weekdayFilter = handler.readAgencyWeekDay(router, agencyId);
		Map<String, WeekdayException> weekdayException = handler.readAgencyWeekDayExceptions(router, agencyId);

		Multimap<String, String> daysMap = ArrayListMultimap.create();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		for (String eq : weekdayFilter.keySet()) {
			WeekdayFilter filter = weekdayFilter.get(eq);
			String from = filter.getFromDate();
			String to = filter.getToDate();

			Calendar fromDate = new GregorianCalendar();
			Calendar toDate = new GregorianCalendar();

			fromDate.setTime(df.parse(from));
			toDate.setTime(df.parse(to));
			Calendar date = new GregorianCalendar();
			date.setTime(fromDate.getTime());
			String prevDay = null;
			while (df.format(date.getTime()).compareTo(to) <= 0) {
				String day = df.format(date.getTime());

				boolean sameDay = day.equals(prevDay);

				if (!sameDay) {
					int dotw = convertDayOfTheWeek(date.get(Calendar.DAY_OF_WEEK));
					if (filter.getDays()[dotw]) {
						daysMap.put(day, eq);
					}
				}
				prevDay = day;
				date.setTime(new Date(date.getTime().getTime() + (RecurrentUtil.DAY)));
			}

		}

		for (String key : weekdayException.keySet()) {
			WeekdayException ex = weekdayException.get(key);
			for (String toAdd : ex.getAdded()) {
				daysMap.put(toAdd, key);
			}
			for (String toRemove : ex.getRemoved()) {
				daysMap.remove(toRemove, key);
			}
		}

		Multimap<String, String> reversedDaysMap = ArrayListMultimap.create();
		for (String day : daysMap.keySet()) {
			String dayKey = getEqString(daysMap.get(day).toString(), agencyId);
			reversedDaysMap.put(dayKey, day);
		}

		SortedSet<String> calendarData = new TreeSet<String>();
		for (String key : reversedDaysMap.keySet()) {
			for (String day : reversedDaysMap.get(key)) {
				calendarData.add("\"" + day + "\":\"" + key + "\"");
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for (String line : calendarData) {
			sb.append(line + ",\n");
		}
		sb.replace(sb.length() - 2, sb.length() - 1, "");
		sb.append("};");
		String c = sb.toString();

		String fn = d + "/calendar.js";
		boolean toIndex = !compare(fn, c);
		written.add("calendar.js");
		result.put(CacheEntryStatus.WRITTEN, "calendar");
		if (toIndex) {
			result.put(CacheEntryStatus.ADDED, "calendar");
		}

		FileWriter fw = new FileWriter(fn);
		fw.write(c);
		fw.close();

		for (String key : reversedDaysMap.keySet()) {
			String randomDay = (String) ((List) reversedDaysMap.get(key)).get(0);

			Calendar randomDate = new GregorianCalendar();

			randomDate.setTime(df.parse(randomDay));

			long from = randomDate.getTimeInMillis();
			long to = from + RecurrentUtil.DAY - 1000 * 60;

			List<Route> allRoutes = handler.getRoutes(router);

			for (Route route : allRoutes) {

				String id = null;
				if (route.getId().getAgency().equals(agencyId)) {
					id = route.getId().getId();
				} else {
					continue;
				}
				String res2 = manager.getTransitSchedule(router, agencyId, id, from, to, TransitScheduleResults.TIMES,
						tripsIds);

				TransitTimeTable ttt = mapper.readValue(res2, TransitTimeTable.class);
				CompressedTransitTimeTable cttt = new CompressedTransitTimeTable(ttt);

				String res3 = mapper.writeValueAsString(cttt);

				fn = d + "/" + id + "_" + key + ".js";
				boolean toWrite = ttt.getTimes().get(0).size() != 0;
				toIndex = !compare(fn, toWrite ? res3 : "");
				fw = new FileWriter(fn);
				if (toWrite) {
					fw.write(res3);
					result.put(CacheEntryStatus.WRITTEN, id + "_" + key);
					if (toIndex) {
						result.put(CacheEntryStatus.ADDED, id + "_" + key);
					}
				}
				written.add(id + "_" + key + ".js");
				fw.close();
			}

		}

		File[] old = new File(d).listFiles();
		for (File f : old) {
			if (!written.contains(f.getName())) {
				result.put(CacheEntryStatus.REMOVED, f.getName().replace(".js", ""));
				break;
			}
		}

		return result.asMap();
	}

	private Map<CacheEntryStatus, Collection<String>> buildCache(String router, String agencyId, boolean tripsIds,
			boolean csv) throws Exception {
		List<String> updated = new ArrayList<String>();
		List<String> written = new ArrayList<String>();

		Multimap<CacheEntryStatus, String> result = ArrayListMultimap.create();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

		String d0 = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router + System.getProperty("file.separator") +  Constants.CACHE_DIR
				+ System.getProperty("file.separator") + destinationDirectory;
		String d = d0 + System.getProperty("file.separator") + agencyId;
		String d2 = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router + System.getProperty("file.separator") +Constants.CACHE_DIR
				+ System.getProperty("file.separator") + "csv" + System.getProperty("file.separator") + agencyId;

		File dir = new File(d);
		if (!dir.exists()) {
			dir.mkdir();
		}
		if (csv) {
			File dir2 = new File(d2);
			if (!dir2.exists()) {
				dir2.mkdir();
			}
		}

		Map<String, WeekdayFilter> weekdayFilter = handler.readAgencyWeekDay(router, agencyId);
		Map<String, WeekdayException> weekdayException = handler.readAgencyWeekDayExceptions(router, agencyId);

		Multimap<String, String> daysMap = ArrayListMultimap.create();
		DateFormat df = new SimpleDateFormat("yyyyMMdd");

		for (String eq : weekdayFilter.keySet()) {
			WeekdayFilter filter = weekdayFilter.get(eq);
			String from = filter.getFromDate();
			String to = filter.getToDate();

			Calendar fromDate = new GregorianCalendar();
			Calendar toDate = new GregorianCalendar();

			fromDate.setTime(df.parse(from));
			toDate.setTime(df.parse(to));
			Calendar date = new GregorianCalendar();
			date.setTime(fromDate.getTime());
			String prevDay = null;
			while (df.format(date.getTime()).compareTo(to) <= 0) {
				String day = df.format(date.getTime());

				boolean sameDay = day.equals(prevDay);

				if (!sameDay) {
					int dotw = convertDayOfTheWeek(date.get(Calendar.DAY_OF_WEEK));
					if (filter.getDays()[dotw]) {
						daysMap.put(day, eq);
					}
				}
				prevDay = day;
				date.setTime(new Date(date.getTime().getTime() + (RecurrentUtil.DAY)));
			}

		}

		for (String key : weekdayException.keySet()) {
			WeekdayException ex = weekdayException.get(key);
			for (String toAdd : ex.getAdded()) {
				daysMap.put(toAdd, key);
			}
			for (String toRemove : ex.getRemoved()) {
				daysMap.remove(toRemove, key);
			}
		}

		List<Route> allRoutes = handler.getRoutes(router);

		for (Route route : allRoutes) {

			String id = null;
			if (route.getId().getAgency().equals(agencyId)) {
				id = route.getId().getId();
			} else {
				continue;
			}

			CalendarBuildResult buildResult = buildCalendarFile(router, agencyId, id, daysMap, written, result, d);

			for (String key : buildResult.getCalendar().keySet()) {

				if (buildResult.getEmpty().contains(key)) {
					continue;
				}

				List<String> days = (List<String>) buildResult.getCalendar().get(key);
				Collections.sort(days);
				String randomDay = (String) days.get(0);
				Calendar randomDate = new GregorianCalendar();
				randomDate.setTime(df.parse(randomDay));
				long from = randomDate.getTimeInMillis();
				long to = from + RecurrentUtil.DAY - 1000 * 60;

				String res2 = manager.getTransitSchedule(router, agencyId, id, from, to, TransitScheduleResults.TIMES,
						tripsIds);

				TransitTimeTable ttt = mapper.readValue(res2, TransitTimeTable.class);
				CompressedTransitTimeTable cttt = new CompressedTransitTimeTable(ttt);

				List<String> routesIds = Lists.newArrayList();
				for (String tid : cttt.getTripIds()) {
					routesIds.add(route.getId().getId());
				}
				cttt.setRoutesIds(routesIds);

				String res3 = mapper.writeValueAsString(cttt);

				String fn = d + "/" + id + "_" + key + ".js";
				boolean toWrite = ttt.getTimes().get(0).size() != 0;
				boolean toIndex = !compare(fn, toWrite ? res3 : "");
				FileWriter fw = new FileWriter(fn);
				if (toWrite) {
					fw.write(res3);

					if (csv) {
						String fn2 = d2 + "/" + id + "_" + key + ".csv";
						FileWriter fw2 = new FileWriter(fn2);
						fw2.write(ttt.toCSV());
						fw2.close();
					}

					result.put(CacheEntryStatus.WRITTEN, id + "_" + key);
					if (toIndex) {
						result.put(CacheEntryStatus.ADDED, id + "_" + key);
					}
				}
				written.add(id + "_" + key + ".js");
				fw.close();
			}

		}

		File[] old = new File(d).listFiles();
		for (File f : old) {
			if (!written.contains(f.getName())) {
				result.put(CacheEntryStatus.REMOVED, f.getName().replace(".js", ""));
				break;
			}
		}

		return result.asMap();
	}

	private CalendarBuildResult buildCalendarFile(String router, String agencyId, String routeId,
			Multimap<String, String> daysMap, List<String> written, Multimap<CacheEntryStatus, String> result,
			String dir) throws Exception {
		CalendarBuildResult buildResult = new CalendarBuildResult();
		Set<String> empty = new TreeSet<String>();
		List<String> calendar = manager.getRouteCalendarEntries(router, routeId);

		Multimap<List<String>, String> reversedDaysMap = ArrayListMultimap.create();
		Multimap<String, String> compactReversedDaysMap = ArrayListMultimap.create();
		for (String day : daysMap.keySet()) {
			List<String> sl = new ArrayList<String>(daysMap.get(day));
			Collections.sort(sl);
			String dayKey = getEqString(sl, agencyId);
			reversedDaysMap.put(sl, day);
		}

		SortedSet<String> calendarData = new TreeSet<String>();
		for (List<String> key : reversedDaysMap.keySet()) {
			List<String> shortKey = new ArrayList<String>(key);
			for (Iterator<String> iterator = shortKey.iterator(); iterator.hasNext();) {
				String next = iterator.next().replace(agencyId + "_", "");
				if (!calendar.contains(next)) {
					iterator.remove();
				}
			}

			String sk = getEqString(shortKey, agencyId);
			compactReversedDaysMap.putAll(sk, reversedDaysMap.get(key));

			for (String day : compactReversedDaysMap.get(sk)) {
				if (!shortKey.isEmpty()) {
					calendarData.add("\"" + day + "\":\"" + sk + "\"");
				} else {
					calendarData.add("\"" + day + "\":\"" + null + "\"");
					empty.add(sk);
				}
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{\n");
		for (String line : calendarData) {
			sb.append(line + ",\n");
		}
		sb.replace(sb.length() - 2, sb.length() - 1, "");
		sb.append("};");
		String c = sb.toString();

		String cn = "calendar_" + routeId + ".js";
		String fn = dir + "/" + cn;

		boolean toIndex = !compare(fn, c);
		written.add(cn);
		result.put(CacheEntryStatus.WRITTEN, "calendar_" + routeId);
		if (toIndex) {
			result.put(CacheEntryStatus.ADDED, "calendar_" + routeId);
		}

		FileWriter fw = new FileWriter(fn);
		fw.write(c);
		fw.close();

		buildResult.setCalendar(compactReversedDaysMap);
		buildResult.setEmpty(empty);

		return buildResult;
	}

	public Map<String, CacheUpdateResponse> getStatus(String router, Map<String, String> versions) throws Exception {
		if (!initialized) {
			init(router);
		}

		Map<String, CacheUpdateResponse> result = new TreeMap<String, CacheUpdateResponse>();
		for (String agencyId : versions.keySet()) {
			result.put(agencyId, getStatus(router, agencyId, Long.parseLong(versions.get(agencyId))));
		}
		return result;
	}

	private CacheUpdateResponse getStatus(String router, String agencyId, long version) throws IOException {
		if (indexes.containsKey(agencyId)) {
			return indexes.get(agencyId).getUpdate(router, version);
		} else {
			return null;
		}
	}

	public Map<String, CacheUpdateResponse> getPartialStatus(String router, Map<String, Map> par) throws Exception {
		if (!initialized) {
			init(router);
		}

		Map<String, CacheUpdateResponse> result = new TreeMap<String, CacheUpdateResponse>();
		for (String agencyId : par.keySet()) {
			PartialAgencyRequest req = null;

			try {
				ObjectMapper mapper = new ObjectMapper();
				req = mapper.convertValue(par.get(agencyId), PartialAgencyRequest.class);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}

			CacheUpdateResponse cur = getStatus(router, agencyId, Long.parseLong(req.getVersion()));
			if (req.getRoutes() != null) {
				filterByRoute(cur, req.getRoutes());
			}
			result.put(agencyId, cur);
		}
		return result;
	}

	private void filterByRoute(CacheUpdateResponse value, List<String> routes) {
		Iterator<String> it = value.getAdded().iterator();
		while (it.hasNext()) {
			String entry = it.next();
			if (!toKeep(entry, routes)) {
				it.remove();
			}
		}
		it = value.getRemoved().iterator();
		while (it.hasNext()) {
			String entry = it.next();
			if (!toKeep(entry, routes)) {
				it.remove();
			}
		}

		Map<String, CompressedCalendar> newCalendars = new TreeMap<String, CompressedCalendar>();
		for (String route : routes) {
			String key = "calendar_" + route;
			CompressedCalendar calendar = value.getCalendars().get(key);
			newCalendars.put(key, calendar);
		}
		value.setCalendars(newCalendars);
	}

	private boolean toKeep(String entry, List<String> routes) {
		for (String route : routes) {
			if (entry.startsWith(route + "_")) {
				return true;
			}
		}
		return false;
	}

	public CompressedTransitTimeTable getUpdate(String router, String agencyId, String fileName) throws Exception {
		if (!initialized) {
			init(router);
		}
		String key = agencyId + "#" + fileName;
		if (fileCache.containsKey(key)) {
			return fileCache.get(key);
		} else {
			String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
					+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
					+ destinationDirectory + System.getProperty("file.separator") + agencyId;
			File f = new File(d, fileName + ".js");

			ObjectMapper mapper = new ObjectMapper();
			CompressedTransitTimeTable result = mapper.readValue(f, CompressedTransitTimeTable.class);
			fileCache.put(key, result);
			return result;
		}

	}

	public Map<String, Long> getVersions(String router) throws Exception {
		Map<String, Long> results = Maps.newHashMap();
		ObjectMapper mapper = new ObjectMapper();

		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR;
		File dir = new File(d);

		for (File file : dir.listFiles()) {
			if (!file.getName().endsWith(Constants.INDEX_SUFFIX)) {
				continue;
			}
			String agencyId = file.getName().replace(Constants.INDEX_SUFFIX, "");

			AgencyCacheIndex aci = null;
			aci = mapper.readValue(file, AgencyCacheIndex.class);

			results.put(agencyId, aci.getVersion());
		}

		return results;
	}

	private Boolean compare(String fileName, String content) throws IOException {
		FileReader fr = null;
		try {
			fr = new FileReader(fileName);
		} catch (FileNotFoundException e) {
			return false;
		}
		String s = "";
		int c;
		while ((c = fr.read()) != -1) {
			s += (char) c;
		}

		fr.close();
		boolean res = s.equals(content);
		if (!res) {
			System.out.println(fileName + " = " + s.length() + " / " + content.length());
		}

		return res;
	}

	private int convertDayOfTheWeek(int day) {
		int conv = day - 2;
		if (conv < 0) {
			conv = 6;
		}
		return conv;
	}

	private String getEqString(String eqs, String agencyId) {
		String eq = eqs;
		eq = eq.replaceAll(agencyId, "").replaceAll("[_ ]", "").replaceAll(",", ";").replaceAll("[\\[\\]]", "");
		return Hashing.sha1().hashString(eq, Charset.forName("UTF-8")).toString();
	}

	private String getEqString(List<String> eqs, String agencyId) {
		List<String> sEqus = new ArrayList<String>(eqs);
		Collections.sort(sEqus);
		String eq = sEqus.toString();
		eq = eq.replaceAll(agencyId, "").replaceAll("[_ ]", "").replaceAll(",", ";").replaceAll("[\\[\\]]", "");
		return Hashing.sha1().hashString(eq, Charset.forName("UTF-8")).toString();
	}

}
