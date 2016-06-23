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

package it.sayservice.platform.smartplanner.core.adapter;

import it.sayservice.platform.smartplanner.data.message.StopId;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.bikerental.CityBikesBikeStation;
import it.sayservice.platform.smartplanner.model.bikerental.JCDecauxBikeStation;

public class OpenToProprietaryBikeStationAdapter {

	public static BikeStation getBikeStationFromJCDecaux(JCDecauxBikeStation jCDecauxBikeStation) {
		BikeStation bikeStation = new BikeStation();
		
		if(jCDecauxBikeStation.getAddress()!=null){
			bikeStation.setFullName(jCDecauxBikeStation.getAddress());
		}
		
		if(jCDecauxBikeStation.getPosition()!=null&&
			 jCDecauxBikeStation.getPosition().getLat()>0&&
			 jCDecauxBikeStation.getPosition().getLng()>0){
			double[] position = {jCDecauxBikeStation.getPosition().getLat(),jCDecauxBikeStation.getPosition().getLng()};
			bikeStation.setPosition(position);
		}
		
		bikeStation.setAvailableSharingVehicles(jCDecauxBikeStation.getAvailable_bikes());
		
		bikeStation.setPosts(jCDecauxBikeStation.getBike_stands());

		StopId stopId = new StopId();
		stopId.setAgencyId(String.valueOf(jCDecauxBikeStation.getNumber()));
		stopId.setId(jCDecauxBikeStation.getName());
		bikeStation.setStationId(stopId);
		
		bikeStation.setMonitored(true);
		bikeStation.setType("BIKE-RENTAL");

		return bikeStation;
	}

	public static BikeStation getBikeStationFromCityBikes(CityBikesBikeStation cityBikesBikeStation) {
		BikeStation bikeStation = new BikeStation();
		
		if(cityBikesBikeStation.getName()!=null){
			bikeStation.setFullName(cityBikesBikeStation.getName());
		}
		
		double[] position = {cityBikesBikeStation.getLat(),cityBikesBikeStation.getLng()};
		bikeStation.setPosition(position);
		
		bikeStation.setAvailableSharingVehicles(cityBikesBikeStation.getFree());
		
		bikeStation.setPosts(cityBikesBikeStation.getBikes());

		StopId stopId = new StopId();
		stopId.setAgencyId(String.valueOf(cityBikesBikeStation.getId()));
		stopId.setId(String.valueOf(cityBikesBikeStation.getName()));
		bikeStation.setStationId(stopId);
		
		bikeStation.setMonitored(true);
		
		return bikeStation;
	}

}
