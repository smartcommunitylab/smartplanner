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

package it.sayservice.platform.smartplanner.otp.schedule;

import java.util.Arrays;

public class WeekdayFilter {

	private String name;
	private boolean[] days;
	private String fromDate;
	private String toDate;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean[] getDays() {
		return days;
	}

	public void setDays(boolean[] days) {
		this.days = days;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String startDate) {
		this.fromDate = startDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String endDate) {
		this.toDate = endDate;
	}

	@Override
	public String toString() {
		return Arrays.toString(days);
	}

}
