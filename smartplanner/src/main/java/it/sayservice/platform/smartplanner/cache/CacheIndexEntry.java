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

public class CacheIndexEntry {

	private String id;
	private long version;
	private CacheEntryStatus status;

	public CacheIndexEntry() {
	}

	public CacheIndexEntry(String id, long version, CacheEntryStatus status) {
		this.id = id;
		this.version = version;
		this.status = status;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public CacheEntryStatus getStatus() {
		return status;
	}

	public void setStatus(CacheEntryStatus status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return id + ":" + version + "," + status;
	}

}
