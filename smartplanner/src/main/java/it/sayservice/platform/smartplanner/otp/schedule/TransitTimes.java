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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import it.sayservice.platform.smartplanner.otp.schedule.sorter.Bucket;
import it.sayservice.platform.smartplanner.otp.schedule.sorter.BucketSet;

public class TransitTimes {

	private String routeId;
	private List<String> stopIds;
	private List<TripTimes> times;

	public TransitTimes() {
		stopIds = new ArrayList<String>();
		times = new ArrayList<TripTimes>();
	}

	public String getRouteId() {
		return routeId;
	}

	public void setRouteId(String routeId) {
		this.routeId = routeId;
	}

	public List<String> getStopIds() {
		return stopIds;
	}

	public void setStopIds(List<String> stopIds) {
		this.stopIds = stopIds;
	}

	public List<TripTimes> getTimes() {
		return times;
	}

	public void setTimes(ArrayList<TripTimes> times) {
		this.times = times;
	}

	public void buildStopIds() {
		BucketSet mergedBs = new BucketSet();
		Multimap<String, TripTimes> map = ArrayListMultimap.create();
		for (TripTimes tt : times) {
			map.put(tt.getRecurrence(), tt);
		}

		List<BucketSet> bss = new ArrayList<BucketSet>();
		BucketSet bs;
		for (String key : map.keySet()) {
			bs = new BucketSet();
			List<TripTimes> ttl = (List<TripTimes>) map.get(key);

			Collections.sort(ttl, new Comparator<TripTimes>() {
				@Override
				public int compare(TripTimes o1, TripTimes o2) {
					return o2.getTripTimes().size() - o1.getTripTimes().size();
				}
			});

			for (TripTimes tt : ttl) {
				List<Bucket> buckets = new ArrayList<Bucket>();
				for (TripTimeEntry sch : tt.getTripTimes()) {
					Bucket b = sch.toBucket();
					buckets.add(b);
				}
				bs.merge(buckets);
			}
			bss.add(bs);
		}

		for (BucketSet bs0 : bss) {
			mergedBs.merge(bs0.getBuckets());
		}

		stopIds = mergedBs.getIds();
	}

	@Override
	public String toString() {
		// return "{" + routeId + " => " + schedules + "}";
		return "{" + routeId + " => " + stopIds + "\n" + times + "}";
	}

}
