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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.data.message.TType;

/**
 * class responsible for connection with OTP planner.
 * 
 * @author nawazk
 */
@Component
public class OTPConnector {

	@Autowired
	private ConfigurationManager configurationManager;
	private static final double WALK_SPEED = 0.8;
	private static final double BIKE_SPEED = 4.5; // 2.77;
	private static final double WALK_RELUCTANCE = 3;
	private static final String BATCH = "false";
	private static final int MIN_TRANSERTIME = 120;
	private Logger log;

	/**
	 * default constructor.
	 */
	public OTPConnector() {
		log = Logger.getLogger("");
		try {
			Handler fh = new FileHandler(System.getenv("OTP_HOME") + "/otp_url.log", true);
			Formatter sf = (new Formatter() {
				@Override
				public String format(LogRecord record) {
					return record.getMillis() + ": " + record.getMessage() + "\n";
				}
			});

			fh.setFormatter(sf);
			log.addHandler(fh);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * connect to OTP
	 * @param router
	 * @param params
	 * @return
	 * @throws IOException
	 */
	public String connect(String router, Map<String, String> params) throws IOException {

		String otpEndPoint = configurationManager.getRouter(router).getOtpEndpoint();
		boolean wheelChair = false;
		if (params.containsKey(Constants.WHEELCHAIR) && params.get(Constants.WHEELCHAIR).equalsIgnoreCase("true")) {
			wheelChair = true;
		}
		
		StringBuffer response = new StringBuffer();

		try {

			String urlParams = Constants.OTP_RQ_ITNS + "=" + params.get(Constants.OTP_RQ_ITNS) + "&"
					+ Constants.OTP_RQ_FROM + "=" + params.get(Constants.OTP_RQ_FROM) + "&" + Constants.OTP_RQ_TO + "="
					+ params.get(Constants.OTP_RQ_TO) + "&" + Constants.OTP_RQ_MODE + "="
					+ params.get(Constants.OTP_RQ_MODE) + "&" + Constants.OTP_RQ_MAXWALK + "="
					+ params.get(Constants.OTP_RQ_MAXWALK) + "&" + Constants.OTP_RQ_TIME + "="
					+ params.get(Constants.OTP_RQ_TIME) + "&" + Constants.OTP_RQ_DATE + "="
					+ params.get(Constants.OTP_RQ_DATE) + "&" + Constants.OTP_RQ_ARRIVEBY + "="
					+ params.get(Constants.OTP_RQ_ARRIVEBY) + "&" + Constants.BATCH + "=" + BATCH + "&"
					+ Constants.WALK_SPEED + "=" + WALK_SPEED + "&" + Constants.BIKE_SPEED + "=" + BIKE_SPEED + "&"
					+ Constants.MIN_TRANSFERTIME + "=" + MIN_TRANSERTIME + "&" + Constants.OTP_RQ_OPTIMIZE + "="
					+ params.get(Constants.OTP_RQ_OPTIMIZE)
					+ (Constants.OPTIMIZATION.TRIANGLE.toString().equals(params.get(Constants.OTP_RQ_OPTIMIZE))
							? "&" + Constants.OTP_RQ_T_SAFETY + "=" + params.get(Constants.OTP_RQ_T_SAFETY) + "&"
									+ Constants.OTP_RQ_T_SLOPE + "=" + params.get(Constants.OTP_RQ_T_SLOPE) + "&"
									+ Constants.OTP_RQ_T_TIME + "=" + params.get(Constants.OTP_RQ_T_TIME)
							: "")
					+ "&" + Constants.WALK_RELUCTANCE + "="
					+ (params.get(Constants.OTP_RQ_MODE).equals(TType.CAR.name()) ? 0:WALK_RELUCTANCE) + "&"
					+ Constants.WHEELCHAIR + "=" + wheelChair;

			URL url = new URL(otpEndPoint + router + Constants.API_PLAN + "?" + urlParams);

			// System.out.println("=========================================================================");
			// System.out.println("URL: " + url.getQuery());
			// System.out.println("=========================================================================");
			log.info(url.getQuery());

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			if (conn.getResponseCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));

			String output = null;
			while ((output = br.readLine()) != null) {
				response.append(output);
			}

			conn.disconnect();

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return response.toString();
	}

}
