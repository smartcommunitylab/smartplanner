/**
 *    Copyright 2011-2016 SAYservice s.r.l.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package it.sayservice.platform.smartplanner.data.message.otpbeans;

import java.util.Iterator;
import java.util.List;

public class CompressedTransitTimeTable {
	private List<String> stops;

	private List<String> stopsId;

	private List<String> tripIds;

	private String compressedTimes;
	
	private List<String> routesIds;

	public CompressedTransitTimeTable() {
		
	}
	
	public CompressedTransitTimeTable(TransitTimeTable tt) {
		this.stops = tt.getStops();
		this.stopsId = tt.getStopsId();
		if (tt.getTripIds() != null && tt.getTripIds().size() >= 1) {
			this.tripIds = tt.getTripIds().get(0);
		}
		List<List<List<String>>> times = tt.getTimes();
		compressedTimes = "";
		Iterator<List<List<String>>> it = times.iterator();
		while (it.hasNext()) {
			List<List<String>> day = it.next();
			String compressedDayTimes = "";
			for (List<String> trip : day) {
				for (String time : trip) {
					if (time.length() == 0) {
						compressedDayTimes += "|";
					} else {
						compressedDayTimes += time.replace(":", "");
					}
				}
			}
			compressedTimes += compressedDayTimes + (it.hasNext() ? "#" : "");
		}

	}

	public List<String> getStops() {
		return stops;
	}

	public void setStops(List<String> stops) {
		this.stops = stops;
	}

	public List<String> getStopsId() {
		return stopsId;
	}

	public void setStopsId(List<String> stopsId) {
		this.stopsId = stopsId;
	}

	public String getCompressedTimes() {
		return compressedTimes;
	}

	public void setCompressedTimes(String compressedTimes) {
		this.compressedTimes = compressedTimes;
	}

	public List<String> getTripIds() {
		return tripIds;
	}

	public void setTripIds(List<String> tripIds) {
		this.tripIds = tripIds;
	}

	public List<String> getRoutesIds() {
		return routesIds;
	}

	public void setRoutesIds(List<String> routeIds) {
		this.routesIds = routeIds;
	}

	@Override
	public String toString() {
		return stops + ", " + stopsId + ", " + tripIds + ", " + compressedTimes;
	}

}
