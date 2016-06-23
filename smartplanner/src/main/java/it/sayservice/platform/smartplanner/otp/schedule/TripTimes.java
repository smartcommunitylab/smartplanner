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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

public class TripTimes implements Comparable<TripTimes> {

	private String tripId;
	private SortedSet<TripTimeEntry> tripTimes;
	private List<Integer> days;
	private String recurrence;
	private WeekdayException exceptions;
	private String fromDate;
	private String toDate;

	public TripTimes() {
		tripTimes = new TreeSet<TripTimeEntry>();
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public SortedSet<TripTimeEntry> getTripTimes() {
		return tripTimes;
	}

	public void setTripTimes(SortedSet<TripTimeEntry> times) {
		this.tripTimes = times;
	}

	public List<Integer> getDays() {
		return days;
	}

	public void setDays(List<Integer> days) {
		this.days = days;
	}

	public String getRecurrence() {
		return recurrence;
	}

	public void setRecurrence(String recurrence) {
		this.recurrence = recurrence;
	}

	public WeekdayException getExceptions() {
		return exceptions;
	}

	public void setExceptions(WeekdayException exceptions) {
		this.exceptions = exceptions;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	@Override
	public String toString() {
		return tripId + " => " + tripTimes.toString() + " ~ " + days;
	}

	@Override
	public int compareTo(TripTimes o) {
		return tripTimes.first().getTime().compareTo(o.getTripTimes().first().getTime());
	}

}
