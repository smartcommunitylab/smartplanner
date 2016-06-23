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

package it.sayservice.platform.smartplanner.cache.annotated;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public class CacheTable {

	private List<String> stopsId;
	private List<String> stops;
	private List<String> tripIds;
	private List<List<String>> times;
	private String compressedTimes;

	private List<Integer> invisibles;
	private List<String> line;
	private List<String> frequency;
	private List<String> routesIds;

	private String shortDescription;
	private String longDescription;
	private String validity;
	private String schedule;

	public List<String> getStopsId() {
		return stopsId;
	}

	public void setStopsId(List<String> stopsId) {
		this.stopsId = stopsId;
	}

	public List<String> getStops() {
		return stops;
	}

	public void setStops(List<String> stops) {
		this.stops = stops;
	}

	public List<String> getTripIds() {
		return tripIds;
	}

	public void setTripIds(List<String> tripIds) {
		this.tripIds = tripIds;
	}

	@JsonIgnore
	public List<List<String>> getTimes() {
		return times;
	}

	public void setTimes(List<List<String>> compressedTimes) {
		this.times = compressedTimes;
	}

	public String getCompressedTimes() {
		return compressedTimes;
	}

	public void setCompressedTimes(String compressedTimes) {
		this.compressedTimes = compressedTimes;
	}

	// @JsonIgnore
	public List<Integer> getInvisibles() {
		return invisibles;
	}

	public void setInvisibles(List<Integer> invisible) {
		this.invisibles = invisible;
	}

	// @JsonIgnore
	public List<String> getLine() {
		return line;
	}

	public void setLine(List<String> line) {
		this.line = line;
	}

	// @JsonIgnore
	public List<String> getFrequency() {
		return frequency;
	}

	public void setFrequency(List<String> frequency) {
		this.frequency = frequency;
	}

	public List<String> getRoutesIds() {
		return routesIds;
	}

	public void setRoutesIds(List<String> routesIds) {
		this.routesIds = routesIds;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public String getLongDescription() {
		return longDescription;
	}

	public void setLongDescription(String longDescription) {
		this.longDescription = longDescription;
	}

	public String getValidity() {
		return validity;
	}

	public void setValidity(String validity) {
		this.validity = validity;
	}

	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public void compress() {
		compressedTimes = "";
		for (List<String> trip : times) {
			for (String time : trip) {
				if (time.length() == 0) {
					compressedTimes += "|";
				} else {
					compressedTimes += time.replace(":", "");
				}
			}
		}

	}

}
