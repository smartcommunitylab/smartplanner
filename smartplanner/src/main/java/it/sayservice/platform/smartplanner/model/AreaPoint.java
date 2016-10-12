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

import it.sayservice.platform.smartplanner.areainfo.AreaData;
import it.sayservice.platform.smartplanner.areainfo.FaresZonePeriod;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

public class AreaPoint {

	@Id
	private String id;
	private String regionId;
	private String areaId;
	@GeoSpatialIndexed
	private double[] location;
	private String costZoneId;

	private AreaData data;
	private FaresZonePeriod[] faresZonePeriod;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public double[] getLocation() {
		return location;
	}

	public void setLocation(double[] location) {
		this.location = location;
	}

	public AreaData getData() {
		return data;
	}

	public void setData(AreaData data) {
		this.data = data;
	}

	public String getCostZoneId() {
		return costZoneId;
	}

	public void setCostZoneId(String costZoneId) {
		this.costZoneId = costZoneId;
	}

	public FaresZonePeriod[] getFaresZonePeriod() {
		return faresZonePeriod;
	}

	public void setFaresZonePeriod(FaresZonePeriod[] faresZonePeriods) {
		this.faresZonePeriod = faresZonePeriods;
	}
}
