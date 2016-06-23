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

package it.sayservice.platform.smartplanner.geocoder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.StopId;
import it.sayservice.platform.smartplanner.utils.Constants;
import it.sayservice.platform.smartplanner.utils.HTTPConnector;

/**
 * Gateway for external geocoder utility APIs.
 * 
 * @author nawazk
 */
@Component
public class GeocodeAPIsManager {

	@Autowired
	private ConfigurationManager configurationManager;

	/** logger. **/
	private Logger logger;

	/**
	 * default constructor.
	 */
	public GeocodeAPIsManager() {
		// logger.
		logger = Logger.getLogger(this.getClass().getName());
		try {
			Handler fh = new FileHandler(System.getenv("OTP_HOME") + Constants.LOG_PATH_GEOCODE_REQUEST, true);
			Formatter sf = (new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMillis() + ": " + record.getMessage() + "\n";
				}
			});

			fh.setFormatter(sf);
			logger.addHandler(fh);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Google api gateway.
	 * @param router
	 * @param request
	 * @param accept
	 * @param contentType
	 * @return
	 */
	public final Position nearbySearchGoogle(String router, final String request, final String accept, final String contentType) {
		/** definition. **/
		Position result = null;
		String address = configurationManager.getRouter(router).getGoogleAPINearBySearch();
		String key = configurationManager.getRouter(router).getGoogleAPIKey();

		try {

			/** data flow. **/
			// prepare request
			String req = request + "&key=" + key;
			String contType = contentType != null ? contentType : MediaType.APPLICATION_JSON;
			// invoke API
			String res = HTTPConnector.doGet(address, req, null, contType);
			// parse response and create response position.
			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Map<String, Object> map = mapper.readValue(res, Map.class);
			// check the validity of result.
			if (String.valueOf(map.get("status")).equalsIgnoreCase("OK")) {
				ArrayList list = (ArrayList) (map.get("results"));
				// first result.
				/*
				 * Map<String, Object> tmpMap = mapper.convertValue(list.get(0),
				 * Map.class); String id = tmpMap.get("id").toString(); String
				 * name = tmpMap.get("name").toString(); Map<String, Object>
				 * geoMap = mapper.convertValue( tmpMap.get("geometry"),
				 * Map.class); Map<String, Object> locMap = mapper.convertValue(
				 * geoMap.get("location"), Map.class); String lat =
				 * locMap.get("lat").toString(); String lon =
				 * locMap.get("lng").toString(); result = new Position(name, new
				 * StopId("", id), "route", lon, lat);
				 */
				NearByResult place = mapper.convertValue(list.get(0), NearByResult.class);
				result = new Position(place.getName(), new StopId("", place.getId()),
						place.getTypes().get(0).toString(), place.getGeometry().getLocation().getLng(),
						place.getGeometry().getLocation().getLat());
				logger.info("suggested location from API (" + result.getLat() + ", " + result.getLon() + ")");

			} else {
				logger.info("no suggested location from API for " + request);
			}
		} catch (IOException e1) {
			System.err.println("Error: " + e1);
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			System.err.println("Error: " + e2);
		}
		// return response
		return result;
	}

	/*
	 * public static void main(String[] args) { try { FileInputStream fXmlFile =
	 * new FileInputStream( new File (System.getenv("OTP_HOME") +
	 * System.getProperty("file.separator") + "cache" +
	 * System.getProperty("file.separator") + "smart-config" +
	 * System.getProperty("file.separator") +"config.properties")); Properties
	 * properties = new Properties(); // load the inputStream using the
	 * Properties properties.load(fXmlFile); GeocodeAPIsManager instance =
	 * GeocodeAPIsManager.getInstance(); instance.setConfigProps(properties);
	 * Position pos = instance.nearbySearchGoogle(
	 * "location=46.2887741,11.4516513&radius=50&sensor=false&types=route",
	 * null, MediaType.APPLICATION_JSON); } catch (Exception e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } }
	 */

}