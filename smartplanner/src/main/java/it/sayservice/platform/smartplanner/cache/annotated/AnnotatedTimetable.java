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

public class AnnotatedTimetable {

	private String symbolicRouteId;
	private String shortDescription;
	private String longDescription;
	private String validity;
	private String schedule;
	private List<AnnotatedColumn> columns;
	private List<String> stopNames;
	private List<String> stopIds;
	private List<Integer> invisibles;
	private List<String> line;
	private List<String> frequency;
	private List<String> routeIds;

	public String getSymbolicRouteId() {
		return symbolicRouteId;
	}

	public void setSymbolicRouteId(String symbolicRouteId) {
		this.symbolicRouteId = symbolicRouteId;
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

	public List<AnnotatedColumn> getColumns() {
		return columns;
	}

	public void setColumns(List<AnnotatedColumn> columns) {
		this.columns = columns;
	}

	public List<String> getStopNames() {
		return stopNames;
	}

	public void setStopNames(List<String> stopNames) {
		this.stopNames = stopNames;
	}

	public List<String> getStopIds() {
		return stopIds;
	}

	public void setStopIds(List<String> stopIds) {
		this.stopIds = stopIds;
	}

	public List<Integer> getInvisibles() {
		return invisibles;
	}

	public void setInvisibles(List<Integer> invisibles) {
		this.invisibles = invisibles;
	}

	public List<String> getLine() {
		return line;
	}

	public void setLine(List<String> line) {
		this.line = line;
	}

	public List<String> getFrequency() {
		return frequency;
	}

	public void setFrequency(List<String> frequency) {
		this.frequency = frequency;
	}

	public List<String> getRouteIds() {
		return routeIds;
	}

	public void setRouteIds(List<String> routeIds) {
		this.routeIds = routeIds;
	}

	@Override
	public String toString() {
		return shortDescription;
	}

}
