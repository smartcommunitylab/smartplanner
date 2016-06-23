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

package it.sayservice.platform.smartplanner.areainfo;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SearchTime {

	private Map<Integer, SearchTimeSlot[]> dayMap = new HashMap<Integer, SearchTimeSlot[]>();

	public SearchTimeSlot compute(long timestamp) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(timestamp);
		int day = c.get(Calendar.DAY_OF_WEEK);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		try {
			return dayMap.get(day)[hour];
		} catch (Exception e) {
			return null;
		}
	}

	public Map<Integer, SearchTimeSlot[]> getDayMap() {
		return dayMap;
	}

	public void setDayMap(Map<Integer, SearchTimeSlot[]> dayMap) {
		this.dayMap = dayMap;
	}

	public static class SearchTimeSlot {
		private int min;
		private int max;

		public SearchTimeSlot() {
			super();
		}

		public SearchTimeSlot(int min, int max) {
			super();
			this.min = min;
			this.max = max;
		}

		public int getMin() {
			return min;
		}

		public void setMin(int min) {
			this.min = min;
		}

		public int getMax() {
			return max;
		}

		public void setMax(int max) {
			this.max = max;
		}

		@Override
		public String toString() {
			return min + "," + max;
		}

	}
}
