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

import java.util.Map;

public class SymbolicRouteDayInfoHashCalendar {

	private String agencyId;
	private String routeId;
	private Map<String, String> calendar;
	private Map<String, SymbolicRouteDayInfo> values;

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public Map<String, String> getCalendar() {
		return calendar;
	}

	public void setCalendar(Map<String, String> calendar) {
		this.calendar = calendar;
	}

	public Map<String, SymbolicRouteDayInfo> getValues() {
		return values;
	}

	public void setValues(Map<String, SymbolicRouteDayInfo> values) {
		this.values = values;
	}

	@Override
	public String toString() {
		return agencyId + " / " + routeId;
	}

}
