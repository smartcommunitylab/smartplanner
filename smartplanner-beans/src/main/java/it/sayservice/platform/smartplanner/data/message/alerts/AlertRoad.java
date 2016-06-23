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

package it.sayservice.platform.smartplanner.data.message.alerts;

import it.sayservice.platform.smartplanner.data.message.RoadElement;

public class AlertRoad extends Alert {
	private static final long serialVersionUID = -6928706057154015012L;

	/**
	 * agency managing the roads
	 */
	private String agencyId;
	/**
	 * Road affected
	 */
	private RoadElement road;
	
	/**
	 * Types of changes
	 */
	private AlertRoadType[] changeTypes;

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public RoadElement getRoad() {
		return road;
	}

	public void setRoad(RoadElement road) {
		this.road = road;
	}

	public AlertRoadType[] getChangeTypes() {
		return changeTypes;
	}

	public void setChangeTypes(AlertRoadType[] changeTypes) {
		this.changeTypes = changeTypes;
	}
}
