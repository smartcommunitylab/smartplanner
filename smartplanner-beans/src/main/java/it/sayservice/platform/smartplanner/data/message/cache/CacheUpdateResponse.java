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

package it.sayservice.platform.smartplanner.data.message.cache;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CacheUpdateResponse {

	private List<String> added;
	private List<String> removed;
	private long version;
	private Map<String, CompressedCalendar> calendars;

	public CacheUpdateResponse() {
		added = new ArrayList<String>();
		removed = new ArrayList<String>();
		calendars = new TreeMap<String, CompressedCalendar>();
	}

	public List<String> getAdded() {
		return added;
	}

	public void setAdded(List<String> added) {
		this.added = added;
	}

	public List<String> getRemoved() {
		return removed;
	}

	public void setRemoved(List<String> removed) {
		this.removed = removed;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Map<String, CompressedCalendar> getCalendars() {
		return calendars;
	}

	public void setCalendars(Map<String, CompressedCalendar> calendar) {
		this.calendars = calendar;
	}

	@Override
	public String toString() {
		return "V" + version + ": " + calendars + "," + added + "," + removed;
	}
	
}
