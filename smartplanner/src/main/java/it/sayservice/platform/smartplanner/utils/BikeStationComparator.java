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

import java.util.Comparator;

import it.sayservice.platform.smartplanner.model.BikeStation;

public class BikeStationComparator implements Comparator<BikeStation> {

	final double dx;
	final double dy;
	private ItineraryBuildHelper helper;

	public BikeStationComparator(ItineraryBuildHelper helper, final double dx, final double dy) {
		this.helper = helper;
		this.dx = dx;
		this.dy = dy;
	}

	@Override
	public int compare(BikeStation o1, BikeStation o2) {
		double distStationToTarget1 = helper.calculateHarvesineDistance(o1.getPosition()[0], o1.getPosition()[1], dx,
				dy);
		double distStationToTarget2 = helper.calculateHarvesineDistance(o2.getPosition()[0], o2.getPosition()[1], dx,
				dy);
		return (int) ((distStationToTarget1 - distStationToTarget2) * 1E6);
	}

}
