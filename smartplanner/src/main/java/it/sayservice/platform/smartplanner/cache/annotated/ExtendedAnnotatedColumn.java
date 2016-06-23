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

package it.sayservice.platform.smartplanner.cache.annotated;

public class ExtendedAnnotatedColumn extends AnnotatedColumn {

	private AnnotatedTimetable source;

	public ExtendedAnnotatedColumn(AnnotatedColumn column) {
		this.days = column.days;
		this.symbolicRouteId = column.symbolicRouteId;
		this.routeId = column.routeId;
		this.serviceId = column.serviceId;
		this.times = column.times;
		this.tripId = column.tripId;
	}

	public AnnotatedTimetable getSource() {
		return source;
	}

	public void setSource(AnnotatedTimetable source) {
		this.source = source;
	}

}
