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

public class AnnotatedTrip {

	private String agencyId;
	private String tripId;
	private String routeId;
	private String symbolicRouteId;
	private List<String> times;
	private int order;
	private int direction;

	public AnnotatedTrip() {
	}

	public AnnotatedTrip(String tripId, String routeId, String agencyId, String symbolicRouteId, List<String> times, int order, int direction) {
		super();
		this.tripId = tripId;
		this.routeId = routeId;
		this.agencyId = agencyId;
		this.symbolicRouteId = symbolicRouteId;
		this.times = times;
		this.order = order;
		this.direction = direction;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public String getSymbolicRouteId() {
		return symbolicRouteId;
	}

	public void setSymbolicRouteId(String symbolicRouteId) {
		this.symbolicRouteId = symbolicRouteId;
	}

	public List<String> getTimes() {
		return times;
	}

	public void setTimes(List<String> times) {
		this.times = times;
	}

	public int getOrder() {
		return order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public int getDirection() {
		return direction;
	}

	public void setDirection(int direction) {
		this.direction = direction;
	}

}
