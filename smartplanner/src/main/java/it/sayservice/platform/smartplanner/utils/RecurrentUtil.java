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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class RecurrentUtil {

	public static final long DAY = 24 * 1000 * 60 * 60;

	public static List<String> computeParameters(long startTime, long endTime, String recurrence) {
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
		List<Integer> daysList = new ArrayList<Integer>();
		String ds[] = recurrence.split(",");
		for (String d : ds) {
			daysList.add(Integer.parseInt(d.trim()));
		}

		boolean weekday = false;
		List<String> result = new ArrayList<String>();
		long currentTime = startTime;
		while (currentTime <= endTime) {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(currentTime);
			int dow = cal.get(Calendar.DAY_OF_WEEK);
			if (daysList.contains(dow)) {
				if (weekday == false) {
					String d = df.format(new Date(currentTime));
					result.add(d);
					weekday = true;
				}
			}
			
			cal.setTimeInMillis(currentTime);
			cal.add(Calendar.DAY_OF_YEAR, 1);
			currentTime = cal.getTimeInMillis();
		}

		return result;
	}

}
