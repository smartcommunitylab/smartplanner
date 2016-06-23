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

import java.util.ArrayList;
import java.util.List;

public class PolylineEncoder {

	private static StringBuffer encodeSignedNumber(int num) {
		int sgn_num = num << 1;
		if (num < 0) {
			sgn_num = ~(sgn_num);
		}
		return (encodeNumber(sgn_num));
	}

	private static StringBuffer encodeNumber(int num) {
		StringBuffer encodeString = new StringBuffer();
		while (num >= 0x20) {
			int nextValue = (0x20 | (num & 0x1f)) + 63;
			encodeString.append((char) (nextValue));
			num >>= 5;
		}
		num += 63;
		encodeString.append((char) (num));
		return encodeString;
	}

	public static String encode(List<Location> polyline) {
		StringBuffer encodedPoints = new StringBuffer();
		int prev_lat = 0, prev_lng = 0;
		for (Location trackpoint : polyline) {
			int lat = (int) (trackpoint.getLatitude() * 1E5);
			int lng = (int) (trackpoint.getLongitude() * 1E5);
			encodedPoints.append(encodeSignedNumber(lat - prev_lat));
			encodedPoints.append(encodeSignedNumber(lng - prev_lng));
			prev_lat = lat;
			prev_lng = lng;
		}
		return encodedPoints.toString();
	}

	public static ArrayList<Location> decode(String encodedString, double d) {
		ArrayList<Location> polyline = new ArrayList<Location>();
		int index = 0;
		int len = encodedString.length();
		double lat = 0, lng = 0;

		while (index < len) {
			int b, shift = 0, result = 0;
			do {
				b = encodedString.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lat += dlat;

			shift = 0;
			result = 0;
			do {
				b = encodedString.charAt(index++) - 63;
				result |= (b & 0x1f) << shift;
				shift += 5;
			} while (b >= 0x20);
			int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
			lng += dlng;

			Location p = new Location(lat * d, lng * d);
			polyline.add(p);
		}

		return polyline;
	}

}