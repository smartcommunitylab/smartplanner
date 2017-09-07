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

package it.sayservice.platform.smartplanner.data.message;

/**
 * Transport type enum
 */
public enum TType {

//	CAR,BICYCLE,TRANSIT,SHAREDBIKE,SHAREDBIKE_WITHOUT_STATION,
//	CARWITHPARKING,SHAREDCAR,SHAREDCAR_WITHOUT_STATION,BUS,TRAIN,WALK;
	

	CAR(0,11),BICYCLE(16,0),TRANSIT(0,2),SHAREDBIKE(16,0),SHAREDBIKE_WITHOUT_STATION(16,0),GONDOLA(0,2),
	CARWITHPARKING(0,11),SHAREDCAR(0,4),SHAREDCAR_WITHOUT_STATION(0,4),BUS(0,2),TRAIN(0,2),WALK(12,0),SHUTTLE(0,2),
	PARK_AND_RIDE(0,4),TRAM(0,2),LIGHT_RAIL(0,2),STREETCAR(0,11),SUBWAY(0,2),METRO(0,2),RAIL(0,2),FERRY(0,2),CABLE_CAR(0,2),
	SUSPENDED_CABLE_CAR(0,2), FUNICULAR(0,2), PARKWALK(12,0);
	
	private int health;
	private int green;
	
	private TType(int health, int green) {
		this.health = health;
		this.green = green;
	}
	
	public static TType getMode(String mode) {
		
		if (mode.equalsIgnoreCase("CAR"))
			return TType.CAR;
		else if (mode.equalsIgnoreCase("BICYCLE"))
			return TType.BICYCLE;
		else if (mode.equalsIgnoreCase("TRANSIT"))
			return TType.TRANSIT;
		else if (mode.equalsIgnoreCase("SHAREDBIKE"))
			return TType.SHAREDBIKE;
		else if (mode.equalsIgnoreCase("SHAREDBIKE_WITHOUT_STATION"))
			return TType.SHAREDBIKE_WITHOUT_STATION;
		else if (mode.equalsIgnoreCase("CARWITHPARKING"))
			return TType.CARWITHPARKING;
		else if (mode.equalsIgnoreCase("SHAREDCAR"))
			return TType.SHAREDCAR;
		else if (mode.equalsIgnoreCase("SHAREDCAR_WITHOUT_STATION"))
			return TType.SHAREDCAR_WITHOUT_STATION;
		else if (mode.equalsIgnoreCase("BUS"))
			return TType.BUS;
		else if (mode.equalsIgnoreCase("TRAIN") || mode.equalsIgnoreCase("RAIL"))
			return TType.TRAIN;
		else if (mode.equalsIgnoreCase("WALK"))
			return TType.WALK;
		else if (mode.equalsIgnoreCase("GONDOLA") || mode.equalsIgnoreCase("CABLE_CAR") || mode.equalsIgnoreCase("FUNICULAR")
						 ||mode.equalsIgnoreCase("TRAM") || mode.equalsIgnoreCase("LIGHT_RAIL")|| mode.equalsIgnoreCase("STREETCAR")
						 ||mode.equalsIgnoreCase("SUBWAY") || mode.equalsIgnoreCase("METRO")|| mode.equalsIgnoreCase("RAIL")
						 ||mode.equalsIgnoreCase("FERRY") ||mode.equalsIgnoreCase("CABLE_CAR") ||mode.equalsIgnoreCase("SUSPENDED_CABLE_CAR")
						 ||mode.equalsIgnoreCase("FUNICULAR"))
			return TType.TRANSIT;
		else if (mode.equalsIgnoreCase("SHUTTLE"))
			return TType.SHUTTLE;
		else if (mode.equalsIgnoreCase("PARK_AND_RIDE"))
			return TType.PARK_AND_RIDE;	
		else if (mode.equalsIgnoreCase("PARKWALK"))
			return TType.PARKWALK;				
		return TType.TRANSIT;
		}

	public int getHealth() {
		return health;
	}

	public void setHealth(int health) {
		this.health = health;
	}

	public int getGreen() {
		return green;
	}

	public void setGreen(int green) {
		this.green = green;
	}
	
	
	
}

