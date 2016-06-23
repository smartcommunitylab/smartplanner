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

package it.sayservice.platform.smartplanner.otp;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import it.sayservice.platform.smartplanner.data.message.otpbeans.ExtendedTransitTimeTable;

@Component
public class OTPCache {

	@Autowired
	private OTPManager manager;

	private LoadingCache<String, ExtendedTransitTimeTable> cache;

	public OTPCache() {
		cache = CacheBuilder.newBuilder().maximumSize(1000).expireAfterAccess(24, TimeUnit.HOURS)
				.build(new CacheLoader<String, ExtendedTransitTimeTable>() {
					@Override
					public ExtendedTransitTimeTable load(String key) throws Exception {
						String pars[] = keyToParams(key);
						ExtendedTransitTimeTable ett = manager.buildTransitSchedule(pars[0], pars[1], pars[2],
								Long.parseLong(pars[3]), Long.parseLong(pars[4]), TransitScheduleResults.ALL, true,
								true);
						return ett;
					}

				});

	}

	public final ExtendedTransitTimeTable getTransitSchedule(final String router, final String agencyId,
			final String routeId, final Long from, final Long to, TransitScheduleResults filter, boolean tripsIds,
			boolean annotated) throws ExecutionException {

		Calendar c = new GregorianCalendar();
		c.setTimeInMillis(from);
		c.set(Calendar.HOUR, 23);
		c.set(Calendar.MINUTE, 59);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long newTo = Math.min(to, c.getTimeInMillis());

		c = new GregorianCalendar();
		c.setTimeInMillis(from);
		c.set(Calendar.HOUR, 0);
		c.set(Calendar.MINUTE, 1);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		long newFrom = c.getTimeInMillis();

		String key = router + "#" + agencyId + "#" + routeId + "#" + newFrom + "#" + newTo;
		ExtendedTransitTimeTable ett = cache.get(key);

		ett = finishTable(router, ett, newFrom, filter, tripsIds, annotated);
		return ett;
	}

	private String[] keyToParams(String key) {
		return key.split("#");
	}

	private ExtendedTransitTimeTable finishTable(String router, ExtendedTransitTimeTable ett, long from,
			TransitScheduleResults filter, boolean tripsIds, boolean annotated) {
		ExtendedTransitTimeTable newEtt = new ExtendedTransitTimeTable(ett);

		if (filter.equals(TransitScheduleResults.ALL) || filter.equals(TransitScheduleResults.DELAYS)) {
			manager.writeDelays(router, newEtt, from);
		}
		if (filter.equals(TransitScheduleResults.DELAYS) || tripsIds == false) {
			newEtt.setTripIds(null);
		}
		if (filter.equals(TransitScheduleResults.DELAYS)) {
			newEtt.setStops(new ArrayList<String>());
			newEtt.setStopsId(new ArrayList<String>());
			newEtt.setTimes(new ArrayList<List<List<String>>>());
		}
		if (!annotated) {
			newEtt.setShortDescription(null);
			newEtt.setLongDescription(null);
			newEtt.setFrequency(null);
			newEtt.setInvisibles(null);
			newEtt.setLine(null);
			newEtt.setSchedule(null);
			newEtt.setValidity(null);
			newEtt.setRoutesIds(null);
		}

		return newEtt;
	}

}
