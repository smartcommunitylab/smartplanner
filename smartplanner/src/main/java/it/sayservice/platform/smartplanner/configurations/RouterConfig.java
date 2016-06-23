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

package it.sayservice.platform.smartplanner.configurations;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import it.sayservice.platform.smartplanner.utils.Agency;

public class RouterConfig {

	private String otpEndpoint;
	private String router;
	private String googleAPINearBySearch;
	private String googleAPIKey;
	private Map<String, Agency> publicTransport;
	private Map<String, Agency> carParking;
	private Map<String, Agency> carSharing;
	private Map<String, Agency> bikeSharing;
	private Map<String, Agency> areaData;
	private static List<String> gtfsTrainAgencyIds = null;
	private static List<String> gtfsTrainRouteIds = null;
	private Map<String, Agency> taxi;

	private enum transportType {
		BUS {
			public String toString() {
				return "bus";
			}
		},
		TRAIN {
			public String toString() {
				return "train";
			}
		}
	};

	public RouterConfig() {
		super();
		// TODO Auto-generated constructor stub
	}

	public String getOtpEndpoint() {
		return otpEndpoint;
	}

	public void setOtpEndpoint(String otpEndpoint) {
		this.otpEndpoint = otpEndpoint;
	}

	public String getRouter() {
		return router;
	}

	public void setRouter(String router) {
		this.router = router;
	}

	public String getGoogleAPINearBySearch() {
		return googleAPINearBySearch;
	}

	public void setGoogleAPINearBySearch(String googleAPINearBySearch) {
		this.googleAPINearBySearch = googleAPINearBySearch;
	}

	public String getGoogleAPIKey() {
		return googleAPIKey;
	}

	public void setGoogleAPIKey(String googleAPIKey) {
		this.googleAPIKey = googleAPIKey;
	}

	public Map<String, Agency> getPublicTransport() {
		return publicTransport;
	}

	public void setPublicTransport(Map<String, Agency> publicTransport) {
		this.publicTransport = publicTransport;
	}

	public Map<String, Agency> getCarParking() {
		return carParking;
	}

	public void setCarParking(Map<String, Agency> carParking) {
		this.carParking = carParking;
	}

	public Map<String, Agency> getCarSharing() {
		return carSharing;
	}

	public void setCarSharing(Map<String, Agency> carSharing) {
		this.carSharing = carSharing;
	}

	public Map<String, Agency> getBikeSharing() {
		return bikeSharing;
	}

	public void setBikeSharing(Map<String, Agency> bikeSharing) {
		this.bikeSharing = bikeSharing;
	}

	public Map<String, Agency> getAreaData() {
		return areaData;
	}

	public void setAreaData(Map<String, Agency> areaData) {
		this.areaData = areaData;
	}

	public Map<String, Agency> getTaxi() {
		return taxi;
	}

	public void setTaxi(Map<String, Agency> taxi) {
		this.taxi = taxi;
	}

	public List<String> getGtfsTrainAgencyIds() {
		if (gtfsTrainAgencyIds == null) {
			gtfsTrainAgencyIds = getTrainAgencyIds();
		}
		return gtfsTrainAgencyIds;
	}

	public void setGtfsTrainAgencyIds(List<String> gtfsTrainAgencyIds) {
		RouterConfig.gtfsTrainAgencyIds = gtfsTrainAgencyIds;
	}

	public List<String> getGtfsTrainRouteIds() {
		if (gtfsTrainRouteIds == null) {
			gtfsTrainRouteIds = getTrainRouteIds();
		}
		return gtfsTrainRouteIds;
	}

	public static void setGtfsTrainRouteIds(List<String> gtfsTrainRouteIds) {
		RouterConfig.gtfsTrainRouteIds = gtfsTrainRouteIds;
	}

	/**
	 * Utility method to get list of all train agencies id.
	 * 
	 * @return List<String>.
	 */
	private List<String> getTrainAgencyIds() {

		List<String> trainAgencyIds = new ArrayList<String>();

		for (String agencyKey : this.getPublicTransport().keySet()) {
			Agency agency = this.getPublicTransport().get(agencyKey);
			if (agency.getType().equals(transportType.TRAIN.toString())) {
				trainAgencyIds.add(agency.getAgencyId());
			}
		}

		return trainAgencyIds;
	}

	/**
	 * Utility method to get all train routeIds.
	 * 
	 * @return String[]
	 */
	private List<String> getTrainRouteIds() {
		List<String> routeIds = new ArrayList<String>();

		for (String agencyKey : this.getPublicTransport().keySet()) {
			Agency agency = this.getPublicTransport().get(agencyKey);
			if (agency.getType().equals(transportType.TRAIN.toString())) {
				if (agency.getSpecificProperties().containsKey("routeIds")) {
					String[] ids = agency.getSpecificProperties().get("routeIds").split(",");
					for (String id : ids) {
						routeIds.add(id);
					}

				}
			}
		}

		return routeIds;
	}

}
