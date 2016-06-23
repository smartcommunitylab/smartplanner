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

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;
import it.sayservice.platform.smartplanner.model.BikeStation;
import it.sayservice.platform.smartplanner.model.bikerental.BikeStationStatus;
import it.sayservice.platform.smartplanner.model.bikerental.CityBikesBikeStation;
import it.sayservice.platform.smartplanner.model.bikerental.CoordsPosition;
import it.sayservice.platform.smartplanner.model.bikerental.JCDecauxBikeStation;

public class ProprietaryToOpenBikeStationAdapter {

	
	public static JCDecauxBikeStation getJCDecauxFromBikestation(BikeStation bikeStation) {
		JCDecauxBikeStation jCDecauxBikeStation = new JCDecauxBikeStation();
		
		jCDecauxBikeStation.setAddress(bikeStation.getFullName());
		
		if(bikeStation.getStationId()!=null){
			jCDecauxBikeStation.setName(bikeStation.getStationId().getId());
		}
		
		CoordsPosition position = new CoordsPosition();
		position.setLat(bikeStation.getPosition()[0]);
		position.setLng(bikeStation.getPosition()[1]);
		jCDecauxBikeStation.setPosition(position);
		
		jCDecauxBikeStation.setAvailable_bikes(bikeStation.getAvailableSharingVehicles());
		jCDecauxBikeStation.setBike_stands(bikeStation.getPosts());
		jCDecauxBikeStation.setNumber(bikeStation.getStationId().getAgencyId());
		jCDecauxBikeStation.setStatus(BikeStationStatus.OPEN);
		
		return jCDecauxBikeStation;
	}

	public static JCDecauxBikeStation getJCDecauxFromParking(Parking parking, BikeStation bikeStation) {
		JCDecauxBikeStation jCDecauxBikeStation = new JCDecauxBikeStation();
		
		jCDecauxBikeStation.setAddress(bikeStation.getFullName());
		
		if(bikeStation.getStationId()!=null){
			jCDecauxBikeStation.setName(parking.getName());
		}
		
		CoordsPosition position = new CoordsPosition();
		position.setLat(parking.getPosition()[0]);
		position.setLng(parking.getPosition()[1]);
		jCDecauxBikeStation.setPosition(position);
		
		jCDecauxBikeStation.setAvailable_bike_stands(parking.getSlotsAvailable());
		jCDecauxBikeStation.setBike_stands(parking.getSlotsTotal());
		jCDecauxBikeStation.setNumber(bikeStation.getStationId().getAgencyId());
		jCDecauxBikeStation.setStatus(BikeStationStatus.OPEN);
		
		return jCDecauxBikeStation;
	}
	
	public static CityBikesBikeStation getCityBikesFromBikestation(BikeStation bikeStation) {
		CityBikesBikeStation cityBikesBikeStation = new CityBikesBikeStation();
		
		if(bikeStation.getStationId()!=null){
			cityBikesBikeStation.setName(bikeStation.getStationId().getId());
			cityBikesBikeStation.setId(bikeStation.getStationId().getAgencyId());
			cityBikesBikeStation.setIdx(bikeStation.getStationId().getAgencyId());
		}
		
		cityBikesBikeStation.setLat(bikeStation.getPosition()[0]);
		cityBikesBikeStation.setLng(bikeStation.getPosition()[1]);

		cityBikesBikeStation.setFree(bikeStation.getAvailableSharingVehicles());

		cityBikesBikeStation.setBikes(bikeStation.getPosts());
		
		
		return cityBikesBikeStation;
	}

	public static CityBikesBikeStation getCityBikesFromParking(Parking parking, BikeStation bikeStation) {
		CityBikesBikeStation cityBikesBikeStation = new CityBikesBikeStation();

		cityBikesBikeStation.setName(parking.getName());

		if(bikeStation.getStationId()!=null){
			cityBikesBikeStation.setId(bikeStation.getStationId().getAgencyId());
			cityBikesBikeStation.setIdx(bikeStation.getStationId().getAgencyId());
		}

		cityBikesBikeStation.setLat(parking.getPosition()[0]);
		cityBikesBikeStation.setLng(parking.getPosition()[1]);

		cityBikesBikeStation.setFree(parking.getSlotsAvailable());

		int bikes = parking.getSlotsTotal() - parking.getSlotsAvailable();
		cityBikesBikeStation.setBikes(bikes);
		
		return cityBikesBikeStation;
	}
	
}
