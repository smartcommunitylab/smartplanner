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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;

/**
 * Read areas from KML input stream
 * 
 * @author raman
 * 
 */
public class AreaKMLProcessor implements AreaProcessor {

	@Override
	public List<Area> read(InputStream is) {
		List<Area> res = new ArrayList<Area>();
		final Kml kml = Kml.unmarshal(is);
		final Document document = (Document) kml.getFeature();
		List<Feature> t = document.getFeature();
		for (Object o : t) {
			Folder f = (Folder) o;
			List<Feature> tg = f.getFeature();
			for (Object ftg : tg) {
				Area area = new Area();
				Placemark pm = (Placemark) ftg;

				String id = pm.getName();
				String name = pm.getName();

				area.setId(id);
				area.setName(name);
				List<double[]> positions = new ArrayList<double[]>();

				Polygon polygon = (Polygon) pm.getGeometry();
				List<Coordinate> coordinates = polygon.getOuterBoundaryIs().getLinearRing().getCoordinates();
				for (Coordinate c : coordinates) {
					double[] position = new double[] { c.getLatitude(), c.getLongitude() };
					positions.add(position);
				}
				area.setPositions(positions);
				res.add(area);
			}
		}
		return res;
	}

}
