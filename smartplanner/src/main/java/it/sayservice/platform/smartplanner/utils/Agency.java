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

package it.sayservice.platform.smartplanner.utils;

import java.util.Map;

public class Agency {

	private String agencyId;
	private String type;
	private String region;
	private String api;
	private String tripTxt;
	private String stopTxt;
	private Map<String, String> specificProperties;
	private String filePath;
	private String costfilePath;
	private String datafilePath;
	private String pointsfilePath;
	private String contactfilePath;

	public String getAgencyId() {
		return agencyId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getTripTxt() {
		return tripTxt;
	}

	public void setTripTxt(String tripTxt) {
		this.tripTxt = tripTxt;
	}

	public String getStopTxt() {
		return stopTxt;
	}

	public void setStopTxt(String stopTxt) {
		this.stopTxt = stopTxt;
	}

	public Map<String, String> getSpecificProperties() {
		return specificProperties;
	}

	public void setSpecificProperties(Map<String, String> specificProperties) {
		this.specificProperties = specificProperties;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCostfilePath() {
		return costfilePath;
	}

	public void setCostfilePath(String costfilePath) {
		this.costfilePath = costfilePath;
	}

	public String getDatafilePath() {
		return datafilePath;
	}

	public void setDatafilePath(String datafilePath) {
		this.datafilePath = datafilePath;
	}

	public String getPointsfilePath() {
		return pointsfilePath;
	}

	public void setPointsfilePath(String pointsfilePath) {
		this.pointsfilePath = pointsfilePath;
	}

	public String getContactfilePath() {
		return contactfilePath;
	}

	public void setContactfilePath(String contactfilePath) {
		this.contactfilePath = contactfilePath;
	}

}
