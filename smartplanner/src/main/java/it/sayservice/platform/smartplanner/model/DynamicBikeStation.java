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

package it.sayservice.platform.smartplanner.model;

import it.sayservice.platform.smartplanner.data.message.alerts.CreatorType;

public class DynamicBikeStation {

	private String agencyId;
	private String id;
	private int posts;
	private int bikes;
	private long duration;
	private String creatorId;
	private CreatorType creatorType;

	public DynamicBikeStation() {
	}

	public DynamicBikeStation(String agencyId, String id, int posts, int bikes, long duration, String creatorId,
			CreatorType creatorType) {
		super();
		this.agencyId = agencyId;
		this.id = id;
		this.posts = posts;
		this.bikes = bikes;
		this.duration = duration;
		this.creatorId = creatorId;
		this.creatorType = creatorType;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getPosts() {
		return posts;
	}

	public void setPosts(int posts) {
		this.posts = posts;
	}

	public int getBikes() {
		return bikes;
	}

	public void setBikes(int bikes) {
		this.bikes = bikes;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(String creatorId) {
		this.creatorId = creatorId;
	}

	public CreatorType getCreatorType() {
		return creatorType;
	}

	public void setCreatorType(CreatorType creatorType) {
		this.creatorType = creatorType;
	}

	@Override
	public String toString() {
		return "DynamicBikeStation [agencyId=" + agencyId + ", id=" + id + ", posts=" + posts + ", bikes=" + bikes
				+ ", duration=" + duration + "]";
	}
}
