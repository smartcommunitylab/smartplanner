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

package it.sayservice.platform.smartplanner.cache.annotated;

import java.io.File;
import java.io.FileInputStream;
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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.Sets;
import com.google.common.collect.TreeMultimap;
import com.google.common.hash.Hashing;
import com.google.gdata.util.io.base.UnicodeReader;

import it.sayservice.platform.smartplanner.cache.AgencyCacheIndex;
import it.sayservice.platform.smartplanner.cache.CacheEntryStatus;
import it.sayservice.platform.smartplanner.cache.CacheIndexEntry;
import it.sayservice.platform.smartplanner.otp.OTPHandler;
import it.sayservice.platform.smartplanner.otp.schedule.StopNames;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayException;
import it.sayservice.platform.smartplanner.otp.schedule.WeekdayFilter;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.RecurrentUtil;

public class AnnotatedReader {

	private OTPHandler handler;

	public AnnotatedReader(String router, OTPHandler handler) {
		this.handler = handler;

		String auxDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") +
				router + System.getProperty("file.separator") + "cache"
				+ System.getProperty("file.separator") + "client" + System.getProperty("file.separator")
				+ Constants.AUXILIARY_CACHE_DIR;
		File d = new File(auxDir);
		if (!d.exists()) {
			d.mkdir();
		}
	}

	public void generateCache(String router, String agencyId, boolean setTripsIds) throws Exception {
		String dir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + "cache" + System.getProperty("file.separator") + "annotated"
				+ System.getProperty("file.separator") + agencyId;
		File files[] = new File(dir).listFiles();
		List<AnnotatedTimetable> annotatedTimetables = Lists.newArrayList();

		if (files == null) {
			System.err.println("No files found for " + agencyId);
			return;
		}

		for (File file : files) {
			if (!file.getName().endsWith(".csv")) {
				continue;
			}
			try {
				annotatedTimetables.add(read(agencyId, file.getAbsolutePath()));
			} catch (Exception e) {
				System.err.println("Problems reading " + file.getName());
				e.printStackTrace();
				System.exit(0);
			}
		}
		buildTimetable(router, agencyId, setTripsIds, annotatedTimetables);
		processAnnnotatedTimetables(router, agencyId, annotatedTimetables);
	}

	private void processAnnnotatedTimetables(String router, String agencyId, List<AnnotatedTimetable> annotatedTimetables)
			throws Exception {
		List<AnnotatedTrip> annotatedTrips = Lists.newArrayList();

		for (AnnotatedTimetable annotatedTimetable : annotatedTimetables) {
			int order = 0;
			for (AnnotatedColumn annotatedColumn: annotatedTimetable.getColumns()) {
				int direction = annotatedColumn.getSymbolicRouteId().endsWith("R")?1:0;
				AnnotatedTrip annotatedTrip = new AnnotatedTrip(annotatedColumn.getTripId(),annotatedColumn.getRouteId(), agencyId, annotatedColumn.getSymbolicRouteId(), annotatedColumn.getTimes(), order++, direction);
				annotatedTrips.add(annotatedTrip);
			}
		}

		String cacheDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + "cache" + System.getProperty("file.separator") + "client";
		String annotatedFile = cacheDir + System.getProperty("file.separator") + Constants.AUXILIARY_CACHE_DIR
				+ System.getProperty("file.separator") + agencyId + "_" + Constants.ANNOTATED_TRIPS + ".txt";
		ObjectMapper mapper = new ObjectMapper();
		mapper.writeValue(new File(annotatedFile), annotatedTrips);

		// return annotatedTrips;
	}

	private AnnotatedTimetable read(String agencyId, String fileName) throws Exception {
		System.out.println("Reading: " + fileName);

		List<String[]> lines = readCSV(fileName);

		int index = fileName.lastIndexOf(System.getProperty("file.separator"));
		String routeId = fileName.substring(index + 1);
		index = routeId.lastIndexOf("-");
		index = routeId.indexOf("-");
		routeId = routeId.substring(0, index).replace("_", "");

		AnnotatedTimetable aTimetable = new AnnotatedTimetable();
		List<String> tripIds = Lists.newArrayList();
		List<String> serviceIds = Lists.newArrayList();

		List<String> stopsIds = Lists.newArrayList();
		List<String> stopNames = Lists.newArrayList();
		List<Integer> invisibles = Lists.newArrayList();
		List<List<String>> cols = Lists.newArrayList();
		List<String> frequenza = Lists.newArrayList();
		List<String> linea = Lists.newArrayList();
		List<String> routeIds = Lists.newArrayList();

		List<Integer> toSkip = Lists.newArrayList();
		boolean stopStarted = false;

		int maxLine = 0;
		for (String[] line : lines) {
			maxLine = Math.max(maxLine, line.length);
		}

		for (String[] line : lines) {
			if (line.length < 2) {
				continue;
			}

			if (stopStarted) {
				boolean skip = true;
				for (int i = 2; i < line.length; i++) {
					if (!line[i].isEmpty()) {
						skip = false;
						break;
					}
				}
				if (skip) {
					System.err.println("Skipping empty line: " + line[0]);
					continue;
				}
			}

			String c0 = line[0];
			String c1 = line[1];
			String c2 = line[2];
			if ("Descr.breve:".equals(c0)) {
				aTimetable.setShortDescription(c2);
			}
			if ("Descr.lunga:".equals(c0)) {
				aTimetable.setLongDescription(c2);
			}
			if ("Validità:".equals(c0)) {
				aTimetable.setValidity(c2);
			}
			if ("Orario:".equals(c0)) {
				aTimetable.setSchedule(c2);
			}

			if ("Linea:".equals(c0)) {
				for (int i = 2; i < line.length; i++) {
					linea.add(line[i]);
				}
			}
			if ("Frequenza:".equals(c0)) {
				for (int i = 2; i < line.length; i++) {
					frequenza.add(line[i]);
				}
			}
			if ("smartplanner route_id".equals(c0)) {
				for (int i = 2; i < line.length; i++) {
					routeIds.add(line[i]);
				}
			}

			if (stopStarted) {
				if (c0.startsWith("*")) {
					invisibles.add(1);
				} else {
					invisibles.add(0);
				}
				if (c0.isEmpty()) {
					System.err.println("Empty line in " + fileName);
					break;
				}
				stopNames.add(c0.replace("*", ""));
				stopsIds.add(c1 + "_" + agencyId);
				for (int i = 0; i < cols.size(); i++) {
					String time = line[i + 2].replace("-", "").replace(".", ":");
					cols.get(i).add(time);
				}
			}

			if ("gtfs trip_id".equals(c0)) {
				for (int i = 2; i < line.length; i++) {
					if (line[i].isEmpty()) {
						toSkip.add(i - 2);
					}
					tripIds.add(line[i]);
					cols.add(new ArrayList<String>());
				}
			}

			if ("service_id".equals(c0)) {
				for (int i = 2; i < line.length; i++) {
					if (line[i].contains("$")) {
						System.out.println("warn " + line[i]);
					}
					serviceIds.add(line[i]);
				}
			}

			if ("stops".equals(c0)) {
				stopStarted = true;
			}

		}

		for (int i = linea.size(); i < cols.size(); i++) {
			linea.add("");
		}
		for (int i = frequenza.size(); i < cols.size(); i++) {
			frequenza.add("");
		}

		Collections.reverse(toSkip);

		for (int i : toSkip) {
			tripIds.remove(i);
			serviceIds.remove(i);
			cols.remove(i);
			routeIds.remove(i);
			frequenza.remove(i);
			linea.remove(i);
		}
		int keepN = tripIds.size();
		serviceIds = serviceIds.subList(0, keepN);
		cols = cols.subList(0, keepN);
		routeIds = routeIds.subList(0, keepN);
		frequenza = frequenza.subList(0, keepN);
		linea = linea.subList(0, keepN);

		List<AnnotatedColumn> aCols = Lists.newArrayList();
		for (String tripId : tripIds) {
			int tripIndex = tripIds.indexOf(tripId);
			AnnotatedColumn aCol = new AnnotatedColumn();
			aCol.setTripId(tripId);
			aCol.setSymbolicRouteId(routeId);
			aCol.setRouteId(routeIds.get(tripIndex));
			aCol.setServiceId(serviceIds.get(tripIndex));
			List<String> times = Lists.newArrayList();
			int i = 0;
			for (String stopId : stopsIds) {
				String time = cols.get(tripIndex).get(i);
				time = fixTimes(time);
				times.add(time);
				i++;
			}
			aCol.setTimes(times);

			aCols.add(aCol);
		}
		aTimetable.setColumns(aCols);

		aTimetable.setStopNames(stopNames);
		aTimetable.setStopIds(stopsIds);
		aTimetable.setInvisibles(invisibles);
		aTimetable.setFrequency(frequenza);
		aTimetable.setRouteIds(routeIds);
		aTimetable.setLine(linea);
		aTimetable.setSymbolicRouteId(routeId);

		boolean eq = aCols.size() == routeIds.size() && frequenza.size() == linea.size()
				&& aCols.size() == frequenza.size();
		if (!eq) {
			System.err.println("ERRROR: different column sizes.");
		}

		return aTimetable;
	}

	public void buildTimetable(String router, String agencyId, boolean setTripsIds, List<AnnotatedTimetable> timetables)
			throws Exception {
		AgencyCacheIndex aci = new AgencyCacheIndex(agencyId);
		aci.setVersion(1);

		ObjectMapper mapper = new ObjectMapper();

		String cacheDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + "cache" + System.getProperty("file.separator") + "client";
		String agencyDir = cacheDir + System.getProperty("file.separator") + agencyId;
		File dir = new File(agencyDir);
		if (!dir.exists()) {
			dir.mkdir();
		} else {
			for (File f : dir.listFiles()) {
				f.delete();
			}
		}

		Map<String, WeekdayFilter> weekdayFilter = handler.readAgencyWeekDay(router, agencyId);
		Map<String, WeekdayException> weekdayException = handler.readAgencyWeekDayExceptions(router, agencyId);

		Multimap<String, ExtendedAnnotatedColumn> extendedAnnotatedColumn = createExtendedAnnotatedColumns(timetables);
		SetMultimap<String, String> tripsSymbolicRouteId = TreeMultimap.create();

		List<SymbolicRouteDayInfoHashCalendar> agencySrdihs = Lists.newArrayList();

		for (String rId : extendedAnnotatedColumn.keySet()) {
			System.out.println("Generating " + rId);

			Map<String, AnnotatedColumn> tripsColumns = Maps.newTreeMap();
			Map<String, AnnotatedTimetable> tripsTable = Maps.newTreeMap();
			Map<String, AnnotatedTimetable> serviceTable = Maps.newTreeMap();

			AnnotatedTimetable baseTable = null;

			Multimap<String, String> daysTrips = ArrayListMultimap.create();
			SetMultimap<String, String> daysServices = TreeMultimap.create();
			Map<String, AnnotatedTimetable> daysTable = Maps.newTreeMap();
			Map<String, String> daysHashes = Maps.newTreeMap();
			Map<String, String> tripsRoutes = Maps.newTreeMap();

			for (ExtendedAnnotatedColumn ac : extendedAnnotatedColumn.get(rId)) {
				baseTable = ac.getSource();

				String tripId = agencyId + "_" + ac.getTripId();
				tripsSymbolicRouteId.put(tripId, rId);
				tripsRoutes.put(ac.getTripId(), ac.getRouteId());

				tripsColumns.put(tripId, ac);

				tripsTable.put(tripId, ac.getSource());

				String serviceId = agencyId + "_" + ac.getServiceId();

				serviceTable.put(serviceId, ac.getSource());

				WeekdayFilter filter = weekdayFilter.get(serviceId);
				if (filter == null) {
					System.out.println("ServiceId not found: " + serviceId);
				}

				Set<String> days = Sets.newHashSet();

				DateFormat df = new SimpleDateFormat("yyyyMMdd");
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
							days.add(day);
						}
					}
					prevDay = day;
					date.setTime(new Date(date.getTime().getTime()));
					date.add(Calendar.DAY_OF_YEAR, 1);
				}

				WeekdayException ex = weekdayException.get(serviceId);
				if (ex != null) {
					for (String toAdd : ex.getAdded()) {
						days.add(toAdd);
					}
					for (String toRemove : ex.getRemoved()) {
						days.remove(toRemove);
					}
				}

				ac.setDays(Lists.newArrayList(days));

				for (String day : days) {
					daysTable.put(day, baseTable);
					daysTrips.put(day, tripId);
					daysServices.put(day, serviceId);
				}

			}

			Map<String, CacheTable> hashTable = Maps.newTreeMap();

			SymbolicRouteDayInfoHashCalendar srdihs = new SymbolicRouteDayInfoHashCalendar();
			srdihs.setAgencyId(agencyId);
			agencySrdihs.add(srdihs);

			Map<String, String> srdihsCalendar = Maps.newTreeMap();
			Map<String, SymbolicRouteDayInfo> srdihsValues = Maps.newTreeMap();
			srdihs.setCalendar(srdihsCalendar);
			srdihs.setValues(srdihsValues);
			srdihs.setRouteId(rId);

			for (String day : daysTrips.keySet()) {
				List<String> dayTrips = Lists.newArrayList(daysTrips.get(day));
				List<String> dayServices = Lists.newArrayList(daysServices.get(day));
				String hash = getEqString(dayServices, agencyId);

				if (daysHashes.containsKey(day)) {
					continue;
				}

				daysHashes.put(day, hash);
				Set<String> dayServicesSet = daysServices.get(day);

				AnnotatedTimetable reducedTable = reduceAnnotatedTimetable(daysTable.get(day), dayServicesSet,
						agencyId);

				CacheTable ct = new CacheTable();

				List<String> tripIds = Lists.newArrayList();
				List<List<String>> compressedTimes = Lists.newArrayList();
				ct.setTimes(compressedTimes);

				for (String trip : dayTrips) {
					int index = trip.indexOf("_");
					tripIds.add(trip.substring(index + 1));
					compressedTimes.add(tripsColumns.get(trip).getTimes());
				}

				if (setTripsIds) {
					ct.setTripIds(tripIds);
				}

				List<String> routesIds = Lists.newArrayList();
				for (String ti : tripIds) {
					routesIds.add(tripsRoutes.get(ti));
				}

				ct.setStops(reducedTable.getStopNames());
				ct.setStopsId(reducedTable.getStopIds());
				ct.setInvisibles(reducedTable.getInvisibles());
				ct.setFrequency(reducedTable.getFrequency());
				ct.setLine(reducedTable.getLine());
				ct.setRoutesIds(routesIds);

				ct.setShortDescription(reducedTable.getShortDescription());
				ct.setLongDescription(reducedTable.getLongDescription());
				ct.setValidity(reducedTable.getValidity());
				ct.setSchedule(reducedTable.getSchedule());

				ct.compress();

				hashTable.put(hash, ct);

				if (!srdihsValues.containsKey(hash)) {
					SymbolicRouteDayInfo srdi = new SymbolicRouteDayInfo(reducedTable);
					StopNames sn = new StopNames();
					sn.setRouteId(rId);
					sn.setIds(reducedTable.getStopIds());
					sn.setNames(reducedTable.getStopNames());
					srdi.setStopNames(sn);
					srdi.setTripIds(tripIds);
					srdihsValues.put(hash, srdi);
				}
				srdihsCalendar.put(day, hash);
			}

			System.out.println("Writing " + rId);

			String calendar = agencyDir + System.getProperty("file.separator") + "calendar_" + rId + ".js";
			mapper.writeValue(new File(calendar), daysHashes);
			for (String hash : hashTable.keySet()) {
				String hashFile = agencyDir + System.getProperty("file.separator") + rId + "_" + hash + ".js";
				mapper.writeValue(new File(hashFile), hashTable.get(hash));

				CacheIndexEntry cie = new CacheIndexEntry();
				cie.setId(rId + "_" + hash);
				cie.setVersion(1);
				cie.setStatus(CacheEntryStatus.ADDED);
				aci.getEntries().put(rId + "_" + hash, cie);
			}

		}

		String indexFile = cacheDir + System.getProperty("file.separator") + agencyId + "_index.txt";
		File aciFile = new File(indexFile);
		if (aciFile.exists()) {
			AgencyCacheIndex oldAci = mapper.readValue(aciFile, AgencyCacheIndex.class);
			aci.setVersion(oldAci.getVersion() + 1);
		}
		mapper.writeValue(new File(indexFile), aci);

		String auxDir = cacheDir + System.getProperty("file.separator") + Constants.AUXILIARY_CACHE_DIR;
		String infoFile = auxDir + System.getProperty("file.separator") + agencyId + "_info.txt";
		mapper.writeValue(new File(infoFile), agencySrdihs);

		String symbolicFile = auxDir + System.getProperty("file.separator") + agencyId + "_symbolic_trips.txt";

		Map<String, Collection<String>> map = Maps.newTreeMap();
		for (String key : tripsSymbolicRouteId.keys()) {
			List<String> rids = Lists.newArrayList();
			rids.addAll(tripsSymbolicRouteId.get(key));
			map.put(key, rids);
		}

		mapper.writeValue(new File(symbolicFile), map);
	}

	private AnnotatedTimetable reduceAnnotatedTimetable(AnnotatedTimetable timetable, Set<String> serviceIds,
			String agencyId) {
		AnnotatedTimetable result = new AnnotatedTimetable();
		result.setStopIds(timetable.getStopIds());
		result.setStopNames(timetable.getStopNames());
		result.setShortDescription(timetable.getShortDescription());
		result.setLongDescription(timetable.getLongDescription());
		result.setValidity(timetable.getValidity());
		result.setSchedule(timetable.getSchedule());
		result.setInvisibles(timetable.getInvisibles());
		List<Integer> toKeep = Lists.newArrayList();
		List<AnnotatedColumn> columnsToKeep = Lists.newArrayList();
		for (AnnotatedColumn ac : timetable.getColumns()) {
			if (serviceIds.contains(agencyId + "_" + ac.getServiceId())) {
				columnsToKeep.add(ac);
				toKeep.add(timetable.getColumns().indexOf(ac));
			}
		}
		List<String> frequency = Lists.newArrayList();
		List<String> line = Lists.newArrayList();
		List<String> routedIds = Lists.newArrayList();
		for (int index : toKeep) {
			frequency.add(timetable.getFrequency().get(index));
			line.add(timetable.getLine().get(index));
			routedIds.add(timetable.getRouteIds().get(index));
		}
		result.setFrequency(frequency);
		result.setLine(line);
		result.setRouteIds(routedIds);

		return result;
	}

	private Multimap<String, ExtendedAnnotatedColumn> createExtendedAnnotatedColumns(
			List<AnnotatedTimetable> annotatedTimetables) {
		Multimap<String, ExtendedAnnotatedColumn> extendedAnnotatedColumns = ArrayListMultimap.create();

		for (AnnotatedTimetable at : annotatedTimetables) {
			for (AnnotatedColumn ac : at.getColumns()) {
				ExtendedAnnotatedColumn eac = new ExtendedAnnotatedColumn(ac);
				eac.setSource(at);
				extendedAnnotatedColumns.put(eac.getSymbolicRouteId(), eac);
			}
		}
		return extendedAnnotatedColumns;
	}

	private List<String[]> readCSV(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(new File(fileName));
		UnicodeReader ur = new UnicodeReader(fis, "UTF-8");

		List<String[]> lines = new ArrayList<String[]>();
		try {
			for (CSVRecord record : CSVFormat.DEFAULT.withDelimiter(';').parse(ur)) {
				String[] line = Iterables.toArray(record, String.class);
				lines.add(line);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		lines.get(0)[0] = lines.get(0)[0].replaceAll(Constants.UTF8_BOM, "");

		return lines;

	}

	private String getEqString(List<String> eqs, String agencyId) {
		List<String> sEqus = new ArrayList<String>(eqs);
		Collections.sort(sEqus);
		String eq = sEqus.toString();
		eq = eq.replaceAll(agencyId, "").replaceAll("[_ ]", "").replaceAll(",", ";").replaceAll("[\\[\\]]", "");
		return Hashing.sha1().hashString(eq, Charset.forName("UTF-8")).toString();
	}

	private int convertDayOfTheWeek(int day) {
		int conv = day - 2;
		if (conv < 0) {
			conv = 6;
		}
		return conv;
	}

	private String fixTimes(String s) {
		String r = s;
		for (int i = 0; i <= 5; i++) {
			r = r.replace("2" + (i + 4) + ":", "0" + i + ":");
		}
		return r;
	}

	public OTPHandler getHandler() {
		return handler;
	}

	public void setHandler(OTPHandler handler) {
		this.handler = handler;
	}

}
