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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.Iterables;
import com.google.gdata.util.io.base.UnicodeReader;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.QueryBuilder;

import it.sayservice.platform.smartplanner.model.Stop;

/**
 * Utility class to generate trip legs. used by perl script to generate shape
 * files.
 * 
 * @author nawazk
 * 
 */
public class LegGenerator {

	public static final String UTF8_BOM = "\uFEFF";

	public static void main(String[] args) throws IOException {

		Mongo m = new Mongo("localhost"); // default port 27017
		DB db = m.getDB("smart-planner-15x");
		DBCollection coll = db.getCollection("stops");

		// read trips.txt(trips,serviceId).
		List<String[]> trips = readFileGetLines("src/main/resources/schedules/17/trips.txt");
		List<String[]> stopTimes = readFileGetLines("src/main/resources/schedules/17/stop_times.txt");
		for (String[] words : trips) {
			try {
				String routeId = words[0].trim();
				String serviceId = words[1].trim();
				String tripId = words[2].trim();
				// fetch schedule for trips.
				for (int i = 0; i < stopTimes.size(); i++) {
					// already ordered by occurence.
					String[] scheduleLeg = stopTimes.get(i);
					if (scheduleLeg[0].equalsIgnoreCase(tripId)) {
						// check if next leg belongs to same trip
						if (stopTimes.get(i + 1)[0].equalsIgnoreCase(tripId)) {

							String arrivalT = scheduleLeg[1];
							String departT = scheduleLeg[2];
							String sourceId = scheduleLeg[3];
							String destId = stopTimes.get(i + 1)[3];
							// get coordinates of stops.
							/**
							 * make sure that mongo stop collection is
							 * populated. if, not, invoke
							 * http://localhost:7070/smart
							 * -planner/rest/getTransitTimes
							 * /TB_R2_R/1366776000000/1366819200000
							 */
							Stop source = (Stop) getObjectByField(db, "id", sourceId, coll, Stop.class);
							Stop destination = (Stop) getObjectByField(db, "id", destId, coll, Stop.class);
							// System.out.println(tripId + ","
							// + routeId + ","
							// + source.getId() + ","
							// + source.getLatitude() + ","
							// + source.getLongitude() + ","
							// + arrivalT + ","
							// + destination.getId() + ","
							// + destination.getLatitude() + ","
							// + destination.getLongitude() + ","
							// + departT + ","
							// + serviceId
							// );
							String content = tripId + "," + routeId + "," + source.getStopId() + ","
									+ source.getLatitude() + "," + source.getLongitude() + "," + arrivalT + ","
									+ destination.getStopId() + "," + destination.getLatitude() + ","
									+ destination.getLongitude() + "," + departT + "," + "Giornaliero" + "\n";

							File file = new File("src/main/resources/legs/legs.txt");
							// single leg file
							if (!file.exists()) {
								file.createNewFile();
							}

							FileWriter fw = new FileWriter(file.getAbsoluteFile(), true);
							BufferedWriter bw = new BufferedWriter(fw);
							bw.write(content);
							bw.close();
							// individual trip leg file.
							File fileT = new File("src/main/resources/legs/legs_" + routeId + ".txt");
							FileWriter fwT = new FileWriter(fileT.getAbsoluteFile(), true);
							BufferedWriter bwT = new BufferedWriter(fwT);
							bwT.write(content);
							bwT.close();

						}
					}
				}

			} catch (Exception e) {
				System.out.println("Error parsing trip: " + words[0] + "," + words[1] + "," + words[2]);
			}
		}
		System.out.println("Done");
	}

	private static List<String[]> readFileGetLines(String fileName) throws IOException {
		FileInputStream fis = new FileInputStream(new File(fileName));
		UnicodeReader ur = new UnicodeReader(fis, "UTF-8");

		List<String[]> lines = new ArrayList<String[]>();
		for (CSVRecord record : CSVFormat.DEFAULT.parse(ur)) {
			String[] line = Iterables.toArray(record, String.class);
			lines.add(line);
		}
		lines.get(0)[0] = lines.get(0)[0].replaceAll(UTF8_BOM, "");
		return lines;
	}

	public static Object getObjectByField(DB db, String key, String value, DBCollection collection,
			Class destinationClass) {
		Object result = null;

		QueryBuilder qb = QueryBuilder.start(key).is(value);

		BasicDBObject dbObject = (BasicDBObject) collection.findOne(qb.get());

		if (dbObject != null) {
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			result = mapper.convertValue(dbObject, destinationClass);
		}

		return result;
	}

}
