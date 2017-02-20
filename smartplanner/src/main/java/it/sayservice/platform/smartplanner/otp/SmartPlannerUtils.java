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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SmartPlannerUtils {

	public static SimpleDateFormat otpDateFormatter = new SimpleDateFormat("yyyyMMdd");
	public static Calendar calendar = Calendar.getInstance();

	public static String getDate(Long date) {
		String result = otpDateFormatter.format(date);
		return result;
	}

	public static void main(String args[]) {
		System.out.println(SmartPlannerUtils.getDate(System.currentTimeMillis()));
	}

	public static long addSecondsToTimeStamp(Long timestamp, int seconds) throws ParseException {
		Date date = otpDateFormatter.parse(otpDateFormatter.format(timestamp));
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, seconds);
		long time = calendar.getTimeInMillis();
		if (time < timestamp) {
			calendar.add(Calendar.DAY_OF_YEAR, 1);
		}
		return calendar.getTimeInMillis();
	}
	
	public static long computeDate(int seconds, Long serviceDay) throws ParseException {
		Date date = otpDateFormatter.parse(otpDateFormatter.format(serviceDay));
		calendar.setTime(date);
		calendar.add(Calendar.SECOND, seconds);
		long time = calendar.getTimeInMillis();
		return calendar.getTimeInMillis();
	}

	public static boolean isNonNegativeDouble(String price) {
		double value;
		boolean valid = false;
		try {
			value = Double.parseDouble(price);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		if (value > 0) {
			valid = true;
		}
		return valid;
	}

	/**
	 * Utility method for checking integer
	 * 
	 * @param s
	 * @return
	 */
	public static boolean isInteger(String s) {
		try {
			Integer.parseInt(s);
		} catch (NumberFormatException e) {
			return false;
		}
		// only got here if we didn't return false
		return true;
	}

}