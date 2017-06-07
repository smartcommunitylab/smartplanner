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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.data.mongodb.core.MongoTemplate;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.mongodb.Mongo;

import it.sayservice.platform.smartplanner.cache.CacheManager;
import it.sayservice.platform.smartplanner.cache.RoutesDBHelper;
import it.sayservice.platform.smartplanner.cache.annotated.AnnotatedReader;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.controllers.PlannerCtrl;
import it.sayservice.platform.smartplanner.otp.OTPHandler;
import it.sayservice.platform.smartplanner.otp.OTPManager;
import it.sayservice.platform.smartplanner.otp.OTPStorage;

public class CacheGenerator {

	private static OTPHandler handler;
	private static OTPStorage storage;
	private static MongoRouterMapper mongoRouterMapper;
	private static ConfigurationManager configurationManager;
	private static OTPManager manager;
	private static PlannerCtrl planner;
	private static String router = "trentino";

	public static void main(String args[]) throws Exception {
		// protected void setUp() throws Exception {

		// System.out.println(System.currentTimeMillis() - 1000 * 60 * 60 * 3);
		// System.out.println(System.currentTimeMillis() + 1000 * 60 * 60 * 3);
		// System.out.println(System.currentTimeMillis());
		// System.out.println(System.currentTimeMillis() + RecurrentUtil.DAY -
		// 1000 * 60);
		// System.exit(0);

		// super.setUp();
		handler = new OTPHandler(router, "http://127.0.0.1:7575");
		MongoTemplate template = new MongoTemplate(new Mongo(), router);
		storage = new OTPStorage(template);
		mongoRouterMapper = new MongoRouterMapper(template, router);

		configurationManager = new ConfigurationManager(router);

		manager = new OTPManager(handler, storage, mongoRouterMapper, configurationManager);
		manager.preinit(true);
		planner = new PlannerCtrl();
		manager.init(router);

	}

	public void test() throws Exception {
		CacheManager cb = new CacheManager(router, manager, handler);
		AnnotatedReader ar = new AnnotatedReader(router, handler);

		cb.updateCache(router, "5", true, false);
		cb.updateCache(router, "6", true, false);
		cb.updateCache(router, "10", true, false);

		// trento.
		// ar.generateCache(router, "12", true);
		// rovereto.
		ar.generateCache(router, "16", true);
		ar.generateCache(router, "17", true);
		// folgaria
		// cb.updateCache(router, "7", true, false);
		// cb.updateCache(router, "8", true, false);
		// ar.generateCache(router, "17", true);

		// trento.
		// ArrayList<String> agencyList = new ArrayList<String>() {{
		// add("5");
		// add("6");
		// add("10");
		// add("12");
		// }};
		//
		// RoutesDBHelper helper = new RoutesDBHelper(router, "trento", false);
		// helper.update(router, agencyList);
		// helper.optimize();
		// helper.zip(router);
		// helper = new RoutesDBHelper(router, "trento", true);
		// helper.update(router, agencyList);
		// helper.optimize();
		// helper.zip(router);

		// rovereto.
		ArrayList<String> agencyIds = new ArrayList<String>() {
			{
				add("5");
				add("6");
				add("10");
				add("16");
				add("17");
			}
		};
		RoutesDBHelper helper = new RoutesDBHelper(router, "rovereto", false);
		// helper.update("5", "6", "10", "16", "17");
		helper.update(router, agencyIds);
		helper.optimize();
		helper.zip(router);
		helper = new RoutesDBHelper(router, "rovereto", true);
		// helper.update("5", "6", "10", "16", "17");
		helper.update(router, agencyIds);
		helper.optimize();
		helper.zip(router);

		// folgaria.
		// ArrayList<String> agencyList = new ArrayList<String>() {{
		// add("5");
		// add("6");
		// add("7");
		// add("8");
		// add("10");
		// add("17");
		// }};
		//
		// RoutesDBHelper helper = new RoutesDBHelper(router, "alpecimbra",
		// false);
		// helper.update(router, agencyList);
		// helper.optimize();
		// helper.zip(router);
		// helper = new RoutesDBHelper(router, "alpecimbra", true);
		// helper.update(router, agencyList);
		// helper.optimize();
		// helper.zip(router);

	}

	public void _testGeneratedTripsSchedule() throws IOException {
		String[] gtfs = { "12", "16", "10", "5", "6", "17" };
		java.util.Map<String, String> agencyPrefixMap = new java.util.HashMap<String, String>();
		agencyPrefixMap.put("12", "tt");
		agencyPrefixMap.put("16", "rv");
		agencyPrefixMap.put("10", "tm");
		agencyPrefixMap.put("5", "bv");
		agencyPrefixMap.put("6", "tb");
		agencyPrefixMap.put("17", "ext");
		String pathToGTFSZip = "C:\\deleted\\daily-work\\07.05.15\\otp-0.15\\trentino\\";
		String schedulesDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + "cache"
				+ System.getProperty("file.separator") + "schedules" + System.getProperty("file.separator");
		String tripsDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + "cache"
				+ System.getProperty("file.separator") + "trips" + System.getProperty("file.separator");
		String stopsDir = System.getenv("OTP_HOME") + System.getProperty("file.separator") + "cache"
				+ System.getProperty("file.separator") + "stops" + System.getProperty("file.separator");
		File cacheSchedulesDir = new File(schedulesDir);
		cacheSchedulesDir.mkdir();
		File cacheTripsDir = new File(tripsDir);
		cacheTripsDir.mkdir();
		File cacheStopsDir = new File(stopsDir);
		cacheStopsDir.mkdir();

		for (String gtfsName : gtfs) {
			// unzip the new GTFS file
			File newFolder = unzip(pathToGTFSZip + gtfsName + ".zip");

			// read/remove headers.
			File trips = new File(newFolder, "trips.txt");
			List<String> linesT = Files.asCharSource(trips, Charsets.UTF_8).readLines();
			File calendar = new File(newFolder, "calendar.txt");
			List<String> linesC = Files.asCharSource(calendar, Charsets.UTF_8).readLines();
			File calendarDates = new File(newFolder, "calendar_dates.txt");
			List<String> linesCD = Files.asCharSource(calendarDates, Charsets.UTF_8).readLines();
			File stopTimes = new File(newFolder, "stop_times.txt");
			List<String> linesStopTimes = Files.asCharSource(stopTimes, Charsets.UTF_8).readLines();
			File stops = new File(newFolder, "stops.txt");
			List<String> linesStop = Files.asCharSource(stops, Charsets.UTF_8).readLines();
			// create dir.
			File scheduleAgenecyDir = new File(schedulesDir, gtfsName);
			scheduleAgenecyDir.mkdir();
			// copy files to cache/schedule folder.
			File calfile = new File(scheduleAgenecyDir, "calendar.txt");
			Files.asCharSink(calfile, Charsets.UTF_8).writeLines(linesC.subList(1, linesC.size()));
			File caldatesfile = new File(scheduleAgenecyDir, "calendar_dates.txt");
			Files.asCharSink(caldatesfile, Charsets.UTF_8).writeLines(linesCD.subList(1, linesCD.size()));
			File stoptimesfile = new File(scheduleAgenecyDir, "stop_times.txt");
			Files.asCharSink(stoptimesfile, Charsets.UTF_8)
					.writeLines(linesStopTimes.subList(1, linesStopTimes.size()));
			File tripfile = new File(scheduleAgenecyDir, "trips.txt");
			Files.asCharSink(tripfile, Charsets.UTF_8).writeLines(linesT.subList(1, linesT.size()));
			// copy files to cache/trip folder.
			File tripsTripfile = new File(cacheTripsDir, "trips_" + agencyPrefixMap.get(gtfsName) + ".txt");
			Files.asCharSink(tripsTripfile, Charsets.UTF_8).writeLines(linesT.subList(1, linesT.size()));
			// copy files to cache/stop folder.
			File stopsStopfile = new File(cacheStopsDir, "stops_" + agencyPrefixMap.get(gtfsName) + ".txt");
			Files.asCharSink(stopsStopfile, Charsets.UTF_8).writeLines(linesStop.subList(1, linesStop.size()));
		}

	}

	private File unzip(String pathToNewZip) throws IOException {
		File dir = Files.createTempDir();

		byte[] buffer = new byte[1024];
		// get the zip file content
		ZipInputStream zis = new ZipInputStream(new FileInputStream(pathToNewZip));
		// get the zipped file list entry
		ZipEntry ze = zis.getNextEntry();

		while (ze != null) {

			String fileName = ze.getName();
			File newFile = new File(dir, fileName);
			// create all non exists folders
			// else you will hit FileNotFoundException for compressed folder
			new File(newFile.getParent()).mkdirs();

			FileOutputStream fos = new FileOutputStream(newFile);
			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}
			fos.close();
			ze = zis.getNextEntry();
		}

		zis.closeEntry();
		zis.close();
		return dir;
	}
}
