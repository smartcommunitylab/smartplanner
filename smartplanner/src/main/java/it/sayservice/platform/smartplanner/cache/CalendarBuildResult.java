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

package it.sayservice.platform.smartplanner.cache;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.Multimap;

public class CalendarBuildResult {

	Multimap<String, String> calendar;
	Set<String> empty;

	public CalendarBuildResult() {
		empty = new TreeSet<String>();
	}

	public Multimap<String, String> getCalendar() {
		return calendar;
	}

	public void setCalendar(Multimap<String, String> calendar) {
		this.calendar = calendar;
	}

	public Set<String> getEmpty() {
		return empty;
	}

	public void setEmpty(Set<String> empty) {
		this.empty = empty;
	}

}
