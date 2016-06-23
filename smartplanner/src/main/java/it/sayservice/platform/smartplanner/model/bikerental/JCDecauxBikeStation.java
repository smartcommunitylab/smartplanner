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

package it.sayservice.platform.smartplanner.model.bikerental;

/*
 *     "number" : 94, 
 *     "name" : "00094-PETIT PORT", 
 *     "address" : "PETIT PORT - BD DU PETIT PORT", 
 *     "position" : { 
 *       "lat" : 47.243263914975486, 
 *       "lng" : -1.556344610167984 }, 
 *     "banking" : true, 
 *     "bonus" : false, 
 *     "status" : "OPEN", 
 *     "contract_name": "Paris",
 *     "bike_stands" : 20, 
 *     "available_bike_stands" : 1, 
 *     "available_bikes" : 19, 
 *     "last_update" : 1368611914000 
 * */
public class JCDecauxBikeStation {

	String number;
	String name;
	String address;
	CoordsPosition position;
	String banking;
	boolean bonus;
	BikeStationStatus status;
	String contract_name;
	int bike_stands;
	int available_bike_stands;
	int available_bikes;
	long last_update;

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public CoordsPosition getPosition() {
		return position;
	}

	public void setPosition(CoordsPosition position) {
		this.position = position;
	}

	public String getBanking() {
		return banking;
	}

	public void setBanking(String banking) {
		this.banking = banking;
	}

	public boolean isBonus() {
		return bonus;
	}

	public void setBonus(boolean bonus) {
		this.bonus = bonus;
	}

	public BikeStationStatus getStatus() {
		return status;
	}

	public void setStatus(BikeStationStatus status) {
		this.status = status;
	}

	public int getBike_stands() {
		return bike_stands;
	}

	public void setBike_stands(int bike_stands) {
		this.bike_stands = bike_stands;
	}

	public int getAvailable_bike_stands() {
		return available_bike_stands;
	}

	public void setAvailable_bike_stands(int available_bike_stands) {
		this.available_bike_stands = available_bike_stands;
	}

	public int getAvailable_bikes() {
		return available_bikes;
	}

	public void setAvailable_bikes(int available_bikes) {
		this.available_bikes = available_bikes;
	}

	public long getLast_update() {
		return last_update;
	}

	public void setLast_update(long last_update) {
		this.last_update = last_update;
	}

	public String getContract_name() {
		return contract_name;
	}

	public void setContract_name(String contract_name) {
		this.contract_name = contract_name;
	}
}
