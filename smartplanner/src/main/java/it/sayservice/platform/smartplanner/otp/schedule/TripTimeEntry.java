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

package it.sayservice.platform.smartplanner.otp.schedule;

import it.sayservice.platform.smartplanner.otp.schedule.sorter.Bucket;

public class TripTimeEntry implements Comparable<TripTimeEntry> {

	private String tripId;
	private String time;
	private String stopId;
	private int sequence;

	@Override
	public String toString() {
		return "{" + time + "," + tripId + "," + stopId + "," + sequence + "}";
	}

	public String getTripId() {
		return tripId;
	}

	public void setTripId(String tripId) {
		this.tripId = tripId;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getStopId() {
		return stopId;
	}

	public void setStopId(String stopId) {
		this.stopId = stopId;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public Bucket toBucket() {
		Bucket b = new Bucket();
		b.setId(stopId);
		b.setOrder(sequence);
		return b;
	}

	@Override
	public int compareTo(TripTimeEntry o) {
		return sequence - o.sequence;
	}

}
