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

package it.sayservice.platform.smartplanner.areainfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import it.sayservice.platform.smartplanner.model.AreaPoint;

public class AreaPointBuilder {

	public List<AreaPoint> generatePoints(List<Area> areas) {
		List<AreaPoint> points = new ArrayList<AreaPoint>();
		for (Area area : areas) {
			points.addAll(generatePoints(area));
		}
		return points;
	}

	private Collection<AreaPoint> generatePoints(Area area) {
		// TODO Auto-generated method stub
		return null;
	}
}
