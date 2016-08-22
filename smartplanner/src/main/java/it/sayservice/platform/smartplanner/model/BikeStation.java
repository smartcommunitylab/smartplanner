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

import it.sayservice.platform.smartplanner.data.message.StopId;

import java.util.Arrays;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;
import org.springframework.data.mongodb.core.index.Indexed;

public class BikeStation {

	@Id
	private String id;
	private StopId stationId;
	private String type;
	@Indexed
	private String fullName;
	@GeoSpatialIndexed
	private double[] location;
	private int availableSharingVehicles;
	private int posts;
	private boolean monitored;
	
	private String stationName;

	/**
	 * @param stationId
	 * @param type
	 * @param agencyId
	 * @param location
	 * @param number
	 */
	public BikeStation(StopId stationId, String name, String fullName, String type, double x, double y,
			int noAvialableVehicles, int post) {
		super();
		this.id = stationId.getId() + "@" + stationId.getAgencyId();
		this.stationId = stationId;
		this.type = type;
		this.location = new double[] { x, y };
		this.availableSharingVehicles = noAvialableVehicles;
		this.posts = post;
		this.fullName = fullName;
	}

	public BikeStation() {
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public double[] getPosition() {
		return location;
	}

	public void setPosition(double[] position) {
		this.location = position;
	}

	public StopId getStationId() {
		return stationId;
	}

	public void setStationId(StopId stationId) {
		this.stationId = stationId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public int getAvailableSharingVehicles() {
		return availableSharingVehicles;
	}

	public void setAvailableSharingVehicles(int availableSharingVehicles) {
		this.availableSharingVehicles = availableSharingVehicles;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public boolean isMonitored() {
		return monitored;
	}

	public void setMonitored(boolean monitored) {
		this.monitored = monitored;
	}

	public String getStationName() {
		return stationName;
	}

	public void setStationName(String stationName) {
		this.stationName = stationName;
	}
	
	public String getBikeStationName() {
		if (stationName != null) {
			return stationName;
		} else if (stationId != null) {
			return stationId.getId();
		} else {
			return id;
		}
	}	

	@Override
	public String toString() {
		return "BikeStation [id=" + id + ", stationId=" + stationId + ", type=" + type + ", fullName=" + fullName
				+ ", position=" + Arrays.toString(location) + ", availableSharingVehicles=" + availableSharingVehicles
				+ ", posts=" + posts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BikeStation other = (BikeStation) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}
