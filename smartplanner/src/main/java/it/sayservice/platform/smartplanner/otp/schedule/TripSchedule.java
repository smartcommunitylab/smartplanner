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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TripSchedule implements Comparable<TripSchedule> {

	private String routeId;
	private String tripId;
	private List<Integer> days;
	private String[] times;
	private List<String> daysAdded;
	private List<String> daysRemoved;
	private String fromDate;
	private String toDate;
	private List<String> symbolicRouteIds;
	private String agencyId;
	private int order = -1;

	public TripSchedule() {
		daysAdded = new ArrayList<String>();
		daysRemoved = new ArrayList<String>();
		symbolicRouteIds = new ArrayList<String>();
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public List<Integer> getDays() {
		return days;
	}

	public void setDays(List<Integer> days) {
		this.days = days;
	}

	public String[] getTimes() {
		return times;
	}

	public void setTimes(String[] times) {
		this.times = times;
	}

	public List<String> getDaysAdded() {
		return daysAdded;
	}

	public void setDaysAdded(List<String> daysAdded) {
		this.daysAdded = daysAdded;
	}

	public List<String> getDaysRemoved() {
		return daysRemoved;
	}

	public void setDaysRemoved(List<String> daysRemoved) {
		this.daysRemoved = daysRemoved;
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

	public List<String> getSymbolicRouteIds() {
		return symbolicRouteIds;
	}

	public void setSymbolicRouteIds(List<String> symbolicRouteIds) {
		this.symbolicRouteIds = symbolicRouteIds;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public void fill() {
		for (int i = 0; i < times.length; i++) {
			if (times[i] == null) {
				times[i] = "";
			}
		}
	}

	public boolean check() {
		String prev = null;
		boolean err = false;
		for (int i = 0; i < times.length; i++) {
			// if (times[i].length() == 0) {
			// continue;
			// }
			if (prev != null && prev.length() > 0 && times[i].length() > 0) {
				boolean ordered = (times[i].compareTo(prev) >= 0);
				if (!ordered) {
					System.err.println(
							"Warning, unordered times: " + routeId + " " + tripId + " / " + times[i] + "," + prev);
					System.err.println(Arrays.asList(times));
					err = true;
				}
			}
			prev = times[i];
		}
		if (err) {
			System.out.println("Warning, unordered times: " + routeId);
			System.err.println(Arrays.asList(times));
		}
		return err;
	}

	public boolean checkComplete() {
		boolean err = false;
		for (int i = 0; i < times.length - 1; i++) {
			for (int j = i + 1; j < times.length; j++) {
				if (times[i].isEmpty() || times[j].isEmpty()) {
					continue;
				}
				if (times[i].compareTo(times[j]) > 0) {
					System.err.println(
							"Warning, unordered times: " + routeId + " " + tripId + " / " + times[i] + "," + times[j]);
					err = true;
				}
			}
		}

		return err;
	}

	@Override
	public String toString() {
		List<String> t = Arrays.asList(times);
		return "{" + routeId + "," + tripId + "," + t + " ~ " + days + "}";
	}

	@Override
	public int compareTo(TripSchedule o) {
		if (getOrder() == -1 && o.getOrder() == -1) {
			String o1 = "";
			String o2 = "";
			for (String s : times) {
				if (s.length() > 0) {
					o1 = s;
					break;
				}
			}
			for (String s : o.times) {
				if (s.length() > 0) {
					o2 = s;
					break;
				}
			}
			return o1.compareTo(o2);
		}

		return getOrder() - o.getOrder();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof TripSchedule) || obj == null) {
			return false;
		}
		return this.toString().equals(obj.toString());
	}

}
