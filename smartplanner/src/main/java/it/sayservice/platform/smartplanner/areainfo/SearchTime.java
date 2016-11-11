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

public class SearchTime {

	public String searchAreaId;
	public SearchTimeWeekDayList weekDayList = new SearchTimeWeekDayList();

	public TimeAndRangeSlot compute(long timestamp) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(timestamp);
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);

		try {
			return weekDayList.get(day).getTimeSlot(calendar);
		} catch (Exception e) {
			return null;
		}
	}

	public SearchTimeWeekDayList getWeekDayList() {
		return weekDayList;
	}

	public void setWeekDayList(SearchTimeWeekDayList weekDayList) {
		this.weekDayList = weekDayList;
	}

	public String getSearchAreaId() {
		return searchAreaId;
	}

	public void setSearchAreaId(String searchAreaId) {
		this.searchAreaId = searchAreaId;
	}
}
