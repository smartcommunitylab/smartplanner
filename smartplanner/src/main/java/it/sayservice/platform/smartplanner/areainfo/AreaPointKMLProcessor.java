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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.MethodNotSupportedException;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.ExtendedData;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import de.micromata.opengis.kml.v_2_2_0.SimpleData;
import it.sayservice.platform.smartplanner.model.AreaPoint;

/**
 * Extract from KML input stream the {@link AreaPoint} objects and associates
 * them to the region.
 * 
 * @author raman
 * 
 */
public class AreaPointKMLProcessor implements AreaPointProcessor {

	@Override
	public List<AreaPoint> read(InputStream is) throws MethodNotSupportedException {
		throw new MethodNotSupportedException();
	}

	@Override
	public List<AreaPoint> read(InputStream is, AreaPointIdentityMapper mapper) {
		List<AreaPoint> res = new ArrayList<AreaPoint>();
		final Kml kml = Kml.unmarshal(is);
		final Document document = (Document) kml.getFeature();
		List<Feature> t = document.getFeature();
		for (Object o : t) {
			Folder f = (Folder) o;
			List<Feature> tg = f.getFeature();
			for (Object ftg : tg) {
				AreaPoint point = new AreaPoint();
				Placemark pm = (Placemark) ftg;

				ExtendedData ext = pm.getExtendedData();
				Map<String, Object> data = new HashMap<String, Object>();
				for (SimpleData d : ext.getSchemaData().get(0).getSimpleData()) {
					data.put(d.getName(), d.getValue());
				}
				point.setId(mapper.getId(data));
				point.setAreaId(mapper.getArea(data));
				point.setCostZoneId(mapper.getCostZone(data));

				List<double[]> positions = new ArrayList<double[]>();
				Polygon polygon = (Polygon) pm.getGeometry();
				List<Coordinate> coordinates = polygon.getOuterBoundaryIs().getLinearRing().getCoordinates();
				for (Coordinate c : coordinates) {
					double[] position = new double[] { c.getLatitude(), c.getLongitude() };
					positions.add(position);
				}
				point.setLocation(extractLocation(positions));
				res.add(point);
			}
		}
		return res;
	}

	private double[] extractLocation(List<double[]> positions) {
		// centroid
		double sumx = 0, sumy = 0;
		for (double[] p : positions) {
			sumx += p[0];
			sumy += p[1];
		}
		return new double[] { sumx / positions.size(), sumy / positions.size() };
	}

}
