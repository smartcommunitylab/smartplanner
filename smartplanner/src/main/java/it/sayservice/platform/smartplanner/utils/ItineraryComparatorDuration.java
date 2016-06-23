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

import it.sayservice.platform.smartplanner.data.message.Itinerary;

/**
 * itinerary duration comparator.
 * 
 * @author nawazk
 * 
 */
public class ItineraryComparatorDuration implements Comparator<Itinerary> {
	@Override
	public final int compare(final Itinerary o1, final Itinerary o2) {
		// Long d1 = o1.getDuration();
		// Long d2 = o2.getDuration();
		Long d1 = o1.getEndtime();
		Long d2 = o2.getEndtime();
		return d1.compareTo(d2);
	}
}
