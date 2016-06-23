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

package it.sayservice.platform.smartplanner.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

import it.sayservice.platform.smartplanner.data.message.StopId;

public class TaxiStation {

	@Id
	private String id;
	private StopId stationId;
	private String type;
	@Indexed
	private String fullName;
	@GeoSpatialIndexed
	private double[] location;
	private int taxis;
	private boolean monitored;
	private String address;

	public TaxiStation(String id, StopId stationId, String type, String fullName, double[] location, int taxis,
			boolean monitored, String addr) {
		super();
		this.id = id;
		this.stationId = stationId;
		this.type = type;
		this.fullName = fullName;
		this.location = location;
		this.taxis = taxis;
		this.monitored = monitored;
		this.address = addr;
	}

	public TaxiStation() {

	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public StopId getStationId() {
		return stationId;
	}

	public void setStationId(StopId stationId) {
		this.stationId = stationId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	public int getTaxis() {
		return taxis;
	}

	public void setTaxis(int taxis) {
		this.taxis = taxis;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}
